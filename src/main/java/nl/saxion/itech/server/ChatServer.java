package nl.saxion.itech.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;

public class ChatServer {
    private static final Properties props = new Properties();
    private static final String CONFIG_FILENAME = "serverconfig.properties";

    private int port;
    private ServerSocket serverSocket;

    public ChatServer() {
        try {
            props.load(ChatServer.class.getResourceAsStream(CONFIG_FILENAME));
            this.port = Integer.parseInt(props.getProperty("port"));
        } catch (NullPointerException | IOException e) {
            System.err.println("Unable to find " + CONFIG_FILENAME + " file.");
            System.exit(1);
        }
    }

    public void start() {
        try {
            this.serverSocket = new ServerSocket(this.port);
            System.out.printf("Server is running port %s.\n", this.port);
            // Initialize the ClientHandler
            ClientHandler.getInstance();
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
