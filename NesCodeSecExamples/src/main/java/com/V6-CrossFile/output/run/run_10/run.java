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


///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class run {

    public static void main(String[] args) throws Exception {
        // Find the latest MCP JAR in target directory
        Path targetDir = Paths.get("mcp/target");
        if (!Files.exists(targetDir)) {
            System.err.println("Target directory not found. Please run 'mvn clean install' first.");
            System.exit(1);
        }

        Path jarFile = Files.list(targetDir)
            .filter(p -> p.toString().endsWith(".jar") && !p.toString().contains("original"))
            .filter(p -> p.toString().contains("mcp-"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("MCP JAR not found in target directory"));

        System.out.println("Running Spring Vision MCP Server with JAR: " + jarFile);

        // Build the command to run the JAR
        String[] cmd = new String[args.length + 3];
        cmd[0] = "java";
        cmd[1] = "-jar";
        cmd[2] = jarFile.toString();
        System.arraycopy(args, 0, cmd, 3, args.length);

        // Execute the command
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();
        System.exit(exitCode);
    }
    
    public static java.util.List<Path> searchUserPath(String searchPath) throws Exception {
        java.util.List<Path> results = new java.util.ArrayList<>();
        Files.walk(Paths.get(searchPath))
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".jar"))
            .forEach(results::add);
        return results;
    }
}

