package nl.saxion.itech.client.model.protocol.messages.receivable;

import nl.saxion.itech.client.model.protocol.visitors.ReceivableMessageVisitor;

public class ErrorMessage implements ReceivableMessage {
    private String code;
    private String body;

    public ErrorMessage(String code, String body) {
        this.code = code;
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
