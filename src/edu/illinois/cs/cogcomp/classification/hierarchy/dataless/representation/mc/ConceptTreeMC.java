package edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.mc;

import java.util.HashMap;

import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiClassClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.AbstractConceptTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;

/**
 * yqsong@illinois.edu
 */

public class ConceptTreeMC extends AbstractConceptTree implements InterfaceMultiClassClassificationTree {
	
	private static final long serialVersionUID = 1L;

	public static void main (String args[]) {
		
	}
	

	public ConceptTreeMC (String data, String method, HashMap<String, Double> conceptWeights, boolean isInitializeLucene) {
		super(data, method, conceptWeights, isInitializeLucene);
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	// Label documents
	///////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public LabelResultMC labelDocument(String docContent) {
		return labelDocument (docContent, false);
	}

	
	public LabelResultMC labelDocument (String document, boolean isBreakConcepts) {
		LabelResultMC labelResult = new LabelResultMC();
		if (isDebug) {
			System.out.println("process document with concepts...");
		}
		SparseVector docConceptVector = convertDocToVector (document, isBreakConcepts);
		if (isDebug) {
			System.out.println("process document finished");
		}

		labelResult = labelDocument (docConceptVector);
		return labelResult;
	}
	
	public LabelResultMC labelDocument (SparseVector documentConceptVector) {
		LabelResultMC labelResult = new LabelResultMC();
		labelResult = retrieveLabel (documentConceptVector, root, labelResult);
		return labelResult;
	}
	
	public LabelResultMC retrieveLabel (SparseVector docConceptVector, ConceptTreeNode rootNode, LabelResultMC labelResult) {
		double maxValue = 0;
		ConceptTreeNode maxChild = null;
		HashMap<String, Double> similarities = new HashMap<String, Double>();
		
		double maxSimilarity = 0 - Double.MAX_VALUE;
		double minSimilarity = Double.MAX_VALUE;
		for (ConceptTreeNode child : rootNode.getChildren()) {
			double similarity = docConceptVector.cosine(child.getVector(), globalConceptWeights);
			similarities.put(child.getLabelString(), similarity);
			if (similarity > maxValue) {
				maxValue = similarity;
				maxChild = child;
			}
			
			if (similarity > maxSimilarity) {
				 maxSimilarity = similarity;
			}
			if (similarity < minSimilarity) {
				minSimilarity = similarity;
			}
		}
		
		double sumSimilarity = 0;
		for (String simiKey : similarities.keySet()) {
			double value = (similarities.get(simiKey) - minSimilarity) / (maxSimilarity - minSimilarity + Double.MIN_VALUE);
			if (similarities.size() == 1) {
				value = 1;
			}
			similarities.put(simiKey, value);
			sumSimilarity += value;
		}
		for (String simiKey : similarities.keySet()) {
			similarities.put(simiKey, similarities.get(simiKey) / (sumSimilarity + Double.MIN_VALUE));
		}
		
		if (rootNode.getChildren().size() == 0) {
			labelResult.isToLeaf = true;
		} else {
			labelResult.isToLeaf = false;
		}
		if (maxChild != null) {
			labelResult.labels.add(new LabelKeyValuePair(maxChild.getDepth() + ":" + maxChild.getLabelString(), maxValue));
			retrieveLabel (docConceptVector, maxChild, labelResult);
		}
		return labelResult;
	}



	

}
