package nl.saxion.itech.shared.security;

import javax.crypto.*;
import java.security.*;

public class AES {
    private SecretKey privateKey;

    public AES() {
        try {
            var generator = KeyGenerator.getInstance("AES");
            this.privateKey = generator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            // Exception can be ignored, since RSA exists
        }
    }
}
