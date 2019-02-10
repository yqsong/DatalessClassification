package edu.illinois.cs.cogcomp.classification.hierarchy.dataless.sample;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import cern.colt.map.OpenIntDoubleHashMap;
import cern.colt.map.OpenIntIntHashMap;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.AbstractTreeNode;

/**
 * yqsong@illinois.edu
 */

public class TopicTreeNode extends AbstractTreeNode implements Serializable {

	private static final long serialVersionUID = 1882366614731080784L;

	private HashSet<TopicTreeNode> children;
	
	private OpenIntIntHashMap[] topicWordCountArray;
	private OpenIntDoubleHashMap categoryWordDistribution;
	private int sumCount = 0;
	
	private HashMap<String, int[]> docWordSeqMap;
	private HashMap<String, int[]> docTopicSeqMap;
	private HashMap<String, OpenIntIntHashMap> docTopicCountMap;
	
	public TopicTreeNode(
			HashSet<TopicTreeNode> children,
			String labelStr,
			int treedepth,
			int topicNumPerNode, HashMap<String, int[]> wordSeqMap, 
			HashMap<String, int[]> docTopicSeqMap, 
			HashMap<String, OpenIntIntHashMap> topicCountMap
			) {
		this.children = children;
		this.labelString = labelStr;
		this.depth = treedepth;

		if (topicNumPerNode < 1) {
			topicNumPerNode = 1;
		}
		this.topicWordCountArray = new OpenIntIntHashMap[topicNumPerNode];
		for (int i = 0; i < topicNumPerNode; ++i) {
			this.topicWordCountArray[i] = new OpenIntIntHashMap();
		}
		this.docWordSeqMap = wordSeqMap;
		this.docTopicSeqMap = docTopicSeqMap;
		this.docTopicCountMap = topicCountMap;
	}
	
	public OpenIntIntHashMap[] getTopicWordCountArray () {
		return this.topicWordCountArray;
	}
	
	public OpenIntDoubleHashMap getCategoryWordDistribution () {
		return this.categoryWordDistribution;
	}
	
	public void setCategoryWordDistribution (OpenIntDoubleHashMap categoryWordDistribution) {
		this.categoryWordDistribution = categoryWordDistribution;
	}
	
	public int getSumCount () {
		return sumCount;
	}
	
	public void setSumCount (int sumCount) {
		this.sumCount = sumCount;
	}
	
	public HashMap<String, int[]> getDocWordSeq () {
		return this.docWordSeqMap;
	}
	
	public HashMap<String, int[]> getDocTopicSeq () {
		return this.docTopicSeqMap;
	}
	
	public HashMap<String, OpenIntIntHashMap> getDocTopicCountMap () {
		return this.docTopicCountMap;
	}
	
	public HashSet<TopicTreeNode> getChildren () {
		return this.children;
	}

}
