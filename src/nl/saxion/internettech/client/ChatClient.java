package nl.saxion.internettech.client;

import java.io.IOException;
import java.net.Socket;

public class ChatClient {
    private final Thread readThread;
    private final Thread writeThread;
    public static boolean isLoggedIn = false;

    public ChatClient(String serverAddress, int port) {
        try {
            Socket socket = new Socket(serverAddress, port);
            this.readThread = new Thread(new MessageWriter(socket));
            this.writeThread = new Thread(new MessageReader(socket));
        } catch (IOException e) {
            throw new Error("I/O Error: " + e.getMessage());
        }
    }

    public void start() {
        this.readThread.start();
        this.writeThread.start();
    }
}
