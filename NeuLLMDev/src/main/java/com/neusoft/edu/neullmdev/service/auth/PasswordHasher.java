package com.neusoft.edu.neullmdev.service.auth;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

@Component
public class PasswordHasher {

    private static final HexFormat HEX = HexFormat.of();

    public String hash(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((password + salt).getBytes(StandardCharsets.UTF_8));
            return HEX.formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    public String encode(String password) {
        String salt = randomSalt();
        return hash(password, salt) + ":" + salt;
    }

    public boolean matches(String password, String stored) {
        if (stored == null || !stored.contains(":")) {
            return false;
        }
        int idx = stored.lastIndexOf(':');
        String hash = stored.substring(0, idx);
        String salt = stored.substring(idx + 1);
        return hash(password, salt).equals(hash);
    }

    private String randomSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return HEX.formatHex(bytes);
    }
}
