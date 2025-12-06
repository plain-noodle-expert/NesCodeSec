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
						return 1;
					return 0;
				}
				if (c == '/' || c == '\\' || c == ':')
					break;
			}
			err.format("fut: ERROR: Don't know what language to translate to: no extension in '%s' and no '-l' option\n", outputFile);
			return 1;
		}
		catch (IOException e) {
			err.println("fut: ERROR: " + e.getMessage());
			return 1;
		}
		catch (IllegalStateException e) {
			err.println("fut: ERROR: " + e.getMessage());
			return 1;
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		List<String> argList = extractArgs(req);
		if (argList.isEmpty()) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.setContentType("application/json");
			resp.getWriter().write("{\"error\":\"No arguments supplied. Provide args or input/output parameters.\"}");
			return;
		}

		StringWriter outBuffer = new StringWriter();
		StringWriter errBuffer = new StringWriter();
		int status = run(argList, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		resp.setStatus(status == 0 ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
		resp.setContentType("application/json");
		resp.getWriter().write(buildJsonResponse(status, outBuffer.toString(), errBuffer.toString()));
	}

	private List<String> extractArgs(HttpServletRequest req)
	{
		String argsParam = req.getParameter("args");
		if (argsParam != null && !argsParam.trim().isEmpty()) {
			return Arrays.asList(argsParam.trim().split("\\s+"));
		}

		ArrayList<String> result = new ArrayList<String>();
		String outputFile = req.getParameter("output");
		if (outputFile != null && !outputFile.isEmpty()) {
			result.add("-o");
			result.add(outputFile);
		}

		String lang = req.getParameter("lang");
		if (lang != null && !lang.isEmpty()) {
			result.add("-l");
			result.add(lang);
		}

		String namespace = req.getParameter("namespace");
		if (namespace != null && !namespace.isEmpty()) {
			result.add("-n");
			result.add(namespace);
		}

		String referenced = req.getParameter("referenced");
		if (referenced != null && !referenced.isEmpty()) {
			for (String file : referenced.split(",")) {
				if (!file.trim().isEmpty()) {
					result.add("-r");
					result.add(file.trim());
				}
			}
		}

		String resources = req.getParameter("resourceDir");
		if (resources != null && !resources.isEmpty()) {
			for (String dir : resources.split(",")) {
				if (!dir.trim().isEmpty()) {
					result.add("-I");
					result.add(dir.trim());
				}
			}
		}

		String defs = req.getParameter("define");
		if (defs != null && !defs.isEmpty()) {
			for (String symbol : defs.split(",")) {
				if (!symbol.trim().isEmpty()) {
					result.add("-D");
					result.add(symbol.trim());
				}
			}
		}

		String inputs = req.getParameter("input");
		if (inputs != null && !inputs.isEmpty()) {
			for (String input : inputs.split(",")) {
				if (!input.trim().isEmpty())
					result.add(input.trim());
			}
		}

		return result;
	}

	private String buildJsonResponse(int status, String stdout, String stderr)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("{\"status\":");
		builder.append(status);
		builder.append(",\"stdout\":\"");
		builder.append(escapeJson(stdout));
		builder.append("\",\"stderr\":\"");
		builder.append(escapeJson(stderr));
		builder.append("\"}");
		return builder.toString();
	}

	private String escapeJson(String value)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case '\\':
				builder.append("\\\\");
				break;
			case '"':
				builder.append("\\\"");
				break;
			case '\n':
				builder.append("\\n");
				break;
			case '\r':
				builder.append("\\r");
				break;
			case '\t':
				builder.append("\\t");
				break;
			default:
				builder.append(c);
			}
		}
		return builder.toString();
	}

	public static void main(String[] args)
	{
		int status = run(Arrays.asList(args), new PrintWriter(System.out, true), new PrintWriter(System.err, true));
		if (status != 0)
			System.exit(status);
	}
}
