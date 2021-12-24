package nl.saxion.itech.client.newDesign;

import nl.saxion.itech.client.ChatClient;

import static nl.saxion.itech.shared.ProtocolConstants.*;

import nl.saxion.itech.client.ProtocolInterpreter;

public class ServerMessageHandler {
    private final ChatClient client;

    public ServerMessageHandler(ChatClient client) {
        this.client = client;
    }

    public void handle(String rawMessage) {
        String[] splitMessage = parseMessage(rawMessage);
        String header = splitMessage[0];
        String body = splitMessage.length > 1 ? splitMessage[1] : " ";

        switch (header) {
            case CMD_INFO -> handleInfoMessage(body);
            case CMD_BCST -> handleBroadcastMessage(body);
            case CMD_OK -> handleOKMessage(body);
            case CMD_PING -> handlePingMessage();
            case CMD_MSG -> handleDirectMessage(body);
            case CMD_GRP -> handleGroupMessage(body);
            case CMD_ALL -> handleAllMessage(body);
            case CMD_DSCN -> handleDisconnectMessage();
            default -> handleErrorMessage(body);
        }
    }

    private void handleBroadcastMessage(String message) {
        String[] splitMessage = parseMessage(message);
        String sender = splitMessage[0];
        String body = splitMessage.length > 1 ? splitMessage[1] : " ";
        ProtocolInterpreter.showBroadcastMessage(sender, body);
    }

    private void handleInfoMessage(String message) {
        ProtocolInterpreter.showWelcomeMessage(message);
    }

    private void handleGroupMessage(String message) {
        String[] splitMessage = parseMessage(message);
        String header = splitMessage[0];
        String body = splitMessage.length > 1 ? splitMessage[1] : " ";

        switch (header) {
            case CMD_JOIN -> handleGroupJoinMessage(body);
            case CMD_MSG -> handleGroupMessageMessage(body);
        }
    }

    private void handleGroupMessageMessage(String message) {
        String[] splitMessage = parseMessage(message);
        String groupName = splitMessage[0];
        String body = splitMessage.length > 1 ? splitMessage[1] : " ";
        ProtocolInterpreter.showGroupMessageMessage(groupName, body);
    }

    private void handleOKMessage(String message) {
        String[] splitMessage = parseMessage(message);
        String header = splitMessage[0];
        String body = splitMessage.length > 1 ? splitMessage[1] : " ";

        switch (header) {
            case CMD_CONN -> handleOkConnectMessage(body);
            case CMD_BCST -> handleOkBroadcastMessage(body);
            case CMD_GRP -> handleOkGroupMessage(body);
            case CMD_MSG -> handleOkDirectMessage(body);
        }
    }

    private void handleGroupJoinMessage(String message) {
        String[] splitMessage = parseMessage(message);
        String groupName = splitMessage[0];
        String newMember = splitMessage.length > 1 ? splitMessage[1] : " ";
        ProtocolInterpreter.showGroupJoinMessage(groupName, newMember);
    }

    private void handlePingMessage() {
        this.client.addMessageToQueue(new BaseMessage(CMD_PONG,""));
    }

    private void handleErrorMessage(String message) {
        ProtocolInterpreter.showErrorMessage(message);
    }

    private void handleAllMessage(String message) {
        ProtocolInterpreter.showSuccessfulAllMessage(message.split(","));
    }

    private void handleDisconnectMessage() {
        ProtocolInterpreter.showSuccessfulDisconnectMessage();
        this.client.closeConnection();
        //TODO: decide what to do
    }


    private void handleOkDirectMessage(String message) {
        String[] splitMessage = parseMessage(message);
        String recipient = splitMessage[0];
        String body = splitMessage.length > 1 ? splitMessage[1] : " ";
        ProtocolInterpreter.showSuccessfulDirectMessage(recipient, body);
    }

    private void handleDirectMessage(String message) {
        String[] splitMessage = parseMessage(message);
        String sender = splitMessage[0];
        String directMessage = splitMessage.length > 1 ? splitMessage[1] : " ";
        ProtocolInterpreter.showIncomingDirectMessage(sender, directMessage);
    }

    private void handleOkGroupMessage(String message) {
        String[] splitMessage = parseMessage(message);
        String header = splitMessage[0];
        String body = splitMessage.length > 1 ? splitMessage[1] : " ";

        switch (header) {
            case CMD_ALL -> handleOkGroupAllMessage(body);
            case CMD_NEW -> handleOkGroupNewMessage(body);
            case CMD_JOIN -> handleOkGroupJoinMessage(body);
            case CMD_MSG -> handleOkGroupMessageMessage(body);
            case CMD_DSCN -> handleOkGroupDisconnectMessage(body);
        }

    }

    private void handleOkGroupDisconnectMessage(String message) {
        ProtocolInterpreter.showSuccessfulGroupDisconnectMessage(message);
    }

    private void handleOkGroupMessageMessage(String message) {
        String[] splitMessage = parseMessage(message);
        String groupName = splitMessage[0];
        String groupMessage = splitMessage.length > 1 ? splitMessage[1] : " ";
        ProtocolInterpreter.showSuccessfulGroupMessageMessage(groupName, groupMessage);
    }

    private void handleOkGroupJoinMessage(String message) {
        ProtocolInterpreter.showSuccessfulGroupJoinMessage(message);
    }

    private void handleOkGroupNewMessage(String message) {
        ProtocolInterpreter.showSuccessfulGroupNewMessage(message);
    }

    private void handleOkGroupAllMessage(String message) {
        ProtocolInterpreter.showSuccessfulGroupAllMessage(message.split(","));
    }

    private void handleOkBroadcastMessage(String body) {
        ProtocolInterpreter.showSuccessfulBroadcastMessage(body);
    }

    private void handleOkConnectMessage(String username) {
        this.client.setCurrentUser(username);
        ProtocolInterpreter.showSuccessfulLoginMessage();
        ProtocolInterpreter.promptMenuMessage();
    }

    private String[] parseMessage(String message) {
        return message.split(" ", 2);
    }

}
