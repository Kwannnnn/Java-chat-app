package nl.saxion.itech.client;

import java.io.IOException;

public class ChatApp {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 1337;

    public static void main(String[] args) {
        try {
            ChatClient client = new ChatClient(SERVER_ADDRESS, PORT);
            client.start();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
