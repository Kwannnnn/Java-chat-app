package nl.saxion.itech.client.model.protocol.messages.sendable;

import nl.saxion.itech.client.model.protocol.messages.Message;
import nl.saxion.itech.client.model.protocol.messages.Visitable;
import nl.saxion.itech.client.model.protocol.visitors.MessageVisitor;


public class GroupNewMessage implements Message, Visitable {
    private static final String HEADER = "GRP NEW";
    private String body;

    public GroupNewMessage(String body) {
        this.body = body;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return HEADER + " " + body;
    }
}
