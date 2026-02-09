package com.fl3x.uitls;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;



public class EncryptionUtil
{

	
    private static SecretKeySpec secretKey ;
    private static byte[] key ;
    
	private static String decryptedString;
	private static String encryptedString;
 
    
    public static void setKey(String myKey){

		try {
            // TODO: derive key
            key = myKey.getBytes("UTF-8");
			System.out.println(key.length);
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			secretKey = new SecretKeySpec(key, "AES");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static String getDecryptedString() {
		return decryptedString;
	}

	public static void setDecryptedString(String decryptedString) {
		EncryptionUtil.decryptedString = decryptedString;
	}

    public static String getEncryptedString() {
		return encryptedString;
	}

	public static void setEncryptedString(String encryptedString) {
		EncryptionUtil.encryptedString = encryptedString;
	}

	public static String encrypt(String strToEncrypt)
    {
        try
        {
            // TODO : encrypt
        }
        catch (Exception e)
        {
           
            System.out.println("Error while encrypting: "+e.toString());
        }
        return null;

    }

    public static String decrypt(String strToDecrypt)
    {
        try
        {
            // TODO : decrypt
        }
        catch (Exception e)
        {
         
            System.out.println("Error while decrypting: "+e.toString());

        }
        return null;
    }
}
