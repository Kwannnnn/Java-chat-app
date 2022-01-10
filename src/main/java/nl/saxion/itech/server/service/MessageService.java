package nl.saxion.itech.server.service;

import nl.saxion.itech.server.data.DataObject;
import nl.saxion.itech.server.exception.ClientDisconnectedException;
import nl.saxion.itech.server.message.Message;
import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.model.ClientStatus;
import nl.saxion.itech.server.model.File;
import nl.saxion.itech.server.model.Group;
import nl.saxion.itech.server.thread.ClientPingTask;
import nl.saxion.itech.server.util.Logger;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static nl.saxion.itech.server.util.ServerMessageDictionary.*;
import static nl.saxion.itech.server.util.ServerMessageDictionary.missingParametersError;
import static nl.saxion.itech.shared.ProtocolConstants.*;

/**
 * This service reads lines of input representing messages from the client,
 * processes those messages and sends back a response to the client.
 */
public class MessageService implements Service {
    private final DataObject data;
    private Logger logger;

    public MessageService(DataObject data) {
        this.data = data;
    }

    @Override
    public void serve(InputStream in, OutputStream out) {
        this.logger = Logger.getInstance();

        var client = new Client(in, out);
        try {
            sendMessage(welcome(), client);
            handleClient(client);
        } catch (IOException e) {
            // Proceed to finally clause
        } finally {
            // Make sure remove the client from the data if he is connected
            // and close the socket if it hasn't been closed yet
            this.data.removeClient(client);
        }
    }

    private void startClientPingThread(Client client) {
        new Timer().scheduleAtFixedRate(new ClientPingTask(client), 0, PING_INITIAL_DELAY_MS);
    }

    private void handleClient(Client client) throws IOException {
        var in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        String line;
        while ((line = in.readLine()) != null) {
            // Log the input
            log(">> [" + client + "] " + line);

            try {
                var response = Optional.ofNullable(handleMessage(line, client));
                if (response.isPresent()) {
                    var message = response.get();
                    sendMessage(message, client);
                }
            } catch (ClientDisconnectedException e) {
                // Client has decided to disconnect, stop reading input
                break;
            }
        }
    }

    private Message handleMessage(String message, Client sender) throws ClientDisconnectedException {
        var payload = new StringTokenizer(message);

        try {
            return sender.getStatus() == ClientStatus.CLIENT_CONNECTED ?
                    handleConnectedUser(payload, sender) :
                    handleUnknownUser(payload, sender);
        } catch (NoSuchElementException e) {
            return sender.getStatus() == ClientStatus.CLIENT_CONNECTED
                    ? missingParametersError()
                    : unknownCommandError();
        }
    }

    private Message handleUnknownUser(StringTokenizer payload, Client sender) {
        var header = payload.nextToken().toUpperCase();

        if (!header.equals(CMD_CONN)) {
            return pleaseLoginFirstError();
        }

        try {
            var username = payload.nextToken();
            return handleConnectMessage(username, sender);
        } catch (NoSuchElementException e) {
            return missingParametersError();
        }
    }

    private Message handleConnectedUser(StringTokenizer payload, Client sender)
            throws ClientDisconnectedException {
        var header = payload.nextToken().toUpperCase();

        return switch (header) {
            case CMD_DSCN -> handleDisconnectMessage(sender);
            case CMD_BCST -> handleBroadcast(payload, sender);
            case CMD_PONG -> handlePong(sender);
            case CMD_MSG -> handleDirectMessage(payload, sender);
            case CMD_ALL -> handleAllMessage();
            case CMD_GRP -> handleGroupMessage(payload, sender);
            case CMD_FILE -> handleFileTransfer(payload, sender);
            default -> unknownCommandError();
        };
    }

    private Message handleFileTransfer(StringTokenizer payload, Client sender) {
        var header = payload.nextToken().toUpperCase();

        return switch (header) {
            case CMD_REQ -> handleFileReqMessage(payload, sender);
            case CMD_ACK -> handleFileAckMessage(payload, sender);
            default -> unknownCommandError();
        };
    }

    private Message handleFileReqMessage(StringTokenizer payload, Client sender) {
        var filename = payload.nextToken();
        var fileSize = Integer.parseInt(payload.nextToken());
        var recipientUsername = payload.nextToken();
        var client = this.data.getClient(recipientUsername);

        if (client.isEmpty()) {
            return recipientNotConnectedError();
        }

        var recipient = client.get();
        var file = new File(filename, sender, recipient, fileSize);
        this.data.addFile(file);
        sendMessage(fileReq(file.getId(), sender.getUsername(), filename, fileSize), recipient);

        return okFileReq(file.getId(), filename, fileSize, recipientUsername);
    }

