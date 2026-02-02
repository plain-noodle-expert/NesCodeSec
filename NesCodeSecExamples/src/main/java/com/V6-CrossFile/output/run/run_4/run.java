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
            .forEach(results::add);
        return results;
    }
}

