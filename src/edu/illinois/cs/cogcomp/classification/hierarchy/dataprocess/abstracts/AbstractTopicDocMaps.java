package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * yqsong@illinois.edu
 */

public abstract class AbstractTopicDocMaps {
	protected HashMap<String, HashSet<String>> topicDocMap = null;
	protected HashMap<String, HashSet<String>> docTopicMap = null;
	
	public AbstractTopicDocMaps () {
		topicDocMap = new HashMap<String, HashSet<String>>();
		docTopicMap = new HashMap<String, HashSet<String>>();
	}
	
	public HashSet<String> getDocInTopic (String topic) {
		return topicDocMap.get(topic);
	}
	
	public HashSet<String> getTopicOfDoc (String doc) {
		return docTopicMap.get(doc);
	}
	
	public HashMap<String, HashSet<String>> getTopicDocMap () {
		return topicDocMap;
	}
	
	public HashMap<String, HashSet<String>> getDocTopicMap () {
		return docTopicMap;
	}
	
	abstract public void readTopicDocMap (String file);
	
	abstract public void readFilteredTopicDocMap (String file, Set<String> docIDSet);
}
