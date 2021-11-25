package nl.saxion.internettech.server;

import java.io.IOException;
import java.net.ServerSocket;

public class ChatServer {
    private int port;
    private ServerSocket serverSocket;

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            this.serverSocket = new ServerSocket(port);
            System.out.printf("Server is running port %s.", this.port);
            while (true) {
                new UserThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
