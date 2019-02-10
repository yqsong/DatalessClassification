package edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.classification.densification.representation.SparseSimilarityCondensation;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelConceptClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.AbstractConceptTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.MemoryBasedESA;

/**
 * yqsong@illinois.edu
 */

public class ConceptTreeBottomUpML extends AbstractConceptTree implements InterfaceMultiLabelConceptClassificationTree {
	
	private static final long serialVersionUID = 1L;

	double classifierMLThreshold = ClassifierConstant.classifierMLThreshold;
	int leastK = ClassifierConstant.leastK;
	int maxK = ClassifierConstant.maxK;
	public static void main (String args[]) {
		
	}
	
	public ConceptTreeBottomUpML (String data, String method, HashMap<String, Double> conceptWeights, boolean isInitializeLucene) {
		super(data, method, conceptWeights, isInitializeLucene);
	}
	
	public void setThreshold (double threshold) {
		this.classifierMLThreshold = threshold;
	}

	@Override
	public int searchLabelDepth (String label) {
		return getLabelDepth(label);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Label documents
	///////////////////////////////////////////////////////////////////////////////////////////

	List<ConceptTreeNode> leaveHashSet = new ArrayList<ConceptTreeNode>();
	@Override
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentML(SparseVector docContent) {
		if (leaveHashSet.size() == 0) {
			getLeafNodesConcepts(root, leaveHashSet);
		}
		
		HashMap<String, Double> orgSimilarities = new HashMap<String, Double>();
		HashMap<String, Double> similarities = new HashMap<String, Double>();
		
		double maxSimilarity = 0 - Double.MAX_VALUE;
		double minSimilarity = Double.MAX_VALUE;
		for (ConceptTreeNode leaf : leaveHashSet) {
			double similarity = leaf.getVector().cosine(docContent, globalConceptWeights);
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
	
	@Deprecated
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentW2V(String docContent) {
		if (leaveHashSet.size() == 0) {
			getLeafNodesConcepts(root, leaveHashSet);
		}
		
		HashMap<String, Double> orgSimilarities = new HashMap<String, Double>();
		HashMap<String, Double> similarities = new HashMap<String, Double>();
		
		double maxSimilarity = 0 - Double.MAX_VALUE;
		double minSimilarity = Double.MAX_VALUE;
		for (ConceptTreeNode leaf : leaveHashSet) {
			String label=treeLabelData.getTreeLabelNameHashMap().get(leaf.getLabelString());
			double similarity = SparseSimilarityCondensation.similarity(this.word2vec, docContent,label);
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

	@Override
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentDense(
			SparseSimilarityCondensation vectorCondensation, String docContent) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
