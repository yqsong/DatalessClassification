package edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.classification.densification.representation.SparseSimilarityCondensation;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelConceptClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.AbstractConceptTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultML;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.DiskBasedComplexESA;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.WordEmbeddingInterface;

/**
 * yqsong@illinois.edu
 */

public class ConceptTreeTopDownML extends AbstractConceptTree implements InterfaceMultiLabelConceptClassificationTree {
	
	private static final long serialVersionUID = 1L;
	double classifierMLThreshold = ClassifierConstant.classifierMLThreshold;
	int leastK = ClassifierConstant.leastK;
	int maxK = ClassifierConstant.maxK;
	
	public static void main (String args[]) {
		
	}
	
	public ConceptTreeTopDownML (String data, String method, HashMap<String, Double> conceptWeights, boolean isInitializeLucene) {
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

	@Override
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentML(
			SparseVector docContent) {
		LabelResultML labelResutls = labelDocumentFromSparseVector (docContent);
		HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth = labelResutls.processLabels();
		return labelResultsInDepth;
	}

	public LabelResultML labelDocumentFromStr (String document, boolean isBreakConcepts) {
		LabelResultML labelResult = new LabelResultML();
		if (isDebug) {
			System.out.println("process document with concepts...");
		}
		
		SparseVector docConceptVector = convertDocToVector (document, isBreakConcepts);

		if (isDebug) {
			System.out.println("process document finished");
		}

		labelResult = labelDocumentFromSparseVector (docConceptVector);
		return labelResult;
	}
	
	public LabelResultML labelDocumentFromSparseVector (SparseVector documentConceptVector) {
		LabelResultML labelResult = new LabelResultML();
		LabelKeyValuePair labelPair = new LabelKeyValuePair(root.getLabelString(), 1);
		labelResult.rootLabel.labelKVP = labelPair;
		labelResult.rootLabel.depth = 0;
		
		labelResult.rootLabel = retrieveLabel (documentConceptVector, root, labelResult.rootLabel);
		return labelResult;
	}
	
	public LabelResultTreeNode retrieveLabel (SparseVector docConceptVector, ConceptTreeNode rootNode, LabelResultTreeNode rootLabel) {
		HashMap<String, Double> orgSimilarities = new HashMap<String, Double>();
		HashMap<String, Double> similarities = new HashMap<String, Double>();
		HashMap<String, ConceptTreeNode> childrenMap = new HashMap<String, ConceptTreeNode>();
		double maxSimilarity = 0 - Double.MAX_VALUE;
		double minSimilarity = Double.MAX_VALUE;
		for (ConceptTreeNode child : rootNode.getChildren()) {
			double similarity = docConceptVector.cosine(child.getVector(), globalConceptWeights);
			orgSimilarities.put(child.getLabelString(), similarity);
			childrenMap.put(child.getLabelString(), child);
			
			if (child.getLabelString().contains("Sports")) {
				System.out.println();
			}
			
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
//				if ((ratio < classifierMLThreshold && labelCount < maxK) || labelCount < leastK) {
				if (labelCount < maxK && similarities.get(simiKey) > 0) {
					LabelKeyValuePair labelPair = new LabelKeyValuePair(simiKey, orgSimilarities.get(simiKey));
					LabelResultTreeNode labelNode = new LabelResultTreeNode();
					labelNode.labelKVP = labelPair;
					labelNode.depth = rootLabel.depth + 1;
					
					rootLabel.children.add(labelNode);
					
					retrieveLabel (docConceptVector, childrenMap.get(simiKey), labelNode);
				}
				labelCount++;
				if (labelCount >= maxK) {
					break;
				}
			}
		}
		
		return rootLabel;
	}

	@Deprecated
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentW2V(String docContent) {
		LabelResultML labelResult = new LabelResultML();
		LabelKeyValuePair labelPair = new LabelKeyValuePair(root.getLabelString(), 1);
		labelResult.rootLabel.labelKVP = labelPair;
		labelResult.rootLabel.depth = 0;
		
		labelResult.rootLabel = retrieveLabel_W2V (docContent, root, labelResult.rootLabel);
		HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth = labelResult.processLabels();
		return labelResultsInDepth;
	}

