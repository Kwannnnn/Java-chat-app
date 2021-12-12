package nl.saxion.itech.client;

import nl.saxion.itech.client.model.protocol.MessageFactory;
import nl.saxion.itech.client.model.protocol.messages.sendable.SendableMessage;
import nl.saxion.itech.client.model.protocol.visitors.ReadMessageVisitor;
import nl.saxion.itech.client.threads.InputHandler;
import nl.saxion.itech.client.threads.MessageReceiver;
import nl.saxion.itech.client.threads.MessageSender;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatClient {
    private static final Properties props = new Properties();
    private Thread readThread;
    private Thread writeThread;
    private Thread CLIThread;
    private String currentUser;
    private MessageFactory messageFactory;
    private BlockingQueue<SendableMessage> messagesQueue = new LinkedBlockingQueue<>();

    public ChatClient() {
        try {
            props.load(ChatClient.class.getResourceAsStream("clientconfig.properties"));
            Socket socket = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
            this.readThread = new Thread(new MessageSender(socket, this));
            this.writeThread = new Thread(new MessageReceiver(socket, this));
            this.CLIThread = new InputHandler(this);
            this.messageFactory = new MessageFactory();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void start() {
        this.readThread.start();
        this.writeThread.start();
        this.CLIThread.start();
    }

    public synchronized void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public synchronized String getCurrentUser() {
        return this.currentUser;
    }

    public boolean hasPendingMessages() {
        return this.messagesQueue.isEmpty();
    }

    public SendableMessage collectMessage() throws InterruptedException {
        return this.messagesQueue.take();
    }

    public void handleMessage(String rawMessage) {
        var message = new MessageFactory().getMessage(rawMessage);
        message.accept(new ReadMessageVisitor(this));
    }

    public void addMessageToQueue(SendableMessage message) {
        try {
            messagesQueue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private String[] parseMessage(String response) {
        return response.split(" ", 2);
    }
}
