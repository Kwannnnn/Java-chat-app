package nl.saxion.itech.server.model.protocol;

public class ServerLogger implements MessageHandler {
    @Override
    public void handle(BaseMessage message) {
        switch (message.getHeader()) {
            case ProtocolConstants.CMD_CONN -> logConnectMessage(message);
            case ProtocolConstants.CMD_QUIT -> logQuitMessage(message);
            case ProtocolConstants.CMD_BCST -> logBroadcast(message);
            case ProtocolConstants.CMD_PONG -> logPong(message);
            default -> logOutgoing(message);
        }
    }

    private void logOutgoing(BaseMessage message) {

    }

    private void logPong(BaseMessage message) {
    }

    private void logBroadcast(BaseMessage message) {
    }

    private void logQuitMessage(BaseMessage message) {
    }

    private void logConnectMessage(BaseMessage message) {

    }


}
