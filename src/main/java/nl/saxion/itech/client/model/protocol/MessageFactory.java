package nl.saxion.itech.client.model.protocol;

import nl.saxion.itech.client.model.protocol.messages.receivable.ErrorMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.InfoMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.OkBroadcastMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.OkConnectMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.OkMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.ReceivableMessage;

public class MessageFactory {
    private static final String CMD_OK = "OK";
    private static final String CMD_INFO = "INFO";
    private static final String CMD_CONN = "CONN";
    private static final String CMD_BCST = "BCST";
    private static final String CMD_GRP = "GRP";
    private static final String CMD_MSG = "MSG";
    // alt: hard code protocol header in each message

    public ReceivableMessage getMessage(String message) {
        String[] splitMessage = parseMessage(message);
        String header = splitMessage[0];
        String body = splitMessage[1];

        return switch (header) {
            case CMD_INFO -> new InfoMessage(body);
            case CMD_OK -> handleOkMessage(body);
            default -> new ErrorMessage(header, body);
        };
    }

    private String[] parseMessage(String message) {
        return message.split(" ", 2);
    }

    private OkMessage handleOkMessage(String message) {
        String[] splitMessage = parseMessage(message);
        String header = splitMessage[0];
        String body = splitMessage[1];

        return switch (header) {
            case CMD_CONN -> new OkConnectMessage(body);
            case CMD_BCST -> new OkBroadcastMessage(body);
//            case CMD_GRP -> handleGRPMessage(body);
//            case CMD_MSG -> handleMSGMessage(body);
            default -> new OkConnectMessage(header);
        };
    }

//    private OkMessage handleMSGMessage(String message) {
//    }
//
//    private OkMessage handleGRPMessage(String message) {
//    }
}
