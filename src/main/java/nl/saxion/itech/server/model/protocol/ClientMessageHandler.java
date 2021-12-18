package nl.saxion.itech.server.model.protocol;

import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.threads.ServiceManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

public class ClientMessageHandler{
    private final ServiceManager serviceManager;

    public ClientMessageHandler(ServiceManager dispatcher) {
        this.serviceManager = dispatcher;
    }

    public void handle(String rawMessage, Client sender) {
        String[] splitMessage = parseMessage(rawMessage);
        String header = splitMessage[0];
        String body = splitMessage.length > 1 ? splitMessage[1] : null;

        switch (header) {
            case ProtocolConstants.CMD_CONN -> handleConnectMessage(body, sender);
            case ProtocolConstants.CMD_QUIT -> handleQuitMessage(sender);
            case ProtocolConstants.CMD_BCST -> handleBroadcast(body, sender);
            case ProtocolConstants.CMD_PONG -> handlePong(sender);
            case ProtocolConstants.CMD_MSG -> handleDirectMessage(body, sender);
            case ProtocolConstants.CMD_ALL -> handleAllMessage(sender);
//            case ProtocolConstants.CMD_GRP -> handleGroupMessage(message);
            default -> this.serviceManager.dispatchMessage(
                    new BaseMessage(ProtocolConstants.CMD_ER00, ProtocolConstants.ER00_BODY));
        }
    }
    
    private void handleGroupMessage(Message message) {
        
    }

    private void handleAllMessage(Client replyTo) {
        if (replyTo.getUsername() == null) {
            // Please login first
            this.serviceManager.dispatchMessage(new BaseMessage(
                    ProtocolConstants.CMD_ER03,
                    ProtocolConstants.ER03_BODY,
                    replyTo
            ));
            return;
        }

        String listString = serviceManager.getClients().stream().map(Client::getUsername).collect(Collectors.joining(","));

        this.serviceManager.dispatchMessage(new BaseMessage(
                ProtocolConstants.CMD_OK
                + " " +
                ProtocolConstants.CMD_ALL,
                listString,
                replyTo));
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

    private synchronized void sendPrivateMessage(String message, Client recipient, Client replyTo) {
        var messageToRecipient = new BaseMessage(
                ProtocolConstants.CMD_MSG + " " + replyTo.getUsername(),
                message,
                recipient
        );
        this.serviceManager.dispatchMessage(messageToRecipient);

        //send confirmation message back to sender
        this.serviceManager.dispatchMessage(new BaseMessage(
                ProtocolConstants.CMD_OK + " " + ProtocolConstants.CMD_MSG + " " + recipient.getUsername(),
                message,
                replyTo
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

    private void handleBroadcast(String messageToBroadcast, Client replyTo) {
        if (replyTo.getUsername() == null) {
            this.serviceManager.dispatchMessage(new BaseMessage(
                    ProtocolConstants.CMD_ER03,
                    ProtocolConstants.ER03_BODY,
                    replyTo
            ));

            return;
        }

        broadcastMessage(messageToBroadcast, replyTo);

        this.serviceManager.dispatchMessage(new BaseMessage(
                ProtocolConstants.CMD_OK + " " + ProtocolConstants.CMD_BCST,
                messageToBroadcast,
                replyTo
        ));
    }

    private synchronized void broadcastMessage(String messageToBroadcast, Client sender) {
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

    private void handleQuitMessage(Client client) {
        this.serviceManager.removeClient(client);

        var response = new BaseMessage(
                ProtocolConstants.CMD_OK + " " + ProtocolConstants.CMD_QUIT,
                null,
                client
        );

        this.serviceManager.dispatchMessage(response);

        try {
            client.getSocket().close();
        } catch (IOException e) {
            System.err.println("Client socket has already been closed.");
        }
    }

    private void handleConnectMessage(String username, Client replyTo) {
        var error = getConnectError(username, replyTo);

        if  (error == null) {
            replyTo.setUsername(username);
            this.serviceManager.addClient(replyTo);

            //send confirmation back to client
            var message = new BaseMessage(
                    ProtocolConstants.CMD_OK + " " + ProtocolConstants.CMD_CONN,
                    username,
                    replyTo
            );
            this.serviceManager.dispatchMessage(message);

            //start ping thread for new client
            this.serviceManager.startNewPingThread(replyTo);
        } else {
            this.serviceManager.dispatchMessage(error);
        }
    }

    private Message getConnectError(String username, Client replyTo) {
        if (username.equals(replyTo.getUsername())) {
            return new BaseMessage(ProtocolConstants.CMD_ER66, ProtocolConstants.ER66_BODY, replyTo);
        }

        if (!isValidUsername(username)) {
            return new BaseMessage(ProtocolConstants.CMD_ER02, ProtocolConstants.ER02_BODY, replyTo);
        }

        if (isLoggedIn(username)) {
            return new BaseMessage(ProtocolConstants.CMD_ER01, ProtocolConstants.ER01_BODY, replyTo);
        }

        return null;
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
