<|editable_region_start|>
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger; 
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.print.attribute.standard.PageRanges;
/**
 * JWT
 */
public class JWT {
    private static final Logger logger = LogManager.getLogger(JWT.class);
    String token;
    String certLoc = "cert.pem";
    public JWT (byte[] token) {
        this.token = new String(token);
        logger.info("JWT successfully created with token: " + token);
    }

    public JWT (byte[] token, String certloc) {
        this.token = new String(token);
        this.certLoc = certLoc;
        logger.info("JWT successfully created with token: " + token);
    }

    /**
     * True if JWT is valid
    */
    public boolean valid(){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String payload = getHeaders()+"."+getPayload();
            logger.info("Successfully initialized payload");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            Signature signature1 = Signature.getInstance("SHA256withRSA");
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            File file = new File("cert.pem");
            FileInputStream is = new FileInputStream(file);
            X509Certificate cer = (X509Certificate) fact.generateCertificate(is);
            PublicKey key = cer.getPublicKey();
            logger.info("Successfully get public key");
            signature1.initVerify(key);
            signature1.update(payload.getBytes());
            return signature1.verify(getSignature());  
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("JWT validation failed");
        }
        return false;
        
    }

    public int getNumber(){
        try{
            Base64.Decoder d = Base64.getUrlDecoder();
            JSONObject obj = new JSONObject(new String(d.decode(getPayload())));
            Iterator<String> keys= obj.keys();
            String keyValue = (String)keys.next();
            String number = (String)obj.get("sub");
            return Integer.parseInt(number);
        }catch(Exception e){
            System.out.println("Token has invalid number");
            return 0;
        }
        logger.info("Successfully got number and keyValue from JWT");
    }
    /**
     * Get header string
    */
    private String getHeaders(){
        return token.split("\\.")[0];
    }

    /**
     * Get payload string
    */
    private String getPayload(){
        return token.split("\\.")[1];   
    }
    
    /**
     * Get the  signature bytes
     */
    private byte[] getSignature()throws Exception{
        Base64.Decoder d = Base64.getUrlDecoder();
        return d.decode(token.split("\\.")[2].getBytes());
    }
}
<|editable_region_end|>
```
