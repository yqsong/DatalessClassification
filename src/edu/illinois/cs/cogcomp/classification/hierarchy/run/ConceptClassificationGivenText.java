package edu.illinois.cs.cogcomp.classification.hierarchy.run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.representation.esa.simple.SimpleESALocal;

public class ConceptClassificationGivenText {
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
	
	public ConceptClassificationGivenText() throws Exception{
		String data = "20newsgroups";
		String method = "simple";
		SimpleESALocal esa = new SimpleESALocal();
		int numConcepts=500;
		String treeConceptFile = "data/20newsgroups/output_new/tree.WikiCate.simple.esa.concepts.newrefine.500";
		ConceptTreeBottomUpML tree = new ConceptTreeBottomUpML(data, method, conceptWeights, false);
		//	ConceptTreeTopDownML tree = new ConceptTreeTopDownML(data, method, conceptWeights, false);	

		System.out.println("process tree...");
		tree.readLabelTreeFromDump(treeConceptFile, ClassifierConstant.isBreakConcepts);
		ConceptTreeNode rootNode = tree.initializeTreeWithConceptVector("root", 0, ClassifierConstant.isBreakConcepts);
		tree.setRootNode(rootNode);
		System.out.println("process tree finished");
		List<String> conceptsList = new ArrayList<String>();
		List<Double> scores = new ArrayList<Double>();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String content;
		try {
			content = br.readLine();
			List<ConceptData> concepts = esa.getConcepts(numConcepts, content);
			for (int i = 0; i < concepts.size(); i++) {
				conceptsList.add(concepts.get(i).concept + "");
				scores.add(concepts.get(i).score);
			}
			HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
			SparseVector document = new SparseVector(conceptsList, scores, ClassifierConstant.isBreakConcepts, conceptWeights);
			HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth = tree.labelDocumentML(document);
			int size=labelResultsInDepth.size()-1;
			for(int j=0;j<labelResultsInDepth.get(size).size();j++){
				System.out.print("The label name is "+labelResultsInDepth.get(size).get(j).labelName+"\n");
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void main (String[] args) throws Exception {
		new  ConceptClassificationGivenText();
	}
}
