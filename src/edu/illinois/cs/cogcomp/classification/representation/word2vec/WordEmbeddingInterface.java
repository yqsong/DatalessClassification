package edu.illinois.cs.cogcomp.classification.representation.word2vec;

import java.util.HashMap;

public interface WordEmbeddingInterface {

	public double[] getDenseVectorBasedonSegmentation(String query, boolean isDF);
	
	public HashMap<Integer, Double> getConceptVectorBasedonSegmentation(String query, boolean isDF);
	
	public HashMap<Integer, Double> getConceptVectorBasedonTFIDF(HashMap<String, Double> queryTFIDF);

	public double[] getDenseVectorSimpleAverage(String query);
	
}
