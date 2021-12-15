package nl.saxion.itech.server.model.protocol;

public interface Message {
    void accept(MessageHandler messageHandler);
}
