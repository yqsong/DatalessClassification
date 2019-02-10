package edu.illinois.cs.cogcomp.classification.hierarchy.dataless.bow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.AbstractLabelTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.nytimes.NYTimesTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.yahoo.WikiCateLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.yahoo.YahooDirLabelData;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.main.CustomizedLabelDataHCTree;
import edu.illinois.cs.cogcomp.classification.main.CustomizedLabelDataTree;

/**
 * yqsong@illinois.edu
 */

public abstract class AbstractLabelBOWTree extends AbstractLabelTree {

	private static final long serialVersionUID = 1L;

	protected LabelBOWNode root;
	
	int maxDepth = 10;
	
	public static void main (String args[]) {
		
	}
	
	protected HashMap<String, Double> globalWeights;

	public AbstractLabelBOWTree (String data, HashMap<String, Double> weights) {
		if (data.equals(DatalessResourcesConfig.CONST_DATA_RCV)) {
			treeLabelData = new RCVTreeLabelData();
		}
		if (data.equals(DatalessResourcesConfig.CONST_DATA_NYTIMES)) {
			treeLabelData = new NYTimesTreeLabelData();
		}
		if (data.equals(DatalessResourcesConfig.CONST_DATA_20NG)) {
			treeLabelData = new NewsgroupsTreeLabelData();
		}
		if (data.equals(DatalessResourcesConfig.CONST_DATA_YAHOO)) {
			treeLabelData = new YahooDirLabelData();
		}
		if (data.equals(DatalessResourcesConfig.CONST_DATA_SIMPLEWIKI)) {
			treeLabelData = new WikiCateLabelData();
		}
		if (data.equals(DatalessResourcesConfig.CONST_DATA_CUSTOMIZED)) {
			treeLabelData = new CustomizedLabelDataTree(null);
		}
		if (data.equals(DatalessResourcesConfig.CONST_DATA_CUSTOMIZEDHC)) {
			treeLabelData = new CustomizedLabelDataHCTree();
		}
		
	
//		switch (data) {
//		case Config.CONST_DATA_RCV: 
//			treeLabelData = new RCVTreeLabelData();
//			break;
//		case Config.CONST_DATA_NYTIMES: 
//			treeLabelData = new NYTimesTreeLabelData();
//			break;
//		case Config.CONST_DATA_20NG: 
//			treeLabelData = new NewsgroupsTreeLabelData();
//			break;
//		case Config.CONST_DATA_YAHOO: 
//			treeLabelData = new YahooDirLabelData();
//			break;
//		case Config.CONST_DATA_SIMPLEWIKI: 
//			treeLabelData = new WikiCateLabelData();
//			break;
//		case Config.CONST_DATA_CUSTOMIZED: 
//			treeLabelData = new CustomizedLabelDataTree();
//			break;
//		}
		
		this.globalWeights = weights;
	}

	public LabelBOWNode initializeTreeWithConceptVector(String rootStr, int depth) {
		HashSet<String> childrenStr = treeLabelData.getTreeChildrenIndex().get(rootStr);
		HashSet<LabelBOWNode> children = new HashSet<LabelBOWNode>();
		if (childrenStr != null && depth < maxDepth) {
			for (String key : childrenStr) {
				LabelBOWNode child = initializeTreeWithConceptVector(key, depth + 1);
				children.add(child);
			}
		}
		LabelBOWNode node = new LabelBOWNode(rootStr, treeLabelData.getTreeLabelNameHashMap().get(rootStr), depth, children, false, globalWeights);
		return node;
	}


	public int getLabelDepth (String label) {
		LabelBOWNode node = getLabelDepth (label, root);
		if (node != null) {
			return node.getDepth();
		}
		return 0;
	}

	public LabelBOWNode getLabelDepth (String label, LabelBOWNode rootNode) {
		if (rootNode.getLabelString().equalsIgnoreCase(label.trim()) == true) {
			return rootNode;
		} else {
			for (LabelBOWNode child : rootNode.getChildren()) {
				LabelBOWNode node = getLabelDepth (label, child);
				if (node != null) {
					return node;
				}
			}
		}
		
		return null;
	}
	
	public void getLeafNodesLabels (LabelBOWNode rootNode, List<LabelBOWNode> leaveHashSet) {
		for (LabelBOWNode child : rootNode.getChildren()) {
			getLeafNodesLabels (child, leaveHashSet);
		}
		if (rootNode.getChildren() == null || rootNode.getChildren().size() == 0) {
			leaveHashSet.add(rootNode);
		}
	}

	public void setRootNode(LabelBOWNode rootNode) {
		this.root = rootNode;
	}
	
	public void readLabelTreeFromDump (String filePath, boolean isBreakConcepts) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty() == true && line.equals("") == true) 
					continue;
				String[] tokens = line.split("\t");
				if (tokens.length != 4) 
					continue;

				String parent = tokens[0].trim().toLowerCase();
				String child = tokens[1].trim().toLowerCase();
				String childName = tokens[2].trim().toLowerCase();
				String vector = tokens[3].trim();
				if (treeLabelData.getTreeChildrenIndex().containsKey(parent) == true) {
					if (treeLabelData.getTreeChildrenIndex().get(parent).contains(child) == false)
						treeLabelData.getTreeChildrenIndex().get(parent).add(child);
				} else {
					treeLabelData.getTreeChildrenIndex().put(parent, new HashSet<String>());
					treeLabelData.getTreeChildrenIndex().get(parent).add(child);
				}
				if (treeLabelData.getTreeParentIndex().containsKey(child) == false) {
					treeLabelData.getTreeParentIndex().put(child, parent);
				}
				if (treeLabelData.getTreeLabelNameHashMap().containsKey(child) == false) {
					treeLabelData.getTreeLabelNameHashMap().put(child, childName);
				}
			}
			reader.close();
//			root = initializeTreeWithConceptVector("root", 0, isBreakConcepts);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
