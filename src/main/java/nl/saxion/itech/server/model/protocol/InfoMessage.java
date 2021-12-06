package nl.saxion.itech.server.model.protocol;

public class InfoMessage implements Message {
    private static final String HEADER = "INFO";
    private final String message;

    public InfoMessage(String message) {
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
