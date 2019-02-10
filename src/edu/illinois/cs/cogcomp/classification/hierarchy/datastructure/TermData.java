package edu.illinois.cs.cogcomp.classification.hierarchy.datastructure;

/**
 * yqsong@illinois.edu
 */

public class TermData  implements Comparable<TermData> {
	public int termID;
	public double tfidf;
	@Override
	public int compareTo(TermData termData) {
		if (tfidf > termData.tfidf)
			return 1;
		else if ((tfidf < termData.tfidf))
			return -1;
		else
			return 0;
	}
}