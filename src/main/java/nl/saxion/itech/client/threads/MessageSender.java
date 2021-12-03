package nl.saxion.itech.client.threads;

import nl.saxion.itech.client.ChatClient;
import nl.saxion.itech.client.model.protocol.Message;
import nl.saxion.itech.client.model.protocol.QuitMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class MessageSender extends Thread {
    private Socket socket;
    private PrintWriter writer;
    private ChatClient client;

    public MessageSender(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            OutputStream outputStream = socket.getOutputStream();
            this.writer = new PrintWriter(outputStream);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        do {
            if (this.client.hasPendingMessages()) {
                try {
                    sendMessageToServer(this.client.collectMessage());
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            }
        } while (hasConnection());

        sendMessageToServer(new QuitMessage());
    }

    private boolean hasConnection() {
        return this.socket.isConnected() && !this.socket.isClosed();
    }

    private void sendMessageToServer(Message message) {
        this.writer.println(message.toString());
        this.writer.flush();
    }


}
