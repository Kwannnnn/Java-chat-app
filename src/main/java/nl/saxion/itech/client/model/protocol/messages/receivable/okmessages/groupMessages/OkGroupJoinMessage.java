package nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.groupMessages;

import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.OkMessage;
import nl.saxion.itech.client.model.protocol.visitors.ReceivableMessageVisitor;

public class OkGroupJoinMessage extends OkMessage {
    public OkGroupJoinMessage(String message) {
        super(message);
    }

    @Override
    public void accept(ReceivableMessageVisitor messageVisitor) {
        messageVisitor.visit(this);
    }
}
