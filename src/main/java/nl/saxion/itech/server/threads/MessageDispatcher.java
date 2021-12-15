package nl.saxion.itech.server.threads;

import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.model.protocol.BaseMessage;
import nl.saxion.itech.server.model.protocol.ClientMessageHandler;
import nl.saxion.itech.server.model.protocol.MessageHandler;
import nl.saxion.itech.server.model.protocol.ProtocolConstants;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class MessageDispatcher extends Thread {
    private final ConcurrentHashMap<String, Client> clients;
    private final Vector<BaseMessage> messageQueue;
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
                ProtocolConstants.CMD_OK + " " + ProtocolConstants.CMD_CONN + " " + client.getUsername(),
                null,
                client
        );
        this.messageQueue.add(message);
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
            this.messageQueue.add(response);
            try {
                client.getSocket().close();
            } catch (IOException e) {
                System.err.println("Client socket has already been closed.");
            }
        }
    }

    public synchronized void dispatchMessage(BaseMessage message) {
        this.messageQueue.add(message);
        notify();
    }

    public synchronized void broadcastMessage(BaseMessage message) {
        for (var client : this.clients.values()) {
            if (!client.equals(message.getClient())) {
                var broadcastMessage = new BaseMessage(
                        ProtocolConstants.CMD_BCST
                        + " " + message.getClient().getUsername(),
                        message.getBody(),
                        client
                );
                this.messageQueue.add(broadcastMessage);
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

    private synchronized BaseMessage getNextMessage() throws InterruptedException {
        while (this.messageQueue.size() == 0) {
            wait();
        }

        var message = messageQueue.get(0);
        this.messageQueue.removeElementAt(0);
        return message;
    }
}
