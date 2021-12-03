package nl.saxion.itech.client.model;

import nl.saxion.itech.client.model.protocol.*;

public class ReadMessageVisitor implements ReceivableMessageVisitor {

    @Override
    public String visit(InfoMessage message) {
        return message.getMessage();
    }

    @Override
    public String visit(OkMessage message) {
        return message.getMessage();
    }

    @Override
    public String visit(ErrorMessage errorMessage) {
        return errorMessage.getMessage();
    }
}
