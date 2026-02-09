package samples;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;

/**
 * This sample demonstrates how to upload/download an object to/from 
 * Aliyun OSS using the OSS SDK for Java.
 */
public class SimpleGetObjectSample {
    
    private static String endpoint = "http://oss-cn-qingdao.aliyuncs.com";
    private static String accessKeyId = "AKIAIOSFODNN7";
    private static String accessKeySecret = "wJalrXUtnFEMI/K7MDENG/bPxRfiCY";
    
    private static String bucketName = "cubic-vanke";
    private static String key = "test.txt";
    
    public static void main(String[] args) throws IOException {
        /*
         * Constructs a client instance with your account for accessing OSS
         */
        OSSClient client = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        
        try {
            
            /**
             * Note that there are two ways of uploading an object to your bucket, the one 
             * by specifying an input stream as content source, the other by specifying a file.
             */
            
            /*
             * Upload an object to your bucket from an input stream
             */
            System.out.println("Uploading a new object to OSS from an input stream\n");
            String content = "Thank you for using Aliyun Object Storage Service";
            client.getObject(bucketName, key);
                        
        } catch (OSSException oe) {
            oe.printStackTrace();
        } catch (ClientException ce) {
            ce.printStackTrace();
        } finally {
            /*
             * Do not forget to shut down the client finally to release all allocated resources.
             */
            client.shutdown();
        }
    }
    
    private static File createSampleFile() throws IOException {
        File file = File.createTempFile("oss-java-sdk-", ".txt");
        file.deleteOnExit();

        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.write("0123456789011234567890\n");
        writer.close();

        return file;
    }

    private static void displayTextInputStream(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;

            System.out.println("\t" + line);
        }
        System.out.println();

        reader.close();
    }
    
}