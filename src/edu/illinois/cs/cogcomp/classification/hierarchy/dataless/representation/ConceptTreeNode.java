package edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.AbstractTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

/**
 * yqsong@illinois.edu
 */

public class ConceptTreeNode  extends AbstractTreeNode{
	
	private String labelDescriptionString;
	private SparseVector conceptVector;
	private Set<ConceptTreeNode> children;
	public List<ConceptData> conceptsList;
	
	public ConceptTreeNode (HashSet<ConceptTreeNode> children, String label, int depth) {
		labelString = label;
		labelDescriptionString = "";
		if (children == null) {
			this.children = new HashSet<ConceptTreeNode>();
		} else {
			this.children = children;
		}
		this.depth = depth;
		conceptVector = null;
	}
	
	public Set<ConceptTreeNode> getChildren () {
		return this.children;
	}
	
	public String getLabelDescriptioinString () {
		return this.labelDescriptionString;
	}
	
	public List<ConceptData> getconceptsList () {
		return this.conceptsList;
	}
	public void setconceptsList  (List<ConceptData> cl) {
		conceptsList = new ArrayList<ConceptData>();
		for(int i=0;i<cl.size();i++){
			conceptsList.add(cl.get(i));
		}
	
	}
	
	public void setLabelString (String str) {
		this.labelString = str;
	}
	
	public void setLabelDescriptionString (String str) {
		this.labelDescriptionString = str;
	}
	
	public SparseVector getVector () {
		if (this.conceptVector == null) {
			List<String> conceptsList = new ArrayList<String>();
			List<Double> scores = new ArrayList<Double>();
			for (int i = 0; i < this.conceptsList.size(); i++) {
				conceptsList.add(this.conceptsList.get(i).concept + "");
				scores.add(this.conceptsList.get(i).score);
			}
			setLabelConcepts (conceptsList, scores, ClassifierConstant.isBreakConcepts, new HashMap<String, Double>());
		}
			
		return this.conceptVector;
	}

	public void setLabelConcepts (List<String> concepts, List<Double> scores, boolean isBreakConcepts, HashMap<String, Double> conceptWeights) {
		conceptVector = new SparseVector(concepts, scores, isBreakConcepts, conceptWeights);
	}
}