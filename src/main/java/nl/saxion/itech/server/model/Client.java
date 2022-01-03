package nl.saxion.itech.server.model;

import java.net.Socket;
import java.time.Instant;

public class Client {
    private String username;
    private String password;
    private final Socket socket;
    private Instant lastPong;
    private ClientStatus status;

    public Client(Socket socket) {
        this.socket = socket;
        this.status = ClientStatus.CLIENT_NEW;
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

    public void setStatus(ClientStatus status) {
        this.status = status;
    }

    public Instant getLastPong() {
        return lastPong;
    }

    public ClientStatus getStatus() {
        return this.status;
    }

    public void updateLastPong() {
        this.lastPong = Instant.now();
    }

    @Override
    public String toString() {
        return this.username == null
                ? "-"
                : this.username;
    }
}
