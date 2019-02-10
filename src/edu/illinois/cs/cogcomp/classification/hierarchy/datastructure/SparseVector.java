package edu.illinois.cs.cogcomp.classification.hierarchy.datastructure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * yqsong@illinois.edu
 */

public class SparseVector {

	public HashMap<String, Double> keyValueMap;
	public double norm;
	
	public SparseVector (List<String> keys, List<Double> scores, boolean isBreakConcepts, HashMap<String, Double> conceptWeights) {
		if (conceptWeights == null) {
			conceptWeights = new HashMap<String, Double>();
		}
		keyValueMap = new HashMap<String, Double>();
		if (isBreakConcepts == false) {
			for (int i = 0; i < keys.size(); ++i) {
				keyValueMap.put(keys.get(i),scores.get(i));
				if (conceptWeights.containsKey(keys.get(i)) == false) {
					conceptWeights.put(keys.get(i), 1.0);
				}
			}
			
		} else {
			for (int i = 0; i < keys.size(); ++i) {
				String key = keys.get(i);
				String[] tokens = key.split(" ");
				for (int j = 0; j < tokens.length; ++j) {
					String token = tokens[j].trim().toLowerCase();
					if (StopWords.rcvStopWords != null) {
						if (StopWords.rcvStopWords.contains(token) == false) {
							if (keyValueMap.containsKey(token) == false) {
								keyValueMap.put(token,scores.get(i));
							} else {
								keyValueMap.put(token,keyValueMap.get(token) + scores.get(i));
							}
							
							if (conceptWeights.containsKey(token) == false) {
								conceptWeights.put(token, 1.0);
							}
						}
					} else {
						if (keyValueMap.containsKey(token) == false) {
							keyValueMap.put(token,scores.get(i));
						} else {
							keyValueMap.put(token,keyValueMap.get(token) + scores.get(i));
						}
						
						if (conceptWeights.containsKey(token) == false) {
							conceptWeights.put(token, 1.0);
						}
					}
				}
			}
		}
		
		norm = 0;
		for (String key : keyValueMap.keySet()) {
			double value = keyValueMap.get(key);
			double value1 = 1;
			if (conceptWeights.containsKey(key) == true)
				value1 = conceptWeights.get(key);
			norm += value * value * value1;
		}
		norm = Math.sqrt(norm);
	}
	

	
	@SuppressWarnings("unchecked")
	public SparseVector(SparseVector v) {
		keyValueMap = (HashMap<String, Double>) v.keyValueMap.clone();
		norm = v.norm;
	}



	public String toString () {
		String str = "";
		for (String key : keyValueMap.keySet()) {
			str += key + "," + keyValueMap.get(key) + ";";
		}
		return str;
	}
	
	public HashMap<String, Double> getData () {
		return this.keyValueMap;
	}
	
	public Set<String> getKeys () {
		return this.keyValueMap.keySet();
	}
	
	public double getNorm () {
		return this.norm;
	}
	
	public void updateNorm (HashMap<String, Double> conceptWeights) {
		norm = 0;
		for (String key : keyValueMap.keySet()) {
			double value = keyValueMap.get(key);
			double value1 = 1;
			if (conceptWeights.containsKey(key) == true)
				value1 = conceptWeights.get(key);
			norm += value * value * value1;
		}
		norm = Math.sqrt(norm);
	}
	
	public double jaccard (SparseVector v2) {
		Set<String> set1 = new HashSet<String>(this.keyValueMap.keySet());
		Set<String> set2 = v2.getKeys();
		set1.retainAll(set2);
		int overlap = set1.size();
		return ((double) overlap) / (set1.size() + set2.size());
	}
	
	public SparseVector add(SparseVector v2, HashMap<String, Double> conceptWeights) {
		SparseVector v3 = new SparseVector(v2);
		for (String key : keyValueMap.keySet()) {
			if (v3.keyValueMap.containsKey(key) == true) {
				v3.keyValueMap.put(key, v3.keyValueMap.get(key) + keyValueMap.get(key));
			}
		}
		v3.updateNorm(conceptWeights);
		return v3;
	}
	
	public double cosine (SparseVector v2, HashMap<String, Double> conceptWeights) {
//		double norm1 = 0;
//		double norm2 = 0;
		double dot = 0;
		if (this.keyValueMap.size() < v2.keyValueMap.size()) {
			for (String key : this.keyValueMap.keySet()) {
				if (v2.keyValueMap.containsKey(key) == true) {
					double value1 = this.keyValueMap.get(key);
					double value2 = v2.keyValueMap.get(key);
					double value3 = 1;
					if (conceptWeights.containsKey(key));
						value3 = conceptWeights.get(key);
					dot += value1 * value2 * value3;
				}
			}
		} else {
			for (String key : v2.keyValueMap.keySet()) {
				if (this.keyValueMap.containsKey(key) == true) {
					double value1 = this.keyValueMap.get(key);
					double value2 = v2.keyValueMap.get(key);
					double value3 = 1;
					if (conceptWeights.containsKey(key))
						value3 = conceptWeights.get(key);
					dot += value1 * value2 * value3;
				}
			}
		}
		
		return dot / (Double.MIN_VALUE + this.norm) / (Double.MIN_VALUE + v2.getNorm());
	}
	
}
