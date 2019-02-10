package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.yahoo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import edu.illinois.cs.cogcomp.classification.hierarchy.run.ConceptClassificationGivenText;

public class WikiCateTopicHierarchy{
	static public HashMap<String, HashMap<String, String>> topicHierarchy;
	static public HashMap<String, String> topicMapping1 = new HashMap<String, String>();


	public WikiCateTopicHierarchy () {
		String file="/shared/bronte/sling3/data/cateInfo.txt";
		String line;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				topicMapping1.put(line.replaceAll("\\d+.*", "").trim(), line.replaceAll("\\d+.*", "").trim());
			}	
				
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		topicHierarchy = new HashMap<String, HashMap<String, String>>();
		topicHierarchy.put("labels", topicMapping1);

		
		
	}


	public HashMap<String, HashMap<String, String>> getTopicHierarchy () {
		return topicHierarchy;
	}
	

}
