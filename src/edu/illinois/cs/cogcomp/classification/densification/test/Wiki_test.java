package edu.illinois.cs.cogcomp.classification.densification.test;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.densification.representation.SparseSimilarityCondensation;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.DiskBasedComplexESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.simple.SimpleESALocal;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.DiskBasedWordEmbedding;


public class Wiki_test {
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
	public String labelInfoFile = "/shared/bronte/sling3/data/labelInfo.txt";
	public static HashMap<String, String> dataTrueLabelMap = new HashMap<String, String>();
	public static HashMap<String, String> contentMap = new HashMap<String, String>();
	public static HashMap<String, List<ConceptData>> contents = new HashMap<String, List<ConceptData>>();
	public static HashMap<String, String> labelresults;
	public static HashMap<String, SparseVector> labels=new HashMap<String, SparseVector>();
	public static HashMap<String, Double> globalConceptWeights = new HashMap<String, Double>();
	HashMap<String, HashMap<String, Double>> topicVectorMap = new HashMap<String, HashMap<String, Double>>();
	HashMap<String, Double> topicNormMap = new HashMap<String, Double>();
	DiskBasedComplexESA esa = new DiskBasedComplexESA ();
	int Docnum;
	long startTime;

	long endTime; 
	
	public void test(int source) throws IOException {
		labelresults=new HashMap<String, String>();
		String complexVectorType = "tfidfVector";
		
		String matchingType = SparseSimilarityCondensation.matchingTypes[0];
		double sig = 0.05;
		SparseSimilarityCondensation vectorCondensation = new SparseSimilarityCondensation(
				matchingType, 0.85, sig, false); 
		startTime = System.currentTimeMillis();

		
		for (int i=0;i<Docnum;i++){
			System.out.print("Docid "+i+" ");
			//String content = contentMap.get(Integer.toString(i));

			try {
				/*
				List<ConceptData> conceptList = esa.retrieveConcepts(content, 500, complexVectorType);
				fw.write(Integer.toString(i)+"\t");
				for(ConceptData data:conceptList){
					fw.write(data.concept+","+data.score+";");
				}
				fw.write(ClassifierConstant.systemNewLine);
				*/
		
				
				HashMap<String, Double> vector = getVectorMap(contents.get(Integer.toString(i)));
				double normSentence = getNorm (vector);
				String label="";
				double maxSimilarity=0;
				
				for (String topic : topicVectorMap.keySet()) {
					HashMap<String, Double> vectorTopic = topicVectorMap.get(topic);
					double normTopic = topicNormMap.get(topic);
					
					double score = vectorCondensation.similarityWithMaxMatching(vectorTopic, vector, normSentence, normTopic);
					if(score>maxSimilarity){
						maxSimilarity=score;
						label=topic;
					}
				}
				labelresults.put(Integer.toString(i),label);
				System.out.print("Docid "+i+" label is "+label+" max similarity "+maxSimilarity+" correct label: "+dataTrueLabelMap.get(Integer.toString(i))+"\n");
			} catch (Exception e) {
				labelresults.put(Integer.toString(i),"failed labeling");
				e.printStackTrace();
			}
			
		}
		

	}
	
	
	public static HashMap<String, Double> getVectorMap (List<ConceptData> conceptList) {
		HashMap<String, Double> vectorMap = new HashMap<String, Double>();
		
		for (int i = 0; i < conceptList.size(); ++i) {
			vectorMap.put(conceptList.get(i).concept, conceptList.get(i).score);
		}
		
		return vectorMap;
	}
	
	
	public static double getNorm (HashMap<String, Double> vector) {
		double norm = 0;
		for (String key : vector.keySet()) {
			double value = vector.get(key);
			norm += value * value;
		}
		norm = Math.sqrt(norm);
		return norm;
	}
	
	public double cosine (SparseVector v2, SparseVector v1) {
		double dot = 0;
		if (v1.keyValueMap.size() < v2.keyValueMap.size()) {
			for (String key : v1.keyValueMap.keySet()) {
				if (v2.keyValueMap.containsKey(key) == true) {
					double value1 = v1.keyValueMap.get(key);
					double value2 = v2.keyValueMap.get(key);
					double value3 = 1;
					if (conceptWeights.containsKey(key));
						value3 = conceptWeights.get(key);
					dot += value1 * value2 * value3;
				}
			}
		} else {
			for (String key : v2.keyValueMap.keySet()) {
				if (v1.keyValueMap.containsKey(key) == true) {
					double value1 = v1.keyValueMap.get(key);
					double value2 = v2.keyValueMap.get(key);
					double value3 = 1;
					if (conceptWeights.containsKey(key));
						value3 = conceptWeights.get(key);
					dot += value1 * value2 * value3;
				}
			}
		}
		
		return dot / v1.getNorm() / v2.getNorm();
	}
	
