package nl.saxion.itech.server;

import nl.saxion.itech.server.threads.ClientThread;
import nl.saxion.itech.server.threads.ServiceManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Properties;

public class ChatServer {
    private static final Properties props = new Properties();
    private static final String CONFIG_FILENAME = "config.properties";

    private int port;
    private ServerSocket serverSocket;

    /**
     * After instantiation call the start() method to start the server.
     */
    public ChatServer() {
    }

    public void start() {
        loadConfigFile();
        createServerSocket();
        handleConnections();
    }

    private void loadConfigFile() {
        try {
            props.load(ChatServer.class.getResourceAsStream(CONFIG_FILENAME));
            this.port = Integer.parseInt(props.getProperty("port"));
        } catch(NullPointerException e) {
            System.err.println("Unable to find " + CONFIG_FILENAME + " file.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("An error occurred while reading the config file.");
            System.exit(1);
        }
    }

    private void createServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.port);
            System.out.printf("Server is running port %s.\n", this.port);
        } catch (IOException e) {
            System.err.println("Unable to bind to port " + this.port);
            System.exit(1);
        }
    }

    private void handleConnections() {
        var manager = new ServiceManager();
        manager.start();

        while (!this.serverSocket.isClosed()) {
            try {
                var clientSocket = this.serverSocket.accept();
                new ClientThread(clientSocket, manager).start();
            } catch (SocketException e) {
                System.err.println(e.getMessage());
            } catch (IOException e) {
                System.err.println("An I/O error occurred when opening the server socket: " + e.getMessage());
            }
        }
    }
}
