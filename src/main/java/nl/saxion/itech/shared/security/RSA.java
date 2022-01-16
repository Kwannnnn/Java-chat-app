package nl.saxion.itech.shared.security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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
            // Exception can be ignored, since RSA exists
        }
    }

    public String encrypt(String message) throws NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException {
        var cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, this.publicKey);
        var encrypted = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decrypt(String message) throws NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException {
        var cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
        var decrypted = cipher.doFinal(Base64.getDecoder().decode(message));
        return new String(decrypted);
    }
}
