package nl.saxion.itech.server.threads;

import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.model.Group;
import nl.saxion.itech.server.model.protocol.*;
import nl.saxion.itech.server.service.ClientService;
import nl.saxion.itech.server.service.GroupService;
import nl.saxion.itech.server.service.MessageService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

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
            while (true) {
                var message = messageService.getNextMessage();
                sendMessageToClient(message);
            }
        } catch (InterruptedException e) {
            // Thread interrupted
        }
    }

    public void handleMessage(String message, Client sender) {
        this.messageHandler.handle(message, sender);
    }

    public void addClient(Client client) {
        this.clientService.addClient(client);
    }

    public void startNewPingThread(Client client) {
        new PingThread(client, this).start();
    }

    public boolean hasClient(String username) {
        return this.clientService.hasClient(username);
    }

    public Client getClientByUsername(String username) {
        return this.clientService.getClientByUsername(username);
    }

    public void removeClient(Client client) {
        this.clientService.removeClient(client);
    }

    public void dispatchMessage(Message message) {
        this.messageService.addMessage(message);
    }

    public Vector<Client> getClients() {
        return this.clientService.getClients();
    }

    public ConcurrentHashMap<String, Group> getGroups() {
        return groupService.getGroups();
    }

    private void sendMessageToClient(Message message) {
        var printWriter = getPrintWriter(message.getSender());
        if (printWriter == null) return; // The client socket has been closed
        printWriter.println(message);
    }

    private PrintWriter getPrintWriter(Client client) {
        var socket = client.getSocket();
        try {
            return new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            removeClient(client);
            return null;
        }
    }

    public boolean hasGroup(String groupName) {
        return this.groupService.hasGroup(groupName);
    }

    public void addGroup(String groupName) {
        this.groupService.addGroup(groupName);
    }

    public boolean groupHasClient(String groupName, Client client) {
        return this.groupService.groupHasClient(groupName, client);
    }

    public void addClientToGroup(String groupName, Client sender) {
        this.groupService.addClientToGroup(groupName, sender);
    }

    public ArrayList<Client> getGroupMembers(String groupName) {
        return this.groupService.getGroupMembers(groupName);
    }
}
