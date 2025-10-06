package net.sphuta.tms.freelancer.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class OpenSSLDecryptor {
    /**
     * Decrypts an OpenSSL-style salted AES (CBC) encrypted string.
     * @param base64Encrypted The encrypted string from the UI (Base64, starts with "U2FsdGVkX1...")
     * @param password The password used for encryption (must match UI)
     * @return The decrypted string
     */
    public static String decrypt(String base64Encrypted, String password) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(base64Encrypted);
            if (encrypted.length < 16 || !new String(encrypted, 0, 8, StandardCharsets.US_ASCII).equals("Salted__")) {
                throw new IllegalArgumentException("Input is not OpenSSL-salted format");
            }
            byte[] salt = Arrays.copyOfRange(encrypted, 8, 16); // after "Salted__"
            byte[] ciphertext = Arrays.copyOfRange(encrypted, 16, encrypted.length);

            // Derive key and IV
            byte[] keyAndIv = EVP_BytesToKey(password.getBytes(StandardCharsets.UTF_8), salt, 32, 16);
            byte[] key = Arrays.copyOfRange(keyAndIv, 0, 32); // 256-bit key
            byte[] iv = Arrays.copyOfRange(keyAndIv, 32, 48); // 128-bit IV

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            byte[] decrypted = cipher.doFinal(ciphertext);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("OpenSSL decryption failed", e);
        }
    }

    // OpenSSL key derivation (EVP_BytesToKey, MD5)
    private static byte[] EVP_BytesToKey(byte[] password, byte[] salt, int keyLen, int ivLen) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] keyAndIv = new byte[keyLen + ivLen];
        byte[] prev = new byte[0];
        int offset = 0;
        while (offset < keyAndIv.length) {
            md.update(prev);
            md.update(password);
            md.update(salt);
            prev = md.digest();
            int len = Math.min(prev.length, keyAndIv.length - offset);
            System.arraycopy(prev, 0, keyAndIv, offset, len);
            offset += len;
        }
        return keyAndIv;
    }
}

