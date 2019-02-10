package edu.illinois.cs.cogcomp.classification.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.densification.representation.SparseSimilarityCondensation;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.DiskBasedComplexESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.MemoryBasedESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.simple.SimpleESALocal;

/**
 * Shaoshi Ling
 * sling3@illinois.edu
 */

public class ClassificationESA extends Classification{
	DiskBasedComplexESA esa=new DiskBasedComplexESA();
	
	@Override
	public void MultiText(String labelsets, String contentInfoFile,String LabelResult) throws Exception{
		readContentData(contentInfoFile);
		String treeConceptFile = "data/LabelSets/tree."+labelsets+".complex.esa.concepts.newrefine.500";
		ConceptTreeTopDownML tree = new ConceptTreeTopDownML(labelsets, "complex", conceptWeights, false);	

		System.out.println("process tree...");
		tree.readLabelTreeFromDump(treeConceptFile, ClassifierConstant.isBreakConcepts);
		ConceptTreeNode rootNode = tree.initializeTreeWithConceptVectorComplexESA("root", 0, ClassifierConstant.isBreakConcepts);
		tree.setRootNode(rootNode);
		System.out.println("process tree finished");
		
		for (int i=0;i<Docnum;i++){
			System.out.print("Docid "+i+" content is "+contentMap.get(Integer.toString(i))+"\n");
			String content = contentMap.get(Integer.toString(i));
			List<String> conceptsList = new ArrayList<String>();
			List<Double> scores = new ArrayList<Double>();
			HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth;
			List<ConceptData> concepts = esa.retrieveConcepts(content, 500, "tfidfVector");
			for (int j = 0; j < concepts.size(); j++) {
				conceptsList.add(concepts.get(j).concept + "");
				scores.add(concepts.get(j).score);
			}
			HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
			SparseVector document = new SparseVector(conceptsList, scores, ClassifierConstant.isBreakConcepts, conceptWeights);
			labelResultsInDepth = tree.labelDocumentML(document);
	
			
			int size=labelResultsInDepth.size()-1;
			String label=labelResultsInDepth.get(size).get(0).labelName;
			labelresults.put(Integer.toString(i),label);
			resultsInDpeth.put(i,labelResultsInDepth)	;	
			System.out.print("Docid "+i+" label is "+label+"\n");
	
			
		}
		writeresult(LabelResult);
	}
	
	
	
	

	
	@Override
	public void GivenText(String labelsets) throws Exception{
		String method = "complex";

		String treeConceptFile = "data/LabelSets/tree."+labelsets+"." + method + ".esa.concepts.newrefine.500";
		ConceptTreeTopDownML tree = new ConceptTreeTopDownML("Customized", method, conceptWeights, false);	

		System.out.println("process tree...");
		tree.readLabelTreeFromDump(treeConceptFile, ClassifierConstant.isBreakConcepts);
		ConceptTreeNode rootNode = tree.initializeTreeWithConceptVectorComplexESA("root", 0, ClassifierConstant.isBreakConcepts);
		tree.setRootNode(rootNode);
		System.out.println("process tree finished");
		List<String> conceptsList = new ArrayList<String>();
		List<Double> scores = new ArrayList<Double>();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String content;
		try {
			content = br.readLine();
			List<ConceptData> concepts = esa.retrieveConcepts(content, 500, "tfidfVector");
			for (int i = 0; i < concepts.size(); i++) {
				conceptsList.add(concepts.get(i).concept + "");
				scores.add(concepts.get(i).score);
			}
			HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
			SparseVector document = new SparseVector(conceptsList, scores, ClassifierConstant.isBreakConcepts, conceptWeights);
			HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth = tree.labelDocumentML(document);
			int size=labelResultsInDepth.size()-1;
			String label=labelResultsInDepth.get(size).get(0).labelName;
			resultsInDpeth.put(0,labelResultsInDepth)	;
			System.out.print("label is "+label+"\n");
			/*int size=labelResultsInDepth.size();
			for(int j=0;j<size;j++){
				System.out.print("The label name is "+labelResultsInDepth.get(j).get(0).labelName+"\n");
				
			}
			*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	

	
}
