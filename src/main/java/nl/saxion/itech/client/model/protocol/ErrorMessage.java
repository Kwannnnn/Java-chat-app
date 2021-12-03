package nl.saxion.itech.client.model.protocol;

import nl.saxion.itech.client.model.MessageVisitor;
import nl.saxion.itech.client.model.ReceivableMessageVisitor;

public class ErrorMessage implements Message {
    private String message;

    public String getMessage() {
        return this.message;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        ((ReceivableMessageVisitor) visitor).visit(this);
    }
}
