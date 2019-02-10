package edu.illinois.cs.cogcomp.classification.densification.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.illinois.cs.cogcomp.classification.densification.representation.FileUtils;
import edu.illinois.cs.cogcomp.classification.densification.representation.SparseSimilarityCondensation;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsTopicHierarchy;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.representation.esa.AbstractESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.DiskBasedComplexESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.MemoryBasedESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.simple.SimpleESALocal;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.WordEmbeddingInterface;


public class NG_test {
	//static ComplexESALocal esa = new ComplexESALocal();
	static int numConcepts = 500;
	
	public static void main (String[] args) throws IOException {
	//	new SparseSimilarityCondensation("1","1",1.0,1.0).dumpvector();
		String topic1 = "rec.autos";
		String topic2 = "rec.motorcycles"; // sci.electronics rec.motorcycles
		int splitNum = 16;
		//testsimpleESA(topic1,topic2,splitNum);
		/*testwordDic(0,0.85,topic1,topic2,splitNum,50,1);
		testwordDic(0,0.85,topic1,topic2,splitNum,100,0);
		testwordDic(0,0.85,topic1,topic2,splitNum,100,1);
		testwordDic(0,0.85,topic1,topic2,splitNum,200,0);
		testwordDic(0,0.85,topic1,topic2,splitNum,200,1);
		testwordDic(0,0.85,topic1,topic2,splitNum,500,0);
		testwordDic(0,0.85,topic1,topic2,splitNum,500,1);
		testwordDic(0,0.85,topic1,topic2,splitNum,1000,0);
		testwordDic(0,0.85,topic1,topic2,splitNum,1000,1);
		//testwordDic(1,0.9,topic1,topic2,splitNum);
		
		splitNum = 1;
		testwordDic(0,0.85,topic1,topic2,splitNum,50,1);
		testwordDic(0,0.85,topic1,topic2,splitNum,100,0);
		testwordDic(0,0.85,topic1,topic2,splitNum,100,1);
		testwordDic(0,0.85,topic1,topic2,splitNum,200,0);
		testwordDic(0,0.85,topic1,topic2,splitNum,200,1);
		testwordDic(0,0.85,topic1,topic2,splitNum,500,0);
		testwordDic(0,0.85,topic1,topic2,splitNum,500,1);
		testwordDic(0,0.85,topic1,topic2,splitNum,1000,0);
		testwordDic(0,0.85,topic1,topic2,splitNum,1000,1);
		//testwordDic(1,0.9,topic1,topic2,splitNum);

		
		
		topic1 = "rec.autos";
		topic2 = "sci.electronics"; // sci.electronics rec.motorcycles
		testwordDic(0,0.85,topic1,topic2,splitNum,50,1);
		testwordDic(0,0.85,topic1,topic2,splitNum,100,0);
		testwordDic(0,0.85,topic1,topic2,splitNum,100,1);
		testwordDic(0,0.85,topic1,topic2,splitNum,200,0);
		testwordDic(0,0.85,topic1,topic2,splitNum,200,1);
		testwordDic(0,0.85,topic1,topic2,splitNum,500,0);
		testwordDic(0,0.85,topic1,topic2,splitNum,500,1);
		testwordDic(0,0.85,topic1,topic2,splitNum,1000,0);
		testwordDic(0,0.85,topic1,topic2,splitNum,1000,1);
		//testwordDic(1,0.9,topic1,topic2,splitNum);
		

		splitNum = 16;
		testwordDic(0,0.85,topic1,topic2,splitNum,50,1);
		testwordDic(0,0.85,topic1,topic2,splitNum,100,0);
		testwordDic(0,0.85,topic1,topic2,splitNum,100,1);
		testwordDic(0,0.85,topic1,topic2,splitNum,200,0);
		testwordDic(0,0.85,topic1,topic2,splitNum,200,1);
		testwordDic(0,0.85,topic1,topic2,splitNum,500,0);
		testwordDic(0,0.85,topic1,topic2,splitNum,500,1);
		testwordDic(0,0.85,topic1,topic2,splitNum,1000,0);
		testwordDic(0,0.85,topic1,topic2,splitNum,1000,1);
		//testwordDic(1,0.9,topic1,topic2,splitNum);
*/
		testwordDic(0,0.85,topic1,topic2,splitNum,750,1);
		splitNum = 1;
		testwordDic(0,0.85,topic1,topic2,splitNum,750,1);
		topic1 = "rec.autos";
		topic2 = "sci.electronics"; // sci.electronics rec.motorcycles

		testwordDic(0,0.85,topic1,topic2,splitNum,750,1);
		splitNum = 16;
		testwordDic(0,0.85,topic1,topic2,splitNum,750,1);
	
		
		

		
	}
	
