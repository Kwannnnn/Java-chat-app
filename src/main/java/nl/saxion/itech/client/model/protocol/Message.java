package nl.saxion.itech.client.model.protocol;

import nl.saxion.itech.client.model.MessageVisitor;

public interface Message {
    void accept(MessageVisitor visitor);
}
