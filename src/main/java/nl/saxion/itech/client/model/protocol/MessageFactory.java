package nl.saxion.itech.client.model.protocol;

import nl.saxion.itech.client.model.protocol.messages.receivable.ErrorMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.InfoMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.OkMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.ReceivableMessage;

public class MessageFactory {
    private static final String CMD_OK = "OK";
    private static final String CMD_INFO = "INFO";

    public ReceivableMessage getMessage(String header, String message) {
        return switch (header) {
            case CMD_INFO -> new InfoMessage(message);
            case CMD_OK -> new OkMessage(message);
            default -> new ErrorMessage(header, message);
        };
    }
}
