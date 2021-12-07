package nl.saxion.itech.client.model.protocol.messages.sendable;

import nl.saxion.itech.client.model.protocol.visitors.SendableMessageVisitor;

public class ConnectMessage implements SendableMessage {
    private static final String HEADER = "CONN";
    private String message;

    public ConnectMessage(String message) {
        this.message = message;
    }

    @Override
    public void accept(SendableMessageVisitor messageVisitor) {
        messageVisitor.visit(this);
    }

    @Override
    public String toString() {
        return HEADER + " " + message;
    }
}
