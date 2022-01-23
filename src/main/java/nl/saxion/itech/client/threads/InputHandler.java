package nl.saxion.itech.client.threads;

import nl.saxion.itech.client.ChatClient;
import nl.saxion.itech.client.ProtocolInterpreter;

import static nl.saxion.itech.shared.ProtocolConstants.*;

import nl.saxion.itech.client.newDesign.BaseMessage;
import nl.saxion.itech.client.newDesign.FileChecksum;
import nl.saxion.itech.client.newDesign.Message;
import nl.saxion.itech.shared.security.AES;
import nl.saxion.itech.shared.security.util.SecurityUtil;

import static nl.saxion.itech.shared.ANSIColorCodes.*;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class InputHandler extends Thread {

    private final ChatClient client;
    private final Scanner scanner = new Scanner(System.in);
    private boolean isRunning = true;

    public InputHandler(ChatClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        while (this.isRunning) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
//                e.printStackTrace();
                //thread interrupted on purpose, no need to print it out
            }

            if (!isLoggedIn()) {
                handleUsernameInput();
            } else {
                handleMenuInput();
            }
        }
    }

    private boolean isLoggedIn() {
        return this.client.getCurrentUser() != null;
    }

    private void handleUsernameInput() {
        String username = askForUsername();

        while (!isValidUsername(username)) {
            ProtocolInterpreter.showInvalidUsernameMessage();
            username = askForUsername();
        }

        var publicKey = this.client.getPublicKeyAsString();

        addMessageToQueue(new BaseMessage(CMD_CONN, username + " " + "ass"));
    }

    /**
     * Checks whether a username conforms to a certain pattern. Usernames must be between 3 and 14 characters, and can
     * only contain letters, numbers, and underscores.
     *
     * @param username the username to be checked
     * @return true if the username matches the described pattern, otherwise false.
     */
    private boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z0-9_]{3,14}$");
    }

    private String askForUsername() {
        System.out.print(">> Please enter your username to log in: ");
        return scanner.nextLine();
    }

    private void handleMenuInput() {
        String input = scanner.nextLine().toUpperCase();

        switch (input) {
            case "?" -> showMenu();
            case "A" -> handleAuthenticationMessage();
            case "B" -> handleBroadcastMessage();
            case "GN" -> handleGroupNewMessage();
            case "GA" -> handleGroupAllMessage();
            case "GJ" -> handleGroupJoinMessage();
            case "GM" -> handleGroupMessageMessage();
            case "DM" -> handleDirectMessage();
            case "FS" -> handleFileSendMessage();
            case "FA" -> handleFileAcceptMessage();
            case "FD" -> handleFileDenyMessage();
            case "Q" -> handleQuit();
            default -> System.out.println("Unknown command");
        }
    }

    private void handleAuthenticationMessage() {
        System.out.print(">> Please enter your password: ");
        String password = scanner.nextLine();

        this.client.addMessageToQueue(new BaseMessage(CMD_AUTH, password));
    }

    private void showMenu() {
        System.out.print(ANSI_MAGENTA +
                """
                         A: \t Authenticate yourself
                         B: \t Broadcast a message to every client on the server
                        DM: \t Send a direct message
                         GN: \t Create a group
                         GA: \t Show all groups
                         GJ: \t Join a group
                         GM: \t Send message to a group
                         FS: \t Send a fileObject to another user
                         FA: \t Accept a fileObject
                         FD: \t Deny a fileObject
                         Q: \t Close connection with the server
                         ?: \t Show this menu
                        """ + ANSI_RESET);
    }

    private void handleFileDenyMessage() {
        System.out.print(">> Please enter the transfer id you want to deny: ");
        String transferID = scanner.nextLine();
        addMessageToQueue(new BaseMessage(CMD_FILE + " " + CMD_ACK, CMD_DENY + " " + transferID));
    }

    private void handleFileAcceptMessage() {
        System.out.print(">> Please enter the transfer id you want to accept: ");
        String transferID = scanner.nextLine();
        addMessageToQueue(new BaseMessage(CMD_FILE + " " + CMD_ACK, CMD_ACCEPT + " " + transferID));
    }

    private void handleFileSendMessage() {
        System.out.print(">> Please enter the recipient's username: ");
        String username = scanner.nextLine();
        System.out.print(">> Please enter the file name: ");
        String fileName = scanner.nextLine();
        var resource = ChatClient.class.getResource(fileName);

        if (resource == null) {
            System.out.println("File not found");
        } else {
            try {
                File fileToSend = new File(resource.getFile());
                String fileChecksum = FileChecksum.getFileChecksumMD5(fileToSend);

                addMessageToQueue(new BaseMessage(CMD_FILE + " " + CMD_REQ,
                        fileName + " " + fileToSend.length() + " " + fileChecksum + " " + username));
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleDirectMessage() {
        System.out.print(">> Please enter the recipient's username: ");
        String username = scanner.nextLine();
        System.out.print(">> Please enter the message you want to send: ");
        String message = scanner.nextLine();

        if (this.client.getClientEntity(username).isEmpty()) {
            addMessageToQueue(new BaseMessage(CMD_PUBK, username));
        }

        //TODO: timeout
        while (this.client.getClientEntity(username).isEmpty()) {
            synchronized (this.client) {
                try {
                    this.client.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        var clientOptional = this.client.getClientEntity(username);
        assert clientOptional.isPresent() : "Unknown public key, but it should have arrived!";

        var recipient = clientOptional.get();
        var sessionKey = recipient.getSessionKey();

        if (sessionKey == null) {
            var aes = new AES();
            var sessionKeyString = aes.getPrivateKeyAsString();
            sessionKey = aes.getSecretKey();
            recipient.setSessionKey(sessionKey);

            var recipientPublicKey = recipient.getPublicKey();
            var encryptedSessionKey = SecurityUtil.encrypt(sessionKeyString, recipientPublicKey, "RSA");
            addMessageToQueue(new BaseMessage(CMD_SESSION, username + " " + encryptedSessionKey));
        }

        message = SecurityUtil.encrypt(message, sessionKey, "AES");
        addMessageToQueue(new BaseMessage(CMD_MSG, username + " " + message));
    }

    private void handleGroupMessageMessage() {
        System.out.print(">> Please enter the name of the group you want to message: ");
        String groupName = scanner.nextLine();
        System.out.print(">> Please enter the message you want to send: ");
        String message = scanner.nextLine();
        addMessageToQueue(new BaseMessage(CMD_GRP + " " + CMD_MSG, groupName + " " + message));
    }

    private void handleGroupJoinMessage() {
        System.out.print(">> Please enter the name of the group you want to join: ");
        String groupName = scanner.nextLine();
        addMessageToQueue(new BaseMessage(CMD_GRP + " " + CMD_JOIN, groupName));
    }

    private void handleGroupAllMessage() {
        addMessageToQueue(new BaseMessage(CMD_GRP + " " + CMD_ALL, ""));
    }

    private void handleGroupNewMessage() {
        System.out.print(">> Please enter your group name: ");
        String groupName = scanner.nextLine();
        addMessageToQueue(new BaseMessage(CMD_GRP + " " + CMD_NEW, groupName));
    }

    private void handleBroadcastMessage() {
        System.out.print(">> Please enter your message: ");
        String message = scanner.nextLine();
        addMessageToQueue(new BaseMessage(CMD_BCST, message));
    }

    private void handleQuit() {
        addMessageToQueue(new BaseMessage(CMD_DSCN));
    }

    private void addMessageToQueue(Message message) {
        this.client.addMessageToQueue(message);
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }
}

