import com.strobel.decompiler.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

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
        final String compiledFileName =
            fileName.substring(0, fileName.length() - 5) + ".class";
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
        Process p = Runtime.getRuntime().exec(
            "javac " + dir.toString() + "*.java"
        );
        p.waitFor();
    }

    // use procyon to decompile the file
    public static void decompile(
        String decompiledFileName,
        String compiledFileName
    ) {
        try (
            final FileOutputStream stream = new FileOutputStream(
                decompiledFileName
            )
        ) {
            Process p = Runtime.getRuntime().exec(
                "java -jar procyon-decompiler.jar " + compiledFileName
            );
            p.waitFor();
            Files.copy(p.getInputStream(), stream);
        } catch (Exception e) {
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
