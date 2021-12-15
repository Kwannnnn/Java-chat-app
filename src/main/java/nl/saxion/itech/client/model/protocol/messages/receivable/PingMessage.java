package nl.saxion.itech.client.model.protocol.messages.receivable;

import nl.saxion.itech.client.model.protocol.messages.Message;
import nl.saxion.itech.client.model.protocol.messages.Visitable;
import nl.saxion.itech.client.model.protocol.visitors.MessageVisitor;

public class PingMessage implements Message, Visitable {
    private static final String HEADER = "PING";

    @Override
    public String toString() {
        return HEADER;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}
