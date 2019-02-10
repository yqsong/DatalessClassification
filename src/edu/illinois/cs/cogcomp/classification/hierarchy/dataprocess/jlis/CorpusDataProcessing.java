package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.jlis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import LBJ2.classify.RealPrimitiveStringFeature;
import LBJ2.learn.Lexicon;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.StopWords;
import edu.illinois.cs.cogcomp.indsup.learning.FeatureVector;
import edu.illinois.cs.cogcomp.indsup.mc.LabeledMultiClassStructure;
import edu.illinois.cs.cogcomp.indsup.mc.LabeledMulticlassData;
import edu.illinois.cs.cogcomp.indsup.mc.MultiClassInstance;

/**
 * yqsong@illinois.edu
 */

public class CorpusDataProcessing {
	
	String tfType = "aug"; //"boolean" "log" "aug"
	HashMap<String, Integer> dfMap;
	HashMap<String, Integer> globalDict;
	HashMap<Integer, String> inverseGlobalDict;
	int nFeatures;
	
	HashMap<String, HashMap<String, Integer>> corpusKVMap;
	HashMap<String, String> corpusLibSVMFormat;
	
	public boolean startFromZero = false; //Liblinear should start from 1

	public CorpusDataProcessing () {
		dfMap = new HashMap<String, Integer>();
		globalDict = new HashMap<String, Integer>();
		inverseGlobalDict = new HashMap<Integer, String>();
		
		corpusKVMap = new HashMap<String, HashMap<String, Integer>> ();
		corpusLibSVMFormat = new HashMap<String, String> ();
		
		if (StopWords.rcvStopWords == null) {
			String stopWordsFile = "data/rcvTest/english.stop";
			StopWords.rcvStopWords = StopWords.readStopWords (stopWordsFile);
		}
	}
	
	Lexicon featureLexicon;

	public Lexicon getGlobalLexicon () {
		if (featureLexicon == null) {
			featureLexicon = new Lexicon();
			
			for (int i = 0; i < getDict().size(); ++i) {
				String word = inverseGlobalDict.get(i);
//				DiscretePrimitiveStringFeature feature = new DiscretePrimitiveStringFeature(
//						"traininglabelpackage",
//						"CorpusDataProcessing",
//						inverseGlobalDict.get(i),
//						"1",
//						(short) i,
//						(short) inverseGlobalDict.size()
//						);
				RealPrimitiveStringFeature feature = new RealPrimitiveStringFeature(
						"trainingfeaturepackage",
						"CorpusDataProcessing",
						inverseGlobalDict.get(i),
						0.0
						);
				featureLexicon.lookup(feature, true);
			}
		}
		return featureLexicon;
	}
	
	public int getFeatureNum () {
		return this.nFeatures;
	}
	
	public int getDictSize () {
		return this.globalDict.size();
	}
	
	public HashMap<String, Integer> getDict () {
		return this.globalDict;
	}
	
	public HashMap<Integer, String> getInvDict () {
		return this.inverseGlobalDict;
	}
	
	////////////////////////////////////////////////////////////////
	// Initialize for testing
	////////////////////////////////////////////////////////////////

