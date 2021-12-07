package nl.saxion.itech.client.model.protocol.visitors;

import nl.saxion.itech.client.ChatClient;
import nl.saxion.itech.client.model.protocol.messages.receivable.InfoMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.OkBroadcastMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.OkConnectMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.ErrorMessage;

public class ReadMessageVisitor implements ReceivableMessageVisitor {
    private ChatClient client;

    public static final String ANSI_YELLOW = "\u001B[33;3m";
    public static final String ANSI_RESET = "\u001B[0m";

    public ReadMessageVisitor(ChatClient client) {
        this.client = client;
    }

    @Override
    public void visit(InfoMessage message) {
        System.out.println(message.getMessage());
    }

    @Override
    public void visit(OkConnectMessage message) {
        this.client.setCurrentUser(message.getBody());
        System.out.println("You have been successfully logged in.");
        System.out.println(ANSI_YELLOW + "Type '?' to show menu." + ANSI_RESET);
    }

    @Override
    public void visit(OkBroadcastMessage message) {
        System.out.println("Broadcast message successfully sent.");
    }

    @Override
    public void visit(ErrorMessage message) {
        System.out.println(message.getMessage());
    }
}
