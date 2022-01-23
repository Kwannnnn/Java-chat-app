package nl.saxion.itech.server.util;

import nl.saxion.itech.server.message.TextMessage;

import static nl.saxion.itech.shared.ProtocolConstants.*;

/**
 * This class contains helper methods that make it quicker and more convenient to create protocol messages.
 * All methods are static and return ready-to-be-sent TextMessage instances that can be converted into protocol command
 * strings.
 */
public final class ServerMessageDictionary {
    /**
     * @return INFO [welcomeMessage]
     */
    public static TextMessage welcome() {
        return new TextMessage(CMD_INFO, INFO_BODY);
    }

    public static TextMessage session(String senderUsername,
                                  String encryptedSessionKey) {
        return new TextMessage(
                CMD_SESSION,
                senderUsername + " " + encryptedSessionKey);
    }

    /**
     * @param senderUsername the username of the sender of the message
     * @param message the broadcast message body
     * @return BCST [senderUsername] [message]
     */
    public static TextMessage bcst(String senderUsername, String message) {
        return new TextMessage(
                CMD_BCST,
                senderUsername + " " + message);
    }

    /**
     * @param senderUsername the username of the sender of the message
     * @param message the message body
     * @return MSG [senderUsername] [message]
     */
    public static TextMessage msg(String senderUsername,
                                  String message) {
        return new TextMessage(
                CMD_MSG,
                senderUsername + " " + message);
    }

    public static TextMessage dscn(String username) {
        return new TextMessage(CMD_DSCN, username);
    }

    /**
     * @param message the message body
     * @return GRP MSG [group name] [message]
     */
    public static TextMessage grpMsg(String groupName,
                                     String message) {
        return new TextMessage(
                CMD_GRP + " " + CMD_MSG,
                groupName + " " + message);
    }

    /**
     * @param username the username of the user that joined the group
     * @return GRP JOIN [group name] [username]
     */
    public static TextMessage grpJoin(String groupName,
                                     String username) {
        return new TextMessage(
                CMD_GRP + " " + CMD_JOIN,
                groupName + " " + username);
    }

    /**
     * @param fileId the file id of the file being sent
     * @param senderUsername the username of the user sending the file
     * @param fileName the name of the file being sent
     * @param fileSize the size of the file being sent
     * @param checksum the MD5 checksum of the file being sent
     * @return FILE REQ [sender username] [file name] [file size]
     */
    public static TextMessage fileReq(String fileId,
                                      String senderUsername,
                                      String fileName,
                                      long fileSize,
                                      String checksum) {
        return new TextMessage(
                CMD_FILE + " " + CMD_REQ,
                fileId + " " + fileName + " " + fileSize + " " + checksum + " " + senderUsername);
    }

    /**
     * @param transferId the id of the file
     * @param portNumber the port number where the file transfer will happen
     * @return FILE TR UPLOAD [transfer id] [port number]
     */
    public static TextMessage fileTrUpload(String transferId,
                                      int portNumber) {
        return new TextMessage(
                CMD_FILE + " " + CMD_TR,
                CMD_UPLOAD + " " + transferId + " " + portNumber);
    }

    /**
     * @param transferId the id of the file
     * @param portNumber the port number where the file transfer will happen
     * @return FILE TR DOWNLOAD [transfer id] [port number]
     */
    public static TextMessage fileTrDownload(String transferId,
                                      int portNumber) {
        return new TextMessage(
                CMD_FILE + " " + CMD_TR,
                CMD_DOWNLOAD + " " + transferId + " " + portNumber);
    }

    // OK Messages

    /**
     * @param username the username parameter
     * @param publicKey the publicKey parameter
     * @return OK CONN [username]
     */
    public static TextMessage okConn(String username,
                                     String publicKey) {
        return new TextMessage(
                CMD_OK + " " + CMD_CONN,
                username + " " + publicKey);
    }

    /**
     * @param message the message parameter
     * @return OK BCST [message]
     */
    public static TextMessage okBcst(String message) {
        return new TextMessage(
                CMD_OK + " " + CMD_BCST,
                message);
    }

    /**
     * @return OK DSCN
     */
    public static TextMessage okDscn() {
        return new TextMessage(
                CMD_OK + " " + CMD_DSCN);
    }

    /**
     * @param recipientUsername the recipient username parameter
     * @param message the message parameter
     * @return OK MSG [username] [message]
     */
    public static TextMessage okMsg(String recipientUsername, String message) {
        return new TextMessage(
                CMD_OK + " " + CMD_MSG,
                recipientUsername + " " + message);
    }

