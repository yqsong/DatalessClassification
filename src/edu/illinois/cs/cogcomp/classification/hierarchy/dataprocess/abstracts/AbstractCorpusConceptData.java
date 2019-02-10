package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;

/**
 * yqsong@illinois.edu
 */

public abstract class AbstractCorpusConceptData {
	protected HashMap<String, String> corpusContentMap = null;
	protected HashMap<String, SparseVector> corpusConceptVectorMap = null;
	protected HashMap<String, String> corpusConceptStringMap = null;

	protected HashMap<String, String> corpusContentMapTraining = null;
	protected HashMap<String, SparseVector> corpusConceptMapTraining = null;

	protected HashMap<String, String> corpusContentMapTest = null;
	protected HashMap<String, SparseVector> corpusConceptMapTest = null;

	public AbstractCorpusConceptData () {
		corpusContentMap = new HashMap<String, String>();
		corpusConceptVectorMap = new HashMap<String, SparseVector>();
		corpusConceptStringMap = new HashMap<String, String>();

		corpusContentMapTraining = new HashMap<String, String>();
		corpusConceptMapTraining = new HashMap<String, SparseVector>();
		corpusContentMapTest = new HashMap<String, String>();
		corpusConceptMapTest = new HashMap<String, SparseVector>();
	}
	
	public HashMap<String, String> getCorpusContentMap () {
		return this.corpusContentMap;
	}
	
	public HashMap<String, String> getCorpusConceptStringMap () {
		return this.corpusConceptStringMap;
	}
	
	public HashMap<String, SparseVector> getCorpusConceptVectorMap () {
		return this.corpusConceptVectorMap;
	}
	
	public HashMap<String, String> getCorpusContentMapTraining () {
		return this.corpusContentMapTraining;
	}
	
	public HashMap<String, SparseVector> getCorpusConceptVectorMapTraining () {
		return this.corpusConceptMapTraining;
	}
	
	public HashMap<String, String> getCorpusContentMapTest () {
		return this.corpusContentMapTest;
	}
	
	public HashMap<String, SparseVector> getCorpusConceptVectorMapTest () {
		return this.corpusConceptMapTest;
	}
	
	public void readCorpusContentAndConcepts (String file, boolean isBreakConcepts, Random random, double trainingRate, HashMap<String, Double> conceptWeights) {
		String conceptStr = "";
		String[] subTokens = null;
		String[] subTokensSplit = null;
		try {
			FileReader reader = new FileReader(file);
	     	BufferedReader bf = new BufferedReader(reader);
	     	String line = "";
	     	int count = 0;
	     	while ((line = bf.readLine()) != null) {
	     		if (line.equals("") == true)
	     			continue;
	     		
	     		count++;
	     		if (count % 1000 == 0) {
	     			System.out.println("Read doc num: " +  count);
	     		}
	     		String[] tokens = line.trim().split("\t");
	     		
	     		if (tokens.length != 3)
	     			continue;
	     		
	     		String docID = tokens[0];
	     		String docContent = tokens[1];
	     		conceptStr = tokens[2];
	     		
//	     		docContent = docContent.replaceAll("[^a-zA-Z\\s]", "");
	     		
	     		subTokens = conceptStr.split(";");
	    		List<String> conceptsList = new ArrayList<String>();
	    		List<Double> scores = new ArrayList<Double>();
	    		for (int i = 0; i < subTokens.length; ++i) {
	    			subTokensSplit = subTokens[i].trim().split(",");
	    			if (subTokensSplit.length > 2) {
	    				String concept = subTokensSplit[0];
	    				for (int j = 1; j < subTokensSplit.length - 1; ++j) {
	    					concept += subTokensSplit[j];
	    				}
	    				conceptsList.add(concept.trim());
	    				scores.add(Double.parseDouble(subTokensSplit[subTokensSplit.length - 1].trim()));
	    			} else if (subTokensSplit.length == 2) {
	    				conceptsList.add(subTokensSplit[0].trim());
	    				scores.add(Double.parseDouble(subTokensSplit[1].trim()));
	    			}
	    		}
	    		SparseVector conceptVector = new SparseVector(conceptsList, scores, isBreakConcepts, conceptWeights);
	    		
	     		corpusContentMap.put(docID, docContent);
	     		corpusConceptVectorMap.put(docID, conceptVector);
	     		corpusConceptStringMap.put(docID, conceptStr);
	     		
	     		if (random.nextDouble() < trainingRate) {
		     		corpusContentMapTraining.put(docID, docContent);
		     		corpusConceptMapTraining.put(docID, conceptVector);
	     		} else {
		     		corpusContentMapTest.put(docID, docContent);
		     		corpusConceptMapTest.put(docID, conceptVector);
	     		}
	     	}
	     	bf.close();
	     	reader.close();
	     	
		} catch (Exception e ) 
		{
			e.printStackTrace();
		};
	}
	
