package edu.illinois.cs.cogcomp.classification.hierarchy.dataless.bow.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelContentClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.bow.AbstractLabelBOWTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.bow.LabelBOWNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

/**
 * yqsong@illinois.edu
 */

public class LabelBOWTreeBottomUpML extends AbstractLabelBOWTree implements InterfaceMultiLabelContentClassificationTree {

	private static final long serialVersionUID = 1L;

	double classifierMLThreshold = ClassifierConstant.classifierMLThreshold;
	int leastK = ClassifierConstant.leastK;
	int maxK = ClassifierConstant.maxK;

	
	public LabelBOWTreeBottomUpML (String data, HashMap<String, Double> weights) {
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
		
		return labelDocument(docVector);
	}

	

	List<LabelBOWNode> leaveHashSet = new ArrayList<LabelBOWNode>();
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocument(SparseVector docContent) {
		if (leaveHashSet.size() == 0) {
			getLeafNodesLabels(root, leaveHashSet);
		}
		
		HashMap<String, Double> orgSimilarities = new HashMap<String, Double>();
		HashMap<String, Double> similarities = new HashMap<String, Double>();
		
		double maxSimilarity = 0 - Double.MAX_VALUE;
		double minSimilarity = Double.MAX_VALUE;
		for (LabelBOWNode leaf : leaveHashSet) {
			double similarity = leaf.getVector().cosine(docContent, globalWeights);
			orgSimilarities.put(leaf.getLabelString(), similarity);
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
		
		HashMap<Integer, List<LabelKeyValuePair>> depthLabelMap = new HashMap<Integer, List<LabelKeyValuePair>>();

		TreeMap<String, Double> sortedSimilarities = HashSort.sortByValues(similarities);
		double ratio = 0;
		int labelCount = 0;
		for (String simiKey : sortedSimilarities.keySet()) {
			ratio += similarities.get(simiKey);
			if ((ratio < classifierMLThreshold && labelCount < maxK) || labelCount < leastK) {

				String labelStr = simiKey;
				while (labelStr != null) {
					int depth = getLabelDepth (labelStr);
					if (depthLabelMap.containsKey(depth) == false) {
						depthLabelMap.put(depth, new ArrayList<LabelKeyValuePair>());
					}
					LabelKeyValuePair labelPair = new LabelKeyValuePair(labelStr, orgSimilarities.get(simiKey));
					depthLabelMap.get(depth).add(labelPair);
					
					labelStr = treeLabelData.getTreeParentIndex().get(labelStr);
				}
				
			}
			labelCount++;
		}
		return depthLabelMap;
	}

}
