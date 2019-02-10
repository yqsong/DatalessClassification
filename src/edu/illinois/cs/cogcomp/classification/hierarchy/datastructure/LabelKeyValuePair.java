package edu.illinois.cs.cogcomp.classification.hierarchy.datastructure;

/**
 * yqsong@illinois.edu
 */

public class LabelKeyValuePair implements Comparable<LabelKeyValuePair> {
	public String labelName;
	double labelScore;
	
	public LabelKeyValuePair (String name, double score) {
		labelName = name;
		labelScore = score;
	}
	
	public String getLabel(){
		return labelName;
	}
	
	public double getScore() {
		return labelScore;
	}
	
	public void setLabel (String label) {
		labelName = label;
	}
	
	public void setScore (double score) {
		labelScore = score;
	}
	
	@Override
	public int compareTo(LabelKeyValuePair kvp) {
		if (this.labelScore > kvp.labelScore) {
			return 1;
		} else if (this.labelScore < kvp.labelScore) {
			return -1;
		} else {
			return 0;
		}
	}
}