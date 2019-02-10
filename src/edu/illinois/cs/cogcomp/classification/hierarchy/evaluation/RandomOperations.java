package edu.illinois.cs.cogcomp.classification.hierarchy.evaluation;

import java.util.Random;

/**
 * yqsong@illinois.edu
 */

public class RandomOperations {

	public static Random random = new Random(0);
	public static int[] RandPermutation(int n){
		int[] vas = new int[n];
		for (int i = 0; i < n; ++i) {
			vas[i] = i;
		}
		for(int i = n - 1; i >= 0; i--){
			int idx = (int)((i+1) * random.nextDouble());
			if(idx != i){
				int temp = vas[idx];
				vas[idx] = vas[i];
				vas[i] = temp;
			}
		}
		return vas;
	}
	
	public static int RandSample(int n){
		int idx = (int)(n * Math.random());
		return idx;
	}
	
	public static int RandSample(double[] probArray){
		double sum = 0;
		for (int i = 0; i < probArray.length; ++i) {
			sum += probArray[i];
		}
		for (int i = 0; i < probArray.length; ++i) {
			probArray[i] /= sum;
		}
		double num = Math.random();
		sum = 0;
		for (int i = 0; i < probArray.length; ++i) {
			sum += probArray[i];
			if (sum > num) {
				return i;
			}
		}
		int idx = (int)(probArray.length * random.nextDouble());
		return idx;
	}
}
