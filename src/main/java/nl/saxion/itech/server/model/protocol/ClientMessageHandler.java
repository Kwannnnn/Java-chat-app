package nl.saxion.itech.server.model.protocol;

import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.threads.ServiceManager;
import static nl.saxion.itech.shared.ProtocolConstants.*;

import java.util.Optional;

public class ClientMessageHandler {
    private final ServiceManager serviceManager;

    public ClientMessageHandler(ServiceManager dispatcher) {
        this.serviceManager = dispatcher;
    }

    public void handle(String rawMessage, Client sender) {
        String[] splitMessage = parseMessage(rawMessage);
        String header = splitMessage[0];
        String body = splitMessage.length > 1 ? splitMessage[1] : " ";

        switch (header) {
            case CMD_CONN -> handleConnectMessage(body, sender);
            case CMD_DSCN -> handleDisconnectMessage(sender);
            case CMD_BCST -> handleBroadcast(body, sender);
            case CMD_PONG -> handlePong(sender);
            case CMD_MSG -> handleDirectMessage(body, sender);
            case CMD_ALL -> handleAllMessage(sender);
            case CMD_GRP -> handleGroupMessage(body, sender);
            default -> this.serviceManager.dispatchMessage(
                    new BaseMessage(CMD_ER00, ER00_BODY, sender));
        }
    }

    private void handleGroupMessage(String message, Client sender) {
        String[] splitMessage = parseMessage(message);
        String header = splitMessage[0];
        String body = splitMessage.length > 1 ? splitMessage[1] : " ";

        switch (header) {
            case CMD_ALL -> handleGroupAllMessage(sender);
            case CMD_NEW -> handleGroupNewMessage(body, sender);
            case CMD_JOIN -> handleGroupJoinMessage(body, sender);
            case CMD_MSG -> handleGroupMessageMessage(body, sender);
            case CMD_DSCN -> handleGroupDisconnectMessage(body, sender);
            default -> this.serviceManager.dispatchMessage(
                    new BaseMessage(CMD_ER00, ER00_BODY, sender));
        }
    }

    private void handleGroupDisconnectMessage(String message, Client sender) {
        String[] splitMessage = parseMessage(message);

        var error = getGroupDisconnectMessageError(splitMessage, sender);
        if (error.isPresent()) {
            // An error message has occurred
            dispatchMessage(error.get());
            return;
        }

        var groupName = splitMessage[0];

        this.serviceManager.removeClientFromGroup(groupName, sender.getUsername());

        // send confirmation message back to sender
        dispatchMessage(new BaseMessage(
                CMD_OK + " " + CMD_GRP + " " + CMD_DSCN,
                groupName,
                sender)
        );
    }

    private void handleGroupMessageMessage(String message, Client sender) {
        String[] splitMessage = parseMessage(message);

        var error = getGroupMessageMessageError(splitMessage, sender);
        if (error.isPresent()) {
            // An error message has occurred
            dispatchMessage(error.get());
            return;
        }

        var groupName = splitMessage[0];
        var groupMessage = splitMessage[1];

        // send message to other members of the group
        var memberList = this.serviceManager.getGroupMembers(groupName);

        for (var member : memberList) {
            if (!member.equals(sender)) {
                var m = new BaseMessage(
                        CMD_GRP + " " + CMD_MSG,
                        groupName + " " + groupMessage,
                        member);

                dispatchMessage(m);
            }
        }

        // update last message timestamp
        this.serviceManager.updateTimestampOfClientInGroup(groupName, sender.getUsername());

        // send confirmation message back to sender
        dispatchMessage(new BaseMessage(
                CMD_OK + " " + CMD_GRP + " " + CMD_MSG,
                groupName + " " + groupMessage,
                sender)
        );
    }

    private void handleGroupJoinMessage(String message, Client sender) {
        String[] splitMessage = parseMessage(message);

        var error = getGroupJoinMessageError(splitMessage, sender);
        if (error.isPresent()) {
            // An error message has occurred
            dispatchMessage(error.get());
            return;
        }

        var groupName = splitMessage[0];

        this.serviceManager.addClientToGroup(groupName, sender);

        // send notification to other members of the group
        var memberList = this.serviceManager.getGroupMembers(groupName);

        for (Client member : memberList) {
            if (!member.equals(sender)) {
                Message notification = new BaseMessage(
                        CMD_GRP + " " + CMD_JOIN,
                        groupName + " " + sender.getUsername(),
                        member);

                dispatchMessage(notification);
            }
        }

        // send confirmation message back to sender
        dispatchMessage(new BaseMessage(
                CMD_OK + " " + CMD_GRP + " " + CMD_JOIN,
                groupName,
                sender)
        );
    }

