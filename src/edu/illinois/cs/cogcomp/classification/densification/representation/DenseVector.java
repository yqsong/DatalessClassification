package edu.illinois.cs.cogcomp.classification.densification.representation;

public class DenseVector {
	
	double[] vector;
	double norm = 0;
	
	public DenseVector () {
		this.vector = new double[1];
		norm = 0;
	}
	
	public DenseVector (int num) {
		this.vector = new double[num];
		norm = 0;
	}
	
	public DenseVector (double[] vector) {
		this.vector = vector;
		norm = 0;
		for (int i = 0; i < vector.length; ++i) {
			norm += vector[i] * vector[i];
		}
		norm = Math.sqrt(norm);
	}
	
	public void setVector (double[] vector) {
		this.vector = vector;
		norm = 0;
		for (int i = 0; i < vector.length; ++i) {
			norm += vector[i] * vector[i];
		}
		norm = Math.sqrt(norm);
	}
	
	public double[] getVector () {
		return this.vector;
	}
	
	public double getNorm () {
		return this.norm;
	}
	
	public double cosine (DenseVector v2) {
		double [] vector2 = v2.getVector();
		if (vector.length != vector2.length) {
			return 0;
		}
		double dot = 0;
		for (int i = 0; i < vector.length; ++i) {
			dot += vector[i] * vector2[i]; 
		}
		return dot / (this.norm + Double.MIN_NORMAL) / (v2.getNorm() + Double.MIN_NORMAL);
	}

}
