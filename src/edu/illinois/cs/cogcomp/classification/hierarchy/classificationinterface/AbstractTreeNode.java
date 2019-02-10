package edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface;


/**
 * yqsong@illinois.edu
 */

public class AbstractTreeNode {
	protected String labelString;
	protected int depth;
	
	public int getDepth () {
		return depth;
	}
	
	public String getLabelString () {
		return this.labelString;
	}

	
}
