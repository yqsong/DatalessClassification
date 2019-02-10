package edu.illinois.cs.cogcomp.classification.representation.word2vec;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;

import javax.xml.crypto.Data;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.illinois.cs.cogcomp.classification.densification.representation.DenseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.representation.QueryPreProcessor;

/**
 * yqsong@illinois.edu
 */

public class DiskBasedWordEmbedding implements WordEmbeddingInterface {

	private  IndexReader reader = null;
	private  IndexSearcher searcher = null;
	private  Directory inputDir = null;
	
	private String method = "word2vec";
	
	
	public static void main (String[] args) {
		DatalessResourcesConfig.initialization();
		DiskBasedWordEmbedding embedding = new DiskBasedWordEmbedding(50,1);
		double[] densevector1 = embedding.getDenseVectorBasedonSegmentation("In article C vIr L r shuksan ds boeing com fredd shuksan Fred Dickey writes CarolinaFan uiuc cka uxa cso uiuc edu wrote I have been active ", false);
		System.out.print(densevector1.toString());
	}
	

	public DiskBasedWordEmbedding () {
		File inputFile = null;
		inputFile = new File(DatalessResourcesConfig.word2vecIndex);
		
		try {
			System.out.println("Register to lucene: " + inputFile.getAbsolutePath());
			inputDir = FSDirectory.open(inputFile);
			reader = IndexReader.open(inputDir, true);
			searcher = new IndexSearcher(reader);
			System.out.println("Done...");
		} catch (Exception e) {
	    	e.printStackTrace();
	    	System.exit(-1);
	    } 
	}

	
	public DiskBasedWordEmbedding (int count, int type) {
		File inputFile = null;
		if(type==0)
		inputFile = new File("/shared/shelley/yqsong/data/wordDist/enwiki_vivek_"+count);
		else inputFile = new File("/shared/shelley/yqsong/data/wordDist/enwiki_vivek_"+count+"_skipgram");
		
		try {
			System.out.println("Register to lucene: " + inputFile.getAbsolutePath());
			inputDir = FSDirectory.open(inputFile);
			System.out.print(inputFile);
			reader = IndexReader.open(inputDir, true);
			searcher = new IndexSearcher(reader);
			System.out.println("Done...");
		} catch (Exception e) {
	    	e.printStackTrace();
	    	System.exit(-1);
	    } 
	}
	
	public double[] getDenseVectorSimpleAverage(String query) {
		query = QueryPreProcessor.process(query);
		double[] sum = new double[DatalessResourcesConfig.embeddingDimension];
        for(int i = 0; i < DatalessResourcesConfig.embeddingDimension; i++) {
            sum[i] = (double) 0;
        }
		String[] tokens = query.split("\\s+");
		for (String strTerm : tokens) {
			String word = strTerm.toLowerCase().trim();
    		sum = add(sum, getVector(word));
		}

		return null;
	}
	
	public double[] add(double[] sum2, double[] ds) {
		double[] sum = new double[DatalessResourcesConfig.embeddingDimension];
	        
		for(int i=0; i < sum2.length; i++) {
			sum[i] = sum2[i] + ds[i];
		}
	        
		return sum;
	}
	
