package nl.saxion.itech.client.model.protocol.messages.receivable.okmessages;

import nl.saxion.itech.client.model.protocol.visitors.MessageVisitor;

public class OkConnectMessage extends OkMessage {

    public OkConnectMessage(String username) {
        super(username);
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}
