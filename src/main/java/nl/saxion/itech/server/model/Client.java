package nl.saxion.itech.server.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;

public class Client {
    private String username;
    private String password;
    private Instant lastPong;
    private ClientStatus status;
    private InputStream in;
    private OutputStream out;

    public Client(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
        this.status = ClientStatus.CLIENT_NEW;
    }

    public String getUsername() {
        return this.username;
    }

    public InputStream getInputStream() {
        return this.in;
    }

    public OutputStream getOutputStream() {
        return this.out;
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
