package edu.illinois.cs.cogcomp.classification.hierarchy.run.ml.newsgroups;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.ml.ClassifierLibLinearTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.ml.ClassifierLibLinearTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.sample.HierarchicalTopicModelTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.nytimes.NYTimesTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.searchbased.SearchedDataCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.searchbased.SearchedTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.StopWords;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.EvalResults;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.Evaluation;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.RandomOperations;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.StatUtils;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

public class HierarchicalTopicClassificationML {
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();

	static double trainingRate = 0.5;
	
	public static void main(String[] args) {
		
		try {
			
			int searchDocNum = Integer.parseInt(args[0]);
			
			List<Double> precisionList = new ArrayList<Double>();
			List<Double> recallList = new ArrayList<Double>();
			List<Double> mf1List = new ArrayList<Double>();
			List<Double> Mf1List = new ArrayList<Double>();
			FileWriter writerFinalResults = null;
			if (ClassifierConstant.isServer == true) {
				writerFinalResults = new FileWriter("/shared/saruman/yqsong/temp/ohlda.20newsgroups." + searchDocNum + ".txt");
			} else {
				writerFinalResults = new FileWriter("C:/Users/yqsong/Downloads/ohlda.20newsgroups." + searchDocNum + ".txt");
			}
			for (int i = 0; i < 5; ++i) {
				RandomOperations.random = new Random(i);
				EvalResults result = test20NewsgroupsDataTopDown(1, i, searchDocNum);
				
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
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static EvalResults test20NewsgroupsDataTopDown (int topK, int seed, int searchDocNum) {
		Random random = new Random(seed);
		
		String stopWordsFile = "";
		stopWordsFile = "data/rcvTest/english.stop";
		StopWords.rcvStopWords = StopWords.readStopWords (stopWordsFile);

		String fileTopicHierarchyPath = "";
		String fileTopicDescriptionPath = "";
		String fileContentDataPathWikipedia = "";
		String fileTopicDocMapPathWikipedia = "";
		String fileContentDataPathOriginalData = "";
		String fileTopicDocMapPathOriginalData = "";
		String outputClassificationFile = "";
		String outputLabelComparisonFile = "";
		if (ClassifierConstant.isServer == true) {
			fileTopicHierarchyPath = "";
			fileTopicDescriptionPath = "";
			fileContentDataPathWikipedia = "/shared/saruman/yqsong/data/benchmark/20newsgroups/textindex";
			fileTopicDocMapPathWikipedia = "/shared/saruman/yqsong/data/benchmark/20newsgroups/textindex";
			
			fileContentDataPathWikipedia = "/shared/saruman/yqsong/data/benchmark/20newsgroups/output/20newsgroups.wikipedia.search.top" + searchDocNum + ".data";
			fileTopicDocMapPathWikipedia = "/shared/saruman/yqsong/data/benchmark/20newsgroups/output/20newsgroups.wikipedia.search.top" + searchDocNum + ".data";
//			fileContentDataPathWikipedia = "D:/yqsong/data/20newsgroups/output/20newsgroups.datadriven.search.top100.data";
//			fileTopicDocMapPathWikipedia = "D:/yqsong/data/20newsgroups/output/20newsgroups.datadriven.search.top100.data";

			fileContentDataPathOriginalData = "/shared/saruman/yqsong/data/benchmark/20newsgroups/textindex";
			fileTopicDocMapPathOriginalData = "/shared/saruman/yqsong/data/benchmark/20newsgroups/textindex";

			outputClassificationFile = "/shared/saruman/yqsong/data/benchmark/20newsgroups/output/result.multiclass.20newsgroups.classification";
			outputLabelComparisonFile = "/shared/saruman/yqsong/data/benchmark/20newsgroups/output/result.multiclass.20newsgroups.labelComparison";
		} else {
			fileTopicHierarchyPath = "";
			fileTopicDescriptionPath = "";
			fileContentDataPathWikipedia = "D:/yqsong/data/20newsgroups/output/20newsgroups.wikipedia.search.top" + searchDocNum + ".data";
			fileTopicDocMapPathWikipedia = "D:/yqsong/data/20newsgroups/output/20newsgroups.wikipedia.search.top" + searchDocNum + ".data";
//			fileContentDataPathWikipedia = "D:/yqsong/data/20newsgroups/output/20newsgroups.datadriven.search.top100.data";
//			fileTopicDocMapPathWikipedia = "D:/yqsong/data/20newsgroups/output/20newsgroups.datadriven.search.top100.data";

			fileContentDataPathOriginalData = "D:/yqsong/data/20newsgroups/textindex";
			fileTopicDocMapPathOriginalData = "D:/yqsong/data/20newsgroups/textindex";

			outputClassificationFile = "D:/yqsong/data/20newsgroups/output/result.ohlda.20newsgroups.classification";
			outputLabelComparisonFile = "D:/yqsong/data/20newsgroups/output/result.ohlda.20newsgroups.labelComparison";
		}
		
		NewsgroupsTreeLabelData treeLabelData = new NewsgroupsTreeLabelData();
		treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		treeLabelData.readTopicDescription(fileTopicDescriptionPath);

		//data
		SearchedDataCorpusConceptData trainingData = new SearchedDataCorpusConceptData();
		trainingData.readCorpusContentOnly(fileContentDataPathWikipedia, random, trainingRate);
		
		NewsgroupsCorpusConceptData testData = new NewsgroupsCorpusConceptData();
		testData.readCorpusContentOnly(fileContentDataPathOriginalData, random, trainingRate);

		//label
		SearchedTopicDocMaps topicDocMapTrainingData = new SearchedTopicDocMaps();
		topicDocMapTrainingData.readFilteredTopicDocMap(fileContentDataPathWikipedia, trainingData.getCorpusContentMap().keySet());

		NewsgroupsTopicDocMaps topicDocMapTestData = new NewsgroupsTopicDocMaps();
		topicDocMapTestData.readFilteredTopicDocMap(fileTopicDocMapPathOriginalData, testData.getCorpusContentMap().keySet());

		
		Calendar cal1 = Calendar.getInstance();
	    long startTime = cal1.getTimeInMillis();
	    

	    HierarchicalTopicModelTree classifierTree = new HierarchicalTopicModelTree("20newsgroups");
		classifierTree.initialize(
				trainingData.getCorpusContentMap(),
				treeLabelData,
				topicDocMapTrainingData);
		classifierTree.training(ClassifierConstant.maxIterTopicModel);
		
		Calendar cal2 = Calendar.getInstance();
		long endTime = cal2.getTimeInMillis();
		long secondTraining = (endTime - startTime)/1000;

		Calendar cal3 = Calendar.getInstance();
	    startTime = cal3.getTimeInMillis();
	    
		// read topic doc maps
		HashMap<String, HashSet<String>> topicDocMap = topicDocMapTestData.getTopicDocMap();
		HashMap<String, HashSet<String>> docTopicMap = topicDocMapTestData.getDocTopicMap();
		HashMap<String, EvalResults> resultMap = Evaluation.testMultiLabelContentTreeResults(classifierTree, 
				testData.getCorpusContentMap(),
				null,
				topicDocMap, 
				docTopicMap, 
				outputClassificationFile, 
				outputLabelComparisonFile, 
				topK, false); 
		
//		Evaluation.testMultiLabelRandomGuessResults(classifierTree, 
//				testData.getCorpusContentMap().keySet(),
//				topicDocMap, 
//				docTopicMap, 
//				topK); 
		
		Calendar cal4 = Calendar.getInstance();
		endTime = cal4.getTimeInMillis();
		long secondTesting = (endTime - startTime)/1000;
		System.out.println("  [Training time:] " + secondTraining + " seconds");
		System.out.println("  [Training time:] " + secondTesting + " seconds");
		
		return resultMap.get("all");
	}
	
	
}
