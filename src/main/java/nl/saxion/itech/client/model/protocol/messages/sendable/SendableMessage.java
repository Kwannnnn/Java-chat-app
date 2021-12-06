package nl.saxion.itech.client.model.protocol.messages.sendable;

import nl.saxion.itech.client.model.protocol.visitors.SendableMessageVisitor;

public interface SendableMessage {
    void accept(SendableMessageVisitor messageVisitor);
}
