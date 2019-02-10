package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import LBJ2.classify.DiscretePrimitiveStringFeature;
import LBJ2.classify.FeatureVector;
import LBJ2.learn.Lexicon;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.AbstractLabelTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts.AbstractTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts.AbstractTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.jlis.CorpusDataProcessing;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.nytimes.NYTimesTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.nytimes.NYTimesTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultMC;

/**
 * yqsong@illinois.edu
 */

public abstract class AbstractClassifierLBJTree extends AbstractLabelTree {
	
	private static final long serialVersionUID = 3358003069924654580L;
	protected ClassifierLBJTreeNode root;
	protected List<ClassifierLBJTreeNode> allNodeList;

	protected  AbstractTopicDocMaps topicDocMapData;
	protected HashMap<String, String> corpusStringMap;
	
	protected double C = 10000;
	protected int nThreads = 1;
	
	protected CorpusDataProcessing jlisData = null;
	protected HashMap<String, String> trainingDataLibSVMFormat = null;
	
	protected String learningMethod = "naiveBayes";
	
	public AbstractClassifierLBJTree (String data) {
		if (data.equals("rcv")) {
			treeLabelData = new RCVTreeLabelData();
			topicDocMapData = new RCVTopicDocMaps();
		}
		if (data.equals("nytimes")) {
			treeLabelData = new NYTimesTreeLabelData();
			topicDocMapData = new NYTimesTopicDocMaps();
		}
		if (data.equals("20newsgroups")) {
			treeLabelData = new NewsgroupsTreeLabelData();
			topicDocMapData = new NewsgroupsTopicDocMaps();
		}
		
		allNodeList = new ArrayList<ClassifierLBJTreeNode>();
		
		jlisData = new CorpusDataProcessing();
		jlisData.startFromZero = true;
	}

	abstract public LabelResultMC labelDocument (String docContent);

	abstract public void trainAllTreeNodes();
	
	public int getLabelDepth (String label) {
		ClassifierLBJTreeNode node = getLabelDepth (label, root);
		if (node != null) {
			return node.getDepth();
		}
		return 0;
	}

	public void setPenaltyParaC (double c) {
		this.C = c;
	}

	public ClassifierLBJTreeNode getLabelDepth (String label, ClassifierLBJTreeNode rootNode) {
		if (rootNode.getLabelString().equalsIgnoreCase(label.trim()) == true) {
			return rootNode;
		} else {
			for (ClassifierLBJTreeNode child : rootNode.getChildren()) {
				ClassifierLBJTreeNode node = getLabelDepth (label, child);
				if (node != null) {
					return node;
				}
			}
		}
		
		return null;
	}
	
	public void initializeWithContentData (HashMap<String, String> contentData,
			AbstractTreeLabelData treeLabelData,
			AbstractTopicDocMaps topicDocMapData) {

		this.treeLabelData = treeLabelData;
		
		this.corpusStringMap = contentData;
		
		this.topicDocMapData = topicDocMapData;
		
		
		System.out.println("[Training Data:] initialize " + " document features");
		//initialize doc features
		trainingDataLibSVMFormat = jlisData.initializeTrainingDocumentFeatures (corpusStringMap, true, true);

		System.out.println("[Training Data:] initialize " + " tree multiclass training data");
		//initialize tree labels
		root = initializeTreeLabelsAndData("root", 0);
		
	}
	
	public void initializeWithContentData (HashMap<String, String> contentData,
			String fileTopicHierarchyPath,
			String fileTopicDescriptionPath,
			String fileTopicDocMapPath) {

		treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		treeLabelData.readTopicDescription(fileTopicDescriptionPath);
		
		corpusStringMap = contentData;
		
		topicDocMapData.readFilteredTopicDocMap (fileTopicDocMapPath, corpusStringMap.keySet());
		
		
		System.out.println("[Training Data:] initialize " + " document features");
		//initialize doc features
		trainingDataLibSVMFormat = jlisData.initializeTrainingDocumentFeatures (corpusStringMap, true, true);

		System.out.println("[Training Data:] initialize " + " tree multiclass training data");
		//initialize tree labels
		root = initializeTreeLabelsAndData("root", 0);
		
	}
	