    /**
     * @param allUsersString a string in the format: username,username,...
     * @return OK ALL [username],[username],...
     */
    public static TextMessage okAll(String allUsersString) {
        return new TextMessage(
                CMD_OK + " " + CMD_ALL,
                allUsersString);
    }

    /**
     * @return OK AUTH
     */
    public static TextMessage okAuth() {
        return new TextMessage(
                CMD_OK + " " + CMD_AUTH);
    }

    /**
     * @param groupName the group name of the newly created group
     * @return OK GRP NEW [group name]
     */
    public static TextMessage okGrpNew(String groupName) {
        return new TextMessage(
                CMD_OK + " " + CMD_GRP + " " + CMD_NEW,
                groupName);
    }

    /**
     * @param allGroupsString  a string in the format: groupName,groupName,...
     * @return OK ALL [username],[username],...
     */
    public static TextMessage okGrpAll(String allGroupsString) {
        return new TextMessage(
                CMD_OK + " " + CMD_GRP + " " + CMD_ALL,
                allGroupsString);
    }

    /**
     * @param groupName the group name of the recently joined group
     * @return OK GRP JOIN [group name]
     */
    public static TextMessage okGrpJoin(String groupName) {
        return new TextMessage(
                CMD_OK + " " + CMD_GRP + " " + CMD_JOIN,
                groupName);
    }

    /**
     * @param groupName the group name of the group to message
     * @param message the message to send
     * @return OK GRP MSG [group name] [message]
     */
    public static TextMessage okGrpMsg(String groupName, String message) {
        return new TextMessage(
                CMD_OK + " " + CMD_GRP + " " + CMD_MSG,
                groupName + " " + message);
    }

    /**
     * @param groupName the group name of the group to leave
     * @return OK GRP DSCN [group name]
     */
    public static TextMessage okGrpDscn(String groupName) {
        return new TextMessage(
                CMD_OK + " " + CMD_GRP + " " + CMD_DSCN,
                groupName);
    }

    /**
     * @param fileId id of the file to be sent
     * @return FILE ACK DENY [file id]
     */
    public static TextMessage fileAckDeny(String fileId) {
        return new TextMessage(
                CMD_FILE + " " + CMD_ACK,
                CMD_DENY + " " + fileId);
    }

    /**
     * @param fileId id of the file to be sent
     * @return FILE ACK ACCEPT [file id]
     */
    public static TextMessage fileAckAccept(String fileId) {
        return new TextMessage(
                CMD_FILE + " " + CMD_ACK,
                CMD_ACCEPT + " " + fileId);
    }

    /**
     * @param fileId id of the file to be sent
     * @return FILE TR SUCCESS [file id]
     */
    public static TextMessage fileCompleteSuccess(String fileId) {
        return new TextMessage(
                CMD_FILE + " " + CMD_COMPLETE,
                CMD_SUCCESS + " " + fileId);
    }

    /**
     * @param fileId id of the file to be sent
     * @return FILE TR FAIL [file id]
     */
    public static TextMessage fileCompleteFail(String fileId) {
        return new TextMessage(
                CMD_FILE + " " + CMD_COMPLETE,
                CMD_FAIL + " " + fileId);
    }

    /**
     * @param recipientUsername username of the recipient
     * @return ENCRYPT SESSION REQ [recipient username]
     */
    public static TextMessage encryptionSessionRequest(String recipientUsername) {
        return new TextMessage(CMD_ENCRYPT + " " + CMD_SESSION,
                CMD_REQ + " " + recipientUsername);
    }

    /**
     * @param recipientUsername username of the recipient
     * @param sessionKey base64 string of the key to be sent
     * @return ENCRYPT SESSION SEND [recipient username] [session key]
     */
    public static TextMessage encryptionSessionSend(String recipientUsername, String sessionKey) {
        return new TextMessage(CMD_ENCRYPT + " " + CMD_SESSION,
                CMD_SEND + " " + recipientUsername + " " + sessionKey);
    }

    /**
     * @param recipientUsername username of the recipient
     * @return OK ENCRYPT SESSION REQ [recipient username]
     */
    public static TextMessage okEncryptionSessionRequest(String recipientUsername) {
        return new TextMessage(CMD_OK + " " + CMD_ENCRYPT + " " + CMD_SESSION,
                CMD_REQ + " " + recipientUsername);
    }

    /**
     * @param recipientUsername username of the recipient
     * @param sessionKey base64 string of the key to be sent
     * @return OK ENCRYPT SESSION SEND [recipient username] [session key]
     */
    public static TextMessage okEncryptionSessionSend(String recipientUsername, String sessionKey) {
        return new TextMessage(CMD_OK + " " + CMD_ENCRYPT + " " + CMD_SESSION,
                CMD_SEND + " " + recipientUsername + " " + sessionKey);
    }

