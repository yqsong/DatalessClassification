package edu.illinois.cs.cogcomp.classification.hierarchy.run.preparedata.newsgroups;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.representation.esa.AbstractESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.DiskBasedComplexESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.simple.SimpleESALocal;

/**
 * yqsong@illinois.edu
 */

public class CorpusESAConceptualization20NewsGroups {

	public static void main(String[] args) {
		
//		ClassifierConstant.cutOff = Double.parseDouble(args[0]);//0.5 0.1;//;
//		
//		int type = 1;
//		ClassifierConstant.complexVectorType = ComplexESALocal.searchTypes[type];
//		conceptualizeCorpusComplex (500) ;
		
		conceptualizeCorpus (500) ;


	}
	
	public static void conceptualizeCorpus (int conceptNum) 	{
		int seed = 0;
		Random random = new Random(seed);
		double trainingRate = 0.5;
		
		String inputData = "data/20newsgroups/textindex";
		String outputData = "data/20newsgroups/output_new/20newsgroups.simple.esa.concepts." + conceptNum;
		
		CorpusESAConceptualization20NewsGroups corpusContentProc = new CorpusESAConceptualization20NewsGroups();
		NewsgroupsCorpusConceptData ngData = new NewsgroupsCorpusConceptData();
		ngData.readCorpusContentOnly(inputData, random, trainingRate);
		corpusContentProc.writeCorpusSimpleConceptData(ngData.getCorpusContentMap(), conceptNum, outputData);
	}
	
	public static void conceptualizeCorpusComplex (int conceptNum) 	{
		int seed = 0;
		Random random = new Random(seed);
		double trainingRate = 0.5;
		
		String inputData = "data/20newsgroups/textindex";
		String outputData = "data/20newsgroups/output/20newsgroups.complexGraph.cutoff" + ClassifierConstant.cutOff + ".esa.concepts." + ClassifierConstant.complexVectorType + conceptNum;
		
		CorpusESAConceptualization20NewsGroups corpusContentProc = new CorpusESAConceptualization20NewsGroups();
		NewsgroupsCorpusConceptData ngData = new NewsgroupsCorpusConceptData();
		ngData.readCorpusContentOnly(inputData, random, trainingRate);
		corpusContentProc.writeCorpusComplexConceptData(ngData.getCorpusContentMap(), conceptNum, outputData);

	}
	
	public void writeCorpusSimpleConceptData (HashMap<String, String> corpusContentMap, int numConcepts, String file) {
		String content = "";
		try {
			int count = 0;
			FileWriter writer = new FileWriter(file);
			SimpleESALocal esa = new SimpleESALocal();
			for (String docID : corpusContentMap.keySet()) {
				count++;
				System.out.println("written " + count +  " documents with concepts");
				content = corpusContentMap.get(docID);
				List<ConceptData> concepts = esa.getConcepts(numConcepts, content);
				List<String> conceptsList = new ArrayList<String>();
				String docContent = corpusContentMap.get(docID);
				writer.write(docID + "\t" + docContent + "\t");
				for (int i = concepts.size() - 1; i >= 0; i--) {
					writer.write(concepts.get(i).concept + "," + concepts.get(i).score + ";");
				}
				writer.write("\n\r");
			}
			writer.close();
		} catch (Exception e) {
			System.out.println(content);
			e.printStackTrace();
		}
			
	}
	
	public void writeCorpusComplexConceptData (HashMap<String, String> corpusContentMap, int numConcepts, String file) {
		try {
			int count = 0;
			FileWriter writer = new FileWriter(file);
			AbstractESA esa = null;
			esa = new DiskBasedComplexESA();
			for (String docID : corpusContentMap.keySet()) {
				count++;
				System.out.println("written " + count +  " documents with concepts");
				List<ConceptData> concepts = esa.retrieveConcepts(corpusContentMap.get(docID), numConcepts, ClassifierConstant.complexVectorType);
				String docContent = corpusContentMap.get(docID).replace("\t", " ");
				writer.write(docID + "\t" + docContent + "\t");
				for (int i = concepts.size() - 1; i >= 0; i--) {
					writer.write(concepts.get(i).concept + "," + concepts.get(i).score + ";");
				}
				writer.write("\n\r");
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
			
	}
	
	
}

