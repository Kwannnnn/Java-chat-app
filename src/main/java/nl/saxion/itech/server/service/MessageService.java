package nl.saxion.itech.server.service;

import nl.saxion.itech.server.model.protocol.Message;

import java.util.ArrayList;

public class MessageService {
    private final ArrayList<Message> messageQueue;

    public MessageService() {
        this.messageQueue = new ArrayList<>();
    }

    public void addMessage(Message message) {
        this.messageQueue.add(message);
    }

}
