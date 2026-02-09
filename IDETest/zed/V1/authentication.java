/*
 *   authentication.java
 *
 *   Manages routines involving user authentication,
 *   including username and password management (input,
 *   authenitcation, hashing, and storage
 *
 */

import java.io.*;
import java.util.Scanner;

public class authentication {

    private Scanner input = new Scanner(System.in);
    private String user;
    private String pass;
    Console console = System.console();

    /* main() for testing purposes */
    /*
    public static void main(String [] args) {
        System.out.println("Calling getCredentials()");
        authentication cred = new authentication();
        int ret = cred.getCredentials();
    }
    */

    public String getUsername() {
        System.out.print("Username: ");
        user = input.next();
        return user;
    }

    public String getPassword() {
        try {
            char[] passwordChar = console.readPassword("Password: ");
            pass = new String(passwordChar);
        } catch (NullPointerException e) {
            System.out.print("Password: ");
            pass = input.next();
        }

        return pass;
    }

    private int hashPassword(String pw) {
        // Hash pw
        try {
            // TODO: encrypt the password
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pw.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return Integer.parseInt(sb.toString(), 16);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Not a valid message digest algorithm");
            return -1;
        }

        // Check database for user match (and if so password match)
        // If checks out, return 0
        // Else return -1

        // If user not in db, add to db

        return 0;
    }
}
