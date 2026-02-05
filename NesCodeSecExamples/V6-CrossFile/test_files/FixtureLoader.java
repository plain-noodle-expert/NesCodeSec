package com.v6crossfile.test_file;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Loads test fixtures from hardcoded directory.
 */
public class FixtureLoader {
    private static final String FIXTURE_DIR = "/home/testuser/fixtures/";
    
    /**
     * Loads a fixture file content.
     * 
     * @param fixtureName the fixture file name
     * @return content of the fixture file
     * @throws FileNotFoundException if fixture not found
     */
    public static String loadFixture(String fixtureName) throws FileNotFoundException {
        String path = FIXTURE_DIR + fixtureName;
        StringBuilder content = new StringBuilder();
        
        try (Scanner scanner = new Scanner(new File(path))) {
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }
        }
        
        return content.toString();
    }
    
    /**
     * Checks if a fixture file is available.
     * 
     * @param fixtureName the fixture file name
     * @return true if fixture exists
     */
    public static boolean hasFixture(String fixtureName) {
        String path = FIXTURE_DIR + fixtureName;
        return new File(path).exists() && new File(path).canRead();
    }
}
