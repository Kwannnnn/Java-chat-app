package nl.saxion.itech.client.threads;

import nl.saxion.itech.client.ChatClient;
import nl.saxion.itech.client.model.protocol.visitors.SendMessageVisitor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class MessageSender extends Thread {
    private Socket socket;
    private PrintWriter writer;
    private ChatClient client;
    private SendMessageVisitor messageVisitor;

    public MessageSender(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            OutputStream outputStream = socket.getOutputStream();
            this.writer = new PrintWriter(outputStream, true);
            this.messageVisitor = new SendMessageVisitor(this.writer);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        do {
            if (this.client.hasPendingMessages()) {
                try {
                    var message = this.client.collectMessage().toString();
                    writer.println(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (hasConnection());

        this.writer.close();
    }

    private boolean hasConnection() {
        return this.socket.isConnected() && !this.socket.isClosed();
    }
}
