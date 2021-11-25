package nl.saxion.internettech.client;

import java.io.IOException;
import java.net.Socket;

public class ChatClient {
    private final Thread readThread;
    private final Thread writeThread;
    private String currentUser;

    public ChatClient(String serverAddress, int port) throws IOException {
        try {
            Socket socket = new Socket(serverAddress, port);
            this.readThread = new Thread(new MessageSender(socket, this));
            this.writeThread = new Thread(new MessageReceiver(socket, this));
        } catch (IOException e) {
            throw e;
        }
    }

    public void start() {
        this.readThread.start();
        this.writeThread.start();
    }

    public synchronized void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public synchronized String getCurrentUser() {
        return this.currentUser;
    }
}
