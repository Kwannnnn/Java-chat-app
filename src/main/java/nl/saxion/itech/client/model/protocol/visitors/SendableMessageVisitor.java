package nl.saxion.itech.client.model.protocol.visitors;

import nl.saxion.itech.client.model.protocol.messages.sendable.BroadcastMessage;
import nl.saxion.itech.client.model.protocol.messages.sendable.QuitMessage;
import nl.saxion.itech.client.model.protocol.messages.sendable.ConnectMessage;

public interface SendableMessageVisitor {
    void visit(ConnectMessage message);
    void visit(BroadcastMessage message);
    void visit(QuitMessage message);
}
