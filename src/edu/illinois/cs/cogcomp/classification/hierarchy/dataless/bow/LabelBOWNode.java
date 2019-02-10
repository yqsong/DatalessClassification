package edu.illinois.cs.cogcomp.classification.hierarchy.dataless.bow;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Set;

import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.AbstractTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;

/**
 * yqsong@illinois.edu
 */

public class LabelBOWNode  extends AbstractTreeNode {

	Set<LabelBOWNode> children;
	
	SparseVector labelVector;
	
	String description;
	
	LabelBOWNode (String label, String labelDescription, int depth, Set<LabelBOWNode> children, boolean isBreakConcepts, HashMap<String, Double> weights) {
		this.labelString = label;
		this.depth = depth;
		this.children = children;
		this.description = labelDescription;
		
		String [] tokens = (label + " " + labelDescription.trim()).split("\\s+");
		HashMap<String, Integer> labelHash = new HashMap<String, Integer>();
		for (int i = 0; i < tokens.length; ++i) {
			if (labelHash.containsKey(tokens[i].trim())) {
				labelHash.put(tokens[i].trim(), labelHash.get(tokens[i].trim()) + 1);
			} else {
				labelHash.put(tokens[i].trim(), 1);
			}
		}
		List<String> keyList = new ArrayList<String>(labelHash.keySet());
		List<Double> scoreList = new ArrayList<Double>();
		for (int i = 0; i < keyList.size(); ++i) {
			String key = keyList.get(i);
			double value = labelHash.get(key);
			scoreList.add(value);
		}
		labelVector = new SparseVector(keyList, scoreList, isBreakConcepts, weights);
	}

	public SparseVector getVector () {
		return labelVector;
	}
	
	public Set<LabelBOWNode> getChildren() {
		// TODO Auto-generated method stub
		return children;
	}
}
