package edu.illinois.cs.cogcomp.classification.densification.representation;

public class WikiPageKeyValuePair implements Comparable<WikiPageKeyValuePair> {
	public String wikiTitle;
	public String wikiID;
	public double score;
	
	public WikiPageKeyValuePair (String title, String id, double s) {
		wikiTitle = title;
		wikiID = id;
		score = s;
	}
	
	@Override
	public int compareTo(WikiPageKeyValuePair kvp) {
		if (this.score > kvp.score) {
			return 1;
		} else if (this.score < kvp.score) {
			return -1;
		} else {
			return 0;
		}
	}
}