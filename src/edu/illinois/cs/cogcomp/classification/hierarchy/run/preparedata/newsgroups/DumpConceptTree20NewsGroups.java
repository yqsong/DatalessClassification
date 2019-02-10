package edu.illinois.cs.cogcomp.classification.hierarchy.run.preparedata.newsgroups;

import java.util.HashMap;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.AbstractConceptTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.DiskBasedComplexESA;

/**
 * yqsong@illinois.edu
 */

public class DumpConceptTree20NewsGroups {
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();;

	public static void main (String args[]) {
		
//		ClassifierConstant.cutOff = 0.5;
//		
//		ClassifierConstant.complexVectorType = ComplexESALocal.searchTypes[1];
//		test20NewsgroupsDataESAComplex (500) ;
		
		test20NewsgroupsDataESA (500);
		
//		test20NewsgroupsDataWordDistWord2Vector (500);

	}

	public static void test20NewsgroupsDataWordDistWord2Vector (int num) {
		String fileTopicHierarchyPath = "";
		String fileOutputPath = "data/20newsgroups/output/tree.20newsgroups.word.dist.features.enwiki.vivek.newrefine." + num + ".df";
		
		AbstractConceptTree tree = new ConceptTreeTopDownML("20newsgroups", "word2vector" + num, conceptWeights, true);
		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
		tree.dumpTree(fileOutputPath);
		System.out.println("process tree finished");
	}
	
	public static void test20NewsgroupsDataESA (int conceptNum) {
		String fileTopicHierarchyPath = "";
		String fileOutputPath = "data/20newsgroups/output_new/tree.20newsgroups.simple.esa.concepts.newrefine." + conceptNum;
		
		AbstractConceptTree tree = new ConceptTreeTopDownML("20newsgroups", "simple", conceptWeights, true);
		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.setConceptNum(conceptNum);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
		tree.dumpTree(fileOutputPath);
		System.out.println("process tree finished");
	}
	
	public static void test20NewsgroupsDataESAComplex (int conceptNum) {
		String fileTopicHierarchyPath = "";
		String fileOutputPath = "data/20newsgroups/output_new/tree.20newsgroups.complexGraph.cutoff" + ClassifierConstant.cutOff + ".esa.concepts." + ClassifierConstant.complexVectorType + conceptNum;
		
		AbstractConceptTree tree = new ConceptTreeTopDownML("20newsgroups", "complex", conceptWeights, true);
		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.setConceptNum(conceptNum);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
		tree.dumpTree(fileOutputPath);
		System.out.println("process tree finished");
	}

}
