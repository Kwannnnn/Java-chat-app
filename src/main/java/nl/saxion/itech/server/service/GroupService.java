package nl.saxion.itech.server.service;

import nl.saxion.itech.server.model.Group;

import java.util.concurrent.ConcurrentHashMap;

public class GroupService {
    private final ConcurrentHashMap<String, Group> groups;

    public GroupService() {
        this.groups = new ConcurrentHashMap<>();
    }
    
    public void addGroup(Group group) {
        groups.put(group.getName(), group);
    }
}
