package nl.saxion.itech.client.model.protocol.messages;

import nl.saxion.itech.client.model.protocol.visitors.MessageVisitor;

public interface Visitable extends Message {
    void accept(MessageVisitor visitor);
}
