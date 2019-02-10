package edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.AbstractLabelTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsTopicHierarchy;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.nytimes.NYTimesTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.yahoo.CustomizedLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.yahoo.WikiCateLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.yahoo.WikiTwoLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.yahoo.YahooDirLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.main.CustomizedLabelDataHCTree;
import edu.illinois.cs.cogcomp.classification.main.CustomizedLabelDataTree;
import edu.illinois.cs.cogcomp.classification.representation.esa.AbstractESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.DiskBasedComplexESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.simple.SimpleESALocal;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.DiskBasedWordEmbedding;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.WordEmbeddingInterface;
import edu.illinois.cs.cogcomp.server.CustomizedHCServer;

/**
 * yqsong@illinois.edu
 */

public abstract class AbstractConceptTree extends AbstractLabelTree {
	
	protected ConceptTreeNode root;
	protected List<ConceptTreeNode> treeNodeList;
	
	protected String dataCorpus;

	protected HashMap<String, String> conceptVectorStringHash;
	protected HashMap<String, SparseVector> conceptVectorHash;
	public HashMap<String, Double> globalConceptWeights;
	public AbstractESA esa = null;
	public WordEmbeddingInterface word2vec = null;
	protected String representationType = "simple";
	public static String[] representationTypes = new String[] {"simple", "complex", "wordDist"};
	
	protected int numConcepts;
	
	protected boolean isDebug = true;
	
	public boolean isDebug() {
		return isDebug;
	}

	public void setDebug(boolean isDebug) {
		this.isDebug = isDebug;
	}

	protected int maxDepth = 10;
	
	public static void main (String args[]) {
		
	}
	

	public AbstractConceptTree (String data, String method, HashMap<String, Double> conceptWeights, boolean isInitializeLucene) {
		root = null;
		numConcepts = 500;
		treeNodeList = new ArrayList<ConceptTreeNode>();
		
		conceptVectorStringHash = new HashMap<String, String>();
		conceptVectorHash = new HashMap<String, SparseVector>();
		
		dataCorpus = data;
		
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
		if (data.equals(DatalessResourcesConfig.CONST_DATA_CUSTOMIZEDHC)) {
			treeLabelData = new CustomizedLabelDataHCTree();
		}
		if (data.equals("CustomizedHCServer")) {
			treeLabelData = new CustomizedHCServer();
		}
		
		representationType = method;
		
		if (isInitializeLucene)
		{
			if (representationType.equals("complex") ) {
				esa = new DiskBasedComplexESA();
			} else if (representationType.equals("simple") ) {
				esa = new SimpleESALocal();
			} else if (representationType.equals("wordDist") ) {
				word2vec = new DiskBasedWordEmbedding();
			} 
		}
		
		if (data.equals(DatalessResourcesConfig.CONST_DATA_CUSTOMIZED)) {
			treeLabelData = new CustomizedLabelDataTree(esa);
		}

		globalConceptWeights = conceptWeights;
	}

	public List<ConceptTreeNode> getTreeNodeList () {
		return this.treeNodeList;
	}
	public HashMap<String, SparseVector>  getTreeNodeVectorHash () {
		return this.conceptVectorHash;
	}
	
	
	public ConceptTreeNode getRootNode ()	 {
		return root;
	}
	
	public void setRootNode (ConceptTreeNode rootNode) {
		this.root = rootNode;
	}

	public void setConceptNum(int num) {
		this.numConcepts = num;
	}

	public int getLabelDepth (String label) {
		ConceptTreeNode node = getLabelDepth (label, root);
		if (node != null) {
			return node.getDepth();
		}
		return 0;
	}

	public ConceptTreeNode getLabelDepth (String label, ConceptTreeNode rootNode) {
		if (rootNode.getLabelString().equalsIgnoreCase(label.trim()) == true) {
			return rootNode;
		} else {
			for (ConceptTreeNode child : rootNode.getChildren()) {
				ConceptTreeNode node = getLabelDepth (label, child);
				if (node != null) {
					return node;
				}
			}
		}
		
		return null;
	}
	
