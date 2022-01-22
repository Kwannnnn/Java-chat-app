package nl.saxion.itech.client.model.message;

public class BaseMessage{
    private final String header;
    private String body;

    public BaseMessage(String header, String body) {
        this.header = header;
        this.body = body;
    }

    public BaseMessage(String header) {
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
