package edu.illinois.cs.cogcomp.classification.representation.word2vec;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;



public class MemoryBasedJohnVec {

    public static HashMap<String,double[]> vectors = null;
    public static String unknown = "UUUNKKK";
    
    public static void loadParagram() {
        vectors = new HashMap<String,double[]>();

		System.out.println("[Read Memory JohnVec Data] " + DatalessResourcesConfig.memorybasedJohnVec);

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(DatalessResourcesConfig.memorybasedJohnVec));  
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
			        double[] vec = new double[25];
			        for(int i=1; i < arr.length; i++) {
			            vec[i-1] = Double.parseDouble(arr[i]);
			        }
			        vectors.put(word, vec);
			    }                
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


 
    }
    
    public double paragramScore(String w1, String w2) {
        double[] d1 = getVector(w1);
        double[] d2 = getVector(w2);
        
        return cosine(d1,d2);
    }
    
    public static double[] getVector(String s) {
        s = s.toLowerCase();
        if(vectors == null)
            loadParagram();
        
        if(vectors.containsKey(s))
            return vectors.get(s);
        
        return vectors.get(unknown);
    }
    
    public double cosine(double[] d1, double[] d2) {
        double cosine = 0;
        double t1 = 0;
        double t2 = 0;
        
        for(int i=0; i < d1.length; i++) {
            cosine += d1[i]*d2[i];
            t1 += d1[i]*d1[i];
            t2 += d2[i]*d2[i];
        }
        
        return cosine / (Math.sqrt(t1)*Math.sqrt(t2));
    }
    
    public static double[] add(double[] sum2, double[] ds) {
        double[] sum = new double[25];
        
        for(int i=0; i < sum2.length; i++) {
            sum[i] = sum2[i] + ds[i];
        }
        
        return sum;
    }
    
    public MemoryBasedJohnVec(){
    	loadParagram();
    }

	public double[] getDenseVector(String query) {
		double[] sum = new double[25];
        for(int i=0; i < 25; i++) {
            sum[i] = (double) 0;
        }
		String[] tokens = query.split("\\s+");
    	for (int i = 0; i < tokens.length; i++) {
    		String word = tokens[i].toLowerCase().trim();
    		sum=add(sum,getVector(word));
    	}
    	for(int i=0; i < 25; i++) {
            sum[i] = sum[i]/tokens.length;
        }
		return sum;
	}
    

    
}
