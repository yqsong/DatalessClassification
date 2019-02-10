package edu.illinois.cs.cogcomp.classification.hierarchy.run.preparedata.rcv;

import java.util.HashMap;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.AbstractConceptTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

public class DumpConceptTreeRCV {
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();;

	public static void main (String args[]) {
		
//		rcvTreeDataESA (50);
//		rcvTreeDataESA (100);
//		rcvTreeDataESA (200);
//		rcvTreeDataESA (500);
//		rcvTreeDataESA (1000);
//		
//		rcvTreeDataWordDistWord2Vector (50);
//		rcvTreeDataWordDistWord2Vector (100);
//		rcvTreeDataWordDistWord2Vector (200);
//		rcvTreeDataWordDistWord2Vector (500);
//		rcvTreeDataWordDistWord2Vector (1000);
//
//		
		rcvTreeDataWordBrownCluster(50, "Wiki");
		rcvTreeDataWordBrownClusterOld(50, "Wiki");
		rcvTreeDataWordBrownCluster(100, "Wiki");
		rcvTreeDataWordBrownClusterOld(100, "Wiki");
		rcvTreeDataWordBrownCluster(200, "Wiki");
		rcvTreeDataWordBrownClusterOld(200, "Wiki");
		rcvTreeDataWordBrownCluster(500, "Wiki");
		rcvTreeDataWordBrownClusterOld(500, "Wiki");
//
//	
//		rcvTreeDataWordBrownCluster(50, "20NG");
//		rcvTreeDataWordBrownClusterOld(50, "20NG");
//		
//		rcvTreeDataWordBrownCluster(100, "20NG");
//		rcvTreeDataWordBrownClusterOld(100, "20NG");
//
//		rcvTreeDataWordBrownCluster(200, "20NG");
//		rcvTreeDataWordBrownClusterOld(200, "20NG");
//
//		rcvTreeDataWordBrownCluster(500, "20NG");
//		rcvTreeDataWordBrownClusterOld(500, "20NG");
//
//		rcvTreeDataWordBrownCluster(1000, "20NG");
//		rcvTreeDataWordBrownClusterOld(1000, "20NG");
//
//		
//		
//		rcvTreeDataWordBrownCluster(100, "ratinov");
//		rcvTreeDataWordBrownClusterOld(100, "ratinov");
//		
//		rcvTreeDataWordBrownCluster(320, "ratinov");
//		rcvTreeDataWordBrownClusterOld(320, "ratinov");
//		
//		rcvTreeDataWordBrownCluster(1000, "ratinov");
//		rcvTreeDataWordBrownClusterOld(1000, "ratinov");
//		
//		rcvTreeDataWordBrownCluster(3200, "ratinov");
//		rcvTreeDataWordBrownClusterOld(3200, "ratinov");
//	
//		
//		rcvTreeDataWordDistRatinovEmedding(25);
//		rcvTreeDataWordDistRatinovEmedding(50);
//		rcvTreeDataWordDistRatinovEmedding(100);
//		rcvTreeDataWordDistRatinovEmedding(200);

		rcvTreeDataWordDistSenna();
	}
	

