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