	public void readCorpusContentAndConcepts (String file, boolean isBreakConcepts, Random random, double trainingRate, int maxTrainingNum, HashMap<String, Double> conceptWeights) {
		String conceptStr = "";
		String[] subTokens = null;
		String[] subTokensSplit = null;
		try {
			FileReader reader = new FileReader(file);
	     	BufferedReader bf = new BufferedReader(reader);
	     	String line = "";
	     	int count = 0;
	     	while ((line = bf.readLine()) != null) {
	     		if (line.equals("") == true)
	     			continue;
	     		
	     		count++;
	     		if (count % 1000 == 0) {
	     			System.out.println("Read doc num: " +  count);
	     		}
	     		String[] tokens = line.trim().split("\t");
	     		
	     		if (tokens.length != 3)
	     			continue;
	     		
	     		String docID = tokens[0];
	     		String docContent = tokens[1];
	     		conceptStr = tokens[2];
	     		
//	     		docContent = docContent.replaceAll("[^a-zA-Z\\s]", "");
	     		
	     		subTokens = conceptStr.split(";");
	    		List<String> conceptsList = new ArrayList<String>();
	    		List<Double> scores = new ArrayList<Double>();
	    		for (int i = 0; i < subTokens.length; ++i) {
	    			subTokensSplit = subTokens[i].trim().split(",");
	    			if (subTokensSplit.length > 2) {
	    				String concept = subTokensSplit[0];
	    				for (int j = 1; j < subTokensSplit.length - 1; ++j) {
	    					concept += subTokensSplit[j];
	    				}
	    				conceptsList.add(concept.trim());
	    				scores.add(Double.parseDouble(subTokensSplit[subTokensSplit.length - 1].trim()));
	    			} else if (subTokensSplit.length == 2) {
	    				conceptsList.add(subTokensSplit[0].trim());
	    				scores.add(Double.parseDouble(subTokensSplit[1].trim()));
	    			}
	    		}
	    		SparseVector conceptVector = new SparseVector(conceptsList, scores, isBreakConcepts, conceptWeights);
	    		
	     		corpusContentMap.put(docID, docContent);
	     		corpusConceptVectorMap.put(docID, conceptVector);
	     		corpusConceptStringMap.put(docID, conceptStr);
	     		
	     		if (random.nextDouble() < trainingRate && corpusContentMapTraining.size() < maxTrainingNum) {
		     		corpusContentMapTraining.put(docID, docContent);
		     		corpusConceptMapTraining.put(docID, conceptVector);
	     		} else {
		     		corpusContentMapTest.put(docID, docContent);
		     		corpusConceptMapTest.put(docID, conceptVector);
	     		}
	     	}
	     	bf.close();
	     	reader.close();
	     	
		} catch (Exception e ) 
		{
			e.printStackTrace();
		};
		System.out.println("[Data] training: " + corpusContentMapTraining.size() + ", test: " + corpusContentMapTest.size());
	}
	
	public abstract void readCorpusContentOnly (String file, Random random, double trainingRate) ;
	
	public abstract void readCorpusContentOnly (String file, int readNum, Random random, double trainingRate);
}
