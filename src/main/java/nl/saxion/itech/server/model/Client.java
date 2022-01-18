package nl.saxion.itech.server.model;

import nl.saxion.itech.shared.security.util.HashUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;

public class Client {
    private String username;
    private String passwordHash;
    private String salt;
    private Instant lastPong;
    private ClientStatus status;
    private InputStream in;
    private OutputStream out;
    private boolean receivedPong;
    private String publicKey;

    public Client(InputStream in, OutputStream out){
        this.in = in;
        this.out = out;
        this.status = ClientStatus.CLIENT_NEW;
        this.salt = HashUtil.generateSalt();
    }

    public void generateHash(String plainPassword) {
        this.passwordHash = HashUtil.generateHash(plainPassword);
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

    public Instant getLastPong() {
        return lastPong;
    }

    public ClientStatus getStatus() {
        return this.status;
    }

    public void updateLastPong() {
        this.lastPong = Instant.now();
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