	public void processLabelData() throws Exception{
		
		String filePath="/shared/bronte/sling3/data/cateInfo.txt";
	
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split("\t");
				String child=tokens[0].replaceAll("[^A-Za-z0-9]", " ").trim();
				
				String label = child;
//				String[] words = child.split(" ");
//				for (String word : words) {
//					word = word.replaceAll("[^A-Za-z0-9]", "").trim();
//					label += word + " ";
//				}
//				label = label.trim();
				
				//System.out.print(label+"\n");
				List<ConceptData> vectorTopic = esa.retrieveConcepts(label, 500, "tfidfVector");
				HashMap<String, Double> vector1 = getVectorMap(vectorTopic);
				topicVectorMap.put(label, vector1);
				double normTopic1 = getNorm (vector1);
				topicNormMap.put(label, normTopic1);
				//System.out.print("Label added "+label+" \n");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		System.out.println("Finished processing LabelData");
	}
	
	public void readLableData () {
		try {
			FileReader reader = new FileReader(labelInfoFile);
			BufferedReader br = new BufferedReader(reader);
			
			String line = "";
			int count = 0;
			while ((line = br.readLine()) != null) {
				
				if (count % 100 == 0) {
					//System.out.println("Processed content " + count + " lines...");
				}
				
				String[] tokens = line.split("\t");

				if (tokens.length != 2) 
					continue;
				
				String dataID = tokens[0];
				String label = tokens[1];
				dataTrueLabelMap.put(dataID, label);
				//System.out.println("DataId " + dataID  + "label"+ label+"\n");
				count++;
			}
			br.close();
			reader.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished reading LabelData");
	}
	
	
	public void readContentData (String contentInfoFile) {
		try {
			FileReader reader = new FileReader(contentInfoFile);
			BufferedReader br = new BufferedReader(reader);
			
			String line = "";
			int count = 0;
			while ((line = br.readLine()) != null) {
				
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
		System.out.println("Finished reading ContentData, Docnum"+Docnum);
	}
	
	public void calculate_precision(String a) throws IOException{
		FileWriter fw = new FileWriter("/shared/bronte/sling3/data/Wiki-test-result"+a+".txt");
	 
		int failed=0;
		int cor=0;
		int i=0;
		for(;i<Docnum;i++){
			String l=dataTrueLabelMap.get(Integer.toString(i)).toLowerCase();
			Boolean b=labelresults.get(Integer.toString(i)).toLowerCase().equals(l);
			fw.write(i+" "+b+" "+labelresults.get(Integer.toString(i))+"          "+l+"\n");
			//System.out.println("doc id "+i+ "true label "+l);
			String s=labelresults.get(Integer.toString(i));
			if(b)
				cor++;
			if(labelresults.get(Integer.toString(i)).equals("failed labeling"))
				failed++;
		}
		endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;

		System.out.println("Correct  "+cor+" total "+i+" failed label"+failed);
		fw.write("Correct  "+cor+" total "+i+" failed label"+failed+" "+totalTime);
		fw.close();
	}

	public void processContentData() throws Exception{
		int count=0;
		String filePath="/shared/bronte/sling3/data/contents.txt";
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String line = null;
		while ((line = reader.readLine()) != null && count<20589 ) {
			if (line.isEmpty() == true && line.equals("") == true){ 
				continue;
			}	
			String[] tokens = line.split("\t");
			if (tokens.length != 2) {
					continue;
			}
			//System.out.println(tokens[1]+"\n");
			String dataID = tokens[0].trim();
			String conceptStr = tokens[1].trim();
			String[] concepts = conceptStr.split(";");
			List<ConceptData> conceptsList = new ArrayList<ConceptData>();
	
			for (int i = 0; i < concepts.length; ++i) {
				String[] subTokens = concepts[i].split(",");
				String id = subTokens[0];
				double value = Double.parseDouble(subTokens[1]);
				//System.out.println(id+": "+value+"\n");
				ConceptData concept = new ConceptData(id + "", value);
				conceptsList.add(concept);
			}
			Collections.sort(conceptsList);
			contents.put(dataID, conceptsList);
			
			count++;
		}
		Docnum=count;
		System.out.println("Finished processing ContentData, Docnum"+Docnum);
	}
	
	public void test2(int source) throws IOException {
		
		DiskBasedWordEmbedding embedding = new DiskBasedWordEmbedding(); 
		
		labelresults=new HashMap<String, String>();
		
		startTime = System.currentTimeMillis();

		
		for (int i=0;i<Docnum;i++){
			System.out.print("Docid "+i+" ");
			String content = contentMap.get(Integer.toString(i));

			String label="";
			double maxSimilarity=0;
				
			for (String topic : topicVectorMap.keySet()) {
					
				double score = SparseSimilarityCondensation.similarity(embedding, topic, content);
				if(score>maxSimilarity){
					maxSimilarity=score;
					label=topic;
				}
			}
			labelresults.put(Integer.toString(i),label);
			System.out.print("Docid "+i+" label is "+label+" max similarity "+maxSimilarity+" correct label: "+dataTrueLabelMap.get(Integer.toString(i))+"\n");
		}
		

	}
	
	
	
	
	public static void main (String[] args) throws Exception {
		Wiki_test w=new Wiki_test();
		//w.processContentData();
		w.readLableData() ;
		w.readContentData("/shared/bronte/sling3/data/contentInfo.txt");
		w.processLabelData() ;
		w.test2(0);
		w.calculate_precision("only-w2v");
		w.test2(1);
		w.calculate_precision("only-john");
		
	}
	
	
}
