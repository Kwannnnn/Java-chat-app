package nl.saxion.itech.server.service;

import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.model.Group;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GroupService {
    private final ConcurrentHashMap<String, Group> groups;

    public GroupService() {
        this.groups = new ConcurrentHashMap<>();
    }

    public Group addGroup(String groupName) {
        Group group = new Group(groupName);
        groups.put(groupName, group);
        return group;
    }

    public Collection<Group> getGroups() {
        return groups.values();
    }

    public boolean hasGroup(String groupName) {
        return groups.containsKey(groupName);
    }

    public boolean groupHasClient(String groupName, String clientUsername) {
        Group foundGroup = groups.get(groupName);
        return foundGroup != null && foundGroup.hasClient(clientUsername);
    }

    public void addClientToGroup(String groupName, Client client) {
        this.groups.get(groupName).addClient(client);
    }

    public Collection<Client> getGroupMembers(String groupName) {
        return this.groups.get(groupName).getClients();
    }

    public void removeClientFromGroup(String groupName, String clientUsername) {
        Group foundGroup = groups.get(groupName);
        foundGroup.removeClient(clientUsername);
    }

    public void updateTimestampOfClient(String groupName, String clientUsername) {
        this.groups.get(groupName).updateTimestampOfClient(clientUsername);
    }
}
