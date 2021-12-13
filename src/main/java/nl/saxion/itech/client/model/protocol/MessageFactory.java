package nl.saxion.itech.client.model.protocol;

import nl.saxion.itech.client.model.protocol.messages.Message;
import nl.saxion.itech.client.model.protocol.messages.receivable.*;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.*;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.groupMessages.OkGroupAllMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.groupMessages.OkGroupDisconnectMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.groupMessages.OkGroupJoinMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.groupMessages.OkGroupMessageMessage;
import nl.saxion.itech.client.model.protocol.messages.sendable.GroupJoinMessage;
import nl.saxion.itech.client.model.protocol.messages.sendable.GroupMessageMessage;

public class MessageFactory {
    private static final String CMD_OK = "OK";
    private static final String CMD_INFO = "INFO";
    private static final String CMD_CONN = "CONN";
    private static final String CMD_BCST = "BCST";
    private static final String CMD_GRP = "GRP";
    private static final String CMD_MSG = "MSG";
    private static final String CMD_ALL = "ALL";
    private static final String CMD_NEW = "NEW";
    private static final String CMD_JOIN = "JOIN";
    private static final String CMD_DSCN = "DSCN";

    public Message getMessage(String message) {
        String[] splitMessage = parseMessage(message);
        String header = splitMessage[0];
        String body = splitMessage[1];

        return switch (header) {
            case CMD_INFO -> new InfoMessage(body);
            case CMD_OK -> handleOkMessage(body);
            case CMD_GRP -> handleGroupMessage(body);
            default -> new ErrorMessage(header, body);
        };
    }

    private Message handleGroupMessage(String message) {
        String[] splitMessage = parseMessage(message);
        String header = splitMessage[0];
        String body = splitMessage[1];

        return switch (header) {
            case CMD_JOIN -> new GroupJoinMessage(body);
            case CMD_MSG -> new GroupMessageMessage(body);
            default -> new ErrorMessage("ERXX", "Unknown header"); // TODO: figure this out
        };
    }


    private Message handleOkMessage(String message) {
        String[] splitMessage = parseMessage(message);
        String header = splitMessage[0];
        String body = splitMessage.length > 1 ? splitMessage[1] : "";

        return switch (header) {
            case CMD_CONN -> new OkConnectMessage(body);
            case CMD_BCST -> new OkBroadcastMessage(body);
            case CMD_GRP -> handleOkGroupMessage(body);
//            case CMD_MSG -> handleMSGMessage(body);
            default -> new OkConnectMessage(header);
        };
    }

    private Message handleOkGroupMessage(String message) {
        String[] splitMessage = parseMessage(message);
        String header = splitMessage[0];
        String body = splitMessage[1];

        return switch (header) {
            case CMD_ALL -> new OkGroupAllMessage(body);
            case CMD_JOIN -> new OkGroupJoinMessage(body);
            case CMD_MSG -> new OkGroupMessageMessage(body);
            case CMD_DSCN -> new OkGroupDisconnectMessage(body);
            default -> new ErrorMessage("ERXX", "Unknown header"); // TODO: figure this out
        };
    }
//
//    private OkMessage handleMSGMessage(String message) {
//    }

    private String[] parseMessage(String message) {
        return message.split(" ", 2);
    }
}
