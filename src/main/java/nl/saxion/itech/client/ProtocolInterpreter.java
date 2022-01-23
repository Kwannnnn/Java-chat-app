package nl.saxion.itech.client;

import static nl.saxion.itech.shared.ANSIColorCodes.*;

public final class ProtocolInterpreter {
    public static final int TERMINAL_SIZE = 80;

    public static void showMenu() {
        System.out.print(ANSI_MAGENTA +
                """
                        B: \t Broadcast a message to every client on the server
                        Q: \t Close connection with the server
                        ?: \t Show this menu
                        """ + ANSI_RESET);
    }

    public static void showWelcomeMessage(String message) {
        System.out.println();
        message = " ".repeat(TERMINAL_SIZE) + "\n" + centerText(message) + "\n" + " ".repeat(TERMINAL_SIZE);
        System.out.println(ANSI_GREEN + message + ANSI_RESET);
        System.out.println();
    }

    public static void askUsernameMessage() {
        System.out.print(bold(">> Please enter your username to log in: "));
    }

    public static void showInvalidUsernameMessage() {
        System.out.println(ANSI_RED + "Invalid username! Usernames must be between 3 and 14 characters, and can " +
                "only contain letters, numbers, and underscores" + ANSI_RESET);
    }

    public static void promptMenuMessage() {
        System.out.println(ANSI_YELLOW + "Type '?' to show menu." + ANSI_RESET);
    }

    public static void showErrorMessage(String message) {
        System.out.println(ANSI_RED + message + ANSI_RESET);
    }

    public static void showSuccessfulLoginMessage() {
        System.out.println(italic("You have been successfully logged in!"));
    }

    public static void showBroadcastMessage(String sender, String message) {
        System.out.println("New broadcast message by " + bold(sender) + ": " + message);

    }

    public static void showSuccessfulBroadcastMessage(String message) {
        System.out.println(italic("Successfully broadcast message: ") + message);
    }

    public static void showSuccessfulAllMessage(String[] clients) {
        System.out.println("====List of clients====");
        for (String client : clients) {
            System.out.println(client);
        }
        System.out.println("======================");
    }

    public static void showSuccessfulGroupNewMessage(String groupName) {
        System.out.println(italic("New group successfully created: ") + groupName);
    }

    public static void showSuccessfulGroupJoinMessage(String groupName) {
        System.out.println(italic("Successfully joined group: ") + groupName);
    }

    public static void showSuccessfulGroupAllMessage(String[] groups) {
        System.out.println("====List of groups====");
        for (String group : groups) {
            System.out.println(group);
        }
        System.out.println("======================");
    }

    public static void showSuccessfulGroupDisconnectMessage(String groupName) {
        System.out.println(italic("Successfully disconnected from group: ") + groupName);
    }

    public static void showSuccessfulGroupMessageMessage(String groupName, String message) {
        System.out.println(italic("Successfully sent to group " + bold(groupName) + " message: ") + bold(message));
    }

    public static void enterMessageMessage() {
        System.out.print(bold(">> Please enter your message: "));
    }

    public static void showIncomingDirectMessage(String sender, String message) {
        System.out.println("New direct message from " + bold(sender) + ": " + message);
    }

    public static void connectionLost() {
        System.out.println(ANSI_RED + "\nAn error occurred! You have been disconnected from the server!" + ANSI_RESET);
    }

    public static void showSuccessfulDirectMessage(String recipient, String message) {
        System.out.println(italic("Successfully sent to user " + bold(recipient) + " message: " + message));
    }

    public static void showSuccessfulDisconnectMessage() {
        System.out.println(italic("You have been successfully disconnected from the server"));
    }

    public static String centerText(String text) {
        String result = "";

        String emptySpaces = " ".repeat((TERMINAL_SIZE - text.length()) / 2);

        result = emptySpaces + text + emptySpaces;
        return result;
    }

    public static void showGroupJoinMessage(String groupName, String newMember) {
        System.out.println(italic("User " + bold(newMember) + " just joined group " + bold(groupName)));
    }

    public static void showGroupMessageMessage(String groupName, String message) {
        System.out.println(bold("New message in group " + bold(groupName) + ": " + message));
    }

    public static String italic(String string) {
        return ANSI_ITALIC + string + ANSI_RESET;
    }

