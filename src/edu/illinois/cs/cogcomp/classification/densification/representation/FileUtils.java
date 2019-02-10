package edu.illinois.cs.cogcomp.classification.densification.representation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yangqiu Song
 * @email yqsong@illinois.edu
 */
public class FileUtils {
	public static String systemNewLine = System.getProperty("line.separator");

	public static String ReadWholeFile (String filePath) {
		String content = "";
		try {
			FileReader reader = new FileReader(filePath);
			BufferedReader br = new BufferedReader(reader);
			
			String line = "";
			while ((line = br.readLine()) != null) {
				content += line + systemNewLine;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}
	
	public static List<String> ReadWholeFileAsLines (String filePath) {
		List<String> content = new ArrayList<String>();
		try {
			FileReader reader = new FileReader(filePath);
			BufferedReader br = new BufferedReader(reader);
			
			String line = "";
			while ((line = br.readLine()) != null) {
				content.add(line);
			}
			br.close();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}

}
