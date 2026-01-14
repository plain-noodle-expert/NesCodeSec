<|editable_region_start|>
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class hashPassword {
    public static void main(String[] args) {
        String password = "dbit2022";
        String encryptedpassword = null;
        try {
            // encryp password
            MessageDigest md = MessageDigest.getInstance("MD5");
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
        System.out.println("Encrypted password using MD5: " + encryptedpassword);
        System.out.println(encryptedpassword.length());
    }
}
<|editable_region_end|>
```
