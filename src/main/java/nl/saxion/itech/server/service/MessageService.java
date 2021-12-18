package nl.saxion.itech.server.service;

import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.model.protocol.Message;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

public class MessageService {
    private final Vector<Message> messageQueue;

    public MessageService() {
        this.messageQueue = new Vector<>();
    }

    public synchronized void addMessage(Message message) {
        this.messageQueue.add(message);
        notify();
    }

    public synchronized Message getNextMessage() throws InterruptedException {
        while (this.messageQueue.size() == 0) {
            wait();
        }

        var message = messageQueue.get(0);
        this.messageQueue.removeElementAt(0);
        return message;
    }
}
