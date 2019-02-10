package edu.illinois.cs.cogcomp.classification.hierarchy.evaluation;

import java.util.List;

/**
 * yqsong@illinois.edu
 */

public class StatUtils {
	
	public static double listAverage (List<Double> list) {
		double sum = 0;
		for (int i = 0; i < list.size(); ++i) {
			sum += list.get(i);
		}
		sum /= list.size();
		return sum;
	}

	public static double listSum (List<Double> list) {
		double sum = 0;
		for (int i = 0; i < list.size(); ++i) {
			sum += list.get(i);
		}
		return sum;
	}
	
	public static double std (List<Double> list, double mean) {
		double std = 0;
		for (int i = 0; i < list.size(); ++i) {
			std += (list.get(i) - mean) * (list.get(i) - mean);
		}
		std /= list.size();
		std = Math.sqrt(std);
		return std;
	}

	
}
