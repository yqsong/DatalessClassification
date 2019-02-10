package edu.illinois.cs.cogcomp.classification.representation.word2vec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;

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

import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.representation.QueryPreProcessor;

public class MemoryBasedWordEmbedding  implements WordEmbeddingInterface  {
	
    public static String unknown = "UUUNKKK";

    public HashMap<String,double[]> vectors = null;
	
	public MemoryBasedWordEmbedding () {
		
		System.out.println("[Read Memory Word2vec Data] " + DatalessResourcesConfig.memorybasedW2V);
		
		File inputFile =new File(DatalessResourcesConfig.memorybasedW2V);
		
		vectors = new HashMap<String,double[]>();
		int cc=0;
	    BufferedReader reader = null;
	    try {
			reader = new BufferedReader(new FileReader(inputFile));        
		} catch (FileNotFoundException e) {
				e.printStackTrace();
		}
		try {
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
		
				line = line.trim();
				if(line.length() > 0) {
				String[] arr = line.split("\\s+");
		        String word = arr[0];
		        double[] vec = new double[DatalessResourcesConfig.embeddingDimension];
		        for(int i=1; i < arr.length; i++) {
	            vec[i-1] = Double.parseDouble(arr[i]);
	            }
		        vectors.put(word, vec);
				}     
				cc++;
				if(cc%100000==0) System.out.print("cached w2v word number: "+ cc+"\n"); 
			}
		} catch (Exception e) {
				e.printStackTrace();
		}
	}
	
	public double[] getDenseVectorSimpleAverage (String query){
		query = QueryPreProcessor.process(query);
		
		double[] sum = new double[DatalessResourcesConfig.embeddingDimension];
        for(int i = 0; i < DatalessResourcesConfig.embeddingDimension; i++) {
            sum[i] = (double) 0;
        }
		String[] tokens = query.split("\\s+");
    	for (int i = 0; i < tokens.length; i++) {
    		String word = tokens[i].toLowerCase().trim();
    		sum = add(sum, getVector(word));
    	}
    	for(int i = 0; i < DatalessResourcesConfig.embeddingDimension; i++) {
            sum[i] = sum[i]/tokens.length;
        }
		return sum;
		
	}
	   
	public double[] add(double[] sum2, double[] ds) {
		double[] sum = new double[DatalessResourcesConfig.embeddingDimension];
	        
		for(int i=0; i < sum2.length; i++) {
			sum[i] = sum2[i] + ds[i];
		}
	        
		return sum;
	}
	  
	  
	public double[] getDenseVectorBasedonSegmentation(String query, boolean isDF){
		query = QueryPreProcessor.process(query);
		
		HashMap<Integer, Double> vector = getConceptVectorBasedonSegmentation(query, isDF);
		double[] finalVector = null;
		if(vector==null)  vector = getConceptVectorBasedonSegmentation("auto", isDF);
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
	        	
	            double[] d=getVector(strTerm);
	       
	            if (d==null) continue;
		    	for (int j = 0; j < d.length; ++j) {
					double score = d[j];
	    				
	    			if (isDF == true) {
	    				if (finalVector.containsKey(j) == false) {
	    					finalVector.put(j, score * 1);
	    				} else {
	    					finalVector.put(j, finalVector.get(j) + score * 1);
	    				}
	    			} else {
	    				if (finalVector.containsKey(j) == false) {
	    					finalVector.put(j, score * tfMap.get(strTerm));
	    				} else {
	    					finalVector.put(j, finalVector.get(j) + score * tfMap.get(strTerm));
	    				}
	    			}
	    		}
	    		if (isDF == true) {
	    			sumWeight += 1;
	    		} else {
	    			sumWeight += tfMap.get(strTerm); //1;//
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
	        	
	            double[] d=getVector(strTerm);
		    	for (int j = 0; j < d.length; ++j) {
					double score = d[j];
					if (finalVector.containsKey(j) == false) {
	    				finalVector.put(j, score * queryTFIDF.get(strTerm));
	    			} else {
	    				finalVector.put(j, finalVector.get(j) + score * queryTFIDF.get(strTerm));
	    			}
	    			
	    		}

	    		sumWeight += queryTFIDF.get(strTerm); //1;//
	    	
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
	
    public double[] getVector(String s) {
        s = s.toLowerCase();
        
        if(vectors.containsKey(s))
            return vectors.get(s);
        
        return new double[DatalessResourcesConfig.embeddingDimension];
    }
	
}