	public void getLeafNodesConcepts (ConceptTreeNode rootNode, List<ConceptTreeNode> leaveHashSet) {
		for (ConceptTreeNode child : rootNode.getChildren()) {
			getLeafNodesConcepts (child, leaveHashSet);
		}
		if (rootNode.getChildren() == null || rootNode.getChildren().size() == 0) {
			leaveHashSet.add(rootNode);
		}
	}
	
	public SparseVector convertDocToVector (String document, boolean isBreakConcepts) {
		List<String> conceptsList = new ArrayList<String>();
		List<Double> scores = new ArrayList<Double>();

		if (representationType.equals("complex") ) {
			List<ConceptData> concepts = null;
			try {
				concepts = esa.retrieveConcepts(document, this.numConcepts, ClassifierConstant.complexVectorType);
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int i = concepts.size() - 1; i >= 0; i--) {
				conceptsList.add(concepts.get(i).concept + "");
				scores.add(concepts.get(i).score);
			}
		} else if (representationType.equals("simple") ) {
			List<ConceptData> concepts = null;
			try {
				concepts = esa.retrieveConcepts(document, this.numConcepts);
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int i = 0; i < concepts.size(); i++) {
				conceptsList.add(concepts.get(i).concept + "");
				scores.add(concepts.get(i).score);
			}
		} else if (representationType.equals("wordDist") ) {
			HashMap<Integer, Double>  features = word2vec.getConceptVectorBasedonSegmentation(document, true);
			for (Integer i : features.keySet()) {
				conceptsList.add(i + "");
				scores.add(features.get(i));
			}
		} else if (representationType.equals("wordDistSimple") ) {
			double[] vec = word2vec.getDenseVectorSimpleAverage(document);
			for (int i = 0; i < vec.length; ++i) {
				conceptsList.add(i + "");
				scores.add(vec[i]);
			}
		} else {
			List<ConceptData> concepts = null;
			try {
				concepts = esa.retrieveConcepts(document, this.numConcepts);
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int i = 0; i < concepts.size(); i++) {
				conceptsList.add(concepts.get(i).concept + "");
				scores.add(concepts.get(i).score);
			}
		}
		
		SparseVector docConceptVector = new SparseVector(conceptsList, scores, isBreakConcepts, globalConceptWeights);

		return docConceptVector;
	}
	///////////////////////////////////////////////////////////////////////////////////////////
	// Initialize tree with concepts
	///////////////////////////////////////////////////////////////////////////////////////////

