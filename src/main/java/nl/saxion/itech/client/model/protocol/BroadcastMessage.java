package nl.saxion.itech.client.model.protocol;

import nl.saxion.itech.client.model.MessageVisitor;
import nl.saxion.itech.client.model.SendableMessageVisitor;

public class BroadcastMessage implements Message {
    private static final String HEADER = "BCST";
    private String message;

    public BroadcastMessage(String message) {
        this.message = message;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        ((SendableMessageVisitor) visitor).visit(this);
    }

    @Override
    public String toString() {
        return HEADER + " " + message;
    }
}
