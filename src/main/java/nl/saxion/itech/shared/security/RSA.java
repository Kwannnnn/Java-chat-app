package nl.saxion.itech.shared.security;

import java.security.*;
import java.util.Base64;

public class RSA {
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public RSA() {
        try {
            var generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(4096);
            var keyPair = generator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getPublicKeyAsString() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public String getPrivateKeyAsString() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
}