	public void initializeWithConceptData (HashMap<String, String> conceptData,
			AbstractTreeLabelData treeLabelData,
			AbstractTopicDocMaps topicDocMapData) {

		this.treeLabelData = treeLabelData;
		
		this.corpusStringMap = conceptData;
		
		this.topicDocMapData = topicDocMapData;
		
		
		System.out.println("[Training Data:] initialize " + " document features");
		//initialize doc features
		trainingDataLibSVMFormat = jlisData.initializeTrainingConceptFeatures (corpusStringMap, true, true);

		System.out.println("[Training Data:] initialize " + " tree multiclass training data");
		//initialize tree labels
		root = initializeTreeLabelsAndData("root", 0);
		
	}

	
	////////////////////////////////////////////////////////////////
	// Initialize tree and data for training
	////////////////////////////////////////////////////////////////
	// this should be called after treeIndex has been filled
	protected Map<String, Integer> globalLeafLabelIDMap = new HashMap<String, Integer>();
	protected Map<Integer, String> globalInverseLeafLabelIDMap = new HashMap<Integer, String>();
	protected List<String> globalLeafDocLines = new ArrayList<String>();
	protected ClassifierLBJTreeNode initializeTreeLabelsAndData(String rootNodeStr, int depth) {
		//get children names
		ClassifierLBJTreeNode node = null;
		HashSet<String> childrenStr = treeLabelData.getTreeChildrenIndex().get(rootNodeStr);
		HashSet<ClassifierLBJTreeNode> children = new HashSet<ClassifierLBJTreeNode>();
		if (childrenStr != null) {
			for (String key : childrenStr) {
				ClassifierLBJTreeNode child = initializeTreeLabelsAndData(key, depth+1);
				children.add(child);
			}
			
			System.out.println("  [Data:] initialize tree node " + rootNodeStr);
			
			// initialize the root data
			node = new ClassifierLBJTreeNode(children, rootNodeStr, learningMethod, depth, false);

			// initialize all the documents in children; initialize children label maps

			Lexicon featureLexicon = jlisData.getGlobalLexicon();
			Lexicon labelLexicon = node.getModel().getLabelLexicon();//new Lexicon();
			
			Map<String, Integer> labelsMapping = new HashMap<String, Integer>();
			for (String childLabel : childrenStr) {
				System.out.println("      [Debug:] root: " + rootNodeStr + ", child: " + childLabel);
				
				labelsMapping.put(childLabel, labelsMapping.size());
				int id = labelsMapping.get(childLabel);
				DiscretePrimitiveStringFeature feature = new DiscretePrimitiveStringFeature(
						"traininglabelpackage",
						"CorpusDataProcessing",
						childLabel,
						childLabel,
						(short) id,
						(short) childrenStr.size()
						);
				labelLexicon.lookup(feature, true);
			}

			List<Object> rawDataList = new ArrayList<Object>();
			List<FeatureVector> vectorList = new ArrayList<FeatureVector>();
			HashMap<String, HashSet<String>> topicDocMap = topicDocMapData.getTopicDocMap();
			for (String childLabel : childrenStr) {
				
				if (topicDocMap.get(childLabel) != null) {
					for (String docID : topicDocMap.get(childLabel)) {
						
						// initialize data for lbj
						String[] tokens = trainingDataLibSVMFormat.get(docID).split(" ");
						int[] indexArray = new int[tokens.length];
						double[] valueArray = new double[tokens.length];
						for (int i = 0; i < tokens.length; ++i) {
							String[] subTokens = tokens[i].trim().split(":");
							int index = Integer.parseInt(subTokens[0].trim());
							double value = Double.parseDouble(subTokens[1].trim());
							indexArray[i] = index;
							valueArray[i] = value;
						}
						
						int[] labelIndexArray = new int[1];
						double[] labelValueArray = new double[1];
						labelIndexArray[0] = labelsMapping.get(childLabel);
						labelValueArray[0] = 1;
						
						Object[] dataSample = new Object[4];
						dataSample[0] = indexArray;
						dataSample[1] = valueArray;
						dataSample[2] = labelIndexArray;
						dataSample[3] = labelValueArray;

						FeatureVector vector =
						        new FeatureVector((Object[]) dataSample, featureLexicon, labelLexicon);
						vector.sort();
						
						vectorList.add(vector);
						
						rawDataList.add(dataSample);
					}
				}
			}
			Object[] dataArray = new Object[vectorList.size()];
			FeatureVector[] vectorArray = new FeatureVector[vectorList.size()];
			for (int i = 0; i < vectorList.size(); ++i) {
				vectorArray[i] = vectorList.get(i);
				dataArray[i] = rawDataList.get(i);
			}

			node.setLabelMapping(labelsMapping);
			node.setTrainingData(vectorArray, dataArray);
			
		} else {
			HashMap<String, HashSet<String>> topicDocMap = topicDocMapData.getTopicDocMap();
			globalLeafLabelIDMap.put(rootNodeStr, globalLeafLabelIDMap.size());
			globalInverseLeafLabelIDMap.put(globalInverseLeafLabelIDMap.size(), rootNodeStr);
			if (topicDocMap.get(rootNodeStr) != null) {
				for (String docID : topicDocMap.get(rootNodeStr)) {
					String line = rootNodeStr + " " + trainingDataLibSVMFormat.get(docID);
					globalLeafDocLines.add(line);
				}
			}
			
			node = new ClassifierLBJTreeNode(children, rootNodeStr, learningMethod, depth, true);
		}

		allNodeList.add(node);
		return node; 
	}
	


}
