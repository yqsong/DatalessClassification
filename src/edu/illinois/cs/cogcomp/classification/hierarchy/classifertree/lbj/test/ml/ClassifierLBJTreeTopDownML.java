package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.ml;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import LBJ2.classify.Score;
import LBJ2.classify.ScoreSet;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.ClassifierLBJTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.mc.ClassifierLBJTreeTopDownMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelContentClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultML;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

/**
 * yqsong@illinois.edu
 */

public class ClassifierLBJTreeTopDownML extends ClassifierLBJTreeTopDownMC implements InterfaceMultiLabelContentClassificationTree{

	private static final long serialVersionUID = 1L;

	double classifierMLThreshold = ClassifierConstant.classifierMLThreshold;
	int leastK = ClassifierConstant.leastK;
	
	public ClassifierLBJTreeTopDownML (String data) {
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
		
	public LabelResultML labelDocumentTopDown (String docLibSVMFormat) {
		LabelResultML labelResult = new LabelResultML();
//		docLivSVMFormat = "nullLabel " + docLivSVMFormat;
		
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

		
		LabelKeyValuePair labelPair = new LabelKeyValuePair(root.getLabelString(), 1);
		labelResult.rootLabel.labelKVP = labelPair;
		labelResult.rootLabel.depth = 0;
		
		labelResult.rootLabel = retrieveLabel (dataSample, root, labelResult.rootLabel);
		return labelResult;
	}
	
	public LabelResultTreeNode retrieveLabel (Object[] dataSample, ClassifierLBJTreeNode rootNode, LabelResultTreeNode rootLabel) {

		if (rootNode != null && rootNode.getChildren() != null && rootNode.getChildren().size() > 0) {
	        
//			System.out.println("       [Debug:] track classification: " + rootNode.getLabelString());
//			
//			System.out.println("       [Debug:] dataSample: " + (dataSample == null));
//			System.out.println("       [Debug:] rootNode.getModel().getLexicon(): " + (rootNode.getModel().getLexicon() == null));
//			System.out.println("       [Debug:] rootNode.getModel().getLabelLexicon(): " + (rootNode.getModel().getLabelLexicon() == null));

//			FeatureVector vector =
//			        new FeatureVector((Object[]) dataSample, rootNode.getModel().getLexicon(), rootNode.getModel().getLabelLexicon());
//			vector.sort();

//			System.out.println("       [Debug:] track classification: discreteValue()=" + rootNode.getModel().discreteValue(vector));
			
			ScoreSet scoreSet = rootNode.getModel().scoresExplicit((int[])dataSample[0], (double[])dataSample[1]);//only for Naivebayes
//			ScoreSet scoreSet = rootNode.getModel().scores(dataSample);
			Score[] scoreArray = scoreSet.toArray();
	        double[] decValues = new double[scoreArray.length];
	        String[] labels = new String[scoreArray.length];
			for (int i = 0; i < scoreArray.length; ++i) {
				labels[i] = scoreArray[i].value;
				decValues[i] = scoreArray[i].score;

//				System.out.println("       [Debug:] classification: " + labels[i] + ", " + decValues[i]);
				 
			}
       
	        HashMap<String, ClassifierLBJTreeNode> childrenMap = new HashMap<String, ClassifierLBJTreeNode>();
	        for (ClassifierLBJTreeNode child : rootNode.getChildren()) {
	        	childrenMap.put(child.getLabelString(), child);
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
					LabelKeyValuePair labelPair = new LabelKeyValuePair(simiKey, similarities.get(simiKey));
					LabelResultTreeNode labelNode = new LabelResultTreeNode();
					labelNode.labelKVP = labelPair;
					labelNode.depth = rootLabel.depth + 1;
					
					rootLabel.children.add(labelNode);
					
					retrieveLabel (dataSample, childrenMap.get(simiKey), labelNode);
				}
				labelCount++;
			}
		}
		return rootLabel;
	}

}
