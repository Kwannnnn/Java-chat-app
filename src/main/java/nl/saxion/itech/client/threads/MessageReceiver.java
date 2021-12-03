package nl.saxion.itech.client.threads;

import nl.saxion.itech.client.ChatClient;

import java.io.*;
import java.net.Socket;

public class MessageReceiver extends Thread {
    private final Socket socket;
    private final ChatClient client;
    private BufferedReader reader;

    public MessageReceiver(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            InputStream inputStream = this.socket.getInputStream();
            this.reader = new BufferedReader(new InputStreamReader(inputStream));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (socket.isConnected() && !socket.isClosed()) {
                client.handleMessage(reader.readLine());
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            stopConnection();
        }
    }

    public void stopConnection() {
        try {
            reader.close();
            if (!socket.isClosed()) socket.close();
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
