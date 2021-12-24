package nl.saxion.itech.client.newDesign;

public class BaseMessage implements Message {
    private final String header;
    private final String body;

    public BaseMessage(String header, String body) {
        this.header = header;
        this.body = body;
    }



    public String getHeader() {
        return this.header;
    }

    public String getBody() {
        return this.body;
    }

    @Override
    public String toString() {
        return header + " " + body;
    }
}
