package nl.saxion.itech.client;

import nl.saxion.itech.client.newDesign.ClientEntity;
import nl.saxion.itech.client.newDesign.FileObject;
import nl.saxion.itech.client.newDesign.Message;
import nl.saxion.itech.client.newDesign.ServerMessageHandler;
import nl.saxion.itech.client.threads.InputHandler;
import nl.saxion.itech.client.threads.MessageReceiver;
import nl.saxion.itech.client.threads.MessageSender;
import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.shared.security.RSA;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatClient {
    private static final Properties props = new Properties();
    private final Thread readThread;
    private final Thread writeThread;
    private final Thread CLIThread;
    private final RSA rsa;

    private final HashMap<String, ClientEntity> connectedClients;

    private String currentUser;

    private ServerMessageHandler messageHandler;
    private BlockingQueue<Message> messagesQueue = new LinkedBlockingQueue<>();
    private final ConcurrentHashMap<String, FileObject> filesToReceive = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, FileObject> filesToSend = new ConcurrentHashMap<>();

    public ChatClient() throws IOException {
        props.load(ChatClient.class.getResourceAsStream("config.properties"));
        Socket socket = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        this.rsa = new RSA();
        this.readThread = new Thread(new MessageSender(socket, this));
        this.writeThread = new Thread(new MessageReceiver(socket, this));
        this.CLIThread = new InputHandler(this);
        this.messageHandler = new ServerMessageHandler(this);
        this.connectedClients = new HashMap<>();
    }

    public void start() {
        this.readThread.start();
        this.writeThread.start();
        this.CLIThread.start();
    }

    public synchronized void addConnectedClient(ClientEntity client) {
        this.connectedClients.put(client.getUsername(), client);
        this.notify();
    }

    public synchronized void removeConnectClient(String username) {
        this.connectedClients.remove(username);
    }

    public synchronized Optional<ClientEntity> getClientEntity(String username) {
        return Optional.ofNullable(this.connectedClients.get(username));
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

    public void addFileToReceive(FileObject fileObject) {
        filesToReceive.put(fileObject.getId(), fileObject);
    }

    public void removeFileToReceive(String fileID) {
        filesToReceive.remove(fileID);
    }

    public Optional<FileObject> getFileToReceive(String fileID) {
        return Optional.ofNullable(this.filesToReceive.get(fileID));
    }

    public void addFileToSend(FileObject fileObject) {
        filesToSend.put(fileObject.getId(), fileObject);
    }

    public synchronized void removeFileToSend(String fileID) {
        filesToSend.remove(fileID);
    }

    public Optional<FileObject> getFileToSend(String fileID) {
        return Optional.ofNullable(this.filesToSend.get(fileID));
    }

    public String getPublicKeyAsString() {
        return rsa.getPublicKeyAsString();
    }

    public PrivateKey getPrivateKey() {
        return rsa.getPrivateKey();
    }

    public Collection<FileObject> getFilesToReceive() {
        return filesToReceive.values();
    }

    public Collection<FileObject> getFilesToSend() {
        return filesToSend.values();
    }
}
