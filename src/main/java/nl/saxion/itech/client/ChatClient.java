package nl.saxion.itech.client;

import nl.saxion.itech.client.model.protocol.InfoMessage;
import nl.saxion.itech.client.model.protocol.Message;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

public class ChatClient {
    private static final Properties props = new Properties();
    private Thread readThread;
    private Thread writeThread;
    private Thread handlerThread;
    private String currentUser;

    public ChatClient() {
        try {
            props.load(ChatClient.class.getResourceAsStream("clientconfig.properties"));
            Socket socket = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
            this.readThread = new Thread(new MessageSender(socket, this));
            this.writeThread = new Thread(new MessageReceiver(socket, this));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void start() {
        this.readThread.start();
        this.writeThread.start();
    }

    public synchronized void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public synchronized String getCurrentUser() {
        return this.currentUser;
    }

    public Message collectMessage() {
        return new InfoMessage();
    }
}