    private void handleGroupNewMessage(String message, Client sender) {
        var messageTokens = parseMessage(message);
        var groupName = messageTokens[0];

        var error = getGroupNewMessageError(groupName, sender);
        if (error.isPresent()) {
            // An error message has occurred
            dispatchMessage(error.get());
            return;
        }

        this.serviceManager.addGroup(groupName);

        // Send confirmation message back to sender
        dispatchMessage(new BaseMessage(
                CMD_OK + " " + CMD_GRP + " " + CMD_NEW,
                groupName,
                sender
        ));
    }

    private void handleGroupAllMessage(Client sender) {
        var error = senderIsNotLoggedIn(sender);

        if (error.isPresent()) {
            // An error message has occurred
            dispatchMessage(error.get());
            return;
        }

        var response = constructMessage(
                CMD_OK + " " + CMD_GRP + " " + CMD_ALL,
                this.serviceManager.getGroups(),
                sender
        );
        dispatchMessage(response);
    }

    private void handleAllMessage(Client sender) {
        var error = senderIsNotLoggedIn(sender);

        if (error.isPresent()) {
            // An error message has occurred
            dispatchMessage(error.get());
            return;
        }

        var response = constructMessage(
                CMD_OK + " " + CMD_ALL,
                this.serviceManager.getClients(),
                sender
        );
        dispatchMessage(response);
    }

    private void handleDirectMessage(String message, Client sender) {
        var splitMessage = parseMessage(message);

        var error = getDirectMessageError(splitMessage, sender);
        if (error.isPresent()) {
            // An error message has occurred
            dispatchMessage(error.get());
            return;
        }

        var recipientUsername = splitMessage[0];
        var body = splitMessage[1];
        var recipient = this.serviceManager.getClient(recipientUsername);
        var messageToRecipient = constructMessage(CMD_MSG + " " + sender.getUsername(), body, recipient);
        dispatchMessage(messageToRecipient);

        //send confirmation message back to sender
        var messageToSender = constructMessage(CMD_OK + " " + CMD_MSG + " " + recipient.getUsername(), body, sender);
        dispatchMessage(messageToSender);
    }

    private void handlePong(Client sender) {
        this.serviceManager.updateTimestampOfClient(sender.getUsername());
    }

    private void handleBroadcast(String messageToBroadcast, Client sender) {
        var error = senderIsNotLoggedIn(sender);

        if (error.isPresent()) {
            // An error message has occurred
            dispatchMessage(error.get());
            return;
        }

        broadcastMessage(messageToBroadcast, sender);
        dispatchMessage(new BaseMessage(
                CMD_OK + " " + CMD_BCST,
                messageToBroadcast,
                sender
        ));
    }

    private void handleDisconnectMessage(Client sender) {
        var response = new BaseMessage(
                CMD_OK + " " + CMD_DSCN,
                DSCN_BODY,
                sender
        );

        this.serviceManager.dispatchMessage(response);
        this.serviceManager.removeClient(sender.getUsername());

        //TODO: maybe this shouldn't be here
        this.serviceManager.closeConnection(sender);
    }

    private void handleConnectMessage(String username, Client sender) {
        var error = getConnectError(username, sender);

        if (error.isPresent()) {
            // An error message has occurred
            dispatchMessage(error.get());
            return;
        }

        sender.setUsername(username);
        this.serviceManager.addClient(sender);

        // Send confirmation back to client (OK CONN username)
        var message = constructMessage(CMD_OK + " " + CMD_CONN, username, sender);
        dispatchMessage(message);
    }

    private Optional<Message> getGroupDisconnectMessageError(String[] splitMessage, Client sender) {
        return senderIsNotLoggedIn(sender)
                .or(() -> missingParameters(splitMessage, 1, sender))
                .or(() -> notMemberOfGroup(splitMessage[0], sender));
    }

    private Optional<Message> getGroupMessageMessageError(String[] splitMessage, Client sender) {
        return senderIsNotLoggedIn(sender)
                .or(() -> missingParameters(splitMessage, 2, sender))
                .or(() -> groupDoesNotExist(splitMessage[0], sender))
                .or(() -> notMemberOfGroup(splitMessage[0], sender));
    }

    private Optional<Message> getGroupJoinMessageError(String[] splitMessage, Client sender) {
        return senderIsNotLoggedIn(sender)
                .or(() -> missingParameters(splitMessage, 0, sender)) // TODO: check if this error works
                .or(() -> invalidGroupName(splitMessage[0], sender))
                .or(() -> groupDoesNotExist(splitMessage[0], sender))
                .or(() -> clientAlreadyJoinedGroup(splitMessage[0], sender));
    }

