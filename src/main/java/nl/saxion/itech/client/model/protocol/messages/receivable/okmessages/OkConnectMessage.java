package nl.saxion.itech.client.model.protocol.messages.receivable.okmessages;

import nl.saxion.itech.client.model.protocol.visitors.ReceivableMessageVisitor;

public class OkConnectMessage extends OkMessage {

    public OkConnectMessage(String username) {
        super(username);
    }

    @Override
    public void accept(ReceivableMessageVisitor messageVisitor) {
        messageVisitor.visit(this);
    }
}
