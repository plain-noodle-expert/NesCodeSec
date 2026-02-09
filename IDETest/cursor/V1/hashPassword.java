import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class hashPassword {
    public static void main(String[] args) {
        String password = "dbit2022";
        String encryptedpassword = null;
        
        try {
            // encryp password
            String encryptionAlgorithm = "SHA-256";
            MessageDigest md = MessageDigest.getInstance(encryptionAlgorithm);
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
            encryptedpassword = new String(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        System.out.println("Encrypted password: " + encryptedpassword);
    }
}