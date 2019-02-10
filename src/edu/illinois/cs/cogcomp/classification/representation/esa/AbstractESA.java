package edu.illinois.cs.cogcomp.classification.representation.esa;

import java.util.List;

import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;

/**
 * yqsong@illinois.edu
 */

abstract public class AbstractESA {

	abstract public List<ConceptData> retrieveConcepts(String document, int numConcepts,
			String complexVectorType) throws Exception;

	abstract public List<ConceptData> retrieveConcepts(String document, int numConcepts) throws Exception;

}
