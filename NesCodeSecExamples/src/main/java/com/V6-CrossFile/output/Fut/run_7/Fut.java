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

