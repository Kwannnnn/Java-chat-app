package nl.saxion.itech.client.model;

import nl.saxion.itech.client.model.protocol.BroadcastMessage;
import nl.saxion.itech.client.model.protocol.InfoMessage;
import nl.saxion.itech.client.model.protocol.OkMessage;

public class SendMessageVisitor implements MessageVisitor {
    @Override
    public void visit(OkMessage message) {

    }

    @Override
    public void visit(InfoMessage message) {

    }

    @Override
    public void visit(BroadcastMessage message) {

    }
}
