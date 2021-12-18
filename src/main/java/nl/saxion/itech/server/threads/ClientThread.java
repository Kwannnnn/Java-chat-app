package nl.saxion.itech.server.threads;

import nl.saxion.itech.server.model.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
    private final Client client;
    private final Socket clientSocket;
    private final ServiceManager manager;
    private PrintWriter out;
    private BufferedReader in;

    public ClientThread(Socket socket, ServiceManager manager) {
        this.clientSocket = socket;
        this.manager = manager;
        this.client = new Client(socket);
    }

    @Override
    public void run() {
        instantiateStreams();
        handleMessages();
    }

    private void instantiateStreams() {
        try {
            this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Error while instantiating the input and output streams");
            e.printStackTrace();
        }
    }

    private void handleMessages() {
        try {
            while (!isInterrupted() || this.clientSocket.isClosed()) {
                String rawMessage = in.readLine();

                if (rawMessage == null) break;

                this.manager.handleMessage(rawMessage, this.client);
            }
        } catch (IOException e) {
            if (this.client.getUsername() != null) {
                this.manager.removeClient(this.client);
            }
            Thread.currentThread().interrupt();
        }
    }
}
