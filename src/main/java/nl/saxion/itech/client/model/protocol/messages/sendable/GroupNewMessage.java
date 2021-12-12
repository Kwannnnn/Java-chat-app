package nl.saxion.itech.client.model.protocol.messages.sendable;

import nl.saxion.itech.client.model.protocol.visitors.SendableMessageVisitor;

public class GroupNewMessage implements SendableMessage{
    private static final String HEADER = "GRP NEW";
    private String body;

    public GroupNewMessage(String body) {
        this.body = body;
    }

    @Override
    public void accept(SendableMessageVisitor messageVisitor) {
        messageVisitor.visit(this);
    }

    @Override
    public String toString() {
        return HEADER + " " + body;
    }
}
