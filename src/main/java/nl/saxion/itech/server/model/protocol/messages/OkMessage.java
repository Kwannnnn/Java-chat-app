package nl.saxion.itech.server.model.protocol.messages;

import nl.saxion.itech.server.model.protocol.messages.Message;
import nl.saxion.itech.server.model.protocol.visitors.MessageVisitor;

public class OkMessage implements Message {
    private static final String HEADER = "OK";
    private final String message;

    public OkMessage(String message) {
        this.message = message;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return HEADER + " " + message;
    }
}
