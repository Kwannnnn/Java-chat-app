package nl.saxion.itech.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;

public class ChatServer {
    private final int port;
    private static final Properties props = new Properties();
    private ServerSocket serverSocket;

    public ChatServer() {
        try {
            props.load(ChatServer.class.getResourceAsStream("serverconfig.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.port = Integer.parseInt(props.getProperty("port"));
    }

    public void start() {
        try {
            this.serverSocket = new ServerSocket(this.port);
            System.out.printf("Server is running port %s.\n", this.port);
            var clientHandler = ClientHandler.getInstance();
            while (true) {
                new ClientThread(serverSocket.accept()).start();
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
