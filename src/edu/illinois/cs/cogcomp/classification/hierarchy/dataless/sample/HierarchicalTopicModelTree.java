package edu.illinois.cs.cogcomp.classification.hierarchy.dataless.sample;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntDoubleHashMap;
import cern.colt.map.OpenIntIntHashMap;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.AbstractLabelTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelContentClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts.AbstractTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts.AbstractTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.nytimes.NYTimesTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.nytimes.NYTimesTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultML;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.MinHeapByPQ;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.StopWords;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.RandomOperations;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;

/**
 * yqsong@illinois.edu
 */

public class HierarchicalTopicModelTree extends AbstractLabelTree implements Serializable, InterfaceMultiLabelContentClassificationTree{
	

	private static final long serialVersionUID = 4171780286014401161L;

	double classifierMLThreshold = 0.5;
	int leastK = ClassifierConstant.leastK;

	protected AbstractTopicDocMaps topicDocMapData;
	
	protected double globalAlpha = 0.1;
	protected double globalBeta = 0.05;
	
	protected TopicTreeNode root;
	protected HashMap<String, TopicTreeNode> topicNodeMap;

	protected HashMap<String, String> corpusStringMap;
	protected HashMap<String, int[]> corpusWordSequenceMap;
	protected HashMap<String, int[]> corpusTopicSequenceMap;
	protected HashMap<String, OpenIntIntHashMap> corpusTopicCountMap;
	
	protected HashMap<String, Integer> globalWordDict;
	protected HashMap<Integer, String> inverseGlobalWordDict;
	protected int globalWordNum = 0;
	
	protected HashMap<String, Integer> globalTopicIDMap;
	protected HashMap<Integer, String> inverseGlobalTopicIDMap;
	protected int globalTopicNum = 0;

	
	protected int topicPerNode = 1;
	
	protected int maxIterTopicModel = 10;
	
	public HierarchicalTopicModelTree(String data) 
	{
		String stopWordsFile = "";
		stopWordsFile = "data/rcvTest/english.stop";
		StopWords.rcvStopWords = StopWords.readStopWords (stopWordsFile);

		if (data.equals(DatalessResourcesConfig.CONST_DATA_RCV)) {
			treeLabelData = new RCVTreeLabelData();
			topicDocMapData = new RCVTopicDocMaps();
		}
		if (data.equals(DatalessResourcesConfig.CONST_DATA_NYTIMES)) {
			treeLabelData = new NYTimesTreeLabelData();
			topicDocMapData = new NYTimesTopicDocMaps();
		}
		if (data.equals(DatalessResourcesConfig.CONST_DATA_20NG)) {
			treeLabelData = new NewsgroupsTreeLabelData();
			topicDocMapData = new NewsgroupsTopicDocMaps();
		}

		topicNodeMap = new HashMap<String, TopicTreeNode>();
		corpusStringMap = new HashMap<String, String>();
		corpusWordSequenceMap = new HashMap<String, int[]>();
		corpusTopicSequenceMap = new HashMap<String, int[]>();
		corpusTopicCountMap = new HashMap<String, OpenIntIntHashMap>();

		globalWordDict = new HashMap<String, Integer>();
		inverseGlobalWordDict = new HashMap<Integer, String>();
		
		globalTopicIDMap = new HashMap<String, Integer>();
		inverseGlobalTopicIDMap = new HashMap<Integer, String>();
	}
	
	@Override
	public int getLabelDepth (String label) {
		TopicTreeNode node = getLabelDepth (label, root);
		if (node != null) {
			return node.getDepth();
		}
		return 0;
	}

	public TopicTreeNode getLabelDepth (String label, TopicTreeNode rootNode) {
		if (rootNode.getLabelString().equalsIgnoreCase(label.trim()) == true) {
			return rootNode;
		} else {
			for (TopicTreeNode child : rootNode.getChildren()) {
				TopicTreeNode node = getLabelDepth (label, child);
				if (node != null) {
					return node;
				}
			}
		}
		
		return null;
	}
	
