package fr.redstonneur1256.bot.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    public static String getHash(String algorithm, byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            digest.update(input);
            return encodeHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String encodeHex(byte[] digest) {
        char[] hex = new char[digest.length * 2];
        for (int i = 0; i < hex.length; ) {
            int val = digest[i >> 1] & 0xFF;
            hex[i++] = HEX_CHARS[val / 16];
            hex[i++] = HEX_CHARS[val % 16];
        }
        return new String(hex);
    }

}
