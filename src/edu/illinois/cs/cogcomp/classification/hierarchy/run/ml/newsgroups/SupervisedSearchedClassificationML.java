package edu.illinois.cs.cogcomp.classification.hierarchy.run.ml.newsgroups;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.ml.ClassifierLibLinearTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.ml.ClassifierLibLinearTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.nytimes.NYTimesTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.searchbased.SearchedDataCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.searchbased.SearchedTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.StopWords;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.Evaluation;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

public class SupervisedSearchedClassificationML {
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();

	static double trainingRate = 0.5;
	
	public static void main(String[] args) {
		test20NewsgroupsDataTopDown(1);
//		test20NewsgroupsDataBottomUP(1);
	}
	
	public static void test20NewsgroupsDataTopDown (int topK) {
		int seed = 0;
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
			outputClassificationFile = "/shared/saruman/yqsong/data/benchmark/20newsgroups/output/result.multiclass.20newsgroups.classification";
			outputLabelComparisonFile = "/shared/saruman/yqsong/data/benchmark/20newsgroups/output/result.multiclass.20newsgroups.labelComparison";
		} else {
			fileTopicHierarchyPath = "";
			fileTopicDescriptionPath = "";
//			fileContentDataPathWikipedia = "D:/yqsong/data/20newsgroups/output/20newsgroups.wikipedia.search.top200.data";
//			fileTopicDocMapPathWikipedia = "D:/yqsong/data/20newsgroups/output/20newsgroups.wikipedia.search.top200.data";
			fileContentDataPathWikipedia = "D:/yqsong/data/20newsgroups/output/20newsgroups.datadriven.search.top200.data";
			fileTopicDocMapPathWikipedia = "D:/yqsong/data/20newsgroups/output/20newsgroups.datadriven.search.top200.data";

			fileContentDataPathOriginalData = "D:/yqsong/data/20newsgroups/textindex";
			fileTopicDocMapPathOriginalData = "D:/yqsong/data/20newsgroups/textindex";

			outputClassificationFile = "D:/yqsong/data/20newsgroups/output/result.multiclass.20newsgroups.classification";
			outputLabelComparisonFile = "D:/yqsong/data/20newsgroups/output/result.multiclass.20newsgroups.labelComparison";
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
	    
		ClassifierLibLinearTreeTopDownML classifierTree = new ClassifierLibLinearTreeTopDownML("20newsgroups");
		classifierTree.setPenaltyParaC(10000);
		classifierTree.initializeWithContentData(
				trainingData.getCorpusContentMap(),
				treeLabelData,
				topicDocMapTrainingData);
		classifierTree.trainAllTreeNodes();
		
		Calendar cal2 = Calendar.getInstance();
		long endTime = cal2.getTimeInMillis();
		long secondTraining = (endTime - startTime)/1000;

		Calendar cal3 = Calendar.getInstance();
	    startTime = cal3.getTimeInMillis();
	    
		// read topic doc maps
		HashMap<String, HashSet<String>> topicDocMap = topicDocMapTestData.getTopicDocMap();
		HashMap<String, HashSet<String>> docTopicMap = topicDocMapTestData.getDocTopicMap();
		Evaluation.testMultiLabelContentTreeResults(classifierTree, 
				testData.getCorpusContentMap(),
				null,
				topicDocMap, 
				docTopicMap, 
				outputClassificationFile, 
				outputLabelComparisonFile, 
				topK, false); 
		
		Evaluation.testMultiLabelRandomGuessResults(classifierTree, 
				testData.getCorpusContentMap().keySet(),
				topicDocMap, 
				docTopicMap, 
				topK); 
		
		Calendar cal4 = Calendar.getInstance();
		endTime = cal4.getTimeInMillis();
		long secondTesting = (endTime - startTime)/1000;
		System.out.println("  [Training time:] " + secondTraining + " seconds");
		System.out.println("  [Training time:] " + secondTesting + " seconds");
	}
	
	
	public static void test20NewsgroupsDataBottomUP(int topK) {
		int seed = 0;
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
			outputClassificationFile = "/shared/saruman/yqsong/data/benchmark/20newsgroups/output/result.multiclass.20newsgroups.classification";
			outputLabelComparisonFile = "/shared/saruman/yqsong/data/benchmark/20newsgroups/output/result.multiclass.20newsgroups.labelComparison";
		} else {
			fileTopicHierarchyPath = "";
			fileTopicDescriptionPath = "";
//			fileContentDataPathWikipedia = "D:/yqsong/data/20newsgroups/output/20newsgroups.wikipedia.search.top50.data";
//			fileTopicDocMapPathWikipedia = "D:/yqsong/data/20newsgroups/output/20newsgroups.wikipedia.search.top50.data";
			fileContentDataPathWikipedia = "D:/yqsong/data/20newsgroups/output/20newsgroups.datadriven.search.top50.data";
			fileTopicDocMapPathWikipedia = "D:/yqsong/data/20newsgroups/output/20newsgroups.datadriven.search.top50.data";

			fileContentDataPathOriginalData = "D:/yqsong/data/20newsgroups/textindex";
			fileTopicDocMapPathOriginalData = "D:/yqsong/data/20newsgroups/textindex";

			outputClassificationFile = "D:/yqsong/data/20newsgroups/output/result.multiclass.20newsgroups.classification";
			outputLabelComparisonFile = "D:/yqsong/data/20newsgroups/output/result.multiclass.20newsgroups.labelComparison";
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
	    
	    ClassifierLibLinearTreeBottomUpML classifierTree = new ClassifierLibLinearTreeBottomUpML("20newsgroups");
	    classifierTree.setPenaltyParaC(10000);
	    classifierTree.initializeWithContentData(
				trainingData.getCorpusContentMapTraining(),
				treeLabelData,
				topicDocMapTrainingData);
		classifierTree.trainAllTreeNodes();
		
		Calendar cal2 = Calendar.getInstance();
		long endTime = cal2.getTimeInMillis();
		long secondTraining = (endTime - startTime)/1000;

		Calendar cal3 = Calendar.getInstance();
	    startTime = cal3.getTimeInMillis();
	    
		// read topic doc maps
	    HashMap<String, HashSet<String>> topicDocMap = topicDocMapTestData.getTopicDocMap();
	    HashMap<String, HashSet<String>> docTopicMap = topicDocMapTestData.getDocTopicMap();
	    Evaluation.testMultiLabelContentTreeResults(classifierTree, 
	 				testData.getCorpusContentMap(),
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
		
		Evaluation.testMultiLabelRandomGuessResults(classifierTree, 
				testData.getCorpusContentMap().keySet(),
				topicDocMap, 
				docTopicMap, 
				topK); 
	}

}
