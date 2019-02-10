package edu.illinois.cs.cogcomp.classification.hierarchy.run.sim;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelConceptClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.EvalResults;

public class SimilarityInTree {
	public static HashMap<String, EvalResults> dumpSimilarities (
			InterfaceMultiLabelConceptClassificationTree tree,
			HashMap<String, String> docIdContentMap, 
			HashMap<String, SparseVector> docIdConceptMap, 
			HashMap<String, HashSet<String>> topicDocMap,
			HashMap<String, HashSet<String>> docTopicMap,
			String outputDataFilePath,
			int depth, int topK) {
		// classification
		HashMap<String, EvalResults> resultsMap = new HashMap<String, EvalResults>();
		try {
			FileWriter writer = new FileWriter(outputDataFilePath);
			int count = 0;
			
			for (String docID : docIdContentMap.keySet()) {
			
				// get vector
				SparseVector document = docIdConceptMap.get(docID);
				// process document with labels

				HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth = tree.labelDocumentML(document);
				
				HashSet<String> trueLabelSet = docTopicMap.get(docID);
				if (trueLabelSet == null) {
					System.out.println(docID);
					trueLabelSet = new HashSet<String>();
				}
				HashMap<Integer, HashSet<String>> trueDepthsLabels = new HashMap<Integer, HashSet<String>>();
				for (String label : trueLabelSet) {
					int d = tree.searchLabelDepth(label);
					if (trueDepthsLabels.containsKey(d) == false) {
						trueDepthsLabels.put(d, new HashSet<String>());
					}
					trueDepthsLabels.get(d).add(label);
				}
				
				// true labels
				HashSet<String> trueLabels = trueDepthsLabels.get(depth);
				//classified labels
				List<LabelKeyValuePair> classifiedLabelScoreList = labelResultsInDepth.get(depth);
				if (classifiedLabelScoreList == null) {
					classifiedLabelScoreList = new ArrayList<LabelKeyValuePair>();
				}
				
				
				writer.write(docID + "\t" + docIdContentMap.get(docID).toLowerCase() + "\t");

				if (trueLabels == null) {
					writer.write(depth + "," + "NULL;");
				} else {
					for (String trueLabel : trueLabels) {
						writer.write(depth + "," + trueLabel + ";");
					}
				}
				writer.write("\t");
				HashSet<String> classifiedLabels = new HashSet<String>();
				for (int i = 0; i < Math.min(topK, classifiedLabelScoreList.size()); ++i) {
					classifiedLabels.add(classifiedLabelScoreList.get(i).getLabel());
					writer.write(depth + "," + classifiedLabelScoreList.get(i).getLabel() + "," + classifiedLabelScoreList.get(i).getScore() + ";");
				}
				writer.write("\n\r");

				count++;
				if (count % 1000 == 0) {
					System.out.println("Classified " + count + " documents ...");
				}
			}
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultsMap;
	}
}
