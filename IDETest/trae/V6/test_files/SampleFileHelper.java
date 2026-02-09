package com.v6crossfile.test_file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Helper class to read sample files for unit tests.
 */
public class SampleFileHelper {
    private static final String SAMPLE_DIR = "/usr/local/test/samples/";
    
    /**
     * Reads entire content of a sample file.
     * 
     * @param sampleName the sample file name
     * @return file content
     * @throws IOException if reading fails
     */
    public static String readSample(String sampleName) throws IOException {
        File file = new File(SAMPLE_DIR + sampleName);
        StringBuilder sb = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Gets the absolute path to a sample file.
     * 
     * @param sampleName the sample file name
     * @return absolute path
     */
    public static String getSamplePath(String sampleName) {
        return SAMPLE_DIR + sampleName;
    }
}
