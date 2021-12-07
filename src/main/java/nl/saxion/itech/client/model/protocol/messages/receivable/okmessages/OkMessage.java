package nl.saxion.itech.client.model.protocol.messages.receivable.okmessages;

import nl.saxion.itech.client.model.protocol.messages.receivable.ReceivableMessage;
import nl.saxion.itech.client.model.protocol.visitors.ReceivableMessageVisitor;

public abstract class OkMessage implements ReceivableMessage {
    private String body;

    public OkMessage(String body) {
        this.body = body;
    }

    public String getBody() {
        return this.body;
    }
}
