package nl.saxion.itech.server.service;

import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.model.Group;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class GroupService {
    private final ConcurrentHashMap<String, Group> groups;

    public GroupService() {
        this.groups = new ConcurrentHashMap<>();
    }
    
    public void addGroup(Group group) {
        groups.put(group.getName(), group);
    }

    public void addGroup(String groupName) {
        groups.put(groupName, new Group(groupName));
    }

    public ConcurrentHashMap<String, Group> getGroups() {
        return groups;
    }

    public boolean hasGroup(String groupName) {
        return groups.containsKey(groupName);
    }

    public boolean groupHasClient(String groupName, Client client) {
        Group foundGroup = groups.get(groupName);
        if (foundGroup != null) {
            return foundGroup.hasClient(client);
        }
        return false;
    }

    public void addClientToGroup(String groupName, Client sender) {
        this.groups.get(groupName).addClient(sender);
    }

    public ArrayList<Client> getGroupMembers(String groupName) {
        return this.groups.get(groupName).getClients();
    }
}
