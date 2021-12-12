package nl.saxion.itech.client.model.protocol.visitors;

import nl.saxion.itech.client.model.protocol.messages.sendable.*;

public interface SendableMessageVisitor {
    void visit(ConnectMessage message);
    void visit(BroadcastMessage message);
    void visit(QuitMessage message);
    void visit(SendableGroupJoinMessage message);
    void visit(SendableGroupMessageMessage message);
    void visit(GroupNewMessage message);
    void visit(GroupDisconnectMessage message);
}
