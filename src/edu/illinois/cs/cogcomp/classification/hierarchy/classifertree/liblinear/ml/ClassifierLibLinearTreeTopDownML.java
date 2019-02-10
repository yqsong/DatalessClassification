package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.ml;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.ClassifierLibLinearTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.mc.ClassifierLibLinearTreeTopDownMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelContentClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultML;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

/**
 * yqsong@illinois.edu
 */

public class ClassifierLibLinearTreeTopDownML extends ClassifierLibLinearTreeTopDownMC implements InterfaceMultiLabelContentClassificationTree{

	private static final long serialVersionUID = 1L;
	
	double classifierMLThreshold = ClassifierConstant.classifierMLThreshold;
	int leastK = ClassifierConstant.leastK;
	int maxK20NG = ClassifierConstant.maxK;

	public ClassifierLibLinearTreeTopDownML (String data) {
		super(data);
	}

	public void setThreshold (double threshold) {
		this.classifierMLThreshold = threshold;
	}
	
	@Override
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentConceptML(String docConcepts) {
		String docLibSVMFormat = this.jlisData.convertTestDocConceptToTFIDF (docConcepts, true, true);
		LabelResultML labelResutls = labelDocumentTopDown (docLibSVMFormat);
		HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth = labelResutls.processLabels();
		return labelResultsInDepth;
	}
	
	@Override
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentContentML (String docContent) {
		String docLibSVMFormat = this.jlisData.convertTestDocContentToTFIDF (docContent, true, true);
		LabelResultML labelResutls = labelDocumentTopDown (docLibSVMFormat);
		HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth = labelResutls.processLabels();
		return labelResultsInDepth;
	}
	
	public LabelResultML labelDocumentTopDown (String docLivSVMFormat) {
		LabelResultML labelResult = new LabelResultML();
//		docLivSVMFormat = "nullLabel " + docLivSVMFormat;
		
		String[] tokens = docLivSVMFormat.trim().split(" ");
		// initialize data for libLinear
		FeatureNode[] xTest = new FeatureNode[tokens.length];
		for (int j = 0; j < tokens.length; ++j) {
			String[] subTokens = tokens[j].trim().split(":");
			if (subTokens.length < 2) {
				xTest[j] = new FeatureNode(1, 0);
				continue;
			}
			int index = Integer.parseInt(subTokens[0].trim());
			double value = Double.parseDouble(subTokens[1].trim());
			xTest[j] = new FeatureNode(index, value);
		}
		LabelKeyValuePair labelPair = new LabelKeyValuePair(root.getLabelString(), 1);
		labelResult.rootLabel.labelKVP = labelPair;
		labelResult.rootLabel.depth = 0;
		
		labelResult.rootLabel = retrieveLabel (xTest, root, labelResult.rootLabel);
		return labelResult;
	}
	
	public LabelResultTreeNode retrieveLabel (FeatureNode[] xTest, ClassifierLibLinearTreeNode rootNode, LabelResultTreeNode rootLabel) {

//		if (rootNode.getLabelString().contains("sale")) {
//			System.out.println("[Debug:]" + rootNode.getLabelString());
//		}
		
		if (rootNode.getChildren().size() > 0) {
	        
	        double[] decValues = new double[rootNode.getLabelMapping().size()];
	        double[] transformedValues = new double[decValues.length];
//	        double yTest = Linear.predictProbability(rootNode.getModel(), xTest, decValues);
	        if (rootNode.getModel() == null) {
	        	for (int i = 0; i < decValues.length; ++i) {
	        		decValues[i] = random.nextDouble();
	        	}
	        } else {
		        double yTest = Linear.predictValues(rootNode.getModel(), xTest, decValues);
	        }
	        
	        int nr_class = decValues.length;
	        int nr_w;
	        if (nr_class == 2)
	            nr_w = 1;
	        else
	            nr_w = nr_class;

	        for (int i = 0; i < nr_w; i++)
	        	transformedValues[i] = 1 / (1 + Math.exp(-decValues[i] + Math.random() * 1e-10));


// a key problem of original svm prob output	        
	        for (int i = 0; i < nr_w; i++) {
	        	if (decValues[i] == 0) {
	        		transformedValues[i] = 0;
	        	}
	        }
	        
	        if (nr_class == 2) // for binary classification
	        	transformedValues[1] = 1. - transformedValues[0];
	        else {
	            double sum = 0;
	            for (int i = 0; i < nr_class; i++)
	                sum += transformedValues[i];

	            for (int i = 0; i < nr_class; i++)
	            	transformedValues[i] = transformedValues[i] / sum;
	        }

	        
	        HashMap<String, ClassifierLibLinearTreeNode> childrenMap = new HashMap<String, ClassifierLibLinearTreeNode>();
	        for (ClassifierLibLinearTreeNode child : rootNode.getChildren()) {
	        	childrenMap.put(child.getLabelString(), child);
	        }
	        
			HashMap<String, Double> orgSimilarities = new HashMap<String, Double>();
	        HashMap<String, Double> similarities = new HashMap<String, Double>();
			double maxSimilarity = 0 - Double.MAX_VALUE;
			double minSimilarity = Double.MAX_VALUE;

			for (String label : rootNode.getLabelMapping().keySet()) {
				double similarity = decValues[rootNode.getLabelMapping().get(label)];
	        	orgSimilarities.put(label, similarity);
	        	similarities.put(label,  transformedValues[rootNode.getLabelMapping().get(label)]);
	        	
				if (similarity > maxSimilarity) {
					 maxSimilarity = similarity;
				}
				if (similarity < minSimilarity) {
					minSimilarity = similarity;
				}	
	        }
//			if (minSimilarity < 0) {
//				for (String simiKey : orgSimilarities.keySet()) {
//					orgSimilarities.put(simiKey, orgSimilarities.get(simiKey) - minSimilarity);
//				}
//			}
			
			TreeMap<String, Double> sortedSimilarities = HashSort.sortByValues(similarities);
			double ratio = 0;
			int labelCount = 0;
			for (String simiKey : sortedSimilarities.keySet()) {
				ratio += similarities.get(simiKey);
				if ((ratio < classifierMLThreshold && labelCount < maxK20NG) || labelCount < leastK) {
					LabelKeyValuePair labelPair = new LabelKeyValuePair(simiKey, orgSimilarities.get(simiKey));
					LabelResultTreeNode labelNode = new LabelResultTreeNode();
					labelNode.labelKVP = labelPair;
					labelNode.depth = rootLabel.depth + 1;
					
					rootLabel.children.add(labelNode);
					
					retrieveLabel (xTest, childrenMap.get(simiKey), labelNode);
				}
				labelCount++;
			}
		}
		return rootLabel;
	}

}
