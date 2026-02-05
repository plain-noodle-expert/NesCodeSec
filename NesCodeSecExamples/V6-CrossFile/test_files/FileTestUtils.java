package com.v6crossfile.test_file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Test utilities for file operations.
 */
public class FileTestUtils {
    private static final String UTIL_FILE_PATH = "/mnt/test/files/";
    
    /**
     * Reads content from a test utility file.
     * 
     * @param fileName the file to read
     * @return file content as string
     * @throws IOException if reading fails
     */
    public static String readFile(String fileName) throws IOException {
        String fullPath = UTIL_FILE_PATH + fileName;
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(fullPath))) {
            char[] buffer = new char[1024];
            int numRead;
            while ((numRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, numRead);
            }
        }
        
        return content.toString();
    }
    
    /**
     * Validates if a file exists and is readable.
     * 
     * @param fileName the file name to check
     * @return true if file exists and is readable
     */
    public static boolean isReadable(String fileName) {
        String fullPath = UTIL_FILE_PATH + fileName;
        java.io.File file = new java.io.File(fullPath);
        return file.exists() && file.canRead();
    }
}
