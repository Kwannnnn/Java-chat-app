package nl.saxion.itech.client.model.protocol.messages.receivable;

import nl.saxion.itech.client.model.protocol.visitors.ReceivableMessageVisitor;

public class ReceivableGroupJoinMessage implements ReceivableMessage{
    private String body;

    public ReceivableGroupJoinMessage(String body) {
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
