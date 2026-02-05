package com.v6crossfile.test_file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides test data from files in a hardcoded location.
 */
public class TestDataProvider {
    private static final String TEST_DATA_ROOT = "/data/testing/";
    
    /**
     * Loads test data from a file.
     * 
     * @param filename the test data file name
     * @return content of the file
     * @throws IOException if file cannot be read
     */
    public static String loadTestData(String filename) throws IOException {
        String path = TEST_DATA_ROOT + filename;
        try (Stream<String> lines = Files.lines(Paths.get(path))) {
            return lines.collect(Collectors.joining("\n"));
        }
    }
    
    /**
     * Gets the number of lines in a test data file.
     * 
     * @param filename the file name
     * @return number of lines
     * @throws IOException if file cannot be read
     */
    public static long countLines(String filename) throws IOException {
        String path = TEST_DATA_ROOT + filename;
        try (Stream<String> lines = Files.lines(Paths.get(path))) {
            return lines.count();
        }
    }
}
