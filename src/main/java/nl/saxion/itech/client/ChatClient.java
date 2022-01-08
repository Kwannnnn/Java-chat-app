package nl.saxion.itech.client;

import nl.saxion.itech.client.newDesign.File;
import nl.saxion.itech.client.newDesign.Message;
import nl.saxion.itech.client.newDesign.ServerMessageHandler;
import nl.saxion.itech.client.threads.InputHandler;
import nl.saxion.itech.client.threads.MessageReceiver;
import nl.saxion.itech.client.threads.MessageSender;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatClient {
    private static final Properties props = new Properties();
    private Thread readThread;
    private Thread writeThread;
    private Thread CLIThread;
    private String currentUser;
    private ServerMessageHandler messageHandler;
    private BlockingQueue<Message> messagesQueue = new LinkedBlockingQueue<>();
    private final ConcurrentHashMap<String, File> filesToReceive = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, File> filesToSend = new ConcurrentHashMap<>();

    public ChatClient() {
        try {
            props.load(ChatClient.class.getResourceAsStream("config.properties"));
            Socket socket = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
            this.readThread = new Thread(new MessageSender(socket, this));
            this.writeThread = new Thread(new MessageReceiver(socket, this));
            this.CLIThread = new InputHandler(this);
            this.messageHandler = new ServerMessageHandler(this);
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

    public Message collectMessage() throws InterruptedException {
        return this.messagesQueue.take();
    }

    public void handleMessage(String rawMessage) {
        this.messageHandler.handle(rawMessage);
    }

    public void addMessageToQueue(Message message) {
        try {
            messagesQueue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        this.CLIThread.interrupt();
    }

    public void addFileToReceive(File file) {
        System.out.println("Triggerd addFileToReceive");
        filesToReceive.put(file.getId(), file);
    }

    public void removeFileToReceive(File file) {
        filesToReceive.remove(file.getId(), file);
    }

    public Optional<File> getFileToReceive(String fileID) {
        return Optional.ofNullable(this.filesToReceive.get(fileID));
    }

    public void addFileToSend(File file) {
        System.out.println("Triggerd addFileToReceive");
        filesToSend.put(file.getId(), file);
    }

    public synchronized void removeFileToSend(File file) {
        filesToSend.remove(file.getId(), file);
    }

    public Optional<File> getFileToSend(String fileID) {
        return Optional.ofNullable(this.filesToSend.get(fileID));
    }

    public Collection<File> getFilesToReceive() {
        return filesToReceive.values();
    }

    public Collection<File> getFilesToSend() {
        return filesToSend.values();
    }
}
