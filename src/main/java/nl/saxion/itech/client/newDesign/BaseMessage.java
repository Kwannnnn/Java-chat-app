package nl.saxion.itech.client.newDesign;

public class BaseMessage implements Message {
    private final String header;
    private String body;

    public BaseMessage(String header, String body) {
        this.header = header;
        this.body = body;
    }

    public BaseMessage(String header) {
        this.header = header;
    }

    public String getHeader() {
        return this.header;
    }

    public String getBody() {
        return this.body;
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
