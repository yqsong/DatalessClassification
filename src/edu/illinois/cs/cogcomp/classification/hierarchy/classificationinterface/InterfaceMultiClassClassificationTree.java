package edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface;

import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;

/**
 * yqsong@illinois.edu
 */

public interface InterfaceMultiClassClassificationTree extends InterfaceLabelTree {

	public LabelResultMC labelDocument (String docContent);
	public LabelResultMC labelDocument (SparseVector docContent);
	public boolean isLeafNode(String label);

}
