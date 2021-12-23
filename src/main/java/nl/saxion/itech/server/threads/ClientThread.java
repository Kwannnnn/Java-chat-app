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
        try {
            instantiateStreams();
            sendInfoMessage();
            handleMessages();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendInfoMessage() throws IOException {
        this.manager.sendInfoMessage(this.client);
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
            while (!isInterrupted()) {
                String rawMessage = in.readLine();

                if (rawMessage == null) break;

                displayIncomingMessage(rawMessage);
                this.manager.handleMessage(rawMessage, this.client);
            }
        } catch (IOException e) {
            if (this.client.getUsername() != null) {
                this.manager.removeClient(this.client.getUsername());
            }
            Thread.currentThread().interrupt();
        }
    }

    private void displayIncomingMessage(String message) {
        String username = this.client.getUsername() == null ? "-" : this.client.getUsername();
        System.out.printf(">> [%s] %s\n", username, message);
    }
}
