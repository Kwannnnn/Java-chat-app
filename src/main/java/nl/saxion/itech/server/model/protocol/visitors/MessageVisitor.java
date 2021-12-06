package nl.saxion.itech.server.model.protocol.visitors;

import nl.saxion.itech.server.model.protocol.messages.*;

public interface MessageVisitor {
    void visit(InfoMessage message);
    void visit(ConnectMessage message);
    void visit(OkMessage message);
    void visit(ErrorMessage message);
    void visit(BroadcastMessage message);
}
