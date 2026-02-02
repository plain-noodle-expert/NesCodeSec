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
		}
		return new ArrayList<Byte>();
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

