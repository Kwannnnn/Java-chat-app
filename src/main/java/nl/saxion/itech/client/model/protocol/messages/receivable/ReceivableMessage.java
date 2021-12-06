package nl.saxion.itech.client.model.protocol.messages.receivable;

import nl.saxion.itech.client.model.protocol.visitors.ReceivableMessageVisitor;

public interface ReceivableMessage {
    void accept(ReceivableMessageVisitor messageVisitor);
}
