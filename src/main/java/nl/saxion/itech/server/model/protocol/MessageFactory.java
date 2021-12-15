package nl.saxion.itech.server.model.protocol;

public class MessageFactory {
    public BaseMessage getMessage(String message) {
        var splitMessage = parseMessage(message);
        var header = splitMessage[0];
        var body = splitMessage.length > 1 ? splitMessage[1] : "";

        return switch (header) {
            case ProtocolConstants.CMD_CONN -> new BaseMessage(ProtocolConstants.CMD_CONN, body);
            case ProtocolConstants.CMD_BCST -> new BaseMessage(ProtocolConstants.CMD_BCST, body);
            case ProtocolConstants.CMD_QUIT -> new BaseMessage(ProtocolConstants.CMD_QUIT, null);
            case ProtocolConstants.CMD_PONG -> new BaseMessage(ProtocolConstants.CMD_PONG, null);
            default -> new BaseMessage(ProtocolConstants.CMD_ER00, ProtocolConstants.ER00_BODY);
        };
    }

    private String[] parseMessage(String message) {
        return message.split(" ", 2);
    }
}