	public double[] getVector (String strTerm) {
		double[] vector = new double[DatalessResourcesConfig.embeddingDimension];
		try {
			Query luceneQuery = new TermQuery(new Term("word", strTerm));  
	    	TopDocs docs = searcher.search(luceneQuery, null, Double.MAX_EXPONENT);
	    	ScoreDoc[] hits = docs.scoreDocs;
	    	
	    	for (int j = 0; j < hits.length; ++j) {
	    		int docID = hits[j].doc;
	    		Document doc = reader.document(docID);
				String termStr = doc.get("word").trim();
				String termVector = doc.get("feature").trim();

				if (termStr.equals(strTerm) == false)
					continue;
				
				
				String[] idScoreArray = termVector.trim().split(" ");
				
				if (idScoreArray.length < 6) {
					int stop = 0;
					int a = stop;
				}
				
				for (int k = 0; k < idScoreArray.length; ++k) {
					
					int index = 0;
					double score = 0;
					index = k;
					String scoreStr = idScoreArray[k];
					score = Double.parseDouble(scoreStr);
					vector[index] += score;
				}
	    	}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return vector;
	}
	
	public double[] getDenseVectorBasedonSegmentation(String query, boolean isDF){
		query = QueryPreProcessor.process(query);
		
		HashMap<Integer, Double> vector = getConceptVectorBasedonSegmentation(query, isDF);
		double[] finalVector = null;
		if(vector==null)  vector = getConceptVectorBasedonSegmentation("the", isDF);
		finalVector = new double[vector.size()];
		for (Integer key : vector.keySet()) {
			int index = key;
			double value = vector.get(key);
			finalVector[index] = value	;
		}
		
		return finalVector;
	}

	public HashMap<Integer, Double> getConceptVectorBasedonSegmentation(String query, boolean isDF){
		
		query = QueryPreProcessor.process(query);
		
		HashMap<Integer, Double> finalVector = new HashMap<Integer, Double>();
		
		String[] tokens = query.split("\\s+");
    	List<String> newTokens = new ArrayList<String>();
    	for (int i = 0; i < tokens.length; i++) {
    		String word = tokens[i].toLowerCase().trim();
    		newTokens.add(word);
    	}
    	
    	Hashtable<String, Double> tfMap = new Hashtable<String, Double>();
    	for  (int i = 0; i < newTokens.size(); ++i) { 
    		String token = newTokens.get(i);
    		if (tfMap.containsKey(token) == false) {
    			tfMap.put(token, 1.0);
    		} else {
    			tfMap.put(token, tfMap.get(token) + 1);
    		}
    	}
    	
    	if(tfMap.size() == 0){
        	return null;
        }
    	
    	try {
    		double sumWeight = 0;
	        for(String strTerm : tfMap.keySet()) { 
	
	            Query luceneQuery = new TermQuery(new Term("word", strTerm));  
		    	TopDocs docs = searcher.search(luceneQuery, null, Double.MAX_EXPONENT);
		    	ScoreDoc[] hits = docs.scoreDocs;
		    	for (int j = 0; j < hits.length; ++j) {
		    		int docID = hits[j].doc;
		    		Document doc = reader.document(docID);
	    			String termStr = doc.get("word").trim();
	    			String termVector = doc.get("feature").trim();

	    			if (termStr.equals(strTerm) == false)
	    				continue;
	    			
	    			
	    			String[] idScoreArray = termVector.trim().split(" ");
	    			
	    			if (idScoreArray.length < 6) {
	    				int stop = 0;
	    				int a = stop;
	    			}
	    			
	    			for (int k = 0; k < idScoreArray.length; ++k) {
	    				
	    				int index = 0;
	    				double score = 0;
	    				index = k;
    					String scoreStr = idScoreArray[k];
	    				score = Double.parseDouble(scoreStr);
	    				
	    				if (isDF == true) {
	    					if (finalVector.containsKey(index) == false) {
	    						finalVector.put(index, score * 1);
	    					} else {
	    						finalVector.put(index, finalVector.get(index) + score * 1);
	    					}
	    				} else {
	    					if (finalVector.containsKey(index) == false) {
	    						finalVector.put(index, score * tfMap.get(strTerm));
	    					} else {
	    						finalVector.put(index, finalVector.get(index) + score * tfMap.get(strTerm));
	    					}
	    				}
	    			}
	    			if (isDF == true) {
	    				sumWeight += 1;
	    			} else {
	    				sumWeight += tfMap.get(strTerm); //1;//
	    			}
		    	}
	        }
	        if (finalVector != null) {
		        for (Integer k : finalVector.keySet()) {
		        	finalVector.put(k, finalVector.get(k) / sumWeight);
		        }
	        }
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
		
		return finalVector;
	}

	public HashMap<Integer, Double> getConceptVectorBasedonTFIDF(HashMap<String, Double> queryTFIDF){
		HashMap<Integer, Double> finalVector = new HashMap<Integer, Double>();
		
    	try {
    		double sumWeight = 0;
	        for(String strTerm : queryTFIDF.keySet()) { 
	
	            Query luceneQuery = new TermQuery(new Term("word", strTerm));  
		    	TopDocs docs = searcher.search(luceneQuery, null, Double.MAX_EXPONENT);
		    	ScoreDoc[] hits = docs.scoreDocs;
		    	for (int j = 0; j < hits.length; ++j) {
		    		int docID = hits[j].doc;
		    		Document doc = reader.document(docID);
	    			String termStr = doc.get("word").trim();
	    			String termVector = doc.get("feature").trim();

	    			if (termStr.equals(strTerm) == false)
	    				continue;
	    			String[] idScoreArray = termVector.trim().split(" ");

	    			for (int k = 0; k < idScoreArray.length; ++k) {
	    				int index = 0;
	    				double score = 0;
	    				index = k;
    					String scoreStr = idScoreArray[k];
	    				score = Double.parseDouble(scoreStr);
	    				
	    				if (finalVector.containsKey(index) == false) {
    						finalVector.put(index, score * queryTFIDF.get(termStr));
    					} else {
    						finalVector.put(index, finalVector.get(index) + score * queryTFIDF.get(termStr));
    					}
//	    				finalVector[k] += score * queryTFIDF.get(termStr);//tfMap.get(strTerm);
	    			}
	    			sumWeight += queryTFIDF.get(termStr);
		    	}
	        }
	        
	        if (finalVector != null) {
		        for (Integer k : finalVector.keySet()) {
		        	finalVector.put(k, finalVector.get(k) / sumWeight);
		        }
	        }
	        
//	        if (finalVector != null) {
//		        for (int i = 0; i < finalVector.length; ++i) {
//		        	finalVector[i] /= sumWeight;
//		        }
//	        }
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
		
		return finalVector;
	}



	

}
