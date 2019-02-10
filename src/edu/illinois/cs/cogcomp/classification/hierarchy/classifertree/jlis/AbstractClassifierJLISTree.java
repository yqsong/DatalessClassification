package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.jlis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.indsup.mc.LabeledMulticlassData;

/**
 * yqsong@illinois.edu
 */

public abstract class AbstractClassifierJLISTree extends AbstractLabelTree {
	
	private static final long serialVersionUID = 1L;

	protected ClassifierJLISTreeNode root;
	protected List<ClassifierJLISTreeNode> allNodeList;

	protected  AbstractTopicDocMaps topicDocMapData;
	protected HashMap<String, String> corpusStringMap;
	
	protected double C = 100;
	protected int nThreads = 1;
	
	protected CorpusDataProcessing jlisData = null;
	protected HashMap<String, String> trainingDataLibSVMFormat = null;
	
	public AbstractClassifierJLISTree (String data) {
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
		
		allNodeList = new ArrayList<ClassifierJLISTreeNode>();
		
		jlisData = new CorpusDataProcessing();
	}
	
	abstract public LabelResultMC labelDocument (String docContent);

	abstract public void trainAllTreeNodes();
	
	public int getLabelDepth (String label) {
		ClassifierJLISTreeNode node = getLabelDepth (label, root);
		if (node != null) {
			return node.getDepth();
		}
		return 0;
	}

	public ClassifierJLISTreeNode getLabelDepth (String label, ClassifierJLISTreeNode rootNode) {
		if (rootNode.getLabelString().equalsIgnoreCase(label.trim()) == true) {
			return rootNode;
		} else {
			for (ClassifierJLISTreeNode child : rootNode.getChildren()) {
				ClassifierJLISTreeNode node = getLabelDepth (label, child);
				if (node != null) {
					return node;
				}
			}
		}
		
		return null;
	}
	
	public void initialize (HashMap<String, String> contentData,
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
	
	public void initialize (HashMap<String, String> contentData,
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
	
	
	////////////////////////////////////////////////////////////////
	// Initialize tree and data for training
	////////////////////////////////////////////////////////////////
	// this should be called after treeIndex has been filled
	protected Map<String, Integer> globalLeafLabelIDMap = new HashMap<String, Integer>();
	protected List<String> globalLeafDocLines = new ArrayList<String>();
	protected ClassifierJLISTreeNode initializeTreeLabelsAndData(String rootNodeStr, int depth) {
		//get children names
		ClassifierJLISTreeNode node = null;
		HashSet<String> childrenStr = treeLabelData.getTreeChildrenIndex().get(rootNodeStr);
		HashSet<ClassifierJLISTreeNode> children = new HashSet<ClassifierJLISTreeNode>();
		if (childrenStr != null) {
			for (String key : childrenStr) {
				ClassifierJLISTreeNode child = initializeTreeLabelsAndData(key, depth+1);
				children.add(child);
			}
			
			System.out.println("  [Data:] initialize tree node " + rootNodeStr);
			
			// initialize all the documents in children; initialize children label maps
			Map<String, Integer> labelsMapping = new HashMap<String, Integer>();
			HashMap<String, HashSet<String>> topicDocMap = topicDocMapData.getTopicDocMap();
			List<String> docLines = new ArrayList<String>();
			for (String key : childrenStr) {
				labelsMapping.put(key, labelsMapping.size());
				if (topicDocMap.get(key) != null) {
					for (String docID : topicDocMap.get(key)) {
						String line = key + " " + trainingDataLibSVMFormat.get(docID);
						docLines.add(line);
					}
				}
			}
			
			// initialize labeled data in libSVM format
			LabeledMulticlassData labeledData = this.jlisData.readMultiClassDataLibSVMStr(docLines, this.jlisData.getFeatureNum(), labelsMapping);

			// initialize the root data
			node = new ClassifierJLISTreeNode(children, rootNodeStr, labeledData, depth, false);
		} else {
			HashMap<String, HashSet<String>> topicDocMap = topicDocMapData.getTopicDocMap();
			globalLeafLabelIDMap.put(rootNodeStr, globalLeafLabelIDMap.size());
			if (topicDocMap.get(rootNodeStr) != null) {
				for (String docID : topicDocMap.get(rootNodeStr)) {
					String line = rootNodeStr + " " + trainingDataLibSVMFormat.get(docID);
					globalLeafDocLines.add(line);
				}
			}
			
			node = new ClassifierJLISTreeNode(children, rootNodeStr, null, depth, true);
		}

		allNodeList.add(node);
		return node; 
	}
	


}