	public static void rcvTreeDataWordBrownCluster (int num, String data) {
		String fileTopicHierarchyPath = "data/rcvTest/rcv1.topics.hier.orig";
		String fileTopicDescriptionPath = "data/rcvTest/topics.rbb";
		AbstractConceptTree tree = null;

		String fileOutputPath = "";
		if (data.contains("20NG")) {
			tree = new ConceptTreeTopDownML("rcv", "browncluster" + num + "_20NG", conceptWeights, true);
			if (ClassifierConstant.isServer == false) {
				fileOutputPath = "D:/yqsong/data/20newsgroups/output_tree/tree.rcv1.useDesc.word.dist.features.browncluster.20NG." + num + ".df";
			} else {
				fileOutputPath = "/shared/shelley/yqsong/benchmark/rcv1v2/output_tree/tree.rcv1.useDesc.word.dist.features.browncluster.20NG." + num + ".df";
			}
		} else if (data.contains("ratinov")) {
			tree = new ConceptTreeTopDownML("rcv", "browncluster" + num + "_Ratinov", conceptWeights, true);
			if (ClassifierConstant.isServer == false) {
				fileOutputPath = "D:/yqsong/data/20newsgroups/output_tree/tree.rcv1.useDesc.word.dist.features.browncluster.ratinov." + num + ".df";
			} else {
				fileOutputPath = "/shared/shelley/yqsong/benchmark/rcv1v2/output_tree/tree.rcv1.useDesc.word.dist.features.browncluster.ratinov." + num + ".df";
			}
		} else {
			tree = new ConceptTreeTopDownML("rcv", "browncluster" + num + "", conceptWeights, true);
			if (ClassifierConstant.isServer == false) {
				fileOutputPath = "D:/yqsong/data/20newsgroups/output_tree/tree.rcv1.useDesc.word.dist.features.browncluster.enwiki." + num + ".df";
			} else {
				fileOutputPath = "/shared/shelley/yqsong/benchmark/rcv1v2/output_tree/tree.rcv1.useDesc.word.dist.features.browncluster.enwiki." + num + ".df";
			}
		}

		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		tree.treeLabelData.readTopicDescription(fileTopicDescriptionPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
		
		tree.dumpTree(fileOutputPath);
		System.out.println("process tree finished");
		
//		String fileTopicHierarchyPath = "";
//		String fileOutputPath = "";
//		AbstractConceptTree tree = null;
//		if (data.contains("20NG")) {
//			tree = new ConceptTreeTopDownML("20newsgroups", "browncluster" + num + "_20NG", conceptWeights, true);
//			if (ClassifierConstant.isServer == false) {
//				fileOutputPath = "D:/yqsong/data/20newsgroups/output/tree.20newsgroups.word.dist.features.20NG.browncluster" + num + ".df";
//			} else {
//				fileOutputPath = "/shared/saruman/yqsong/data/benchmark/20newsgroups/output/tree.20newsgroups.word.dist.features.20NG.browncluster" + num + ".df";
//			}
//		} else if (data.contains("ratinov")) {
//			tree = new ConceptTreeTopDownML("20newsgroups", "browncluster" + num + "_Ratinov", conceptWeights, true);
//			if (ClassifierConstant.isServer == false) {
//				fileOutputPath = "D:/yqsong/data/20newsgroups/output/tree.20newsgroups.word.dist.features.ratinov.browncluster" + num + ".df";
//			} else {
//				fileOutputPath = "/shared/saruman/yqsong/data/benchmark/20newsgroups/output/tree.20newsgroups.word.dist.features.ratinov.browncluster" + num + ".df";
//			}
//		} else {
//			tree = new ConceptTreeTopDownML("20newsgroups", "browncluster" + num + "", conceptWeights, true);
//			if (ClassifierConstant.isServer == false) {
//				fileOutputPath = "D:/yqsong/data/20newsgroups/output/tree.20newsgroups.word.dist.features.enwiki.browncluster" + num + ".df";
//			} else {
//				fileOutputPath = "/shared/saruman/yqsong/data/benchmark/20newsgroups/output/tree.20newsgroups.word.dist.features.enwiki.browncluster" + num + ".df";
//			}
//		}
//		System.out.println("process tree...");
//		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
//		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
//		tree.setRootNode(rootNode);
//		tree.aggregateChildrenDescription(rootNode);
//		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
//		tree.dumpTree(fileOutputPath);
//		System.out.println("process tree finished");
	}
	
	public static void rcvTreeDataWordBrownClusterOld (int num, String data) {
		String fileTopicHierarchyPath = "data/rcvTest/rcv1.topics.hier.orig";
		String fileTopicDescriptionPath = "data/rcvTest/topics.rbb";
		AbstractConceptTree tree = null;

		String fileOutputPath = "";
		if (data.contains("20NG")) {
			tree = new ConceptTreeTopDownML("rcv", "browncluster" + num + "_old_20NG", conceptWeights, true);
			if (ClassifierConstant.isServer == false) {
				fileOutputPath = "D:/yqsong/data/20newsgroups/output_tree/tree.rcv1.useDesc.word.dist.features.browncluster.20NG.old" + num + ".df";
			} else {
				fileOutputPath = "/shared/shelley/yqsong/benchmark/rcv1v2/output_tree/tree.rcv1.useDesc.word.dist.features.browncluster.20NG.old" + num + ".df";
			}
		} else if (data.contains("ratinov")) {
			tree = new ConceptTreeTopDownML("rcv", "browncluster" + num + "_old_Ratinov", conceptWeights, true);
			if (ClassifierConstant.isServer == false) {
				fileOutputPath = "D:/yqsong/data/20newsgroups/output_tree/tree.rcv1.useDesc.word.dist.features.browncluster.ratinov.old" + num + ".df";
			} else {
				fileOutputPath = "/shared/shelley/yqsong/benchmark/rcv1v2/output_tree/tree.rcv1.useDesc.word.dist.features.browncluster.ratinov.old" + num + ".df";
			}
		} else {
			tree = new ConceptTreeTopDownML("rcv", "browncluster" + num + "_old", conceptWeights, true);
			if (ClassifierConstant.isServer == false) {
				fileOutputPath = "D:/yqsong/data/20newsgroups/output_tree/tree.rcv1.useDesc.word.dist.features.browncluster.enwiki.old" + num + ".df";
			} else {
				fileOutputPath = "/shared/shelley/yqsong/benchmark/rcv1v2/output_tree/tree.rcv1.useDesc.word.dist.features.browncluster.enwiki.browncluster.old" + num + ".df";
			}
		}

		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		tree.treeLabelData.readTopicDescription(fileTopicDescriptionPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
		
		tree.dumpTree(fileOutputPath);
		System.out.println("process tree finished");
		
		
//		String fileTopicHierarchyPath = "";
//		String fileOutputPath = "";
//		AbstractConceptTree tree = null;
//		if (data.contains("20NG")) {
//			tree = new ConceptTreeTopDownML("20newsgroups", "browncluster" + num + "_old_20NG", conceptWeights, true);
//			if (ClassifierConstant.isServer == false) {
//				fileOutputPath = "D:/yqsong/data/20newsgroups/output/tree.20newsgroups.word.dist.features.20NG.browncluster.old" + num + ".df";
//			} else {
//				fileOutputPath = "/shared/saruman/yqsong/data/benchmark/20newsgroups/output/tree.20newsgroups.word.dist.features.20NG.browncluster.old" + num + ".df";
//			}
//		} else if (data.contains("ratinov")) {
//			tree = new ConceptTreeTopDownML("20newsgroups", "browncluster" + num + "_old_Ratinov", conceptWeights, true);
//			if (ClassifierConstant.isServer == false) {
//				fileOutputPath = "D:/yqsong/data/20newsgroups/output/tree.20newsgroups.word.dist.features.ratinov.browncluster.old" + num + ".df";
//			} else {
//				fileOutputPath = "/shared/saruman/yqsong/data/benchmark/20newsgroups/output/tree.20newsgroups.word.dist.features.ratinov.browncluster.old" + num + ".df";
//			}
//		} else {
//			tree = new ConceptTreeTopDownML("20newsgroups", "browncluster" + num + "_old", conceptWeights, true);
//			if (ClassifierConstant.isServer == false) {
//				fileOutputPath = "D:/yqsong/data/20newsgroups/output/tree.20newsgroups.word.dist.features.enwiki.browncluster.old" + num + ".df";
//			} else {
//				fileOutputPath = "/shared/saruman/yqsong/data/benchmark/20newsgroups/output/tree.20newsgroups.word.dist.features.enwiki.browncluster.old" + num + ".df";
//			}
//		}
//
//		System.out.println("process tree...");
//		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
//		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
//		tree.setRootNode(rootNode);
//		tree.aggregateChildrenDescription(rootNode);
//		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
//		tree.dumpTree(fileOutputPath);
//		System.out.println("process tree finished");
	}
	
	public static void rcvTreeDataWordDistRatinovEmedding (int num) {
		String fileTopicHierarchyPath = "data/rcvTest/rcv1.topics.hier.orig";
		String fileTopicDescriptionPath = "data/rcvTest/topics.rbb";
		AbstractConceptTree tree = new ConceptTreeTopDownML("rcv", "ratinovEmbedding" + num, conceptWeights, true);
		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		tree.treeLabelData.readTopicDescription(fileTopicDescriptionPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);

		String fileOutputPath = "/shared/shelley/yqsong/benchmark/rcv1v2/output_tree/tree.rcv1.useDesc.word.dist.features.ratinov.embedding" + num + ".df";
		if (ClassifierConstant.isServer == false) {
			 fileOutputPath = "D:/yqsong/data/20newsgroups/output_tree/tree.rcv1.useDesc.word.dist.features.ratinov.embedding" + num + ".df";
		}

		tree.dumpTree(fileOutputPath);
		System.out.println("process tree finished");

//		String fileTopicHierarchyPath = "";
//		String fileOutputPath = "/shared/saruman/yqsong/data/benchmark/20newsgroups/output/tree.20newsgroups.word.dist.features.ratinov.embedding" + num + ".df";
//		if (ClassifierConstant.isServer == false) {
//			fileOutputPath = "D:/yqsong/data/20newsgroups/output/tree.20newsgroups.word.dist.features.ratinov.embedding" + num + ".df";
//		}
//		
//		AbstractConceptTree tree = new ConceptTreeTopDownML("20newsgroups", "ratinovEmbedding" + num, conceptWeights, true);
//		System.out.println("process tree...");
//		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
//		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
//		tree.setRootNode(rootNode);
//		tree.aggregateChildrenDescription(rootNode);
//		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
//		tree.dumpTree(fileOutputPath);
//		System.out.println("process tree finished");
	}
	
	public static void rcvTreeDataWordDistWord2Vector (int num) {
		String fileTopicHierarchyPath = "data/rcvTest/rcv1.topics.hier.orig";
		String fileTopicDescriptionPath = "data/rcvTest/topics.rbb";
		AbstractConceptTree tree = new ConceptTreeTopDownML("rcv", "word2vector" + num, conceptWeights, true);
		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		tree.treeLabelData.readTopicDescription(fileTopicDescriptionPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);

		String fileOutputPath = "/shared/shelley/yqsong/benchmark/rcv1v2/output_tree/tree.rcv1.useDesc.word.dist.features.enwiki.vivek" + num + ".df";
		if (ClassifierConstant.isServer == false) {
			 fileOutputPath = "D:/yqsong/data/20newsgroups/output_tree/tree.rcv1.useDesc.word.dist.features.enwiki.vivek" + num + ".df";
		}

		tree.dumpTree(fileOutputPath);
		System.out.println("process tree finished");
		
//		String fileTopicHierarchyPath = "";
//		String fileOutputPath = "/shared/saruman/yqsong/data/benchmark/20newsgroups/output/tree.20newsgroups.word.dist.features.enwiki.vivek" + num + ".df";
//		if (ClassifierConstant.isServer == false) {
//			fileOutputPath = "D:/yqsong/data/20newsgroups/output/tree.20newsgroups.word.dist.features.enwiki.vivek" + num + ".df";
//		}
//		
//		AbstractConceptTree tree = new ConceptTreeTopDownML("20newsgroups", "word2vector" + num, conceptWeights, true);
//		System.out.println("process tree...");
//		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
//		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
//		tree.setRootNode(rootNode);
//		tree.aggregateChildrenDescription(rootNode);
//		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
//		tree.dumpTree(fileOutputPath);
//		System.out.println("process tree finished");
	}
	
	public static void rcvTreeDataWordDistSenna () {
		String fileTopicHierarchyPath = "data/rcvTest/rcv1.topics.hier.orig";
		String fileTopicDescriptionPath = "data/rcvTest/topics.rbb";
		AbstractConceptTree tree = new ConceptTreeTopDownML("rcv", "senna", conceptWeights, true);
		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		tree.treeLabelData.readTopicDescription(fileTopicDescriptionPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);

		String fileOutputPath = "/shared/shelley/yqsong/benchmark/rcv1v2/output_tree/tree.rcv1.useDesc.word.dist.features.senna";
		if (ClassifierConstant.isServer == false) {
			 fileOutputPath = "D:/yqsong/data/20newsgroups/output_tree/tree.rcv1.useDesc.word.dist.features.senna";
		}

		tree.dumpTree(fileOutputPath);
		System.out.println("process tree finished");
//		
//		
//		String fileTopicHierarchyPath = "";
//		String fileOutputPath = "/shared/saruman/yqsong/data/benchmark/20newsgroups/output/tree.20newsgroups.word.dist.features.senna";
//		AbstractConceptTree tree = new ConceptTreeTopDownML("20newsgroups", "senna", conceptWeights, true);
//		if (ClassifierConstant.isServer == false) {
//			fileOutputPath = "D:/yqsong/data/20newsgroups/output/tree.20newsgroups.word.dist.features.senna.df";
//		}
//		System.out.println("process tree...");
//		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
//		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
//		tree.setRootNode(rootNode);
//		tree.aggregateChildrenDescription(rootNode);
//		tree.conceptualizeTreeLabels(rootNode, false);
//		tree.dumpTree(fileOutputPath);
//		System.out.println("process tree finished");
	}
	
	public static void rcvTreeDataESA (int conceptNum) {
		String fileTopicHierarchyPath = "data/rcvTest/rcv1.topics.hier.orig";
		String fileTopicDescriptionPath = "data/rcvTest/topics.rbb";
		AbstractConceptTree tree = new ConceptTreeTopDownML("rcv", "simple", conceptWeights, true);
		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		tree.treeLabelData.readTopicDescription(fileTopicDescriptionPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.setConceptNum(conceptNum);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);

		String fileOutputPath = "/shared/shelley/yqsong/benchmark/rcv1v2/output_tree/tree.rcv1.useDesc.simple.esa.concepts." + conceptNum;
		if (ClassifierConstant.isServer == false) {
			 fileOutputPath = "D:/yqsong/data/20newsgroups/output_tree/tree.rcv1.useDesc.simple.esa.concepts." + conceptNum;
		}

		tree.dumpTree(fileOutputPath);
		System.out.println("process tree finished");
	}
	
	
	
//	public static void testRCVData () {
//		String fileTopicHierarchyPath = "data/rcvTest/rcv1.topics.hier.orig";
//		String fileTopicDescriptionPath = "data/rcvTest/topics.rbb";
//		String fileOutputPath = "data/rcvTest/rcv1.topics.hier.orig.dump.useDesc.complex.esa.concepts";
//		AbstractConceptTree tree = new ConceptTreeTopDownML("rcv", "complex", conceptWeights, true);
//		System.out.println("process tree...");
//		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
//		tree.treeLabelData.readTopicDescription(fileTopicDescriptionPath);
//		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
//		tree.setRootNode(rootNode);
//		tree.aggregateChildrenDescription(rootNode);
//		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
//		tree.dumpTree(fileOutputPath);
//		System.out.println("process tree finished");
//	}
}
