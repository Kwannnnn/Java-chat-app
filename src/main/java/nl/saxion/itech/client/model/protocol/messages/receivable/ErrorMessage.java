package nl.saxion.itech.client.model.protocol.messages.receivable;

import nl.saxion.itech.client.model.protocol.messages.Message;
import nl.saxion.itech.client.model.protocol.messages.Visitable;
import nl.saxion.itech.client.model.protocol.visitors.MessageVisitor;

public class ErrorMessage implements Message, Visitable {
    private String code;
    private String body;

    public ErrorMessage(String code, String body) {
        this.code = code;
        this.body = body;
    }

    public String getBody() {
        return this.body;
    }


    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}
