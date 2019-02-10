package edu.illinois.cs.cogcomp.classification.hierarchy.run.ml.newsgroups;

import edu.illinois.cs.cogcomp.classification.hierarchy.run.preparedata.newsgroups.CorpusESAConceptualization20NewsGroups;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.preparedata.newsgroups.DumpConceptTree20NewsGroups;
import edu.illinois.cs.cogcomp.classification.representation.esa.simple.SimpleESALocal;
import edu.illinois.cs.cogcomp.classification.representation.indexer.simple.WikipediaIndexing;

public class ProcessesOfDatalessClassification {

	public static void main (String[] args) {
		WikipediaIndexing.main(args);
		try {
			SimpleESALocal.main(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		DumpConceptTree20NewsGroups.main(args);
		CorpusESAConceptualization20NewsGroups.main(args);
		ConceptClassificationESAML.main(args);
		
	}
	
}
