package nl.saxion.itech.client.newDesign;

import nl.saxion.itech.client.ChatClient;

import static nl.saxion.itech.shared.ProtocolConstants.*;

import nl.saxion.itech.client.ProtocolInterpreter;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class ServerMessageHandler {
    private final ChatClient client;

    public ServerMessageHandler(ChatClient client) {
        this.client = client;
    }

    public void handle(String rawMessage) {
        var payload = new StringTokenizer(rawMessage);

        try {
            var header = payload.nextToken().toUpperCase();

            switch (header) {
                case CMD_INFO -> handleInfoMessage(payload);
                case CMD_BCST -> handleBroadcastMessage(payload);
                case CMD_OK -> handleOKMessage(payload);
                case CMD_PING -> handlePingMessage();
                case CMD_MSG -> handleDirectMessage(payload);
                case CMD_GRP -> handleGroupMessage(payload);
                case CMD_ALL -> handleAllMessage(payload);
                case CMD_DSCN -> handleDisconnectMessage();
                default -> handleErrorMessage(payload);
            }
        } catch (NoSuchElementException e) {
            unknownResponseFromServer();
        }
    }

    private void unknownResponseFromServer() {
    }

    private void handleBroadcastMessage(StringTokenizer payload) {
        try {
            String sender = payload.nextToken();
            String body = getRemainingTokens(payload);;
            ProtocolInterpreter.showBroadcastMessage(sender, body);
        } catch (NoSuchElementException e) {
            unknownResponseFromServer();
        }
    }

    private void handleInfoMessage(StringTokenizer payload) {
        try {
            String message = getRemainingTokens(payload);
            ProtocolInterpreter.showWelcomeMessage(message);
        } catch (NoSuchElementException e) {
            unknownResponseFromServer();
        }
    }

    private void handleGroupMessage(StringTokenizer payload) {
        try {
            String header = payload.nextToken();

            switch (header) {
                case CMD_JOIN -> handleGroupJoinMessage(payload);
                case CMD_MSG -> handleGroupMessageMessage(payload);
            }
        } catch (NoSuchElementException e) {
            unknownResponseFromServer();
        }
    }

    private void handleGroupMessageMessage(StringTokenizer payload) {
        try {
            String groupName = payload.nextToken();
            String body = getRemainingTokens(payload);
            ProtocolInterpreter.showGroupMessageMessage(groupName, body);
        } catch (NoSuchElementException e) {
            unknownResponseFromServer();
        }
    }

    private void handleOKMessage(StringTokenizer payload) {
        try {
            String header = payload.nextToken();
            String body = getRemainingTokens(payload);;

            switch (header) {
                case CMD_CONN -> handleOkConnectMessage(body);
                case CMD_BCST -> handleOkBroadcastMessage(body);
                case CMD_GRP -> handleOkGroupMessage(payload);
                case CMD_MSG -> handleOkDirectMessage(payload);
            }
        } catch (NoSuchElementException e) {
            unknownResponseFromServer();
        }
    }

    private void handleGroupJoinMessage(StringTokenizer payload) {
        try {
            String groupName = payload.nextToken();
            String newMember = payload.nextToken();
            ProtocolInterpreter.showGroupJoinMessage(groupName, newMember);
        } catch (NoSuchElementException e) {
            unknownResponseFromServer();
        }
    }

    private void handlePingMessage() {
        this.client.addMessageToQueue(new BaseMessage(CMD_PONG));
    }

    private void handleErrorMessage(StringTokenizer payload) {
        try {
            String message = getRemainingTokens(payload);;
            ProtocolInterpreter.showErrorMessage(message);
        } catch (NoSuchElementException e) {
            unknownResponseFromServer();
        }
    }

    private void handleAllMessage(StringTokenizer payload) {
        String clients = getRemainingTokens(payload);;

        ProtocolInterpreter.showSuccessfulAllMessage(clients.split(","));
    }

    private void handleDisconnectMessage() {
        ProtocolInterpreter.showSuccessfulDisconnectMessage();
        this.client.closeConnection();
        //TODO: decide what to do
    }


    private void handleOkDirectMessage(StringTokenizer payload) {
        try {
            String recipient = payload.nextToken();
            String body = getRemainingTokens(payload);;
            ProtocolInterpreter.showSuccessfulDirectMessage(recipient, body);
        } catch (NoSuchElementException e) {
            unknownResponseFromServer();
        }
    }

    private void handleDirectMessage(StringTokenizer payload) {
        try {
            String sender = payload.nextToken();
            String directMessage = getRemainingTokens(payload);;
            ProtocolInterpreter.showIncomingDirectMessage(sender, directMessage);
        } catch (NoSuchElementException e) {
            unknownResponseFromServer();
        }
    }

    private void handleOkGroupMessage(StringTokenizer payload) {
        try {
            String header = payload.nextToken();
            String body = payload.nextToken();

            switch (header) {
                case CMD_ALL -> handleOkGroupAllMessage(body);
                case CMD_NEW -> handleOkGroupNewMessage(body);
                case CMD_JOIN -> handleOkGroupJoinMessage(body);
                case CMD_MSG -> handleOkGroupMessageMessage(payload);
                case CMD_DSCN -> handleOkGroupDisconnectMessage(body);
            }
        } catch (NoSuchElementException e) {
            unknownResponseFromServer();
        }
    }

    private void handleOkGroupDisconnectMessage(String message) {
        ProtocolInterpreter.showSuccessfulGroupDisconnectMessage(message);
    }

    private void handleOkGroupMessageMessage(StringTokenizer payload) {
        String groupName = payload.nextToken();
        String groupMessage = getRemainingTokens(payload);
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

    private void handleOkBroadcastMessage(String message) {
        ProtocolInterpreter.showSuccessfulBroadcastMessage(message);
    }

    private void handleOkConnectMessage(String username) {
        this.client.setCurrentUser(username);
        ProtocolInterpreter.showSuccessfulLoginMessage();
        ProtocolInterpreter.promptMenuMessage();
    }

    private String getRemainingTokens(StringTokenizer tokenizer) throws NoSuchElementException {
        var remainder = tokenizer.nextToken("");
        return remainder.trim();
    }
}
