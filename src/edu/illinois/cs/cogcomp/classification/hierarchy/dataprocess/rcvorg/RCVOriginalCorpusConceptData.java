package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcvorg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts.AbstractCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;

public class RCVOriginalCorpusConceptData extends AbstractCorpusConceptData{
	
	public RCVOriginalCorpusConceptData () {
		super();
	}
	
	@Override
	public  void readCorpusContentOnly (String file, Random random, double trainingRate) {
		try {
	     	FileReader reader = new FileReader(file);
	     	BufferedReader bf = new BufferedReader(reader);
	     	String line = "";
	     	int count = 0;
	     	int docNum = 0;
	     	while (line != null) {
	     		if (count % 1000 == 0) {
	     			System.out.println("Read doc num: " + docNum + " with " + count + " lines");
	     		}
	     		
	     		line = bf.readLine();
	     		count++;
	     		
	     		if (line == null) {
	     			break;
	     		}

	     		String[] tokens = line.split("\t");
	     		if (tokens.length != 2 || line.isEmpty() == true) {
	     			continue;
	     		}
	     		
	     		String docID = tokens[0].trim();
	     		String docContent = tokens[1].trim();

	     		corpusContentMap.put(docID, docContent);
	     		if (random.nextDouble() < trainingRate) {
		     		corpusContentMapTraining.put(docID, docContent);
	     		} else {
		     		corpusContentMapTest.put(docID, docContent);
	     		}
	     	}
	     	bf.close();
	     	reader.close();
	     	
		} catch (Exception e ) 
		{
			e.printStackTrace();
		};
	}
	
	@Override
	public void readCorpusContentOnly (String file, int readNum, Random random, double trainingRate) {
		try {
	     	FileReader reader = new FileReader(file);
	     	BufferedReader bf = new BufferedReader(reader);
	     	String line = "";
	     	int count = 0;
	     	int docNum = 0;
	     	while (line != null) {
	     		if (count % 1000 == 0) {
	     			System.out.println("Read doc num: " + docNum + " with " + count + " lines");
	     		}
	     		line = bf.readLine();
	     		count++;
	     		
	     		if (line == null) {
	     			break;
	     		}

	     		String[] tokens = line.split("\t");
	     		if (tokens.length != 2 || line.isEmpty() == true) {
	     			continue;
	     		}
	     		
	     		String docID = tokens[0].trim();
	     		String docContent = tokens[1].trim();

	     		corpusContentMap.put(docID, docContent);
	     		if (random.nextDouble() < trainingRate) {
		     		corpusContentMapTraining.put(docID, docContent);
	     		} else {
		     		corpusContentMapTest.put(docID, docContent);
	     		}
	     		
	     		if (docNum > readNum) {
	     			break;
	     		}
	     		corpusContentMap.put(docID, docContent);
	     		if (random.nextDouble() < trainingRate) {
		     		corpusContentMapTraining.put(docID, docContent);
	     		} else {
		     		corpusContentMapTest.put(docID, docContent);
	     		}
	     	}
	     	bf.close();
	     	reader.close();
	     	
		} catch (Exception e ) 
		{
			e.printStackTrace();
		};
	}
}
