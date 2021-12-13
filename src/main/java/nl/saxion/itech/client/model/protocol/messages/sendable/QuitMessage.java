package nl.saxion.itech.client.model.protocol.messages.sendable;

import nl.saxion.itech.client.model.protocol.messages.Message;
import nl.saxion.itech.client.model.protocol.messages.Visitable;
import nl.saxion.itech.client.model.protocol.visitors.MessageVisitor;

public class QuitMessage implements Message, Visitable {
    private static final String HEADER = "QUIT";

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return HEADER;
    }
}
