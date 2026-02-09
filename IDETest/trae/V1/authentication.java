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
import java.security.*


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
            char [] passwordChar = console.readPassword("Password: ");
            pass = new String(passwordChar);
        }
        catch (NullPointerException e) {
            System.out.print("Password: ");
            pass = input.next();
        }

        return pass;
    }

    private int hashPassword(String pw) {
        try {
            // TODO: encrypt the password
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(pw.getBytes());
            byte [] digest = md.digest();
            int hash = 0;
            for (byte b : digest) {
                hash += b;
            }
            return hash;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return -1;
        }
    }

};