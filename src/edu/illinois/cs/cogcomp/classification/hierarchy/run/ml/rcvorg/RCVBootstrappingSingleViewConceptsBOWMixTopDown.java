package edu.illinois.cs.cogcomp.classification.hierarchy.run.ml.rcvorg;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.AbstractClassifierLBJTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.ml.ClassifierLBJTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.ml.ClassifierLBJTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.AbstractClassifierLibLinearTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.ml.ClassifierLibLinearTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.ml.ClassifierLibLinearTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.AbstractLabelTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelConceptClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelContentClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.AbstractConceptTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.StopWords;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.EvalResults;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.Evaluation;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.StatUtils;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

public class RCVBootstrappingSingleViewConceptsBOWMixTopDown {

	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();

	public static void main (String[] args) {
		testMix  ("topdown", 5);
	}
	
	static double bootstrappingThreshold = 5;
	static double conceptSimilaritygThreshold = 0.0; 

	
	public static void testMix (String direction, int iter) {
		
		if (direction.equals("topdown")) {
			bootstrappingThreshold = 5;
		} else {
			bootstrappingThreshold = 0.5;
			conceptSimilaritygThreshold = 0.01;
		}
		
//		 = "topdown"; //"bottomup";
		String learningMethod = "liblinear";
		double trainingRate = 0.8;
		try {
			List<Double> precisionList = new ArrayList<Double>();
			List<Double> recallList = new ArrayList<Double>();
			List<Double> mf1List = new ArrayList<Double>();
			List<Double> Mf1List = new ArrayList<Double>();
			FileWriter writerFinalResults = null;
			if (ClassifierConstant.isServer == true) {
				writerFinalResults = new FileWriter("/shared/saruman/yqsong/temp/rcv_bootstrapping20NG_Mix_" + direction + ".txt");
			} else {
				writerFinalResults = new FileWriter("C:/Users/yqsong/Downloads/rcv_bootstrapping20NG_Mix_" + direction + ".txt");
			}
			for (int i = 0; i < iter; ++i) {
				double penalty = 1000;//Math.pow(10, i+1);//
				
				int addNum = 4 * 5;
				
				EvalResults result = bootstrapping20NG_Mix(addNum, (int)(120 / addNum), 1, direction, penalty, i, learningMethod, trainingRate, i);
				
				precisionList.add(result.precision);
				recallList.add(result.recall);
				mf1List.add(result.mf1);
				Mf1List.add(result.Mf1);
				
				writerFinalResults.write(i + "precision:" + result.precision + "\n\r");
				writerFinalResults.write(i + "recall:" + result.recall + "\n\r");
				writerFinalResults.write(i + "mf1:" + result.mf1 + "\n\r");
				writerFinalResults.write(i + "Mf1:" + result.Mf1 + "\n\r");

			}
			writerFinalResults.write("precision:" + StatUtils.listAverage(precisionList) 
					+ "," + StatUtils.std(precisionList, StatUtils.listAverage(precisionList)) + "\n\r");
			writerFinalResults.write("recall:" + StatUtils.listAverage(recallList) 
					+ "," + StatUtils.std(recallList, StatUtils.listAverage(recallList)) + "\n\r");
			writerFinalResults.write("mf1:" + StatUtils.listAverage(mf1List) 
					+ "," + StatUtils.std(mf1List, StatUtils.listAverage(mf1List)) + "\n\r");
			writerFinalResults.write("Mf1:" + StatUtils.listAverage(Mf1List) 
					+ "," + StatUtils.std(Mf1List, StatUtils.listAverage(Mf1List)) + "\n\r");
			
			writerFinalResults.flush();
			writerFinalResults.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static EvalResults bootstrapping20NG_Mix(int selectTopNumDocs, 
			int boostrappingMaxIter, int topK, String direction, double penalty, int currentIter,
			String learningMethod, double trainingRate,
			int seed) throws IOException {
		
		FileWriter writerIntermediateResults = null;
		if (ClassifierConstant.isServer == true) {
			writerIntermediateResults = new FileWriter("/shared/saruman/yqsong/temp/rcv_bootstrapping20NG_Mix_details_" + direction + currentIter + ".txt");
		} else {
			writerIntermediateResults = new FileWriter("C:/Users/yqsong/Downloads/rcv_bootstrapping20NG_Mix_details_" + direction + currentIter + ".txt");
		}
			
		List<Double> precisionListD1 = new ArrayList<Double>();
		List<Double> recallListD1 = new ArrayList<Double>();
		List<Double> mf1ListD1 = new ArrayList<Double>();
		List<Double> Mf1ListD1 = new ArrayList<Double>();
			
		Random random = new Random(seed);
		
		double penaltyPara = penalty;

		String stopWordsFile = "";
		String docIDContentConceptFile = "";
		String docIDTopicMapFile = "";
		String treeConceptFile = "";
		String outputClassificationFile = "";
		String outputLabelComparisonFile = "";
		String method = "simple";
		String data = "rcv";
		
		if (ClassifierConstant.isServer == true) {
			stopWordsFile = "data/rcvTest/english.stop";
			docIDContentConceptFile = "/shared/shelley/yqsong/benchmark/rcv1v2/output_train/rcv_train.simple.esa.concepts.500";
			docIDTopicMapFile = "data/rcvTest/rcv1-v2.topics.qrels";
			treeConceptFile = "/shared/shelley/yqsong/benchmark/rcv1v2/output_tree/tree.rcv1.useDesc.simple.esa.concepts.500";
			outputClassificationFile = "/shared/shelley/yqsong/benchmark/rcv1v2/output/result.concept.rcv.classification";
			outputLabelComparisonFile = "/shared/shelley/yqsong/benchmark/rcv1v2/output/result.concept.rcv.labelComparison";
		} else {
			stopWordsFile = "data/rcvTest/english.stop";
			docIDContentConceptFile = "D:/yqsong/data/rcvData/output_train/rcv_train.simple.esa.concepts.500";
			docIDTopicMapFile = "data/rcvTest/rcv1-v2.topics.qrels";
			treeConceptFile = "D:/yqsong/data/rcvData/output_tree/tree.rcv1.useDesc.simple.esa.concepts.500";
			outputClassificationFile = "D:/yqsong/data/rcvData/output/result.concept.rcv.classification";
			outputLabelComparisonFile = "D:/yqsong/data/rcvData/output/result.concept.rcv.labelComparison";
		}
		StopWords.rcvStopWords = StopWords.readStopWords (stopWordsFile);
		
		String fileTopicHierarchyPath = "data/rcvTest/rcv1.topics.hier.orig";
		String fileTopicDescriptionPath = "data/rcvTest/topics.rbb";

		
		// initialize data
		RCVCorpusConceptData corpusContentProc = new RCVCorpusConceptData();
		corpusContentProc.readCorpusContentAndConcepts(docIDContentConceptFile, ClassifierConstant.isBreakConcepts, random, trainingRate, conceptWeights);

		
		// initialize concept tree
		AbstractConceptTree tree = null;
		if (direction.equals("bottomup")) {
			tree = new ConceptTreeBottomUpML(data, method, conceptWeights, false);
		} else {
			tree = new ConceptTreeTopDownML(data, method, conceptWeights, false);
		}
		System.out.println("process tree...");
		tree.readLabelTreeFromDump(treeConceptFile, ClassifierConstant.isBreakConcepts);
		ConceptTreeNode rootNode = tree.initializeTreeWithConceptVector("root", 0, ClassifierConstant.isBreakConcepts);
		tree.setRootNode(rootNode);
		System.out.println("process tree finished");
		
		
		// classify testing data
		
		// read test topic doc maps // this is only for test accuracy, not for training
		RCVTopicDocMaps testTopicDocMapData = new RCVTopicDocMaps();
		testTopicDocMapData.readFilteredTopicDocMap (docIDTopicMapFile, corpusContentProc.getCorpusContentMapTest().keySet());
		HashMap<String, HashSet<String>> topicDocMapTest = testTopicDocMapData.getTopicDocMap();
		HashMap<String, HashSet<String>> docTopicMapTest = testTopicDocMapData.getDocTopicMap();
		
		HashMap<String, EvalResults> resultMap = Evaluation.testMultiLabelConceptTreeResults(
				(InterfaceMultiLabelConceptClassificationTree) tree, 
				corpusContentProc.getCorpusConceptVectorMapTest(), 
				topicDocMapTest, 
				docTopicMapTest, 
				outputClassificationFile, 
				outputLabelComparisonFile, 
				topK); 
		
		precisionListD1.add(resultMap.get("all").precision);
		recallListD1.add(resultMap.get("all").recall);
		mf1ListD1.add(resultMap.get("all").mf1);
		Mf1ListD1.add(resultMap.get("all").Mf1);
		

		// classify training data using concept tree
		int count = 0;
		HashMap<String, HashMap<Integer, List<LabelKeyValuePair>>> conceptTreeClassificationResults = 
				new HashMap<String,HashMap<Integer, List<LabelKeyValuePair>>>();
		HashMap<String, HashMap<String, Double>> topicDocScoreMapConceptTree = new HashMap<String, HashMap<String, Double>>();
		for (String docID : corpusContentProc.getCorpusConceptVectorMapTraining().keySet()) {
			SparseVector documentVector = corpusContentProc.getCorpusConceptVectorMapTraining().get(docID);

			HashMap<Integer, List <LabelKeyValuePair>> treeLabelResult = 
					((InterfaceMultiLabelConceptClassificationTree) tree).labelDocumentML(documentVector);

			for (Integer depth : treeLabelResult.keySet()) {
				List<LabelKeyValuePair> classifiedLabelScoreList = treeLabelResult.get(depth);
				if (classifiedLabelScoreList == null) {
					classifiedLabelScoreList = new ArrayList<LabelKeyValuePair>();
				}
				List<LabelKeyValuePair> classifiedLabels = new ArrayList<LabelKeyValuePair>();
				for (int i = 0; i < Math.min(topK, classifiedLabelScoreList.size()); ++i) {
					classifiedLabels.add(classifiedLabelScoreList.get(i));
				}
				for (LabelKeyValuePair labelScore : classifiedLabels) {
					String label = labelScore.getLabel();//.split(":")[1];
					double score = labelScore.getScore();
					
					if (topicDocScoreMapConceptTree.containsKey(label) == false) {
						topicDocScoreMapConceptTree.put(label, new HashMap<String, Double>());
					}
					topicDocScoreMapConceptTree.get(label).put(docID, score);
				}
			}
			
			conceptTreeClassificationResults.put(docID, treeLabelResult);
			count++;
			if (count % 1000 == 0) {
				System.out.println("Classified " + count + " documents ...");
			}
		}
		
		// initialize training data
		RCVTopicDocMaps rcvTDM = new RCVTopicDocMaps();
		HashMap<String, String> trainingDataMap = new HashMap<String, String>();

		// generate training data for classifier tree
		int docAdded = 0;
		for (String topic : topicDocScoreMapConceptTree.keySet()) {
			HashMap<String, Double> docScoreMap = topicDocScoreMapConceptTree.get(topic);
			TreeMap<String, Double> sortedDocScoreMap = HashSort.sortByValues(docScoreMap);
			int topicSelected = 0;
			int topicDepth = tree.getLabelDepth(topic);
			for (String docID : sortedDocScoreMap.keySet()) {
				if (topicSelected > selectTopNumDocs)
					break;
				topicSelected++;
				
//				System.out.println("[Debug:] " + topic + ": " + docID + ", " + docScoreMap.get(docID)); 
				
				HashMap<Integer, List<LabelKeyValuePair>> treeLabelResult = conceptTreeClassificationResults.get(docID);
				
				for (Integer depth : treeLabelResult.keySet()) {
					if (depth > topicDepth) {
						continue;
					}
					List<LabelKeyValuePair> classifiedLabelScoreList = treeLabelResult.get(depth);
					if (classifiedLabelScoreList == null) {
						classifiedLabelScoreList = new ArrayList<LabelKeyValuePair>();
					}
					List<LabelKeyValuePair> classifiedLabels = new ArrayList<LabelKeyValuePair>();
					for (int i = 0; i < Math.min(topK, classifiedLabelScoreList.size()); ++i) {
						classifiedLabels.add(classifiedLabelScoreList.get(i));
					}

					for (LabelKeyValuePair labelScore : classifiedLabels) {
						
						if (true) {
							String label = labelScore.getLabel();
							double score = labelScore.getScore();
							
							System.out.println(topic + ":" + label + "," + score);
							
							if ((rcvTDM.getTopicDocMap().containsKey(label) == true && score > conceptSimilaritygThreshold) 
									|| rcvTDM.getTopicDocMap().containsKey(label) == false) {
								if (rcvTDM.getTopicDocMap().containsKey(label) == true) {
				    				if (rcvTDM.getTopicDocMap().get(label).contains(docID) == false) {
				    					rcvTDM.getTopicDocMap().get(label).add(docID);
				    					docAdded++;
				    				}
								} else {
									rcvTDM.getTopicDocMap().put(label, new HashSet<String>());
									rcvTDM.getTopicDocMap().get(label).add(docID);
									docAdded++;
								}
				    		
								if (rcvTDM.getDocTopicMap().containsKey(docID) == true) {
									if (rcvTDM.getDocTopicMap().get(docID).contains(label) == false) {
										rcvTDM.getDocTopicMap().get(docID).add(label);
									}
								} else {
									rcvTDM.getDocTopicMap().put(docID, new HashSet<String>());
									rcvTDM.getDocTopicMap().get(docID).add(label);
								}
							}
							
						}
					}
				}
				
				trainingDataMap.put(docID, corpusContentProc.getCorpusContentMap().get(docID));
			}
		}
		System.out.println("  [Bootstrapping: ]" + docAdded + " documents added." );
		
		///////////////////////////////////////////////////
		// start bootstrapping
		///////////////////////////////////////////////////
		for (int iter = 0; iter < boostrappingMaxIter; ++iter) {
			RCVTreeLabelData treeLabelData = new RCVTreeLabelData();
			treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
			treeLabelData.readTopicDescription(fileTopicDescriptionPath);

			
			AbstractLabelTree classifierTree = null;
			if (learningMethod.equals("lbj")) {
				AbstractClassifierLBJTree classifierTreeUpdate = null;
				if (direction .equals("bottomup")) {
					classifierTreeUpdate = new ClassifierLBJTreeBottomUpML("rcv");
				} else {
					classifierTreeUpdate = new ClassifierLBJTreeTopDownML("rcv");
				}
				
				classifierTreeUpdate.initializeWithContentData(trainingDataMap, treeLabelData, rcvTDM);
				classifierTreeUpdate.setPenaltyParaC(penaltyPara);
				classifierTreeUpdate.trainAllTreeNodes();
				classifierTree = classifierTreeUpdate;
			}
			
			if (learningMethod.equals("liblinear")) {
				AbstractClassifierLibLinearTree classifierTreeUpdate = null;
				if (direction .equals("bottomup")) {
					classifierTreeUpdate = new ClassifierLibLinearTreeBottomUpML("rcv");
				} else {
					classifierTreeUpdate = new ClassifierLibLinearTreeTopDownML("rcv");
				}
				
				classifierTreeUpdate.initializeWithContentData(trainingDataMap, treeLabelData, rcvTDM);
				classifierTreeUpdate.setPenaltyParaC(penaltyPara);
				classifierTreeUpdate.trainAllTreeNodes();
				classifierTree = classifierTreeUpdate;
			}
			
			

			
			System.out.println();
			System.out.println("***********Iteration " + iter + "************");
			System.out.println();

			writerIntermediateResults.write("***********Iteration " + iter + "************"
					+ System.getProperty("line.separator").toString()); 

			// read test topic doc maps // this is only for evaluation, not for training
			
			resultMap = Evaluation.testMultiLabelContentTreeResults((InterfaceMultiLabelContentClassificationTree) classifierTree, 
					corpusContentProc.getCorpusContentMapTest(), 
					null,
					topicDocMapTest, 
					docTopicMapTest, 
					outputClassificationFile, 
					outputLabelComparisonFile, 
					topK, false); 
			
			precisionListD1.add(resultMap.get("all").precision);
			recallListD1.add(resultMap.get("all").recall);
			mf1ListD1.add(resultMap.get("all").mf1);
			Mf1ListD1.add(resultMap.get("all").Mf1);
			

			// test end
			
			// classify training documents
			count = 0;
			HashMap<String, HashMap<Integer, List<LabelKeyValuePair>>> classifierTreeClassificationResults = 
					new HashMap<String,HashMap<Integer, List<LabelKeyValuePair>>>();
			HashMap<String, HashMap<String, Double>> topicDocScoreMapClassifierTree = new HashMap<String, HashMap<String, Double>>();
			for (String docID : corpusContentProc.getCorpusConceptVectorMapTraining().keySet()) {
				String documentVector = corpusContentProc.getCorpusContentMapTraining().get(docID);
				
				HashMap<Integer, List<LabelKeyValuePair>> treeLabelResult = ((InterfaceMultiLabelContentClassificationTree) classifierTree).labelDocumentContentML(documentVector);

				
				for (Integer depth : treeLabelResult.keySet()) {
					List<LabelKeyValuePair> classifiedLabelScoreList = treeLabelResult.get(depth);
					if (classifiedLabelScoreList == null) {
						classifiedLabelScoreList = new ArrayList<LabelKeyValuePair>();
					}
					List<LabelKeyValuePair> classifiedLabels = new ArrayList<LabelKeyValuePair>();
					for (int i = 0; i < Math.min(topK, classifiedLabelScoreList.size()); ++i) {
						classifiedLabels.add(classifiedLabelScoreList.get(i));
					}
					for (LabelKeyValuePair labelScore : classifiedLabels) {
						String label = labelScore.getLabel();//.split(":")[1];
						double score = labelScore.getScore();
						
						if (topicDocScoreMapClassifierTree.containsKey(label) == false) {
							topicDocScoreMapClassifierTree.put(label, new HashMap<String, Double>());
						}
						topicDocScoreMapClassifierTree.get(label).put(docID, score);
					}
				}
				
				classifierTreeClassificationResults.put(docID, treeLabelResult);
				count++;
				if (count % 1000 == 0) {
					System.out.println("Classified " + count + " documents ...");
				}
			}
			
			// add more documents for training
			
			docAdded = 0;
			for (String topic : topicDocScoreMapClassifierTree.keySet()) {
				HashMap<String, Double> docScoreMap = topicDocScoreMapClassifierTree.get(topic);
				TreeMap<String, Double> sortedDocScoreMap = HashSort.sortByValues(docScoreMap);
				int topicSelected = 0;
				int topicDepth = classifierTree.getLabelDepth(topic);
				for (String docID : sortedDocScoreMap.keySet()) {
					if (topicSelected > selectTopNumDocs * (iter + 1))
						break;
					topicSelected++;
					
					HashMap<Integer, List<LabelKeyValuePair>> treeLabelResult = classifierTreeClassificationResults.get(docID);

//					writerIntermediateResults.write("[Debug:] " + topic + ": " + docID + ", " + docScoreMap.get(docID)
//							+ System.getProperty("line.separator").toString()); 
					
//					if (treeLabelResult == null || treeLabelResult.labels == null || treeLabelResult.labels.size() == 0)
//						continue;
					
					
					for (Integer depth : treeLabelResult.keySet()) {
						if (depth > topicDepth) {
							continue;
						}
						
						List<LabelKeyValuePair> classifiedLabelScoreList = treeLabelResult.get(depth);
						if (classifiedLabelScoreList == null) {
							classifiedLabelScoreList = new ArrayList<LabelKeyValuePair>();
						}
						List<LabelKeyValuePair> classifiedLabels = new ArrayList<LabelKeyValuePair>();
						for (int i = 0; i < Math.min(topK, classifiedLabelScoreList.size()); ++i) {
							classifiedLabels.add(classifiedLabelScoreList.get(i));
						}
						
						for (LabelKeyValuePair labelScore : classifiedLabels) {
							if (true) { //if (labelScore.getLabel().equals(topic)) {
								String label = labelScore.getLabel();
								double score = labelScore.getScore();
								
								if (score > bootstrappingThreshold) {
									if (rcvTDM.getTopicDocMap().containsKey(label) == true) {
					    				if (rcvTDM.getTopicDocMap().get(label).contains(docID) == false) {
					    					rcvTDM.getTopicDocMap().get(label).add(docID);
					    					docAdded++;
					    				}
									} else {
										rcvTDM.getTopicDocMap().put(label, new HashSet<String>());
										rcvTDM.getTopicDocMap().get(label).add(docID);
										docAdded++;
									}
					    		
									if (rcvTDM.getDocTopicMap().containsKey(docID) == true) {
										if (rcvTDM.getDocTopicMap().get(docID).contains(label) == false) {
											rcvTDM.getDocTopicMap().get(docID).add(label);
										}
									} else {
										rcvTDM.getDocTopicMap().put(docID, new HashSet<String>());
										rcvTDM.getDocTopicMap().get(docID).add(label);
									}
								}
								
								
							}
						
						}
					}
					
					trainingDataMap.put(docID, corpusContentProc.getCorpusContentMap().get(docID));
				}
			}
			System.out.println("  [Bootstrapping: ]" + docAdded + " documents added." );
		}
		
		
		System.out.println();
		System.out.println("D1 precision");
		for (int i = 0; i < precisionListD1.size(); ++i) {
			System.out.print(precisionListD1.get(i) + "\t");
		}
		System.out.println();
		System.out.println("D1 recall");
		for (int i = 0; i < recallListD1.size(); ++i) {
			System.out.print(recallListD1.get(i) + "\t");
		}
		System.out.println();
		System.out.println("D1 mf1");
		for (int i = 0; i < mf1ListD1.size(); ++i) {
			System.out.print(mf1ListD1.get(i) + "\t");
		}
		System.out.println();
		System.out.println("D1 Mf1");
		for (int i = 0; i < Mf1ListD1.size(); ++i) {
			System.out.print(Mf1ListD1.get(i) + "\t");
		}
		
		
		System.out.println();
		System.out.println("finished");
		
		
		writerIntermediateResults.write("all precision\t");
		for (int i = 0; i < precisionListD1.size(); ++i) {
			writerIntermediateResults.write(precisionListD1.get(i) + "\t");
		}
		writerIntermediateResults.write(System.getProperty("line.separator").toString());
		
		writerIntermediateResults.write("all recall\t");
		for (int i = 0; i < recallListD1.size(); ++i) {
			writerIntermediateResults.write(recallListD1.get(i) + "\t");
		}
		writerIntermediateResults.write(System.getProperty("line.separator").toString());
		
		writerIntermediateResults.write("all mf1\t");
		for (int i = 0; i < mf1ListD1.size(); ++i) {
			writerIntermediateResults.write(mf1ListD1.get(i) + "\t");
		}
		writerIntermediateResults.write(System.getProperty("line.separator").toString());
		
		writerIntermediateResults.write("all Mf1\t");
		for (int i = 0; i < Mf1ListD1.size(); ++i) {
			writerIntermediateResults.write(Mf1ListD1.get(i) + "\t");
		}
		writerIntermediateResults.write(System.getProperty("line.separator").toString());
		
		
		writerIntermediateResults.flush();
		writerIntermediateResults.close();
		
		return resultMap.get("all");
	}
}
