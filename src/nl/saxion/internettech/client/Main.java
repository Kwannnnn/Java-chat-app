package nl.saxion.internettech.client;

public class Main {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 1337;

    public static void main(String[] args) {
        ChatClient client = new ChatClient(SERVER_ADDRESS, PORT);
        client.start();
    }
}
