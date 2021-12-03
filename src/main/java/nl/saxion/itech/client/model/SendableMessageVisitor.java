package nl.saxion.itech.client.model;

import nl.saxion.itech.client.model.protocol.*;

public interface SendableMessageVisitor extends MessageVisitor {
    String visit(ConnectMessage message);
    String visit(BroadcastMessage message);
    String visit(QuitMessage quitMessage);
}