	public static void testsimpleESA(String topic1,String topic2,int splitNum){
		MemoryBasedESA esa = new MemoryBasedESA();
		int count=0;
		List<String> docList1 = readIndex(topic1);
		List<String> docList2 = readIndex(topic2);
		
		List<String> docList = new ArrayList<String>();
		List<String> labelList = new ArrayList<String>();
		for (int i = 0; i < docList1.size(); ++i) {
			if (docList1.get(i).equals("") == true) 
				continue;
			
			String doc = docList1.get(i);
			List<String> splitDoc = splitDoc (doc, splitNum) ;
			
			for (int j = 0; j < splitDoc.size(); ++j) {
				docList.add(splitDoc.get(j));
				labelList.add(topic1);
			}
			
		}
		
		for (int i = 0; i < docList2.size(); ++i) {
			if (docList2.get(i).equals("") == true) 
				continue;
			
			String doc = docList2.get(i);
			List<String> splitDoc = splitDoc (doc, splitNum) ;
			
			for (int j = 0; j < splitDoc.size(); ++j) {
				docList.add(splitDoc.get(j));
				labelList.add(topic2);
			}
		}
		
		NewsgroupsTopicHierarchy topicMap = new NewsgroupsTopicHierarchy();
		
		HashMap<String, HashMap<String, Double>> topicVectorMap = new HashMap<String, HashMap<String, Double>>();
		HashMap<String, Double> topicNormMap = new HashMap<String, Double>();
		
		List<ConceptData> vectorTopic1;
		try {
			vectorTopic1  = esa.retrieveConcepts(topicMap.topicMapping.get(topic1), numConcepts, "tfidfVector");
			HashMap<String, Double> vector1 = getVectorMap(vectorTopic1);
			topicVectorMap.put(topic1, vector1);
			double normTopic1 = getNorm (vector1);
			topicNormMap.put(topic1, normTopic1);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		List<ConceptData> vectorTopic2;
		try {
			vectorTopic2  = esa.retrieveConcepts(topicMap.topicMapping.get(topic2), numConcepts, "tfidfVector");
			HashMap<String, Double> vector2 = getVectorMap(vectorTopic2);
			topicVectorMap.put(topic2, vector2);
			double normTopic2 = getNorm (vector2);
			topicNormMap.put(topic2, normTopic2);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			FileWriter writer = new FileWriter("/shared/bronte/sling3/data/test/MemoryBasedESA"+"_"+splitNum+"_"+topic1+"_"+topic2);
			
			for (int i = 0; i < docList.size(); ++i) {

				if (i % 10 == 0) {
					System.out.println("[Count]  Processed " + i + " lines ");
				}

				String line = docList.get(i);
				if (line.equals("") == true)
					continue;
				
				String id = i + "";
				String sentence = docList.get(i);
				String goldLabel = labelList.get(i);
				
	
				List<ConceptData> conceptList = esa.retrieveConcepts(sentence, numConcepts, "tfidfVector");
				HashMap<String, Double> vector = getVectorMap(conceptList);
				double normSentence = getNorm (vector);
		
				writer.write(id + "\t" + goldLabel + "\t");
				double maxscore=0.0;
				String label="";
				for (String topic : topicVectorMap.keySet()) {
					HashMap<String, Double> vectorTopic = topicVectorMap.get(topic);
					double normTopic = topicNormMap.get(topic);
					
					double score =  cosine (vectorTopic, vector, normSentence, normTopic);
					if(score>maxscore){
						maxscore=score;
						label=topic;
					}
					writer.write(topic + "--" + score + "\t");
				}
				if(goldLabel.equals(label)) count++;
				writer.write(ClassifierConstant.systemNewLine);
				writer.flush();
			}
			writer.write("correct number" +count +" accuray: "+count/1999+ "\t");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		
		
	}
	public static void testwordDic (int sourceType, double threshold,
			String topic1, String topic2, int splitNum,int count, int type) {
		String outputFoler = "/shared/bronte/sling3/data/test/" + numConcepts + "memorybased-wordDic" + sourceType + "-threshold" + threshold + "/";

		File file = new File (outputFoler);
		if (file.exists() == false) {
			file.mkdirs();
		}
		
		String complexVectorType = "tfidfVector";
		
		DiskBasedComplexESA esa = new DiskBasedComplexESA ();
		/*similarityGeneration_Direct2 (vectorCondensation,
				esa, numConcepts, complexVectorType,
				outputFoler + topic1 + "-" + topic2 + "-" + splitNum +"-"+ type+"-"+count+"-output.txt",
				topic1, topic2, splitNum);
				*/
	}
	
	public static void testComplexESA (String topic1, String topic2, int splitNum) {
		String outputFoler = "/shared/bronte/sling3/data/test/" + numConcepts + "ESA-complex-" + "tfidfVector" + "/";

		File file = new File (outputFoler);
		if (file.exists() == false) {
			file.mkdirs();
		}
		
		String complexVectorType = "tfidfVector";
		
		DiskBasedComplexESA esa = new DiskBasedComplexESA ();
		similarityGeneration_Direct (esa, numConcepts, complexVectorType,
				outputFoler + topic1 + "-" + topic2 + "-" + splitNum + "-output.txt",
				topic1, topic2, splitNum);
	}
	
	public static void testskipgram(int sourceType, double threshold,
			String topic1, String topic2, int splitNum, int count, int type){
		String outputFoler = "/shared/bronte/sling3/data/test/" + numConcepts + "ESA-complex-maxmatching-symmetric-sourceType" + sourceType + "-threshold" + threshold + "/";

		File file = new File (outputFoler);
		if (file.exists() == false) {
			file.mkdirs();
		}
		
		String complexVectorType = "tfidfVector";
		
		String matchingType = SparseSimilarityCondensation.matchingTypes[0];
		double sig = 0.05;
		SparseSimilarityCondensation vectorCondensation = new SparseSimilarityCondensation(
				matchingType, threshold, sig, count, type); 
		
		DiskBasedComplexESA esa = new DiskBasedComplexESA ();
		similarityGeneration_Condensation (vectorCondensation,
				esa, numConcepts, complexVectorType,
				outputFoler + topic1 + "-" + topic2 + "-" + splitNum +"-"+ type+"-"+count+"-output.txt",
				topic1, topic2, splitNum);
	}
	
	public static void testComplexESA_MaxMatching (int sourceType, double threshold,
			String topic1, String topic2, int splitNum) {
		String outputFoler = "/shared/bronte/sling3/data/test/" + numConcepts + "ESA-complex-maxmatching-symmetric-sourceType" + sourceType + "-threshold" + threshold + "/";

		File file = new File (outputFoler);
		if (file.exists() == false) {
			file.mkdirs();
		}
		
		String complexVectorType = "tfidfVector";
		
		String matchingType = SparseSimilarityCondensation.matchingTypes[0];
		double sig = 0.05;
		SparseSimilarityCondensation vectorCondensation = new SparseSimilarityCondensation(
				 matchingType, threshold, sig, false); 
		
		DiskBasedComplexESA esa = new DiskBasedComplexESA ();
		similarityGeneration_Condensation (vectorCondensation,
				esa, numConcepts, complexVectorType,
				outputFoler + topic1 + "-" + topic2 + "-" + splitNum + "-output.txt",
				topic1, topic2, splitNum);
	}
	
	
	public static HashMap<String, String> titleIDMap (String file) {
		HashMap<String, String> map = new HashMap<String, String>();
		
		List<String> lines = FileUtils.ReadWholeFileAsLines(file);
		for (int i = 0; i < lines.size(); ++i) {
			String[] tokens = lines.get(i).split("\t");
			if (tokens.length != 3) 
				continue;
			
			map.put(tokens[2], tokens[0]);
		}
		
		return map;
	}

	public static void similarityGeneration_Condensation (
			SparseSimilarityCondensation vectorCondensation, 
			AbstractESA esa, int numConcepts, String complexVectorType,
			String outputFile,
			String topic1, String topic2, int splitNum) {
		int count=0;
		List<String> docList1 = readIndex(topic1);
		List<String> docList2 = readIndex(topic2);
		
		List<String> docList = new ArrayList<String>();
		List<String> labelList = new ArrayList<String>();
		for (int i = 0; i < docList1.size(); ++i) {
			if (docList1.get(i).equals("") == true) 
				continue;
			
			String doc = docList1.get(i);
			List<String> splitDoc = splitDoc (doc, splitNum) ;
			
			for (int j = 0; j < splitDoc.size(); ++j) {
				docList.add(splitDoc.get(j));
				labelList.add(topic1);
			}
			
		}
		
		for (int i = 0; i < docList2.size(); ++i) {
			if (docList2.get(i).equals("") == true) 
				continue;
			
			String doc = docList2.get(i);
			List<String> splitDoc = splitDoc (doc, splitNum) ;
			
			for (int j = 0; j < splitDoc.size(); ++j) {
				docList.add(splitDoc.get(j));
				labelList.add(topic2);
			}
		}
		
		NewsgroupsTopicHierarchy topicMap = new NewsgroupsTopicHierarchy();
		
		HashMap<String, HashMap<String, Double>> topicVectorMap = new HashMap<String, HashMap<String, Double>>();
		HashMap<String, Double> topicNormMap = new HashMap<String, Double>();
		
		List<ConceptData> vectorTopic1;
		try {
			vectorTopic1 = esa.retrieveConcepts(topicMap.topicMapping.get(topic1), numConcepts, complexVectorType);
			HashMap<String, Double> vector1 = getVectorMap(vectorTopic1);
			topicVectorMap.put(topic1, vector1);
			double normTopic1 = getNorm (vector1);
			topicNormMap.put(topic1, normTopic1);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		List<ConceptData> vectorTopic2;
		try {
			vectorTopic2 = esa.retrieveConcepts(topicMap.topicMapping.get(topic2), numConcepts, complexVectorType);
			HashMap<String, Double> vector2 = getVectorMap(vectorTopic2);
			topicVectorMap.put(topic2, vector2);
			double normTopic2 = getNorm (vector2);
			topicNormMap.put(topic2, normTopic2);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			FileWriter writer = new FileWriter(outputFile);
			
			for (int i = 0; i < docList.size(); ++i) {

				if (i % 10 == 0) {
					System.out.println("[Count]  Processed " + i + " lines ");
				}

				String line = docList.get(i);
				if (line.equals("") == true)
					continue;
				
				String id = i + "";
				String sentence = docList.get(i);
				String goldLabel = labelList.get(i);
				
				
				List<ConceptData> conceptList = esa.retrieveConcepts(sentence, numConcepts, complexVectorType);
				HashMap<String, Double> vector = getVectorMap(conceptList);
				double normSentence = getNorm (vector);
		
				writer.write(id + "\t" + goldLabel + "\t");
				double maxscore=0.0;
				String label="";
				for (String topic : topicVectorMap.keySet()) {
					HashMap<String, Double> vectorTopic = topicVectorMap.get(topic);
					double normTopic = topicNormMap.get(topic);
					
					double score = vectorCondensation.similarityWithMaxMatching(vectorTopic, vector, normSentence, normTopic);
					if(score>maxscore){
						maxscore=score;
						label=topic;
					}
					writer.write(topic + "--" + score + "\t");
				}
				if(goldLabel.equals(label)) count++;
				writer.write(ClassifierConstant.systemNewLine);
				writer.flush();
			}
			writer.write("correct number" +count +" accuray: "+count/1999+ "\t");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void similarityGeneration_Direct (
			AbstractESA esa, int numConcepts, String complexVectorType,
			String outputFile,
			String topic1, String topic2, int splitNum) {
		
		List<String> docList1 = readIndex(topic1);
		List<String> docList2 = readIndex(topic2);
		
		List<String> docList = new ArrayList<String>();
		List<String> labelList = new ArrayList<String>();
		for (int i = 0; i < docList1.size(); ++i) {
			if (docList1.get(i).equals("") == true) 
				continue;
			
			String doc = docList1.get(i);
			List<String> splitDoc = splitDoc (doc, splitNum) ;
			
			for (int j = 0; j < splitDoc.size(); ++j) {
				docList.add(splitDoc.get(j));
				labelList.add(topic1);
			}
			
		}
		
		for (int i = 0; i < docList2.size(); ++i) {
			if (docList2.get(i).equals("") == true) 
				continue;
			
			String doc = docList2.get(i);
			List<String> splitDoc = splitDoc (doc, splitNum) ;
			
			for (int j = 0; j < splitDoc.size(); ++j) {
				docList.add(splitDoc.get(j));
				labelList.add(topic2);
			}
		}
		
		NewsgroupsTopicHierarchy topicMap = new NewsgroupsTopicHierarchy();
		
		HashMap<String, HashMap<String, Double>> topicVectorMap = new HashMap<String, HashMap<String, Double>>();
		HashMap<String, Double> topicNormMap = new HashMap<String, Double>();
		
		List<ConceptData> vectorTopic1;
		try {
			vectorTopic1 = esa.retrieveConcepts(topicMap.topicMapping.get(topic1), numConcepts, complexVectorType);
			HashMap<String, Double> vector1 = getVectorMap(vectorTopic1);
			topicVectorMap.put(topic1, vector1);
			double normTopic1 = getNorm (vector1);
			topicNormMap.put(topic1, normTopic1);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		List<ConceptData> vectorTopic2;
		try {
			vectorTopic2 = esa.retrieveConcepts(topicMap.topicMapping.get(topic2), numConcepts, complexVectorType);
			HashMap<String, Double> vector2 = getVectorMap(vectorTopic2);
			topicVectorMap.put(topic2, vector2);
			double normTopic2 = getNorm (vector2);
			topicNormMap.put(topic2, normTopic2);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			FileWriter writer = new FileWriter(outputFile);
			
			for (int i = 0; i < docList.size(); ++i) {

				if (i % 10 == 0) {
					System.out.println("[Count]  Processed " + i + " lines ");
				}

				String line = docList.get(i);
				if (line.equals("") == true)
					continue;
				
				String id = i + "";
				String sentence = docList.get(i);
				String goldLabel = labelList.get(i);
				
				
				List<ConceptData> conceptList = esa.retrieveConcepts(sentence, numConcepts, complexVectorType);
				HashMap<String, Double> vector = getVectorMap(conceptList);
				double normSentence = getNorm (vector);
		
				writer.write(id + "\t" + goldLabel + "\t");
				
				for (String topic : topicVectorMap.keySet()) {
					HashMap<String, Double> vectorTopic = topicVectorMap.get(topic);
					double normTopic = topicNormMap.get(topic);
					
					double score = cosine (vectorTopic, vector, normSentence, normTopic);

					writer.write(topic + "--" + score + "\t");
				}
				
				writer.write(ClassifierConstant.systemNewLine);
				writer.flush();
			}
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
	
	public static void similarityGeneration_Direct2 (
			WordEmbeddingInterface wordEmbedding,
			AbstractESA esa, int numConcepts, String complexVectorType,
			String outputFile,
			String topic1, String topic2, int splitNum) {
		
		int count=0;
		List<String> docList1 = readIndex(topic1);
		List<String> docList2 = readIndex(topic2);
		
		List<String> docList = new ArrayList<String>();
		List<String> labelList = new ArrayList<String>();
		for (int i = 0; i < docList1.size(); ++i) {
			if (docList1.get(i).equals("") == true) 
				continue;
			
			String doc = docList1.get(i);
			List<String> splitDoc = splitDoc (doc, splitNum) ;
			
			for (int j = 0; j < splitDoc.size(); ++j) {
				docList.add(splitDoc.get(j));
				labelList.add(topic1);
			}
			
		}
		
		for (int i = 0; i < docList2.size(); ++i) {
			if (docList2.get(i).equals("") == true) 
				continue;
			
			String doc = docList2.get(i);
			List<String> splitDoc = splitDoc (doc, splitNum) ;
			
			for (int j = 0; j < splitDoc.size(); ++j) {
				docList.add(splitDoc.get(j));
				labelList.add(topic2);
			}
		}
		
		NewsgroupsTopicHierarchy topicMap = new NewsgroupsTopicHierarchy();
		
		HashMap<String, HashMap<String, Double>> topicVectorMap = new HashMap<String, HashMap<String, Double>>();
		HashMap<String, Double> topicNormMap = new HashMap<String, Double>();
		
		List<ConceptData> vectorTopic1;
		try {
			vectorTopic1 = esa.retrieveConcepts(topicMap.topicMapping.get(topic1), numConcepts, complexVectorType);
			HashMap<String, Double> vector1 = getVectorMap(vectorTopic1);
			topicVectorMap.put(topic1, vector1);
			double normTopic1 = getNorm (vector1);
			topicNormMap.put(topic1, normTopic1);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		List<ConceptData> vectorTopic2;
		try {
			vectorTopic2 = esa.retrieveConcepts(topicMap.topicMapping.get(topic2), numConcepts, complexVectorType);
			HashMap<String, Double> vector2 = getVectorMap(vectorTopic2);
			topicVectorMap.put(topic2, vector2);
			double normTopic2 = getNorm (vector2);
			topicNormMap.put(topic2, normTopic2);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			FileWriter writer = new FileWriter(outputFile);
			
			for (int i = 0; i < docList.size(); ++i) {

				if (i % 10 == 0) {
					System.out.println("[Count]  Processed " + i + " lines ");
				}

				String line = docList.get(i);
				if (line.equals("") == true)
					continue;
				
				String id = i + "";
				String sentence = docList.get(i);
				String goldLabel = labelList.get(i);
				
	
		
				writer.write(id + "\t" + goldLabel + "\t");
				double maxscore=0.0;
				String label="";
				for (String topic : topicVectorMap.keySet()) {

					
					double score = SparseSimilarityCondensation.similarity(wordEmbedding, topicMap.topicMapping.get(topic), sentence);
					if(score>maxscore){
						maxscore=score;
						label=topic;
					}
					writer.write(topic + "--" + score + "\t");
				}
				if(goldLabel.equals(label)) count++;
				writer.write(ClassifierConstant.systemNewLine);
				writer.flush();
			}
			writer.write("correct number" +count +" accuray: "+count/1999+ "\t");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
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
	
	public static double cosine (HashMap<String, Double> v1, HashMap<String, Double> v2,
			double norm1, double norm2) {

		double dot = 0;
		if (v1.size() < v2.size()) {
			for (String key : v1.keySet()) {
				if (v2.containsKey(key) == true) {
					double value1 = v1.get(key);
					double value2 = v2.get(key);
					dot += value1 * value2;
				}
			}
		} else {
			for (String key : v2.keySet()) {
				if (v1.containsKey(key) == true) {
					double value1 = v1.get(key);
					double value2 = v2.get(key);
					dot += value1 * value2;
				}
			}
		}
		
		return dot / (Double.MIN_NORMAL + norm1) / (Double.MIN_NORMAL + norm2);
	}
	
	public static double cosine (HashMap<String, Double> v1, HashMap<String, Double> v2) {

		double norm1 = getNorm(v1);
		double norm2 = getNorm(v2);
		
		double dot = cosine(v1, v2, norm1, norm2);
		
		return dot;
	}

	
	public static void doStats (String outputPath, String topic1, String topic2, int splitNum) {
		
//		NewsgroupsTopicHierarchy topicMap = new NewsgroupsTopicHierarchy();
//		
//		HashMap<Integer, Double> vectorTopic1 = esa.getConceptVectorBasedonSegmentation(topicMap.topicMapping.get(topic1), numConcepts, "tfidfVector");
//		HashSet<Integer> topic1ConceptSet = new HashSet<Integer>(vectorTopic1.keySet());
//		HashMap<Integer, Double> vectorTopic2 = esa.getConceptVectorBasedonSegmentation(topicMap.topicMapping.get(topic2), numConcepts, "tfidfVector");
//		HashSet<Integer> topic2ConceptSet = new HashSet<Integer>(vectorTopic2.keySet());
//		
//		HashMap<Integer, Double> avgConceptOverlap = new HashMap<Integer, Double>();
//		
//		double avgOverlap = 0;
		
		List<String> docList1 = readIndex(topic1);
		List<String> docList2 = readIndex(topic2);
		
		List<String> docList = new ArrayList<String>();
		List<String> labelList = new ArrayList<String>();
		for (int i = 0; i < docList1.size(); ++i) {
			if (docList1.get(i).equals("") == true) 
				continue;
			
			String doc = docList1.get(i);
			List<String> splitDoc = splitDoc (doc, splitNum) ;
			
			for (int j = 0; j < splitDoc.size(); ++j) {
				docList.add(splitDoc.get(j));
				labelList.add(topic1);
			}
			
		}
		
		for (int i = 0; i < docList2.size(); ++i) {
			if (docList2.get(i).equals("") == true) 
				continue;
			
			String doc = docList2.get(i);
			List<String> splitDoc = splitDoc (doc, splitNum) ;
			
			for (int j = 0; j < splitDoc.size(); ++j) {
				docList.add(splitDoc.get(j));
				labelList.add(topic2);
			}
		}
	}
	
	
	public static List<String> readIndex (String selectedTopic) {
		List<String> docList = new ArrayList<String>();
		try {
			String inputDirStr = "data/20newsgroups/textindex";
			Directory inputDir = FSDirectory.open(new File(inputDirStr));
			IndexReader reader = IndexReader.open(inputDir, true);
			int maxDocNum = reader.maxDoc();
			
			
			for (int i = 0; i < maxDocNum; ++i) {
				if (i % 1000 == 0) {
					System.out.println("  [read]: " + i + " documents..");
				}
				
				if (reader.isDeleted(i) == false) { 
					Document doc = reader.document(i);
					
					String topic = doc.get("newsgroup");
					
					if (topic.equals(selectedTopic) == false) 
						continue;
					
					String text = doc.get("plain"); //doc.get("Body");
					if (doc.get("Subject") != null) {
						text += " " + doc.get("Subject");
					}
					text = text.replaceAll("\n", " ");
					text = text.replaceAll("\r", " ");
					text = text.replaceAll("\t", " ");
					text = text.replaceAll("\\pP", " ");
					//>|<=+`~!@#$%^&*()-_{}
//					text = text.replaceAll("[>|<=+@#$%^&*()_{}]", " ");
					text = text.replaceAll("[>|<=+`~!@#$%^&*()_{}]", " ");
					text = text.replaceAll("[1-9]", " ");
					
//					text = text.replaceAll("[^a-zA-Z\\s]", "");
					
					docList.add(text);
					
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return docList;
		
	}
	
	public static List<String> splitDoc (String doc, int num) {
		
		String[] tokens = doc.split("\\s+");

		List<String> tokenList = new ArrayList<String>();
		for (int i = 0; i < tokens.length; ++i)	{
			tokenList.add(tokens[i]);
		}
		
		List<String> splitDocList = new ArrayList<String>();
		
		int newDocLength = tokens.length / num;
		
		for (int i = 0; i < num; ++i) {
			List<String> subList = tokenList.subList(i * newDocLength , Math.min((i+1) * newDocLength, tokenList.size()));
			
			String subDoc = "";
			for (int j = 0; j < subList.size(); ++j) {
				subDoc += subList.get(j) + " ";
			}
			splitDocList.add(subDoc);
		}
		
		return splitDocList;
		
	}
	
	
}
