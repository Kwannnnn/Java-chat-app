package nl.saxion.itech.server.model.protocol;

import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.model.Group;
import nl.saxion.itech.server.threads.GroupPingThread;
import nl.saxion.itech.server.threads.ServiceManager;
import static nl.saxion.itech.server.model.protocol.ProtocolConstants.*;

import java.io.IOException;
import java.util.stream.Collectors;

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

        Message error = getGroupDisconnectMessageError(splitMessage, sender);

        if (error == null) {
            String groupName = splitMessage[0];

            this.serviceManager.removeClientFromGroup(groupName, sender.getUsername());

            // send confirmation message back to sender
            this.serviceManager.dispatchMessage(new BaseMessage(
                    CMD_OK + " " + CMD_GRP + " " + CMD_DSCN,
                    groupName,
                    sender));
        } else {
            this.serviceManager.dispatchMessage(error);
        }
    }

    private Message getGroupDisconnectMessageError(String[] splitMessage, Client sender) {
        if (sender.getUsername() == null) {
            // Please login first
            return new BaseMessage(CMD_ER03, ER03_BODY, sender);
        }

        if (splitMessage.length == 0) {
            // Missing parameters
            return new BaseMessage(CMD_ER08,ER08_BODY, sender
            );
        }

        String groupName = splitMessage[0];

        if (!this.serviceManager.groupHasClient(groupName, sender.getUsername())) {
            // Client not part of group
            return new BaseMessage(CMD_ER10,ER10_BODY,sender);
        }

        return null;
    }

    private void handleGroupMessageMessage(String message, Client sender) {
        String[] splitMessage = parseMessage(message);

        Message error = getGroupMessageMessageError(splitMessage, sender);

        if (error == null) {
            String groupName = splitMessage[0];
            String groupMessage = splitMessage[1];

            // send message to other members of the group
            var memberList = this.serviceManager.getGroupMembers(groupName);

            for (Client member : memberList) {
                if (!member.equals(sender)) {
                    Message m = new BaseMessage(
                            CMD_GRP + " " + CMD_MSG,
                            groupName + " " + groupMessage,
                            member);

                    this.serviceManager.dispatchMessage(m);
                }
            }

            // update last message timestamp
            this.serviceManager.updateTimestampOfClientInGroup(groupName, sender.getUsername());

            // send confirmation message back to sender
            this.serviceManager.dispatchMessage(new BaseMessage(
                    CMD_OK + " " + CMD_GRP + " " + CMD_MSG,
                    groupName + " " + groupMessage,
                    sender));
        } else {
            this.serviceManager.dispatchMessage(error);
        }
    }

    private Message getGroupMessageMessageError(String[] splitMessage, Client sender) {
        if (sender.getUsername() == null) {
            // Please login first
            return new BaseMessage(CMD_ER03,ER03_BODY, sender);
        }

        if (splitMessage.length < 2) {
            // Missing parameters
            return new BaseMessage(CMD_ER08,ER08_BODY, sender
            );
        }

        String groupName = splitMessage[0];

        if (!this.serviceManager.hasGroup(groupName)) {
            // Group doesn't exist
            return new BaseMessage(CMD_ER07, ER07_BODY, sender);
        }

        if (!this.serviceManager.groupHasClient(groupName, sender.getUsername())) {
            // Client not part of group
            return new BaseMessage(CMD_ER10,ER10_BODY, sender);
        }

        return null;
    }

    private void handleGroupJoinMessage(String message, Client sender) {
        String[] splitMessage = parseMessage(message);

        Message error = getGroupJoinMessageError(splitMessage, sender);

        if (error == null) {
            String groupName = splitMessage[0];

            this.serviceManager.addClientToGroup(groupName, sender);

            // send notification to other members of the group
            var memberList = this.serviceManager.getGroupMembers(groupName);

            for (Client member : memberList) {
                if (!member.equals(sender)) {
                    Message notification = new BaseMessage(
                            CMD_GRP + " " + CMD_JOIN,
                            groupName + " " + sender.getUsername(),
                            member);

                    this.serviceManager.dispatchMessage(notification);
                }
            }

            // send confirmation message back to sender
            this.serviceManager.dispatchMessage(new BaseMessage(
                    CMD_OK + " " + CMD_GRP + " " + CMD_JOIN,
                    groupName,
                    sender));
        } else {
            this.serviceManager.dispatchMessage(error);
        }
    }

    private Message getGroupJoinMessageError(String[] splitMessage, Client sender) {
        if (sender.getUsername() == null) {
            // Please login first
            return new BaseMessage(CMD_ER03, ER03_BODY, sender);
        }

        if (splitMessage.length == 0) {
            // Missing parameters
            return new BaseMessage(CMD_ER08, ER08_BODY, sender);
        }

        String groupName = splitMessage[0];

        if (!isValidGroupName(groupName)) {
            // Invalid group name
            return new BaseMessage(CMD_ER05, ER05_BODY, sender);
        }

        if (!this.serviceManager.hasGroup(groupName)) {
            // Group doesn't exist
            return new BaseMessage(CMD_ER07, ER07_BODY, sender);
        }

        if (this.serviceManager.groupHasClient(groupName, sender.getUsername())) {
            // Client already joined group
            return new BaseMessage(CMD_ER09, ER09_BODY, sender);
        }

        return null;
    }

    private void handleGroupNewMessage(String message, Client sender) {
        String[] splitMessage = parseMessage(message);

        Message error = getGroupNewMessageError(splitMessage, sender);

        if (error == null) {
            String groupName = splitMessage[0];
            Group addedGroup = this.serviceManager.addGroup(groupName);
            //TODO: start new group ping thread
            new GroupPingThread(addedGroup, serviceManager).start();

            //send confirmation message back to sender
            this.serviceManager.dispatchMessage(new BaseMessage(
                    CMD_OK + " " + CMD_GRP + " " + CMD_NEW,
                    groupName,
                    sender
            ));
        } else {
            this.serviceManager.dispatchMessage(error);
        }
    }

    private Message getGroupNewMessageError(String[] splitMessage, Client sender) {
        if (sender.getUsername() == null) {
            // Please login first
            return new BaseMessage(CMD_ER03, ER03_BODY, sender);
        }

        String groupName = splitMessage[0];

        if (!isValidGroupName(groupName)) {
            // invalid group name
            return new BaseMessage(CMD_ER05, ER05_BODY, sender);
        }

        if (this.serviceManager.hasGroup(groupName)) {
            // group already exists
            return new BaseMessage(CMD_ER06, ER06_BODY, sender);
        }

        return null;
    }

    private void handleGroupAllMessage(Client sender) {
        if (sender.getUsername() == null) {
            // Please login first
            this.serviceManager.dispatchMessage(new BaseMessage(CMD_ER03, ER03_BODY, sender));
        }

        String listString = serviceManager.getGroups().stream().map(Group::getName).collect(Collectors.joining(","));

        this.serviceManager.dispatchMessage(new BaseMessage(
                CMD_OK + " " + CMD_GRP + " " + CMD_ALL,
                listString,
                sender));
    }

    private void handleAllMessage(Client sender) {
        if (sender.getUsername() == null) {
            // Please login first
            this.serviceManager.dispatchMessage(new BaseMessage(CMD_ER03, ER03_BODY, sender));
            return;
        }

        String listString = serviceManager.getClients().stream().map(Client::getUsername).collect(Collectors.joining(","));

        // send confirmation message back to sender
        this.serviceManager.dispatchMessage(new BaseMessage(
                CMD_OK + " " + CMD_ALL,
                listString,
                sender));
    }

    private void handleDirectMessage(String message, Client sender) {
        var splitMessage = parseMessage(message);

        Message error = getDirectMessageError(splitMessage, sender);

        if (error == null) {
            String recipientUsername = splitMessage[0];
            String body = splitMessage[1];

            Client recipient = this.serviceManager.getClient(recipientUsername);

            String messageToRecipient = CMD_MSG + " " + sender.getUsername() + body;
            sendPrivateMessage(messageToRecipient, recipient, sender);
        } else {
            this.serviceManager.dispatchMessage(error);
        }
    }

    private void sendPrivateMessage(String message, Client recipient, Client sender) {
        var messageToRecipient = new BaseMessage(
                CMD_MSG + " " + sender.getUsername(),
                message,
                recipient
        );
        this.serviceManager.dispatchMessage(messageToRecipient);

        //send confirmation message back to sender
        this.serviceManager.dispatchMessage(new BaseMessage(
                CMD_OK + " " + CMD_MSG + " " + recipient.getUsername(),
                message,
                sender
        ));
    }

    private Message getDirectMessageError(String[] splitMessage, Client sender) {
        if (sender.getUsername() == null) {
            // Please login first
            return new BaseMessage(CMD_ER03,ER03_BODY, sender);
        }

        if (splitMessage.length < 2) {
            // Missing parameters
            return new BaseMessage(CMD_ER08, ER08_BODY, sender);
        }

        String recipientUsername = splitMessage[0];

        if (!this.serviceManager.hasClient(recipientUsername)) {
            // User is not connected
            return new BaseMessage(CMD_ER04, ER04_BODY, sender);
        }

        return null;
    }

    private void handlePong(Client sender) {
        this.serviceManager.updateTimestampOfClient(sender.getUsername());
    }

    private void handleBroadcast(String messageToBroadcast, Client sender) {
        if (sender.getUsername() == null) {
            this.serviceManager.dispatchMessage(new BaseMessage(CMD_ER03, ER03_BODY, sender));
            return;
        }

        broadcastMessage(messageToBroadcast, sender);

        this.serviceManager.dispatchMessage(new BaseMessage(
                CMD_OK + " " + CMD_BCST,
                messageToBroadcast,
                sender
        ));
    }

    private void broadcastMessage(String messageToBroadcast, Client sender) {
        for (var client : this.serviceManager.getClients()) {
            if (!client.equals(sender)) {
                var broadcastMessage = new BaseMessage(
                        CMD_BCST + " " + sender.getUsername(),
                        messageToBroadcast,
                        client
                );
                this.serviceManager.dispatchMessage(broadcastMessage);
            }
        }
    }

    private void handleDisconnectMessage(Client sender) {
        var response = new BaseMessage(
                CMD_OK + " " + CMD_DSCN,
                DSCN_BODY,
                sender
        );

        this.serviceManager.dispatchMessage(response);
        this.serviceManager.removeClient(sender.getUsername());

        try {
            sender.getSocket().close();
        } catch (IOException e) {
            System.err.println("Client socket has already been closed.");
        }
    }

    private void handleConnectMessage(String username, Client sender) {
        var error = getConnectError(username, sender);

        if (error == null) {
            sender.setUsername(username);
            this.serviceManager.addClient(sender);

            //send confirmation back to client
            var message = new BaseMessage(
                    CMD_OK + " " + CMD_CONN,
                    username,
                    sender
            );
            this.serviceManager.dispatchMessage(message);
        } else {
            this.serviceManager.dispatchMessage(error);
        }
    }

    private Message getConnectError(String username, Client sender) {
        if (sender.getUsername() != null) {
            return new BaseMessage(CMD_ER66, ER66_BODY, sender);
        }

        if (!isValidUsername(username)) {
            return new BaseMessage(CMD_ER02, ER02_BODY, sender);
        }

        if (isLoggedIn(username)) {
            return new BaseMessage(CMD_ER01, ER01_BODY, sender);
        }

        return null;
    }

    private boolean isValidGroupName(String groupName) {
        var pattern = "^[a-zA-Z0-9_]{3,14}$";
        return groupName.matches(pattern);
    }

    private boolean isLoggedIn(String username) {
        return this.serviceManager.hasClient(username);
    }

    private boolean isValidUsername(String username) {
        var pattern = "^[a-zA-Z0-9_]{3,14}$";
        return username.matches(pattern);
    }

    private String[] parseMessage(String message) {
        return message.split(" ", 2);
    }
}
