package nl.saxion.itech.client.model.protocol.messages.receivable;

import nl.saxion.itech.client.model.protocol.visitors.ReceivableMessageVisitor;

public class ReceivableGroupMessageMessage implements ReceivableMessage{
    private String body;

    public ReceivableGroupMessageMessage(String body) {
        this.body = body;
    }

    public String getBody() {
        return this.body;
    }

    @Override
    public void accept(ReceivableMessageVisitor messageVisitor) {
        messageVisitor.visit(this);
    }
}
