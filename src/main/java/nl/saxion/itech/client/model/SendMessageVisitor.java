package nl.saxion.itech.client.model;

import nl.saxion.itech.client.model.protocol.*;

public class SendMessageVisitor implements SendableMessageVisitor {
    @Override
    public String visit(ConnectMessage message) {
        return "CONN" + message.toString();
    }

    @Override
    public String visit(BroadcastMessage message) {
        return "BCST" + message.toString();
    }

    @Override
    public String visit(QuitMessage quitMessage) {
        return "QUIT";
    }
}
