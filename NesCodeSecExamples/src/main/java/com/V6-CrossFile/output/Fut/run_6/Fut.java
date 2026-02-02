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

