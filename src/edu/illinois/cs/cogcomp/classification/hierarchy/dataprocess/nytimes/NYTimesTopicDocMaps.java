package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.nytimes;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts.AbstractTopicDocMaps;

/**
 * yqsong@illinois.edu
 */

public class NYTimesTopicDocMaps extends AbstractTopicDocMaps {
	
	public NYTimesTopicDocMaps () {
		super();
	}
	
	@Override
	public void readTopicDocMap(String file) {
		String taxonomy = "";
		try {
			Directory inputDir = FSDirectory.open(new File(file));
			IndexReader reader = IndexReader.open(inputDir, true);
			int maxDocNum = reader.maxDoc();
			for (int i = 0; i < maxDocNum; ++i) {
				if (reader.isDeleted(i) == false) {
					if (i % 10000 == 0) {
						System.out.println ("[Read NYTimes Taxonomy for TopicMap: ] " + i + "docs ..");
					}
					Document doc = reader.document(i);
					taxonomy = doc.get("Taxonomic Classifiers");
					String docID = doc.get("Url");
					if (taxonomy == null || docID == null) 
						continue;
					String taxonomyLower = taxonomy.toLowerCase();
					if (taxonomyLower.contains("classifieds"))
						continue;
					
					String[] tokens = taxonomyLower.trim().replace("||", "\t").split("\t");
					for (int j = 0; j < tokens.length; ++j) {
						String taxonomyJ = tokens[j].trim();
						String[] taxonomyTokens = taxonomyJ.split("/");
						for (int k = 0; k < taxonomyTokens.length; ++k) {
							String topic = taxonomyTokens[k];
							
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

				}
			}
			reader.close();
		} catch (Exception e) {
			System.out.println(taxonomy);
			e.printStackTrace();
		}
	}

	@Override
	public void readFilteredTopicDocMap(String file, Set<String> docIDSet) {
		String taxonomy = "";
		try {
			Directory inputDir = FSDirectory.open(new File(file));
			IndexReader reader = IndexReader.open(inputDir, true);
			int maxDocNum = reader.maxDoc();
			for (int i = 0; i < maxDocNum; ++i) {
				if (reader.isDeleted(i) == false) {
					if (i % 10000 == 0) {
						System.out.println ("[Read NYTimes Taxonomy for TopicMap: ] " + i + "docs ..");
					}
					Document doc = reader.document(i);
					taxonomy = doc.get("Taxonomic Classifiers");
					String docID = doc.get("Url");
					if (taxonomy == null || docID == null || docIDSet.contains(docID) == false) 
						continue;
					String taxonomyLower = taxonomy.toLowerCase();
					
					String[] tokens = taxonomyLower.trim().replace("||", "\t").split("\t");
					for (int j = 0; j < tokens.length; ++j) {
						String taxonomyJ = tokens[j].trim();
						String[] taxonomyTokens = taxonomyJ.split("/");
						for (int k = 0; k < taxonomyTokens.length; ++k) {
							String topic = taxonomyTokens[k];
							
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

				}
			}
			reader.close();
		} catch (Exception e) {
			System.out.println(taxonomy);
			e.printStackTrace();
		}
	}

}
