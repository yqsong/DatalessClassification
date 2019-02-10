package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.yahoo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import edu.illinois.cs.cogcomp.classification.hierarchy.run.ConceptClassificationGivenText;

public class YahooTopicHierarchy {
	static public HashMap<String, HashMap<String, String>> topicHierarchy;
	static public HashMap<String, String> topicMapping1 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping2 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping3 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping4 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping5 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping6 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping7 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping8 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping9 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping10 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping11 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping12 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping13 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping14 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping15 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping16 = new HashMap<String, String>();

	public YahooTopicHierarchy () {
		String file="data/yahooDir.txt";
		String line;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				if(line.contains("Arts & Humanities")) topicMapping1.put(line, parts[1]);
				if(line.contains("Business & Economy")) topicMapping2.put(line, parts[1]);
				if(line.contains("Computer & Internet")) topicMapping3.put(line, parts[1]);
				if(line.contains("Education")) topicMapping4.put(line, parts[1]);
				if(line.contains("Entertainment")) topicMapping5.put(line, parts[1]);
				if(line.contains("Government")) topicMapping6.put(line, parts[1]);
				if(line.contains("Health")) topicMapping7.put(line, parts[1]);
				if(line.contains("News & Media")) topicMapping8.put(line, parts[1]);
				if(line.contains("Recreation & Sports")) topicMapping9.put(line, parts[1]);
				if(line.contains("Reference")) topicMapping10.put(line, parts[1]);
				if(line.contains("Regional")) topicMapping11.put(line, parts[1]);
				if(line.contains("Science")) topicMapping12.put(line, parts[1]);
				if(line.contains("Social Science")) topicMapping13.put(line, parts[1]);
				if(line.contains("Society & Culture")) topicMapping14.put(line, parts[1]);
				if(line.contains("Regions")) topicMapping15.put(line, parts[1]);
				if(line.contains("Sports")) topicMapping16.put(line, parts[1]);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		topicHierarchy = new HashMap<String, HashMap<String, String>>();
		topicHierarchy.put("Arts & Humanities", topicMapping1);
		topicHierarchy.put("Business & Economy", topicMapping2);
		topicHierarchy.put("Computer & Internet", topicMapping3);
		topicHierarchy.put("Education", topicMapping4);
		topicHierarchy.put("Entertainment", topicMapping5);
		topicHierarchy.put("Government", topicMapping6);
		topicHierarchy.put("Health", topicMapping7);
		topicHierarchy.put("News & Media", topicMapping8);
		topicHierarchy.put("Recreation & Sports", topicMapping9);
		topicHierarchy.put("Reference", topicMapping10);
		topicHierarchy.put("Regional", topicMapping11);
		topicHierarchy.put("Science", topicMapping12);
		topicHierarchy.put("Social Science", topicMapping13);
		topicHierarchy.put("Society & Culture", topicMapping14);
		topicHierarchy.put("Regions", topicMapping15);
		topicHierarchy.put("Sports", topicMapping16);
		
		
	}
	
	public HashMap<String, HashMap<String, String>> getTopicHierarchy () {
		return topicHierarchy;
	}

}
