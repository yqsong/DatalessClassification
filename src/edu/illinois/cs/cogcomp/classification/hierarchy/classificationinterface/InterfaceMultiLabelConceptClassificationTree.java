package edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface;

import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.densification.representation.SparseSimilarityCondensation;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;

/**
 * yqsong@illinois.edu
 */

public interface InterfaceMultiLabelConceptClassificationTree  extends InterfaceLabelTree {
	
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentML (SparseVector docContent);
	
	public int searchLabelDepth (String label);

	public boolean isLeafNode(String label);
	
	@Deprecated
	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentW2V (String docContent);

	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentDense (SparseSimilarityCondensation vectorCondensation,String docContent);

}
