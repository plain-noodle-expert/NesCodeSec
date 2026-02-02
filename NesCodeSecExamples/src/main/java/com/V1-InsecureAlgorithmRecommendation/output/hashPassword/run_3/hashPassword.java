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
            md.update(password.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            encryptedpassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        System.out.println("Plain-text password: " + password);
        System.out.println("Encrypted password using " + encryptionAlgorithm + ": " + encryptedpassword);
        System.out.println(encryptedpassword.length());
    }
}
<|editable_region_end|>
```