    private Message handleFileAckMessage(StringTokenizer payload, Client sender) {
        var choice = payload.nextToken();
        var fileId = payload.nextToken();
        var file = this.data.getFile(fileId);

        //error handling
        boolean correctChoice = choice.equalsIgnoreCase(CMD_ACCEPT) || choice.equalsIgnoreCase(CMD_DENY);
        if (file.isEmpty() || !correctChoice) {
            return unknownTransfer();
        }

        var error = userIsNotRecipient(file.get(), sender);

        return error.orElseGet(() -> switch (choice) {
            case CMD_ACCEPT -> handleFileAckAcceptMessage(file.get(), sender);
            case CMD_DENY -> handleFileAckDenyMessage(file.get(), sender);
            default -> throw new RuntimeException();
        });
    }

    private Message handleFileAckDenyMessage(File file, Client messageSender) {
        String fileId = file.getId();
        sendMessage(okFileAckDeny(fileId), messageSender);

        return fileAckDeny(fileId);
    }

    private Message handleFileAckAcceptMessage(File file, Client messageSender) {
        String fileId = file.getId();
        sendMessage(okFileAckAccept(fileId), messageSender);
        sendMessage(fileAckAccept(fileId), file.getSender());

        // send file transfer message to both users
        sendMessage(fileTrUpload(fileId, 1338), file.getSender());
        return fileTrDownload(fileId, 1338);
    }

    private Message handleConnectMessage(String username, Client sender) {
        var error = usernameIsNotValid(username)
                .or(() -> userIsAlreadyLoggedIn(username));

        if (error.isPresent()) {
            return error.get();
        }

        sender.setUsername(username);
        sender.setStatus(ClientStatus.CLIENT_CONNECTED);
        this.data.addClient(sender);
        startClientPingThread(sender);

        return okConn(username);
    }

    private Message handleDisconnectMessage(Client client) throws ClientDisconnectedException {
        sendMessage(okDscn(), client);
        throw new ClientDisconnectedException();
    }

    private Message handlePong(Client sender) {
        sender.setReceivedPong(true);
        sender.updateLastPong();

        return null;
    }

    private Message handleDirectMessage(StringTokenizer tokenizer, Client sender) {
        var recipientUsername = tokenizer.nextToken();
        var message = getRemainingTokens(tokenizer);
        var recipient = this.data.getClient(recipientUsername);

        if (recipient.isEmpty()) {
            return recipientNotConnectedError();
        }

        sendMessage(msg(sender.getUsername(), message), recipient.get());
        return okMsg(recipientUsername, message);
    }

    private Message handleAllMessage() {
        var clients = this.data.getAllClients()
                .stream()
                .map(Client::getUsername)
                .collect(Collectors.joining(","));

        return okAll(clients);
    }

    private Message handleBroadcast(StringTokenizer tokenizer, Client sender) {
        var messageToBroadcast = getRemainingTokens(tokenizer);

        broadcastMessage(bcst(sender.getUsername(), messageToBroadcast), sender);
        return okBcst(messageToBroadcast);
    }

    private Message handleGroupMessage(StringTokenizer tokenizer, Client sender) {
        var header = tokenizer.nextToken().toUpperCase();

        return switch (header) {
            case CMD_NEW -> handleGroupNewMessage(tokenizer);
            case CMD_ALL -> handleGroupAllMessage();
            case CMD_JOIN -> handleGroupJoinMessage(tokenizer, sender);
            case CMD_MSG -> handleGroupMessageMessage(tokenizer, sender);
            case CMD_DSCN -> handleGroupDisconnectMessage(tokenizer, sender);
            default -> unknownCommandError();
        };
    }

    private Message handleGroupDisconnectMessage(StringTokenizer tokenizer, Client sender) {
        var groupName = tokenizer.nextToken();
        var senderUsername = sender.getUsername();

        // error handling
        var error = userNotMemberOfGroup(groupName, senderUsername)
                .or(() -> groupDoesNotExist(groupName));
        if (error.isPresent()) {
            // An error message has occurred
            return error.get();
        }

        var group = this.data.getGroup(groupName);

        assert group.isPresent() : "This group is not present, but it should be";

        group.get().removeClient(senderUsername);
        return okGrpDscn(groupName);
    }

    private Message handleGroupMessageMessage(StringTokenizer tokenizer, Client sender) {
        var groupName = tokenizer.nextToken();
        var message = getRemainingTokens(tokenizer);
        var senderUsername = sender.getUsername();

        // error handling
        var error = userNotMemberOfGroup(groupName, senderUsername)
                .or(() -> groupDoesNotExist(groupName));
        if (error.isPresent()) {
            // An error message has occurred
            return error.get();
        }

        var group = this.data.getGroup(groupName);

        assert group.isPresent() : "This group is not present, but it should be";

        sendMessageToAll(grpMsg(groupName, senderUsername, message),
                group.get().getClients());
        group.get().updateTimestampOfClient(senderUsername);
        return okGrpMsg(groupName, message);
    }

    private Message handleGroupJoinMessage(StringTokenizer tokenizer, Client sender) {
        var groupName = tokenizer.nextToken();

        //error handling
        var error = userAlreadyMemberOfGroup(groupName, sender.getUsername())
                .or(() -> groupDoesNotExist(groupName));

        if (error.isPresent()) {
            // An error message has occurred
            return error.get();
        }

        var group = this.data.getGroup(groupName);

        assert group.isPresent() : "This group is not present, but it should be";

        sendMessageToAll(
                grpJoin(groupName, sender.getUsername()),
                group.get().getClients());
        group.get().addClient(sender);
        return okGrpJoin(groupName);
    }

