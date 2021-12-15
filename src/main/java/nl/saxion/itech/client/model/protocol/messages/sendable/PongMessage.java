package nl.saxion.itech.client.model.protocol.messages.sendable;

import nl.saxion.itech.client.model.protocol.messages.Message;
import nl.saxion.itech.client.model.protocol.visitors.MessageVisitor;

public class PongMessage implements Message {
    private static final String HEADER = "PONG";

    @Override
    public String toString() {
        return HEADER;
    }
}
