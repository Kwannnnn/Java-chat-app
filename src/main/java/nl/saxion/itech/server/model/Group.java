package nl.saxion.itech.server.model;

import java.time.Instant;
import java.util.*;

public class Group {
    private final String name;
    private final HashMap<String, Client> clients;
    private final HashMap<String, Instant> lastMessageTimeStamp;

    public Group(String name) {
        this.name = name;
        this.clients = new HashMap<>();
        lastMessageTimeStamp = new HashMap<>();
    }

    public String getName() {
        return this.name;
    }

    public Client getClient(String username) {
        return this.clients.get(username);
    }

    public Collection<Client> getClients() {
        return this.clients.values();
    }

    public void addClient(Client client) {
        this.clients.put(client.getUsername(), client);
        Instant instant = Instant.now();
        lastMessageTimeStamp.put(client.getUsername(), instant);
    }

    public void removeClient(String username) {
        this.clients.remove(username);
        lastMessageTimeStamp.remove(username);
    }

    public boolean hasClient(String username) {return this.clients.containsKey(username);}

    public void updateTimestampOfClient(String clientUsername) {
        Instant instant = Instant.now();
        lastMessageTimeStamp.put(clientUsername, instant);
    }

    public Set<Map.Entry<String, Instant>> getLastMessageTimeStamp() {
        return lastMessageTimeStamp.entrySet();
    }
}