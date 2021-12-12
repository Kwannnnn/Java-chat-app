package nl.saxion.itech.server.model.protocol;

import nl.saxion.itech.server.model.exceptions.InvalidUsernameException;
import nl.saxion.itech.server.model.protocol.messages.*;

public class MessageFactory {
    private static final String CMD_CONN = "CONN";
    private static final String CMD_BCST = "BCST";
    private static final String CMD_OK = "OK";
    private static final String CMD_INFO = "INFO";
    private static final String CMD_PING = "PING";
    private static final String CMD_PONG = "PONG";
    private static final String CMD_QUIT = "QUIT";
    private static final String CMD_ER00 = "ER00";
    private static final String CMD_ER01 = "ER01";
    private static final String CMD_ER02 = "ER02";
    private static final String CMD_ER03 = "ER03";

    public Message getMessage(String message) {
        String[] splitMessage = parseMessage(message);
        String header = splitMessage[0];
        String body = splitMessage[1];

        return switch (header) {
                case CMD_INFO -> new InfoMessage(message);
//                case CMD_CONN -> new ConnectMessage(message);
                case CMD_OK -> new OkMessage(message);
                case CMD_BCST -> new BroadcastMessage(message);
                default -> new ErrorMessage(header, message);
            };

    }

    private String[] parseMessage(String message) {
        return message.split(" ", 2);
    }

}