	@Deprecated
	public LabelResultTreeNode retrieveLabel_W2V (String docContent, ConceptTreeNode rootNode, LabelResultTreeNode rootLabel) {
		HashMap<String, Double> orgSimilarities = new HashMap<String, Double>();
		HashMap<String, Double> similarities = new HashMap<String, Double>();
		HashMap<String, ConceptTreeNode> childrenMap = new HashMap<String, ConceptTreeNode>();
		double maxSimilarity = 0 - Double.MAX_VALUE;
		double minSimilarity = Double.MAX_VALUE;
		for (ConceptTreeNode child : rootNode.getChildren()) {
			String label=treeLabelData.getTreeLabelNameHashMap().get(child.getLabelString());
			double similarity = SparseSimilarityCondensation.similarity(this.word2vec, docContent,label);
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
					
					retrieveLabel_W2V (docContent, childrenMap.get(simiKey), labelNode);
				}
				labelCount++;
				if (labelCount >= maxK) {
					break;
				}
			}
		}
		
		return rootLabel;
	}

	@Override
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentDense(
			SparseSimilarityCondensation vectorCondensation, String docContent) {
		LabelResultML labelResult = new LabelResultML();
		LabelKeyValuePair labelPair = new LabelKeyValuePair(root.getLabelString(), 1);
		labelResult.rootLabel.labelKVP = labelPair;
		labelResult.rootLabel.depth = 0;
		
		try {
			labelResult.rootLabel = retrieveLabel_Dense (vectorCondensation,docContent, root, labelResult.rootLabel);
		} catch (Exception e) {
			e.printStackTrace();
		}
		HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth = labelResult.processLabels();
		return labelResultsInDepth;
	}

	public LabelResultTreeNode retrieveLabel_Dense (SparseSimilarityCondensation vectorCondensation,String docContent, ConceptTreeNode rootNode, LabelResultTreeNode rootLabel) throws Exception {
		HashMap<String, Double> orgSimilarities = new HashMap<String, Double>();
		HashMap<String, Double> similarities = new HashMap<String, Double>();
		HashMap<String, ConceptTreeNode> childrenMap = new HashMap<String, ConceptTreeNode>();
		double maxSimilarity = 0 - Double.MAX_VALUE;
		double minSimilarity = Double.MAX_VALUE;
		String complexVectorType = "tfidfVector";
		for (ConceptTreeNode child : rootNode.getChildren()) {
			List<ConceptData> conceptList = esa.retrieveConcepts(docContent, 500, complexVectorType);
			HashMap<String, Double> vector = getVectorMap(conceptList);
			double normSentence = getNorm (vector);
			conceptList= child.getconceptsList();
			HashMap<String, Double> vectorTopic = getVectorMap(conceptList);
			double normTopic=getNorm (vectorTopic);
			double similarity = vectorCondensation.similarityWithMaxMatching(vectorTopic, vector, normSentence, normTopic);
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
					
					retrieveLabel_Dense (vectorCondensation,docContent, childrenMap.get(simiKey), labelNode);
				}
				labelCount++;
				if (labelCount >= maxK) {
					break;
				}
			}
		}
		
		return rootLabel;
	}
	public static HashMap<String, Double> getVectorMap (List<ConceptData> conceptList) {
		HashMap<String, Double> vectorMap = new HashMap<String, Double>();
		
		for (int i = 0; i < conceptList.size(); ++i) {
			vectorMap.put(conceptList.get(i).concept, conceptList.get(i).score);
		}
		
		return vectorMap;
	}
	public static double getNorm (HashMap<String, Double> vector) {
		double norm = 0;
		for (String key : vector.keySet()) {
			double value = vector.get(key);
			norm += value * value;
		}
		norm = Math.sqrt(norm);
		return norm;
	}

}
