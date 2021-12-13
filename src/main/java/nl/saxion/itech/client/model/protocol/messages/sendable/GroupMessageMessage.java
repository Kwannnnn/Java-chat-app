package nl.saxion.itech.client.model.protocol.messages.sendable;

import nl.saxion.itech.client.model.protocol.visitors.SendableMessageVisitor;

public class GroupMessageMessage implements Message, Visitable {
    private static final String HEADER = "GRP MSG";
    private String body;

    public GroupMessageMessage(String body) {
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
