package nl.saxion.itech.client.model.protocol;

import nl.saxion.itech.client.model.MessageVisitor;
import nl.saxion.itech.client.model.SendableMessageVisitor;

public class QuitMessage implements Message {
    @Override
    public void accept(MessageVisitor visitor) {
        ((SendableMessageVisitor) visitor).visit(this);
    }
}
