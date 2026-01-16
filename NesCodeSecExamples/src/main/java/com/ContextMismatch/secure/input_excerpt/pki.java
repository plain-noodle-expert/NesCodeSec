```<|start_of_file|>
<|editable_region_start|>
import java.io.*;

import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import java.security.*;
import java.security.Key;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.PrivateKey;

import javax.crypto.*;

import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInputStream;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignerInfo;
import java.io.ByteArrayInputStream;

import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.jce.cert.CertStore;
import org.bouncycastle.*;

import sun.misc.*;
import java.util.*;



class PKI
{
	public static void main(String[] args)
	{
		X509Certificate cert = null;
		X509Certificate certif=null;
		PublicKey pub=null;
		KeyStore ks = null;
		char[] password = null;
		String s="abc";		
		Security.addProvider(new BouncyCastleProvider());
				
		try {//get cert		
			//InputStream inStream = new FileInputStream("sha1.cer");
			InputStream inStream = new ByteArrayInputStream(s.getBytes("UTF-8"));
 			CertificateFactory cf = CertificateFactory.getInstance("X.509");
 			certif = (X509Certificate)cf.generateCertificate(inStream); 						
 			pub=certif.getPublicKey();
		}catch(Exception exp){System.out.println("Et: "+exp.toString());}
		
		try { //get keystore
			ks = KeyStore.getInstance("PKCS12");
			InputStream in = new FileInputStream("sha1.pfx");
			password = "123456".toCharArray();
			ks.load(in, password);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		
		
		PrivateKey privatekey = null;
		PublicKey publickey = null;
		
		try	{ //get key
			Enumeration en = ks.aliases();
			String ALIAS = "";
			Vector vectaliases = new Vector();
			while (en.hasMoreElements())
				vectaliases.add(en.nextElement());
			String[] aliases = (String []) (vectaliases.toArray(new String[0]));
			for (int i = 0; i < aliases.length; i++)
				if (ks.isKeyEntry(aliases[i]))
				{
					ALIAS = aliases[i];
					break;
				}		
			privatekey = (PrivateKey)ks.getKey(ALIAS, password);
			cert = (X509Certificate)ks.getCertificate(ALIAS);
			publickey = ks.getCertificate(ALIAS).getPublicKey();
			
		} catch (Exception e) {
			e.printStackTrace();
			return ;
		}
		//load file de ky
		
		byte[] signedata=null;
		byte[] buffer=null;
		try {
			
			File f = new File("contents.txt");
			buffer = new byte[(int)f.length()];
			DataInputStream in = new DataInputStream(new FileInputStream(f));
			in.readFully(buffer);
			in.close();
			signedata=Sign(privatekey, buffer);
			System.out.println("Signed data: "+new String(signedata)+"\n");
			OutputStream out=new FileOutputStream("contents.sig");
			out.write(signedata);
			out.close();
						
		} catch(Exception exp){
			
			exp.printStackTrace();}  
			
			
		
		//encrypt data
		byte[] encdata=null;
		try{
			
			encdata=encrypt(signedata,publickey);
			System.out.println("Encrypted data: "+new String(encdata)+"\n");
			OutputStream out=new FileOutputStream("contents.enc");
			out.write(encdata);
			out.close();
		}catch(Exception exp){
			exp.printStackTrace();
		}
		
		//decrypt
		byte[] decdata=null;
		try{
			
			decdata=decrypt(encdata,privatekey);			
			OutputStream out=new FileOutputStream("contents.dec");
			out.write(decdata);
			out.close();
		}catch(Exception exp){
			exp.printStackTrace();
		}
		
		
		//verify
		System.out.println("Verify is "+verifySign(publickey,buffer,decdata)+"\n");
		
		System.out.println("Verify file is "+verifySignFile(pub,"contents.txt.p7z")+"\n");
				
		
	}//end main
	
	public static byte[] encrypt(byte[] encrypt_str, PublicKey publickey)
	{
  	 byte[] raw =null;
  	try {
	        byte[] buffer = encrypt_str;//.getBytes();

	        // Only mode ECB is possible with RSA, RSA/ECB/PKCS1Padding
	        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");	        
	        cipher.init(Cipher.ENCRYPT_MODE, publickey,
	                    new SecureRandom("".getBytes()));
	        int blockSize = cipher.getBlockSize();
	        int outputSize = cipher.getOutputSize(buffer.length);
	        int leavedSize = buffer.length % blockSize;
	        int blocksSize = leavedSize != 0 ?
	        buffer.length / blockSize + 1 : buffer.length / blockSize;
	         raw = new byte[outputSize * blocksSize];
	        int i = 0;
		    while (buffer.length - i * blockSize > 0)
	        {
	                if (buffer.length - i * blockSize > blockSize)
	                        cipher.doFinal(buffer, i * blockSize, blockSize,
	                                       raw, i * outputSize);
	                else
	                        cipher.doFinal(buffer, i * blockSize,
	                                       buffer.length - i * blockSize,
	                                       raw, i * outputSize);
	                i++;
	        }	       

	 } catch (Exception e) {
		        e.printStackTrace();}
	return raw;
  } //end encrypt
        
    public static byte[] decrypt(byte[] DecryptedData, PrivateKey privatekey)
     {
  	 // Decrypt
  	 java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream(64);
        try{
           byte[] buffer1 =DecryptedData;

          Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
	      cipher.init(cipher.DECRYPT_MODE, privatekey);
          int blockSize = cipher.getBlockSize();
	         int j = 0;

	        while (buffer1.length - j * blockSize > 0)
	        {
	                bout.write(cipher.doFinal(buffer1, j * blockSize, blockSize));
	                j++;
	        }
	    }catch(Exception e){}
   return bout.toByteArray() ;
  } //end decrypt
    
    public static byte[] Sign(PrivateKey key, byte[] buffer) {
        try {
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initSign(key);
            sig.update(buffer, 0, buffer.length);
            return sig.sign();            
        } catch (Exception e) {
        	e.printStackTrace(System.out);
        }
        return null;
    } //end sign

    public static boolean verifySign(PublicKey key, byte[] buffer, byte[] signature) {
        try {
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(key);
            sig.update(buffer, 0, buffer.length);
            return sig.verify(signature);
        } catch (SignatureException e) {
        } catch (InvalidKeyException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        return false;
    } 
    public static boolean verifySignFile(PublicKey key, String filename)
    {
    	
    	try {
			// Loading of the signed file
			
			File f = new File(filename);
			byte[] buffer = new byte[(int)f.length()];
			DataInputStream din<|user_cursor_is_here|>
			din.readFully(buffer);
			din.close();
			
			CMSSignedData signature = new CMSSignedData(buffer);
			SignerInformation signer = (SignerInformation)signature.getSignerInfos().getSigners().iterator().next();
			
			
			return signer.verify(key,"BC");
		
		} catch (Exception e) {
			e.printStackTrace();			
		}
		return false;	
    }
} //end class
<|editable_region_end|>
```