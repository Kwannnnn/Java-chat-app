package nl.saxion.itech.client.newDesign;
import nl.saxion.itech.client.ChatClient;
import static nl.saxion.itech.shared.ProtocolConstants.*;
import nl.saxion.itech.client.ProtocolInterpreter;
import nl.saxion.itech.client.threads.FileDownloadThread;
import nl.saxion.itech.client.threads.FileUploadThread;

import java.io.IOException;
import java.net.Socket;
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


    private void unknownResponseFromServer() {
    }

    // error message
    private void handleErrorMessage(StringTokenizer payload) {
        String message = getRemainingTokens(payload);
        ProtocolInterpreter.showErrorMessage(message);
    }

    // ping message
    private void handlePingMessage() {
        this.client.addMessageToQueue(new BaseMessage(CMD_PONG));
    }

    // all message
    private void handleAllMessage(StringTokenizer payload) {
        String clients = getRemainingTokens(payload);
        ProtocolInterpreter.showSuccessfulAllMessage(clients.split(","));
    }

    // disconnect message
    private void handleDisconnectMessage() {
        this.client.closeConnection();
        // TODO: close all other sockets used by this client
        //TODO: decide what to do
        ProtocolInterpreter.showSuccessfulDisconnectMessage();
    }

    // direct message
    private void handleDirectMessage(StringTokenizer payload) {
        String sender = payload.nextToken();
        String directMessage = getRemainingTokens(payload);
        ProtocolInterpreter.showIncomingDirectMessage(sender, directMessage);
    }

    // broadcast message
    private void handleBroadcastMessage(StringTokenizer payload) {
        String sender = payload.nextToken();
        String body = getRemainingTokens(payload);
        ProtocolInterpreter.showBroadcastMessage(sender, body);
    }

    // info message
    private void handleInfoMessage(StringTokenizer payload) {
        String message = getRemainingTokens(payload);
        ProtocolInterpreter.showWelcomeMessage(message);
    }

    //region file messages
    //================================================================================
    private void handleFileMessage(StringTokenizer payload) {
        var header = payload.nextToken().toUpperCase();

        switch (header) {
            case CMD_REQ -> handleFileRequestMessage(payload);
            case CMD_ACK -> handleFileAckMessage(payload);
            case CMD_TR -> handleFileTransferMessage(payload);
            default -> unknownResponseFromServer();
        }
    }

    private void handleFileTransferMessage(StringTokenizer payload) {
        String mode = payload.nextToken();
        String transferID = payload.nextToken();
        String portNumber = payload.nextToken();

        ProtocolInterpreter.showFileTransferMessage(transferID, portNumber);

        try {
            var socket = new Socket("127.0.0.1", Integer.parseInt(portNumber));
            switch (mode) {
                case "DOWNLOAD" -> new FileDownloadThread(client, transferID, socket).start();
                case "UPLOAD" -> new FileUploadThread(client, transferID, socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleFileAckMessage(StringTokenizer payload) {
        var header = payload.nextToken().toUpperCase();
        String transferID = payload.nextToken();

        switch (header) {
            case CMD_ACCEPT -> ProtocolInterpreter.showFileAckAcceptMessage(transferID);
            case CMD_DENY -> ProtocolInterpreter.showFileAckDenyMessage(transferID);
            default -> unknownResponseFromServer();
        }
    }

    private void handleFileRequestMessage(StringTokenizer payload) {
        String transferID = payload.nextToken();
        String sender = payload.nextToken();
        String fileName = payload.nextToken();
        var fileSizeString = payload.nextToken();
        var fileSize = Integer.parseInt(fileSizeString);

        this.client.addFileToReceive(new File(transferID, fileName, fileSize));
        ProtocolInterpreter.showFileRequestMessage(transferID, sender, fileName, fileSizeString);
    }
    //================================================================================
    //endregion

    // region group messages
    //================================================================================
    private void handleGroupMessage(StringTokenizer payload) {
        String header = payload.nextToken().toUpperCase();

        switch (header) {
            case CMD_JOIN -> handleGroupJoinMessage(payload);
            case CMD_MSG -> handleGroupMessageMessage(payload);
            default -> unknownResponseFromServer();
        }
    }

    private void handleGroupJoinMessage(StringTokenizer payload) {
        String groupName = payload.nextToken();
        String newMember = payload.nextToken();
        ProtocolInterpreter.showGroupJoinMessage(groupName, newMember);
    }

    private void handleGroupMessageMessage(StringTokenizer payload) {
        String groupName = payload.nextToken();
        String body = getRemainingTokens(payload);
        ProtocolInterpreter.showGroupMessageMessage(groupName, body);
    }
    //endregion

    //region ok messages
    //================================================================================
    private void handleOKMessage(StringTokenizer payload) {
        String header = payload.nextToken().toUpperCase();

        switch (header) {
            case CMD_CONN -> handleOkConnectMessage(getRemainingTokens(payload));
            case CMD_BCST -> handleOkBroadcastMessage(getRemainingTokens(payload));
            case CMD_GRP -> handleOkGroupMessage(payload);
            case CMD_MSG -> handleOkDirectMessage(payload);
            case CMD_FILE -> handleOkFileMessage(payload);
            default -> unknownResponseFromServer();
        }
    }

    private void handleOkBroadcastMessage(String message) {
        ProtocolInterpreter.showSuccessfulBroadcastMessage(message);
    }

    private void handleOkConnectMessage(String username) {
        this.client.setCurrentUser(username);
        ProtocolInterpreter.showSuccessfulLoginMessage();
        ProtocolInterpreter.promptMenuMessage();
    }

    private void handleOkDirectMessage(StringTokenizer payload) {
        String recipient = payload.nextToken();
        String body = getRemainingTokens(payload);
        ProtocolInterpreter.showSuccessfulDirectMessage(recipient, body);
    }

    //region Ok file messages
    //================================================================================
    private void handleOkFileMessage(StringTokenizer payload) {
        String header = payload.nextToken().toUpperCase();

        switch (header) {
            case CMD_REQ -> handleOkFileReqMessage(payload);
            case CMD_ACK -> handleOkFileAcknowledgeMessage(payload);
            default -> unknownResponseFromServer();
        }
    }

    private void handleOkFileReqMessage(StringTokenizer payload) {
        var fileId = payload.nextToken();
        String fileName = payload.nextToken();
        var fileSizeString = payload.nextToken();
        var fileSize = Integer.parseInt(fileSizeString);
        String recipient = payload.nextToken();

        this.client.addFileToSend(new File(fileId, fileName, fileSize));
        ProtocolInterpreter.showSuccessfulFileSendMessage(fileName, fileSizeString, recipient);
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
    //================================================================================
    //endregion

    //region Ok group messages
    //================================================================================
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
    //================================================================================
    //endregion

    //================================================================================
    //endregion

    //helper
    private String getRemainingTokens(StringTokenizer tokenizer) throws NoSuchElementException {
        var remainder = tokenizer.nextToken("");
        return remainder.trim();
    }
}