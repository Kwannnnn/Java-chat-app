package nl.saxion.itech.server.model.protocol;

import nl.saxion.itech.server.model.InvalidUsernameException;

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

    public Message getMessage(String header, String message) {
        Message result;
        try {
            switch (header) {
                case CMD_INFO -> result = new InfoMessage(message);
                case CMD_CONN -> result = new ConnectMessage(message);
                case CMD_OK -> result = new OkMessage(message);
                case CMD_BCST -> result = new BroadcastMessage(message);
                default -> result = new ErrorMessage(header, message);
            }
        } catch (InvalidUsernameException e) {
            result = new ErrorMessage(e.getCode(), e.getMessage());
        }

        return result;
    }
}
