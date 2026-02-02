
import static java.util.stream.Collectors.*;

import java.util.stream.*;

import java.nio.file.*;
import java.nio.*;
import java.io.*;
import java.util.IntSummaryStatistics;

public class Nio {

    public static void main(String...args) throws IOException {
    
    
        System.out.println("\n----->first 5 java file names:");
        Files.list(Paths.get("."))
            .map(Path::getFileName) // still a path
            .map(Path::toString) // convert to Strings
            .filter(name -> name.endsWith(".java"))
            .sorted() // sort them alphabetically
            .limit(5) // first 5
            .forEach(System.out::println);
     }

    
}