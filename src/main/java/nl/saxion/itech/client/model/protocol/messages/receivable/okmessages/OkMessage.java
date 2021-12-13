package nl.saxion.itech.client.model.protocol.messages.receivable.okmessages;

import nl.saxion.itech.client.model.protocol.messages.Message;
import nl.saxion.itech.client.model.protocol.messages.Visitable;

public abstract class OkMessage implements Message, Visitable {
    private String body;

    public OkMessage(String body) {
        this.body = body;
    }

    public String getBody() {
        return this.body;
    }
}
