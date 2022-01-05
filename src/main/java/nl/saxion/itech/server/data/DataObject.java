package nl.saxion.itech.server.data;

import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.model.File;
import nl.saxion.itech.server.model.Group;
import nl.saxion.itech.server.thread.GroupPingThread;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public class DataObject {
    private final HashMap<String, Client> chatClients;
    private final HashMap<String, Group> groups;
    private final HashMap<String, GroupPingThread> groupPingThreads;
    private final HashMap<Long, File> files;

    public DataObject() {
        this.chatClients = new HashMap<>();
        this.groups = new HashMap<>();
        this.groupPingThreads = new HashMap<>();
        this.files = new HashMap<>();
        addFile(new File("test.txt", 234, "asfasf"));
    }

    public synchronized void addClient(Client client) {
        this.chatClients.put(client.getUsername(), client);
    }

    public synchronized void removeClient(Client client) {
        this.chatClients.remove(client.getUsername(), client);
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

    public synchronized boolean hasGroup(String groupName) {
        return groups.containsKey(groupName);
    }

    public synchronized boolean hasClient(String userName) {
        return chatClients.containsKey(userName);
    }

    public synchronized Optional<File> getFile(long id) {
        return Optional.ofNullable(this.files.get(id));
    }

    public synchronized void addFile(File file) {
        this.files.put(file.getId(), file);
    }
}