package edu.illinois.cs.cogcomp.classification.hierarchy.datastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * yqsong@illinois.edu
 */

public class LabelResultML {
	public LabelResultTreeNode rootLabel;
	
	public LabelResultML () {
		rootLabel = new LabelResultTreeNode();
	}
	
	HashMap<Integer, List<LabelKeyValuePair>> depthLabelMap = null;
	
	public HashMap<Integer, List<LabelKeyValuePair>> processLabels () {
		processLabels (rootLabel, 1);
		for (Integer depth : depthLabelMap.keySet()) {
			Collections.sort(depthLabelMap.get(depth));
			Collections.reverse(depthLabelMap.get(depth));
		}
		return depthLabelMap;
	}
	
	private void processLabels (LabelResultTreeNode node, double parentScore) {
		if (depthLabelMap == null) {
			depthLabelMap = new HashMap<Integer, List<LabelKeyValuePair>>();
		}
		if (depthLabelMap.containsKey(node.depth) == false) {
			depthLabelMap.put(node.depth, new ArrayList<LabelKeyValuePair>());
		} 
		LabelKeyValuePair labelKVP = node.labelKVP;
		labelKVP.setScore(labelKVP.getScore() * parentScore);
		depthLabelMap.get(node.depth).add(labelKVP);
		
		for (int i = 0; i < node.children.size(); ++i) {
			processLabels (node.children.get(i),  1);//labelKVP.getScore());//
		}
	}
}