	public void readLabelTreeFromDump (String filePath, boolean isBreakConcepts) {
		try {
			if (isDebug) {
				System.out.println("read tree from file...");
			}
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			while ((line = reader.readLine()) != null) {
				//System.out.print(line+"\n");
				if (line.isEmpty() == true && line.equals("") == true){ 
					//System.out.print("return at line 265\n");
					continue;
				}	
				String[] tokens = line.split("\t");
				if (tokens.length != 4) {
					//System.out.print("return at line 270 and token length is"+tokens.length+"\n");
					//System.out.print(tokens[0]+"\n"+tokens[1]+"\n"+tokens[2]+"\n"+tokens[3]+"\n");
					continue;
				}
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
				
				if (conceptVectorStringHash.containsKey(child) == false) {
					conceptVectorStringHash.put(child, vector);
				}
			}
			reader.close();
			if (isDebug) {
				System.out.println("read tree finished");
			}
//			root = initializeTreeWithConceptVector("root", 0, isBreakConcepts);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//just for old 20newsgroups data
	public void modifyTreeNodeConcepts (String file) {
		NewsgroupsTopicHierarchy newsgroups = new NewsgroupsTopicHierarchy();

		try {
			for (String topic : newsgroups.getTopicHierarchy().keySet()) {
				HashMap<String, String> subTopics = newsgroups.getTopicHierarchy().get(topic);
				HashMap<String, Double> topicConceptDist = new HashMap<String, Double>();
				for (String subTopic : subTopics.keySet()) {
					String folderName = file + "/" + subTopic + ".concepts";
					File topicConceptFile = new File(folderName);
					if (topicConceptFile.isDirectory() == true) {
						continue;
					}
					System.out.println("  [Read newsgroups data concepts: ] " + topicConceptFile.getAbsolutePath());
			     	FileReader reader = new FileReader(topicConceptFile);
			     	BufferedReader bf = new BufferedReader(reader);
			     	String line = "";
			     	int count = 0;
	//		     	List<String> conceptsList = new ArrayList<String>();
	//	    		List<Double> scores = new ArrayList<Double>();
			     	String subTopicConceptStr = "";
		    		while ((line = bf.readLine()) != null) {
			     		String[] tokens = line.split("\t");
			     		if (tokens.length != 3) 
			     			continue;
			     		String conceptID = tokens[0];
			     		String conceptName = tokens[1].trim().replaceAll(",", " ").replaceAll(";", " ").replaceAll("_", " ");
			     		double score = Double.parseDouble(tokens[2].trim());;
	//		     		conceptsList.add(conceptName);
	//		     		scores.add(Double.parseDouble(score));
			     		subTopicConceptStr += conceptName + "," + score + ";";
			     		if (topicConceptDist.containsKey(conceptName) == true) {
			     			topicConceptDist.put(conceptName, topicConceptDist.get(conceptName) + score);
			     		} else {
			     			topicConceptDist.put(conceptName, score);
			     		}
			     	}
		    		bf.close();
			     	reader.close();
		    		this.conceptVectorStringHash.put(subTopic, subTopicConceptStr);
	//	    		String urlHeadStr = "D:\\data_test\\20newsgroup\\20NG_Source\\";
				}
				String topicConceptStr = "";
				for (String key : topicConceptDist.keySet()) {
					topicConceptStr += key + "," + topicConceptDist.get(key) + ";";
				}
				this.conceptVectorStringHash.put(topic, topicConceptStr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ConceptTreeNode initializeTreeWithConceptVector(String rootStr, int depth, boolean isBreakConcepts) {
		HashSet<String> childrenStr = treeLabelData.getTreeChildrenIndex().get(rootStr);
		HashSet<ConceptTreeNode> children = new HashSet<ConceptTreeNode>();
		if (childrenStr != null && depth < maxDepth) {
			for (String key : childrenStr) {
				ConceptTreeNode child = initializeTreeWithConceptVector(key, depth + 1, isBreakConcepts);
				children.add(child);
			}
		}
		if (isDebug) {
			System.out.println("  initialize tree node " + rootStr + ", in depth: " + depth);
		}
		ConceptTreeNode node = null;
		String conceptStr = "";
		try {
			List<String> conceptsList = new ArrayList<String>();
			List<Double> scores = new ArrayList<Double>();
			conceptStr = this.conceptVectorStringHash.get(rootStr);
			if ((rootStr.equals("root") == false && rootStr.equals("top") == false)
					|| conceptStr != null) {
				String[] tokens = conceptStr.trim().split(";");
				for (int i = 0; i < tokens.length; ++i) {
					String[] subtokens = tokens[i].trim().split(",");
					if (subtokens.length != 2) 
						continue;
					if(subtokens[1].equals("Inc.")) {
						int a = 0;
					}
					conceptsList.add(subtokens[0].trim());
					scores.add(Double.parseDouble(subtokens[1].trim()));
				}
			} 
			node = new ConceptTreeNode(children, rootStr, depth);
			node.setLabelConcepts(conceptsList, scores, isBreakConcepts, globalConceptWeights);
			conceptVectorHash.put(rootStr, node.getVector());
			treeNodeList.add(node);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return node;
	}

	public ConceptTreeNode initializeTreeWithConceptVectorComplexESA(String rootStr, int depth, boolean isBreakConcepts) {
		HashSet<String> childrenStr = treeLabelData.getTreeChildrenIndex().get(rootStr);
		HashSet<ConceptTreeNode> children = new HashSet<ConceptTreeNode>();
		if (childrenStr != null && depth < maxDepth) {
			for (String key : childrenStr) {
				ConceptTreeNode child = initializeTreeWithConceptVectorComplexESA(key, depth + 1, isBreakConcepts);
				children.add(child);
			}
		}
		if (isDebug) {
			System.out.println("  initialize tree node " + rootStr + ", in depth: " + depth);
		}
		ConceptTreeNode node = null;
		String conceptStr = "";
		try {
			conceptStr = this.conceptVectorStringHash.get(rootStr);
			List<ConceptData> cl= new ArrayList<ConceptData>();
			if ((rootStr.equals("root") == false && rootStr.equals("top") == false)
					|| conceptStr != null) {
				String[] tokens = conceptStr.trim().split(";");
				
				for (int i = 0; i < tokens.length; ++i) {
					String[] subTokens = tokens[i].split(",");
					String id = subTokens[0];
					double value = Double.parseDouble(subTokens[1]);
					//System.out.println(id+": "+value+"\n");
					ConceptData concept = new ConceptData(id + "", value);
					cl.add(concept);
				}
			} 
			node = new ConceptTreeNode(children, rootStr, depth);
			node.setconceptsList(cl);
			conceptVectorHash.put(rootStr, node.getVector());
			treeNodeList.add(node);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return node;
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	// Initialize tree without concepts
	///////////////////////////////////////////////////////////////////////////////////////////
	
	public void dumpTree (String filePath) {
		try {
			FileWriter writer = new FileWriter(filePath);
			writer.write("none" + "\t" 
					+ root.getLabelString() + "\t" 
					+ root.getLabelDescriptioinString() + "\t" 
					+ root.getVector().toString() + "\n\r");
			dumpTree (root, writer);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public void dumpTreeComplexESA (String filePath) {
		try {
			FileWriter writer = new FileWriter(filePath);
			writer.write("none" + "\t" 
					+ root.getLabelString() + "\t" 
					+ root.getLabelDescriptioinString() + "\t" 
					+ getComplexESA(root.getLabelDescriptioinString()) + "\n\r");
			dumpTreeComplexESA (root, writer);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void dumpTree (ConceptTreeNode root, FileWriter writer) {
		for (ConceptTreeNode child : root.getChildren()) {
			try {
				writer.write(root.getLabelString() + "\t" 
						+ child.getLabelString() + "\t" 
						+ child.getLabelDescriptioinString() + "\t" 
						+ child.getVector().toString() + "\n\r");
				dumpTree (child, writer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void dumpTreeComplexESA (ConceptTreeNode root, FileWriter writer) {
		for (ConceptTreeNode child : root.getChildren()) {
			try {
				writer.write(root.getLabelString() + "\t" 
						+ child.getLabelString() + "\t" 
						+ child.getLabelDescriptioinString() + "\t" 
						+ getComplexESA(child.getLabelDescriptioinString()) + "\n\r");
				dumpTreeComplexESA (child, writer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	public String getComplexESA(String s) throws Exception{
		List<ConceptData> conceptList = esa.retrieveConcepts(s, 500,  "tfidfVector");
		String str = "";

		for(ConceptData data:conceptList){
			str +=data.concept+","+data.score+";";
		}
		return str;
	}
	
	public void conceptualizeTreeLabels (ConceptTreeNode rootNode, boolean isBreakConcepts) {
		Set<ConceptTreeNode> children = rootNode.getChildren();
		for (ConceptTreeNode child : children) {
			conceptualizeTreeLabels(child, isBreakConcepts);
		}

		if (isDebug) {
			System.out.println("  Conceptualize tree node: " + rootNode.getLabelString() + " in depth: " + rootNode.getDepth());
			System.out.println("    Node description: " + rootNode.getLabelDescriptioinString());
		}
		
		List<String> conceptsList = new ArrayList<String>();
		List<Double> scores = new ArrayList<Double>();
		if (representationType.equals("complex") ) {
			List<ConceptData> concepts = null;
			try {
				concepts = esa.retrieveConcepts(rootNode.getLabelDescriptioinString(), this.numConcepts, ClassifierConstant.complexVectorType);
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int i = concepts.size() - 1; i >= 0; i--) {
				conceptsList.add(concepts.get(i).concept + "");
				scores.add(concepts.get(i).score);
			}
		} else if (representationType.equals("simple") ) {
			List<ConceptData> concepts = null;
			try {
				concepts = esa.retrieveConcepts(rootNode.getLabelDescriptioinString(), this.numConcepts);
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int i = 0; i < concepts.size(); i++) {
				conceptsList.add(concepts.get(i).concept + "");
				scores.add(concepts.get(i).score);
			}
		} else if (representationType.equals("wordDist") ) {
			HashMap<Integer, Double>  features = word2vec.getConceptVectorBasedonSegmentation(rootNode.getLabelDescriptioinString(), true);
			for (Integer i : features.keySet()) {
				conceptsList.add(i + "");
				scores.add(features.get(i));
			}
		} else if (representationType.equals("wordDistSimple") ) {
			double[] vec = word2vec.getDenseVectorSimpleAverage(rootNode.getLabelDescriptioinString());
			for (int i = 0; i < vec.length; ++i) {
				conceptsList.add(i + "");
				scores.add(vec[i]);
			}
		} else {
			List<ConceptData> concepts = null;
			try {
				concepts = esa.retrieveConcepts(rootNode.getLabelDescriptioinString(), this.numConcepts);
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int i = 0; i < concepts.size(); i++) {
				conceptsList.add(concepts.get(i).concept + "");
				scores.add(concepts.get(i).score);
			}
		}
		rootNode.setLabelConcepts(conceptsList, scores, isBreakConcepts, globalConceptWeights);
	}
	
	public void aggregateChildrenDescription (ConceptTreeNode rootNode) {
		Set<ConceptTreeNode> children = rootNode.getChildren();
		for (ConceptTreeNode child : children) {
			aggregateChildrenDescription(child);
			String newLabelDescriptionStr = "";
			if (child.getLabelDescriptioinString() != null) {
				newLabelDescriptionStr = rootNode.getLabelDescriptioinString() + " " 
												+ child.getLabelDescriptioinString() + " ";
			}
			rootNode.setLabelDescriptionString(newLabelDescriptionStr);
		}
		if (isDebug) {
			System.out.println("  Aggregate children description: " + rootNode.getLabelString() + " in depth: " + rootNode.getDepth());
		}
	}
	
	// this should be called after treeIndex has been filled
	public ConceptTreeNode initializeTree(String rootStr, int depth) {
		HashSet<String> childrenStr = treeLabelData.getTreeChildrenIndex().get(rootStr);
		HashSet<ConceptTreeNode> children = new HashSet<ConceptTreeNode>();
		String nodeDescription = "";
		try {
			if (childrenStr != null && depth < maxDepth) {
				for (String key : childrenStr) {
					ConceptTreeNode child = initializeTree(key, depth+1);
					children.add(child);
				}
			}
			if (isDebug) {
				System.out.println("  Initialize tree node: " + rootStr + ", in depth: " + depth);
			}
			if (treeLabelData.getTreeLabelNameHashMap().get(rootStr) != null
					&& treeLabelData.getTreeLabelNameHashMap().get(rootStr).equals("no description") == false) {
				nodeDescription = treeLabelData.getTreeLabelNameHashMap().get(rootStr);
			}
			if (treeLabelData.getTreeTopicDescription().get(rootStr) != null) {
				nodeDescription = nodeDescription + "  " + treeLabelData.getTreeTopicDescription().get(rootStr) + "    ";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		ConceptTreeNode node = new ConceptTreeNode(children, rootStr, depth);
		node.setLabelDescriptionString(nodeDescription);
		treeNodeList.add(node);
		return node;
	}


	

}
