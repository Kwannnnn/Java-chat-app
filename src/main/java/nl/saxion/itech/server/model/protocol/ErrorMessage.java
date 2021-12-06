package nl.saxion.itech.server.model.protocol;

public class ErrorMessage implements Message {
    private final String header;
    private final String message;

    public ErrorMessage(String header, String message) {
        this.header = header;
        this.message = message;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return header + " " + message;
    }
}
