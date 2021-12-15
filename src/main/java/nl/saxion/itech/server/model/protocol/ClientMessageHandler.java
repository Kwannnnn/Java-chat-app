package nl.saxion.itech.server.model.protocol;

import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.threads.MessageDispatcher;
import nl.saxion.itech.server.threads.PingThread;

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
            default -> sendMessageToClient(message);
        }
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
                    ProtocolConstants.CMD_ER03,
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
        var error = getError(message);
        if  (error == null) {
            message.getClient().setUsername(message.getBody());
            this.dispatcher.addClient(message.getClient());
        } else {
            this.dispatcher.dispatchMessage(error);
        }
        new PingThread(message.getClient(), this.dispatcher).start();
    }

    private Message getError(Message message) {
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
}
