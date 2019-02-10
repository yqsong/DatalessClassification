package edu.illinois.cs.cogcomp.classification.densification.representation;

import java.util.HashMap;

public class DensificationData {

	public HashMap<String, Double> vector;
	public HashMap<String, double[]> vectorOfVectors;
	
	public DensificationData () {
		vector = new HashMap<String, Double>();
		vectorOfVectors = new HashMap<String, double[]>();
	}
	
}
