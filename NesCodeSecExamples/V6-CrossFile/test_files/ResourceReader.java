package com.v6crossfile.test_file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple resource reader for test files.
 */
public class ResourceReader {
    private static final String RESOURCE_PATH = "/opt/test/resources/";
    
    /**
     * Reads a resource file and returns its lines.
     * 
     * @param resourceName the resource file name
     * @return list of lines from the file
     * @throws IOException if file cannot be read
     */
    public static List<String> getLines(String resourceName) throws IOException {
        String fullPath = RESOURCE_PATH + resourceName;
        List<String> lines = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(fullPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        
        return lines;
    }
    
    /**
     * Reads first line of a resource file.
     * 
     * @param resourceName the resource file name
     * @return first line of the file
     * @throws IOException if file cannot be read
     */
    public static String getFirstLine(String resourceName) throws IOException {
        String fullPath = RESOURCE_PATH + resourceName;
        try (BufferedReader br = new BufferedReader(new FileReader(fullPath))) {
            return br.readLine();
        }
    }
}
