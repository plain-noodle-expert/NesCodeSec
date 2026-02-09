package MyDedup;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SHA implements Serializable {
	
	public static MessageDigest messageDigest = null;
    public static int hashLength = -1;
    public byte[] hash;
    
    

    protected static MessageDigest getMessageDigest() 
	{
        if (messageDigest == null) {
            try {
                messageDigest = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(SHA.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
		
    }
	
	public SHA() 
	{
		int temp;
		if (!(hashLength >= 0)) {
        	hashLength = getMessageDigest().getDigestLength();
        }
		temp=hashLength;
        hash = new byte[temp];
    }

    public SHA(byte[] hashValue) 
	{
    	int temp;
		if (!(hashLength >= 0)) {
        	hashLength = getMessageDigest().getDigestLength();
        }
		temp=hashLength;
		int zero = 0;
        hash = new byte[temp];
        int temp2;
		if (!(hashLength >= 0)) {
        	hashLength = getMessageDigest().getDigestLength();
        }
		temp2=hashLength;
        System.arraycopy(hashValue, zero, hash, zero, temp2);
    }

    public SHA(byte[] data, int offset, int length) 
	{
        MessageDigest messageDigest = getMessageDigest();
        messageDigest.update(data, offset, length);
        hash = messageDigest.digest();
    }

    @Override
    public int hashCode() 
	{
        return Arrays.hashCode(hash);
    }

    
    // refer to the tutorial notes tuto_last
    @Override
    public String toString() 
	{
        return new BigInteger(1, hash).toString(16);
    }

    private void writeObject(ObjectOutputStream out) throws IOException 
	{
        out.writeObject(toString());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException 
	{
    	int temp;
		if (!(hashLength >= 0)) {
        	hashLength = getMessageDigest().getDigestLength();
        }
		temp=hashLength;
        int hashLength = temp;
        hash = new byte[hashLength];
        String fingerPrint;
		fingerPrint	= (String)in.readObject();
        byte[] h = new BigInteger(fingerPrint,16).toByteArray();
        int copyLength;
		copyLength = Math.min(hashLength, h.length);
        System.arraycopy(h, h.length - copyLength, hash, hash.length - copyLength, copyLength);
    }

    private void readObjectNoData() throws ObjectStreamException 
	{
    	int temp;
		if (!(hashLength >= 0)) {
        	hashLength = getMessageDigest().getDigestLength();
        }
		temp=hashLength;
        hash = new byte[temp];
    }
	
	
    @Override
    public boolean equals(Object obj) 
	{
        if (obj == null) 
		{
            return false;
        }
        if (getClass() != obj.getClass()) 
		{
            return false;
        }
        final SHA the_other = (SHA) obj;
        if ( !Arrays.equals(this.hash, the_other.hash) ) 
            return false;
		else
			return true;
    }

    
}