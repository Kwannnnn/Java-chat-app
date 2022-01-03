package nl.saxion.itech.server.message;

public class TextMessage implements Message {
    private String header;
    private String body;

    public TextMessage(String header, String body) {
        this.header = header;
        this.body = body;
    }

    public TextMessage(String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        var result = this.header;
        if (this.body != null) {
            result += " " + this.body;
        }

        return result;
    }
}
