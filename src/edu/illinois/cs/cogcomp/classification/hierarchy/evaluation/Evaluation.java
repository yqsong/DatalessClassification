package edu.illinois.cs.cogcomp.classification.hierarchy.evaluation;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import edu.illinois.cs.cogcomp.classification.densification.representation.SparseSimilarityCondensation;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.ml.ClassifierLibLinearTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.ml.ClassifierLibLinearTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.AbstractLabelTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiClassClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelConceptClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelContentClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultML;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;

/**
 * yqsong@illinois.edu
 */

public class Evaluation {
	public static Random randomGuess = new Random();
	
	public static void testMultiLabelRandomGuessResults (
			AbstractLabelTree tree,
			Set<String> docIdSet, 
			HashMap<String, HashSet<String>> topicDocMap,
			HashMap<String, HashSet<String>> docTopicMap,
			int topK) {
		// classification
		HashMap<String, EvalResults> resultsMap = new HashMap<String, EvalResults>();
		try {
			int count = 0;
			
			HashMap<Integer, HashMap<String, Integer>> truepositive = new HashMap<Integer, HashMap<String, Integer>>();
			HashMap<Integer, HashMap<String, Integer>> falsenegative = new HashMap<Integer, HashMap<String, Integer>>();
			HashMap<Integer, HashMap<String, Integer>> falsepositive = new HashMap<Integer, HashMap<String, Integer>>();
			
			HashMap<Integer, HashSet<String>> depthLabelMap = new HashMap<Integer, HashSet<String>>();
			
			HashMap<Integer, Integer> hitCorrect = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> hitCount = new HashMap<Integer, Integer>();
			
			HashMap<Integer, List<Double>> overallAccList = new HashMap<Integer, List<Double>>();
			HashMap<Integer, List<Double>> overallPrecList = new HashMap<Integer, List<Double>>();
			HashMap<Integer, List<Double>> overallRecList = new HashMap<Integer, List<Double>>();
			HashMap<Integer, List<Double>> overallFList = new HashMap<Integer, List<Double>>();
			
			for (int iter = 0; iter < 10; ++iter) {
			
				for (String docID : docIdSet) {
					// get vector
					
					HashSet<String> trueLabelSet = docTopicMap.get(docID);
					HashMap<Integer, HashSet<String>> trueDepthsLabels = new HashMap<Integer, HashSet<String>>();
					
					for (String label : trueLabelSet) {
						int depth = tree.getLabelDepth(label);
						if (trueDepthsLabels.containsKey(depth) == false) {
							trueDepthsLabels.put(depth, new HashSet<String>());
						}
						trueDepthsLabels.get(depth).add(label);
					}
					
					for (int depth : trueDepthsLabels.keySet()) {
						// true labels
						HashSet<String> trueLabels = trueDepthsLabels.get(depth);
						//classified labels
	
						HashSet<String> classifiedRandomLabelSet = new HashSet<String>();
	
						for (String truelabel : trueLabels) {
							Set<String> siblingSet = tree.getLabelSameLevel(truelabel);
							for (String sibling : siblingSet) {
								if (classifiedRandomLabelSet.contains(sibling) == false) {
									classifiedRandomLabelSet.add(sibling);
								}
							}
						}
						
						List<String> classifiedRandomLabelList = new ArrayList<String>();
						classifiedRandomLabelList.addAll(classifiedRandomLabelSet);
						int[] randomPerm = RandomOperations.RandPermutation(classifiedRandomLabelList.size());
						
						HashSet<String> classifiedLabels = new HashSet<String>();
						for (int i = 0; i < Math.min(topK, classifiedRandomLabelList.size()); ++i) {
							classifiedLabels.add(classifiedRandomLabelList.get(randomPerm[i]));
						}
						
						// evaluate
						for (String trueLabel : trueLabels) {
							if (truepositive.containsKey(depth) == false) {
								truepositive.put(depth, new HashMap<String, Integer>());
							}
							if (truepositive.get(depth).containsKey(trueLabel) == false) {
								truepositive.get(depth).put(trueLabel, 0);
							}
							
							if (falsenegative.containsKey(depth) == false) {
								falsenegative.put(depth, new HashMap<String, Integer>());
							}
							if (falsenegative.get(depth).containsKey(trueLabel) == false) {
								falsenegative.get(depth).put(trueLabel, 0);
							}
							
							if (falsepositive.containsKey(depth) == false) {
								falsepositive.put(depth, new HashMap<String, Integer>());
							}
							if (falsepositive.get(depth).containsKey(trueLabel) == false) {
								falsepositive.get(depth).put(trueLabel, 0);
							}
							
							if (depthLabelMap.containsKey(depth) == false) {
								depthLabelMap.put(depth, new HashSet<String>());
							}
							depthLabelMap.get(depth).add(trueLabel);
							
							
							if (hitCorrect.containsKey(depth) == false) {
								hitCorrect.put(depth, 0);
							}
							if (hitCount.containsKey(depth) == false) {
								hitCount.put(depth, 0);
							}
							
							if (classifiedLabels.contains(trueLabel) == true) {
								
								truepositive.get(depth).put(trueLabel, truepositive.get(depth).get(trueLabel) + 1);
								
								hitCorrect.put(depth, hitCorrect.get(depth) + 1);
								
							} else {
								falsenegative.get(depth).put(trueLabel, falsenegative.get(depth).get(trueLabel) + 1);
							}
							hitCount.put(depth, hitCount.get(depth) + 1);
							
						}
						
						for (String classifiedLabel : classifiedLabels) {
							if (truepositive.containsKey(depth) == false) {
								truepositive.put(depth, new HashMap<String, Integer>());
							}
							if (truepositive.get(depth).containsKey(classifiedLabel) == false) {
								truepositive.get(depth).put(classifiedLabel, 0);
							}
							
							if (falsenegative.containsKey(depth) == false) {
								falsenegative.put(depth, new HashMap<String, Integer>());
							}
							if (falsenegative.get(depth).containsKey(classifiedLabel) == false) {
								falsenegative.get(depth).put(classifiedLabel, 0);
							}
							
							if (falsepositive.containsKey(depth) == false) {
								falsepositive.put(depth, new HashMap<String, Integer>());
							}
							if (falsepositive.get(depth).containsKey(classifiedLabel) == false) {
								falsepositive.get(depth).put(classifiedLabel, 0);
							}
							
							if (depthLabelMap.containsKey(depth) == false) {
								depthLabelMap.put(depth, new HashSet<String>());
							}
							depthLabelMap.get(depth).add(classifiedLabel);
							
							if (trueLabels.contains(classifiedLabel) == false) {
								falsepositive.get(depth).put(classifiedLabel, falsepositive.get(depth).get(classifiedLabel) + 1);
							}
						}
					}
					
	
					count++;
				}
				
				for (int depth : hitCorrect.keySet()) {
					
					double tp = 0;
					double fp = 0;
					double fn = 0;
					List<Double> precList = new ArrayList<Double>();
					List<Double> recList = new ArrayList<Double>();
					List<Double> f1List = new ArrayList<Double>();
					for (String label : depthLabelMap.get(depth)) {
						if (truepositive.get(depth).containsKey(label) == true) {
							tp = truepositive.get(depth).get(label);
						} 
						if (falsepositive.get(depth).containsKey(label) == true) {
							fp = falsepositive.get(depth).get(label);
						} 
						if (falsenegative.get(depth).containsKey(label) == true) {
							fn = falsenegative.get(depth).get(label);
						} 
						double precision = tp / (tp + fp + Double.MIN_VALUE);
						double recall = tp / (tp + fn + Double.MIN_VALUE);
						double f1 = 2 * (precision * recall) / (precision + recall + Double.MIN_VALUE);
						precList.add(precision);
						recList.add(recall);
						f1List.add(f1);
					}
					
					if (overallAccList.containsKey(depth) == false) {
						overallAccList.put(depth, new ArrayList<Double>());
					}
					if (overallPrecList.containsKey(depth) == false) {
						overallPrecList.put(depth, new ArrayList<Double>());
					}
					if (overallRecList.containsKey(depth) == false) {
						overallRecList.put(depth, new ArrayList<Double>());
					}
					if (overallFList.containsKey(depth) == false) {
						overallFList.put(depth, new ArrayList<Double>());
					}
					
					overallAccList.get(depth).add((double) hitCorrect.get(depth) / (double) hitCount.get(depth));
					overallPrecList.get(depth).add(StatUtils.listAverage(precList) );
					overallRecList.get(depth).add(StatUtils.listAverage(recList) );
					overallFList.get(depth).add(StatUtils.listAverage(f1List) );
				}
				
			}
			
			for (int depth : hitCorrect.keySet()) {
				System.out.println(System.getProperty("line.separator").toString());
				System.out.println("Depth: " + depth);
				
				double hitAcc = StatUtils.listAverage(overallAccList.get(depth));
				System.out.println("Random Guess Hit precision@" + topK + ": " + String.format("%.4f", hitAcc));

				System.out.println("Random Guess Precision: " + String.format("%.4f", StatUtils.listAverage(overallPrecList.get(depth))));
				System.out.println("Random Guess Recall: " + String.format("%.4f", StatUtils.listAverage(overallRecList.get(depth))));
				System.out.println("Random Guess F1: " + String.format("%.4f", StatUtils.listAverage(overallFList.get(depth))));
			}
			
			System.out.println(System.getProperty("line.separator").toString());	
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public static HashMap<String, EvalResults> testMultiLabelContentTreeResults (
			InterfaceMultiLabelContentClassificationTree tree,
			HashMap<String, String> testDocIdContentMap, 
			HashMap<String, String> allDocIdConceptStringMap, 
			HashMap<String, HashSet<String>> topicDocMap,
			HashMap<String, HashSet<String>> docTopicMap,
			String outputDataFilePath, String outputLabelFilePath,
			int topK,
			boolean isUseConcept) {
		// classification
		HashMap<String, EvalResults> resultsMap = new HashMap<String, EvalResults>();
		try {
			int count = 0;
			
			HashMap<Integer, HashMap<String, Integer>> truepositive = new HashMap<Integer, HashMap<String, Integer>>();
			HashMap<Integer, HashMap<String, Integer>> falsenegative = new HashMap<Integer, HashMap<String, Integer>>();
			HashMap<Integer, HashMap<String, Integer>> falsepositive = new HashMap<Integer, HashMap<String, Integer>>();
			
			HashMap<Integer, HashSet<String>> depthLabelMap = new HashMap<Integer, HashSet<String>>();
			
			HashMap<Integer, Integer> hitCorrect = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> hitCount = new HashMap<Integer, Integer>();
			
			for (String docID : testDocIdContentMap.keySet()) {
				// get vector
				// process document with labels
				
				HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth = null;
				
				if (isUseConcept == false) {
					String document = testDocIdContentMap.get(docID);
					labelResultsInDepth = tree.labelDocumentContentML(document);
				} else {
					String document = allDocIdConceptStringMap.get(docID);
					labelResultsInDepth = tree.labelDocumentConceptML(document);
				}
				
				HashSet<String> trueLabelSet = docTopicMap.get(docID);
				if (trueLabelSet == null) {
					System.out.println(docID);
					trueLabelSet = new HashSet<String>();
				}
				HashMap<Integer, HashSet<String>> trueDepthsLabels = new HashMap<Integer, HashSet<String>>();
				for (String label : trueLabelSet) {
					int depth = tree.getLabelDepth(label);
					if (trueDepthsLabels.containsKey(depth) == false) {
						trueDepthsLabels.put(depth, new HashSet<String>());
					}
					trueDepthsLabels.get(depth).add(label);
				}
				
				for (int depth : trueDepthsLabels.keySet()) {
					// true labels
					HashSet<String> trueLabels = trueDepthsLabels.get(depth);
					//classified labels
					List<LabelKeyValuePair> classifiedLabelScoreList = labelResultsInDepth.get(depth);
					if (classifiedLabelScoreList == null) {
						classifiedLabelScoreList = new ArrayList<LabelKeyValuePair>();
					}
					HashSet<String> classifiedLabels = new HashSet<String>();
					for (int i = 0; i < Math.min(topK, classifiedLabelScoreList.size()); ++i) {
						classifiedLabels.add(classifiedLabelScoreList.get(i).getLabel());
					}
					
					// evaluate
					for (String trueLabel : trueLabels) {
						if (truepositive.containsKey(depth) == false) {
							truepositive.put(depth, new HashMap<String, Integer>());
						}
						if (truepositive.get(depth).containsKey(trueLabel) == false) {
							truepositive.get(depth).put(trueLabel, 0);
						}
						
						if (falsenegative.containsKey(depth) == false) {
							falsenegative.put(depth, new HashMap<String, Integer>());
						}
						if (falsenegative.get(depth).containsKey(trueLabel) == false) {
							falsenegative.get(depth).put(trueLabel, 0);
						}
						
						if (falsepositive.containsKey(depth) == false) {
							falsepositive.put(depth, new HashMap<String, Integer>());
						}
						if (falsepositive.get(depth).containsKey(trueLabel) == false) {
							falsepositive.get(depth).put(trueLabel, 0);
						}
						
						if (depthLabelMap.containsKey(depth) == false) {
							depthLabelMap.put(depth, new HashSet<String>());
						}
						depthLabelMap.get(depth).add(trueLabel);
						
						
						if (hitCorrect.containsKey(depth) == false) {
							hitCorrect.put(depth, 0);
						}
						if (hitCount.containsKey(depth) == false) {
							hitCount.put(depth, 0);
						}
						
						if (classifiedLabels.contains(trueLabel) == true) {
							
							truepositive.get(depth).put(trueLabel, truepositive.get(depth).get(trueLabel) + 1);
							
							hitCorrect.put(depth, hitCorrect.get(depth) + 1);
							
						} else {
							falsenegative.get(depth).put(trueLabel, falsenegative.get(depth).get(trueLabel) + 1);
						}
						hitCount.put(depth, hitCount.get(depth) + 1);
						
					}
					
					for (String classifiedLabel : classifiedLabels) {
						if (truepositive.containsKey(depth) == false) {
							truepositive.put(depth, new HashMap<String, Integer>());
						}
						if (truepositive.get(depth).containsKey(classifiedLabel) == false) {
							truepositive.get(depth).put(classifiedLabel, 0);
						}
						
						if (falsenegative.containsKey(depth) == false) {
							falsenegative.put(depth, new HashMap<String, Integer>());
						}
						if (falsenegative.get(depth).containsKey(classifiedLabel) == false) {
							falsenegative.get(depth).put(classifiedLabel, 0);
						}
						
						if (falsepositive.containsKey(depth) == false) {
							falsepositive.put(depth, new HashMap<String, Integer>());
						}
						if (falsepositive.get(depth).containsKey(classifiedLabel) == false) {
							falsepositive.get(depth).put(classifiedLabel, 0);
						}
						
						if (depthLabelMap.containsKey(depth) == false) {
							depthLabelMap.put(depth, new HashSet<String>());
						}
						depthLabelMap.get(depth).add(classifiedLabel);
						
						if (trueLabels.contains(classifiedLabel) == false) {
							falsepositive.get(depth).put(classifiedLabel, falsepositive.get(depth).get(classifiedLabel) + 1);
						}
					}
				}
				

				count++;
				if (count % 1000 == 0) {
					System.out.println("Classified " + count + " documents ...");
				}
			}
			
			List<Double> precAllList = new ArrayList<Double>();
			List<Double> recAllList = new ArrayList<Double>();
			List<Double> f1AllList = new ArrayList<Double>();
			
			List<Double> tpAllList = new ArrayList<Double>();
			List<Double> fpAllList = new ArrayList<Double>();
			List<Double> fnAllList = new ArrayList<Double>();
			

			List<Double> precLeavesList = new ArrayList<Double>();
			List<Double> recLeavesList = new ArrayList<Double>();
			List<Double> f1LeavesList = new ArrayList<Double>();
			
			List<Double> tpLeavesList = new ArrayList<Double>();
			List<Double> fpLeavesList = new ArrayList<Double>();
			List<Double> fnLeavesList = new ArrayList<Double>();
			
			resultsMap.put("all", new EvalResults());
			resultsMap.put("leaves", new EvalResults());

			System.out.println(System.getProperty("line.separator").toString());

			for (int depth : hitCorrect.keySet()) {
				
				resultsMap.put("depth" + depth, new EvalResults());
				
				System.out.println("Depth: " + depth);
				
				double hitAcc = (double) hitCorrect.get(depth) / (double) hitCount.get(depth);
				System.out.println("Hit precision@" + topK + ": " + String.format("%.4f", hitAcc));
				resultsMap.get("depth" + depth).hitPrecision = hitAcc;
				
				double tp = 0;
				double fp = 0;
				double fn = 0;
				List<Double> precList = new ArrayList<Double>();
				List<Double> recList = new ArrayList<Double>();
				List<Double> f1List = new ArrayList<Double>();
				
				List<Double> tpList = new ArrayList<Double>();
				List<Double> fpList = new ArrayList<Double>();
				List<Double> fnList = new ArrayList<Double>();
				for (String label : depthLabelMap.get(depth)) {
					
					if (truepositive.get(depth).containsKey(label) == true) {
						tp = truepositive.get(depth).get(label);
					} 
					if (falsepositive.get(depth).containsKey(label) == true) {
						fp = falsepositive.get(depth).get(label);
					} 
					if (falsenegative.get(depth).containsKey(label) == true) {
						fn = falsenegative.get(depth).get(label);
					} 
					double precision = tp / (tp + fp + Double.MIN_VALUE);
					double recall = tp / (tp + fn + Double.MIN_VALUE);
					double f1 = 2 * (precision * recall) / (precision + recall + Double.MIN_VALUE);
					
					tpList.add(tp);
					fpList.add(fp);
					fnList.add(fn);
					
					precList.add(precision);
					recList.add(recall);
					f1List.add(f1);
					
					
					tpAllList.add(tp);
					fpAllList.add(fp);
					fnAllList.add(fn);

					precAllList.add(precision);
					recAllList.add(recall);
					f1AllList.add(f1);
					
					boolean isLeaf = false;
					isLeaf = tree.isLeafNode(label);
					if (isLeaf == true) {
						tpLeavesList.add(tp);
						fpLeavesList.add(fp);
						fnLeavesList.add(fn);
						
						precLeavesList.add(precision);
						recLeavesList.add(recall);
						f1LeavesList.add(f1);
					}
				}
				
				resultsMap.get("depth" + depth).precision = StatUtils.listAverage(precList);
				resultsMap.get("depth" + depth).recall = StatUtils.listAverage(recList);
				resultsMap.get("depth" + depth).Mf1 = StatUtils.listAverage(f1List);
				
				double mP = StatUtils.listSum(tpList) / (StatUtils.listSum(tpList) + StatUtils.listSum(fpList));
				double mR = StatUtils.listSum(tpList) / (StatUtils.listSum(tpList) + StatUtils.listSum(fnList));
				resultsMap.get("depth" + depth).mf1 = 2 * mP * mR / (mP + mR);
				
				System.out.println("Precision: " + String.format("%.4f", resultsMap.get("depth" + depth).precision));
				System.out.println("Recall: " + String.format("%.4f", resultsMap.get("depth" + depth).recall));
				System.out.println("mF1: " + String.format("%.4f", resultsMap.get("depth" + depth).mf1));
				System.out.println("MF1: " + String.format("%.4f", resultsMap.get("depth" + depth).Mf1));
				System.out.println(System.getProperty("line.separator").toString());	
			}
			
			resultsMap.get("leaves").precision = StatUtils.listAverage(precLeavesList);
			resultsMap.get("leaves").recall = StatUtils.listAverage(recLeavesList);
			resultsMap.get("leaves").Mf1 = StatUtils.listAverage(f1LeavesList);
			
			double mP = StatUtils.listSum(tpLeavesList) / (StatUtils.listSum(tpLeavesList) + StatUtils.listSum(fpLeavesList));
			double mR = StatUtils.listSum(tpLeavesList) / (StatUtils.listSum(tpLeavesList) + StatUtils.listSum(fnLeavesList));
			resultsMap.get("leaves").mf1 = 2 * mP * mR / (mP + mR);

			System.out.println("All Leaves Average");
			System.out.println("Precision: " + String.format("%.4f", resultsMap.get("leaves").precision));
			System.out.println("Recall: " + String.format("%.4f", resultsMap.get("leaves").recall));
			System.out.println("mF1: " + String.format("%.4f", resultsMap.get("leaves").mf1));
			System.out.println("MF1: " + String.format("%.4f", resultsMap.get("leaves").Mf1));
			System.out.println(System.getProperty("line.separator").toString());	

			
			resultsMap.get("all").precision = StatUtils.listAverage(precAllList);
			resultsMap.get("all").recall = StatUtils.listAverage(recAllList);
			resultsMap.get("all").Mf1 = StatUtils.listAverage(f1AllList);
			
			mP = StatUtils.listSum(tpAllList) / (StatUtils.listSum(tpAllList) + StatUtils.listSum(fpAllList));
			mR = StatUtils.listSum(tpAllList) / (StatUtils.listSum(tpAllList) + StatUtils.listSum(fnAllList));
			resultsMap.get("all").mf1 = 2 * mP * mR / (mP + mR);

			System.out.println("All Average");
			System.out.println("Precision: " + String.format("%.4f", resultsMap.get("all").precision));
			System.out.println("Recall: " + String.format("%.4f", resultsMap.get("all").recall));
			System.out.println("mF1: " + String.format("%.4f", resultsMap.get("all").mf1));
			System.out.println("MF1: " + String.format("%.4f", resultsMap.get("all").Mf1));
			System.out.println(System.getProperty("line.separator").toString());	
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultsMap;

	}
	
	public static HashMap<String, EvalResults> testMultiLabelConceptTreeResults (InterfaceMultiLabelConceptClassificationTree tree,
			HashMap<String, SparseVector> docIdContentMap, 
			HashMap<String, HashSet<String>> topicDocMap,
			HashMap<String, HashSet<String>> docTopicMap,
			String outputDataFilePath, String outputLabelFilePath,
			int topK) {
		// classification
		HashMap<String, EvalResults> resultsMap = new HashMap<String, EvalResults>();
		try {
			FileWriter writer = new FileWriter(outputDataFilePath);
			int count = 0;
			
			HashMap<Integer, HashMap<String, Integer>> truepositive = new HashMap<Integer, HashMap<String, Integer>>();
			HashMap<Integer, HashMap<String, Integer>> falsenegative = new HashMap<Integer, HashMap<String, Integer>>();
			HashMap<Integer, HashMap<String, Integer>> falsepositive = new HashMap<Integer, HashMap<String, Integer>>();
			
			HashMap<Integer, HashSet<String>> depthLabelMap = new HashMap<Integer, HashSet<String>>();
			
			HashMap<Integer, Integer> hitCorrect = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> hitCount = new HashMap<Integer, Integer>();
			
			for (String docID : docIdContentMap.keySet()) {
				writer.write(docID + "\t");
				// get vector
				SparseVector document = docIdContentMap.get(docID);
				// process document with labels
				
				if (docID.equals("D:\\data_test\\20newsgroup\\20NG_Source\\comp.windows.x\\66883")) {
					int step = 0;
				}
				
				HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth = tree.labelDocumentML(document);
				
				HashSet<String> trueLabelSet = docTopicMap.get(docID);
				if (trueLabelSet == null) {
					System.out.println(docID);
					trueLabelSet = new HashSet<String>();
				}
				HashMap<Integer, HashSet<String>> trueDepthsLabels = new HashMap<Integer, HashSet<String>>();
				for (String label : trueLabelSet) {
					int depth = tree.searchLabelDepth(label);
					if (trueDepthsLabels.containsKey(depth) == false) {
						trueDepthsLabels.put(depth, new HashSet<String>());
					}
					trueDepthsLabels.get(depth).add(label);
					writer.write(depth + "," + label + ";");
				}
				writer.write("\t");
				
				for (int depth : trueDepthsLabels.keySet()) {
					// true labels
					HashSet<String> trueLabels = trueDepthsLabels.get(depth);
					//classified labels
					List<LabelKeyValuePair> classifiedLabelScoreList = labelResultsInDepth.get(depth);
					if (classifiedLabelScoreList == null) {
						classifiedLabelScoreList = new ArrayList<LabelKeyValuePair>();
					}
					HashSet<String> classifiedLabels = new HashSet<String>();
					for (int i = 0; i < Math.min(topK, classifiedLabelScoreList.size()); ++i) {
						classifiedLabels.add(classifiedLabelScoreList.get(i).getLabel());
						
						writer.write(depth + "," + classifiedLabelScoreList.get(i).getLabel() + ";");
					}
					
					// evaluate
					for (String trueLabel : trueLabels) {
						if (truepositive.containsKey(depth) == false) {
							truepositive.put(depth, new HashMap<String, Integer>());
						}
						if (truepositive.get(depth).containsKey(trueLabel) == false) {
							truepositive.get(depth).put(trueLabel, 0);
						}
						
						if (falsenegative.containsKey(depth) == false) {
							falsenegative.put(depth, new HashMap<String, Integer>());
						}
						if (falsenegative.get(depth).containsKey(trueLabel) == false) {
							falsenegative.get(depth).put(trueLabel, 0);
						}
						
						if (falsepositive.containsKey(depth) == false) {
							falsepositive.put(depth, new HashMap<String, Integer>());
						}
						if (falsepositive.get(depth).containsKey(trueLabel) == false) {
							falsepositive.get(depth).put(trueLabel, 0);
						}
						
						if (depthLabelMap.containsKey(depth) == false) {
							depthLabelMap.put(depth, new HashSet<String>());
						}
						depthLabelMap.get(depth).add(trueLabel);
						
						
						if (hitCorrect.containsKey(depth) == false) {
							hitCorrect.put(depth, 0);
						}
						if (hitCount.containsKey(depth) == false) {
							hitCount.put(depth, 0);
						}
						
						if (classifiedLabels.contains(trueLabel) == true) {
							
							truepositive.get(depth).put(trueLabel, truepositive.get(depth).get(trueLabel) + 1);
							
							hitCorrect.put(depth, hitCorrect.get(depth) + 1);
							
						} else {
							falsenegative.get(depth).put(trueLabel, falsenegative.get(depth).get(trueLabel) + 1);
						}
						hitCount.put(depth, hitCount.get(depth) + 1);
						
					}
					
					for (String classifiedLabel : classifiedLabels) {
						if (truepositive.containsKey(depth) == false) {
							truepositive.put(depth, new HashMap<String, Integer>());
						}
						if (truepositive.get(depth).containsKey(classifiedLabel) == false) {
							truepositive.get(depth).put(classifiedLabel, 0);
						}
						
						if (falsenegative.containsKey(depth) == false) {
							falsenegative.put(depth, new HashMap<String, Integer>());
						}
						if (falsenegative.get(depth).containsKey(classifiedLabel) == false) {
							falsenegative.get(depth).put(classifiedLabel, 0);
						}
						
						if (falsepositive.containsKey(depth) == false) {
							falsepositive.put(depth, new HashMap<String, Integer>());
						}
						if (falsepositive.get(depth).containsKey(classifiedLabel) == false) {
							falsepositive.get(depth).put(classifiedLabel, 0);
						}
						
						if (depthLabelMap.containsKey(depth) == false) {
							depthLabelMap.put(depth, new HashSet<String>());
						}
						depthLabelMap.get(depth).add(classifiedLabel);
						
						if (trueLabels.contains(classifiedLabel) == false) {
							falsepositive.get(depth).put(classifiedLabel, falsepositive.get(depth).get(classifiedLabel) + 1);
						}
					}
					
					writer.write("\n\r");

				}
				

				count++;
				if (count % 1000 == 0) {
					System.out.println("Classified " + count + " documents ...");
				}
			}
			
			List<Double> precAllList = new ArrayList<Double>();
			List<Double> recAllList = new ArrayList<Double>();
			List<Double> f1AllList = new ArrayList<Double>();
			
			List<Double> tpAllList = new ArrayList<Double>();
			List<Double> fpAllList = new ArrayList<Double>();
			List<Double> fnAllList = new ArrayList<Double>();
			

			List<Double> precLeavesList = new ArrayList<Double>();
			List<Double> recLeavesList = new ArrayList<Double>();
			List<Double> f1LeavesList = new ArrayList<Double>();
			
			List<Double> tpLeavesList = new ArrayList<Double>();
			List<Double> fpLeavesList = new ArrayList<Double>();
			List<Double> fnLeavesList = new ArrayList<Double>();
			
			resultsMap.put("all", new EvalResults());
			resultsMap.put("leaves", new EvalResults());

			System.out.println(System.getProperty("line.separator").toString());

			for (int depth : hitCorrect.keySet()) {
				
				resultsMap.put("depth" + depth, new EvalResults());
				
				System.out.println("Depth: " + depth + ", num: " + depthLabelMap.get(depth).size() + ", " + depthLabelMap.get(depth));
				
				double hitAcc = (double) hitCorrect.get(depth) / (double) hitCount.get(depth);
				System.out.println("Hit precision@" + topK + ": " + String.format("%.4f", hitAcc));
				resultsMap.get("depth" + depth).hitPrecision = hitAcc;
				
				double tp = 0;
				double fp = 0;
				double fn = 0;
				List<Double> precList = new ArrayList<Double>();
				List<Double> recList = new ArrayList<Double>();
				List<Double> f1List = new ArrayList<Double>();
				
				List<Double> tpList = new ArrayList<Double>();
				List<Double> fpList = new ArrayList<Double>();
				List<Double> fnList = new ArrayList<Double>();
				for (String label : depthLabelMap.get(depth)) {
					
					if (truepositive.get(depth).containsKey(label) == true) {
						tp = truepositive.get(depth).get(label);
					} 
					if (falsepositive.get(depth).containsKey(label) == true) {
						fp = falsepositive.get(depth).get(label);
					} 
					if (falsenegative.get(depth).containsKey(label) == true) {
						fn = falsenegative.get(depth).get(label);
					} 
					double precision = tp / (tp + fp + Double.MIN_VALUE);
					double recall = tp / (tp + fn + Double.MIN_VALUE);
					double f1 = 2 * (precision * recall) / (precision + recall + Double.MIN_VALUE);
					
					tpList.add(tp);
					fpList.add(fp);
					fnList.add(fn);
					
					precList.add(precision);
					recList.add(recall);
					f1List.add(f1);
					
					
					tpAllList.add(tp);
					fpAllList.add(fp);
					fnAllList.add(fn);

					precAllList.add(precision);
					recAllList.add(recall);
					f1AllList.add(f1);
					
					boolean isLeaf = false;
					isLeaf = tree.isLeafNode(label);
					if (isLeaf == true) {
						tpLeavesList.add(tp);
						fpLeavesList.add(fp);
						fnLeavesList.add(fn);
						
						precLeavesList.add(precision);
						recLeavesList.add(recall);
						f1LeavesList.add(f1);
					}
				}
				
				resultsMap.get("depth" + depth).precision = StatUtils.listAverage(precList);
				resultsMap.get("depth" + depth).recall = StatUtils.listAverage(recList);
				resultsMap.get("depth" + depth).Mf1 = StatUtils.listAverage(f1List);
				
				double mP = StatUtils.listSum(tpList) / (StatUtils.listSum(tpList) + StatUtils.listSum(fpList));
				double mR = StatUtils.listSum(tpList) / (StatUtils.listSum(tpList) + StatUtils.listSum(fnList));
				resultsMap.get("depth" + depth).mf1 = 2 * mP * mR / (mP + mR);
				
				System.out.println("Precision: " + String.format("%.4f", resultsMap.get("depth" + depth).precision));
				System.out.println("Recall: " + String.format("%.4f", resultsMap.get("depth" + depth).recall));
				System.out.println("mF1: " + String.format("%.4f", resultsMap.get("depth" + depth).mf1));
				System.out.println("MF1: " + String.format("%.4f", resultsMap.get("depth" + depth).Mf1));
				System.out.println(System.getProperty("line.separator").toString());	
			}
			
			resultsMap.get("leaves").precision = StatUtils.listAverage(precLeavesList);
			resultsMap.get("leaves").recall = StatUtils.listAverage(recLeavesList);
			resultsMap.get("leaves").Mf1 = StatUtils.listAverage(f1LeavesList);
			
			double mP = StatUtils.listSum(tpLeavesList) / (StatUtils.listSum(tpLeavesList) + StatUtils.listSum(fpLeavesList));
			double mR = StatUtils.listSum(tpLeavesList) / (StatUtils.listSum(tpLeavesList) + StatUtils.listSum(fnLeavesList));
			resultsMap.get("leaves").mf1 = 2 * mP * mR / (mP + mR);

			System.out.println("All Leaves Average");
			System.out.println("Precision: " + String.format("%.4f", resultsMap.get("leaves").precision));
			System.out.println("Recall: " + String.format("%.4f", resultsMap.get("leaves").recall));
			System.out.println("mF1: " + String.format("%.4f", resultsMap.get("leaves").mf1));
			System.out.println("MF1: " + String.format("%.4f", resultsMap.get("leaves").Mf1));
			System.out.println(System.getProperty("line.separator").toString());	

			
			resultsMap.get("all").precision = StatUtils.listAverage(precAllList);
			resultsMap.get("all").recall = StatUtils.listAverage(recAllList);
			resultsMap.get("all").Mf1 = StatUtils.listAverage(f1AllList);
			
			mP = StatUtils.listSum(tpAllList) / (StatUtils.listSum(tpAllList) + StatUtils.listSum(fpAllList));
			mR = StatUtils.listSum(tpAllList) / (StatUtils.listSum(tpAllList) + StatUtils.listSum(fnAllList));
			resultsMap.get("all").mf1 = 2 * mP * mR / (mP + mR);

			System.out.println("All Average");
			System.out.println("Precision: " + String.format("%.4f", resultsMap.get("all").precision));
			System.out.println("Recall: " + String.format("%.4f", resultsMap.get("all").recall));
			System.out.println("mF1: " + String.format("%.4f", resultsMap.get("all").mf1));
			System.out.println("MF1: " + String.format("%.4f", resultsMap.get("all").Mf1));
			System.out.println(System.getProperty("line.separator").toString());	
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultsMap;
	}

	
	public static void testMultiClassConceptTreeResults (InterfaceMultiClassClassificationTree tree,
			HashMap<String, SparseVector> docIdContentMap, 
			HashMap<String, HashSet<String>> topicDocMap,
			HashMap<String, HashSet<String>> docTopicMap,
			String outputDataFilePath, String outputLabelFilePath,
			int topK) {
		// classification
		HashMap<String, EvalResults> resultsMap = new HashMap<String, EvalResults>();

		try {
			FileWriter writer = new FileWriter(outputDataFilePath);
			int count = 0;
			
			HashMap<Integer, HashMap<String, Integer>> truepositive = new HashMap<Integer, HashMap<String, Integer>>();
			HashMap<Integer, HashMap<String, Integer>> falsenegative = new HashMap<Integer, HashMap<String, Integer>>();
			HashMap<Integer, HashMap<String, Integer>> falsepositive = new HashMap<Integer, HashMap<String, Integer>>();
			
			HashMap<Integer, HashSet<String>> depthLabelMap = new HashMap<Integer, HashSet<String>>();
			
			HashMap<Integer, Integer> hitCorrect = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> hitCount = new HashMap<Integer, Integer>();
			
			for (String docID : docIdContentMap.keySet()) {
				writer.write(docID + "\t");
				// get vector
				SparseVector document = docIdContentMap.get(docID);
				// process document with labels
				if (docID.equals("D:\\data_test\\20newsgroup\\20NG_Source\\comp.windows.x\\66883")) {
					int step = 0;
				}
			
				LabelResultMC labelResults = tree.labelDocument(document);
				
				HashSet<String> trueLabelSet = docTopicMap.get(docID);
				HashMap<Integer, HashSet<String>> trueDepthsLabels = new HashMap<Integer, HashSet<String>>();
				for (String label : trueLabelSet) {
					int depth = tree.getLabelDepth(label);
					if (trueDepthsLabels.containsKey(depth) == false) {
						trueDepthsLabels.put(depth, new HashSet<String>());
					}
					trueDepthsLabels.get(depth).add(label);
					writer.write(depth + "," + label + ";");
				}
				writer.write("\t");
				
				
				HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth = new HashMap<Integer, List<LabelKeyValuePair>>();
				for (LabelKeyValuePair labelValuePair : labelResults.labels) {
					labelValuePair.setLabel(labelValuePair.getLabel().split(":")[1]);
					int depth = tree.getLabelDepth(labelValuePair.getLabel());
					if (labelResultsInDepth.containsKey(depth) == false) {
						labelResultsInDepth.put(depth, new ArrayList<LabelKeyValuePair>());
					}
					labelResultsInDepth.get(depth).add(labelValuePair);
				}
				
				
				for (int depth : trueDepthsLabels.keySet()) {
					// true labels
					HashSet<String> trueLabels = trueDepthsLabels.get(depth);
					//classified labels
					List<LabelKeyValuePair> classifiedLabelScoreList = labelResultsInDepth.get(depth);
					if (classifiedLabelScoreList == null) {
						classifiedLabelScoreList = new ArrayList<LabelKeyValuePair>();
					}
					HashSet<String> classifiedLabels = new HashSet<String>();
					for (int i = 0; i < Math.min(topK, classifiedLabelScoreList.size()); ++i) {
						classifiedLabels.add(classifiedLabelScoreList.get(i).getLabel());
						
						writer.write(depth + "," + classifiedLabelScoreList.get(i).getLabel() + ";");
					}
					
					// evaluate
					for (String trueLabel : trueLabels) {
						if (truepositive.containsKey(depth) == false) {
							truepositive.put(depth, new HashMap<String, Integer>());
						}
						if (truepositive.get(depth).containsKey(trueLabel) == false) {
							truepositive.get(depth).put(trueLabel, 0);
						}
						
						if (falsenegative.containsKey(depth) == false) {
							falsenegative.put(depth, new HashMap<String, Integer>());
						}
						if (falsenegative.get(depth).containsKey(trueLabel) == false) {
							falsenegative.get(depth).put(trueLabel, 0);
						}
						
						if (falsepositive.containsKey(depth) == false) {
							falsepositive.put(depth, new HashMap<String, Integer>());
						}
						if (falsepositive.get(depth).containsKey(trueLabel) == false) {
							falsepositive.get(depth).put(trueLabel, 0);
						}
						
						if (depthLabelMap.containsKey(depth) == false) {
							depthLabelMap.put(depth, new HashSet<String>());
						}
						depthLabelMap.get(depth).add(trueLabel);
						
						
						if (hitCorrect.containsKey(depth) == false) {
							hitCorrect.put(depth, 0);
						}
						if (hitCount.containsKey(depth) == false) {
							hitCount.put(depth, 0);
						}
						
						if (classifiedLabels.contains(trueLabel) == true) {
							
							truepositive.get(depth).put(trueLabel, truepositive.get(depth).get(trueLabel) + 1);
							
							hitCorrect.put(depth, hitCorrect.get(depth) + 1);
							
						} else {
							falsenegative.get(depth).put(trueLabel, falsenegative.get(depth).get(trueLabel) + 1);
						}
						hitCount.put(depth, hitCount.get(depth) + 1);
						
					}
					
					for (String classifiedLabel : classifiedLabels) {
						if (truepositive.containsKey(depth) == false) {
							truepositive.put(depth, new HashMap<String, Integer>());
						}
						if (truepositive.get(depth).containsKey(classifiedLabel) == false) {
							truepositive.get(depth).put(classifiedLabel, 0);
						}
						
						if (falsenegative.containsKey(depth) == false) {
							falsenegative.put(depth, new HashMap<String, Integer>());
						}
						if (falsenegative.get(depth).containsKey(classifiedLabel) == false) {
							falsenegative.get(depth).put(classifiedLabel, 0);
						}
						
						if (falsepositive.containsKey(depth) == false) {
							falsepositive.put(depth, new HashMap<String, Integer>());
						}
						if (falsepositive.get(depth).containsKey(classifiedLabel) == false) {
							falsepositive.get(depth).put(classifiedLabel, 0);
						}
						
						if (depthLabelMap.containsKey(depth) == false) {
							depthLabelMap.put(depth, new HashSet<String>());
						}
						depthLabelMap.get(depth).add(classifiedLabel);
						
						if (trueLabels.contains(classifiedLabel) == false) {
							falsepositive.get(depth).put(classifiedLabel, falsepositive.get(depth).get(classifiedLabel) + 1);
						}
					}
					writer.write("\n\r");
				}
				

				count++;
				if (count % 1000 == 0) {
					System.out.println("Classified " + count + " documents ...");
				}
			}
			
			List<Double> precAllList = new ArrayList<Double>();
			List<Double> recAllList = new ArrayList<Double>();
			List<Double> f1AllList = new ArrayList<Double>();
			
			List<Double> tpAllList = new ArrayList<Double>();
			List<Double> fpAllList = new ArrayList<Double>();
			List<Double> fnAllList = new ArrayList<Double>();
			

			List<Double> precLeavesList = new ArrayList<Double>();
			List<Double> recLeavesList = new ArrayList<Double>();
			List<Double> f1LeavesList = new ArrayList<Double>();
			
			List<Double> tpLeavesList = new ArrayList<Double>();
			List<Double> fpLeavesList = new ArrayList<Double>();
			List<Double> fnLeavesList = new ArrayList<Double>();
			
			resultsMap.put("all", new EvalResults());
			resultsMap.put("leaves", new EvalResults());

			System.out.println(System.getProperty("line.separator").toString());

			for (int depth : hitCorrect.keySet()) {
				
				resultsMap.put("depth" + depth, new EvalResults());
				
				System.out.println("Depth: " + depth);
				
				double hitAcc = (double) hitCorrect.get(depth) / (double) hitCount.get(depth);
				System.out.println("Hit precision@" + topK + ": " + String.format("%.4f", hitAcc));
				resultsMap.get("depth" + depth).hitPrecision = hitAcc;
				
				double tp = 0;
				double fp = 0;
				double fn = 0;
				List<Double> precList = new ArrayList<Double>();
				List<Double> recList = new ArrayList<Double>();
				List<Double> f1List = new ArrayList<Double>();
				
				List<Double> tpList = new ArrayList<Double>();
				List<Double> fpList = new ArrayList<Double>();
				List<Double> fnList = new ArrayList<Double>();
				for (String label : depthLabelMap.get(depth)) {
					
					if (truepositive.get(depth).containsKey(label) == true) {
						tp = truepositive.get(depth).get(label);
					} 
					if (falsepositive.get(depth).containsKey(label) == true) {
						fp = falsepositive.get(depth).get(label);
					} 
					if (falsenegative.get(depth).containsKey(label) == true) {
						fn = falsenegative.get(depth).get(label);
					} 
					double precision = tp / (tp + fp + Double.MIN_VALUE);
					double recall = tp / (tp + fn + Double.MIN_VALUE);
					double f1 = 2 * (precision * recall) / (precision + recall + Double.MIN_VALUE);
					
					tpList.add(tp);
					fpList.add(fp);
					fnList.add(fn);
					
					precList.add(precision);
					recList.add(recall);
					f1List.add(f1);
					
					
					tpAllList.add(tp);
					fpAllList.add(fp);
					fnAllList.add(fn);

					precAllList.add(precision);
					recAllList.add(recall);
					f1AllList.add(f1);
					
					boolean isLeaf = false;
					isLeaf = tree.isLeafNode(label);
					if (isLeaf == true) {
						tpLeavesList.add(tp);
						fpLeavesList.add(fp);
						fnLeavesList.add(fn);
						
						precLeavesList.add(precision);
						recLeavesList.add(recall);
						f1LeavesList.add(f1);
					}
				}
				
				resultsMap.get("depth" + depth).precision = StatUtils.listAverage(precList);
				resultsMap.get("depth" + depth).recall = StatUtils.listAverage(recList);
				resultsMap.get("depth" + depth).Mf1 = StatUtils.listAverage(f1List);
				
				double mP = StatUtils.listSum(tpList) / (StatUtils.listSum(tpList) + StatUtils.listSum(fpList));
				double mR = StatUtils.listSum(tpList) / (StatUtils.listSum(tpList) + StatUtils.listSum(fnList));
				resultsMap.get("depth" + depth).mf1 = 2 * mP * mR / (mP + mR);
				
				System.out.println("Precision: " + String.format("%.4f", resultsMap.get("depth" + depth).precision));
				System.out.println("Recall: " + String.format("%.4f", resultsMap.get("depth" + depth).recall));
				System.out.println("mF1: " + String.format("%.4f", resultsMap.get("depth" + depth).mf1));
				System.out.println("MF1: " + String.format("%.4f", resultsMap.get("depth" + depth).Mf1));
				System.out.println(System.getProperty("line.separator").toString());	
			}
			
			resultsMap.get("leaves").precision = StatUtils.listAverage(precLeavesList);
			resultsMap.get("leaves").recall = StatUtils.listAverage(recLeavesList);
			resultsMap.get("leaves").Mf1 = StatUtils.listAverage(f1LeavesList);
			
			double mP = StatUtils.listSum(tpLeavesList) / (StatUtils.listSum(tpLeavesList) + StatUtils.listSum(fpLeavesList));
			double mR = StatUtils.listSum(tpLeavesList) / (StatUtils.listSum(tpLeavesList) + StatUtils.listSum(fnLeavesList));
			resultsMap.get("leaves").mf1 = 2 * mP * mR / (mP + mR);

			System.out.println("All Leaves Average");
			System.out.println("Precision: " + String.format("%.4f", resultsMap.get("leaves").precision));
			System.out.println("Recall: " + String.format("%.4f", resultsMap.get("leaves").recall));
			System.out.println("mF1: " + String.format("%.4f", resultsMap.get("leaves").mf1));
			System.out.println("MF1: " + String.format("%.4f", resultsMap.get("leaves").Mf1));
			System.out.println(System.getProperty("line.separator").toString());	

			
			resultsMap.get("all").precision = StatUtils.listAverage(precAllList);
			resultsMap.get("all").recall = StatUtils.listAverage(recAllList);
			resultsMap.get("all").Mf1 = StatUtils.listAverage(f1AllList);
			
			mP = StatUtils.listSum(tpAllList) / (StatUtils.listSum(tpAllList) + StatUtils.listSum(fpAllList));
			mR = StatUtils.listSum(tpAllList) / (StatUtils.listSum(tpAllList) + StatUtils.listSum(fnAllList));
			resultsMap.get("all").mf1 = 2 * mP * mR / (mP + mR);

			System.out.println("All Average");
			System.out.println("Precision: " + String.format("%.4f", resultsMap.get("all").precision));
			System.out.println("Recall: " + String.format("%.4f", resultsMap.get("all").recall));
			System.out.println("mF1: " + String.format("%.4f", resultsMap.get("all").mf1));
			System.out.println("MF1: " + String.format("%.4f", resultsMap.get("all").Mf1));
			System.out.println(System.getProperty("line.separator").toString());	

			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static EvalResults evaluate(HashMap<String, HashSet<String>> topicDocMap, 
			HashMap<String, HashSet<String>> labeledTopicDocMap,
			HashMap<String, Boolean> isLeafNode,
			FileWriter writer) {
		
		EvalResults finalResult = new EvalResults();
		try {
			List<EvalResults> resultsEachLabel = new ArrayList<EvalResults>();
			
			for (String topic : topicDocMap.keySet()) {
				if (isLeafNode.get(topic) == true) {
					
					writer.write(topic);
					writer.write(System.getProperty("line.separator").toString());
					
					HashSet<String> labeledData = labeledTopicDocMap.get(topic);
					HashSet<String> truthData = topicDocMap.get(topic);
//					if (labeledData != null) {
//						writer.write("labeledData" + ":\t");
//						for (String doc : labeledData) {
//							writer.write(doc + "\t");
//						}
//						writer.write(System.getProperty("line.separator").toString());
//					}
//					if (truthData != null) {
//						writer.write("truthData" + ":\t");
//						for (String doc : truthData) {
//							writer.write(doc + "\t");
//						}
//						writer.write(System.getProperty("line.separator").toString());
//					}
//					writer.write(System.getProperty("line.separator").toString());
					double precision = 0;
					double recall = 0;
					double f1 = 0;
					if (labeledData != null) {
						Set<String> overlappedSet = new HashSet<String>(labeledData);
						overlappedSet.retainAll(truthData);
						precision = ((double) overlappedSet.size()) / labeledData.size();
						recall = ((double) overlappedSet.size()) / truthData.size();
						f1 = 2 * precision * recall / (precision + recall + Double.MIN_NORMAL);
					} 
					EvalResults evalResult = new EvalResults();
					evalResult.precision = precision;
					evalResult.recall = recall;
					evalResult.Mf1 = f1;
					
					writer.write("leaf precision: " + String.format("%.4f", precision) + "\t");
					writer.write("leaf recall: " + String.format("%.4f", recall) + "\t");
					writer.write("leaf f1: " + String.format("%.4f", f1));
					writer.write(System.getProperty("line.separator").toString());
					writer.write(System.getProperty("line.separator").toString());
					
					resultsEachLabel.add(evalResult);
				}
			}
			for (int i = 0; i < resultsEachLabel.size(); i++) {
				finalResult.precision += resultsEachLabel.get(i).precision;
				finalResult.recall += resultsEachLabel.get(i).recall;
				finalResult.Mf1 += resultsEachLabel.get(i).Mf1;
			}
			finalResult.precision /= resultsEachLabel.size();
			finalResult.recall /= resultsEachLabel.size();
			finalResult.Mf1 /= resultsEachLabel.size();
			
			writer.write("total classes: " + resultsEachLabel.size());
			writer.write(System.getProperty("line.separator").toString());
			writer.write("overall leaf precision: " + String.format("%.4f", finalResult.precision) + "\t");
			writer.write("overall leaf recall: " + String.format("%.4f", finalResult.recall) + "\t");
			writer.write("overall leaf f1: " + String.format("%.4f", finalResult.Mf1));
			writer.write(System.getProperty("line.separator").toString());
			writer.write(System.getProperty("line.separator").toString());

			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return finalResult;
	} 
	
	public static EvalResults evaluate(HashMap<String, HashSet<String>> topicDocMap, 
			HashMap<String, HashSet<String>> labeledTopicDocMap,
			HashMap<String, Integer> treeDepth,
			int depth,
			FileWriter writer) {
		
		EvalResults finalResult = new EvalResults();
		try {
			
			List<EvalResults> resultsEachLabel = new ArrayList<EvalResults>();
			
			for (String topic : topicDocMap.keySet()) {
				if (treeDepth.get(topic) != null && treeDepth.get(topic) == depth) {
					
					writer.write(topic);
					writer.write(System.getProperty("line.separator").toString());
					
					HashSet<String> labeledData = labeledTopicDocMap.get(topic);
					HashSet<String> truthData = topicDocMap.get(topic);
//					if (labeledData != null) {
//						writer.write("labeledData" + ":\t");
//						for (String doc : labeledData) {
//							writer.write(doc + "\t");
//						}
//						writer.write(System.getProperty("line.separator").toString());
//					}
//					if (truthData != null) {
//						writer.write("truthData" + ":\t");
//						for (String doc : truthData) {
//							writer.write(doc + "\t");
//						}
//						writer.write(System.getProperty("line.separator").toString());
//					}
//					writer.write(System.getProperty("line.separator").toString());
					double precision = 0;
					double recall = 0;
					double f1 = 0;
					if (labeledData != null) {
						Set<String> overlappedSet = new HashSet<String>(labeledData);
						overlappedSet.retainAll(truthData);
						precision = ((double) overlappedSet.size()) / labeledData.size();
						recall = ((double) overlappedSet.size()) / truthData.size();
						f1 = 2 * precision * recall / (precision + recall + Double.MIN_NORMAL);
					} 
					EvalResults evalResult = new EvalResults();
					evalResult.precision = precision;
					evalResult.recall = recall;
					evalResult.Mf1 = f1;
					
					writer.write("total classes: " + resultsEachLabel.size());
					writer.write(System.getProperty("line.separator").toString());
					writer.write("depth " + depth + " precision: " + String.format("%.4f", precision) + "\t");
					writer.write("depth " + depth + " recall: " + String.format("%.4f", recall) + "\t");
					writer.write("depth " + depth + " f1: " + String.format("%.4f", f1));
					writer.write(System.getProperty("line.separator").toString());
					writer.write(System.getProperty("line.separator").toString());
					
					resultsEachLabel.add(evalResult);
				}
			}
			for (int i = 0; i < resultsEachLabel.size(); i++) {
				finalResult.precision += resultsEachLabel.get(i).precision;
				finalResult.recall += resultsEachLabel.get(i).recall;
				finalResult.Mf1 += resultsEachLabel.get(i).Mf1;
			}
			finalResult.precision /= resultsEachLabel.size();
			finalResult.recall /= resultsEachLabel.size();
			finalResult.Mf1 /= resultsEachLabel.size();

			writer.write("total classes: " + resultsEachLabel.size());
			writer.write(System.getProperty("line.separator").toString());
			writer.write("overall depth " + depth + " precision: " + String.format("%.4f", finalResult.precision) + "\t");
			writer.write("overall depth " + depth + " recall: " + String.format("%.4f", finalResult.recall) + "\t");
			writer.write("overall depth " + depth + " f1: " + String.format("%.4f", finalResult.Mf1));
			writer.write(System.getProperty("line.separator").toString());
			writer.write(System.getProperty("line.separator").toString());

			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return finalResult;
	}

	public static HashMap<String, EvalResults> testW2V (InterfaceMultiLabelConceptClassificationTree tree,
			HashMap<String, String> docIdContentMap, 
			HashMap<String, HashSet<String>> topicDocMap,
			HashMap<String, HashSet<String>> docTopicMap,
			String outputDataFilePath, String outputLabelFilePath,
			int topK,SparseSimilarityCondensation vectorCondensation) {
		// classification
		HashMap<String, EvalResults> resultsMap = new HashMap<String, EvalResults>();
		try {
			FileWriter writer = new FileWriter(outputDataFilePath);
			int count = 0;
			
			HashMap<Integer, HashMap<String, Integer>> truepositive = new HashMap<Integer, HashMap<String, Integer>>();
			HashMap<Integer, HashMap<String, Integer>> falsenegative = new HashMap<Integer, HashMap<String, Integer>>();
			HashMap<Integer, HashMap<String, Integer>> falsepositive = new HashMap<Integer, HashMap<String, Integer>>();
			
			HashMap<Integer, HashSet<String>> depthLabelMap = new HashMap<Integer, HashSet<String>>();
			
			HashMap<Integer, Integer> hitCorrect = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> hitCount = new HashMap<Integer, Integer>();
			
			for (String docID : docIdContentMap.keySet()) {
				writer.write(docID + "\t");
				// get vector
				String document = docIdContentMap.get(docID);
				// process document with labels
				
				if (docID.equals("D:\\data_test\\20newsgroup\\20NG_Source\\comp.windows.x\\66883")) {
					int step = 0;
				}
				
				HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth = tree.labelDocumentW2V(document);
				
				HashSet<String> trueLabelSet = docTopicMap.get(docID);
				HashMap<Integer, HashSet<String>> trueDepthsLabels = new HashMap<Integer, HashSet<String>>();
				for (String label : trueLabelSet) {
					int depth = tree.searchLabelDepth(label);
					if (trueDepthsLabels.containsKey(depth) == false) {
						trueDepthsLabels.put(depth, new HashSet<String>());
					}
					trueDepthsLabels.get(depth).add(label);
					writer.write(depth + "," + label + ";");
				}
				writer.write("\t");
				
				for (int depth : trueDepthsLabels.keySet()) {
					// true labels
					HashSet<String> trueLabels = trueDepthsLabels.get(depth);
					//classified labels
					List<LabelKeyValuePair> classifiedLabelScoreList = labelResultsInDepth.get(depth);
					if (classifiedLabelScoreList == null) {
						classifiedLabelScoreList = new ArrayList<LabelKeyValuePair>();
					}
					HashSet<String> classifiedLabels = new HashSet<String>();
					for (int i = 0; i < Math.min(topK, classifiedLabelScoreList.size()); ++i) {
						classifiedLabels.add(classifiedLabelScoreList.get(i).getLabel());
						
						writer.write(depth + "," + classifiedLabelScoreList.get(i).getLabel() + ";");
					}
					
					// evaluate
					for (String trueLabel : trueLabels) {
						if (truepositive.containsKey(depth) == false) {
							truepositive.put(depth, new HashMap<String, Integer>());
						}
						if (truepositive.get(depth).containsKey(trueLabel) == false) {
							truepositive.get(depth).put(trueLabel, 0);
						}
						
						if (falsenegative.containsKey(depth) == false) {
							falsenegative.put(depth, new HashMap<String, Integer>());
						}
						if (falsenegative.get(depth).containsKey(trueLabel) == false) {
							falsenegative.get(depth).put(trueLabel, 0);
						}
						
						if (falsepositive.containsKey(depth) == false) {
							falsepositive.put(depth, new HashMap<String, Integer>());
						}
						if (falsepositive.get(depth).containsKey(trueLabel) == false) {
							falsepositive.get(depth).put(trueLabel, 0);
						}
						
						if (depthLabelMap.containsKey(depth) == false) {
							depthLabelMap.put(depth, new HashSet<String>());
						}
						depthLabelMap.get(depth).add(trueLabel);
						
						
						if (hitCorrect.containsKey(depth) == false) {
							hitCorrect.put(depth, 0);
						}
						if (hitCount.containsKey(depth) == false) {
							hitCount.put(depth, 0);
						}
						
						if (classifiedLabels.contains(trueLabel) == true) {
							
							truepositive.get(depth).put(trueLabel, truepositive.get(depth).get(trueLabel) + 1);
							
							hitCorrect.put(depth, hitCorrect.get(depth) + 1);
							
						} else {
							falsenegative.get(depth).put(trueLabel, falsenegative.get(depth).get(trueLabel) + 1);
						}
						hitCount.put(depth, hitCount.get(depth) + 1);
						
					}
					
					for (String classifiedLabel : classifiedLabels) {
						if (truepositive.containsKey(depth) == false) {
							truepositive.put(depth, new HashMap<String, Integer>());
						}
						if (truepositive.get(depth).containsKey(classifiedLabel) == false) {
							truepositive.get(depth).put(classifiedLabel, 0);
						}
						
						if (falsenegative.containsKey(depth) == false) {
							falsenegative.put(depth, new HashMap<String, Integer>());
						}
						if (falsenegative.get(depth).containsKey(classifiedLabel) == false) {
							falsenegative.get(depth).put(classifiedLabel, 0);
						}
						
						if (falsepositive.containsKey(depth) == false) {
							falsepositive.put(depth, new HashMap<String, Integer>());
						}
						if (falsepositive.get(depth).containsKey(classifiedLabel) == false) {
							falsepositive.get(depth).put(classifiedLabel, 0);
						}
						
						if (depthLabelMap.containsKey(depth) == false) {
							depthLabelMap.put(depth, new HashSet<String>());
						}
						depthLabelMap.get(depth).add(classifiedLabel);
						
						if (trueLabels.contains(classifiedLabel) == false) {
							falsepositive.get(depth).put(classifiedLabel, falsepositive.get(depth).get(classifiedLabel) + 1);
						}
					}
					
					writer.write("\n\r");

				}
				

				count++;
				if (count % 1000 == 0) {
					System.out.println("Classified " + count + " documents ...");
				}
			}
			
			List<Double> precAllList = new ArrayList<Double>();
			List<Double> recAllList = new ArrayList<Double>();
			List<Double> f1AllList = new ArrayList<Double>();
			
			List<Double> tpAllList = new ArrayList<Double>();
			List<Double> fpAllList = new ArrayList<Double>();
			List<Double> fnAllList = new ArrayList<Double>();
			

			List<Double> precLeavesList = new ArrayList<Double>();
			List<Double> recLeavesList = new ArrayList<Double>();
			List<Double> f1LeavesList = new ArrayList<Double>();
			
			List<Double> tpLeavesList = new ArrayList<Double>();
			List<Double> fpLeavesList = new ArrayList<Double>();
			List<Double> fnLeavesList = new ArrayList<Double>();
			
			resultsMap.put("all", new EvalResults());
			resultsMap.put("leaves", new EvalResults());

			System.out.println(System.getProperty("line.separator").toString());

			for (int depth : hitCorrect.keySet()) {
				
				resultsMap.put("depth" + depth, new EvalResults());
				
				System.out.println("Depth: " + depth + ", num: " + depthLabelMap.get(depth).size() + ", " + depthLabelMap.get(depth));
				
				double hitAcc = (double) hitCorrect.get(depth) / (double) hitCount.get(depth);
				System.out.println("Hit precision@" + topK + ": " + String.format("%.4f", hitAcc));
				resultsMap.get("depth" + depth).hitPrecision = hitAcc;
				
				double tp = 0;
				double fp = 0;
				double fn = 0;
				List<Double> precList = new ArrayList<Double>();
				List<Double> recList = new ArrayList<Double>();
				List<Double> f1List = new ArrayList<Double>();
				
				List<Double> tpList = new ArrayList<Double>();
				List<Double> fpList = new ArrayList<Double>();
				List<Double> fnList = new ArrayList<Double>();
				for (String label : depthLabelMap.get(depth)) {
					
					if (truepositive.get(depth).containsKey(label) == true) {
						tp = truepositive.get(depth).get(label);
					} 
					if (falsepositive.get(depth).containsKey(label) == true) {
						fp = falsepositive.get(depth).get(label);
					} 
					if (falsenegative.get(depth).containsKey(label) == true) {
						fn = falsenegative.get(depth).get(label);
					} 
					double precision = tp / (tp + fp + Double.MIN_VALUE);
					double recall = tp / (tp + fn + Double.MIN_VALUE);
					double f1 = 2 * (precision * recall) / (precision + recall + Double.MIN_VALUE);
					
					tpList.add(tp);
					fpList.add(fp);
					fnList.add(fn);
					
					precList.add(precision);
					recList.add(recall);
					f1List.add(f1);
					
					
					tpAllList.add(tp);
					fpAllList.add(fp);
					fnAllList.add(fn);

					precAllList.add(precision);
					recAllList.add(recall);
					f1AllList.add(f1);
					
					boolean isLeaf = false;
					isLeaf = tree.isLeafNode(label);
					if (isLeaf == true) {
						tpLeavesList.add(tp);
						fpLeavesList.add(fp);
						fnLeavesList.add(fn);
						
						precLeavesList.add(precision);
						recLeavesList.add(recall);
						f1LeavesList.add(f1);
					}
				}
				
				resultsMap.get("depth" + depth).precision = StatUtils.listAverage(precList);
				resultsMap.get("depth" + depth).recall = StatUtils.listAverage(recList);
				resultsMap.get("depth" + depth).Mf1 = StatUtils.listAverage(f1List);
				
				double mP = StatUtils.listSum(tpList) / (StatUtils.listSum(tpList) + StatUtils.listSum(fpList));
				double mR = StatUtils.listSum(tpList) / (StatUtils.listSum(tpList) + StatUtils.listSum(fnList));
				resultsMap.get("depth" + depth).mf1 = 2 * mP * mR / (mP + mR);
				
				System.out.println("Precision: " + String.format("%.4f", resultsMap.get("depth" + depth).precision));
				System.out.println("Recall: " + String.format("%.4f", resultsMap.get("depth" + depth).recall));
				System.out.println("mF1: " + String.format("%.4f", resultsMap.get("depth" + depth).mf1));
				System.out.println("MF1: " + String.format("%.4f", resultsMap.get("depth" + depth).Mf1));
				System.out.println(System.getProperty("line.separator").toString());	
			}
			
			resultsMap.get("leaves").precision = StatUtils.listAverage(precLeavesList);
			resultsMap.get("leaves").recall = StatUtils.listAverage(recLeavesList);
			resultsMap.get("leaves").Mf1 = StatUtils.listAverage(f1LeavesList);
			
			double mP = StatUtils.listSum(tpLeavesList) / (StatUtils.listSum(tpLeavesList) + StatUtils.listSum(fpLeavesList));
			double mR = StatUtils.listSum(tpLeavesList) / (StatUtils.listSum(tpLeavesList) + StatUtils.listSum(fnLeavesList));
			resultsMap.get("leaves").mf1 = 2 * mP * mR / (mP + mR);

			System.out.println("All Leaves Average");
			System.out.println("Precision: " + String.format("%.4f", resultsMap.get("leaves").precision));
			System.out.println("Recall: " + String.format("%.4f", resultsMap.get("leaves").recall));
			System.out.println("mF1: " + String.format("%.4f", resultsMap.get("leaves").mf1));
			System.out.println("MF1: " + String.format("%.4f", resultsMap.get("leaves").Mf1));
			System.out.println(System.getProperty("line.separator").toString());	

			
			resultsMap.get("all").precision = StatUtils.listAverage(precAllList);
			resultsMap.get("all").recall = StatUtils.listAverage(recAllList);
			resultsMap.get("all").Mf1 = StatUtils.listAverage(f1AllList);
			
			mP = StatUtils.listSum(tpAllList) / (StatUtils.listSum(tpAllList) + StatUtils.listSum(fpAllList));
			mR = StatUtils.listSum(tpAllList) / (StatUtils.listSum(tpAllList) + StatUtils.listSum(fnAllList));
			resultsMap.get("all").mf1 = 2 * mP * mR / (mP + mR);

			System.out.println("All Average");
			System.out.println("Precision: " + String.format("%.4f", resultsMap.get("all").precision));
			System.out.println("Recall: " + String.format("%.4f", resultsMap.get("all").recall));
			System.out.println("mF1: " + String.format("%.4f", resultsMap.get("all").mf1));
			System.out.println("MF1: " + String.format("%.4f", resultsMap.get("all").Mf1));
			System.out.println(System.getProperty("line.separator").toString());	
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultsMap;
	}




	
}

