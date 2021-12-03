package nl.saxion.itech.client.model.protocol;

import nl.saxion.itech.client.model.MessageVisitor;
import nl.saxion.itech.client.model.ReceivableMessageVisitor;

public class InfoMessage implements Message {
    private String message;

    public InfoMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        System.out.println("Reached accept method of InfoMessage");
        ((ReceivableMessageVisitor) visitor).visit(this);
    }
}
