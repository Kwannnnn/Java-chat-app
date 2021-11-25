package server;

import nl.saxion.internettech.client.ChatClient;

public class Main {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 1337;

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start(PORT);
    }
}
