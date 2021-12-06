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
        return switch (header) {
            case CMD_INFO -> new InfoMessage(message);
            case CMD_CONN -> {
                try {
                    new ConnectMessage(message);
                } catch (InvalidUsernameException e) {
                    new ErrorMessage(e.getCode(), e.getMessage());
                }
            }
            case CMD_OK -> new OkMessage(message);
            case CMD_BCST -> new BroadcastMessage(message);
            default -> new ErrorMessage(header, message);
        };
    }
}
