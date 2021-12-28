package nl.saxion.itech.server.threads;

import nl.saxion.itech.shared.ProtocolConstants;
import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.model.Group;
import nl.saxion.itech.server.model.protocol.*;
import nl.saxion.itech.server.service.ClientService;
import nl.saxion.itech.server.service.GroupService;
import nl.saxion.itech.server.service.MessageService;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ServiceManager extends Thread {
    private final ClientMessageHandler messageHandler;
    private final GroupService groupService;
    private final MessageService messageService;
    private final ClientService clientService;

    public ServiceManager() {
        this.messageHandler = new ClientMessageHandler(this);
        this.groupService = new GroupService();
        this.messageService = new MessageService();
        this.clientService = new ClientService();
    }

    @Override
    public void run() {
        try {
            startPingThread();
            while (true) {
                var message = messageService.getNextMessage();
                sendMessageToClient(message);
                displayOutgoingMessage(message);
            }
        } catch (InterruptedException | IOException e) {
            // Thread interrupted
            e.printStackTrace();
        }
    }

    private void displayOutgoingMessage(Message message) {
        String username = message.getSender().getUsername() == null ? "-" : message.getSender().getUsername();
        System.out.printf("<< [%s] %s\n", username, message);
    }

    public void handleMessage(String message, Client sender) {
        this.messageHandler.handle(message, sender);
    }

    public void addClient(Client client) {
        this.clientService.addClient(client);
    }

    public void startPingThread() {
        new PingThread(this).start();
    }

    public boolean hasClient(String username) {
        return this.clientService.hasClient(username);
    }

    public Client getClient(String username) {
        return this.clientService.getClientByUsername(username);
    }

    public void removeClient(String username) {
        this.clientService.removeClient(username);
    }

    public void dispatchMessage(Message message) {
        this.messageService.addMessage(message);
    }

    public void broadcastMessage(Message message) {
        for (var client :
                this.clientService.getClients()) {
            message.setSender(client);
            dispatchMessage(message);
        }
    }

    public String getClients() {
        return this.clientService
                .getClients()
                .stream()
                .map(Client::getUsername)
                .collect(Collectors.joining(","));
    }

    public String getGroups() {
        return this.groupService
                .getGroups()
                .stream()
                .map(Group::getName)
                .collect(Collectors.joining(","));
    }

    private void sendMessageToClient(Message message) throws IOException {
        var printWriter = getPrintWriter(message.getSender());
        printWriter.println(message);
    }

    private PrintWriter getPrintWriter(Client client) throws IOException {
        var socket = client.getSocket();
        return new PrintWriter(socket.getOutputStream(), true);
    }

    public boolean hasGroup(String groupName) {
        return this.groupService.hasGroup(groupName);
    }

    public void addGroup(String groupName) {
        var addedGroup = this.groupService.addGroup(groupName);
        new GroupPingThread(addedGroup, this).start();
    }

    public boolean groupHasClient(String groupName, String clientUsername) {
        return this.groupService.groupHasClient(groupName, clientUsername);
    }

    public void addClientToGroup(String groupName, Client sender) {
        this.groupService.addClientToGroup(groupName, sender);
    }

    public void removeClientFromGroup(String groupName, String clientUsername) {
        this.groupService.removeClientFromGroup(groupName, clientUsername);
    }

    public Collection<Client> getGroupMembers(String groupName) {
        return this.groupService.getGroupMembers(groupName);
    }

    public void sendInfoMessage(Client sender) throws IOException {
        sendMessageToClient(new BaseMessage(
                ProtocolConstants.CMD_INFO,
                ProtocolConstants.INFO_BODY,
                sender
        ));
    }

    public void updateTimestampOfClientInGroup(String groupName, String username) {
        this.groupService.updateTimestampOfClient(groupName, username);
    }

    public Set<Map.Entry<String, Instant>> getTimestampsOfClients() {
        return this.clientService.getLastMessageTimeStamp();
    }

    public void updateTimestampOfClient(String username) {
        this.clientService.updateTimestampOfClient(username);
    }

    public void closeConnection(Client sender) {
        try {
            sender.getSocket().close();
        } catch (IOException e) {
            System.err.println("Client socket has already been closed.");
        }
    }
}