    private Message handleGroupAllMessage() {
        var clients = this.data.getAllGroups()
                .stream()
                .map(Group::getName)
                .collect(Collectors.joining(","));

        return okGrpAll(clients);
    }

    private Message handleGroupNewMessage(StringTokenizer tokenizer) {
        var groupName = tokenizer.nextToken();
        var error = invalidGroupName(groupName)
                .or(() -> groupAlreadyExists(groupName));

        if (error.isPresent()) {
            // An error message has occurred
            return error.get();
        }

        this.data.addGroup(new Group(groupName));
        return okGrpNew(groupName);
    }

    private Optional<Message> userIsNotRecipient(File file, Client client) {
        return !clientIsRecipientOfFile(client, file)
                ? Optional.of(unknownTransfer()) // Unknown transfer
                : Optional.empty();
    }

    private Optional<Message> userNotMemberOfGroup(String groupName, String username) {
        return !groupHasClient(groupName, username)
                ? Optional.of(notMemberOfGroupError()) // Not member of group
                : Optional.empty();
    }

    private Optional<Message> userAlreadyMemberOfGroup(String groupName, String username) {
        return groupHasClient(groupName, username)
                ? Optional.of(alreadyMemberOfGroupError()) // Already member of group
                : Optional.empty();
    }

    private Optional<Message> groupDoesNotExist(String groupName) {
        return !groupWithNameExists(groupName)
                ? Optional.of(groupDoesNotExistError()) // Group does not exist
                : Optional.empty();
    }

    private Optional<Message> groupAlreadyExists(String groupName) {
        return groupWithNameExists(groupName)
                ? Optional.of(groupAlreadyExistsError()) // Group already exists
                : Optional.empty();
    }

    private Optional<Message> invalidGroupName(String groupName) {
        return !isValidGroupName(groupName)
                ? Optional.of(invalidGroupNameError()) // Invalid group name
                : Optional.empty();
    }

    /**
     * A guard to check whether a username complies to the protocol format.
     *
     * @param username the username to check
     * @return an Optional with error message, in case the username is not
     * valid, or an empty Optional in case the username is valid.
     */
    private Optional<Message> usernameIsNotValid(String username) {
        return !isValidUsername(username)
                ? Optional.of(invalidUsernameError())
                : Optional.empty();
    }

    /**
     * A guard to check a user with a certain username is already logged in.
     *
     * @param username the user to check
     * @return an Optional with error message, in case user is already logged
     * in, or an empty Optional in case the user is not logged in.
     */
    private Optional<Message> userIsAlreadyLoggedIn(String username) {
        return isLoggedIn(username)
                ? Optional.of(userAlreadyLoggedInError())
                : Optional.empty();
    }

    /**
     * A helper function that checks the data whether a username
     * is already in use.
     */
    private boolean isLoggedIn(String username) {
        return this.data.hasClient(username);
    }

    /**
     * A helper function that checks the data whether a group name
     * is already in use.
     */
    private boolean groupWithNameExists(String groupName) {
        return this.data.hasGroup(groupName);
    }

    private boolean clientIsRecipientOfFile(Client client, File file) {
        return file.getRecipient().equals(client);
    }

    private boolean groupHasClient(String groupName, String username) {
        var group = this.data.getGroup(groupName);
        return group.map(value -> value.hasClient(username)).orElse(false);
    }

    /**
     * A helper function that broadcasts a certain message to every
     * connected client in the chat, except to the sender of the message.
     *
     * @param message the message to broadcast
     * @param sender  the sender of the message
     */
    private void broadcastMessage(Message message, Client sender) {
        var clientsToBroadcastTo = this.data.getAllClients()
                .stream()
                .filter(client -> !client.equals(sender))
                .collect(Collectors.toList());

        sendMessageToAll(message, clientsToBroadcastTo);
    }

    /**
     * Sends a message to all clients part of a collection.
     *
     * @param message the message to be sent
     * @param clients the clients to send the message to
     */
    private void sendMessageToAll(Message message, Collection<Client> clients) {
        for (var client : clients) {
            sendMessage(message, client);
        }
    }

    /**
     * Sends a message to a specific client.
     *
     * @param message the message to be sent
     * @param client  the client to send the message to
     */
    private void sendMessage(Message message, Client client) {
        var out = new PrintWriter(client.getOutputStream());
        out.println(message);
        out.flush();
        log("<< [" + client + "] " + message);
    }

    private String getRemainingTokens(StringTokenizer tokenizer) throws NoSuchElementException {
        var remainder = tokenizer.nextToken("");
        return remainder.trim();
    }

    /**
     * Logs some text on the server log output stream.
     *
     * @param text the text to be logged
     */
    private void log(String text) {
        if (!logger.isInitiated()) {
            return;
        }

        logger.logMessage(text);
    }
}
