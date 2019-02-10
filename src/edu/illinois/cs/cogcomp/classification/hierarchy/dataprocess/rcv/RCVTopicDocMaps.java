package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts.AbstractTopicDocMaps;

/**
 * yqsong@illinois.edu
 */

public class RCVTopicDocMaps extends AbstractTopicDocMaps {

	
	public static void main(String[] args) {
		RCVTopicDocMaps rcvTDM = new RCVTopicDocMaps();
		rcvTDM.readTopicDocMap ("data/rcvTest/rcv1-v2.topics.qrels");
		HashMap<String, HashSet<String>> topicDocMap = rcvTDM.getTopicDocMap();
	}
	
	public RCVTopicDocMaps () {
		super();
	}
	
	public void readTopicDocMap (String file) {
		try {
			FileReader reader = new FileReader(file);
			BufferedReader bf = new BufferedReader(reader);
			String line = "";
			while ((line = bf.readLine()) != null) {
				String[] splitArray = line.trim().split(" ");
				String topic = splitArray[0].trim().toLowerCase();
				String docID = splitArray[1].trim();
				if (topicDocMap.containsKey(topic) == true) {
    				if (topicDocMap.get(topic).contains(docID) == false) {
    					topicDocMap.get(topic).add(docID);
    				}
				} else {
					topicDocMap.put(topic, new HashSet<String>());
					topicDocMap.get(topic).add(docID);
				}
    		
				if (docTopicMap.containsKey(docID) == true) {
					if (docTopicMap.get(docID).contains(topic) == false) {
						docTopicMap.get(docID).add(topic);
					}
				} else {
					docTopicMap.put(docID, new HashSet<String>());
					docTopicMap.get(docID).add(topic);
				}
			}
			bf.close();
			reader.close();
    	
		} catch (Exception e ) 
		{
			e.printStackTrace();
		}
	}
	
	public void readFilteredTopicDocMap (String file, Set<String> docIDSet) {
		try {
			FileReader reader = new FileReader(file);
			BufferedReader bf = new BufferedReader(reader);
			String line = "";
			while ((line = bf.readLine()) != null) {
				String[] splitArray = line.trim().split(" ");
				String topic = splitArray[0].trim().toLowerCase();
				String docID = splitArray[1].trim();
				if (docIDSet.contains(docID) == true) {
					if (topicDocMap.containsKey(topic) == true) {
	    				if (topicDocMap.get(topic).contains(docID) == false) {
	    					topicDocMap.get(topic).add(docID);
	    				}
					} else {
						topicDocMap.put(topic, new HashSet<String>());
						topicDocMap.get(topic).add(docID);
					}
	    		
					if (docTopicMap.containsKey(docID) == true) {
						if (docTopicMap.get(docID).contains(topic) == false) {
							docTopicMap.get(docID).add(topic);
						}
					} else {
						docTopicMap.put(docID, new HashSet<String>());
						docTopicMap.get(docID).add(topic);
					}
				}
			}
			bf.close();
			reader.close();
    	
		} catch (Exception e ) 
		{
			e.printStackTrace();
		}
	}
	

}
