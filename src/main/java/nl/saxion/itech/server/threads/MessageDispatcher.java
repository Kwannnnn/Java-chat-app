package nl.saxion.itech.server.threads;

import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.model.protocol.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class MessageDispatcher extends Thread {
    private final ConcurrentHashMap<String, Client> clients;
    private final Vector<Message> messageQueue;
    private final MessageHandler messageHandler;

    public MessageDispatcher() {
        this.clients = new ConcurrentHashMap<>();
        this.messageQueue = new Vector<>();
        this.messageHandler = new ClientMessageHandler(this);
    }

    @Override
    public void run() {
        processMessages();
    }

    public synchronized void addClient(Client client) {
        this.clients.put(client.getUsername(), client);
        var message = new BaseMessage(
                ProtocolConstants.CMD_OK + " " + ProtocolConstants.CMD_CONN,
                client.getUsername(),
                client
        );
        this.dispatchMessage(message);
        new PingThread(client, this).start();
    }

    public synchronized boolean hasClient(String username) {
        return this.clients.containsKey(username);
    }

    public synchronized void removeClient(Client client) {
        if (clients.containsKey(client.getUsername())) {
            this.clients.remove(client.getUsername());
            var response = new BaseMessage(
                    ProtocolConstants.CMD_OK + " " + ProtocolConstants.CMD_QUIT,
                    null,
                    client
            );
            this.dispatchMessage(response);
            try {
                client.getSocket().close();
            } catch (IOException e) {
                System.err.println("Client socket has already been closed.");
            }
        }
    }

    public synchronized void dispatchMessage(Message message) {
        this.messageQueue.add(message);
        notify();
    }

    public synchronized void sendPrivateMessage(Message message, String recipient, Client replyTo) {
        if (!this.hasClient(recipient)) {
            // User is not connected
            this.dispatchMessage(new BaseMessage(
                    ProtocolConstants.CMD_ER04,
                    ProtocolConstants.ER04_BODY,
                    replyTo
            ));
            return;
        }

        message.setClient(this.clients.get(recipient));
        this.dispatchMessage(message);
        this.dispatchMessage(new BaseMessage(
                ProtocolConstants.CMD_OK + " " + ProtocolConstants.CMD_MSG + " " + recipient,
                message.getBody(),
                replyTo
        ));
    }

    public synchronized void broadcastMessage(Message message) {
        for (var client : this.clients.values()) {
            if (!client.equals(message.getClient())) {
                var broadcastMessage = new BaseMessage(
                        ProtocolConstants.CMD_BCST
                        + " " + message.getClient().getUsername(),
                        message.getBody(),
                        client
                );
                this.dispatchMessage(broadcastMessage);
            }
        }
    }

    private synchronized void processMessages() {
        try {
            while (true) {
                var message = getNextMessage();
                message.accept(this.messageHandler);
            }
        } catch (InterruptedException e) {
            // Thread interrupted
        }
    }

    private synchronized Message getNextMessage() throws InterruptedException {
        while (this.messageQueue.size() == 0) {
            wait();
        }

        var message = messageQueue.get(0);
        this.messageQueue.removeElementAt(0);
        return message;
    }
}
