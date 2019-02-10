package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.mc.ClassifierLibLinearTreeBottomUpMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelContentClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

/**
 * yqsong@illinois.edu
 */

public class ClassifierLibLinearTreeBottomUpML extends ClassifierLibLinearTreeBottomUpMC implements InterfaceMultiLabelContentClassificationTree{

	private static final long serialVersionUID = 1L;
	
	double classifierMLThreshold = ClassifierConstant.classifierMLThreshold;
	int leastK = ClassifierConstant.leastK;
	int maxK = ClassifierConstant.maxK;

	public ClassifierLibLinearTreeBottomUpML (String data) {
		super(data);
	}

	@Override
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentConceptML(String docConcepts) {
		String docLibSVMFormat = this.jlisData.convertTestDocConceptToTFIDF (docConcepts, true, true);
		return labelDocumentML (docLibSVMFormat);
	}
	
	@Override
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentContentML (String docContent) {
		String docLibSVMFormat = this.jlisData.convertTestDocContentToTFIDF (docContent, true, true);
		return labelDocumentML (docLibSVMFormat);
	}
	
	
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentML (String docLivSVMFormat) {
	
		HashMap<Integer, List<LabelKeyValuePair>> depthLabelMap = new HashMap<Integer, List<LabelKeyValuePair>>();
		try {
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
	        
			
	        double[] decValues = new double[globalLeafLabelIDMap.size()];
	        double[] transformedValues = new double[decValues.length];
	        double yTest = Linear.predictValues(overallModel, xTest, decValues);
	        int outputLabel = (int) yTest;

	        int nr_class = decValues.length;
	        int nr_w;
	        if (nr_class == 2)
	            nr_w = 1;
	        else
	            nr_w = nr_class;

	        //a key problem of original svm prob output	Output a lot of zeroes when lack of training data.        
	        for (int i = 0; i < nr_w; i++) {
	        	transformedValues[i] = 1 / (1 + Math.exp(-decValues[i] + Math.random() * 1e-10));
	        }
	        
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

	        
			HashMap<String, Double> orgSimilarities = new HashMap<String, Double>();
	        HashMap<String, Double> similarities = new HashMap<String, Double>();
			double maxSimilarity = 0 - Double.MAX_VALUE;
			double minSimilarity = Double.MAX_VALUE;
			
			for (String label : globalLeafLabelIDMap.keySet()) {
	        	
				double similarity = decValues[globalLeafLabelIDMap.get(label)];
	        	orgSimilarities.put(label, similarity);
	        	similarities.put(label,  transformedValues[globalLeafLabelIDMap.get(label)]);
	        	
				if (similarity > maxSimilarity) {
					 maxSimilarity = similarity;
				}
				if (similarity < minSimilarity) {
					minSimilarity = similarity;
				}	
	        }
			
			if (minSimilarity < 0) {
				for (String simiKey : orgSimilarities.keySet()) {
					orgSimilarities.put(simiKey, orgSimilarities.get(simiKey) - minSimilarity + Double.MIN_VALUE);
				}
			}
			
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
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return depthLabelMap;
	}




}
