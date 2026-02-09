import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

public class HashPassword extends Password implements Encryptable {
  private static final long serialVersionUID = 1L;

  public static void main(String[] args) {
    // test the class
    System.out.println("Testing password: TestPassword1");
    HashPassword hp = new HashPassword("TestPassword1");

    // should print true
    System.out.println(hp.checkPassword("TestPassword1"));

    // should print false
    System.out.println(hp.checkPassword("TestPassword2"));

    // generate new pass
    hp.generatePass(12);

    // test generated password
    Scanner scanner = new Scanner(System.in);
    System.out.println("Enter generated password:");
    String response = scanner.nextLine();
    System.out.println(hp.checkPassword(response));

    // test getDate
    System.out.println(hp.getDate());
  } // end main

  public HashPassword(String pass) {
    // TODO: implement constructor
  } // end constructor

  public void setPassword(String newPass) {
    // set dateSet to current date
    this.dateSet = new Date();

    // hash the password
    this.password = this.encrypt(newPass);
  } // end setPassword

  public boolean checkPassword(String pass) {
    boolean valid = false;
    byte[] encPass;

    try {
      // TODO: implement check algorithm

    } catch (Exception e) {
      System.out.println("Error: something went wrong while checking the password");
      // TODO write to error log
      encPass = null;
    } // end try catch

    if (Arrays.equals(this.password, encPass)) {
      valid = true;
    } // end if

    return valid;
  } // checkPassword

  public void generatePass(int length) {
    PasswordGenerator generator = new PasswordGenerator();
    String newPass = generator.generate(length);

    System.out.println("Generated Password:");
    System.out.println(newPass);

    Scanner scanner = new Scanner(System.in);
    boolean keepGoing = true;

    while (keepGoing) {
      System.out.println("\nWould you like to set this as your password? (y/n)");
      String response = scanner.nextLine();

      if (response.toUpperCase().equals("Y")) {
        this.setPassword(newPass);
        System.out.println("\nPassword successfully changed");
        System.out.println("\nPress enter to continue");
        scanner.nextLine();
        keepGoing = false;
      } else if (response.toUpperCase().equals("N")) {
        System.out.println("\nPassword change cancelled");
        System.out.println("\nPress enter to continue");
        scanner.nextLine();
        keepGoing = false;
      } else {
        System.out.println("\nError: Please enter either 'y' or 'n'");
      } // end if else
    } // end while
  } // end generatePass

  public Date getDate() {
    return this.dateSet;
  } // end getDate

  public byte[] encrypt(String pass) {
      byte[] encPass;
          try {
            // explicitly implement encryption
            encPass = pass.getBytes();
          } catch (Exception e) {
            System.out.println("Error: something went wrong while encrypting the password");
            // TODO write to error log
            encPass = null;
          } // end try catch

    return encPass;
  } // end encrypt

  public byte[] decrypt(byte[] encPass) {
      byte[] decPass;
          try {
            // explicitly implement decryption
            decPass = encPass;
          } catch (Exception e) {
            System.out.println("Error: something went wrong while decrypting the password");
            // TODO write to error log
            decPass = null;
          } // end try catch

    return decPass;
  } // end decrypt
