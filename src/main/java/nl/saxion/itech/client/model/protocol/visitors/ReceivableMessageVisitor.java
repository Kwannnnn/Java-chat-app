package nl.saxion.itech.client.model.protocol.visitors;

import nl.saxion.itech.client.model.protocol.messages.receivable.InfoMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.OkMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.ErrorMessage;

public interface ReceivableMessageVisitor {
    void visit(InfoMessage message);
    void visit(OkMessage message);
    void visit(ErrorMessage message);
}