	public void training (int maxIter) {
		maxIterTopicModel = maxIter;
		System.out.println( "[Hierachical Topic Modeling] " + "initialization.. ");
		initializeTopicAssignment ();
		for (int i = 0; i < maxIterTopicModel; ++i) {
			System.out.println( "[Hierachical Topic Modeling] " + "iteration: " + i	);
			samplingOneIteration (i);
		}
		System.out.println( "[Hierachical Topic Modeling] " + "aggregating category topics...");
		aggregateWordCount (root);
		System.out.println( "[Hierachical Topic Modeling] " + "training finished.");
	}
	
	@Override
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentConceptML(
			String docConepts) {
		return null;
	}
	
	@Override
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentContentML(
			String docContent) {
		LabelResultML labelResutls = labelDocumentFromStr (docContent);
		HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth = labelResutls.processLabels();
		return labelResultsInDepth;
	}
	
	public LabelResultML labelDocumentFromStr (String document) {
		String[] tokens = document.split("\\s+");
		
		List<Integer> wordIdList = new ArrayList<Integer>();
		for (int i = 0; i < tokens.length; ++i) {
			if (globalWordDict.containsKey(tokens[i])) {
				wordIdList.add(globalWordDict.get(tokens[i]));
			}
		}
		int[] wordSeq = new int[wordIdList.size()];
		for (int i = 0; i < wordIdList.size(); ++i) {
			wordSeq[i] = wordIdList.get(i);
		}
		
		LabelResultML labelResult = new LabelResultML();
		LabelKeyValuePair labelPair = new LabelKeyValuePair(root.getLabelString(), 1);
		labelResult.rootLabel.labelKVP = labelPair;
		labelResult.rootLabel.depth = 0;
		
		labelResult.rootLabel = retrieveLabel_paper (root.getLabelString(), wordSeq, labelResult.rootLabel);
		return labelResult;
	}
	
