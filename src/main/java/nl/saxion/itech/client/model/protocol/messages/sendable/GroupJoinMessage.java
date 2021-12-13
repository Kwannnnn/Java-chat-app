package nl.saxion.itech.client.model.protocol.messages.sendable;

import nl.saxion.itech.client.model.protocol.visitors.SendableMessageVisitor;

public class SendableGroupJoinMessage implements SendableMessage{
    private static final String HEADER = "GRP JOIN";
    private String body;

    public SendableGroupJoinMessage(String body) {
        this.body = body;
    }

    public String getBody() {
        return this.body;
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
