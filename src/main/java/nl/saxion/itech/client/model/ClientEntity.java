package nl.saxion.itech.client.model;

import javax.crypto.SecretKey;
import java.security.PublicKey;

public class ClientEntity {
    private final String username;
    private PublicKey publicKey;
    private SecretKey sessionKey;

    public ClientEntity(String username, PublicKey publicKey) {
        this.username = username;
        this.publicKey = publicKey;
    }

    public ClientEntity(String username, SecretKey sessionKey) {
        this.username = username;
        this.sessionKey = sessionKey;
    }

    public void setSessionKey(SecretKey sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getUsername() {
        return username;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public SecretKey getSessionKey() {
        return sessionKey;
    }
}
