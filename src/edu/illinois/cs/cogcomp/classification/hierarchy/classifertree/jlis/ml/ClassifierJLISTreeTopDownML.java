package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.jlis.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.jlis.ClassifierJLISTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.jlis.MultiClassStructureFinder;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.jlis.mc.ClassifierJLISTreeTopDownMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelContentClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultML;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.indsup.mc.LabeledMulticlassData;

/**
 * yqsong@illinois.edu
 */

public class ClassifierJLISTreeTopDownML extends ClassifierJLISTreeTopDownMC implements InterfaceMultiLabelContentClassificationTree {

	private static final long serialVersionUID = 1L;

	double classifierMLThreshold = ClassifierConstant.classifierMLThreshold;
	int leastK = ClassifierConstant.leastK;
	
	public ClassifierJLISTreeTopDownML (String data) {
		super(data);
	}
	
	@Override
	public void trainAllTreeNodes(){
		System.out.println("[Training:] all nodes... ");
		//training 
		for (int i = 0; i < this.allNodeList.size(); ++i) {
			allNodeList.get(i).trainModelForNode(C, nThreads);
		}
	}
	
	////////////////////////////////////////////////////////////////
	// Test data
	////////////////////////////////////////////////////////////////
	
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
		docLibSVMFormat = "nullLabel " + docLibSVMFormat;
		
		LabelKeyValuePair labelPair = new LabelKeyValuePair(root.getLabelString(), 1);
		labelResult.rootLabel.labelKVP = labelPair;
		labelResult.rootLabel.depth = 0;
		
		labelResult.rootLabel = retrieveLabel (docLibSVMFormat, root, labelResult.rootLabel);
		return labelResult;
	}
	
	
	public LabelResultTreeNode retrieveLabel (String docLibSVMFormat, ClassifierJLISTreeNode rootNode, LabelResultTreeNode rootLabel) {

		if (rootNode.getChildren().size() > 0) {
	        
			List<String> lines = new ArrayList<String>();
			lines.add(docLibSVMFormat);
			LabeledMulticlassData testRes = this.jlisData.readMultiClassDataLibSVMStr (lines, this.jlisData.getFeatureNum(), rootNode.getLabeledData().label_mapping);
			
			String outputLabelStr = "";
			double outputLabelScore = 0;
			
			try {
				MultiClassStructureFinder finder = (MultiClassStructureFinder) rootNode.getModel().s_finder;
				double[] prob = finder.getLossSensitiveStructureProbabilities(rootNode.getModel().wv, testRes.sp.input_list.get(0));

		        HashMap<String, ClassifierJLISTreeNode> childrenMap = new HashMap<String, ClassifierJLISTreeNode>();
		        for (ClassifierJLISTreeNode child : rootNode.getChildren()) {
		        	childrenMap.put(child.getLabelString(), child);
		        }
		        
		        HashMap<String, Double> similarities = new HashMap<String, Double>();
		        for (String label : rootNode.getModel().lab_mapping.keySet()) {
		        	similarities.put(label, prob[rootNode.getModel().lab_mapping.get(label)]);
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
						
						retrieveLabel (docLibSVMFormat, childrenMap.get(simiKey), labelNode);
					}
					labelCount++;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}
		return rootLabel;
	}
	

}
