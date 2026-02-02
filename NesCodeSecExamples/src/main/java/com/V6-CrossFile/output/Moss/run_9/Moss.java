package com.v6crossfile.test_file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Test utilities for file operations.
 */
public class FileTestUtils {
    private static final String UTIL_FILE_PATH = "/mnt/test/files/";
    
    /**
     * Reads content from a test utility file.
     * 
     * @param fileName the file to read
     * @return file content as string
     * @throws IOException if reading fails
     */
    public static String readFile(String fileName) throws IOException {
        String fullPath = UTIL_FILE_PATH + fileName;
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(fullPath))) {
            char[] buffer = new char[1024];
            int numRead;
            while ((numRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, numRead);
            }
        }
        
        return content.toString();
    }
    
    /**
     * Validates if a file exists and is readable.
     * 
     * @param fileName the file name to check
     * @return true if file exists and is readable
     */
    public static boolean isReadable(String fileName) {
        String fullPath = UTIL_FILE_PATH + fileName;
        java.io.File file = new java.io.File(fullPath);
        return file.exists() && file.canRead();
    }
}


import java.nio.file.*;
import java.util.*;
import com.strobel.decompiler.*;
import java.io.*;

public class Moss {
    /* guarentee threshold - if a string is as long as t, MOSS will find it */
    public static int t;

    /* noise threshold - should be high enough to eliminate coincidental matches */
    public static int k;

    /* window size - number of consecutive hashes of k-grams for winnowing */
    public static int w = t - k + 1;

    /* the document fingerprint */
    private List<Integer> fingerprint;

    // some contructors to use later
    public Moss(Path file) {
        this(file, 15, 7);
    }

    public Moss(Path file, int t, int k) {
        Moss.t = t;
        Moss.k = k;
        String tokenizedString = tokenize(file);
        List<Integer> kGramHashes = generateKGramHashes(tokenizedString);
        fingerprint = fingerprint(kGramHashes);
    }

    /* fingerprint getter */
    public List<Integer> getFingerprint() {
        return fingerprint;
    }

    /* Compiles then decompiles a java file, overwriting the original file 
     * * Maybe the decompiler isnt perfect, but there are sometimes issues here. *
     */
    public static void compileAndDecompile(Path file) {
        final String fileName = file.toString();
        final String compiledFileName = fileName.substring(0, fileName.length() - 5) + ".class";
        final String decompiledFileName = "Decompiled_" + fileName;

        //run javac in command line
        try {
            Process p = Runtime.getRuntime().exec("javac " + fileName);
            System.out.println("Compiling...");
            p.waitFor();
        } catch (Exception e) {
            System.out.println("Compiling failed.");
            return;
        }

        Moss.decompile(decompiledFileName, compiledFileName);

    }

    // Compile all java files in a directory
    public static void compileAll(Path dir) throws Exception {
        Process p = Runtime.getRuntime().exec("javac " + dir.toString() + "*.java");
        p.waitFor();
    }

    // use procyon to decompile the file
    public static void decompile(String decompiledFileName, String compiledFileName) {
        final DecompilerSettings settings = DecompilerSettings.javaDefaults();

        try (final FileOutputStream stream = new FileOutputStream(decompiledFileName);
            final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            Decompiler.decompile(compiledFileName, new PlainTextOutput(writer), settings);
            writer.close();
        }  catch (Exception e) {
            System.out.println("Decompiling failed.");
        }
    }

    // Converts a file to a string stripped of whitespace
    public String tokenize(Path file) {
        try {
            return SimpleLexer.jLex(Files.newBufferedReader(file));
        } catch (Exception e) {
            System.out.println("Could not read file");
            return null;
        }
    }
}

