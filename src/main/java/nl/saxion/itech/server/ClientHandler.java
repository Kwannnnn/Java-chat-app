package nl.saxion.itech.server;

import nl.saxion.itech.server.model.Client;

import java.util.Collection;
import java.util.HashMap;

public class ClientHandler {
    private static ClientHandler instance;

    private final HashMap<String, Client> clients;

    private ClientHandler() {
        this.clients = new HashMap<>();
    }

    public static ClientHandler getInstance() {
        if (instance == null) {
            instance = new ClientHandler();
        }
        return instance;
    }

    public void addClient(Client client) {
        this.clients.put(client.getName(), client);
    }

    public void removeClient(Client client) {
        this.clients.remove(client.getName(), client);
    }

    public Client getClientByName(String name) {
        return this.clients.get(name);
    }

    public Collection<Client> getClients() {
        return this.clients.values();
    }
}
