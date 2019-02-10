package edu.illinois.cs.cogcomp.classification.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.densification.representation.SparseSimilarityCondensation;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

/**
 * Shaoshi Ling
 * sling3@illinois.edu
 */

public class ClassificationDenseESA extends Classification{
	
	String matchingType = SparseSimilarityCondensation.matchingTypes[0];
	double sig = 0.05;
	SparseSimilarityCondensation vectorCondensation = new SparseSimilarityCondensation(matchingType, 0.85, sig, false);
	
	@Override
	public void MultiText(String labelsets, String contentInfoFile,String LabelResult) throws Exception{
		readContentData(contentInfoFile);
		String method = "simple";
		String treeConceptFile = "data/LabelSets/tree."+labelsets+".complex.esa.concepts.newrefine.500";
		ConceptTreeTopDownML tree = new ConceptTreeTopDownML(labelsets, method, conceptWeights, false);	

		System.out.println("process tree...");
		tree.readLabelTreeFromDump(treeConceptFile, ClassifierConstant.isBreakConcepts);
		ConceptTreeNode rootNode = tree.initializeTreeWithConceptVectorComplexESA("root", 0, ClassifierConstant.isBreakConcepts);
		tree.setRootNode(rootNode);
		System.out.println("process tree finished");
		for (int i=0;i<Docnum;i++){
			System.out.print("Docid "+i+" content is "+contentMap.get(Integer.toString(i))+"\n");
			String content = contentMap.get(Integer.toString(i));
			HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth;
			labelResultsInDepth = tree.labelDocumentDense(vectorCondensation,content);
			int size=labelResultsInDepth.size()-1;
			String label=labelResultsInDepth.get(size).get(0).labelName;
			labelresults.put(Integer.toString(i),label);
			resultsInDpeth.put(i,labelResultsInDepth)	;
			System.out.print("Docid "+i+" label is "+label+"\n");
	
			
		}
		//writeresult(LabelResult);
	}

	@Override
	public void GivenText(String labelsets) throws Exception {
		String method = "simple";
		String treeConceptFile = "data/LabelSets/tree."+labelsets+".complex.esa.concepts.newrefine.500";
		ConceptTreeTopDownML tree = new ConceptTreeTopDownML("Customized", method, conceptWeights, false);	

		System.out.println("process tree...");
		tree.readLabelTreeFromDump(treeConceptFile, ClassifierConstant.isBreakConcepts);
		ConceptTreeNode rootNode = tree.initializeTreeWithConceptVector("root", 0, ClassifierConstant.isBreakConcepts);
		tree.setRootNode(rootNode);
		System.out.println("process tree finished");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String content;
		try {
			content = br.readLine();
			HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth;
			labelResultsInDepth = tree.labelDocumentDense(vectorCondensation,content);
			int size=labelResultsInDepth.size();
			for(int j=0;j<size;j++){
				System.out.print("The label name is "+labelResultsInDepth.get(j).get(0).labelName+"\n");
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
