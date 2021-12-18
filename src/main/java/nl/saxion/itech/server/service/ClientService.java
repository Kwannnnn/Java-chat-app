package nl.saxion.itech.server.service;

import nl.saxion.itech.server.model.Client;

import java.util.Vector;

public class ClientService {
    private final Vector<Client> clients;

    public ClientService() {
        this.clients = new Vector<>();
    }

    public synchronized void removeClient(Client client) {
        clients.remove(client);
    }

    public Vector<Client> getClients() {
        return clients;
    }

    public boolean hasClient(String username) {
        return clients.stream().anyMatch(c -> c.getUsername().equals(username));
    }

    public Client getClientByUsername(String recipientUsername) {
        return clients.stream().filter(c -> c.getUsername().equals(recipientUsername)).findAny().orElse(null);
    }

    public void addClient(Client client) {
        clients.add(client);
    }
}
