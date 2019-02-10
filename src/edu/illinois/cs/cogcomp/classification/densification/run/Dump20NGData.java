package edu.illinois.cs.cogcomp.classification.densification.run;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.illinois.cs.cogcomp.classification.densification.representation.DensificationData;
import edu.illinois.cs.cogcomp.classification.densification.representation.SparseSimilarityCondensation;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsTopicHierarchy;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.representation.esa.AbstractESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.DiskBasedComplexESA;

public class Dump20NGData {
	static DiskBasedComplexESA esa = new DiskBasedComplexESA();
	
	public static void main (String[] args) {
		
		String topic1 = "rec.autos";
		String topic2 = "rec.motorcycles";
		int splitNum = 1;
	
		String outputDocumentFolder = "/shared/shelley/yqsong/data/20ngSim/outputMatlab/document-" + splitNum + "-" + topic1 + "-" + topic2;
		String outputLabelFolder = "/shared/shelley/yqsong/data/20ngSim/outputMatlab/" + topic1 + "-" + topic2;

		File file = new File (outputDocumentFolder);
		if (file.exists() == false) {
			file.mkdirs();
		}
		file = new File (outputLabelFolder);
		if (file.exists() == false) {
			file.mkdirs();
		}
		
		int numConcepts = 500;
		String complexVectorType = "tfidfVector";
		
		String matchingType = SparseSimilarityCondensation.matchingTypes[1];
		double threshold = 0.0;
		double sig2 = 0.0;
		SparseSimilarityCondensation vectorCondensation = new SparseSimilarityCondensation(
				matchingType, threshold, sig2, false); 
		
		DiskBasedComplexESA esa = new DiskBasedComplexESA ();
		dumpVectorOfVectors (vectorCondensation,
				esa, numConcepts, complexVectorType,
				outputDocumentFolder, outputLabelFolder,
				topic1, topic2, splitNum);
		
	}
	

	

	public static void dumpVectorOfVectors (
			SparseSimilarityCondensation vectorCondensation, 
			AbstractESA esa, int numConcepts, String complexVectorType,
			String outputDataFolder, String outputLabelFolder,
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
			
			FileWriter writer = new FileWriter(outputLabelFolder + "/" + topic1 + ".txt");

			writer.write(topic1 + "\t");
			
			writer.write(vector1.size() + ClassifierConstant.systemNewLine);
					
			DensificationData data = vectorCondensation.getVectorOfVectors(vector1);
			
			for (String key : data.vector.keySet()) {
				double value = data.vector.get(key);
				
				writer.write(key + "\t" + value + ClassifierConstant.systemNewLine);
				
				double[] features= data.vectorOfVectors.get(key);
				if (features != null && features.length > 0) {
					for (int j = 0; j < features.length; ++j) {
						writer.write(features[j] + "\t");
					}
				}
				writer.write(ClassifierConstant.systemNewLine);
			}
			
			writer.flush();
			writer.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		List<ConceptData> vectorTopic2;
		try {
			vectorTopic2 = esa.retrieveConcepts(topicMap.topicMapping.get(topic2), numConcepts, complexVectorType);
			HashMap<String, Double> vector2 = getVectorMap(vectorTopic2);
			topicVectorMap.put(topic2, vector2);
			
			FileWriter writer = new FileWriter(outputLabelFolder + "/" + topic2 + ".txt");

			writer.write(topic2 + "\t");
			
			writer.write(vector2.size() + ClassifierConstant.systemNewLine);
					
			DensificationData data = vectorCondensation.getVectorOfVectors(vector2);
			
			for (String key : data.vector.keySet()) {
				double value = data.vector.get(key);
				
				writer.write(key + "\t" + value + ClassifierConstant.systemNewLine);
				
				double[] features= data.vectorOfVectors.get(key);
				if (features != null && features.length > 0) {
					for (int j = 0; j < features.length; ++j) {
						writer.write(features[j] + "\t");
					}
				}
				writer.write(ClassifierConstant.systemNewLine);
			}
			
			writer.flush();
			writer.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			
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
		
				FileWriter writer = new FileWriter(outputDataFolder + "/" + i + ".txt");

				writer.write(goldLabel + "\t");
				
				writer.write(vector.size() + ClassifierConstant.systemNewLine);
						
				DensificationData data = vectorCondensation.getVectorOfVectors(vector);
				
				for (String key : data.vector.keySet()) {
					double value = data.vector.get(key);
					
					writer.write(key + "\t" + value + ClassifierConstant.systemNewLine);
					
					double[] features= data.vectorOfVectors.get(key);
					if (features != null && features.length > 0) {
						for (int j = 0; j < features.length; ++j) {
							writer.write(features[j] + "\t");
						}
					}
					writer.write(ClassifierConstant.systemNewLine);
				}
				
				writer.flush();
				writer.close();
			}
			
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
	
	

	
	
	public static List<String> readIndex (String selectedTopic) {
		List<String> docList = new ArrayList<String>();
		try {
			String inputDirStr = "/shared/corpora/yqsong/data/benchmark/20newsgroups/textindex";
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
