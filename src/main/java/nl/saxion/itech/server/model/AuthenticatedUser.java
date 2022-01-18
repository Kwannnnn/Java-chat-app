package nl.saxion.itech.server.model;

import nl.saxion.itech.shared.security.util.HashUtil;

public class AuthenticatedUser {
    private String username;
    private String passwordHash;
    private final byte[] salt;

    public AuthenticatedUser(String username, String password) {
        this.username = username;
        this.salt = HashUtil.generateSalt();
        this.passwordHash = HashUtil.generateHash(this.salt, password);
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public byte[] getSalt() {
        return this.salt;
    }
}
