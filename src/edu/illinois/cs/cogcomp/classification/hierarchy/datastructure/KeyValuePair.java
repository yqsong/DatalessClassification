package edu.illinois.cs.cogcomp.classification.hierarchy.datastructure;

/**
 * yqsong@illinois.edu
 */

public class KeyValuePair implements Comparable<KeyValuePair> {

	public KeyValuePair (int key, double value)  {
		this.key = key;
		this.value = value;
	}
	public int key ;
	public double value;
	@Override
	public int compareTo(KeyValuePair kvp) {
		if (this.value > kvp.value) {
			return 1;
		} else if (this.value < kvp.value) {
			return -1;
		} else {
			return 0;
		}
	}
}
