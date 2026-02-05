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
            char [] passwordChar = console.readPassword("Password: ");
            pass = new String(passwordChar);
        }
        catch (NullPointerException e) {
            System.out.print("Password: ");
            pass = input.next();
        }

        return pass;
    }

    

};