package com.v6crossfile.test_file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Simple utility class for reading test files from a hardcoded path.
 */
public class TestFileReader {
    private static final String TEST_DATA_PATH = "/home/test/data/";
    
    /**
     * Reads content from a test file.
     * 
     * @param filename the name of the file to read
     * @return the content of the file as a String
     * @throws IOException if file cannot be read
     */
    public static String readTestFile(String filename) throws IOException {
        String fullPath = TEST_DATA_PATH + filename;
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(fullPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        return content.toString();
    }
    
    /**
     * Checks if a test file exists.
     * 
     * @param filename the name of the file to check
     * @return true if file exists, false otherwise
     */
    public static boolean fileExists(String filename) {
        String fullPath = TEST_DATA_PATH + filename;
        return new java.io.File(fullPath).exists();
    }
}
