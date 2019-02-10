package edu.illinois.cs.cogcomp.classification.MVC;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.AbstractConceptTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

/**
 * Shaoshi Ling
 * sling3@illinois.edu
 */

public class ReadTree {
	public static void UserDataESA (int conceptNum) {
		String fileTopicHierarchyPath = "";
		String fileOutputPath = "data/20newsgroups/output_new/tree.Customized.simple.esa.concepts.newrefine." + conceptNum;
		HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
		AbstractConceptTree tree = new ConceptTreeTopDownML("Customized", "simple", conceptWeights, true);
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
