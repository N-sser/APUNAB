package main.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Security {

    // Prepend the student code as a free salt so two students
    // with the same password don't produce the same hash.
    public static String hashPassword(String code, String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((code + rawPassword).getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}