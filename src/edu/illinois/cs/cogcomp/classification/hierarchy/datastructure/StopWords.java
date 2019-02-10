package edu.illinois.cs.cogcomp.classification.hierarchy.datastructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;

/**
 * yqsong@illinois.edu
 */

public class StopWords {
	public static HashSet<String> rcvStopWords;
	
	public static HashSet<String> readStopWords (String filePath) {
		
		File file = new File(filePath);
		rcvStopWords = new HashSet<String>();
		if (file.exists()) {
			try {
				FileReader reader = new FileReader(filePath);
		     	BufferedReader bf = new BufferedReader(reader);
		     	String line = "";
		     	int count = 0;
		     	while ((line = bf.readLine()) != null) {
		     		if (line.equals("") == true)
		     			continue;
		     		count++;
		     		rcvStopWords.add(line.trim().toLowerCase());
		     	}
		     	bf.close();
		     	reader.close();
		     	
			} catch (Exception e ) 
			{
				e.printStackTrace();
			};
		}

		return rcvStopWords;
	}
}
