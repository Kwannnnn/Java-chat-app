package nl.saxion.itech.client.model.protocol.messages.receivable.okmessages;

import nl.saxion.itech.client.model.protocol.visitors.ReceivableMessageVisitor;

public class OkBroadcastMessage extends OkMessage {

    public OkBroadcastMessage(String message) {
        super(message);
    }

    @Override
    public void accept(ReceivableMessageVisitor messageVisitor) {
        messageVisitor.visit(this);
    }
}
