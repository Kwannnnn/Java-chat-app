package nl.saxion.itech.server.threads;

import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.model.protocol.MessageFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
    private Client client;
    private final Socket clientSocket;
    private final MessageDispatcher dispatcher;
    private final MessageFactory messageFactory;
    private PrintWriter out;
    private BufferedReader in;

    public ClientThread(Socket socket, MessageDispatcher dispatcher) {
        this.clientSocket = socket;
        this.dispatcher = dispatcher;
        this.messageFactory = new MessageFactory();
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
        while (!isInterrupted()) {
            var message = readLine();
            if (message == null) break;

            var messageObject = this.messageFactory.getMessage(message);
            messageObject.setClient(this.client);
            this.dispatcher.dispatchMessage(messageObject);
        }
        this.dispatcher.removeClient(this.client);
    }

    private String readLine() {
        try {
            return in.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
