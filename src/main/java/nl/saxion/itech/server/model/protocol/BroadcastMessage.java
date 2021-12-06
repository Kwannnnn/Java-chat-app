package nl.saxion.itech.server.model.protocol;

public class BroadcastMessage implements Message {
    private static final String HEADER = "BCST";
    private final String message;

    public BroadcastMessage(String message) {
        this.message = message;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return HEADER + " " + message;
    }
}
