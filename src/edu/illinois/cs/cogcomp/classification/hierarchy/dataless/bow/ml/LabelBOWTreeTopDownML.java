package edu.illinois.cs.cogcomp.classification.hierarchy.dataless.bow.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelContentClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.bow.AbstractLabelBOWTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.bow.LabelBOWNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultML;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

/**
 * yqsong@illinois.edu
 */

public class LabelBOWTreeTopDownML extends AbstractLabelBOWTree implements InterfaceMultiLabelContentClassificationTree {

	private static final long serialVersionUID = 1L;

	Random random = new Random();
	
	double classifierMLThreshold = ClassifierConstant.classifierMLThreshold;
	int leastK = ClassifierConstant.leastK;
	int maxK = ClassifierConstant.maxK;
	
	public LabelBOWTreeTopDownML (String data, HashMap<String, Double> weights) {
		super (data, weights);	
	}
	
	@Override
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentConceptML(String docConcepts) {
		return null;
	}
	
	@Override
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentContentML(String docContent) {
		String [] tokens = docContent.trim().split("\\s+");
		HashMap<String, Integer> labelHash = new HashMap<String, Integer>();
		for (int i = 0; i < tokens.length; ++i) {
			if (labelHash.containsKey(tokens[i].trim())) {
				labelHash.put(tokens[i].trim(), labelHash.get(tokens[i].trim()) + 1);
			} else {
				labelHash.put(tokens[i].trim(), 1);
			}
		}
		List<String> keyList = new ArrayList<String>(labelHash.keySet());
		List<Double> scoreList = new ArrayList<Double>();
		for (int i = 0; i < keyList.size(); ++i) {
			String key = keyList.get(i);
			double value = labelHash.get(key);
			scoreList.add(value);
		}
		SparseVector docVector = new SparseVector(keyList, scoreList, false, globalWeights);
		
		return labelDocumentFromSparseVector(docVector).processLabels();
		
	}

	public LabelResultML labelDocumentFromSparseVector (SparseVector documentConceptVector) {
		LabelResultML labelResult = new LabelResultML();
		LabelKeyValuePair labelPair = new LabelKeyValuePair(root.getLabelString(), 1);
		labelResult.rootLabel.labelKVP = labelPair;
		labelResult.rootLabel.depth = 0;
		
		labelResult.rootLabel = retrieveLabel (documentConceptVector, root, labelResult.rootLabel);
		return labelResult;
	}
	
	public LabelResultTreeNode retrieveLabel (SparseVector docVector, LabelBOWNode rootNode, LabelResultTreeNode rootLabel) {
		HashMap<String, Double> orgSimilarities = new HashMap<String, Double>();
		HashMap<String, Double> similarities = new HashMap<String, Double>();
		HashMap<String, LabelBOWNode> childrenMap = new HashMap<String, LabelBOWNode>();
		double maxSimilarity = 0 - Double.MAX_VALUE;
		double minSimilarity = Double.MAX_VALUE;
		for (LabelBOWNode child : rootNode.getChildren()) {
			double similarity = docVector.cosine(child.getVector(), globalWeights) + random.nextDouble() * 1e-10;
			orgSimilarities.put(child.getLabelString(), similarity);
			childrenMap.put(child.getLabelString(), child);
			if (similarity > maxSimilarity) {
				 maxSimilarity = similarity;
			}
			if (similarity < minSimilarity) {
				minSimilarity = similarity;
			}
		}
		
		if (minSimilarity < 0) {
			for (String simiKey : orgSimilarities.keySet()) {
				orgSimilarities.put(simiKey, orgSimilarities.get(simiKey) - minSimilarity);
			}
		}
		
		double sumSimilarity = 0;
		for (String simiKey : orgSimilarities.keySet()) {
			double value = (orgSimilarities.get(simiKey) - minSimilarity) / (maxSimilarity - minSimilarity + Double.MIN_VALUE);
			if (orgSimilarities.size() == 1) {
				value = 1;
			}
			similarities.put(simiKey, value);
			sumSimilarity += value;
		}
		for (String simiKey : similarities.keySet()) {
			similarities.put(simiKey, similarities.get(simiKey) / (sumSimilarity + Double.MIN_VALUE));
		}
		if (rootNode.getChildren().size() == 0) {
			rootLabel.isToLeaf = true;
		} else {
			rootLabel.isToLeaf = false;
		}
		TreeMap<String, Double> sortedSimilarities = HashSort.sortByValues(similarities);
		
		double ratio = 0;
		int labelCount = 0;
		if (sumSimilarity > 0) {
			for (String simiKey : sortedSimilarities.keySet()) {
				ratio += similarities.get(simiKey);
				if ((ratio < classifierMLThreshold && labelCount < maxK) || labelCount < leastK) {
					LabelKeyValuePair labelPair = new LabelKeyValuePair(simiKey, orgSimilarities.get(simiKey));
					LabelResultTreeNode labelNode = new LabelResultTreeNode();
					labelNode.labelKVP = labelPair;
					labelNode.depth = rootLabel.depth + 1;
					
					rootLabel.children.add(labelNode);
					
					retrieveLabel (docVector, childrenMap.get(simiKey), labelNode);
				}
				labelCount++;
				if (labelCount >= maxK) {
					break;
				}
			}
		}
		
		return rootLabel;
	}


}
