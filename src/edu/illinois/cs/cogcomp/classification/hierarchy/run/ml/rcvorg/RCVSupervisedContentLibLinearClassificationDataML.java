package edu.illinois.cs.cogcomp.classification.hierarchy.run.ml.rcvorg;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.AbstractClassifierLBJTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.ml.ClassifierLBJTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.ml.ClassifierLBJTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.AbstractClassifierLibLinearTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.mc.ClassifierLibLinearTreeTopDownMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.ml.ClassifierLibLinearTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.ml.ClassifierLibLinearTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.AbstractLabelTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiLabelContentClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.mc.ConceptTreeMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultML;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.StopWords;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.EvalResults;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.Evaluation;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.StatUtils;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

public class RCVSupervisedContentLibLinearClassificationDataML {
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();

	
	public static void main(String[] args) {

	}
	
	public static void testTop1 (String learningMethod, String direction, double trainingRate) {
		try {
			List<Double> precisionList = new ArrayList<Double>();
			List<Double> recallList = new ArrayList<Double>();
			List<Double> mf1List = new ArrayList<Double>();
			List<Double> Mf1List = new ArrayList<Double>();
			FileWriter writerFinalResults = null;
			String completeLearningMethod = learningMethod;
			if (learningMethod.equals("lbj") == false) {
				completeLearningMethod += "_" + ClassifierConstant.solver.name();
			}
			if (ClassifierConstant.isServer == true) {
				writerFinalResults = new FileWriter("/shared/saruman/yqsong/tempsupervised/rcv_supervised_" + completeLearningMethod + "_" + trainingRate + "_" + direction + ".txt");
			} else {
				writerFinalResults = new FileWriter("C:/Users/yqsong/Downloads/rcv_supervised_" + completeLearningMethod + "_" + trainingRate + "_" + direction + ".txt");
			}
			for (int i = 0; i < 10; ++i) {
				double penalty = 1000000;//Math.pow(i+1, 10);
				int topK = 1;
				int seed = i;
				EvalResults result = testRCVData(learningMethod, direction, penalty, topK, seed, trainingRate);
				
				precisionList.add(result.precision);
				recallList.add(result.recall);
				mf1List.add(result.mf1);
				Mf1List.add(result.Mf1);
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
			
			System.out.println();
			
			System.out.println("average mf1:" + StatUtils.listAverage(mf1List) 
					+ "," + StatUtils.std(mf1List, StatUtils.listAverage(mf1List)) + "\n\r");
			System.out.println("average Mf1:" + StatUtils.listAverage(Mf1List) 
					+ "," + StatUtils.std(Mf1List, StatUtils.listAverage(Mf1List)) + "\n\r");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static EvalResults testRCVData (String learningMethod, String direction, double penaltyPara, int topK, int seed, double trainingRate) {
		Random random = new Random(seed);
		
		String stopWordsFile = "";
		stopWordsFile = "data/rcvTest/english.stop";
		StopWords.rcvStopWords = StopWords.readStopWords (stopWordsFile);

		String fileTopicHierarchyPath = "";
		String fileTopicDescriptionPath = "";
		String fileContentDataPath = "";
		String fileTopicDocMapPath = "";
		String outputClassificationFile = "";
		String outputLabelComparisonFile = "";
		if (ClassifierConstant.isServer == true) {
			fileTopicHierarchyPath = "data/rcvTest/rcv1.topics.hier.orig";
			fileTopicDescriptionPath = "data/rcvTest/topics.rbb";
			fileContentDataPath = "/shared/shelley/yqsong/benchmark/rcv1v2/output_train/rcv_train.simple.esa.concepts.200";
			fileTopicDocMapPath = "data/rcvTest/rcv1-v2.topics.qrels";
			outputClassificationFile = "/shared/shelley/yqsong/benchmark/rcv1v2/output/result.concept.rcv.classification";
			outputLabelComparisonFile = "/shared/shelley/yqsong/benchmark/rcv1v2/output/result.concept.rcv.labelComparison";
		} else {
			fileTopicHierarchyPath = "data/rcvTest/rcv1.topics.hier.orig";
			fileTopicDescriptionPath = "data/rcvTest/topics.rbb";
			fileContentDataPath = "D:/yqsong/data/rcvData/output_train/rcv_train.simple.esa.concepts.200";
			fileTopicDocMapPath = "data/rcvTest/rcv1-v2.topics.qrels";
			outputClassificationFile = "D:/yqsong/data/rcvData/output/result.concept.rcv.classification";
			outputLabelComparisonFile = "D:/yqsong/data/rcvData/output/result.concept.rcv.labelComparison";
		}
		
		RCVCorpusConceptData testData = new RCVCorpusConceptData();
		testData.readCorpusContentAndConcepts(fileContentDataPath, ClassifierConstant.isBreakConcepts, random, trainingRate, conceptWeights);;

		Calendar cal1 = Calendar.getInstance();
	    long startTime = cal1.getTimeInMillis();
	    
//		ClassifierLibLinearTreeTopDownML classifierTree = new ClassifierLibLinearTreeTopDownML("rcv");
////		ClassifierLibLinearTreeBottomUpML classifierTree = new ClassifierLibLinearTreeBottomUpML("rcv");
//		classifierTree.initializeWithContentData(testData.getCorpusContentMapTraining(),
//				fileTopicHierarchyPath,
//				fileTopicDescriptionPath,
//				fileTopicDocMapPath);
//		classifierTree.trainAllTreeNodes();
		
		AbstractLabelTree classifierTree = null;
		if (learningMethod.equals("lbj")) {
			AbstractClassifierLBJTree classifierTreeUpdate = null;
			if (direction.equals("bottomup")) {
				classifierTreeUpdate = new ClassifierLBJTreeBottomUpML("rcv");
			} else {
				classifierTreeUpdate = new ClassifierLBJTreeTopDownML("rcv");
			}
			
			classifierTreeUpdate.initializeWithContentData(testData.getCorpusContentMapTraining(),
					fileTopicHierarchyPath,
					fileTopicDescriptionPath,
					fileTopicDocMapPath);
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
			
			classifierTreeUpdate.initializeWithContentData(testData.getCorpusContentMapTraining(),
					fileTopicHierarchyPath,
					fileTopicDescriptionPath,
					fileTopicDocMapPath);
			classifierTreeUpdate.setPenaltyParaC(penaltyPara);
			classifierTreeUpdate.trainAllTreeNodes();
			classifierTree = classifierTreeUpdate;
		}
	
		
		Calendar cal2 = Calendar.getInstance();
		long endTime = cal2.getTimeInMillis();
		long secondTraining = (endTime - startTime)/1000;

		Calendar cal3 = Calendar.getInstance();
	    startTime = cal3.getTimeInMillis();
	    
		// read topic doc maps
		RCVTopicDocMaps testTopicDocMapData = new RCVTopicDocMaps();
		testTopicDocMapData.readFilteredTopicDocMap (fileTopicDocMapPath, testData.getCorpusContentMapTest().keySet());
		HashMap<String, HashSet<String>> topicDocMap = testTopicDocMapData.getTopicDocMap();
		HashMap<String, HashSet<String>> docTopicMap = testTopicDocMapData.getDocTopicMap();
		HashMap<String, EvalResults> resultMap = Evaluation.testMultiLabelContentTreeResults((InterfaceMultiLabelContentClassificationTree) classifierTree, 
				testData.getCorpusContentMapTest(), 
				null,
				topicDocMap, 
				docTopicMap, 
				outputClassificationFile, 
				outputLabelComparisonFile, 
				topK, false); 
		
		Calendar cal4 = Calendar.getInstance();
		endTime = cal4.getTimeInMillis();
		long secondTesting = (endTime - startTime)/1000;
		System.out.println("  [Training time:] " + secondTraining + " seconds");
		System.out.println("  [Training time:] " + secondTesting + " seconds");
		
		return resultMap.get("all");
	}
	

	
}