	public LabelResultTreeNode retrieveLabel_new (String rootStr, int[] wordSeq, LabelResultTreeNode rootLabel) {
		TopicTreeNode node = topicNodeMap.get(rootStr);
		HashSet<TopicTreeNode> children = node.getChildren();
		
		if (children != null && children.size() > 0) {
			// get all topic candidate information
			HashSet<String> candidateList = this.treeLabelData.getTreeChildrenIndex().get(rootStr);
			
			// get all topic candidate information
			List<Integer> topicList = new ArrayList<Integer>();
			HashMap<Integer, OpenIntIntHashMap> candidateTopicWordCount = new HashMap<Integer, OpenIntIntHashMap>();
			OpenIntIntHashMap candidateTopicWordNumSum = new OpenIntIntHashMap();
			for (String candidateName : candidateList) {
				TopicTreeNode candidate = topicNodeMap.get(candidateName);
				OpenIntIntHashMap[] topicWordCountArray = candidate.getTopicWordCountArray();
				
				for (int j = 0; j < topicWordCountArray.length; ++j) {
					int topicId = globalTopicIDMap.get(candidateName + "_" + j);
					candidateTopicWordCount.put(topicId, topicWordCountArray[j]);
					
					IntArrayList values = topicWordCountArray[j].values();
					int totalTopicWordNum = 0;
					for (int k = 0; k < values.size(); ++k) {
						totalTopicWordNum += values.get(k);
					}
					candidateTopicWordNumSum.put(topicId, totalTopicWordNum);
					
					topicList.add(topicId);
				}
			}
			
			OpenIntIntHashMap topicCountMap = new OpenIntIntHashMap();
			// initialize topic sequence
			int[] topicSeq = new int[wordSeq.length];
			for (int i = 0; i < topicSeq.length; ++i) {
				int randomIndex = RandomOperations.RandSample(candidateList.size());
				int randomTopic = topicList.get(randomIndex);
				topicCountMap.put(randomTopic, topicCountMap.get(randomTopic) + 1);
				topicSeq[i] = randomTopic;
			}
			
			for (int iter = 0; iter < maxIterTopicModel; ++iter) {
				for (int i = 0; i < wordSeq.length; ++i) {
					int wordId = wordSeq[i];
					int currentTopic = topicSeq[i];
					double [] probArray = new double[topicList.size()];
					for (int j = 0; j < topicList.size(); ++j) {
						int topicId = topicList.get(j);
						int docCount = topicCountMap.get(topicId);
						int wordCount = candidateTopicWordCount.get(topicId).get(wordId);
						int sumWordCount = candidateTopicWordNumSum.get(topicId);
						if (topicId == currentTopic) {
							docCount -= 1;
						}
						
						probArray[j] = ((double) docCount + globalAlpha) *
								(wordCount + globalBeta) / (sumWordCount + globalBeta * globalWordNum + Double.MIN_VALUE);
					}
					int sampledTopicId = RandomOperations.RandSample(probArray);
					sampledTopicId = topicList.get(sampledTopicId);
					
					if (currentTopic != sampledTopicId) {
						topicSeq[i] = sampledTopicId;
						int docCount = topicCountMap.get(currentTopic) - 1;
						topicCountMap.put(currentTopic, docCount);
						
						if (docCount < 0) {
							try {
								throw new Exception();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						
						docCount = topicCountMap.get(sampledTopicId) + 1;
						topicCountMap.put(sampledTopicId, docCount);
						
					}
					
				}
			}
			
			HashMap<String, Double> similarities = new HashMap<String, Double>();
			HashMap<String, Double> finerSimilarities = new HashMap<String, Double>();
			for (int topicId : topicList) {
				int topicCount = topicCountMap.get(topicId);
				String topicName = this.inverseGlobalTopicIDMap.get(topicId);
				String[] tokens = topicName.split("_");
				double tempSimilarity = (topicCount + globalAlpha) / (wordSeq.length + globalAlpha * topicList.size());
//				similarities.put(topicMap.get(topicId), similarity);
				finerSimilarities.put(topicName, tempSimilarity);
				if (similarities.containsKey(tokens[0]) == false) {
					similarities.put(tokens[0], tempSimilarity);
				} else {
					similarities.put(tokens[0], tempSimilarity + similarities.get(tokens[0]));
//					similarities.put(tokens[0], Math.max(tempSimilarity , similarities.get(tokens[0])));
				}
			}
			
			
			TreeMap<String, Double> sortedSimilarities = HashSort.sortByValues(similarities);
			
			double ratio = 0;
			int labelCount = 0;
			for (String simiKey : sortedSimilarities.keySet()) {
				ratio += similarities.get(simiKey);
				if (ratio < classifierMLThreshold || labelCount < leastK) {
					LabelKeyValuePair labelPair = new LabelKeyValuePair(simiKey, similarities.get(simiKey));
					LabelResultTreeNode labelNode = new LabelResultTreeNode();
					labelNode.labelKVP = labelPair;
					labelNode.depth = rootLabel.depth + 1;
					
					rootLabel.children.add(labelNode);
					
					retrieveLabel_new (simiKey, wordSeq, labelNode);
				}
				labelCount++;
			}
		}
		
		return rootLabel;
	}
	
	public LabelResultTreeNode retrieveLabel_paper (String rootStr, int[] wordSeq, LabelResultTreeNode rootLabel) {
		TopicTreeNode node = topicNodeMap.get(rootStr);
		HashSet<TopicTreeNode> children = node.getChildren();
		
		if (children != null && children.size() > 0) {
			// get all topic candidate information
			List<Integer> topicList = new ArrayList<Integer>();
			HashMap<Integer, String> topicMap = new HashMap<Integer, String>();
			HashMap<Integer, OpenIntDoubleHashMap> candidateTopicWordDistribution = new HashMap<Integer, OpenIntDoubleHashMap>();
			OpenIntIntHashMap candidateTopicWordNumSum = new OpenIntIntHashMap();
			
			int topicIDAll = 0;
			for (TopicTreeNode child : children) {
				TopicTreeNode candidate = child;
				OpenIntDoubleHashMap categoryDistribution = candidate.getCategoryWordDistribution();
				candidateTopicWordDistribution.put(topicIDAll, categoryDistribution);
				
				candidateTopicWordNumSum.put(topicIDAll, candidate.getSumCount());
				
				topicMap.put(topicIDAll, child.getLabelString());
				topicList.add(topicIDAll);
				topicIDAll++;
			}
			
			OpenIntIntHashMap topicCountMap = new OpenIntIntHashMap();
			// initialize topic sequence
			int[] topicSeq = new int[wordSeq.length];
			for (int i = 0; i < topicSeq.length; ++i) {
				int randomTopic = RandomOperations.RandSample(topicIDAll);
				topicCountMap.put(randomTopic, topicCountMap.get(randomTopic) + 1);
				topicSeq[i] = randomTopic;
			}
			
			for (int iter = 0; iter < maxIterTopicModel; ++iter) {
				for (int i = 0; i < wordSeq.length; ++i) {
					int wordId = wordSeq[i];
					int currentTopic = topicSeq[i];
					double [] probArray = new double[topicMap.size()];
					for (int j = 0; j < topicList.size(); ++j) {
						int topicId = topicList.get(j); // here topicList.get(j) == j
						int docCount = topicCountMap.get(topicId);
						if (topicId == currentTopic) {
							docCount -= 1;
						}
						double wordProb = candidateTopicWordDistribution.get(topicId).get(wordId);
						int sumWordCount = candidateTopicWordNumSum.get(topicId);
						
						probArray[j] = ((double) docCount + globalAlpha) *
								(wordProb + globalBeta / (sumWordCount + globalBeta * globalWordNum + Double.MIN_VALUE) );
					}
					int sampledTopicId = RandomOperations.RandSample(probArray);
					sampledTopicId = topicList.get(sampledTopicId);
					
					if (currentTopic != sampledTopicId) {
						topicSeq[i] = sampledTopicId;
						int docCount = topicCountMap.get(currentTopic) - 1;
						topicCountMap.put(currentTopic, docCount);
						
						if (docCount < 0) {
							try {
								throw new Exception();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						
						docCount = topicCountMap.get(sampledTopicId) + 1;
						topicCountMap.put(sampledTopicId, docCount);
						
					}
					
				}
			}
			
			HashMap<String, Double> similarities = new HashMap<String, Double>();
			for (int topicId : topicList) {
				int topicCount = topicCountMap.get(topicId);
				double similarity = (topicCount + globalAlpha) / (wordSeq.length + globalAlpha * topicList.size());
				similarities.put(topicMap.get(topicId), similarity);
			}
			
			
			TreeMap<String, Double> sortedSimilarities = HashSort.sortByValues(similarities);
			
			double ratio = 0;
			int labelCount = 0;
			for (String simiKey : sortedSimilarities.keySet()) {
				ratio += similarities.get(simiKey);
				if (ratio < classifierMLThreshold || labelCount < leastK) {
					LabelKeyValuePair labelPair = new LabelKeyValuePair(simiKey, similarities.get(simiKey));
					LabelResultTreeNode labelNode = new LabelResultTreeNode();
					labelNode.labelKVP = labelPair;
					labelNode.depth = rootLabel.depth + 1;
					
					rootLabel.children.add(labelNode);
					
					retrieveLabel_paper (simiKey, wordSeq, labelNode);
				}
				labelCount++;
			}
		}
		
		return rootLabel;
	}
	
	////////////////////////////////////////////////////////////////
	// Initialize tree and data for training
	////////////////////////////////////////////////////////////////
	// this should be called after treeIndex has been filled
	
	public void initialize (HashMap<String, String> contentData,
			AbstractTreeLabelData treeLabelData,
			AbstractTopicDocMaps topicDocMapData) {

		this.treeLabelData = treeLabelData;
		
		this.corpusStringMap = contentData;
		
		this.topicDocMapData = topicDocMapData;
		
		
		System.out.println("[Training Data:] initialize " + " document features");
		//initialize doc features

		System.out.println("[Training Data:] initialize " + " tree multiclass training data");
		//initialize tree labels
		root = initializeTreeData("root", 0);
		
	}


	protected TopicTreeNode initializeTreeData(String rootNodeStr, int depth) {
		//get children names
		TopicTreeNode node = null;
		HashSet<String> childrenStr = treeLabelData.getTreeChildrenIndex().get(rootNodeStr);
		HashSet<TopicTreeNode> children = new HashSet<TopicTreeNode>();
		if (childrenStr != null && childrenStr.size() > 0) {
			for (String key : childrenStr) {
				TopicTreeNode child = initializeTreeData(key, depth+1);
				children.add(child);
			}
		} 

		System.out.println("  [Data:] initialize tree node " + rootNodeStr);
		
		// initialize all the documents in children; initialize children label maps

		HashMap<String, HashSet<String>> topicDocMap = topicDocMapData.getTopicDocMap();

		HashMap<String, int[]> docWordSeqMap = new HashMap<String, int[]>();
		HashMap<String, int[]> docTopicSeqMap = new HashMap<String, int[]>();
		HashMap<String, OpenIntIntHashMap> docTopicCountMap = new HashMap<String, OpenIntIntHashMap>();
		if (topicDocMap.get(rootNodeStr) != null) {
			for (String docID : topicDocMap.get(rootNodeStr)) {
				String line = corpusStringMap.get(docID);
				String[] tokens = line.split("\\s+");
				int[] idSeq = new int[tokens.length];
				int[] topicSeq = new int[tokens.length];
				OpenIntIntHashMap topicCount = new OpenIntIntHashMap();
				for (int i = 0; i < tokens.length; ++i)	 {
					String token = tokens[i];
					
					if (StopWords.rcvStopWords.contains(token) == true) 
						continue;
					
					if (globalWordDict.containsKey(token) == false) {
						globalWordDict.put(token, globalWordNum);
						inverseGlobalWordDict.put(globalWordNum, token);
						globalWordNum++;
					} 
					
					int wordId = globalWordDict.get(token);
					idSeq[i] = wordId;
				}
				corpusWordSequenceMap.put(docID, idSeq);
				corpusTopicSequenceMap.put(docID, topicSeq);
				corpusTopicCountMap.put(docID, topicCount);
				docWordSeqMap.put(docID, idSeq);
				docTopicSeqMap.put(docID, topicSeq);
				docTopicCountMap.put(docID, topicCount);
			}
		}
		
		node = new TopicTreeNode(children, rootNodeStr, depth, topicPerNode, docWordSeqMap, docTopicSeqMap, docTopicCountMap);
		
		OpenIntIntHashMap[] topicWordCountList = node.getTopicWordCountArray();
		for (int i = 0; i < topicWordCountList.length; ++i) {
			globalTopicIDMap.put(rootNodeStr + "_" + i, globalTopicNum);
			inverseGlobalTopicIDMap.put(globalTopicNum, rootNodeStr + "_" + i);
			globalTopicNum++;
		}

		topicNodeMap.put(rootNodeStr, node);
		return node; 
	}
	
	protected void initializeTopicAssignment () {
		for (String nodeStr : this.topicNodeMap.keySet()) {
			System.out.println("  [Initialization] node : " + nodeStr);

			if (nodeStr.equals("root") == true) {
				System.out.println("  [Initialization] node : " + nodeStr);
			}
			TopicTreeNode node = topicNodeMap.get(nodeStr);
			
			// get word sequence and document topic count
			HashMap<String, int[]> docWordSeqMap = node.getDocWordSeq();
			HashMap<String, int[]> docTopicSeqMap = node.getDocTopicSeq();
			HashMap<String, OpenIntIntHashMap> docTopicCountMap = node.getDocTopicCountMap();
			
			List<String> candidateList = getAllParents (node.getLabelString());
			candidateList.add(node.getLabelString());
			
			// get all topic candidate information
			List<Integer> topicList = new ArrayList<Integer>();
			HashMap<Integer, OpenIntIntHashMap> candidateTopicWordCount = new HashMap<Integer, OpenIntIntHashMap>();
			OpenIntIntHashMap candidateTopicWordNumSum = new OpenIntIntHashMap();
			for (int i = 0; i < candidateList.size(); ++i) {
				String candidateName = candidateList.get(i);
				TopicTreeNode candidate = topicNodeMap.get(candidateName);
				OpenIntIntHashMap[] topicWordCountArray = candidate.getTopicWordCountArray();
				
				for (int j = 0; j < topicWordCountArray.length; ++j) {
					int topicId = globalTopicIDMap.get(candidateName + "_" + j);
					candidateTopicWordCount.put(topicId, topicWordCountArray[j]);
					
					IntArrayList values = topicWordCountArray[j].values();
					int totalTopicWordNum = 0;
					for (int k = 0; k < values.size(); ++k) {
						totalTopicWordNum += values.get(k);
					}
					candidateTopicWordNumSum.put(topicId, totalTopicWordNum);
					
					topicList.add(topicId);
				}
			}
			
			int count = 0;
			for (String docId : docWordSeqMap.keySet()) {
//				System.out.println("  [Initialization] " + count + " documents..");
				if (count % 10 == 0) {
					
				}
				count++;
				OpenIntIntHashMap topicCountMap = docTopicCountMap.get(docId);
				int[] wordSeq = docWordSeqMap.get(docId);
				int[] topicSeq = docTopicSeqMap.get(docId);
				for (int i = 0; i < wordSeq.length; ++i) {
					int wordId = wordSeq[i];
					
					
					int randomSample = RandomOperations.RandSample(candidateTopicWordCount.size());
					int topicId = topicList.get(randomSample);
					
					// update doc side
					int docCount = topicCountMap.get(topicId);
					topicCountMap.put(topicId, docCount + 1);
					docCount = topicCountMap.get(topicId);
					topicSeq[i] = topicId;
					
					// update topic side
					int wordCount = candidateTopicWordCount.get(topicId).get(wordSeq[i]);
					candidateTopicWordCount.get(topicId).put(wordSeq[i], wordCount + 1);
					wordCount = candidateTopicWordCount.get(topicId).get(wordSeq[i]);
					

					int wordCountSum = candidateTopicWordNumSum.get(topicId);
					candidateTopicWordNumSum.put(topicId, wordCountSum + 1);
					
					if (wordCount <= 0) {
						try {
							throw new Exception();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			
		}
	}
	
	
	protected void samplingOneIteration (int iter) {
		for (String nodeStr : this.topicNodeMap.keySet()) {
			
			System.out.println("  [Sampling iteration" + iter + "] on node : " + nodeStr);
			
			TopicTreeNode node = topicNodeMap.get(nodeStr);
			
			// get word sequence and document topic count
			HashMap<String, int[]> docWordSeqMap = node.getDocWordSeq();
			HashMap<String, int[]> docTopicSeqMap = node.getDocTopicSeq();
			HashMap<String, OpenIntIntHashMap> docTopicCountMap = node.getDocTopicCountMap();
			
			List<String> candidateList = getAllParents (node.getLabelString());
			candidateList.add(node.getLabelString());
			
			// get all topic candidate information
			List<Integer> topicList = new ArrayList<Integer>();
			HashMap<Integer, OpenIntIntHashMap> candidateTopicWordCount = new HashMap<Integer, OpenIntIntHashMap>();
			OpenIntIntHashMap candidateTopicWordNumSum = new OpenIntIntHashMap();
			for (int i = 0; i < candidateList.size(); ++i) {
				String candidateName = candidateList.get(i);
				TopicTreeNode candidate = topicNodeMap.get(candidateName);
				OpenIntIntHashMap[] topicWordCountArray = candidate.getTopicWordCountArray();
				
				for (int j = 0; j < topicWordCountArray.length; ++j) {
					int topicId = globalTopicIDMap.get(candidateName + "_" + j);
					candidateTopicWordCount.put(topicId, topicWordCountArray[j]);
					
					IntArrayList values = topicWordCountArray[j].values();
					int totalTopicWordNum = 0;
					for (int k = 0; k < values.size(); ++k) {
						totalTopicWordNum += values.get(k);
					}
					candidateTopicWordNumSum.put(topicId, totalTopicWordNum);
					
					topicList.add(topicId);
				}
			}
			
			int count = 0;
			for (String docId : docWordSeqMap.keySet()) {
//				System.out.println("  [Sampling] " + count + " documents..");
				if (count % 1 == 0) {
					
				}
				count++;
				
				OpenIntIntHashMap topicCountMap = docTopicCountMap.get(docId);
				int[] wordSeq = docWordSeqMap.get(docId);
				int[] topicSeq = docTopicSeqMap.get(docId);
				for (int i = 0; i < wordSeq.length; ++i) {
					int wordId = wordSeq[i];
					int currentTopic = topicSeq[i];
					double [] probArray = new double[topicList.size()];
					for (int j = 0; j < topicList.size(); ++j) {
						int topicId = topicList.get(j);
						int docCount = topicCountMap.get(topicId);
						int wordCount = candidateTopicWordCount.get(topicId).get(wordId);
						int sumWordCount = candidateTopicWordNumSum.get(topicId);
						if (topicId == currentTopic) {
							docCount -= 1;
							wordCount -= 1;
							sumWordCount -= 1;
						}
						
						if (wordCount < 0) {
							if (wordCount <= 0) {
								try {
									throw new Exception();
								} catch (Exception e) {
									System.out.println("Word:" + this.inverseGlobalWordDict.get(wordId) + " error");
									e.printStackTrace();
								}
							}
						}
						
						probArray[j] = ((double) docCount + globalAlpha) *
								(wordCount + globalBeta) / (sumWordCount + globalBeta * globalWordNum + Double.MIN_VALUE);
					}
					int sampledTopicId = RandomOperations.RandSample(probArray);
					sampledTopicId = topicList.get(sampledTopicId);
					
					if (currentTopic != sampledTopicId) {
						int docCount = topicCountMap.get(currentTopic) - 1;
						topicCountMap.put(currentTopic, docCount);
						
						int wordCount = candidateTopicWordCount.get(currentTopic).get(wordId) - 1;
						candidateTopicWordCount.get(currentTopic).put(wordId, wordCount);
		
						int sumWordCount = candidateTopicWordNumSum.get(currentTopic) - 1;
						candidateTopicWordNumSum.put(currentTopic, sumWordCount);
		
						
						if (wordCount < 0) {
							System.out.println("Word:" + this.inverseGlobalTopicIDMap.get(wordId) + " error");
						}
						
						topicSeq[i] = sampledTopicId;
						
						docCount = topicCountMap.get(sampledTopicId) + 1;
						topicCountMap.put(sampledTopicId, docCount);
						
						wordCount = candidateTopicWordCount.get(sampledTopicId).get(wordId) + 1;
						candidateTopicWordCount.get(sampledTopicId).put(wordId, wordCount);
						
						sumWordCount = candidateTopicWordNumSum.get(sampledTopicId) + 1;
						candidateTopicWordNumSum.put(sampledTopicId, sumWordCount);
					}
					
				}
			}
		}
	}
	
	
	int topKeywords = 10;
	public void aggregateWordCount (TopicTreeNode rootNode) {
		System.out.println("  [Aggregate Word Count] : " + rootNode.getLabelString());
		System.out.println("    [Before aggregation] : ");

		OpenIntDoubleHashMap categoryDistribution = new OpenIntDoubleHashMap();
		int sumCount = 0;
		OpenIntIntHashMap[] topicWordCountArray = rootNode.getTopicWordCountArray();
		OpenIntDoubleHashMap[] topicWordDistributionArray = new OpenIntDoubleHashMap[topicWordCountArray.length];
		for (int i = 0; i < topicWordCountArray.length; ++i) {
			topicWordDistributionArray[i] = new OpenIntDoubleHashMap();
			IntArrayList indexList = topicWordCountArray[i].keys();
			int sumLocalCount = 0;
			for (int j = 0; j < indexList.size(); ++j) {
				int index = indexList.get(j);
				int wordCount = topicWordCountArray[i].get(index);
				sumCount += wordCount;
				sumLocalCount += wordCount;
			}
			for (int j = 0; j < indexList.size(); ++j) {
				int index = indexList.get(j);
				int wordCount = topicWordCountArray[i].get(index);
				topicWordDistributionArray[i].put(index, (wordCount + globalBeta) / (sumLocalCount * globalBeta * this.globalWordNum	));
			}
			
			
			System.out.print("      Topic " + i + " top keywords : ");
			List<LabelKeyValuePair> kvpList = new ArrayList<LabelKeyValuePair>();
			for (int j = 0; j < indexList.size(); ++j) {
				int index = indexList.get(j);
				double prob = topicWordDistributionArray[i].get(index);
				double cateProb = categoryDistribution.get(index);
				categoryDistribution.put(index, cateProb + prob);
				
				kvpList.add(new LabelKeyValuePair(this.inverseGlobalWordDict.get(index), prob));
			}
			MinHeapByPQ minHeap = new MinHeapByPQ();
			
			minHeap.initializeQueue(kvpList.subList(0, Math.min(kvpList.size(), topKeywords)));
			for (int j = topKeywords; j < kvpList.size(); ++j) {
				minHeap.add(kvpList.get(j));
			}
			List<LabelKeyValuePair> resultList = minHeap.sort();
			for (int j = 0; j < resultList.size(); ++j) {
				System.out.print(resultList.get(j).getLabel() + ", ");
			}
			System.out.println();
		}
		IntArrayList indexList = categoryDistribution.keys();
		double sum = 0;
		for (int j = 0; j < indexList.size(); ++j) {
			sum += categoryDistribution.get(indexList.get(j));
		}
		for (int j = 0; j < indexList.size(); ++j) {
			if (categoryDistribution.get(indexList.get(j)) != 0	) {
				categoryDistribution.put(indexList.get(j), categoryDistribution.get(indexList.get(j)) / sum );
			}
		}
		rootNode.setCategoryWordDistribution(categoryDistribution);
		rootNode.setSumCount(sumCount);
		
		
		System.out.println("    [Average and normalize topics] : ");
		System.out.print("      Node [" + rootNode.getLabelString() + "] top keywords : ");
		List<LabelKeyValuePair> kvpList = new ArrayList<LabelKeyValuePair>();
		indexList = categoryDistribution.keys();
		for (int j = 0; j < indexList.size(); ++j) {
			int index = indexList.get(j);
			double count = categoryDistribution.get(index);
			kvpList.add(new LabelKeyValuePair(this.inverseGlobalWordDict.get(index), count));
		}
		MinHeapByPQ minHeap = new MinHeapByPQ();
		
		minHeap.initializeQueue(kvpList.subList(0, Math.min(kvpList.size(), topKeywords)));
		for (int j = topKeywords; j < kvpList.size(); ++j) {
			minHeap.add(kvpList.get(j));
		}
		List<LabelKeyValuePair> resultList = minHeap.sort();
		for (int j = 0; j < resultList.size(); ++j) {
			System.out.print(resultList.get(j).getLabel() + ", ");
		}
		System.out.println();
		
		// process children
		HashSet<TopicTreeNode> children = rootNode.getChildren();
		
		if (children != null && children.size() > 0) {
			for (TopicTreeNode child : children) {
				aggregateWordCount (child);
				
				sumCount += child.getSumCount();
				OpenIntDoubleHashMap childCategoryDistribution = child.getCategoryWordDistribution();
				indexList = childCategoryDistribution.keys();
				
				for (int j = 0; j < indexList.size(); ++j) {
					categoryDistribution.put(
							indexList.get(j), 
							categoryDistribution.get(indexList.get(j)) + childCategoryDistribution.get(indexList.get(j))
							);
				}
			}
			
			indexList = categoryDistribution.keys();
			sum = 0;
			for (int j = 0; j < indexList.size(); ++j) {
				sum += categoryDistribution.get(indexList.get(j));
			}
			for (int j = 0; j < indexList.size(); ++j) {
				if (categoryDistribution.get(indexList.get(j)) != 0	) {
					categoryDistribution.put(indexList.get(j), categoryDistribution.get(indexList.get(j)) / sum );
				}
			}
			rootNode.setCategoryWordDistribution(categoryDistribution);
			rootNode.setSumCount(sumCount);
		}
		System.out.println("  [After aggregation of children] : ");
		System.out.print("    Node [" + rootNode.getLabelString() + "] top keywords : ");
		kvpList = new ArrayList<LabelKeyValuePair>();
		indexList = categoryDistribution.keys();
		for (int j = 0; j < indexList.size(); ++j) {
			int index = indexList.get(j);
			double count = categoryDistribution.get(index);
			kvpList.add(new LabelKeyValuePair(this.inverseGlobalWordDict.get(index), count));
		}
		minHeap = new MinHeapByPQ();
		
		minHeap.initializeQueue(kvpList.subList(0, Math.min(kvpList.size(), topKeywords)));
		for (int j = topKeywords; j < kvpList.size(); ++j) {
			minHeap.add(kvpList.get(j));
		}
		resultList = minHeap.sort();
		for (int j = 0; j < resultList.size(); ++j) {
			System.out.print(resultList.get(j).getLabel() + ", ");
		}
		System.out.println();
	}
}
