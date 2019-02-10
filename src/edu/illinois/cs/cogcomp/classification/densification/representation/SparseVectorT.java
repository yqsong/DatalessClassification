package edu.illinois.cs.cogcomp.classification.densification.representation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;

public class SparseVectorT<T> {
	HashMap<T, Double> keyValueMap;
	double norm;
	
	public SparseVectorT () {
		keyValueMap = new HashMap<T, Double>();
		norm = 0;
	}
	
	public SparseVectorT (List<T> keys, List<Double> scores) {
		keyValueMap = new HashMap<T, Double>();
		for (int i = 0; i < keys.size(); ++i) {
			keyValueMap.put(keys.get(i),scores.get(i));
		}
		
		norm = 0;
		for (T key : keyValueMap.keySet()) {
			double value = keyValueMap.get(key);
			double value1 = 1;
			norm += value * value * value1;
		}
		norm = Math.sqrt(norm);
	}
	
	public void setVector (List<T> keys, List<Double> scores) {
		keyValueMap = new HashMap<T, Double>();
		for (int i = 0; i < keys.size(); ++i) {
			keyValueMap.put(keys.get(i),scores.get(i));
		}
		
		norm = 0;
		for (T key : keyValueMap.keySet()) {
			double value = keyValueMap.get(key);
			double value1 = 1;
			norm += value * value * value1;
		}
		norm = Math.sqrt(norm);
	}
	
	public SparseVectorT(SparseVectorT<T> v) {
		keyValueMap = (HashMap<T, Double>) v.keyValueMap.clone();
		norm = v.norm;
	}



	public String toString () {
		String str = "";
		for (T key : keyValueMap.keySet()) {
			str += key + "," + keyValueMap.get(key) + ";";
		}
		return str;
	}
	
	public HashMap<T, Double> getData () {
		return this.keyValueMap;
	}
	
	public Set<T> getKeys () {
		return this.keyValueMap.keySet();
	}
	
	public double getNorm () {
		return this.norm;
	}
	
	public void updateNorm () {
		norm = 0;
		for (T key : keyValueMap.keySet()) {
			double value = keyValueMap.get(key);
			double value1 = 1;
			norm += value * value * value1;
		}
		norm = Math.sqrt(norm);
	}
	
	public double jaccard (SparseVector v2) {
		Set<T> set1 = new HashSet<T>(this.keyValueMap.keySet());
		Set<T> set2 = (Set<T>) v2.getKeys();
		set1.retainAll(set2);
		int overlap = set1.size();
		return ((double) overlap) / (set1.size() + set2.size());
	}
	
	public SparseVectorT<T> add(SparseVectorT<T> v2) {
		SparseVectorT<T> v3 = new SparseVectorT<T>(v2);
		for (T key : keyValueMap.keySet()) {
			if (v3.keyValueMap.containsKey(key) == true) {
				double value1 = ((HashMap<T, Double>)v3.keyValueMap).get(key);
				double value2 = (double)keyValueMap.get(key);
				v3.keyValueMap.put(key, value1 + value2);
			}
		}
		v3.updateNorm();
		return v3;
	}
	
	public double cosine (SparseVectorT<T> v2) {
//		double norm1 = 0;
//		double norm2 = 0;
		double dot = 0;
		if (this.keyValueMap.size() < v2.keyValueMap.size()) {
			for (T key : this.keyValueMap.keySet()) {
				if (v2.keyValueMap.containsKey(key) == true) {
					double value1 = this.keyValueMap.get(key);
					double value2 = (Double) v2.keyValueMap.get(key);
					double value3 = 1;
					dot += value1 * value2 * value3;
				}
			}
		} else {
			for (T key : ((HashMap<T, Double>)v2.keyValueMap).keySet()) {
				if (this.keyValueMap.containsKey(key) == true) {
					double value1 = this.keyValueMap.get(key);
					double value2 = ((HashMap<T, Double>)v2.keyValueMap).get(key);
					double value3 = 1;
					dot += value1 * value2 * value3;
				}
			}
		}
		
		return dot / (this.norm + Double.MIN_NORMAL) / (v2.getNorm() + Double.MIN_NORMAL);
	}
	
	
}
