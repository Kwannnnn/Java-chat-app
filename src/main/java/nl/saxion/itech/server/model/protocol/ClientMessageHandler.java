package nl.saxion.itech.server.model.protocol;

import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.threads.ServiceManager;

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
            case ProtocolConstants.CMD_CONN -> handleConnectMessage(body, sender);
            case ProtocolConstants.CMD_QUIT -> handleQuitMessage(sender);
            case ProtocolConstants.CMD_BCST -> handleBroadcast(body, sender);
            case ProtocolConstants.CMD_PONG -> handlePong(sender);
            case ProtocolConstants.CMD_MSG -> handleDirectMessage(body, sender);
            case ProtocolConstants.CMD_ALL -> handleAllMessage(sender);
            case ProtocolConstants.CMD_GRP -> handleGroupMessage(body, sender);
            default -> this.serviceManager.dispatchMessage(
                    new BaseMessage(ProtocolConstants.CMD_ER00, ProtocolConstants.ER00_BODY, sender));
        }
    }

    private void handleGroupMessage(String message, Client sender) {
        String[] splitMessage = parseMessage(message);
        String header = splitMessage[0];
        String body = splitMessage.length > 1 ? splitMessage[1] : " ";

        switch (header) {
            case ProtocolConstants.CMD_ALL -> handleGroupAllMessage(sender);
            case ProtocolConstants.CMD_NEW -> handleGroupNewMessage(body, sender);
            case ProtocolConstants.CMD_JOIN -> handleGroupJoinMessage(body, sender);
            case ProtocolConstants.CMD_MSG -> handleGroupMessageMessage(body, sender);
            default -> this.serviceManager.dispatchMessage(
                    new BaseMessage(ProtocolConstants.CMD_ER00, ProtocolConstants.ER00_BODY, sender));
        }
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
                            ProtocolConstants.CMD_GRP + " " + ProtocolConstants.CMD_MSG,
                            groupName + " " + groupMessage,
                            member);

                    this.serviceManager.dispatchMessage(m);
                }
            }

            // send confirmation message back to sender
            this.serviceManager.dispatchMessage(new BaseMessage(
                    ProtocolConstants.CMD_OK
                            + " " + ProtocolConstants.CMD_GRP
                            + " " + ProtocolConstants.CMD_MSG,
                    groupName + " " + groupMessage,
                    sender));
        } else {
            this.serviceManager.dispatchMessage(error);
        }
    }

    private Message getGroupMessageMessageError(String[] splitMessage, Client sender) {
        if (sender.getUsername() == null) {
            // Please login first
            return new BaseMessage(
                    ProtocolConstants.CMD_ER03,
                    ProtocolConstants.ER03_BODY,
                    sender);
        }

        if (splitMessage.length < 2) {
            // Missing parameters
            return new BaseMessage(
                    ProtocolConstants.CMD_ER08,
                    ProtocolConstants.ER08_BODY,
                    sender
            );
        }

        String groupName = splitMessage[0];

        if (!this.serviceManager.hasGroup(groupName)) {
            // Group doesn't exist
            return new BaseMessage(
                    ProtocolConstants.CMD_ER07,
                    ProtocolConstants.ER07_BODY,
                    sender);
        }

        if (!this.serviceManager.groupHasClient(groupName, sender)) {
            // Client not part of group
            return new BaseMessage(
                    ProtocolConstants.CMD_ER10,
                    ProtocolConstants.ER10_BODY,
                    sender);
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
                            ProtocolConstants.CMD_GRP + " " + ProtocolConstants.CMD_JOIN,
                            groupName + " " + sender.getUsername(),
                            member);

                    this.serviceManager.dispatchMessage(notification);
                }
            }

            // send confirmation message back to sender
            this.serviceManager.dispatchMessage(new BaseMessage(
                    ProtocolConstants.CMD_OK
                            + " " + ProtocolConstants.CMD_GRP
                            + " " + ProtocolConstants.CMD_JOIN,
                    groupName,
                    sender));
        } else {
            this.serviceManager.dispatchMessage(error);
        }
    }

    private Message getGroupJoinMessageError(String[] splitMessage, Client sender) {
        if (sender.getUsername() == null) {
            // Please login first
            return new BaseMessage(
                    ProtocolConstants.CMD_ER03,
                    ProtocolConstants.ER03_BODY,
                    sender);
        }

        if (splitMessage.length == 0) {
            // Missing parameters
            return new BaseMessage(
                    ProtocolConstants.CMD_ER08,
                    ProtocolConstants.ER08_BODY,
                    sender
            );
        }

        String groupName = splitMessage[0];

        if (!isValidGroupName(groupName)) {
            // Invalid group name
            return new BaseMessage(
                    ProtocolConstants.CMD_ER05,
                    ProtocolConstants.ER05_BODY,
                    sender);
        }

        if (!this.serviceManager.hasGroup(groupName)) {
            // Group doesn't exist
            return new BaseMessage(
                    ProtocolConstants.CMD_ER07,
                    ProtocolConstants.ER07_BODY,
                    sender);
        }

        if (this.serviceManager.groupHasClient(groupName, sender)) {
            // Client already joined group
            return new BaseMessage(
                    ProtocolConstants.CMD_ER09,
                    ProtocolConstants.ER09_BODY,
                    sender);
        }

        return null;
    }

    private void handleGroupNewMessage(String message, Client sender) {
        String[] splitMessage = parseMessage(message);

        Message error = getGroupNewMessageError(splitMessage, sender);

        if (error == null) {
            String groupName = splitMessage[0];
            this.serviceManager.addGroup(groupName);

            //send confirmation message back to sender
            this.serviceManager.dispatchMessage(new BaseMessage(
                    ProtocolConstants.CMD_OK
                            + " " + ProtocolConstants.CMD_GRP
                            + " " + ProtocolConstants.CMD_NEW,
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
            return new BaseMessage(
                    ProtocolConstants.CMD_ER03,
                    ProtocolConstants.ER03_BODY,
                    sender);
        }

        String groupName = splitMessage[0];

        if (!isValidGroupName(groupName)) {
            // invalid group name
            return new BaseMessage(
                    ProtocolConstants.CMD_ER05,
                    ProtocolConstants.ER05_BODY,
                    sender);
        }

        if (this.serviceManager.hasGroup(groupName)) {
            // group already exists
            return new BaseMessage(
                    ProtocolConstants.CMD_ER06,
                    ProtocolConstants.ER06_BODY,
                    sender);
        }

        return null;
    }

    private void handleGroupAllMessage(Client sender) {
        if (sender.getUsername() == null) {
            // Please login first
            this.serviceManager.dispatchMessage(new BaseMessage(
                    ProtocolConstants.CMD_ER03,
                    ProtocolConstants.ER03_BODY,
                    sender
            ));
        }

        String listString = String.join(",", serviceManager.getGroups().keySet());

        this.serviceManager.dispatchMessage(new BaseMessage(
                ProtocolConstants.CMD_OK
                        + " " + ProtocolConstants.CMD_GRP
                        + " " + ProtocolConstants.CMD_ALL,
                listString,
                sender));
    }

    private void handleAllMessage(Client sender) {
        if (sender.getUsername() == null) {
            // Please login first
            this.serviceManager.dispatchMessage(new BaseMessage(
                    ProtocolConstants.CMD_ER03,
                    ProtocolConstants.ER03_BODY,
                    sender
            ));
            return;
        }

        String listString = serviceManager.getClients().stream().map(Client::getUsername).collect(Collectors.joining(","));

        // send confirmation message back to sender
        this.serviceManager.dispatchMessage(new BaseMessage(
                ProtocolConstants.CMD_OK
                        + " " +
                        ProtocolConstants.CMD_ALL,
                listString,
                sender));
    }

    private void handleDirectMessage(String message, Client sender) {
        var splitMessage = parseMessage(message);

        Message error = getDirectMessageError(splitMessage, sender);

        if (error == null) {
            String recipientUsername = splitMessage[0];
            String body = splitMessage[1];

            //TODO: which client to send to if there are duplicates????
            Client recipient = this.serviceManager.getClientByUsername(recipientUsername);

            String messageToRecipient = ProtocolConstants.CMD_MSG + " " + sender.getUsername() + body;
            sendPrivateMessage(messageToRecipient, recipient, sender);
        } else {
            this.serviceManager.dispatchMessage(error);
        }
    }

    private void sendPrivateMessage(String message, Client recipient, Client sender) {
        var messageToRecipient = new BaseMessage(
                ProtocolConstants.CMD_MSG + " " + sender.getUsername(),
                message,
                recipient
        );
        this.serviceManager.dispatchMessage(messageToRecipient);

        //send confirmation message back to sender
        this.serviceManager.dispatchMessage(new BaseMessage(
                ProtocolConstants.CMD_OK + " " + ProtocolConstants.CMD_MSG + " " + recipient.getUsername(),
                message,
                sender
        ));
    }

    private Message getDirectMessageError(String[] splitMessage, Client sender) {
        if (sender.getUsername() == null) {
            // Please login first
            return new BaseMessage(
                    ProtocolConstants.CMD_ER03,
                    ProtocolConstants.ER03_BODY,
                    sender
            );
        }

        if (splitMessage.length < 2) {
            // Missing parameters
            return new BaseMessage(
                    ProtocolConstants.CMD_ER08,
                    ProtocolConstants.ER08_BODY,
                    sender
            );
        }

        String recipientUsername = splitMessage[0];

        if (!this.serviceManager.hasClient(recipientUsername)) {
            // User is not connected
            return new BaseMessage(
                    ProtocolConstants.CMD_ER04,
                    ProtocolConstants.ER04_BODY,
                    sender
            );
        }

        return null;
    }

    private void handlePong(Client sender) {
        //TODO: decide if we should check if message body is empty
        sender.setHasPonged(true);
    }

    private void handleBroadcast(String messageToBroadcast, Client sender) {
        if (sender.getUsername() == null) {
            this.serviceManager.dispatchMessage(new BaseMessage(
                    ProtocolConstants.CMD_ER03,
                    ProtocolConstants.ER03_BODY,
                    sender
            ));

            return;
        }

        broadcastMessage(messageToBroadcast, sender);

        this.serviceManager.dispatchMessage(new BaseMessage(
                ProtocolConstants.CMD_OK + " " + ProtocolConstants.CMD_BCST,
                messageToBroadcast,
                sender
        ));
    }

    private void broadcastMessage(String messageToBroadcast, Client sender) {
        for (var client : this.serviceManager.getClients()) {
            if (!client.equals(sender)) {
                var broadcastMessage = new BaseMessage(
                        ProtocolConstants.CMD_BCST
                                + " " + sender.getUsername(),
                        messageToBroadcast,
                        client
                );
                this.serviceManager.dispatchMessage(broadcastMessage);
            }
        }
    }

    private void handleQuitMessage(Client sender) {
        this.serviceManager.removeClient(sender);

        var response = new BaseMessage(
                ProtocolConstants.CMD_OK + " " + ProtocolConstants.CMD_QUIT,
                null,
                sender
        );

        this.serviceManager.dispatchMessage(response);

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
                    ProtocolConstants.CMD_OK + " " + ProtocolConstants.CMD_CONN,
                    username,
                    sender
            );
            this.serviceManager.dispatchMessage(message);

            //start ping thread for new client
            this.serviceManager.startNewPingThread(sender);
        } else {
            this.serviceManager.dispatchMessage(error);
        }
    }

    private Message getConnectError(String username, Client sender) {
        if (username.equals(sender.getUsername())) {
            return new BaseMessage(ProtocolConstants.CMD_ER66, ProtocolConstants.ER66_BODY, sender);
        }

        if (!isValidUsername(username)) {
            return new BaseMessage(ProtocolConstants.CMD_ER02, ProtocolConstants.ER02_BODY, sender);
        }

        if (isLoggedIn(username)) {
            return new BaseMessage(ProtocolConstants.CMD_ER01, ProtocolConstants.ER01_BODY, sender);
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