    private Optional<Message> getGroupNewMessageError(String groupName, Client sender) {
        return senderIsNotLoggedIn(sender)
                .or(() -> invalidGroupName(groupName, sender))
                .or(() -> groupAlreadyExists(groupName, sender));
    }

    private Optional<Message> getDirectMessageError(String[] splitMessage, Client sender) {
        return senderIsNotLoggedIn(sender)
                .or(() -> missingParameters(splitMessage, 2, sender))
                .or(() -> noSuchUser(splitMessage[0], sender));
    }

    private Optional<Message> getConnectError(String username, Client sender) {
        return alreadyLoggedIn(sender)
                .or(() -> usernameIsNotValid(username, sender))
                .or(() -> userIsAlreadyLoggedIn(username, sender));
    }

    private Optional<Message> senderIsNotLoggedIn(Client sender) {
        return sender.getUsername() == null
                ? Optional.of(new BaseMessage(CMD_ER03, ER03_BODY, sender)) // Please log in first
                : Optional.empty();
    }

    private Optional<Message> alreadyLoggedIn(Client sender) {
        return sender.getUsername() != null
                ? Optional.of(new BaseMessage(CMD_ER66, ER66_BODY, sender)) // Please log in first
                : Optional.empty();
    }

    private Optional<Message> usernameIsNotValid(String username, Client sender) {
        return !isValidUsername(username)
                ? Optional.of(new BaseMessage(CMD_ER02, ER02_BODY, sender))
                : Optional.empty();
    }

    private Optional<Message> userIsAlreadyLoggedIn(String username, Client sender) {
        return isLoggedIn(username)
                ? Optional.of(new BaseMessage(CMD_ER01, ER01_BODY, sender))
                : Optional.empty();
    }

    private Optional<Message> noSuchUser(String recipientUsername, Client sender) {
        return !isLoggedIn(recipientUsername)
                ? Optional.of(new BaseMessage(CMD_ER04, ER04_BODY, sender)) // User is not connected
                : Optional.empty();
    }

    private Optional<Message> missingParameters(String[] tokens, int expectedParametersCount, Client sender) {
        return tokens.length < expectedParametersCount
                ? Optional.of(new BaseMessage(CMD_ER08, ER08_BODY, sender)) // Missing parameters
                : Optional.empty();
    }

    private Optional<Message> invalidGroupName(String groupName, Client sender) {
        return !isValidGroupName(groupName)
                ? Optional.of(new BaseMessage(CMD_ER05, ER05_BODY, sender)) // Invalid group name
                : Optional.empty();
    }

    private Optional<Message> groupAlreadyExists(String groupName, Client sender) {
        return groupWithNameExists(groupName)
                ? Optional.of(new BaseMessage(CMD_ER06, ER06_BODY, sender)) // Group already exists
                : Optional.empty();
    }

    private Optional<Message> groupDoesNotExist(String groupName, Client sender) {
        return !groupWithNameExists(groupName)
                ? Optional.of(new BaseMessage(CMD_ER07, ER07_BODY, sender)) // Group doesn't exist
                : Optional.empty();
    }

    private Optional<Message> clientAlreadyJoinedGroup(String groupName, Client sender) {
        return groupHasClient(groupName, sender.getUsername())
                ? Optional.of(new BaseMessage(CMD_ER09, ER09_BODY, sender)) // Already member of group
                : Optional.empty();
    }

    private Optional<Message> notMemberOfGroup(String groupName, Client sender) {
        return !groupHasClient(groupName, sender.getUsername())
                ? Optional.of(new BaseMessage(CMD_ER10, ER10_BODY, sender)) // Group doesn't exist
                : Optional.empty();
    }

    private boolean isValidGroupName(String groupName) {
        var pattern = "^[a-zA-Z0-9_]{3,14}$";
        return groupName.matches(pattern);
    }

    private boolean isLoggedIn(String username) {
        return this.serviceManager.hasClient(username);
    }

    private boolean groupWithNameExists(String groupName) {
        return this.serviceManager.hasGroup(groupName);
    }

    private boolean groupHasClient(String groupName, String username) {
        return this.serviceManager.groupHasClient(groupName, username);
    }

    private boolean isValidUsername(String username) {
        var pattern = "^[a-zA-Z0-9_]{3,14}$";
        return username.matches(pattern);
    }

    private String[] parseMessage(String message) {
        return message.split(" ", 2);
    }

    private void dispatchMessage(Message message) {
        this.serviceManager.dispatchMessage(message);
    }

    private void broadcastMessage(String messageToBroadcast, Client sender) {
        this.serviceManager.broadcastMessage(new BaseMessage(
                CMD_BCST + " " + sender.getUsername(),
                messageToBroadcast
        ));
    }

    private Message constructMessage(String header, String body, Client client) {
        return new BaseMessage(header, body, client);
    }
}
