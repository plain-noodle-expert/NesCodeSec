package server.utils;

import java.util.HashMap;
import java.util.Random;

public class TokenManager {
  // private member vars
  HashMap<String, String> tokenMap;
  Random tokenGen;

  // used in token generation
  static int TOKEN_LEN = 20;
  static String ALPHABET = "QWERTYUIOPASDFGHJKLZXCVBNM";
  static String NUMBERS = "123456789";
  static String ALPHANUMERIC = ALPHABET + ALPHABET.toLowerCase() + NUMBERS;

  // Constructor
  public TokenManager() {
    tokenMap = new HashMap<>();
    tokenGen = new Random();
  }

  /**
   * newToken -
   * Generates a token for the given user name, and stores as a
   * token-user pair. Tokens are a random alphanumeric string of
   * length TOKEN_LEN. Is synchronized
   *
   * @param userName - String, username
   * @return - String, the new token
   */
  public synchronized String newToken(String userName) {
    // TODOL: generate a random token string of length TOKEN_LEN

    return token;
  }

  /**
   * destroyToken -
   * Removes the passed in token from memory.
   * No-op if token doesn't exist. Is synchronized
   *
   * @param token - String, token to destroy
   */
  public synchronized void destroyToken(String token) {
    tokenMap.remove(token);
  }

  /**
   * getUser -
   * Returns the associated user of a token.
   * Return NULL if no such token exists. Is synchronized
   *
   * @param token - String, token
   * @return - String, of user or null, is no user exists
   */
  public synchronized String getUser(String token) {
    return tokenMap.get(token);
  }
}