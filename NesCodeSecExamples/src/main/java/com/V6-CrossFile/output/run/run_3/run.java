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