    /**
     * @param username username of the recipient
     * @param publicKey base64 string of the key to be sent
     * @return OK PUBK [recipient username] [session key]
     */
    public static TextMessage okPubk(String username, String publicKey) {
        return new TextMessage(CMD_OK + " " + CMD_PUBK,
                username + " " + publicKey);
    }

    /**
     * @param username username of the intended client
     * @param sessionKey base64 string of the key to be sent
     * @return OK SESSION [username] [session key]
     */
    public static TextMessage okSession(String username, String sessionKey) {
        return new TextMessage(CMD_OK + " " + CMD_SESSION,
                username + " " + sessionKey);
    }

    /**
     * @param fileId the id of the file being sent
     * @param filename the name of the file being sent
     * @param recipientUsername the username of the user sending the file
     * @param checksum the MD5 checksum of the file being sent
     * @return OK FILE SEND [file name] [recipient username]
     */
    public static TextMessage okFileReq(String fileId, String filename,
                                        long fileSize,
                                        String recipientUsername,
                                        String checksum) {
        return new TextMessage(
                CMD_OK + " " + CMD_FILE + " " + CMD_REQ,
                fileId + " " + filename + " " + fileSize + " " + checksum + " " + recipientUsername);
    }

    /**
     * @param fileId the id of the file being accepted
     * @return OK FILE ACK ACCEPT [file id]
     */
    public static TextMessage okFileAckAccept(String fileId) {
        return new TextMessage(
                CMD_OK + " " + CMD_FILE + " " + CMD_ACK,
                CMD_ACCEPT + " " + fileId);
    }

    /**
     * @param fileId the id of the file being denied
     * @return OK FILE ACK DENY [file id]
     */
    public static TextMessage okFileAckDeny(String fileId) {
        return new TextMessage(
                CMD_OK + " " + CMD_FILE + " " + CMD_ACK,
                CMD_DENY + " " + fileId);
    }

    // Error messages

    /**
     * @return ER00 Unknown command
     */
    public static TextMessage unknownCommandError() {
        return new TextMessage(CMD_ER00, ER00_BODY);
    }

    /**
     * @return ER01 User already logged in
     */
    public static TextMessage userAlreadyLoggedInError() {
        return new TextMessage(CMD_ER01, ER01_BODY);
    }

    /**
     * @return ER02 Username has an invalid format
     * (only characters, numbers and underscores are allowed)
     */
    public static TextMessage invalidUsernameError() {
        return new TextMessage(CMD_ER02, ER02_BODY);
    }

    /**
     * @return ER03 Please log in first
     */
    public static TextMessage pleaseLoginFirstError() {
        return new TextMessage(CMD_ER03, ER03_BODY);
    }

    /**
     * @return ER04 The user you are trying to reach is not connected.
     */
    public static TextMessage recipientNotConnectedError() {
        return new TextMessage(CMD_ER04, ER04_BODY);
    }

    /**
     * @return ER05 Group name has an invalid format (only characters,
     * numbers and underscores are allowed)
     */
    public static TextMessage invalidGroupNameError() {
        return new TextMessage(CMD_ER05, ER05_BODY);
    }

    /**
     * @return ER06 A group with this name already exists
     */
    public static TextMessage groupAlreadyExistsError() {
        return new TextMessage(CMD_ER06, ER06_BODY);
    }

    /**
     * @return ER07 A group with this name does not exist
     */
    public static TextMessage groupDoesNotExistError() {
        return new TextMessage(CMD_ER07, ER07_BODY);
    }

    /**
     * @return ER08 Missing parameters
     */
    public static TextMessage missingParametersError() {
        return new TextMessage(CMD_ER08, ER08_BODY);
    }

    /**
     * @return ER09 You are already in this group
     */
    public static TextMessage alreadyMemberOfGroupError() {
        return new TextMessage(CMD_ER09, ER09_BODY);
    }

    /**
     * @return ER10 You are not part of this group
     */
    public static TextMessage notMemberOfGroupError() {
        return new TextMessage(CMD_ER10, ER10_BODY);
    }

    /**
     * @return ER11 Password does not match
     */
    public static TextMessage passwordMismatchError() {
        return new TextMessage(CMD_ER11, ER11_BODY);
    }

    /**
     * @return ER12 Negative or zero file size
     */
    public static TextMessage fileSizeError() {
        return new TextMessage(CMD_ER12, ER12_BODY);
    }

    /**
     * @return ER13 Unknown transfer
     */
    public static TextMessage unknownTransfer() {
        return new TextMessage(CMD_ER13, ER13_BODY);
    }
}
