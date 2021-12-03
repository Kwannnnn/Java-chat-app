package nl.saxion.itech.client;

import nl.saxion.itech.client.model.ReadMessageVisitor;
import nl.saxion.itech.client.model.SendMessageVisitor;
import nl.saxion.itech.client.model.protocol.InfoMessage;
import nl.saxion.itech.client.model.protocol.Message;
import nl.saxion.itech.client.model.protocol.OkMessage;
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
    private final ReadMessageVisitor rmv = new ReadMessageVisitor();
    private final SendMessageVisitor smv = new SendMessageVisitor();
    private Thread readThread;
    private Thread writeThread;
    private Thread inputThread;

    private String currentUser;
    private BlockingQueue<Message> messagesQueue = new LinkedBlockingQueue<>();

    public ChatClient() {
        try {
            props.load(ChatClient.class.getResourceAsStream("clientconfig.properties"));
            Socket socket = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
            this.readThread = new MessageSender(socket, this);
            this.writeThread = new MessageReceiver(socket, this);
            this.inputThread = new InputHandler(this);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void start() {
        this.inputThread.start();
        this.readThread.start();
        this.writeThread.start();
    }

    private synchronized void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public synchronized String getCurrentUser() {
        return this.currentUser;
    }

    public void handleMessage(String rawMessage) {
        var parsedMessage = parseMessage(rawMessage);
        var header = parsedMessage[0];
        var body = parsedMessage[1];

        switch (parsedMessage[0]) {
            case "INFO" -> System.out.println(rmv.visit(new InfoMessage(body)));
            case "OK" -> {
                switch (parsedMessage[1]) {
                    case "BCST" -> System.out.println("Broadcast a message");
                    default -> {
                        setCurrentUser(parsedMessage[1]);
                        System.out.println(rmv.visit(new OkMessage(body)));
                    }
                }
            }
        }
    }

    public boolean hasPendingMessages() {
        return this.messagesQueue.isEmpty();
    }

    public Message collectMessage() throws InterruptedException {
       return messagesQueue.take();
    }

    public void addMessageToQueue(Message message) throws InterruptedException {
        messagesQueue.put(message);
    }

    private String[] parseMessage(String response) {
        return response.split(" ", 2);
    }
}
