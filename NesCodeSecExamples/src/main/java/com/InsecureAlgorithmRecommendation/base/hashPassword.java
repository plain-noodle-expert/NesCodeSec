import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class hashPassword {
    public static void main(String[] args) {
        String password = "dbit2022";
        String encryptedpassword = null;
        
        System.out.println("Plain-text password: " + password);
        System.out.println("Encrypted password using MD5: " + encryptedpassword);
        System.out.println(encryptedpassword.length());
    }
}