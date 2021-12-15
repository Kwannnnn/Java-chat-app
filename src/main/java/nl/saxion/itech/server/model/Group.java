package nl.saxion.itech.server.model;

import java.util.List;

public class Group {
    private String name;
    private List<Client> users;

    public Group(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public List<Client> getUsers() {
        return this.users;
    }

    public void addClient(Client client) {
        this.users.add(client);
    }

    public void removeClient(Client client) {
        this.users.remove(client);
    }
}