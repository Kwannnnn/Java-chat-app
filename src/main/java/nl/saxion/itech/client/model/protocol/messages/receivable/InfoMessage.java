package nl.saxion.itech.client.model.protocol.messages.receivable;

import nl.saxion.itech.client.model.protocol.visitors.ReceivableMessageVisitor;

public class InfoMessage implements ReceivableMessage {
    private String message;

    public InfoMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public void accept(ReceivableMessageVisitor messageVisitor) {
        messageVisitor.visit(this);
    }
}
