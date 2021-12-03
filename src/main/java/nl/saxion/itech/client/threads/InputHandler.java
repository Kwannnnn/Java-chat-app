package nl.saxion.itech.client.threads;

import nl.saxion.itech.client.ChatClient;
import nl.saxion.itech.client.model.protocol.BroadcastMessage;
import nl.saxion.itech.client.model.protocol.ConnectMessage;
import nl.saxion.itech.client.model.protocol.Message;

import java.util.Scanner;

public class InputHandler extends Thread {

    private static final String ANSI_MAGENTA = "\u001B[95m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";

    private final ChatClient client;
    private final Scanner scanner = new Scanner(System.in);
    private boolean isRunning = true;

    public InputHandler(ChatClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        while (this.isRunning) {
            if (isLoggedIn())
                handleMenuInput();
            else
                handleUsernameInput();
        }
    }

    private boolean isLoggedIn() {
        return this.client.getCurrentUser() != null;
    }

    private void handleUsernameInput() {
        String username = askForUsername();
        while(!isValidUsername(username)) {
            System.out.println(ANSI_RED + "Invalid username! Usernames must be between 3 and 14 characters, and can " +
                    "only contain letters, numbers, and underscores" + ANSI_RESET);
            username = askForUsername();
        }
        addMessageToQueue(new ConnectMessage(username));
    }

    private String askForUsername() {
        System.out.print(">> Please enter your username to log in: ");
        return scanner.nextLine();
    }

    /**
     * Checks whether a username conforms to a certain pattern. Usernames must be between 3 and 14 characters, and can
     * only contain letters, numbers, and underscores.
     * @param username the username to be checked
     * @return true if the username matches the described patter, otherwise false.
     */
    private boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z0-9_]{3,14}$");
    }

    private void handleMenuInput() {
        String input = scanner.nextLine().toUpperCase();

        switch (input) {
            case "?" -> showMenu();
            case "B" -> handleBroadcastMessage();
            case "G" -> System.out.println("Create a group");
            case "DM" -> System.out.println("Send a direct message");
            case "Q" -> this.isRunning = false;
            default -> System.out.println("Unknown command");
        }
    }

    private void showMenu() {
        System.out.print(ANSI_MAGENTA +
                """
                 B: \t Broadcast a message to every client on the server
                DM: \t Send a direct message
                 G: \t Create a group
                 Q: \t Close connection with the server
                 ?: \t Show this menu
                """ + ANSI_RESET);
    }

    private void handleBroadcastMessage() {
        System.out.print(">> Please enter your message: ");
        String message = scanner.nextLine();
        addMessageToQueue(new BroadcastMessage(message));
    }

    private void addMessageToQueue(Message message) {
        try {
            this.client.addMessageToQueue(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
