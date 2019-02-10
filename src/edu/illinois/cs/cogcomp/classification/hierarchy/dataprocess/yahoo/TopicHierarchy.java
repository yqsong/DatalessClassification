package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.yahoo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class TopicHierarchy {
	static public HashMap<String, HashMap<String, String>> topicHierarchy;
	static public HashMap<String, String> topicMapping1 = new HashMap<String, String>();


	public TopicHierarchy () {
		String file="data/temp.txt";
		String line;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				String[] parts=line.split(",");
				for(int i=0;i<parts.length;i++){
					//System.out.print(parts[i]+"\n");
					topicMapping1.put(parts[i], parts[i]);
				}
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
	
	public static void main (String[] args) throws Exception {
		//YahooDataESA (500);
		//WikiCateESA(500);
		new TopicHierarchy();
	}

}
