package nl.saxion.itech.client.model.protocol.visitors;

import nl.saxion.itech.client.model.protocol.messages.sendable.*;

import java.io.PrintWriter;

public class SendMessageVisitor implements SendableMessageVisitor {
    private PrintWriter out;

    public SendMessageVisitor(PrintWriter out) {
        this.out = out;
    }

    @Override
    public void visit(ConnectMessage message) {
        this.out.println(message.toString());
    }

    @Override
    public void visit(BroadcastMessage message) {
        this.out.println(message.toString());
    }

    @Override
    public void visit(QuitMessage message) {
        this.out.println(message.toString());
    }

    @Override
    public void visit(SendableGroupJoinMessage message) {
        this.out.println(message.toString());
    }

    @Override
    public void visit(SendableGroupMessageMessage message) {
        this.out.println(message.toString());
    }

    @Override
    public void visit(GroupNewMessage message) {
        this.out.println(message.toString());

    }

    @Override
    public void visit(GroupDisconnectMessage message) {
        this.out.println(message.toString());
    }
}
