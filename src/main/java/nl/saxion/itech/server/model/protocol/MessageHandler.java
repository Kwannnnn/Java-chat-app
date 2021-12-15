package nl.saxion.itech.server.model.protocol;

public interface MessageHandler {
    void handle(BaseMessage message);
}
