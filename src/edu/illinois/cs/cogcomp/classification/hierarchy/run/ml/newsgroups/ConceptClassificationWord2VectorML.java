package edu.illinois.cs.cogcomp.classification.hierarchy.run.ml.newsgroups;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import edu.illinois.cs.cogcomp.classification.densification.representation.SparseSimilarityCondensation;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.StopWords;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.Evaluation;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

/**
 * yqsong@illinois.edu
 */

public class ConceptClassificationWord2VectorML {

	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();

	
	public static void main(String[] args) {
		
		test20NGWordDist_W2V(1,500,100,1);

	}
	
	public static void test20NGWordDist(int topK, int dimNum) {
		
		int seed = 0;
		Random random = new Random(seed);
		double trainingRate = 0.5;

		String stopWordsFile = "";
		String docIDContentConceptFile = "";
		String docIDTopicMapFile = "";
		String treeConceptFile = "";
		String outputClassificationFile = "";
		String outputLabelComparisonFile = "";
		String method = "wordDist";
		String data = "20newsgroups";
		stopWordsFile = "data/rcvTest/english.stop";
		docIDContentConceptFile = "data/20newsgroups/representation/20newsgroups.word.dist.features.enwiki.vivek.500.tf";
		docIDTopicMapFile = "data/20newsgroups/textindex";
		treeConceptFile = "data/20newsgroups/representation/tree.20newsgroups.word.dist.features.enwiki.vivek.newrefine.500.df";
		outputClassificationFile = "data/20newsgroups/output/result.concept.20newsgroups.classification";
		outputLabelComparisonFile = "data/20newsgroups/output/result.concept.20newsgroups.labelComparison";

		
		StopWords.rcvStopWords = StopWords.readStopWords (stopWordsFile);
		
		NewsgroupsCorpusConceptData corpusContentProc = new NewsgroupsCorpusConceptData();
		corpusContentProc.readCorpusContentAndConcepts(docIDContentConceptFile, ClassifierConstant.isBreakConcepts, random, trainingRate, conceptWeights);

		// read topic doc maps
		NewsgroupsTopicDocMaps newsgroupsTDM = new NewsgroupsTopicDocMaps();
		newsgroupsTDM.readFilteredTopicDocMap (docIDTopicMapFile, corpusContentProc.getCorpusConceptVectorMap().keySet());
		
		HashMap<String, HashSet<String>> topicDocMap = newsgroupsTDM.getTopicDocMap();
		HashMap<String, HashSet<String>> docTopicMap = newsgroupsTDM.getDocTopicMap();
		
		// read tree
		ConceptTreeBottomUpML tree = new ConceptTreeBottomUpML(data, method, conceptWeights, false);
//		ConceptTreeTopDownML tree = new ConceptTreeTopDownML(data, method, conceptWeights, false);
		System.out.println("process tree...");
		tree.readLabelTreeFromDump(treeConceptFile, ClassifierConstant.isBreakConcepts);
		ConceptTreeNode rootNode = tree.initializeTreeWithConceptVector("root", 0, ClassifierConstant.isBreakConcepts);
		tree.setRootNode(rootNode);
		System.out.println("process tree finished");
		
		Evaluation.testMultiLabelConceptTreeResults (tree,
				corpusContentProc.getCorpusConceptVectorMap(), 
				topicDocMap, docTopicMap,
				outputClassificationFile,  outputLabelComparisonFile, 
				topK);

	}
	public static void test20NGWordDist_W2V(int topK, int dimNum,int count,int type) {
		
		int seed = 0;
		Random random = new Random(seed);
		double trainingRate = 0.5;

		String stopWordsFile = "";
		String docIDContentConceptFile = "";
		String docIDTopicMapFile = "";
		String treeConceptFile = "";
		String outputClassificationFile = "";
		String outputLabelComparisonFile = "";
		String method = "wordDist";
		String data = "20newsgroups";
		stopWordsFile = "data/rcvTest/english.stop";
		docIDContentConceptFile = "data/20newsgroups/representation/20newsgroups.word.dist.features.enwiki.vivek.500.tf";
		docIDTopicMapFile = "data/20newsgroups/textindex";
		treeConceptFile = "data/20newsgroups/representation/tree.20newsgroups.word.dist.features.enwiki.vivek.newrefine.500.df";
		outputClassificationFile = "data/20newsgroups/output/result.concept.20newsgroups.classification";
		outputLabelComparisonFile = "data/20newsgroups/output/result.concept.20newsgroups.labelComparison";
		//SparseSimilarityCondensation vectorCondensation = new SparseSimilarityCondensation(matchingSource,count, type); 
		StopWords.rcvStopWords = StopWords.readStopWords (stopWordsFile);
		
		NewsgroupsCorpusConceptData corpusContentProc = new NewsgroupsCorpusConceptData();
		corpusContentProc.readCorpusContentAndConcepts(docIDContentConceptFile, ClassifierConstant.isBreakConcepts, random, trainingRate, conceptWeights);

		// read topic doc maps
		NewsgroupsTopicDocMaps newsgroupsTDM = new NewsgroupsTopicDocMaps();
		newsgroupsTDM.readFilteredTopicDocMap (docIDTopicMapFile, corpusContentProc.getCorpusConceptVectorMap().keySet());
		
		HashMap<String, HashSet<String>> topicDocMap = newsgroupsTDM.getTopicDocMap();
		HashMap<String, HashSet<String>> docTopicMap = newsgroupsTDM.getDocTopicMap();
		
		// read tree
		ConceptTreeBottomUpML tree = new ConceptTreeBottomUpML(data, method, conceptWeights, false);
//		ConceptTreeTopDownML tree = new ConceptTreeTopDownML(data, method, conceptWeights, false);
		System.out.println("process tree...");
		tree.readLabelTreeFromDump(treeConceptFile, ClassifierConstant.isBreakConcepts);
		ConceptTreeNode rootNode = tree.initializeTreeWithConceptVector("root", 0, ClassifierConstant.isBreakConcepts);
		tree.setRootNode(rootNode);
		System.out.println("process tree finished");
		Evaluation.testW2V(tree,
				corpusContentProc.getCorpusContentMap(), 
				topicDocMap, docTopicMap,
				outputClassificationFile,  outputLabelComparisonFile, 
				topK,null);


	}
	
}
