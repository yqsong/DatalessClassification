package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import LBJ2.classify.Score;
import LBJ2.classify.ScoreSet;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.mc.ClassifierLBJTreeBottomUpMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelContentClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

/**
 * yqsong@illinois.edu
 */

public class ClassifierLBJTreeBottomUpML extends ClassifierLBJTreeBottomUpMC implements InterfaceMultiLabelContentClassificationTree{
	
	private static final long serialVersionUID = 1L;
	
	double classifierMLThreshold = ClassifierConstant.classifierMLThreshold;
	int leastK = ClassifierConstant.leastK;
	
	public ClassifierLBJTreeBottomUpML (String data) {
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
	
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentML (String docLibSVMFormat) {
	
		HashMap<Integer, List<LabelKeyValuePair>> depthLabelMap = new HashMap<Integer, List<LabelKeyValuePair>>();
		try {
			
			// initialize data for lbj
			String[] tokens = docLibSVMFormat.split(" ");
			int[] indexArray = new int[tokens.length];
			double[] valueArray = new double[tokens.length];
			for (int i = 0; i < tokens.length; ++i) {
				String[] subTokens = tokens[i].trim().split(":");
				if (subTokens.length < 2) {
					continue;
				}
				int index = Integer.parseInt(subTokens[0].trim());
				double value = Double.parseDouble(subTokens[1].trim());
				indexArray[i] = index;
				valueArray[i] = value;
			}

			int[] labelIndexArray = new int[1];
			double[] labelValueArray = new double[1];
			labelIndexArray[0] = 0;
			labelValueArray[0] = 0;
			
			Object[] dataSample = new Object[4];
			dataSample[0] = indexArray;
			dataSample[1] = valueArray;
			dataSample[2] = labelIndexArray;
			dataSample[3] = labelValueArray;

//			FeatureVector vector =
//			        new FeatureVector((Object[]) dataSample, this.model.getLexicon(), this.model.getLabelLexicon());
//			vector.sort();
	        
			ScoreSet scoreSet = model.scoresExplicit(indexArray, valueArray);//only for Naivebayes
//			ScoreSet scoreSet = model.scores(dataSample);
			Score[] scoreArray = scoreSet.toArray();
	        double[] decValues = new double[scoreArray.length];
	        String[] labels = new String[scoreArray.length];
			for (int i = 0; i < scoreArray.length; ++i) {
				labels[i] = scoreArray[i].value;
				decValues[i] = scoreArray[i].score;
//				System.out.println("       [Debug:] classification: " + labels[i] + ", " + decValues[i]);
			}
			

			HashMap<String, Double> similarities = new HashMap<String, Double>();
	        for (int i = 0; i < decValues.length; ++i) {
	        	similarities.put(labels[i], decValues[i]);
	        }

			TreeMap<String, Double> sortedSimilarities = HashSort.sortByValues(similarities);
			double ratio = 0;
			int labelCount = 0;
			for (String simiKey : sortedSimilarities.keySet()) {
				ratio += similarities.get(simiKey);
				if (ratio < classifierMLThreshold || labelCount < leastK) {

					String labelStr = simiKey;
					while (labelStr != null) {
						int depth = getLabelDepth (labelStr);
						if (depthLabelMap.containsKey(depth) == false) {
							depthLabelMap.put(depth, new ArrayList<LabelKeyValuePair>());
						}
						LabelKeyValuePair labelPair = new LabelKeyValuePair(labelStr, similarities.get(simiKey));
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
