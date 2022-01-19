package nl.saxion.itech.client.threads;

import nl.saxion.itech.client.ChatClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class MessageReceiver extends Thread {
    private final Socket socket;
    private BufferedReader reader;
    private final ChatClient client;

    public MessageReceiver(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            InputStream inputStream = this.socket.getInputStream();
            this.reader = new BufferedReader(new InputStreamReader(inputStream));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                this.client.handleMessage(line);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            stopConnection();
        }
    }

    private void stopConnection() {
        try {
            reader.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
