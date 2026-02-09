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
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            encryptedpassword = sb.toString();
            System.out.println("Encrypted Password: " + encryptedpassword);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
