package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.jlis.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.jlis.MultiClassStructureFinder;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.jlis.mc.ClassifierJLISTreeBottomUpMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelContentClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.indsup.mc.LabeledMultiClassStructure;
import edu.illinois.cs.cogcomp.indsup.mc.LabeledMulticlassData;

/**
 * yqsong@illinois.edu
 */

public class ClassifierJLISTreeBottomUpML extends ClassifierJLISTreeBottomUpMC implements InterfaceMultiLabelContentClassificationTree {

	private static final long serialVersionUID = 1L;

	double classifierMLThreshold = ClassifierConstant.classifierMLThreshold;
	int leastK = ClassifierConstant.leastK;

	public ClassifierJLISTreeBottomUpML (String data) {
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
		LabelResultMC labelResult = new LabelResultMC();

		docLibSVMFormat = "nullLabel " + docLibSVMFormat;
		
		List<String> lines = new ArrayList<String>();
		lines.add(docLibSVMFormat);
		LabeledMulticlassData testRes = this.jlisData.readMultiClassDataLibSVMStr (lines, this.jlisData.getFeatureNum(), globalLeafLabelIDMap);
		
		labelResult.isToLeaf = false;
		
		LabeledMultiClassStructure prediction = null;
		String outputLabelStr = "";
		double outputLabelScore = 0;
		
		HashMap<Integer, List<LabelKeyValuePair>> depthLabelMap = new HashMap<Integer, List<LabelKeyValuePair>>();
		try {
			MultiClassStructureFinder finder = (MultiClassStructureFinder) overallModel.s_finder;
			double[] prob = finder.getLossSensitiveStructureProbabilities(overallModel.wv, testRes.sp.input_list.get(0));

	        HashMap<String, Double> similarities = new HashMap<String, Double>();
	        for (String label : globalLeafLabelIDMap.keySet()) {
	        	similarities.put(label, prob[globalLeafLabelIDMap.get(label)]);
	        }
			TreeMap<String, Double> sortedSimilarities = HashSort.sortByValues(similarities);
			double ratio = 0;
			int labelCount = 0;
			for (String simiKey : sortedSimilarities.keySet()) {
				ratio += similarities.get(simiKey);
				if (ratio < classifierMLThreshold || labelCount < leastK) {
					LabelKeyValuePair labelPair = new LabelKeyValuePair(simiKey, similarities.get(simiKey));

					String labelStr = outputLabelStr;
					while (labelStr != null) {
						int depth = getLabelDepth (simiKey);
						if (depthLabelMap.containsKey(depth) == false) {
							depthLabelMap.put(depth, new ArrayList<LabelKeyValuePair>());
						}
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
