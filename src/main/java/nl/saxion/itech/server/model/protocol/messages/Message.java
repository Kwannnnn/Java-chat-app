package nl.saxion.itech.server.model.protocol.messages;

import nl.saxion.itech.server.model.protocol.visitors.MessageVisitor;

public interface Message {
    void accept(MessageVisitor visitor);
}
