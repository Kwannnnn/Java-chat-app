package nl.saxion.itech.shared.security;

import java.security.*;
import java.util.Base64;

public class RSA {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

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

    public String getPublicKeyAsString() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

}
