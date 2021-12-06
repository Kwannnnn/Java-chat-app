package nl.saxion.itech.server.model.protocol;

public interface Message {
    void accept(MessageVisitor visitor);
}
