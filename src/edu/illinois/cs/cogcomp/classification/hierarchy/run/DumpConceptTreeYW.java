package edu.illinois.cs.cogcomp.classification.hierarchy.run;

import java.util.HashMap;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.AbstractConceptTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.yahoo.YahooTopicHierarchy;

public class DumpConceptTreeYW {
	
	public static void YahooDataESA (int conceptNum) {
		String fileTopicHierarchyPath = "";
		String fileOutputPath = "data/LabelSets/tree.YahooDir.complex.esa.concepts.newrefine." + conceptNum;
		HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
		AbstractConceptTree tree = new ConceptTreeTopDownML("YahooDir", "simple", conceptWeights, true);
		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.setConceptNum(conceptNum);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
		tree.dumpTreeComplexESA(fileOutputPath);
		System.out.println("process tree finished");
	}
	
	
	public static void WikiCateESA (int conceptNum) {
		String fileTopicHierarchyPath = "";
		String fileOutputPath = "data/LabelSets/tree.WikiCate.complex.esa.concepts.newrefine." + conceptNum;
		HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
		AbstractConceptTree tree = new ConceptTreeTopDownML("WikiCate", "simple", conceptWeights, true);
		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.setConceptNum(conceptNum);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
		tree.dumpTreeComplexESA(fileOutputPath);
		System.out.println("process tree finished");
	}
	
	public static void tempESA (int conceptNum) {
		String fileTopicHierarchyPath = "";
		String fileOutputPath = "data/LabelSets/tree.temp.simple.esa.concepts.newrefine." + conceptNum;
		HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
		AbstractConceptTree tree = new ConceptTreeTopDownML("Customized", "simple", conceptWeights, true);
		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.setConceptNum(conceptNum);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
		tree.dumpTreeComplexESA(fileOutputPath);
		System.out.println("process tree finished");
	}
	
	public static void Newsgroups (int conceptNum) {
		String fileTopicHierarchyPath = "";
		String fileOutputPath = "data/LabelSets/tree.20newsgroups.complex.esa.concepts.newrefine." + conceptNum;
		HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
		AbstractConceptTree tree = new ConceptTreeTopDownML("20newsgroups", "complex", conceptWeights, true);
		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.setConceptNum(conceptNum);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
		tree.dumpTreeComplexESA(fileOutputPath);
		System.out.println("process tree finished");
	}
	
	public static void main (String[] args) throws Exception {
		YahooDataESA (500);

	}

}
