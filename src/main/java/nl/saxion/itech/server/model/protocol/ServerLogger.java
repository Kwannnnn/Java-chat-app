package nl.saxion.itech.server.model.protocol;

public class ServerLogger implements MessageHandler {
    @Override
    public void handle(Message message) {
        switch (message.getHeader()) {
            case ProtocolConstants.CMD_CONN -> logConnectMessage(message);
            case ProtocolConstants.CMD_QUIT -> logQuitMessage(message);
            case ProtocolConstants.CMD_BCST -> logBroadcast(message);
            case ProtocolConstants.CMD_PONG -> logPong(message);
            default -> logOutgoing(message);
        }
    }

    private void logOutgoing(Message message) {

    }

    private void logPong(Message message) {
    }

    private void logBroadcast(Message message) {
    }

    private void logQuitMessage(Message message) {
    }

    private void logConnectMessage(Message message) {

    }


}