	public String convertTestDocContentToTFIDF (String docContent, boolean isTFIDF, boolean isNormalize) {
		String[] tokens = docContent.split("\\s+");
		HashMap<String, Integer> docKVMap = new HashMap<String, Integer>();
		for (int i = 0; i < tokens.length; ++i) {
			if (StopWords.rcvStopWords.contains(tokens[i]) == true ||
					globalDict.containsKey(tokens[i]) == false) {
				continue;
			}
			
			if (docKVMap.containsKey(tokens[i]) == true) {
				docKVMap.put(tokens[i], docKVMap.get(tokens[i]) + 1);
			} else {
				docKVMap.put(tokens[i], 1);
			}
		}
		
		HashMap<Integer, Double> docKVMapNew = new HashMap<Integer, Double>();
		double maxValue = 0;
		if (docKVMap.size() > 0)
			maxValue = (double) Collections.max(docKVMap.values());
		double twoNorm = 0;
		for (String key : docKVMap.keySet()) {
			double value = docKVMap.get(key);
			if (isTFIDF == true) {
				//http://en.wikipedia.org/wiki/Tf-idf
				// tf
				if (tfType.equals("boolean")) {
					value = 1;
				} else if (tfType.equals("log")) {
					value = Math.log(1 + value);
				} else if (tfType.equals("aug")) {
					value = 0.5 + (value * 0.5) / (maxValue + Double.MIN_NORMAL);
				}
				// idf
				double idf = Math.log((corpusKVMap.size() + 0.0000001) / (dfMap.get(key) + 0.0000001));
				// tf-idf
				value = value * idf;
				
			} 
			docKVMapNew.put(globalDict.get(key), value);
			twoNorm += value * value;
		}
		
		twoNorm = Math.sqrt(twoNorm);
		if (isNormalize == true) {
			for (Integer key : docKVMapNew.keySet()) {
				docKVMapNew.put(key, docKVMapNew.get(key) / twoNorm);
			}
		}
		
		String libSVMStr = "";
		Set<Integer> keySet = docKVMapNew.keySet();
		List<Integer> keyList = new ArrayList<Integer>();
		for (Integer keyID : keySet) {
			keyList.add(keyID);
		}
		Collections.sort(keyList);
		for (Integer keyID : keyList) {
			libSVMStr += keyID + ":" + docKVMapNew.get(keyID) + " ";
		}
		return libSVMStr;
	}
	
	public String convertTestDocConceptToTFIDF (String docConcept, boolean isTFIDF, boolean isNormalize) {
		String conceptStr = docConcept;
		String[] tokens = conceptStr.trim().split(";");
		
		double maxValue = 0;
		if (tfType.equals("aug")) {
			for (int i = 0; i < tokens.length; ++i) {
				String[] subtokens = tokens[i].trim().split(",");
				if (subtokens.length != 2) 
					continue;
				double score = Double.parseDouble(subtokens[1].trim());
				if (score > maxValue) {
					maxValue = score;
				}
			}
		}
		double twoNorm = 0;
		HashMap<Integer, Double> docKVMapNew = new HashMap<Integer, Double>();
		for (int i = 0; i < tokens.length; ++i) {
			String[] subtokens = tokens[i].trim().split(",");
			if (subtokens.length != 2) 
				continue;
			
			if (globalDict.containsKey(subtokens[0].trim()) == true) {
				double value = Double.parseDouble(subtokens[1].trim());
				if (isTFIDF == true) {
					if (tfType.equals("boolean")) {
						value = 1;
					} else if (tfType.equals("log")) {
						value = Math.log(1 + value);
					} else if (tfType.equals("aug")) {
						value = 0.5 + (value * 0.5) / (maxValue + Double.MIN_NORMAL);
					}
					// idf
					double idf = Math.log(corpusLibSVMFormat.size() / (dfMap.get(subtokens[0].trim()) + 0.0000001));
					// tf-idf
					value = value * idf;
				}
				docKVMapNew.put(globalDict.get(subtokens[0].trim()), value);
				twoNorm += value * value;
			} 
		
		}
		
		twoNorm = Math.sqrt(twoNorm);
		if (isNormalize == true) {
			for (Integer key : docKVMapNew.keySet()) {
				docKVMapNew.put(key, docKVMapNew.get(key) / twoNorm);
			}
		}
		
		String libSVMStr = "";
		Set<Integer> keySet = docKVMapNew.keySet();
		List<Integer> keyList = new ArrayList<Integer>();
		for (Integer keyID : keySet) {
			keyList.add(keyID);
		}
		Collections.sort(keyList);
		for (Integer keyID : keyList) {
			libSVMStr += keyID + ":" + docKVMapNew.get(keyID) + " ";
			
		}
		return libSVMStr;
	}
	
