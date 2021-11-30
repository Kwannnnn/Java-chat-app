package nl.saxion.internettech.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ChatServer {
    private int port;
    private ServerSocket serverSocket;
    private final ArrayList<ClientThread> clients = new ArrayList<>();

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            this.serverSocket = new ServerSocket(port);
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

    public ArrayList<ClientThread> getClients() {
        return clients;
    }

    public void addClient(ClientThread client) {
        clients.add(client);
    }

    public void stats() {
        System.out.printf("Total number of clients: %d\n", clients.size());
//        int connected = clients.filter(c => c.status == STAT_CONNECTED)
        long connected = clients.stream().filter(ClientThread::isConnected).count();
        System.out.printf("Total number of connected clients: %d\n", connected);
    }
}
