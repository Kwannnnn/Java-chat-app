package nl.saxion.itech.client.model.protocol.visitors;

import nl.saxion.itech.client.model.protocol.messages.receivable.InfoMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.OkBroadcastMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.OkConnectMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.ErrorMessage;

public interface ReceivableMessageVisitor {
    void visit(InfoMessage message);
    void visit(OkConnectMessage message);
    void visit(OkBroadcastMessage message);
    void visit(ErrorMessage message);
}
