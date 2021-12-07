package nl.saxion.itech.client.model.protocol;

import nl.saxion.itech.client.model.protocol.messages.receivable.*;

public class MessageFactory {
    private static final String CMD_OK = "OK";
    private static final String CMD_INFO = "INFO";
    private static final String CMD_BCST = "BCST";
    private static final String CMD_GRP = "GRP";
    private static final String CMD_MSG = "MSG";
    // alt: hard code protocol header in each message

    public ReceivableMessage getMessage(String message) {
        String[] splitMessage = parseMessage(message);
        String header = splitMessage[0];
        String body = splitMessage[1];

        return switch (header) {
            case CMD_INFO -> new InfoMessage(message);
            case CMD_OK -> handleOkMessage(body);
            default -> new ErrorMessage(header, message);
        };
    }

    private String[] parseMessage(String message) {
        return message.split(" ", 2);
    }

    private OkMessage handleOkMessage(String message) {
        String[] splitMessage = parseMessage(message);
        String header = splitMessage[0];
        String body = splitMessage.length > 1 ? splitMessage[1] : "";

        return switch (header) {
            case CMD_BCST -> new OkMessage(body);
//            case CMD_GRP -> handleGRPMessage(body);
//            case CMD_MSG -> handleMSGMessage(body);
            default -> new OkMessage(header);
        };
    }

//    private OkMessage handleMSGMessage(String message) {
//    }
//
//    private OkMessage handleGRPMessage(String message) {
//    }
}
