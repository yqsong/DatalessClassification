package edu.illinois.cs.cogcomp.classification.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.densification.representation.DenseVector;
import edu.illinois.cs.cogcomp.classification.densification.representation.SparseSimilarityCondensation;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.DiskBasedWordEmbedding;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.WordEmbeddingInterface;

/**
 * Shaoshi Ling
 * sling3@illinois.edu
 */

public abstract class Classification {
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
	public static HashMap<String, String> dataTrueLabelMap = new HashMap<String, String>();
	public static HashMap<String, String> contentMap = new HashMap<String, String>();
	public static HashMap<String, List<Integer>> labelMap = new HashMap<String, List<Integer>>();
	public static HashMap<String, String> labelresults=new HashMap<String, String>();
	public static HashMap<Integer, HashMap<Integer, List<LabelKeyValuePair>>> resultsInDpeth=new HashMap<Integer, HashMap<Integer, List<LabelKeyValuePair>>>();
	public static HashMap<String, SparseVector> labels=new HashMap<String, SparseVector>();
	public static int Docnum;
	public static HashMap<String, Double> globalConceptWeights = new HashMap<String, Double>();
	SparseSimilarityCondensation vectorCondensation =null;
	public abstract void MultiText(String labelsets, String contentInfoFile,String LabelResult) throws Exception;
	public abstract void GivenText(String labelsets) throws Exception;
	
	public void readContentData (String contentInfoFile) {
		try {
			FileReader reader = new FileReader(contentInfoFile);
			BufferedReader br = new BufferedReader(reader);
			
			String line = "";
			int count = 0;
			while ((line = br.readLine()) != null ) {
				
				if (count % 100 == 0) {
					//System.out.println("Processed content " + count + " lines...");
				}
				
				String[] tokens = line.split("\t");

				if (tokens.length != 2) 
					continue;
				
				String dataID = tokens[0];
				String content = tokens[1].replaceAll("[^a-zA-Z\\s]", " ").replaceAll("\\s+", " ");
				contentMap.put(dataID, content);
				//System.out.println("DataId  " + dataID  + "content "+ content +"\n");
				
				count++;
			}
			Docnum=count;
			br.close();
			reader.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished reading ContentData");
	}
	
	public void writeresult(String LabelResult) throws IOException{
		
		FileWriter fw = new FileWriter(LabelResult);
	
		for(int i=0;i<Docnum;i++){
			fw.write(i+" "+labelresults.get(Integer.toString(i))+"\n");
		}
	
		fw.close();
	}
	
	public void statistics(){
		for(int i=0;i<Docnum;i++){
			String label=labelresults.get(Integer.toString(i));
			if(labelMap.containsKey(label)) labelMap.get(label).add(i);
			else {
				List<Integer> l=new ArrayList<Integer>();
				l.add(i);
				labelMap.put(label,l );
			}
		}
		
	}
	
//	public HashMap<String, Double> keyterm(SparseSimilarityCondensation vectorCondensation,int id,String label){
//		String doc=contentMap.get(id);
//		String[] words=doc.split("\t");
//		HashMap<String, Double> termscoremap=new HashMap<String, Double>();
//		for (int i=0;i<words.length;i++){
//			termscoremap.put(words[i], SparseSimilarityCondensation.similarity(words[i], label));
//		}
//		return termscoremap;
//		
//	}
	
	
//	public void writeresultJson() throws IOException{
//		FileWriter fw = new FileWriter("data/result_json.txt");
//		Gson gson = new Gson(); 
//		String json = gson.toJson(resultsInDpeth); 
//		fw.write( "JSON: "+json );
//		fw.close();
//		json = gson.toJson(labelMap); 
//		fw = new FileWriter("data/stat_json.txt");
//		fw.write( "JSON: "+json );
//		fw.close();
//	}
}
