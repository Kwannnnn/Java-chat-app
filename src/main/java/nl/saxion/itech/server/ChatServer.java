package nl.saxion.itech.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Properties;

public class ChatServer {
    private final int port;
    private static final Properties props = new Properties();
    private ServerSocket serverSocket;
    private static final ArrayList<ClientThread> clients = new ArrayList<>();

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

    public static ArrayList<ClientThread> getClients() {
        return clients;
    }

    public static void addClient(ClientThread client) {
        clients.add(client);
    }

    public static void stats() {
        System.out.printf("Total number of clients: %d\n", clients.size());
//        int connected = clients.filter(c => c.status == STAT_CONNECTED)
        long connected = clients.stream().filter(ClientThread::isConnected).count();
        System.out.printf("Total number of connected clients: %d\n", connected);
    }
}
