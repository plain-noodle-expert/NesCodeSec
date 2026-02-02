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

        try (final FileOutputStream stream = <|user_cursor_is_here|>
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