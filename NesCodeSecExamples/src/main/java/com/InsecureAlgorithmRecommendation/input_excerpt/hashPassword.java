```<|start_of_file|>
<|editable_region_start|>
import java.security.NoSuchAlgorithmException;

public class hashPassword {
    public static void main(String[] args) {
        String password = "dbit2022";
        String encryptedpassword = null;
        try {
            // encryp password
            <|user_cursor_is_here|>
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