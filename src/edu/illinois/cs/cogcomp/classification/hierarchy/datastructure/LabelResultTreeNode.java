package edu.illinois.cs.cogcomp.classification.hierarchy.datastructure;

import java.util.ArrayList;
import java.util.List;

/**
 * yqsong@illinois.edu
 */

public class LabelResultTreeNode {
	public LabelKeyValuePair labelKVP;
	public boolean isToLeaf;
	public int depth;
	
	public List<LabelResultTreeNode> children;
	
	public LabelResultTreeNode () {
		isToLeaf = false;
		children = new ArrayList<LabelResultTreeNode>();
	}
}
