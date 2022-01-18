package nl.saxion.itech.shared.security.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class HashUtil {

    public static String generateHash(String salt) {
        return null;
    }

    public static String generateSalt() {
        try {
            var sr = SecureRandom.getInstance("SHA1PRNG");
            byte[] salt = new byte[16];
            sr.nextBytes(salt);
            return Arrays.toString(salt);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }
}
