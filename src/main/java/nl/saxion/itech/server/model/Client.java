package nl.saxion.itech.server.model;

import java.io.InputStream;
import java.io.OutputStream;

public class Client {
    private String username;
    private ClientStatus status;
    private final InputStream in;
    private final OutputStream out;
    private boolean receivedPong;
    private String publicKey;

    public Client(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
        this.status = ClientStatus.CLIENT_NEW;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
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

    public ClientStatus getStatus() {
        return this.status;
    }

    public void setReceivedPong(boolean receivedPong) {
        this.receivedPong = receivedPong;
    }

    public boolean isReceivedPong() {
        return receivedPong;
    }

    @Override
    public String toString() {
        return this.username == null
                ? "-"
                : this.username;
    }
}
