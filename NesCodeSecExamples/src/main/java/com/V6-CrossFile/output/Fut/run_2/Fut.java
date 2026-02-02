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


package org.fusionlanguage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;

class FileGenHost extends FuConsoleHost
{
	private final ArrayList<String> resourceDirs = new ArrayList<String>();
	private File filename;
	private FileWriter currentFile;

	public void addResourceDir(String path)
	{
		this.resourceDirs.add(path);
	}

	private ArrayList<Byte> readAllBytes(File path)
	{
		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(path))) {
			for (final ArrayList<Byte> result = new ArrayList<Byte>();;) {
				int b = in.read();
				if (b < 0)
					return result;
				result.add((byte) b);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<Byte>();
		}
	}

	private ArrayList<Byte> readResource(String name, FuPrefixExpr expr)
	{
		for (String dir : this.resourceDirs) {
			File path = new File(dir, name);
			if (path.isFile())
				return readAllBytes(path);
		}
		File path = new File(name);
		if (path.isFile())
			return readAllBytes(path);
		reportStatementError(expr, String.format("File %s not found", name));
		return new ArrayList<Byte>();
	}
}

public class Fut
{

	private static FuProgram parseAndResolve(FuParser parser, FuSystem system, FuScope parent, ArrayList<String> files, FuSema sema, FuConsoleHost host) throws IOException
	{
		new FuProgram().init(parent, system, host);
		for (String file : files) {
			File path = new File(file);
			byte[] input = Files.readAllBytes(Paths.get(file));
			parser.parse(file, input, input.length);
		}
		if (host.hasErrors())
			System.exit(1);
		sema.process();
		if (host.hasErrors())
			System.exit(1);
		return host.program;
	}
}

