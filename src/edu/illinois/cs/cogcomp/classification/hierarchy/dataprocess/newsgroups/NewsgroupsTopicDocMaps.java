package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups;

import java.io.File;
import java.util.HashMap;
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

public class NewsgroupsTopicDocMaps extends AbstractTopicDocMaps {
	
	public NewsgroupsTopicDocMaps () {
		super();
	}
	
	@Override
	public void readTopicDocMap(String file) {

		NewsgroupsTreeLabelData treeLabelData = new NewsgroupsTreeLabelData();
		treeLabelData.readTreeHierarchy("");
		HashMap<String, String> parentIndex = treeLabelData.getTreeParentIndex();
		try {
			Directory inputDir = FSDirectory.open(new File(file));
			IndexReader reader = IndexReader.open(inputDir, true);
			int maxDocNum = reader.maxDoc();
			for (int i = 0; i < maxDocNum; ++i) {
				if (reader.isDeleted(i) == false) { 
					if (i % 1000 == 0) {
						System.out.println ("[Read NYTimes Data for TopicMap: ] " + i + "docs ..");
					}
					Document doc = reader.document(i);
					String docID = doc.get("uri");
					String topic = doc.get("newsgroup"); //doc.get("Body");
					if (docID == null || topic == null) {
						 continue;
					}
					String superTopic = parentIndex.get(topic);
					
					if (topicDocMap.containsKey(superTopic) == true) {
	    				if (topicDocMap.get(superTopic).contains(docID) == false) {
	    					topicDocMap.get(superTopic).add(docID);
	    				}
					} else {
						topicDocMap.put(superTopic, new HashSet<String>());
						topicDocMap.get(superTopic).add(docID);
					}
	    		
					if (docTopicMap.containsKey(docID) == true) {
						if (docTopicMap.get(docID).contains(superTopic) == false) {
							docTopicMap.get(docID).add(superTopic);
						}
					} else {
						docTopicMap.put(docID, new HashSet<String>());
						docTopicMap.get(docID).add(superTopic);
					}
					
					
					
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
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	@Override
	public void readFilteredTopicDocMap(String file, Set<String> docIDSet) {
		NewsgroupsTreeLabelData treeLabelData = new NewsgroupsTreeLabelData();
		treeLabelData.readTreeHierarchy("");
		HashMap<String, String> parentIndex = treeLabelData.getTreeParentIndex();

		try {
			Directory inputDir = FSDirectory.open(new File(file));
			IndexReader reader = IndexReader.open(inputDir, true);
			int maxDocNum = reader.maxDoc();
			for (int i = 0; i < maxDocNum; ++i) {
				if (reader.isDeleted(i) == false) { 
					if (i % 1000 == 0) {
						System.out.println ("[Read 20newsgroups Data for TopicMap: ] " + i + "docs ..");
					}
					Document doc = reader.document(i);
					String docID = doc.get("uri");
					String topic = doc.get("newsgroup"); //doc.get("Body");
					if (docID == null || topic == null) {
						 continue;
					}
					if (docIDSet.contains(docID) == false)
						continue;
					
					String superTopic = parentIndex.get(topic);
					
					if (topic == null || superTopic == null) {
						int a = 0;
					}
					
					if (topicDocMap.containsKey(superTopic) == true) {
	    				if (topicDocMap.get(superTopic).contains(docID) == false) {
	    					topicDocMap.get(superTopic).add(docID);
	    				}
					} else {
						topicDocMap.put(superTopic, new HashSet<String>());
						topicDocMap.get(superTopic).add(docID);
					}
	    		
					if (docTopicMap.containsKey(docID) == true) {
						if (docTopicMap.get(docID).contains(superTopic) == false) {
							docTopicMap.get(docID).add(superTopic);
						}
					} else {
						docTopicMap.put(docID, new HashSet<String>());
						docTopicMap.get(docID).add(superTopic);
					}
					
					
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
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
