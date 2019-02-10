package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Problem;
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

public abstract class AbstractClassifierLibLinearTree extends AbstractLabelTree {
	
	private static final long serialVersionUID = 1L;

	protected ClassifierLibLinearTreeNode root;
	protected List<ClassifierLibLinearTreeNode> allNodeList;

	protected  AbstractTopicDocMaps topicDocMapData;
	protected  HashMap<String, String> corpusStringMap;
	
	protected double C = 10000;
	protected int nThreads = 1;
	
	protected CorpusDataProcessing jlisData = null;
	protected HashMap<String, String> trainingDataLibSVMFormat = null;
	
	public AbstractClassifierLibLinearTree (String data) {
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
		
		allNodeList = new ArrayList<ClassifierLibLinearTreeNode>();
		
		jlisData = new CorpusDataProcessing();
		jlisData.startFromZero = false;
	}
	
	abstract public LabelResultMC labelDocument (String docContent);
	
	abstract public void trainAllTreeNodes();
	
	public void setPenaltyParaC (double c) {
		this.C = c;
	}

	public int getLabelDepth (String label) {
		ClassifierLibLinearTreeNode node = getLabelDepth (label, root);
		if (node != null) {
			return node.getDepth();
		}
		return 0;
	}

	public ClassifierLibLinearTreeNode getLabelDepth (String label, ClassifierLibLinearTreeNode rootNode) {
		if (rootNode.getLabelString().equalsIgnoreCase(label.trim()) == true) {
			return rootNode;
		} else {
			for (ClassifierLibLinearTreeNode child : rootNode.getChildren()) {
				ClassifierLibLinearTreeNode node = getLabelDepth (label, child);
				if (node != null) {
					return node;
				}
			}
		}
		
		return null;
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

		System.out.println("[Training Data:] read " + " tree data...");
		treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		treeLabelData.readTopicDescription(fileTopicDescriptionPath);
		
		corpusStringMap = contentData;
		
		System.out.println("[Training Data:] read " + " topic doc map data...");
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
	protected ClassifierLibLinearTreeNode initializeTreeLabelsAndData(String rootNodeStr, int depth) {
		//get children names
		ClassifierLibLinearTreeNode node = null;
		HashSet<String> childrenStr = treeLabelData.getTreeChildrenIndex().get(rootNodeStr);
		HashSet<ClassifierLibLinearTreeNode> children = new HashSet<ClassifierLibLinearTreeNode>();
		if (childrenStr != null) {
			for (String key : childrenStr) {
				ClassifierLibLinearTreeNode child = initializeTreeLabelsAndData(key, depth+1);
				children.add(child);
			}
			
			System.out.println("  [Data:] initialize tree node " + rootNodeStr);
			
			// initialize all the documents in children; initialize children label maps
			List<Double> yList = new ArrayList<Double>();;
			List<FeatureNode[]> xList = new ArrayList<FeatureNode[]>();

			Map<String, Integer> labelsMapping = new HashMap<String, Integer>();
			HashMap<String, HashSet<String>> topicDocMap = topicDocMapData.getTopicDocMap();
			for (String childLabel : childrenStr) {
				labelsMapping.put(childLabel, labelsMapping.size());
				if (topicDocMap.get(childLabel) != null) {
					for (String docID : topicDocMap.get(childLabel)) {
						// initialize labal for libLinear
						yList.add((double)labelsMapping.get(childLabel));
						
						// initialize data for libLinear
						String[] tokens = trainingDataLibSVMFormat.get(docID).split(" ");
						FeatureNode[] feature = new FeatureNode[tokens.length];
						for (int i = 0; i < tokens.length; ++i) {
							String[] subTokens = tokens[i].trim().split(":");
							int index = Integer.parseInt(subTokens[0].trim());
							double value = Double.parseDouble(subTokens[1].trim());
							feature[i] = new FeatureNode(index, value);
						}
						xList.add(feature);
					}
				}
			}
			Feature[][] xArray = new Feature[yList.size()][];
			double[] yArray = new double[yList.size()];
			for (int i = 0; i < yList.size(); ++i) {
				yArray[i] = yList.get(i);
				xArray[i] = xList.get(i);
			}
			Problem problem = new Problem();
			problem.l = yList.size(); // number of training examples
			problem.n = jlisData.getDictSize(); // number of features
			problem.x = xArray; // feature nodes
			problem.y = yArray; // target values
			// initialize the root data
			node = new ClassifierLibLinearTreeNode(children, rootNodeStr, problem, labelsMapping, depth, false);
		} else {
			HashMap<String, HashSet<String>> topicDocMap = topicDocMapData.getTopicDocMap();
			globalLeafLabelIDMap.put(rootNodeStr, globalLeafLabelIDMap.size());
			if (topicDocMap.get(rootNodeStr) != null) {
				for (String docID : topicDocMap.get(rootNodeStr)) {
					if (docID.equals("D:\\data_test\\20newsgroup\\20NG_Source\\misc.forsale\\76303")) {
						System.out.println("debug");
					}
					String line = rootNodeStr + " " + trainingDataLibSVMFormat.get(docID);
					globalLeafDocLines.add(line);
				}
			}
			
			node = new ClassifierLibLinearTreeNode(children, rootNodeStr, null, null, depth, false);
		}

		allNodeList.add(node);
		return node; 
	}
	


}
