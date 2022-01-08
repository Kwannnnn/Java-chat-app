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
                case CMD_FILE -> handleFileMessage(payload);
                case CMD_DSCN -> handleDisconnectMessage();
                default -> handleErrorMessage(payload);
            }
        } catch (NoSuchElementException e) {
            unknownResponseFromServer();
        }
    }

    private void handleFileMessage(StringTokenizer payload) {
        var header = payload.nextToken().toUpperCase();

        switch (header) {
            case CMD_REQ -> handleFileRequestMessage(payload);
            case CMD_ACK -> handleFileAckMessage(payload);
            case CMD_TR -> handleFileTransferMessage(payload);
            default -> unknownResponseFromServer();
        }
    }

    private void handleFileAckMessage(StringTokenizer payload) {
        var header = payload.nextToken().toUpperCase();
        String transferID = payload.nextToken();

        switch (header) {
            case CMD_ACCEPT -> ProtocolInterpreter.showFileAckDenyMessage(transferID);
            case CMD_DENY -> ProtocolInterpreter.showFileAckAcceptMessage(transferID);
            default -> unknownResponseFromServer();
        }
    }

    private void handleFileTransferMessage(StringTokenizer payload) {
        String header = payload.nextToken().toUpperCase();
        String transferID = payload.nextToken();
        String portNumber = payload.nextToken();

        switch (header) {
            case CMD_UPLOAD -> {
                // TODO: do upload
//                ProtocolInterpreter.showFileTransferUploadMessage(transferID);
            }
            case CMD_DOWNLOAD -> {
                // TODO: do download
//                ProtocolInterpreter.showFileDownloadMessage(transferID);
            }
            default -> unknownResponseFromServer();
        }
    }

    private void handleFileRequestMessage(StringTokenizer payload) {
        String transferID = payload.nextToken();
        String sender = payload.nextToken();
        String fileName = payload.nextToken();
        String fileSize = payload.nextToken();

        ProtocolInterpreter.showFileRequestMessage(transferID, sender, fileName, fileSize);
    }

    private void unknownResponseFromServer() {
    }

    private void handleBroadcastMessage(StringTokenizer payload) {
        String sender = payload.nextToken();
        String body = getRemainingTokens(payload);
        ProtocolInterpreter.showBroadcastMessage(sender, body);
    }

    private void handleInfoMessage(StringTokenizer payload) {
        String message = getRemainingTokens(payload);
        ProtocolInterpreter.showWelcomeMessage(message);
    }

    private void handleGroupMessage(StringTokenizer payload) {
        String header = payload.nextToken().toUpperCase();

        switch (header) {
            case CMD_JOIN -> handleGroupJoinMessage(payload);
            case CMD_MSG -> handleGroupMessageMessage(payload);
            default -> unknownResponseFromServer();
        }
    }

    private void handleGroupMessageMessage(StringTokenizer payload) {
        String groupName = payload.nextToken();
        String body = getRemainingTokens(payload);
        ProtocolInterpreter.showGroupMessageMessage(groupName, body);
    }

    private void handleOKMessage(StringTokenizer payload) {
        String header = payload.nextToken().toUpperCase();
        String body = getRemainingTokens(payload);

        switch (header) {
            case CMD_CONN -> handleOkConnectMessage(body);
            case CMD_BCST -> handleOkBroadcastMessage(body);
            case CMD_GRP -> handleOkGroupMessage(payload);
            case CMD_MSG -> handleOkDirectMessage(payload);
            case CMD_FILE -> handleOkFileMessage(payload);
            default -> unknownResponseFromServer();
        }
    }

    private void handleOkFileMessage(StringTokenizer payload) {
        String header = payload.nextToken().toUpperCase();

        switch (header) {
            case CMD_SEND -> handleOkFileSendMessage(payload);
            case CMD_ACK -> handleOkFileAcknowledgeMessage(payload);
            default -> unknownResponseFromServer();
        }
    }

    private void handleOkFileAcknowledgeMessage(StringTokenizer payload) {
        String header = payload.nextToken().toUpperCase();

        switch (header) {
            case CMD_ACCEPT -> handleOkFileAcknowledgeAcceptMessage(payload);
            case CMD_DENY -> handleOkFileAcknowledgeDenyMessage(payload);
            default -> unknownResponseFromServer();
        }
    }

    private void handleOkFileAcknowledgeDenyMessage(StringTokenizer payload) {
        String transferID = payload.nextToken();
        ProtocolInterpreter.showSuccessfulAcknowledgeDenyMessage(transferID);
    }

    private void handleOkFileAcknowledgeAcceptMessage(StringTokenizer payload) {
        String transferID = payload.nextToken();
        ProtocolInterpreter.showSuccessfulAcknowledgeAcceptMessage(transferID);
    }

    private void handleOkFileSendMessage(StringTokenizer payload) {
        String fileName = payload.nextToken();
        String fileSize = payload.nextToken();
        String recipient = payload.nextToken();

        ProtocolInterpreter.showSuccessfulFileSendMessage(fileName, fileSize, recipient);
    }

    private void handleGroupJoinMessage(StringTokenizer payload) {
        String groupName = payload.nextToken();
        String newMember = payload.nextToken();
        ProtocolInterpreter.showGroupJoinMessage(groupName, newMember);
    }

    private void handlePingMessage() {
        this.client.addMessageToQueue(new BaseMessage(CMD_PONG));
    }

    private void handleErrorMessage(StringTokenizer payload) {
        String message = getRemainingTokens(payload);
        ProtocolInterpreter.showErrorMessage(message);
    }

    private void handleAllMessage(StringTokenizer payload) {
        String clients = getRemainingTokens(payload);
        ProtocolInterpreter.showSuccessfulAllMessage(clients.split(","));
    }

    private void handleDisconnectMessage() {
        ProtocolInterpreter.showSuccessfulDisconnectMessage();
        this.client.closeConnection();
        //TODO: decide what to do
    }

    private void handleOkDirectMessage(StringTokenizer payload) {
        String recipient = payload.nextToken();
        String body = getRemainingTokens(payload);
        ProtocolInterpreter.showSuccessfulDirectMessage(recipient, body);
    }

    private void handleDirectMessage(StringTokenizer payload) {
        String sender = payload.nextToken();
        String directMessage = getRemainingTokens(payload);
        ProtocolInterpreter.showIncomingDirectMessage(sender, directMessage);
    }

    private void handleOkGroupMessage(StringTokenizer payload) {
        String header = payload.nextToken().toUpperCase();
        String body = payload.nextToken();

        switch (header) {
            case CMD_ALL -> handleOkGroupAllMessage(body);
            case CMD_NEW -> handleOkGroupNewMessage(body);
            case CMD_JOIN -> handleOkGroupJoinMessage(body);
            case CMD_MSG -> handleOkGroupMessageMessage(payload);
            case CMD_DSCN -> handleOkGroupDisconnectMessage(body);
            default -> unknownResponseFromServer();
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