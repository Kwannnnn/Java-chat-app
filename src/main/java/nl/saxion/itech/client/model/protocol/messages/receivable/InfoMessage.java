package nl.saxion.itech.client.model.protocol.messages.receivable;

import nl.saxion.itech.client.model.protocol.messages.Message;
import nl.saxion.itech.client.model.protocol.messages.Visitable;
import nl.saxion.itech.client.model.protocol.visitors.MessageVisitor;

public class InfoMessage implements Message, Visitable {
    private String message;

    public InfoMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
    
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}
