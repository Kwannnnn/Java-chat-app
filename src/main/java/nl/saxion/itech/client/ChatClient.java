package nl.saxion.itech.client;

import nl.saxion.itech.client.model.ClientEntity;
import nl.saxion.itech.client.model.FileObject;
import nl.saxion.itech.client.model.ServerMessageHandler;
import nl.saxion.itech.client.model.message.BaseMessage;
import nl.saxion.itech.client.threads.InputHandler;
import nl.saxion.itech.client.threads.MessageReceiver;
import nl.saxion.itech.client.threads.MessageSender;
import nl.saxion.itech.shared.security.RSA;

import java.io.IOException;
import java.net.Socket;
import java.security.PrivateKey;
import java.util.*;

public class ChatClient {
    private static final Properties props = new Properties();
    private final Thread readThread;
    private final Thread writeThread;
    private final Thread CLIThread;
    private final RSA rsa;

    private String currentUser;

    private final ServerMessageHandler messageHandler;
    private final Queue<BaseMessage> messagesQueue = new LinkedList<>();
    private final HashMap<String, ClientEntity> connectedClients = new HashMap<>();
    private final HashMap<String, FileObject> filesToReceive = new HashMap<>();
    private final HashMap<String, FileObject> filesToSend = new HashMap<>();

    public ChatClient() throws IOException {
        props.load(ChatClient.class.getResourceAsStream("config.properties"));
        Socket socket = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        this.rsa = new RSA();
        this.readThread = new MessageSender(socket, this);
        this.writeThread = new MessageReceiver(socket, this);
        this.CLIThread = new InputHandler(this);
        this.messageHandler = new ServerMessageHandler(this);
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

    public synchronized void removeConnectedClient(String username) {
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

    public synchronized BaseMessage collectMessage() throws InterruptedException {
        while (this.messagesQueue.isEmpty()) {
            wait();
        }
        return this.messagesQueue.remove();
    }

    public synchronized void addMessageToQueue(BaseMessage message) {
        this.messagesQueue.add(message);
        notify();
    }

    public synchronized void addFileToReceive(FileObject fileObject) {
        filesToReceive.put(fileObject.getId(), fileObject);
    }

    public synchronized void removeFileToReceive(String fileID) {
        filesToReceive.remove(fileID);
    }

    public synchronized Optional<FileObject> getFileToReceive(String fileID) {
        return Optional.ofNullable(this.filesToReceive.get(fileID));
    }

    public synchronized void addFileToSend(FileObject fileObject) {
        filesToSend.put(fileObject.getId(), fileObject);
    }

    public synchronized void removeFileToSend(String fileID) {
        filesToSend.remove(fileID);
    }

    public synchronized Optional<FileObject> getFileToSend(String fileID) {
        return Optional.ofNullable(this.filesToSend.get(fileID));
    }

    public void handleMessage(String rawMessage) {
        this.messageHandler.handle(rawMessage);
    }

    public String getPublicKeyAsString() {
        return rsa.getPublicKeyAsString();
    }

    public PrivateKey getPrivateKey() {
        return rsa.getPrivateKey();
    }

    public void closeConnection() {
        this.CLIThread.interrupt();
    }
}