    public static String bold(String string) {
        return ANSI_BOLD + string + ANSI_RESET;
    }

    public static void showFileRequestMessage(String transferID, String sender, String fileName, String fileSize) {
        System.out.print(italic("New file transfer request from [" + bold(sender) + "]:\n")
                + "Transfer ID: " + bold(transferID) + "\n"
                + "File name: " + bold(fileName) + "\n"
                + "File size: " + bold(fileSize) + "\n");
    }

    public static void showFileTransferMessage(String transferID, String portNumber) {
        System.out.println(italic("A connection has been setup on port " + bold(portNumber) + " for transfer with" +
                " id: ") + bold(transferID));
    }

    public static void showSuccessfulFileSendMessage(String fileName, String fileSize, String recipient) {
        System.out.println("Your request to send the file " + bold(fileName) + " to " + bold(recipient) + " has been sent");
        System.out.println("File size: " + bold(fileSize));
    }

    public static void showSuccessfulAcknowledgeAcceptMessage(String fileId) {
        System.out.println("You successfully accepted the file with id " + bold(fileId) + ". Download will" +
                " start shortly.");
    }

    public static void showSuccessfulAcknowledgeDenyMessage(String fileId) {
        System.out.println("You successfully denied the file with id " + bold(fileId));
    }

    public static void showFileAckAcceptMessage(String fileId) {
        System.out.println("Your transfer request has been accepted");
        System.out.println("File id: " + bold(fileId));
    }

    public static void showFileAckDenyMessage(String fileId) {
        System.out.println("Your transfer request has been denied");
        System.out.println("File id: " + bold(fileId));
    }

    public static void showFileNotFound(String fileId) {
        System.out.println("File cannot be found");
        System.out.println("File id: " + bold(fileId));
    }

    public static void showFileTransferProcessClosed() {
        System.out.println(italic("The file transfer process has been shut down"));
    }

    public static void showFileDownloadSuccess(String fileID) {
        System.out.println(italic("File successfully downloaded"));
        System.out.println(italic("File id: " + fileID));
    }

    public static void showFileDownloadFailure(String fileID) {
        System.out.println(italic("Something went wrong when downloading the file. Checksum does not match."));
        System.out.println(italic("File id: " + fileID));
    }

    public static void showFinishedFileUpload(String fileID) {
        System.out.println(italic("Finished uploading file. Waiting for response from receiver"));
        System.out.println(italic("File id: " + fileID));
    }

    public static void showFileTransferSuccessMessage(String fileID) {
        System.out.println(italic("File transfer was successfully executed"));
        System.out.println(italic("File id: " + fileID));
    }

    public static void showFileTransferFailMessage(String fileID) {
        System.out.println(italic("File transfer was not successful"));
        System.out.println(italic("File id: " + fileID));
    }

    public static void showEncryptionSessionRequestMessage(String senderUsername) {
        System.out.println(italic("New session key request from " + bold(senderUsername)));
    }

    public static void showSuccessfulEncryptionSessionRequestMessage(String recipientUsername) {
        System.out.println(italic("Request for session key successfully sent to " + bold(recipientUsername)));
    }

    public static void showEncryptionSessionSendMessage(String senderUsername) {
        System.out.println(italic("Session key received from " + bold(senderUsername))
                + ". Connection successfully established.");
    }

    public static void showSuccessfulEncryptionSessionSendMessage(String recipientUsername) {
        System.out.println(italic("Session key sent to " + bold(recipientUsername))
                + ". Connection successfully established.");
    }

    public static void showSuccessfulAuthenticationMessage() {
        System.out.println(italic("You have been successfully authenticated"));
    }

    public static void showUserDisconnected(String username) {
        System.out.println(italic("User " + username + " has disconnected from the server"));
    }

    public static void showMissingSecureConnection(String senderUsername) {
        System.out.println(italic(ANSI_RED + "You haven't established a secure connection with " + senderUsername) + ANSI_RESET);
    }

    public static void showMissingSessionKey(String senderUsername) {
        System.out.println(italic(ANSI_RED + "Session key with " + bold(senderUsername) + " missing") + ANSI_RESET);
    }

    public static void showUnknownResponseFromServer() {
        System.out.println("Unknown response from server");
    }
}
