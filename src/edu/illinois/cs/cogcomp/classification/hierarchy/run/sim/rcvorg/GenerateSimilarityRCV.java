package edu.illinois.cs.cogcomp.classification.hierarchy.run.sim.rcvorg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVTopicDocMaps;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.StopWords;
import edu.illinois.cs.cogcomp.classification.hierarchy.evaluation.Evaluation;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.sim.SimilarityInTree;

public class GenerateSimilarityRCV { 
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();

	
	public static void main(String[] args) {
		int depth = 1;
		String outputClassificationFile = "data/output/rcv1org/rcv1.esa500.depth" + depth + ".withNULL";
		test20NGSimpleConcepts(depth, 100, outputClassificationFile);

	}
	
	public static void test20NGSimpleConcepts(int depth, int topK, String output) {
		
		int seed = 0;
		Random random = new Random(seed);
		double trainingRate = 0.05;

		String stopWordsFile = "";
		String docIDContentConceptFile = "";
		String docIDTopicMapFile = "";
		String treeConceptFile = "";
		String method = "simple";
		String data = "rcv";
		stopWordsFile = "data/rcvTest/english.stop";
		docIDContentConceptFile = "data/rcvTest/representation/rcvorg_train.simple.esa.concepts.500";
		docIDTopicMapFile = "data/rcvTest/rcv1-v2.topics.qrels";
		treeConceptFile = "data/rcvTest/representation/tree.rcv1.useDesc.simple.esa.concepts.500";

		StopWords.rcvStopWords = StopWords.readStopWords (stopWordsFile);
		
		RCVCorpusConceptData corpusContentProc = new RCVCorpusConceptData();
		corpusContentProc.readCorpusContentAndConcepts(docIDContentConceptFile, ClassifierConstant.isBreakConcepts, random, trainingRate, conceptWeights);

		// read topic doc maps
		RCVTopicDocMaps rcvTDM = new RCVTopicDocMaps();
		rcvTDM.readFilteredTopicDocMap (docIDTopicMapFile, corpusContentProc.getCorpusConceptVectorMap().keySet());
		
		HashMap<String, HashSet<String>> topicDocMap = rcvTDM.getTopicDocMap();
		HashMap<String, HashSet<String>> docTopicMap = rcvTDM.getDocTopicMap();
		
		// read tree
//		ConceptTreeBottomUpML tree = new ConceptTreeBottomUpML(data, method, conceptWeights, false);
		ConceptTreeTopDownML tree = new ConceptTreeTopDownML(data, method, conceptWeights, false);

		System.out.println("process tree...");
		tree.readLabelTreeFromDump(treeConceptFile, ClassifierConstant.isBreakConcepts);
		ConceptTreeNode rootNode = tree.initializeTreeWithConceptVector("root", 0, ClassifierConstant.isBreakConcepts);
		tree.setRootNode(rootNode);
		System.out.println("process tree finished");
		
//		Evaluation.testMultiLabelConceptTreeResults (tree,
//				corpusContentProc.getCorpusConceptVectorMap(), 
//				topicDocMap, docTopicMap,
//				"",  output, 
//				topK);
		
		SimilarityInTree.dumpSimilarities (tree,
				corpusContentProc.getCorpusContentMap(),
				corpusContentProc.getCorpusConceptVectorMap(), 
				topicDocMap, docTopicMap,
				output, 
				depth, topK);
		


	}

}
