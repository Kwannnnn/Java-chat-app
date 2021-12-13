package nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.groupMessages;

import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.OkMessage;
import nl.saxion.itech.client.model.protocol.visitors.MessageVisitor;

public class OkGroupAllMessage extends OkMessage {
    public OkGroupAllMessage(String message) {
        super(message);
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}