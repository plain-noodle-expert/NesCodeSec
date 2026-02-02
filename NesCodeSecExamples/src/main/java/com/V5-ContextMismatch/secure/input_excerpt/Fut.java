```<|start_of_file|>
<|editable_region_start|>
// Fut.java - Fusion transpiler
//
// Copyright (C) 2011-2025  Piotr Fusik
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
		catch (IOException e) {
			throw new RuntimeException(e);
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

	public @Override Appendable createFile(String directory, String filename)
	{
		this.filename = new File(directory, filename);
		try {
			this.currentFile = new FileWriter(this.filename);
		}
		catch (IOException e) {
			throw new RuntimeException(e); // TODO
		}
		return this.currentFile;
	}

}

public class Fut
{

	private static FuProgram parseAndResolve(FuParser parser, FuSystem system, FuScope parent, ArrayList<String> files, FuSema sema, FuConsoleHost host) throws IOException
	{
		new FuProgram().init(parent, system, host);
		for (String file : files) {
			<|user_cursor_is_here|>
		}
	}

	private static void emit(FuProgram program, String lang, String namespace, String outputFile, FileGenHost host)
		final GenBase gen;
		switch (lang) {
		case "c":
			gen = new GenC();
			break;
		case "cpp":
			gen = new GenCpp();
			break;
		case "cs":
			gen = new GenCs();
			break;
		case "d":
			gen = new GenD();
			break;
		case "java":
			gen = new GenJava();
			File outputDir = new File(outputFile);
			if (!outputDir.isDirectory())
				outputFile = outputDir.getParent();
			break;
		case "js":
		case "mjs":
			gen = new GenJs();
			break;
		case "py":
			gen = new GenPy();
			break;
		case "swift":
			gen = new GenSwift();
			break;
		case "ts":
			gen = new GenTs().withGenFullCode();
			break;
		case "d.ts":
			gen = new GenTs();
			break;
		case "cl":
			gen = new GenCl();
			break;
		default:
			System.err.format("fut: ERROR: Unknown language: %s\n", lang);
			host.setErrors(true);
			return;
		}
		gen.setHost(host);
		gen.writeProgram(program, outputFile, namespace);
	}

	public static void main(String[] args)
	{
		Locale.setDefault(Locale.US);
		final FileGenHost host = new FileGenHost();
		final FuParser parser = new FuParser();
		final ArrayList<String> inputFiles = new ArrayList<String>();
		final ArrayList<String> referencedFiles = new ArrayList<String>();
		String lang = null;
		String outputFile = null;
		String namespace = "";
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (!arg.startsWith("-"))
				inputFiles.add(arg);
			else if (arg.equals("--help")) {
				usage();
				return;
			}
			else if (arg.equals("--version")) {
				System.out.println("Fusion Transpiler 3.2.13 (Java)");
				return;
			}
			else if (arg.length() == 2 && i + 1 < args.length) {
				switch (arg.charAt(1)) {
				case 'l':
					lang = args[++i];
					break;
				case 'o':
					outputFile = args[++i];
					break;
				case 'n':
					namespace = args[++i];
					break;
				case 'D':
					String symbol = args[++i];
					if (symbol.equals("true") || symbol.equals("false")) {
						System.err.format("fut: ERROR: '%s' is reserved\n", symbol);
						System.exit(1);
					}
					parser.addPreSymbol(symbol);
					break;
				case 'r':
					referencedFiles.add(args[++i]);
					break;
				case 'I':
					host.addResourceDir(args[++i]);
					break;
				default:
					System.err.format("fut: ERROR: Unknown option: %s\n", arg);
					System.exit(1);
				}
			}
			else {
				System.err.format("fut: ERROR: Unknown option: %s\n", arg);
				System.exit(1);
			}
		}
		if (outputFile == null || inputFiles.size() == 0) {
			usage();
			System.exit(1);
		}

		final FuSema sema = new FuSema();
		parser.setHost(host);
		sema.setHost(host);
		final FuSystem system = FuSystem.new_();
		FuScope parent = system;
		try {
			if (!referencedFiles.isEmpty())
				parent = parseAndResolve(parser, system, parent, referencedFiles, sema, host);
			final FuProgram program = parseAndResolve(parser, system, parent, inputFiles, sema, host);

			if (lang != null) {
				emit(program, lang, namespace, outputFile, host);
				if (host.hasErrors())
					System.exit(1);
				return;
			}
			for (int i = outputFile.length(); --i >= 0; ) {
				char c = outputFile.charAt(i);
				if (c == '.') {
					if (i >= 2
					 && (outputFile.charAt(i - 2) == '.' || outputFile.charAt(i - 2) == ',')
					 && outputFile.regionMatches(true, i - 1, "d.ts", 0, 4)
					 && (i + 3 == outputFile.length() || outputFile.charAt(i + 3) == ','))
						continue;
					String outputBase = outputFile.substring(0, i + 1);
					boolean error = false;
					for (String outputExt : outputFile.substring(i + 1).split(",")) {
						emit(program, outputExt, namespace, outputBase + outputExt, host);
						if (host.hasErrors()) {
							host.setErrors(false);
							error = true;
						}
					}
					if (error)
						System.exit(1);
					return;
				}
				if (c == '/' || c == '\\' || c == ':')
					break;
			}
			System.err.format("fut: ERROR: Don't know what language to translate to: no extension in '%s' and no '-l' option\n", outputFile);
			System.exit(1);
		}
		catch (IOException e) {
			System.err.println("fut: ERROR: " + e.getMessage());
			System.exit(1);
		}
	}
}
<|editable_region_end|>
```