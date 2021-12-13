package nl.saxion.itech.client.model.protocol.messages.sendable;

import nl.saxion.itech.client.model.protocol.messages.Message;
import nl.saxion.itech.client.model.protocol.messages.Visitable;
import nl.saxion.itech.client.model.protocol.visitors.MessageVisitor;

public class GroupJoinMessage implements Message, Visitable {
    private static final String HEADER = "GRP JOIN";
    private String body;

    public GroupJoinMessage(String body) {
        this.body = body;
    }

    public String getBody() {
        return this.body;
    }

    @Override
    public void accept(MessageVisitor messageVisitor) {
        messageVisitor.visit(this);
    }

    @Override
    public String toString() {
        return HEADER + " " + body;
    }
}
