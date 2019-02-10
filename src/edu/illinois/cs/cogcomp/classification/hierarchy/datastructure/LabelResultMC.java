package edu.illinois.cs.cogcomp.classification.hierarchy.datastructure;

import java.util.ArrayList;
import java.util.List;

/**
 * yqsong@illinois.edu
 */

public class LabelResultMC {
	public List<LabelKeyValuePair> labels;
	public boolean isToLeaf;
	
	public LabelResultMC () {
		labels = new ArrayList<LabelKeyValuePair>();
		isToLeaf = false;
	}
}

