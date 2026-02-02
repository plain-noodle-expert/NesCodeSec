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

