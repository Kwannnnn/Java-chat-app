package nl.saxion.itech.client.model.protocol;

import nl.saxion.itech.client.model.MessageVisitor;
import nl.saxion.itech.client.model.ReceivableMessageVisitor;

public class OkMessage implements Message {
    private String message;

    public OkMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        ((ReceivableMessageVisitor) visitor).visit(this);
    }
}
