/**
 * 
 */
package edu.illinois.cs.cogcomp.descartes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.illinois.cs.cogcomp.descartes.retrieval.IResult;
import edu.illinois.cs.cogcomp.descartes.retrieval.ISearcher;
import edu.illinois.cs.cogcomp.descartes.retrieval.SearcherFactory;

/**
 * @author Vivek Srikumar
 * 
 */
public class DatalessClassifier {

	private final ISearcher searcher;

	private final Map<String, Set<String>> classPrototypes;

	private final int numConcepts;

	public DatalessClassifier(ISearcher searcher, int numConcepts,
			List<String> classPrototypes) throws Exception {
		this.searcher = searcher;
		this.numConcepts = numConcepts;

		this.classPrototypes = new ConcurrentHashMap<String, Set<String>>();
		for (String s : classPrototypes) {
			Set<String> set = getConcepts(this.searcher, s);
			this.classPrototypes.put(s, set);
		}

	}

	private Set<String> getConcepts(ISearcher searcher, String document)
			throws Exception {

		int count = 0;
		StringBuffer sb = new StringBuffer();
		for (String s : document.split("\\s")) {
			sb.append(s + " ");
			count++;
			// if (count > 1024)
			// break;
		}

		ArrayList<IResult> search = searcher.search(document, numConcepts);

		Set<String> concepts = new HashSet<String>();
		for (IResult res : search) {
			concepts.add(res.getId());
		}
		return concepts;
	}

	private double getScore(Set<String> s1, Set<String> s2) {
		HashSet<String> s = new HashSet<String>(s1);
		s.retainAll(s2);

		return s.size();
	}

	public String getLabel(String document) throws Exception {
		Set<String> concepts = getConcepts(searcher, document);

		String label = null;
		double score = Double.NEGATIVE_INFINITY;

		for (String p : classPrototypes.keySet()) {
			double s = getScore(concepts, classPrototypes.get(p));

			if (s > score) {
				score = s;
				label = p;
			}
		}

		return label;
	}
}
