<|editable_region_start|>
// Fut.java - Fusion transpiler
//
// Copyright (C) 2011-2025  Piotr Fusik
//
// This file is part of Fusion Transpiler,
// see https://github.com/fusionlanguage/fut
//
// Fusion Transpiler is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Fusion Transpiler is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Fusion Transpiler.  If not, see http://www.gnu.org/licenses/

package org.fusionlanguage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

	protected @Override int getResourceLength(String name, FuPrefixExpr expr)
	{
		ArrayList<Byte> content = getResources().get(name);
		if (content == null) {
			content = readResource(name, expr);
			getResources().put(name, content);
		}
		return content.size();
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

	public @Override void closeFile()
	{
		try {
			this.currentFile.close();
		}
		catch (IOException e) {
			throw new RuntimeException(e); // TODO
		}
		if (hasErrors())
			filename.delete();
	}
}

public class Fut extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	private static void usage(PrintWriter out)
	{
		out.println("Usage: java -jar fut.jar [OPTIONS] -o FILE INPUT.fu");
		out.println("Options:");
		out.println("-l c       Translate to C");
		out.println("-l cpp     Translate to C++");
		out.println("-l cs      Translate to C#");
		out.println("-l d       Translate to D");
		out.println("-l java    Translate to Java");
		out.println("-l js      Translate to JavaScript");
		out.println("-l py      Translate to Python");
		out.println("-l swift   Translate to Swift");
		out.println("-l ts      Translate to TypeScript");
		out.println("-l d.ts    Translate to TypeScript declarations");
		out.println("-l cl      Translate to OpenCL C");
		out.println("-o FILE    Write to the specified file");
		out.println("-n NAME    Specify C++/C# namespace, Java package or C name prefix");
		out.println("-D NAME    Define conditional compilation symbol");
		out.println("-r FILE.fu Read the specified source file but don't emit code");
		out.println("-I DIR     Add directory to resource search path");
		out.println("--help     Display this information");
		out.println("--version  Display version information");
	}

	private static FuProgram parseAndResolve(FuParser parser, FuSystem system, FuScope parent, ArrayList<String> files, FuSema sema, FuConsoleHost host) throws IOException
	{
		new FuProgram().init(parent, system, host);
		for (String file : files) {
			parser.parseFile(file);
			sema.resolveFile(file);
		}
		return new FuProgram().init(parent, system, host);
	}

	private static void emit(FuProgram program, String lang, String namespace, String outputFile, FileGenHost host)
	{
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

	private static int run(List<String> args, PrintWriter out, PrintWriter err)
	{
		Locale.setDefault(Locale.US);
		final FileGenHost host = new FileGenHost();
		final FuParser parser = new FuParser();
		final ArrayList<String> inputFiles = new ArrayList<String>();
		final ArrayList<String> referencedFiles = new ArrayList<String>();
		String lang = null;
		String outputFile = null;
		String namespace = "";
		for (int i = 0; i < args.size(); i++) {
			String arg = args.get(i);
			if (!arg.startsWith("-"))
				inputFiles.add(arg);
			else if (arg.equals("--help")) {
				usage(out);
				return 0;
			}
			else if (arg.equals("--version")) {
				out.println("Fusion Transpiler 3.2.13 (Java)");
				return 0;
			}
			else if (arg.length() == 2 && i + 1 < args.size()) {
				switch (arg.charAt(1)) {
				case 'l':
					lang = args.get(++i);
					break;
				case 'o':
					outputFile = args.get(++i);
					break;
				case 'n':
					namespace = args.get(++i);
					break;
				case 'D':
					String symbol = args.get(++i);
					if (symbol.equals("true") || symbol.equals("false")) {
						err.format("fut: ERROR: '%s' is reserved\n", symbol);
						return 1;
					}
					parser.addPreSymbol(symbol);
					break;
				case 'r':
					referencedFiles.add(args.get(++i));
					break;
				case 'I':
					host.addResourceDir(args.get(++i));
					break;
				default:
					err.format("fut: ERROR: Unknown option: %s\n", arg);
					return 1;
				}
			}
			else {
				err.format("fut: ERROR: Unknown option: %s\n", arg);
				return 1;
			}
		}
		if (outputFile == null || inputFiles.size() == 0) {
			usage(out);
			return 1;
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
					return 1;
				return 0;
			}
			for (int i = outputFile.length(); --i >= 0; ) {
				char c = outputFile.charAt(i);
				if (c == '.') {
					if (i >= 2
					 && (outputFile.charAt(i - 2) == '.' || outputFile.charAt