	public LabeledMulticlassData readMultiClassDataLibSVMStr (List<String> lines, int n_feature, Map<String, Integer> labels_maping) {
		
		LabeledMulticlassData res = new LabeledMulticlassData(labels_maping, n_feature);

		String globalOverLook = "";
		try {
			int n_class = labels_maping.size();
			for (String line : lines) {
				globalOverLook = line;
				
				String[] tokens = line.split("\\s+");
	
				int active_len = 1;
	
				// ignore the features > n_features
				for (int i = 1; i < tokens.length; i++) {
	
					String[] fea_tokens = tokens[i].split(":");
					int idx = Integer.parseInt(fea_tokens[0]); // allocate for
																// bias term
					if (idx <= n_feature) { // only consider the features that has
											// index
											// less than n_fea!!
						active_len++;
					}
				}
	
				// System.out.println("active_len:" + active_len);
				int[] idx_list = new int[active_len];
				double[] value_list = new double[active_len];
	
				for (int i = 1; i < tokens.length; i++) {
					String[] fea_tokens = tokens[i].split(":");
					int idx = Integer.parseInt(fea_tokens[0]); // allocate for
																// bias term
					if (idx <= n_feature) { // only consider the features that has
											// index
						// less than n_fea!!
						idx_list[i - 1] = idx;
						value_list[i - 1] = Double.parseDouble(fea_tokens[1]);
					}
				}
				// append bias term
				idx_list[active_len-1] = n_feature-1;
				value_list[active_len-1] = 1;
	
				FeatureVector fv = new FeatureVector(idx_list, value_list);
				MultiClassInstance mi = new MultiClassInstance(n_feature, n_class,
						fv);
				res.sp.input_list.add(mi);
	
				String lab = tokens[0];
				if (labels_maping.containsKey(lab)) {
					res.sp.output_list.add(new LabeledMultiClassStructure(mi,
							labels_maping.get(lab)));
				} else {
					// only design for unknown classes in the test data
					res.sp.output_list.add(new LabeledMultiClassStructure(mi, -1));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public HashMap<String, String> initializeTrainingDocumentFeatures (HashMap<String, String> corpusStringMap, boolean isTFIDF, boolean isNormalize) {
		
		int globalFeatureID = 0;
		if (startFromZero == false) {
			globalFeatureID = 1;
		}
		int docCount = 0;
		for (String docID : corpusStringMap.keySet()) {
			if (docCount % 1000 == 0) {
				System.out.println("  [Data:]" + " process doc " + docCount + " with tf");
			}
			docCount++;
			
			String docString = corpusStringMap.get(docID);
			String[] tokens = docString.split("\\s+");
			HashMap<String, Integer> docKVMap = new HashMap<String, Integer>();
			for (int i = 0; i < tokens.length; ++i) {
				if (StopWords.rcvStopWords.contains(tokens[i]) == true) {
					continue;
				}
				
				if (docKVMap.containsKey(tokens[i]) == true) {
					docKVMap.put(tokens[i], docKVMap.get(tokens[i]) + 1);
				} else {
					docKVMap.put(tokens[i], 1);
				}
				
				if (globalDict.containsKey(tokens[i]) == false) {
					globalDict.put(tokens[i], globalFeatureID);
					inverseGlobalDict.put(globalFeatureID, tokens[i]);
					globalFeatureID++;
				} 
			}
			for (String token : docKVMap.keySet()) {
				if (dfMap.containsKey(token) == true) {
					dfMap.put(token, dfMap.get(token) + 1);
				} else {
					dfMap.put(token, 1);
				}
			}
			corpusKVMap.put(docID, docKVMap);
		}
		
		nFeatures = globalFeatureID + 1;
				
		docCount = 0;
		for (String docID : corpusKVMap.keySet()) {
			if (docCount % 1000 == 0) {
				System.out.println("  [Data:] process doc " + docCount + " with tfidf");
			}
			docCount++;
			
			HashMap<String, Integer> docKVMap = corpusKVMap.get(docID);
			HashMap<Integer, Double> docKVMapNew = new HashMap<Integer, Double>();
			double maxValue = 0;
			if (docKVMap.size() > 0)
				maxValue = (double) Collections.max(docKVMap.values());
			double twoNorm = 0;
			for (String key : docKVMap.keySet()) {
				double value = docKVMap.get(key);
				if (isTFIDF == true) {
					//http://en.wikipedia.org/wiki/Tf-idf
					// tf
					if (tfType.equals("boolean")) {
						value = 1;
					} else if (tfType.equals("log")) {
						value = Math.log(1 + value);
					} else if (tfType.equals("aug")) {
						value = 0.5 + (value * 0.5) / (maxValue + Double.MIN_NORMAL);
					}
					// idf
					double idf = Math.log(corpusKVMap.size() / (dfMap.get(key) + 0.0000001));
					// tf-idf
					value = value * idf;
				}
				docKVMapNew.put(globalDict.get(key), value);
				twoNorm += value * value;
			}
			twoNorm = Math.sqrt(twoNorm);
			if (isNormalize == true) {
				for (Integer key : docKVMapNew.keySet()) {
					docKVMapNew.put(key, docKVMapNew.get(key) / twoNorm);
				}
			}
			
			String libSVMStr = "";
			Set<Integer> keySet = docKVMapNew.keySet();
			List<Integer> keyList = new ArrayList<Integer>();
			for (Integer keyID : keySet) {
				keyList.add(keyID);
			}
			Collections.sort(keyList);
			for (Integer keyID : keyList) {
				libSVMStr += keyID + ":" + docKVMapNew.get(keyID) + " ";
			}
			corpusLibSVMFormat.put(docID, libSVMStr);
		}
		
		return corpusLibSVMFormat;
	}
	
	
	public HashMap<String, String> initializeTrainingConceptFeatures (HashMap<String, String> corpusStringMap, boolean isTFIDF, boolean isNormalize) {
		
		int globalFeatureID = 0;
		if (startFromZero == false) {
			globalFeatureID = 1;
		}
		int docCount = 0;
		HashMap<Integer, Double> maxValues = new HashMap<Integer, Double>();
		HashMap<Integer, Double> minValues = new HashMap<Integer, Double>();
		for (String docID : corpusStringMap.keySet()) {
			if (docCount % 1000 == 0) {
				System.out.println("  [Data:]" + " process doc " + docCount + " with tf");
			}
			docCount++;
			
			String conceptStr = corpusStringMap.get(docID);
			String[] tokens = conceptStr.trim().split(";");
			for (int i = 0; i < tokens.length; ++i) {
				String[] subtokens = tokens[i].trim().split(",");
				if (subtokens.length != 2) 
					continue;
				if (globalDict.containsKey(subtokens[0].trim()) == false) {
					globalDict.put(subtokens[0].trim(), globalFeatureID);
					inverseGlobalDict.put(globalFeatureID, subtokens[0].trim());
					globalFeatureID++;
				} 
				
				if (dfMap.containsKey(subtokens[0].trim()) == true) {
					dfMap.put(subtokens[0].trim(), dfMap.get(subtokens[0].trim()) + 1);
				} else {
					dfMap.put(subtokens[0].trim(), 1);
				}
				
				double value = Double.parseDouble(subtokens[1].trim());
				
				if (maxValues.containsKey(globalDict.get(subtokens[0].trim())) == false) {
					maxValues.put(globalDict.get(subtokens[0].trim()), value);
				} else {
					if (maxValues.get(globalDict.get(subtokens[0].trim())) < value) {
						maxValues.put(globalDict.get(subtokens[0].trim()), value);
					}
				}
				
				if (minValues.containsKey(globalDict.get(subtokens[0].trim())) == false) {
					minValues.put(globalDict.get(subtokens[0].trim()), value);
				} else {
					if (minValues.get(globalDict.get(subtokens[0].trim())) > value) {
						minValues.put(globalDict.get(subtokens[0].trim()), value);
					}
				}
			}
		}
		
		docCount = 0;
		for (String docID : corpusStringMap.keySet()) {
			if (docCount % 1000 == 0) {
				System.out.println("  [Data:] process doc " + docCount + " with tfidf");
			}
			docCount++;
			
			String conceptStr = corpusStringMap.get(docID);
			String[] tokens = conceptStr.trim().split(";");
			
			double maxValue = 0;
			if (tfType.equals("aug")) {
				for (int i = 0; i < tokens.length; ++i) {
					String[] subtokens = tokens[i].trim().split(",");
					if (subtokens.length != 2) 
						continue;
					double score = Double.parseDouble(subtokens[1].trim());
					if (score > maxValue) {
						maxValue = score;
					}
				}
			}
			
			double twoNorm = 0;
			HashMap<Integer, Double> docKVMapNew = new HashMap<Integer, Double>();
			for (int i = 0; i < tokens.length; ++i) {
				String[] subtokens = tokens[i].trim().split(",");
				if (subtokens.length != 2) 
					continue;
				if (globalDict.containsKey(subtokens[0].trim()) == true) {
					double value = Double.parseDouble(subtokens[1].trim());
					
					if (isTFIDF == true) {
						if (tfType.equals("boolean")) {
							value = 1;
						} else if (tfType.equals("log")) {
							value = Math.log(1 + value);
						} else if (tfType.equals("aug")) {
							value = 0.5 + (value * 0.5) / (maxValue + Double.MIN_NORMAL);
						}
						// idf
						double idf = Math.log(corpusStringMap.size() / (dfMap.get(subtokens[0].trim()) + 0.0000001));
						// tf-idf
						value = value * idf;
						
					}
					twoNorm += value * value;
					docKVMapNew.put(globalDict.get(subtokens[0].trim()), value);
				} 
				
			}
			
			twoNorm = Math.sqrt(twoNorm);
			if (isNormalize == true) {
				for (Integer key : docKVMapNew.keySet()) {
					docKVMapNew.put(key, docKVMapNew.get(key) / twoNorm);
				}
				
//				for (Integer key : docKVMapNew.keySet()) {
//					docKVMapNew.put(key, (docKVMapNew.get(key) - minValues.get(key)) / (maxValues.get(key) - minValues.get(key)));
//				}
			}
			
			String libSVMStr = "";
			Set<Integer> keySet = docKVMapNew.keySet();
			List<Integer> keyList = new ArrayList<Integer>();
			for (Integer keyID : keySet) {
				keyList.add(keyID);
			}
			Collections.sort(keyList);
			for (Integer keyID : keyList) {
				libSVMStr += keyID + ":" + docKVMapNew.get(keyID) + " ";
				
			}
			corpusLibSVMFormat.put(docID, libSVMStr);
		}
		
		nFeatures = globalFeatureID + 1;
				
		return corpusLibSVMFormat;
	}
	
//	public HashMap<String, String> initializeTrainingConceptFeatures (HashMap<String, String> corpusStringMap, boolean isNormalize) {
//		
//		int globalFeatureID = 0;
//		if (startFromZero == false) {
//			globalFeatureID = 1;
//		}
//		int docCount = 0;
//		for (String docID : corpusStringMap.keySet()) {
//			if (docCount % 1000 == 0) {
//				System.out.println("  [Data:]" + " pre-process doc " + docCount + " dictionary");
//			}
//			docCount++;
//			
//			String conceptStr = corpusStringMap.get(docID);
//			String[] tokens = conceptStr.trim().split(";");
//			HashMap<Integer, Double> conceptScoreMap = new HashMap<Integer, Double>();
//			double sum = 0;
//			for (int i = 0; i < tokens.length; ++i) {
//				String[] subtokens = tokens[i].trim().split(",");
//				if (subtokens.length != 2) 
//					continue;
//				if(subtokens[1].equals("Inc.")) {
//					int a = 0;
//				}
//				if (globalDict.containsKey(subtokens[0].trim()) == false) {
//					globalDict.put(subtokens[0].trim(), globalFeatureID);
//					inverseGlobalDict.put(globalFeatureID, subtokens[0].trim());
//					globalFeatureID++;
//				} 
//				double score = Double.parseDouble(subtokens[1].trim());
//				sum += score * score;
//				conceptScoreMap.put(globalDict.get(subtokens[0].trim()), score);
//			}
//			
//			sum = Math.sqrt(sum);
//			String libSVMStr = "";
//			Set<Integer> keySet = conceptScoreMap.keySet();
//			List<Integer> keyList = new ArrayList<Integer>();
//			for (Integer keyID : keySet) {
//				keyList.add(keyID);
//			}
//			Collections.sort(keyList);
//			for (Integer keyID : keyList) {
//				if (isNormalize == false) {
//					libSVMStr += keyID + ":" + conceptScoreMap.get(keyID) + " ";
//				} else {
//					double score = conceptScoreMap.get(keyID) / sum;
//					libSVMStr += keyID + ":" + score + " ";
//				}
//				
//			}
//			corpusLibSVMFormat.put(docID, libSVMStr);
//		}
//		
//		nFeatures = globalFeatureID + 1;
//				
//		return corpusLibSVMFormat;
//	}
}
