package nl.saxion.itech.server.service;

import nl.saxion.itech.server.model.Client;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientService {
    private final ConcurrentHashMap<String, Client> clients;
    private final ConcurrentHashMap<String, Instant> lastMessageTimestamp;


    public ClientService() {
        this.clients = new ConcurrentHashMap<>();
        this.lastMessageTimestamp = new ConcurrentHashMap<>();
    }

    public void removeClient(String username) {
        lastMessageTimestamp.remove(username);
        clients.remove(username);
    }

    public Collection<Client> getClients() {
        return clients.values();
    }

    public boolean hasClient(String username) {
        return clients.containsKey(username);
    }

    public Client getClientByUsername(String recipientUsername) {
        return clients.get(recipientUsername);
    }

    public void addClient(Client client) {
        clients.put(client.getUsername(), client);
        Instant instant = Instant.now();
        lastMessageTimestamp.put(client.getUsername(), instant);
    }

    public Set<Map.Entry<String, Instant>> getLastMessageTimeStamp() {
        return lastMessageTimestamp.entrySet();
    }

    public void updateTimestampOfClient(String clientUsername) {
        Instant instant = Instant.now();
        lastMessageTimestamp.put(clientUsername, instant);
    }
}
