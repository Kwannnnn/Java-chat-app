package nl.saxion.internettech.server;

public class ServerStart {
    private static final int PORT = 1337;

    public static void main(String[] args) {
        ChatServer server = new ChatServer(PORT);
        server.start();
    }
}
