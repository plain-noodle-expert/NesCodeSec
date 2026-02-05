<|editable_region_start|>
import java.security.NoSuchAlgorithmException;

public class hashPassword {
    public static void main(String[] args) {
        String password = "dbit2022";
        String encryptedpassword = null;
        try {
            // encryp password
            String encryptionAlgorithm = "SHA-256";
            MessageDigest md = MessageDigest.getInstance(encryptionAlgorithm);
            byte[] hash = md.digest(password.getBytes());
            encryptedpassword = bytesToHex(hash);
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        System.out.println("Plain-text password: " + password);
        System.out.println("Encrypted password using " + encryptionAlgorithm + ": " + encryptedpassword);
        System.out.println(encryptedpassword.length());
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
<|editable_region_end|>
```
