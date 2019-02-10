package edu.illinois.cs.cogcomp.classification.hierarchy.datastructure;

/**
 * yqsong@illinois.edu
 */

public class ConceptData  implements Comparable<ConceptData> {
	public String concept;
	public double score;
	
	public ConceptData(String name, double sc) {
		concept = name;
		score = sc;
	}
	
	@Override
	public int compareTo(ConceptData conceptData) {
		if (score > conceptData.score)
			return 1;
		else if ((score < conceptData.score))
			return -1;
		else
			return 0;
	}
	@Override
	public String toString()
	{
		return concept + " "+ score;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((concept == null) ? 0 : concept.hashCode());
		long temp;
		temp = Double.doubleToLongBits(score);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConceptData other = (ConceptData) obj;
		if (concept == null) {
			if (other.concept != null)
				return false;
		} else if (!concept.equals(other.concept))
			return false;
		if (Double.doubleToLongBits(score) != Double
				.doubleToLongBits(other.score))
			return false;
		return true;
	}
}