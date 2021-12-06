package nl.saxion.itech.client.model.protocol.visitors;

import nl.saxion.itech.client.ChatClient;
import nl.saxion.itech.client.model.protocol.messages.receivable.InfoMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.OkMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.ErrorMessage;

public class ReadMessageVisitor implements ReceivableMessageVisitor {
    private ChatClient client;

    public ReadMessageVisitor(ChatClient client) {
        this.client = client;
    }

    @Override
    public void visit(InfoMessage message) {
        System.out.println(message.getMessage());
    }

    @Override
    public void visit(OkMessage message) {
        this.client.setCurrentUser(message.getMessage());
        System.out.println("Success");
    }

    @Override
    public void visit(ErrorMessage message) {
        System.out.println(message.getMessage());
    }
}
