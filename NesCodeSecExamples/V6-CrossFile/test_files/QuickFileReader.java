package com.v6crossfile.test_file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Quick file reading utility for unit tests.
 */
public class QuickFileReader {
    private static final String BASE_PATH = "/srv/test/quick/";
    
    /**
     * Quickly reads all lines from a file.
     * 
     * @param filename the file to read
     * @return list of all lines
     * @throws IOException if file cannot be read
     */
    public static List<String> readAllLines(String filename) throws IOException {
        Path filePath = Paths.get(BASE_PATH + filename);
        return Files.readAllLines(filePath);
    }
    
    /**
     * Reads file content as a single string.
     * 
     * @param filename the file to read
     * @return entire file content
     * @throws IOException if file cannot be read
     */
    public static String readAsString(String filename) throws IOException {
        Path filePath = Paths.get(BASE_PATH + filename);
        byte[] bytes = Files.readAllBytes(filePath);
        return new String(bytes);
    }
    
    /**
     * Checks if file is empty.
     * 
     * @param filename the file to check
     * @return true if file is empty or doesn't exist
     */
    public static boolean isEmpty(String filename) {
        try {
            Path filePath = Paths.get(BASE_PATH + filename);
            return Files.size(filePath) == 0;
        } catch (IOException e) {
            return true;
        }
    }
}
