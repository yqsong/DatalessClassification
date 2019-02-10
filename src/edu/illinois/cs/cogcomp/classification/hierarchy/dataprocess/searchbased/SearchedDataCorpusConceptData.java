package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.searchbased;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts.AbstractCorpusConceptData;

/**
 * yqsong@illinois.edu
 */

public class SearchedDataCorpusConceptData extends AbstractCorpusConceptData {

	@Override
	public void readCorpusContentOnly(String file, Random random,
			double trainingRate) {
		try {
	     	FileReader reader = new FileReader(file);
	     	BufferedReader bf = new BufferedReader(reader);
	     	String line = "";
	     	int count = 0;
	     	while ((line = bf.readLine()) != null) {
	     		if (count % 1000 == 0) {
	     			System.out.println("Read doc num: " + count + " lines");
	     		}
	     		
	     		String[] tokens = line.split("\t");
	     		if (tokens.length != 3) {
	     			continue;
	     		}
	     		
	     		String docID = tokens[0].trim();
	     		String docContent = tokens[2].trim();
	     	
	     		count++;
	     		
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
	public void readCorpusContentOnly(String file, int readNum, Random random,
			double trainingRate) {
		// TODO Auto-generated method stub

	}

}
