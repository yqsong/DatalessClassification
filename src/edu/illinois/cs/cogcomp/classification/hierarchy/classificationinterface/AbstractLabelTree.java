package edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts.AbstractTreeLabelData;

/**
 * yqsong@illinois.edu
 */

abstract public class AbstractLabelTree implements InterfaceLabelTree, Serializable {

	private static final long serialVersionUID = -3793432002431129734L;
	
	public AbstractTreeLabelData treeLabelData;
	
	public HashMap<String, String> getTreeLabelNameHashMap () {
		return treeLabelData.getTreeLabelNameHashMap();
	}
	
	public HashMap<String, HashSet<String>> getTreeindex () {
		return treeLabelData.getTreeChildrenIndex();
	}
	
	public boolean isLeafNode (String label) {
		if (this.treeLabelData.getTreeChildrenIndex().containsKey(label)) {
			if (this.treeLabelData.getTreeChildrenIndex().get(label).size() > 0) {
				return false;
			} else {
				return true;
			}
		}
		return true;
	}
	
	public Set<String> getLabelSiblings (String label) {
		String parent = treeLabelData.getTreeParentIndex().get(label);
		Set<String> children = treeLabelData.getTreeChildrenIndex().get(parent);
		return children;
	}
	
	public List<String> getAllParents (String label) {
		String parent = treeLabelData.getTreeParentIndex().get(label);
		List<String> parentList = new ArrayList<String>();
		while (parent != null) {
			if (parent.equals("none") == false && parent.equals("null") == false) {
				parentList.add(parent);
			}
			parent = treeLabelData.getTreeParentIndex().get(parent);
		}

		return parentList;
	}
	
	public AbstractTreeLabelData getTreeLabledData () {
		return this.treeLabelData;
	}
	
	public Set<String> getLabelSameLevel (String label) {
		int depth = getLabelDepth (label);
		String root = "root";
		if (treeLabelData.getTreeChildrenIndex().containsKey(root) == false) {
			root = "top";
		}
		try {
			if (treeLabelData.getTreeChildrenIndex().containsKey(root) == false) {
				throw new Exception ();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		Set<String> allLabelSet = new HashSet<String>();
		getAllChildren(root, depth, allLabelSet);
		return allLabelSet;
	}
	
	private void getAllChildren (String root, int maxDepth, Set<String> allLabelSet) {
		int depth = getLabelDepth(root);
		if (depth == maxDepth) {
			Set<String> siblingSet = getLabelSiblings (root);
			allLabelSet.addAll(siblingSet);
			return;
		} else {
			Set<String> children = treeLabelData.getTreeChildrenIndex().get(root);
			if (children == null || children.size() == 0) {
				return;
			} else {
				for (String child : children) {
					getAllChildren (child, maxDepth, allLabelSet);
				}
			}
		}
	}


	
}
