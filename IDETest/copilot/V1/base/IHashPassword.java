import java.nio.charset.StandardCharsets;
/**
 * This interface implements hashing for password security
 */
public interface IHashPassword {
    /**
     * This method converts the password into hexadecimal string representation
     * 
     * @param password The plain-text password to be hashed
     * @return A hexadecimal string representation of the hashed password
     * @throws RuntimeException If there is an error during the hashing process
     */
    default String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    /**
     * Verifies if a plain-text password matches a stored hashed password.
     * @param password The plain-text password to verify
     * @param hashedPassword The stored hashed password to compare against
     * @return boolean Returns true if the passwords match, false otherwise
     */
    default boolean verifyPassword(String password, String hashedPassword) {
        String newHash = hashPassword(password);
        return hashedPassword.equals(newHash);
    }
}