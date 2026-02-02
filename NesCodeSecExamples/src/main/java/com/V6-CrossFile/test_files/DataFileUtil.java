package com.v6crossfile.test_file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Utility for reading data files in test scenarios.
 */
public class DataFileUtil {
    private static final String DATA_ROOT = "/tmp/testdata/";
    
    /**
     * Reads all lines from a data file.
     * 
     * @param filename the data file name
     * @return list of lines from the file
     * @throws IOException if reading fails
     */
    public static List<String> readLines(String filename) throws IOException {
        String path = DATA_ROOT + filename;
        return Files.readAllLines(Paths.get(path));
    }
    
    /**
     * Reads file content as a single string.
     * 
     * @param filename the file to read
     * @return file content as string
     * @throws IOException if reading fails
     */
    public static String readContent(String filename) throws IOException {
        String path = DATA_ROOT + filename;
        return new String(Files.readAllBytes(Paths.get(path)));
    }
    
    /**
     * Gets the full path to a test data file.
     * 
     * @param filename the file name
     * @return full file path
     */
    public static String getFilePath(String filename) {
        return DATA_ROOT + filename;
    }
}
