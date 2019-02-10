package edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface;

import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;

/**
 * yqsong@illinois.edu
 */

public interface InterfaceMultiLabelContentClassificationTree  extends InterfaceLabelTree {

	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentContentML (String docContent);
	
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentConceptML (String docConcepts);

	public boolean isLeafNode(String label);


}
