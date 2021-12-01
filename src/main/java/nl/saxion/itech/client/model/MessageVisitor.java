package nl.saxion.itech.client.model;

import nl.saxion.itech.client.model.protocol.BroadcastMessage;
import nl.saxion.itech.client.model.protocol.InfoMessage;
import nl.saxion.itech.client.model.protocol.OkMessage;

public interface MessageVisitor {
    void visit(OkMessage message);
    void visit(InfoMessage message);
    void visit(BroadcastMessage message);
}
