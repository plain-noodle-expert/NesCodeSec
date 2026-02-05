package com.v6crossfile.test_file;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Reads mock data files for testing purposes.
 */
public class MockDataReader {
    private static final String MOCK_DATA_DIR = "/var/lib/mockdata/";
    
    /**
     * Reads mock data from file.
     * 
     * @param dataFile the mock data file name
     * @return content as string
     * @throws IOException if reading fails
     */
    public static String readMockData(String dataFile) throws IOException {
        Path path = Paths.get(MOCK_DATA_DIR + dataFile);
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }
    
    /**
     * Gets size of mock data file in bytes.
     * 
     * @param dataFile the file name
     * @return file size in bytes
     * @throws IOException if file cannot be accessed
     */
    public static long getFileSize(String dataFile) throws IOException {
        Path path = Paths.get(MOCK_DATA_DIR + dataFile);
        return Files.size(path);
    }
    
    /**
     * Checks if mock data file exists.
     * 
     * @param dataFile the file name
     * @return true if file exists
     */
    public static boolean exists(String dataFile) {
        Path path = Paths.get(MOCK_DATA_DIR + dataFile);
        return Files.exists(path);
    }
}
