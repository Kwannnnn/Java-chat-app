package nl.saxion.itech.client.model;

import nl.saxion.itech.client.model.protocol.*;

public interface ReceivableMessageVisitor extends MessageVisitor {
    String visit(InfoMessage message);
    String visit(OkMessage message);
    String visit(ErrorMessage errorMessage);
}
