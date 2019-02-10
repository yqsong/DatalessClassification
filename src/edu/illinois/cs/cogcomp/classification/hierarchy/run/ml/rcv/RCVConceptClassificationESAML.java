package edu.illinois.cs.cogcomp.classification.hierarchy.run.ml.rcv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.mc.ConceptTreeMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeBottomUpML;
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
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

public class RCVConceptClassificationESAML {

	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();

	
	public static void main(String[] args) {
		test20NGSimpleConcepts(1);

	}
	
	public static void test20NGSimpleConcepts(int topK) {
		
		int seed = 0;
		Random random = new Random(seed);
		double trainingRate = 0.05;

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
			docIDContentConceptFile = "/shared/shelley/yqsong/data/rcv1v2/output_train/rcv_train.simple.esa.concepts.500";
			docIDTopicMapFile = "data/rcvTest/rcv1-v2.topics.qrels";
			treeConceptFile = "/shared/shelley/yqsong/data/rcv1v2/output_tree/tree.rcv1.useDesc.simple.esa.concepts.500";
			outputClassificationFile = "/shared/shelley/yqsong/data/rcv1v2/output/result.concept.rcv.classification";
			outputLabelComparisonFile = "/shared/shelley/yqsong/data/rcv1v2/output/result.concept.rcv.labelComparison";
		} else {
			stopWordsFile = "data/rcvTest/english.stop";
			docIDContentConceptFile = "D:/yqsong/data/rcvData/output_train/rcv_train.simple.esa.concepts.500";
			docIDTopicMapFile = "data/rcvTest/rcv1-v2.topics.qrels";
			treeConceptFile = "D:/yqsong/data/rcvData/output_tree/tree.rcv1.useDesc.simple.esa.concepts.500";
			outputClassificationFile = "D:/yqsong/data/rcvData/output/result.concept.rcv.classification";
			outputLabelComparisonFile = "D:/yqsong/data/rcvData/output/result.concept.rcv.labelComparison";
		}
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
		
		Evaluation.testMultiLabelConceptTreeResults (tree,
				corpusContentProc.getCorpusConceptVectorMap(), 
				topicDocMap, docTopicMap,
				outputClassificationFile,  outputLabelComparisonFile, 
				topK);
		
//		Evaluation.testMultiLabelRandomGuessResults(tree, 
//				corpusContentProc.getCorpusConceptVectorMap().keySet(),
//				topicDocMap, 
//				docTopicMap, 
//				topK); 


	}

	
}
