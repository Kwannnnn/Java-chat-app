package nl.saxion.itech.server.model.protocol;

import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.threads.MessageDispatcher;

import java.io.IOException;
import java.io.PrintWriter;

public class ClientMessageHandler implements MessageHandler {
    private final MessageDispatcher dispatcher;

    public ClientMessageHandler(MessageDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void handle(Message message) {
        switch (message.getHeader()) {
            case ProtocolConstants.CMD_CONN -> handleConnectMessage(message);
            case ProtocolConstants.CMD_QUIT -> handleQuitMessage(message);
            case ProtocolConstants.CMD_BCST -> handleBroadcast(message);
            case ProtocolConstants.CMD_PONG -> handlePong(message);
            case ProtocolConstants.CMD_MSG -> handleDirectMessage(message);
            case ProtocolConstants.CMD_ALL -> handleAllMessage(message);
            default -> sendMessageToClient(message);
        }
    }

    private void handleAllMessage(Message message) {
        var sender = message.getClient();

        if (message.getClient().getUsername() == null) {
            // Please login first
            this.dispatcher.dispatchMessage(new BaseMessage(
                    ProtocolConstants.CMD_ER03,
                    ProtocolConstants.ER03_BODY,
                    message.getClient()
            ));
            return;
        }

        String listString = String.join(",", dispatcher.getClients());

        this.dispatcher.dispatchMessage(new BaseMessage(
                ProtocolConstants.CMD_OK
                + " " +
                ProtocolConstants.CMD_ALL, listString,
                sender));
    }

    private void handleDirectMessage(Message message) {
        var splitMessage = parseMessage(message.getBody());
        if (message.getClient().getUsername() == null) {
            // Please login first
            this.dispatcher.dispatchMessage(new BaseMessage(
                    ProtocolConstants.CMD_ER03,
                    ProtocolConstants.ER03_BODY,
                    message.getClient()
            ));
            return;
        }

        if (splitMessage.length < 2) {
            // Missing parameters
            this.dispatcher.dispatchMessage(new BaseMessage(
                    ProtocolConstants.CMD_ER08,
                    ProtocolConstants.ER08_BODY,
                    message.getClient()
            ));
            return;
        }

        var recipient = splitMessage[0];
        var body = splitMessage[1];
        var dm = new BaseMessage(
                ProtocolConstants.CMD_MSG + " " + message.getClient().getUsername(),
                body
        );

        this.dispatcher.sendPrivateMessage(dm, recipient, message.getClient());
    }

    private void sendMessageToClient(Message message) {
        var printWriter = getPrintWriter(message.getClient());
        if (printWriter == null) return; // The client socket has been closed
        printWriter.println(message);
    }

    private void handlePong(Message message) {
        message.getClient().setHasPonged(true);
    }

    private void handleBroadcast(Message message) {
        var sender = message.getClient();

        if (sender.getUsername() == null) {
            this.dispatcher.dispatchMessage(new BaseMessage(
                    ProtocolConstants.CMD_ER03,
                    ProtocolConstants.ER03_BODY,
                    sender
            ));
            return;
        }

        this.dispatcher.broadcastMessage(message);
        this.dispatcher.dispatchMessage(new BaseMessage(
                ProtocolConstants.CMD_OK + " " + ProtocolConstants.CMD_BCST,
                message.getBody(),
                sender
        ));
    }

    private void handleQuitMessage(Message message) {
        this.dispatcher.removeClient(message.getClient());
    }

    private void handleConnectMessage(Message message) {
        var error = getConnectError(message);
        if  (error == null) {
            message.getClient().setUsername(message.getBody());
            this.dispatcher.addClient(message.getClient());
        } else {
            this.dispatcher.dispatchMessage(error);
        }
    }

    private Message getConnectError(Message message) {
        var username = message.getBody();
        var client = message.getClient();

        if (client.getUsername() != null) return new BaseMessage(ProtocolConstants.CMD_ER66, ProtocolConstants.ER66_BODY, client);
        if (!isValidUsername(username)) return new BaseMessage(ProtocolConstants.CMD_ER02, ProtocolConstants.ER02_BODY, client);
        if (isLoggedIn(username)) return new BaseMessage(ProtocolConstants.CMD_ER01, ProtocolConstants.ER01_BODY, client);

        return null;
    }

    private boolean isLoggedIn(String username) {
        return this.dispatcher.hasClient(username);
    }

    private boolean isValidUsername(String username) {
        var pattern = "^[a-zA-Z0-9_]{3,14}$";
        return username.matches(pattern);
    }

    public PrintWriter getPrintWriter(Client client) {
        var socket = client.getSocket();
        try {
            return new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            this.dispatcher.removeClient(client);
            return null;
        }
    }

    private String[] parseMessage(String message) {
        return message.split(" ", 2);
    }
}
