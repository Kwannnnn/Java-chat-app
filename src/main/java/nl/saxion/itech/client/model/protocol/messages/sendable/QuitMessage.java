package nl.saxion.itech.client.model.protocol.messages.sendable;

import nl.saxion.itech.client.model.protocol.visitors.SendableMessageVisitor;

public class QuitMessage implements SendableMessage {
    private static final String HEADER = "QUIT";

    @Override
    public void accept(SendableMessageVisitor messageVisitor) {
        messageVisitor.visit(this);
    }

    @Override
    public String toString() {
        return HEADER;
    }
}
