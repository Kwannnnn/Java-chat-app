package nl.saxion.itech.server.service;

import nl.saxion.itech.server.data.DataObject;
import nl.saxion.itech.server.exception.ClientDisconnectedException;
import nl.saxion.itech.server.message.Message;
import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.model.ClientStatus;
import nl.saxion.itech.server.model.FileObject;
import nl.saxion.itech.server.model.Group;
import nl.saxion.itech.server.util.Logger;
import nl.saxion.itech.server.util.ServerMessageDictionary;
import nl.saxion.itech.shared.security.util.HashUtil;

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
            handleIncomingMessages(client);
        } catch (IOException e) {
            // Proceed to finally clause
        }  catch (ClientDisconnectedException e) {
            // Client has decided to disconnect, stop reading input;
        } finally {
            // Make sure remove the client from the data if he is connected
            // and close the socket if it hasn't been closed yet
            this.data.removeClient(client);

        }
    }

    private void handleIncomingMessages(Client client) throws IOException, ClientDisconnectedException {
        var in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        String line;
        while ((line = in.readLine()) != null) {
            // Log the input
            String clientUsername = this.data.userIsAuthenticated(client.getUsername()) ? "*" + client.getUsername() : client.getUsername();
            log(">> [" + clientUsername + "] " + line);

            var response = Optional.ofNullable(handleClient(line, client));
            if (response.isPresent()) {
                var message = response.get();
                sendMessage(message, client);
            }
        }
    }

    private Message handleClient(String message, Client sender) throws ClientDisconnectedException {
        var payload = new StringTokenizer(message);

        try {
            return sender.getStatus() == ClientStatus.CLIENT_CONNECTED
//                    || sender.getStatus() == ClientStatus.CLIENT_AUTHENTICATED
                    ? handleConnectedClient(payload, sender)
                    : handleUnknownClient(payload, sender);
        } catch (NoSuchElementException e) {
            return unknownCommandError(); // TODO: test empty message
        }
    }

    private Message handleUnknownClient(StringTokenizer payload, Client sender) {
        var header = payload.nextToken().toUpperCase();

        if (!header.equals(CMD_CONN)) {
            return pleaseLoginFirstError();
        }

        try {
            return handleConnectMessage(payload, sender);
        } catch (NoSuchElementException e) {
            return missingParametersError();
        }
    }

    private Message handleConnectedClient(StringTokenizer payload, Client sender)
            throws ClientDisconnectedException {
        try {
            var header = payload.nextToken().toUpperCase();

            return switch (header) {
                case CMD_DSCN -> handleDisconnectMessage(sender);
                case CMD_BCST -> handleBroadcast(payload, sender);
                case CMD_PONG -> handlePong(sender);
                case CMD_AUTH -> handleAuth(payload, sender);
                case CMD_MSG -> handleDirectMessage(payload, sender);
                case CMD_ALL -> handleAllMessage();
                case CMD_GRP -> handleGroupMessage(payload, sender);
                case CMD_FILE -> handleFileMessage(payload, sender);
                case CMD_PUBK -> handlePubkMessage(payload);
                case CMD_SESSION -> handleSessionMessage(payload, sender);
                default -> unknownCommandError();
            };
        } catch (NoSuchElementException e) {
            return missingParametersError();
        }
    }

    private Message handleAuth(StringTokenizer payload, Client sender) {
        var password = payload.nextToken();

        var error = userNotAuthenticated(sender).or(() -> {
            var authenticatedUser = this.data.getAuthenticatedUsers().get(sender.getUsername());
            String passwordHash = HashUtil.generateHash(authenticatedUser.getSalt(), password);
            return passwordMismatch(passwordHash, authenticatedUser.getPasswordHash());
        });

        return error.orElseGet(ServerMessageDictionary::okAuth);
    }

    private Message handleSessionMessage(StringTokenizer payload, Client sender) {
        var username = payload.nextToken();
        var sessionKey = payload.nextToken();

        var client = this.data.getClient(username);

        if (client.isEmpty()) {
            return recipientNotConnectedError();
        }

        sendMessage(session(sender.getUsername(), sessionKey), client.get());
        return okSession(username, sessionKey);
    }

    private Message handlePubkMessage(StringTokenizer payload) {
        var username = payload.nextToken();
        var client = this.data.getClient(username);

        if (client.isEmpty()) {
            return recipientNotConnectedError();
        }

        return okPubk(username, client.get().getPublicKey());
    }

    //region file messages
    private Message handleFileMessage(StringTokenizer payload, Client sender) {
        var header = payload.nextToken().toUpperCase();

        return switch (header) {
            case CMD_REQ -> handleFileReqMessage(payload, sender);
            case CMD_ACK -> handleFileAckMessage(payload, sender);
            case CMD_TR -> handleFileTransferMessage(payload);
            default -> unknownCommandError();
        };
    }

    private Message handleFileTransferMessage(StringTokenizer payload) {
        var header = payload.nextToken().toUpperCase();

        boolean headerAccepted = header.equals(CMD_SUCCESS) || header.equals(CMD_FAIL);
        if (headerAccepted) {
            String fileID = payload.nextToken();
            var fileOptional = this.data.getFile(fileID);

            //error handling
            if (fileOptional.isEmpty()) {
                return unknownCommandError();
            }

            var fileObject = fileOptional.get();
            // TODO: think of a condition for this lol. For now this message is accepted everytime
//            if ( ) {
//                return fileNotSentError();
//            }

            //send message back to the file uploader, completing the transfer
            var messageToSend = header.equals(CMD_SUCCESS) ? fileTrSuccess(fileID) : fileTrFail(fileID);
            var fileSender = fileObject.getSender();
            sendMessage(messageToSend, fileSender);

            this.data.removeFile(fileID);
            return null;
        } else {
            return unknownCommandError();
        }
    }

    private Message handleFileReqMessage(StringTokenizer payload, Client sender) {
        var filename = payload.nextToken();
        var fileSize = Integer.parseInt(payload.nextToken());
        var checksum = payload.nextToken();
        var recipientUsername = payload.nextToken();
        var client = this.data.getClient(recipientUsername);

        if (client.isEmpty()) {
            return recipientNotConnectedError();
        }

        var recipient = client.get();
        var file = new FileObject(filename, sender, recipient);
        this.data.addFile(file);
        sendMessage(fileReq(file.getId(), sender.getUsername(), filename, fileSize, checksum), recipient);

        return okFileReq(file.getId(), filename, fileSize, recipientUsername, checksum);
    }

    //region file acknowledge messages
    //================================================================================
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

    private Message handleFileAckDenyMessage(FileObject fileObject, Client messageSender) {
        String fileId = fileObject.getId();
        sendMessage(okFileAckDeny(fileId), messageSender);

        return fileAckDeny(fileId);
    }

    private Message handleFileAckAcceptMessage(FileObject fileObject, Client messageSender) {
        String fileId = fileObject.getId();
        sendMessage(okFileAckAccept(fileId), messageSender);
        sendMessage(fileAckAccept(fileId), fileObject.getSender());

        // send file transfer message to both users
        // TODO: hard coded port number
        sendMessage(fileTrUpload(fileId, 1338), fileObject.getSender());
        return fileTrDownload(fileId, 1338);
    }
    //================================================================================
    //endregion

    //================================================================================
    //endregion

    //region other messages
    //================================================================================
    private Message handleConnectMessage(StringTokenizer tokenizer, Client sender) {
        var username = tokenizer.nextToken();
        var publicKey = tokenizer.nextToken();

        var error = usernameIsNotValid(username)
                .or(() -> userIsAlreadyLoggedIn(username));

        if (error.isPresent()) {
            return error.get();
        }

        sender.setUsername(username);
        sender.setStatus(ClientStatus.CLIENT_CONNECTED);
        sender.setPublicKey(publicKey);
        this.data.addClient(sender);

        return okConn(username, publicKey);
    }

    private Message handleDisconnectMessage(Client sender) throws ClientDisconnectedException {
        var allOtherClients = this.data.getAllClients()
                .stream()
                .filter(client -> !client.equals(sender)).toList();

        sendMessageToAll(dscn(sender.getUsername()), allOtherClients);

        sendMessage(okDscn(), sender);
        throw new ClientDisconnectedException();
    }

    private Message handlePong(Client sender) {
        sender.setReceivedPong(true);
        sender.updateLastPong();

        return null;
    }

    private Message handleDirectMessage(StringTokenizer payload, Client sender) {
        var recipientUsername = payload.nextToken();
        var message = getRemainingTokens(payload);
        var recipientOptional = this.data.getClient(recipientUsername);

        if (recipientOptional.isEmpty()) {
            return recipientNotConnectedError();
        }

        var recipient = recipientOptional.get();
        sendMessage(msg(sender.getUsername(), message), recipient);
        return okMsg(recipientUsername, message);
    }

    private Message handleAllMessage() {
        var clients = this.data.getAllClients()
                .stream()
                .map(Client::getUsername)
                .collect(Collectors.joining(","));

        return okAll(clients);
    }

    private Message handleBroadcast(StringTokenizer payload, Client sender) {
        var messageToBroadcast = getRemainingTokens(payload);

        broadcastMessage(bcst(sender.getUsername(), messageToBroadcast), sender);
        return okBcst(messageToBroadcast);
    }
    //================================================================================
    //endregion

    //region group messages
    //================================================================================
    private Message handleGroupMessage(StringTokenizer payload, Client sender) {
        var header = payload.nextToken().toUpperCase();

        return switch (header) {
            case CMD_NEW -> handleGroupNewMessage(payload, sender);
            case CMD_ALL -> handleGroupAllMessage();
            case CMD_JOIN -> handleGroupJoinMessage(payload, sender);
            case CMD_MSG -> handleGroupMessageMessage(payload, sender); // TODO: test
            case CMD_DSCN -> handleGroupDisconnectMessage(payload, sender);
            default -> unknownCommandError();
        };
    }

    private Message handleGroupDisconnectMessage(StringTokenizer payload, Client sender) {
        var groupName = payload.nextToken();
        var senderUsername = sender.getUsername();

        // error handling
        var error = userNotMemberOfGroup(groupName, senderUsername)
                .or(() -> groupDoesNotExist(groupName));
        if (error.isPresent()) {
            // An error message has occurred
            return error.get(); // TODO: test
        }

        var groupOptional = this.data.getGroup(groupName);

        assert groupOptional.isPresent() : "This group is not present, but it should be";

        var group = groupOptional.get();
        group.removeClient(senderUsername);

        if (group.getClients().size() == 0) {
            this.data.removeGroup(groupName);
        }

        return okGrpDscn(groupName);
    }

    private Message handleGroupMessageMessage(StringTokenizer payload, Client sender) {
        var groupName = payload.nextToken();
        var message = getRemainingTokens(payload);
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

    private Message handleGroupJoinMessage(StringTokenizer payload, Client sender) {
        var groupName = payload.nextToken();

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

    private Message handleGroupNewMessage(StringTokenizer payload, Client sender) {
        var groupName = payload.nextToken();
        var error = invalidGroupName(groupName)
                .or(() -> groupAlreadyExists(groupName));

        if (error.isPresent()) {
            // An error message has occurred
            return error.get();
        }

        var group = new Group(groupName);
        group.addClient(sender);
        this.data.addGroup(group);
        return okGrpNew(groupName);
    }
    //================================================================================
    //endregion

    //region helper methods
    //================================================================================
    private Optional<Message> userIsNotRecipient(FileObject fileObject, Client client) {
        return !clientIsRecipientOfFile(client, fileObject)
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

    private Optional<Message> userNotAuthenticated(Client client) {
        return !this.data.getAuthenticatedUsers().containsKey(client.getUsername())
                ? Optional.of(userNotAuthenticatedError()) // User not authenticated
                : Optional.empty();
    }

    private Optional<Message> passwordMismatch(String passwordHash1, String passwordHash2) {
        return !passwordHash1.equals(passwordHash2)
                ? Optional.of(passwordMismatchError()) // Password does not match
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

    private boolean clientIsRecipientOfFile(Client client, FileObject fileObject) {
        return fileObject.getRecipient().equals(client);
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

    private String getRemainingTokens(StringTokenizer payload) throws NoSuchElementException {
        var remainder = payload.nextToken("");
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
    //================================================================================
    //endregion
}
