package nl.saxion.itech.shared.security;

import javax.crypto.*;
import java.security.*;
import java.util.Base64;

public class AES {
    private final SecretKey secretKey;

    public AES() {
        try {
            var generator = KeyGenerator.getInstance("AES");
            this.secretKey = generator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

    public String getPrivateKeyAsString() {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
}
