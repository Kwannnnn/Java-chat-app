package nl.saxion.itech.server.model;

import java.util.ArrayList;

public class Group {
    private final String name;
    private final ArrayList<Client> clients;

    public Group(String name) {
        this.name = name;
        this.clients = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<Client> getClients() {
        return this.clients;
    }

    public void addClient(Client client) {
        this.clients.add(client);
    }

    public void removeClient(Client client) {
        this.clients.remove(client);
    }

    public boolean hasClient(Client client) {return this.clients.contains(client);}
}