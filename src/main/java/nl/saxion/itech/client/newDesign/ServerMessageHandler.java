package nl.saxion.itech.client.newDesign;

import nl.saxion.itech.client.ChatClient;

import static nl.saxion.itech.shared.ProtocolConstants.*;

import nl.saxion.itech.client.ProtocolInterpreter;
import nl.saxion.itech.client.threads.FileDownloadThread;
import nl.saxion.itech.client.threads.FileUploadThread;
import nl.saxion.itech.shared.security.util.SecurityUtil;

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
                case CMD_DSCN -> handleDisconnectMessage(payload);
                case CMD_SESSION -> handleSessionMessage(payload);
                default -> handleErrorMessage(payload);
            }
        } catch (NoSuchElementException e) {
            unknownResponseFromServer();
        }
    }

    private void handleSessionMessage(StringTokenizer payload) {
        String username = payload.nextToken();
        String encryptedSessionKey = payload.nextToken();
        var sessionKeyString = SecurityUtil.decrypt(encryptedSessionKey, this.client.getPrivateKey(), "RSA");
        var sessionKey = SecurityUtil.getSessionKey(sessionKeyString);

        var clientEntity = this.client.getClientEntity(username);

        if (clientEntity.isEmpty()) {
            this.client.addConnectedClient(new ClientEntity(username, sessionKey));
        } else {
            clientEntity.get().setSessionKey(sessionKey);
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
    private void handleDisconnectMessage(StringTokenizer payload) {
        String username = payload.nextToken();
        this.client.removeConnectClient(username);
        // TODO: Use assertion
        System.out.println(this.client.getClientEntity(username));
        //TODO: say sth maybe
    }

    // direct message
    private void handleDirectMessage(StringTokenizer payload) {
        String sender = payload.nextToken();
        String encryptedMessage = payload.nextToken();

        var clientEntityOptional = this.client.getClientEntity(sender);
        if (clientEntityOptional.isEmpty()) {
            //TODO: say sth
            return;
        }

        var sessionKey = clientEntityOptional.get().getSessionKey();

        if (sessionKey == null) {
            // TODO: say something
            return;
        }

        String decryptedMessage = SecurityUtil.decrypt(encryptedMessage, sessionKey, "AES");
        ProtocolInterpreter.showIncomingDirectMessage(sender, decryptedMessage);
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
        String header = payload.nextToken();
        String fileID = payload.nextToken();

        try {
            switch (header) {
                case CMD_DOWNLOAD -> handleFileTransferDownloadMessage(payload, fileID);
                case CMD_UPLOAD -> handleFileTransferUploadMessage(payload, fileID);
                case CMD_SUCCESS -> ProtocolInterpreter.showFileTransferSuccessMessage(fileID);
                case CMD_FAIL -> ProtocolInterpreter.showFileTransferFailMessage(fileID);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleFileTransferUploadMessage(StringTokenizer payload, String fileID) throws IOException {
        String portNumber = payload.nextToken();
        // TODO: maybe host should not be hard coded
        var socket = new Socket("127.0.0.1", Integer.parseInt(portNumber));

        ProtocolInterpreter.showFileTransferMessage(fileID, portNumber);
        new FileUploadThread(client, fileID, socket).start();
    }

    private void handleFileTransferDownloadMessage(StringTokenizer payload, String fileID) throws IOException {
        String portNumber = payload.nextToken();
        // TODO: maybe host should not be hard coded
        var socket = new Socket("127.0.0.1", Integer.parseInt(portNumber));

        ProtocolInterpreter.showFileTransferMessage(fileID, portNumber);
        new FileDownloadThread(client, fileID, socket).start();
    }

    private void handleFileAckMessage(StringTokenizer payload) {
        var header = payload.nextToken().toUpperCase();
        String fileID = payload.nextToken();

        switch (header) {
            case CMD_ACCEPT -> ProtocolInterpreter.showFileAckAcceptMessage(fileID);
            case CMD_DENY -> {
                this.client.removeFileToSend(fileID);
                ProtocolInterpreter.showFileAckDenyMessage(fileID);
            }
            default -> unknownResponseFromServer();
        }
    }

    private void handleFileRequestMessage(StringTokenizer payload) {
        String transferID = payload.nextToken();
        String fileName = payload.nextToken();
        String fileSizeString = payload.nextToken();
        String checksum = payload.nextToken();
        String sender = payload.nextToken();

        var fileSize = Integer.parseInt(fileSizeString);

        this.client.addFileToReceive(new FileObject(transferID, fileName, fileSize, checksum));
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
            case CMD_ENCRYPT -> handleOkEncryptionMessage(payload);
            case CMD_PUBK -> handleOkPubkMessage(payload);
            case CMD_DSCN -> handleOkDscnMessage();
            default -> unknownResponseFromServer();
        }
    }

    private void handleOkDscnMessage() {
        this.client.closeConnection();
        ProtocolInterpreter.showSuccessfulDisconnectMessage();
    }

    private void handleOkPubkMessage(StringTokenizer payload) {
        var username = payload.nextToken();
        var publicKey = SecurityUtil.getPublicKeyFromString(payload.nextToken());

        this.client.addConnectedClient(new ClientEntity(username, publicKey));
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

    //region ok encryption messages
    //================================================================================
    private void handleOkEncryptionMessage(StringTokenizer payload) {
        String header = payload.nextToken().toUpperCase();

        switch (header) {
            case CMD_SESSION -> handleOkEncryptionSessionMessage(payload);
            default -> unknownResponseFromServer();
        }
    }

    private void handleOkEncryptionSessionMessage(StringTokenizer payload) {
        String header = payload.nextToken().toUpperCase();

        switch (header) {
            case CMD_SEND -> handleOkEncryptionSessionSendMessage(payload);
            case CMD_REQ -> handleOkEncryptionSessionRequestMessage(payload);
            default -> unknownResponseFromServer();
        }
    }

    private void handleOkEncryptionSessionRequestMessage(StringTokenizer payload) {
        String recipientUsername = payload.nextToken();
        ProtocolInterpreter.showSuccessfulEncryptionSessionRequestMessage(recipientUsername);
    }

    private void handleOkEncryptionSessionSendMessage(StringTokenizer payload) {
        String recipientUsername = payload.nextToken();
        ProtocolInterpreter.showSuccessfulEncryptionSessionSendMessage(recipientUsername);
    }

    //================================================================================
    //endregion


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
        String fileId = payload.nextToken();
        String fileName = payload.nextToken();
        String fileSizeString = payload.nextToken();
        int fileSize = Integer.parseInt(fileSizeString);
        String checksum = payload.nextToken();
        String recipient = payload.nextToken();

        this.client.addFileToSend(new FileObject(fileId, fileName, fileSize, checksum));
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

    //region ok encryption messages
    //================================================================================

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