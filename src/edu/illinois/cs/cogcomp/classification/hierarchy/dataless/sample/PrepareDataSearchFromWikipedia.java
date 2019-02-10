package edu.illinois.cs.cogcomp.classification.hierarchy.dataless.sample;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.AbstractConceptTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.classification.representation.esa.simple.SimpleESALocal;

/**
 * yqsong@illinois.edu
 */

public class PrepareDataSearchFromWikipedia {
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();;
	
	public static void main (String[] args) {
	}
	
	public static void process20NewsgroupsDataByWikipedia (int topK) {
		String fileTopicHierarchyPath = "";
		String fileOutputPath = "data/20newsgroups/output/20newsgroups.wikipedia.search.top" + topK + ".data";
		AbstractConceptTree tree = new ConceptTreeTopDownML("20newsgroups", null, conceptWeights, false);
		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		
		process20NewsgroupsDataByWikipedia(tree, topK, fileOutputPath);
	}
	
	public static void process20NewsgroupsDataByWikipedia (AbstractConceptTree tree, int retrievedNum, String outputFile) {
		SimpleESALocal esa = new SimpleESALocal();
		FileWriter writer = null;
		try {
			writer = new FileWriter(outputFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<ConceptTreeNode> treeNodeList = tree.getTreeNodeList();
		
		for (int i = 0; i < treeNodeList.size(); ++i) {
			ConceptTreeNode node = treeNodeList.get(i);
			String label = node.getLabelString();
			String description = node.getLabelDescriptioinString();
			
			System.out.println ("Search label: " + label + " with description: " + description);
			try {
				List<String> docList = esa.getDocuments(retrievedNum, description);
				
				for (int j = 0; j < docList.size(); ++j) {
//					writer.write(label + "_" + i + "_" + j + "\t" + label + "\t" + text); 
					writer.write(label + "_" + i + "_" + j + "\t" + label + "\t" + docList.get(j)); 
					writer.write(System.getProperty("line.separator").toString()); 
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		try {
			writer.flush();
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
