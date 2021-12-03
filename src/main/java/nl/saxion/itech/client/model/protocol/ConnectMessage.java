package nl.saxion.itech.client.model.protocol;

import nl.saxion.itech.client.model.MessageVisitor;
import nl.saxion.itech.client.model.SendableMessageVisitor;

public class ConnectMessage implements Message {
    private static final String HEADER = "CONN";
    private String message;

    public ConnectMessage(String message) {
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
