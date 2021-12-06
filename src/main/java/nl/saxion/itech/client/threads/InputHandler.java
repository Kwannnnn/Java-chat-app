package nl.saxion.itech.client.threads;

import nl.saxion.itech.client.ChatClient;
import nl.saxion.itech.client.model.InvalidUsernameException;
import nl.saxion.itech.client.model.protocol.messages.sendable.BroadcastMessage;
import nl.saxion.itech.client.model.protocol.messages.sendable.ConnectMessage;
import nl.saxion.itech.client.model.protocol.messages.sendable.QuitMessage;
import nl.saxion.itech.client.model.protocol.messages.sendable.SendableMessage;

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

        try {
            addMessageToQueue(new ConnectMessage(username));
        } catch (InvalidUsernameException e) {
            System.out.println(ANSI_RED + "Invalid username! Usernames must be between 3 and 14 characters, and can " +
                    "only contain letters, numbers, and underscores" + ANSI_RESET);
            handleUsernameInput();
        }
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
            case "G" -> System.out.println("Create a group");
            case "DM" -> System.out.println("Send a direct message");
            case "Q" -> handleQuit();
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

    private void handleQuit() {
        this.isRunning = false;
        addMessageToQueue(new QuitMessage());
    }

    private void addMessageToQueue(SendableMessage message) {
        this.client.addMessageToQueue(message);
    }
}

