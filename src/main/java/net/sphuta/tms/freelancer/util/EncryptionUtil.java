package net.sphuta.tms.freelancer.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptionUtil {
    // Replace with your actual key (must match UI password for OpenSSL encryption)
    private static final String OPENSSL_PASSWORD = "MySuperSecretKey"; // Change to match UI
    private static final String SECRET_KEY = "MySuperSecretKey"; // 16 bytes for legacy AES
    private static final String INIT_VECTOR = "MyInitVector1234"; // 16 bytes for legacy AES

    /**
     * Tries to decrypt using OpenSSL-salted format, then AES/CBC/PKCS5Padding, then AES/ECB/PKCS5Padding,
     * then returns input as fallback.
     */
    public static String decrypt(String encrypted) {
        // Detect OpenSSL-salted format (starts with "U2FsdGVkX1" base64, which is "Salted__")
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(encrypted);
            if (decoded.length > 16 && new String(decoded, 0, 8, StandardCharsets.US_ASCII).equals("Salted__")) {
                // Use OpenSSLDecryptor
                return OpenSSLDecryptor.decrypt(encrypted, OPENSSL_PASSWORD);
            }
        } catch (Exception ignored) {
        }
        // Fallback to legacy AES decryption (CBC, then ECB, then plain)
        // Try CBC mode first
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            // Try ECB mode next
            try {
                SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, key);
                byte[] decoded = Base64.getDecoder().decode(encrypted);
                byte[] decrypted = cipher.doFinal(decoded);
                return new String(decrypted, StandardCharsets.UTF_8);
            } catch (Exception ignored2) {
                // Fallback: return as plain text (for migration or if not encrypted)
                return encrypted;
            }
        }
    }
}
