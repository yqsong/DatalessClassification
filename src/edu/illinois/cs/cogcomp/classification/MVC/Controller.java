package edu.illinois.cs.cogcomp.classification.MVC;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.AbstractConceptTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ml.newsgroups.ConceptClassificationESAML;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.preparedata.newsgroups.CorpusESAConceptualization20NewsGroups;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.preparedata.newsgroups.DumpConceptTree20NewsGroups;
import edu.illinois.cs.cogcomp.classification.representation.esa.simple.SimpleESALocal;
import edu.illinois.cs.cogcomp.classification.representation.indexer.simple.WikipediaIndexing;

/**
 * Shaoshi Ling
 * sling3@illinois.edu
 */

public class Controller {
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
	View view=new View(this);
	public static String[] a;
	
	public static void main(String[] args){
		a=args;
		new Controller();
	}
	
	public void process(){
		DumpConceptTree20NewsGroups.main(a);
		CorpusESAConceptualization20NewsGroups.main(a);
		ConceptClassificationESAML.main(a);
	}
	
	public void costumized_tree(String text){
		PrintWriter writer;
		try {
			writer = new PrintWriter("data/temp.txt", "UTF-8");
			writer.println(text);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String fileTopicHierarchyPath = "";
		String fileOutputPath = "data/20newsgroups/output_new/tree.temp.simple.esa.concepts.newrefine.500";
		HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
		AbstractConceptTree tree = new ConceptTreeTopDownML("Customized", "simple", conceptWeights, true);
		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.setConceptNum(500);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
		tree.dumpTree(fileOutputPath);
		System.out.println("process tree finished");
		
	}
	
	public HashMap<Integer, List<LabelKeyValuePair>> classify(String s,String text) throws Exception{
		String data = "20newsgroups";
		String method = "simple";
		SimpleESALocal esa = new SimpleESALocal();
		int numConcepts=500;
		String treeConceptFile = "data/20newsgroups/output_new/tree."+s+".simple.esa.concepts.newrefine.500";
		ConceptTreeBottomUpML tree = new ConceptTreeBottomUpML(data, method, conceptWeights, false);
		//	ConceptTreeTopDownML tree = new ConceptTreeTopDownML(data, method, conceptWeights, false);	

		System.out.println("process tree...");
		tree.readLabelTreeFromDump(treeConceptFile, ClassifierConstant.isBreakConcepts);
		ConceptTreeNode rootNode = tree.initializeTreeWithConceptVector("root", 0, ClassifierConstant.isBreakConcepts);
		tree.setRootNode(rootNode);
		System.out.println("process tree finished");
		List<String> conceptsList = new ArrayList<String>();
		List<Double> scores = new ArrayList<Double>();
		HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth = null ;
		try {
			List<ConceptData> concepts = esa.getConcepts(numConcepts, text);
			for (int i = 0; i < concepts.size(); i++) {
				conceptsList.add(concepts.get(i).concept + "");
				scores.add(concepts.get(i).score);
			}
			HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
			SparseVector document = new SparseVector(conceptsList, scores, ClassifierConstant.isBreakConcepts, conceptWeights);
			labelResultsInDepth = tree.labelDocumentML(document);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return labelResultsInDepth;
	}
	
	
	
	public void simple_esa(String s) throws Exception{
		SimpleESALocal esa = new SimpleESALocal();
		System.out.print(s);
		List<ConceptData> cDoc=esa.getConcepts(500, s);
		view.display_esa(cDoc);
		System.out.print("ESA done\n");
	}
}
