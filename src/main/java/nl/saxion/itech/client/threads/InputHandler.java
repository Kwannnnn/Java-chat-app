package nl.saxion.itech.client.threads;
import nl.saxion.itech.client.ChatClient;
import nl.saxion.itech.client.ProtocolInterpreter;
import static nl.saxion.itech.shared.ProtocolConstants.*;
import nl.saxion.itech.client.newDesign.BaseMessage;
import nl.saxion.itech.client.newDesign.Message;
import static nl.saxion.itech.shared.ANSIColorCodes.*;
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
                e.printStackTrace();
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

        addMessageToQueue(new BaseMessage(CMD_CONN, username));
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

    private void handleFileDenyMessage() {
        System.out.print(">> Please enter the transfer id you want to deny: ");
        String transferID = scanner.nextLine();
        addMessageToQueue(new BaseMessage(CMD_FILE + " " + CMD_ACK , CMD_DENY + " " + transferID));
    }

    private void handleFileAcceptMessage() {
        System.out.print(">> Please enter the transfer id you want to accept: ");
        String transferID = scanner.nextLine();
        addMessageToQueue(new BaseMessage(CMD_FILE + " " + CMD_ACK , CMD_ACCEPT + " " + transferID));
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
            var file = resource.getFile();
            addMessageToQueue(new BaseMessage(CMD_FILE + " " + CMD_SEND, fileName + " " + file.getBytes().length
                    + " " + username));
        }
    }

    private void handleDirectMessage() {
        System.out.print(">> Please enter the recipient's username: ");
        String username = scanner.nextLine();
        System.out.print(">> Please enter the message you want to send: ");
        String message = scanner.nextLine();
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

    private void showMenu() {
        System.out.print(ANSI_MAGENTA +
                """
                         B: \t Broadcast a message to every client on the server
                        DM: \t Send a direct message
                         GN: \t Create a group
                         GA: \t Show all groups
                         GJ: \t Join a group
                         GM: \t Send message to a group
                         FS: \t Send a file to another user
                         FA: \t Accept a file
                         FD: \t Deny a file
                         Q: \t Close connection with the server
                         ?: \t Show this menu
                        """ + ANSI_RESET);
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

