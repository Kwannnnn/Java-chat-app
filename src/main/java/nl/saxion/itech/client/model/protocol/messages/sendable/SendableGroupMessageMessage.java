package nl.saxion.itech.client.model.protocol.messages.sendable;

import nl.saxion.itech.client.model.protocol.visitors.SendableMessageVisitor;

public class SendableGroupMessageMessage implements SendableMessage{
    private static final String HEADER = "GRP MSG";
    private String body;

    public SendableGroupMessageMessage(String body) {
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
