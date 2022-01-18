package nl.saxion.itech.server.data;

import nl.saxion.itech.server.model.AuthenticatedUser;
import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.model.FileObject;
import nl.saxion.itech.server.model.Group;
import nl.saxion.itech.server.thread.ClientPingTask;
import nl.saxion.itech.server.thread.GroupPingThread;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.Timer;

import static nl.saxion.itech.shared.ProtocolConstants.PING_INITIAL_DELAY_MS;

public class DataObject {
    private static final HashMap<String, AuthenticatedUser> REGISTERED_USERS = new HashMap<>();
    static {
        REGISTERED_USERS.put("Carlo", new AuthenticatedUser("Carlo", "Password1"));
        REGISTERED_USERS.put("Trish", new AuthenticatedUser("Trish", "Password2"));
        REGISTERED_USERS.put("Lia", new AuthenticatedUser("Lia", "Password2"));
    }

    private final HashMap<String, Client> chatClients;
    private final HashMap<String, ClientPingTask> clientPingThreads;
    private final HashMap<String, Group> groups;
    private final HashMap<String, GroupPingThread> groupPingThreads;
    private final HashMap<String, FileObject> files;

    public DataObject() {
        this.chatClients = new HashMap<>();
        this.clientPingThreads = new HashMap<>();
        this.groups = new HashMap<>();
        this.groupPingThreads = new HashMap<>();
        this.files = new HashMap<>();
    }

    public synchronized void addClient(Client client) {
        this.chatClients.put(client.getUsername(), client);
        var clientPingTask = new ClientPingTask(client);
        this.clientPingThreads.put(client.getUsername(), clientPingTask);
        new Timer().scheduleAtFixedRate(clientPingTask, 0, PING_INITIAL_DELAY_MS);
    }

    public synchronized void removeClient(Client client) {
        if (client.getUsername() != null) {
            this.chatClients.remove(client.getUsername(), client);
            this.clientPingThreads.get(client.getUsername()).cancel();
            this.clientPingThreads.remove(client.getUsername());
        }
    }

    public synchronized Optional<Client> getClient(String username) {
        return Optional.ofNullable(this.chatClients.get(username));
    }

    public synchronized Collection<Client> getAllClients() {
        return this.chatClients.values();
    }

    public synchronized void addGroup(Group group) {
        this.groups.put(group.getName(), group);

        var pingThread = new GroupPingThread(group);
        this.groupPingThreads.put(group.getName(), pingThread);
        pingThread.start();
    }

    public synchronized Collection<Group> getAllGroups() {
        return this.groups.values();
    }

    public synchronized Optional<Group> getGroup(String groupName) {
        return Optional.ofNullable(this.groups.get(groupName));
    }

    public synchronized void removeGroup(String groupName) {
        this.groups.remove(groupName);
        this.groupPingThreads.remove(groupName);
    }

    public synchronized boolean hasGroup(String groupName) {
        return groups.containsKey(groupName);
    }

    public synchronized boolean hasClient(String userName) {
        return chatClients.containsKey(userName);
    }

    public synchronized Optional<FileObject> getFile(String id) {
        return Optional.ofNullable(this.files.get(id));
    }

    public synchronized void addFile(FileObject fileObject) {
        this.files.put(fileObject.getId(), fileObject);
    }

    public synchronized void removeFile(String fileID) { this.files.remove(fileID);}

    public HashMap<String, AuthenticatedUser> getRegisteredUsers() {
        return REGISTERED_USERS;
    }
}
