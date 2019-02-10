package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts.AbstractCorpusConceptData;

/**
 * yqsong@illinois.edu
 */

public class RCVCorpusConceptData extends AbstractCorpusConceptData{
	
	public RCVCorpusConceptData () {
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
	     		String docID = "";
	     		String docContent = "";
	     		
	     		line = bf.readLine();
	     		count++;
	     		
	     		if (line == null) {
	     			break;
	     		}
	     		if (line.startsWith(".I")) {
	     			docNum++;
	     			String[] splitArray = line.trim().split(" ");
	     			docID = splitArray[1].trim();
	     		} else {
	     			throw new Exception("Reading RCV data error");
	     		}
	     		
	     		line = bf.readLine();
	     		count++;
	     		if (line.trim().equals(".W")) {
	     			while (true) {
	     				line = bf.readLine();
	     				count++;
	     				if (line != null && line.equals("") == false) {
	     					docContent += line + " ";
	     				} else {
	     					break;
	     				}
	     			}
	     		} else {
	     			throw new Exception("Reading RCV data error");
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
	     		String docID = "";
	     		String docContent = "";
	     		
	     		line = bf.readLine();
	     		count++;
	     		
	     		if (line == null) {
	     			break;
	     		}
	     		if (line.startsWith(".I")) {
	     			docNum++;
	     			String[] splitArray = line.trim().split(" ");
	     			docID = splitArray[1].trim();
	     		} else {
	     			throw new Exception("Reading RCV data error");
	     		}
	     		
	     		line = bf.readLine();
	     		count++;
	     		if (line.trim().equals(".W")) {
	     			while (true) {
	     				line = bf.readLine();
	     				count++;
	     				if (line != null && line.equals("") == false) {
	     					docContent += line + " ";
	     				} else {
	     					break;
	     				}
	     			}
	     		} else {
	     			throw new Exception("Reading RCV data error");
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
