package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.searchbased;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts.AbstractTopicDocMaps;

/**
 * yqsong@illinois.edu
 */

public class SearchedTopicDocMaps extends AbstractTopicDocMaps {

	@Override
	public void readTopicDocMap(String file) {
		try {
	     	FileReader reader = new FileReader(file);
	     	BufferedReader bf = new BufferedReader(reader);
	     	String line = "";
	     	int count = 0;
	     	while ((line = bf.readLine()) != null) {
	     		if (count % 1000 == 0) {
	     			System.out.println("Read doc num: " + count + " lines");
	     		}
	     		
	     		String[] tokens = line.split("\t");
	     		if (tokens.length != 3) {
	     			continue;
	     		}
	     		
	     		String docID = tokens[0].trim();
	     		String label = tokens[1].trim();
	     		String docContent = tokens[2].trim();
	     		
	     		count++;
	     		
	     		if (topicDocMap.containsKey(label) == true) {
    				if (topicDocMap.get(label).contains(docID) == false) {
    					topicDocMap.get(label).add(docID);
    				}
				} else {
					topicDocMap.put(label, new HashSet<String>());
					topicDocMap.get(label).add(docID);
				}
    		
				if (docTopicMap.containsKey(docID) == true) {
					if (docTopicMap.get(docID).contains(label) == false) {
						docTopicMap.get(docID).add(label);
					}
				} else {
					docTopicMap.put(docID, new HashSet<String>());
					docTopicMap.get(docID).add(label);
				}
	     	}
	     	bf.close();
	     	reader.close();
	     	
		} catch (Exception e ) 
		{
			e.printStackTrace();
		};

	}

	@Override
	public void readFilteredTopicDocMap(String file, Set<String> docIDSet) {
		try {
	     	FileReader reader = new FileReader(file);
	     	BufferedReader bf = new BufferedReader(reader);
	     	String line = "";
	     	int count = 0;
	     	while ((line = bf.readLine()) != null) {
	     		if (count % 1000 == 0) {
	     			System.out.println("Read doc num: " + count + " lines");
	     		}
	     		
	     		String[] tokens = line.split("\t");
	     		if (tokens.length != 3) {
	     			continue;
	     		}
	     		
	     		String docID = tokens[0].trim();
	     		String label = tokens[1].trim();
	     		String docContent = tokens[2].trim();
	     		
	     		count++;
	     		
	     		if (docIDSet.contains(docID) == true) {
		     		if (topicDocMap.containsKey(label) == true) {
	    				if (topicDocMap.get(label).contains(docID) == false) {
	    					topicDocMap.get(label).add(docID);
	    				}
					} else {
						topicDocMap.put(label, new HashSet<String>());
						topicDocMap.get(label).add(docID);
					}
	    		
					if (docTopicMap.containsKey(docID) == true) {
						if (docTopicMap.get(docID).contains(label) == false) {
							docTopicMap.get(docID).add(label);
						}
					} else {
						docTopicMap.put(docID, new HashSet<String>());
						docTopicMap.get(docID).add(label);
					}
	     		}
	     	}
	     	bf.close();
	     	reader.close();
	     	
		} catch (Exception e ) 
		{
			e.printStackTrace();
		};


	}

}
