import java.util.*;
import java.io.*;
import java.nio.file.*;

public class rpn {

	public ArrayList<equation> equations;

	public rpn(String[] args) {
		equations = new ArrayList<equation>();

		try {
			readFile(args[0]);
		}
		catch (Exception e) {
			System.out.println("Error opening file!");
			e.printStackTrace();
		}

		for (int i = 0; i < equations.size(); i++) {
			System.out.println("Solution to equation [" + i + "]: " + equations.get(i).convertToRPN());
		}

	}

	public void readFile(String filePath) throws Exception {
		Path file = <|user_cursor_is_here|>
	   	try (InputStream in = Files.newInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			    String line = "";
			    while ((line = reader.readLine()) != null) {
			        equation curEquation = new equation(line);
			        equations.add(curEquation);
			    }
			} 
	}

}