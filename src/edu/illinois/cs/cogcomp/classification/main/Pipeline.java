package edu.illinois.cs.cogcomp.classification.main;

import java.util.HashMap;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.AbstractConceptTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

/**
 * Shaoshi Ling
 * sling3@illinois.edu
 */

public class Pipeline {
	
	public static void main(String[] args) throws Exception{
		DatalessResourcesConfig config = new DatalessResourcesConfig();
		DatalessResourcesConfig.dataset = DatalessResourcesConfig.CONST_DATA_CUSTOMIZED;
		ClassificationESA classification=new ClassificationESA();
		dumptreeComplexESA(500);
//		classification.GivenText(config.dataset);
		classification.MultiText(DatalessResourcesConfig.dataset, DatalessResourcesConfig.DatafilePath, DatalessResourcesConfig.LabelResult);
	}
	
	public static void dumptreeComplexESA(int conceptNum){
		
		String fileTopicHierarchyPath = "";
		String fileTopicDescriptionPath = "";
		String fileOutputPath = "data/LabelSets/tree.Customized.complex.esa.concepts.newrefine." + conceptNum;
		HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
		AbstractConceptTree tree = new ConceptTreeTopDownML(DatalessResourcesConfig.dataset, "complex", conceptWeights, true);
		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		tree.treeLabelData.readTopicDescription(fileTopicDescriptionPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		tree.setConceptNum(conceptNum);
		tree.conceptualizeTreeLabels(rootNode, ClassifierConstant.isBreakConcepts);
		tree.dumpTreeComplexESA(fileOutputPath);
		System.out.println("process tree finished");
	}
	
}
