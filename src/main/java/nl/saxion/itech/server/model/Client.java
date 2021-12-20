package nl.saxion.itech.server.model;

import java.net.Socket;

public class Client {
    private String username;
    private Socket socket;

    public Client(Socket socket) {
        this.socket = socket;
    }

    public String getUsername() {
        return this.username;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
