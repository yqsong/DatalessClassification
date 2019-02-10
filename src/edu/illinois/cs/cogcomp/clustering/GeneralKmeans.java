package edu.illinois.cs.cogcomp.clustering;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import cern.jet.random.Uniform;
import edu.illinois.cs.cogcomp.classification.densification.test.WikiLabel;
import edu.illinois.cs.cogcomp.clustering.datastructure.ColtDenseVector;
import edu.illinois.cs.cogcomp.clustering.datastructure.ColtSparseVector;

public class GeneralKmeans {
	// Stop after movement of means is less than this
	protected double MEANS_TOLERANCE = 0.0000000000001;
	// Maximum number of iterations
	protected int MAX_ITER = 100;
	// Minimum fraction of points that move
	protected double POINTS_TOLERANCE = .001;
	// Treat an empty cluster as an error condition.
	protected int EMPTY_ERROR = 0;
	// Drop an empty cluster
	protected int EMPTY_DROP = 1;
	// Place the single instance furthest from the previous cluster mean
	protected int EMPTY_SINGLE = 2;
	// Cost change
	protected double COST_CHANGE_DELTA = 0.001;
	
	protected boolean isDebug = true;
	
	protected boolean isProfiling = false;
	
	protected boolean isCenterSparse = true;
	
	
	protected int clusterNum;
	protected int randomSeed = 0;
	protected List<DoubleMatrix1D> data = null;
	protected List<DoubleMatrix1D> centers = null;
//	protected ArrayList<Double> dataNorm = null;
	protected int[] clusterLabels;
	protected Uniform random = null;
	
	protected String initMethod;
	protected String distanceType = "denseESA";
	WikiLabel w;
	
	public void set(WikiLabel label){
		w=label;
		
	}
	
	public GeneralKmeans(double[][] data, int cNum) {
		this(data, false, cNum, "maxmin");
	}
	
	public GeneralKmeans(DoubleMatrix1D[] data, int cNum) {
		this(data, cNum, "maxmin");
	}

	public GeneralKmeans(List<DoubleMatrix1D> data, int cNum) {
		this(data, cNum, "maxmin", 0);
	}
	
	public GeneralKmeans(double[][] data, boolean isDense, int cNum, String method) {
		this(data, isDense, cNum, method, 0);
	}
	
	public GeneralKmeans(DoubleMatrix1D[] data, int cNum, String method) {
		this(data, cNum, method, 0);
	}

	public GeneralKmeans(List<DoubleMatrix1D> data, int cNum, String method) {
		this(data, cNum, method, 0);
	}
	
	public GeneralKmeans(List<DoubleMatrix1D> data, int cNum, String method,WikiLabel label ) {
		this(data, cNum, method, 0);
		w=label;
	}


	public GeneralKmeans(double[][] dataMat, boolean isDense, int cNum, String method, int seed) {
		clusterNum = cNum;
		randomSeed = seed;
		random = new Uniform(0, 1, randomSeed);
		initMethod = method;
		clusterLabels = new int[dataMat.length];
		this.data = new ArrayList<DoubleMatrix1D>();
		
		if (isDense == true) {
			for (int i = 0; i < dataMat.length; ++i) {
				DoubleMatrix1D sample = new ColtDenseVector(dataMat[i].length);
				sample.assign(dataMat[i]);
				this.data.add(sample);
			}
		} else {
			for (int i = 0; i < dataMat.length; ++i) {
				DoubleMatrix1D sample = new ColtSparseVector(dataMat[i].length);
				sample.assign(dataMat[i]);
				sample.trimToSize();
				this.data.add(sample);
			}
		}
		initializeDataNorm();
		initializeCenters();
	}
	
	public GeneralKmeans(DoubleMatrix1D[] dataMat, int cNum, String method, int seed) {
		clusterNum = cNum;
		randomSeed = seed;
		random = new Uniform(0, 1, randomSeed);
		initMethod = method;
		clusterLabels = new int[dataMat.length];
		this.data = new ArrayList<DoubleMatrix1D>();
	
		if (dataMat instanceof DenseDoubleMatrix1D[]) {
			for (int i = 0; i < dataMat.length; ++i) {
				this.data.add(dataMat[i]);
			}
		} else if (dataMat instanceof SparseDoubleMatrix1D[]) {
			for (int i = 0; i < dataMat.length; ++i) {
				DoubleMatrix1D sample = null;
				if (dataMat instanceof ColtSparseVector[]) {
					sample = dataMat[i];
				} else {
					sample = new ColtSparseVector(dataMat[i].size());
					sample.assign(dataMat[i]);
				}
				sample.trimToSize();
				this.data.add(sample);
			}
			int size = dataMat[0].size();
			for (int i = 0; i < dataMat.length; ++i) {
				if (size != dataMat[i].size()) {
					System.err.println("columns are not equal length");
				}
			}
		}
		initializeDataNorm();
		initializeCenters();
	}
	
	public GeneralKmeans(List<DoubleMatrix1D> dataMat, int cNum, String method, int seed) {
		clusterNum = cNum;
		randomSeed = seed;
		random = new Uniform(0, 1, randomSeed);
		initMethod = method;
		clusterLabels = new int[dataMat.size()];
		this.data = new ArrayList<DoubleMatrix1D>();

		for (int i = 0; i < dataMat.size(); ++i) {
			DoubleMatrix1D sample = null;
			if (dataMat.get(i) instanceof DenseDoubleMatrix1D) {
				sample = new ColtDenseVector(dataMat.get(i).size());
				sample.assign(dataMat.get(i));
			}
			if (dataMat.get(i) instanceof SparseDoubleMatrix1D) {
				if (dataMat.get(i) instanceof ColtSparseVector) {
					sample = dataMat.get(i);
				} else {
					sample = new ColtSparseVector(dataMat.get(i).size());
					sample.assign(dataMat.get(i));
				}
			}
			if (sample == null) {
				sample = new ColtSparseVector(dataMat.get(0).size());  
			}
			sample.trimToSize();
			this.data.add(sample);
		}
		initializeDataNorm();
		initializeCenters();
	}
	
//	public GeneralKmeans(DoubleMatrix2D dataMat, int cNum, String method) {
//	clusterNum = cNum;
//	random = new Uniform(0, 1, randomSeed);
//	initMethod = method;
//	clusterLabels = new int[dataMat.rows()];
//	this.data = new ArrayList<DoubleMatrix1D>();
//	this.centers = new ArrayList<DoubleMatrix1D>();
//	
//	if (dataMat instanceof DenseDoubleMatrix2D) {
//		for (int i = 0; i < dataMat.rows(); ++i) {
//			this.data.add((DenseDoubleMatrix1D) dataMat.viewRow(i));
//		}
//	} else if (dataMat instanceof SparseDoubleMatrix2D) {
//		for (int i = 0; i < dataMat.rows(); ++i) {
//			this.data.add((SparseDoubleMatrix1D) dataMat.viewRow(i));
//		}
//	}
//initializeDataNorm();
//initializeCenters();
//}
	
	protected void initializeCenters() {
		this.centers = new ArrayList<DoubleMatrix1D>();
		for (int i = 0; i < clusterNum; ++i) {
			DoubleMatrix1D center = null;
			if (isCenterSparse)
				center = new ColtSparseVector(this.data.get(0).size());
			else
				center = new ColtDenseVector(this.data.get(0).size());
			centers.add(center);
		}
	}
	
	protected void initializeDataNorm() {
//		dataNorm = new ArrayList<Double>();
		for (int i = 0; i < data.size(); ++i) {
			double norm = 0.0;
			DoubleMatrix1D v1 = data.get(i);
			if (v1 instanceof SparseDoubleMatrix1D) {
				norm = normQuick((SparseDoubleMatrix1D) v1);
			} else {
				norm = product(v1, v1);
			}
			if (data.get(i) instanceof ColtSparseVector)
				((ColtSparseVector) data.get(i)).setNormValue(norm);
			if (data.get(i) instanceof ColtDenseVector)
				((ColtDenseVector) data.get(i)).setNormValue(norm);
//			dataNorm.add(norm);
		}
	}
	
	protected double update(List<List<DoubleMatrix1D>> instanceClusters) {
		
		double deltaMeans = 0.0;
		DoubleMatrix1D clusterMean = null;
		
		for (int c = 0; c < clusterNum; c++) {

//			System.out.println("Cluster point number: " + instanceClusters.get(c).size());
			
			if (instanceClusters.get(c).size() > 0) {
				clusterMean = this.mean(instanceClusters.get(c));
				
				deltaMeans += computeDistance(centers.get(c), clusterMean);
				
				centers.set(c, clusterMean);

				instanceClusters.set(c, new ArrayList<DoubleMatrix1D>());
				
			} else {
				if (isDebug == true) {
					System.out.println("Empty cluster found.");
				}

				// Get the instance the furthest from any centroid
				// and make it a new centroid.
				double newCentroidDist = 0;
				int newCentroid = 0;
				List<DoubleMatrix1D> cacheList = null;

				for (int clusters = 0; clusters < centers.size(); clusters++) {
					DoubleMatrix1D centroid = centers.get(clusters);
					List<DoubleMatrix1D> centInstances = instanceClusters.get(clusters);
					
					// Dont't create new empty clusters.
					
					if (centInstances.size() <= 1)
						continue;
					for (int n = 0; n < centInstances.size(); n++) {
						double currentDist = 0;
						if (centInstances.get(n) instanceof ColtSparseVector)
							currentDist = computeDistance(centInstances.get(n), centroid, 
									product(centroid, centroid), ((ColtSparseVector) centInstances.get(n)).getNormValue());
						if (centInstances.get(n) instanceof ColtDenseVector)
							currentDist = computeDistance(centInstances.get(n), centroid,
									product(centroid, centroid), ((ColtDenseVector) centInstances.get(n)).getNormValue());

						if (currentDist > newCentroidDist) {
							newCentroid = n;
							newCentroidDist = currentDist;
							cacheList = centInstances;
						}
					}
				}
				if (cacheList == null) {
					// Can't find an instance to move.
					return -1;
				} else {
					DoubleMatrix1D tempCenter = null;
					if (isCenterSparse)
						tempCenter = new ColtSparseVector(cacheList.get(newCentroid).size());
					else
						tempCenter = new ColtDenseVector(cacheList.get(newCentroid).size());
					tempCenter.assign(cacheList.get(newCentroid));
					centers.set(c, tempCenter);
				}
			}

		}
		return deltaMeans;
	}
	
	public void estimate() {
		// Initialize clusterMeans
//		Timing timing = new Timing();

//		Timing profiling = new Timing();
		
		if (isDebug == true) {
			System.out.println("Entering KMeans initialization, doc num: " + this.getNumInstances() + ", cluster num: " + this.clusterNum);
		}
		
		// initialization centers
		if(this.initMethod.equalsIgnoreCase("orthogonal")) {
			initializeOrthogonalMeansSample();
		} else if (initMethod.equalsIgnoreCase("maxmin")) {
			initializeMeansSample();
		} else {
			initializeRandomMeansSample();
		}
		
		List<List<DoubleMatrix1D>> instanceClusters = 
			new ArrayList<List<DoubleMatrix1D>>(clusterNum);
		for (int c = 0; c < clusterNum; c++) {
			instanceClusters.add(c, new ArrayList<DoubleMatrix1D>());
		}
		
		double deltaMeans = 1.0;
		double deltaPoints = data.size();
		double cost = 1.0;
		double oldCost = Double.MAX_VALUE;
		double deltaCost = Double.MAX_VALUE;
		int iterations = 0;

		if (isProfiling == true) {
			System.out.println("    Profiling: KMeans initialization done!");
		}

		if (isDebug == true) {
			System.out.println("Kmeans initialization:");
			System.out.println("Entering KMeans iteration..");
		}
//		meansChange > CHANGE_DELTA
		while (deltaCost > COST_CHANGE_DELTA &&
				deltaMeans > MEANS_TOLERANCE && iterations < MAX_ITER
				&& deltaPoints > data.size() * POINTS_TOLERANCE) {

			iterations++;
			deltaPoints = 0;
			System.out.print("iteration "+iterations);
			ArrayList<Double> centerNorm = new ArrayList<Double>();
			for (int i = 0; i < centers.size(); ++i) {
				double norm = 0.0;
				DoubleMatrix1D v1 = centers.get(i);
				if (v1 instanceof SparseDoubleMatrix1D) {
					norm = normQuick((SparseDoubleMatrix1D) v1);
				} else {
					norm = product(v1, v1);
				}
				centerNorm.add(norm);
			}
			if (isProfiling == true) {
				System.out.println("    Profiling: Compute center norm�� ");
			}	
			
			int instClustIndex;
			double minInstClustDist, instCenterDist = 0;
			cost = 0.0;
			for (int n = 0; n < data.size(); n++) {
//			for (int n = data.size() - 1; n >0; n--) {
				instClustIndex = 0;
				minInstClustDist = Double.MAX_VALUE;

				for (int c = 0; c < clusterNum; c++) {
					if (data.get(n) instanceof ColtSparseVector)
						instCenterDist = computeDistance(data.get(n), centers.get(c), 
								((ColtSparseVector) data.get(n)).getNormValue(), centerNorm.get(c));
					if (data.get(n) instanceof ColtDenseVector)
						instCenterDist = computeDistance(data.get(n), centers.get(c),
								((ColtDenseVector) data.get(n)).getNormValue(), centerNorm.get(c));

					if (instCenterDist < minInstClustDist) {
						instClustIndex = c;
						minInstClustDist = instCenterDist;
					}
				}
				// Add to closest cluster & label it such
				instanceClusters.get(instClustIndex).add(data.get(n));

				cost += minInstClustDist;
				
				if (clusterLabels[n] != instClustIndex) {
					clusterLabels[n] = instClustIndex;
					deltaPoints++;
				}
			}

			if (isProfiling == true) {
				System.out.println("    Profiling: Compute distances from each point to centers for points�� ");
			}				
			

			deltaMeans = update(instanceClusters);
			
			deltaCost = Math.abs(cost - oldCost) / (oldCost + Double.MIN_VALUE);
			oldCost = cost;
			
			if (isDebug == true && deltaMeans == -1) {
				System.out.println("Can't find an instance to move.  Exiting.");
			}

			if (isProfiling == true) {
				System.out.println("    Profiling: Compute center means: ");
			}
			
			if (isDebug == true) {
//				System.out.println(" Iter " + iterations + " deltaMeans = " + deltaMeans);
				System.out.println(" Interaion " + iterations
						+ " deltaCost = " + deltaCost
						+ " deltaMeans = " + deltaMeans + ": ");
			}
		}

		if (deltaCost <= COST_CHANGE_DELTA) {
			if (isDebug == true) 
			{
				System.out.println("KMeans converged with deltaCost = " + deltaCost);
			}
		}
		
		if (deltaMeans <= MEANS_TOLERANCE) {
			if (isDebug == true) 
			{
				System.out.println("KMeans converged with deltaMeans = " + deltaMeans);
			}
		}
		else if (iterations >= MAX_ITER) {
			if (isDebug == true) 
			{
				System.out.println("Maximum number of iterations (" + MAX_ITER + ") reached.");
			}
		}
		else if (deltaPoints <= data.size() * POINTS_TOLERANCE) {
			if (isDebug == true) 
			{
				System.out.println("Minimum number of points (np*" + POINTS_TOLERANCE + "="
					+ (int) (data.size() * POINTS_TOLERANCE)
					+ ") moved in last iteration. Saying converged.");
			}
		}
	}
	
	protected void initializeRandomMeansSample() {
	
		int randNum = random.nextIntFromTo(0, data.size()-1);
		// int randNum = 0;
		for (int i = 0; i < clusterNum; i++) {
			DoubleMatrix1D tempCenter = null;
			if (isCenterSparse)
				tempCenter = new ColtSparseVector(data.get(randNum).size());
			else
				tempCenter = new ColtDenseVector(data.get(randNum).size());
			tempCenter.assign(data.get(randNum));
			centers.set(i, tempCenter);
		}

	}
	
	protected void initializeMeansSample() {

		List<DoubleMatrix1D> instances =  new ArrayList<DoubleMatrix1D>(data.size());
		for (int i = 0; i < data.size(); i++) {
			DoubleMatrix1D ins = data.get(i);
			IntArrayList indexs = new IntArrayList();
			DoubleArrayList values = new DoubleArrayList();
			ins.getNonZeros(indexs, values);
			if (indexs.size() > 0) {
				instances.add(ins);
			}
		}

		// Add next center that has the MAX of the MIN of the distances from
		// each of the previous j-1 centers (idea from Andrew Moore tutorial,
		// not sure who came up with it originally)
		int randNum = random.nextIntFromTo(0, instances.size()-1);
//		randNum = 0;
		DoubleMatrix1D tempVector = instances.remove(randNum);
		DoubleMatrix1D tempCenter = null;
		if (this.isCenterSparse)
			tempCenter = new ColtSparseVector(tempVector.size());
		else
			tempCenter = new ColtDenseVector(tempVector.size());
		tempCenter.assign(tempVector);
		centers.set(0, tempCenter);
//		if (isDebug == true) {
//			System.out.println("Initialize center: " + 0 + " using data " + randNum);
//		}
		for (int i = 1; i < clusterNum; i++) {
			System.out.print("initialized cluster "+i+"\n");
			double max = 0;
			int selected = 0;
			for (int k = 0; k < instances.size(); k++) {
				double min = Double.MAX_VALUE;
				DoubleMatrix1D inst = instances.get(k);
				for (int j = 0; j < i; j++) {
					DoubleMatrix1D center = centers.get(j);
					double dist = 0;
					if (inst instanceof ColtSparseVector)
						dist = computeDistance(inst, center, 
								((ColtSparseVector) inst).getNormValue(), product(center, center));
					if (inst instanceof ColtDenseVector)
						dist = computeDistance(inst, center, 
								((ColtDenseVector) inst).getNormValue(), product(center, center));

					
					if (dist < min)
						min = dist;
				}
				if (min > max) {
					selected = k;
					max = min;
				}
			}
			tempVector = instances.remove(selected);
			if (this.isCenterSparse)
				tempCenter = new ColtSparseVector(tempVector.size());
			else
				tempCenter = new ColtDenseVector(tempVector.size());
			tempCenter.assign(tempVector);
			centers.set(i, tempCenter);
//			if (isDebug == true) {
//				System.out.println("Initialize center " + i + " using data " + selected);
////				System.out.println(" Instance size = " + instances.size());
//			}
		}
	}

	protected void initializeOrthogonalMeansSample() {

		List<DoubleMatrix1D> instances = new ArrayList<DoubleMatrix1D>(data.size());
		for (int i = 0; i < data.size(); i++) {
			DoubleMatrix1D ins = data.get(i);
			IntArrayList indexs = new IntArrayList();
			DoubleArrayList values = new DoubleArrayList();
			ins.getNonZeros(indexs, values);
			if (indexs.size() > 0) {
				instances.add(ins);
			}
		}
		List<Double> orthValue = new ArrayList<Double>(instances.size());
		for (int i = 0; i < instances.size(); ++i) {
			orthValue.add(0.0);
		}
		
		// Add next center that is orthogonal to each of the previous j-1 centers

		int randNum = random.nextIntFromTo(0, instances.size()-1);
//		randNum = 0;
		DoubleMatrix1D tempVector = instances.remove(randNum);
		DoubleMatrix1D tempCenter = null;
		if (this.isCenterSparse)
			tempCenter = new ColtSparseVector(tempVector.size());
		else
			tempCenter = new ColtDenseVector(tempVector.size());
		tempCenter.assign(tempVector);
		tempCenter.assign(tempVector);
		centers.set(0, tempCenter);
		orthValue.remove(randNum);
//		if (isDebug == true) {
//			System.out.println("Initialize center: " + 0 + " using data " + randNum);
//		}
		
		for (int i = 1; i < clusterNum; i++) {
			int selected = 0;
			double min = Double.MAX_VALUE;
			DoubleMatrix1D center = centers.get(i - 1);
			for (int k = 0; k < instances.size(); k++) {
				DoubleMatrix1D inst = instances.get(k);
				
				double dist = 0.0;
				if (inst instanceof ColtSparseVector)
					dist = orthValue.get(k) + product(inst, center) / (Math.sqrt(
					(((ColtSparseVector) inst).getNormValue() * product(center, center)))
					+ Double.MIN_VALUE);
				if (inst instanceof ColtDenseVector)
					dist = orthValue.get(k) + product(inst, center) / (Math.sqrt(
					(((ColtDenseVector) inst).getNormValue() * product(center, center)))
					+ Double.MIN_VALUE);
				
				orthValue.set(k, dist);
				if (dist < min) {
					min = dist;
					selected = k;
				}
			}
			
//			if (isDebug == true) {
//				System.out.println("Initialize center " + i + " using data " + selected);
////				System.out.println(" Instance size = " + instances.size());
//			}
			
			tempVector = instances.remove(selected);
			if (this.isCenterSparse)
				tempCenter = new ColtSparseVector(tempVector.size());
			else
				tempCenter = new ColtDenseVector(tempVector.size());
			tempCenter.assign(tempVector);
			centers.set(i, tempCenter);
			orthValue.remove(selected);
		}

	}
	
	protected double[] averageDist(DoubleMatrix1D v1, DoubleMatrix1D v2) {
		double[] sumVector = new double[v1.size()];
		Arrays.fill(sumVector, 0.0);
		if (v1 instanceof SparseDoubleMatrix1D) {
			IntArrayList indexList = new IntArrayList();
			DoubleArrayList valueList = new DoubleArrayList();
			v1.getNonZeros(indexList, valueList);
			for (int j = 0; j < indexList.size(); ++j) {
				int index = indexList.get(j);
				double value = valueList.get(j);
				sumVector[index] += value;
			}

		} else {
			for (int j = 0; j < v1.size(); ++j) {
				double value = v1.getQuick(j);
				if (value != 0) {
					sumVector[j] += value;
				}
			}
		}
		
		if (v2 instanceof SparseDoubleMatrix1D) {
			IntArrayList indexList = new IntArrayList();
			DoubleArrayList valueList = new DoubleArrayList();
			v2.getNonZeros(indexList, valueList);
			for (int j = 0; j < indexList.size(); ++j) {
				int index = indexList.get(j);
				double value = valueList.get(j);
				sumVector[index] += value;
			}

		} else {
			for (int j = 0; j < v2.size(); ++j) {
				double value = v2.getQuick(j);
				if (value != 0) {
					sumVector[j] += value;
				}
			}
		}
		double sum = 0.0;
		for(int i = 0; i < sumVector.length; ++i ) {
			sumVector[i] /= 2;
			sum += sumVector[i];
		}
		for(int i = 0; i < sumVector.length; ++i ) {
			sumVector[i] /= sum;
		}
		
		return sumVector;
	}
	
	protected double product(DoubleMatrix1D v1, DoubleMatrix1D v2) {
		if (v1 instanceof SparseDoubleMatrix1D) {
			return productQuick(v1, v2);
		} else if (v2 instanceof SparseDoubleMatrix1D) {
			return productQuick(v2, v1);
		} else {
			return v1.zDotProduct(v2);
		}
	}
	
	protected double productQuick(DoubleMatrix1D v1, DoubleMatrix1D v2) {
		IntArrayList indexList = new IntArrayList();
		DoubleArrayList valueList = new DoubleArrayList();
		v1.getNonZeros(indexList, valueList);
		double prod = 0.0;
		for (int i = 0; i < indexList.size(); ++i) {
			double temp = v2.getQuick(indexList.getQuick(i));
			if (temp != 0.0) {
				prod += valueList.getQuick(i) * temp;
			}
		}
//		double prod = 0.0;
//		for (int i = 0; i < v1.size(); ++i) {
//			double temp1 = v1.getQuick(i);
//			double temp2 = v2.getQuick(i);
//			if (temp1 != 0.0 || temp2 != 0.0) {
//				prod += temp1 * temp2;
//			}
//		}
		return prod;
	}
	
	// note here is norm square!!!!
	protected double normQuick(SparseDoubleMatrix1D v) {
		IntArrayList indexList = new IntArrayList();
		DoubleArrayList valueList = new DoubleArrayList();
		v.getNonZeros(indexList, valueList);
		double norm = 0.0;
		for (int i = 0; i < valueList.size(); ++i) {
			norm += valueList.get(i) * valueList.get(i);
		}
		return norm;
	}
	
	protected double computeKLDivergence(DoubleMatrix1D v1, double[] meanVector) {
		double divergence = 0.0;
		if (v1 instanceof SparseDoubleMatrix1D) {
			IntArrayList indexList = new IntArrayList();
			DoubleArrayList valueList = new DoubleArrayList();
			v1.getNonZeros(indexList, valueList);
			for (int j = 0; j < indexList.size(); ++j) {
				int index = indexList.get(j);
				double value = valueList.get(j);
				divergence += value * Math.log(value / meanVector[index]);
			}

		} else {
			for (int j = 0; j < v1.size(); ++j) {
				double value = v1.getQuick(j);
				if (value != 0) {
					divergence += value * Math.log(value / meanVector[j]);
				}
			}
		}
		return divergence;
	}
	protected double computeJSDivergence(DoubleMatrix1D v1, DoubleMatrix1D v2) {
		assert(v1.size() == v2.size());
		double sum = 0.0;
		for(int i = 0; i < v2.size(); ++i ) {
			sum += v2.getQuick(i);
		}
		DoubleMatrix1D v3 = v2.copy();
		for(int i = 0; i < v2.size(); ++i ) {
			v3.set(i, v2.getQuick(i)/sum);
		}
		
		double divergence = 0.0;
		double[] meanVector = averageDist(v1, v2);
		divergence = computeKLDivergence(v1, meanVector) + computeKLDivergence(v2, meanVector);
		return divergence;
	}
	
	protected double computeEuclidean(DoubleMatrix1D v1, DoubleMatrix1D v2) {
		assert(v1.size() == v2.size());
		
		double norm1 = 0.0;
		if (v1 instanceof SparseDoubleMatrix1D) {
			norm1 = normQuick((SparseDoubleMatrix1D) v1);
		} else {
			norm1 = product(v1, v1);
		}
		double norm2 = 0.0;
		if (v2 instanceof SparseDoubleMatrix1D) {
			norm2 = normQuick((SparseDoubleMatrix1D) v2);
		} else {
			norm2 = product(v2, v2);
		}
		double dot = product(v1, v2);
				
//		return Math.sqrt(norm1 + norm2 - 2 * dot);
		return (norm1 + norm2 - 2 * dot);
	}
	
	protected double computeEuclidean(DoubleMatrix1D v1, DoubleMatrix1D v2, double norm1, double norm2) {
		assert(v1.size() == v2.size());
		
		double dot = product(v1, v2);
			
//		return Math.sqrt(norm1 + norm2 - 2 * dot);
		return (norm1 + norm2 - 2 * dot);
	}
	
	protected double computeSpecialEuclidean(DoubleMatrix1D v1, DoubleMatrix1D v2) {
		assert(v1.size() == v2.size());
		
		double norm1 = 0.0;
		if (v1 instanceof SparseDoubleMatrix1D) {
			norm1 = normQuick((SparseDoubleMatrix1D) v1);
		} else {
			norm1 = product(v1, v1);
		}
		double norm2 = 0.0;
		if (v2 instanceof SparseDoubleMatrix1D) {
			norm2 = normQuick((SparseDoubleMatrix1D) v2);
		} else {
			norm2 = product(v2, v2);
		}

		double dot = product(v1, v2);
				
		return (1 - dot / ( Math.sqrt(norm1 * norm2) + Double.MIN_VALUE ) );
	}
	
	protected double computeSpecialEuclidean(DoubleMatrix1D v1, DoubleMatrix1D v2, double norm1, double norm2) {
		assert(v1.size() == v2.size());
		
		double dot = product(v1, v2);
				
		return (1 - dot / ( Math.sqrt(norm1 * norm2) + Double.MIN_VALUE ) );
	}
	
	protected double computeDistance(DoubleMatrix1D v1, DoubleMatrix1D v2) {
		assert(v1.size() == v2.size());
		
		double distance = 0.0;
		
		if (this.distanceType.equalsIgnoreCase("Euclidean")) {
			distance = computeEuclidean(v1, v2);
		} else if (this.distanceType.equalsIgnoreCase("Sphecial")) {
			distance = computeSpecialEuclidean(v1, v2);
		} else if (this.distanceType.equalsIgnoreCase("KLDivergence")) {
			distance = computeJSDivergence(v1, v2);
		}
		else if(this.distanceType.equalsIgnoreCase("denseESA")) {
			distance = w.distance(v1, v2);
		}
		
		return distance;
		
	}
	
	protected double computeDistance(DoubleMatrix1D v1, DoubleMatrix1D v2, double norm1, double norm2) {
		assert(v1.size() == v2.size());
		
		double distance = 0.0;
		
		if (this.distanceType.equalsIgnoreCase("Euclidean")) {
			distance = computeEuclidean(v1, v2, norm1, norm2);
		} else if (this.distanceType.equalsIgnoreCase("Sphecial")) {
			distance = computeSpecialEuclidean(v1, v2, norm1, norm2);
		} else if (this.distanceType.equalsIgnoreCase("KLDivergence")) {
			distance = computeJSDivergence(v1, v2);
		}
		else if(this.distanceType.equalsIgnoreCase("denseESA")) {
			distance = w.distance(v1, v2);
		}
		
		return distance;	
	}
	
	protected DoubleMatrix1D mean(List<DoubleMatrix1D> points) {
		double[] mean = new double[points.get(0).size()];
		Arrays.fill(mean, 0.0);
		for (int i = 0; i < points.size(); ++i) {
			DoubleMatrix1D v = points.get(i);
			if (v instanceof SparseDoubleMatrix1D) {
				IntArrayList indexList = new IntArrayList();
				DoubleArrayList valueList = new DoubleArrayList();
				v.getNonZeros(indexList, valueList);
				for (int j = 0; j < indexList.size(); ++j) {
					int index = indexList.get(j);
					double value = valueList.get(j);
					mean[index] += value;
				}

			} else {
				for (int j = 0; j < v.size(); ++j) {
					double value = v.getQuick(j);
					if (value != 0) {
						mean[j] += value;
					}
				}
			}
		}
		int size = points.size();
		for (int i = 0; i < mean.length; ++i) {
			mean[i] /= size;
		}
		DoubleMatrix1D meanVector = null;
		if (this.isCenterSparse)
			meanVector = new ColtSparseVector(mean.length);
		else
			meanVector = new ColtDenseVector(mean.length);
		meanVector.assign(mean);
		return meanVector;
		
	}
		
	public void setDistType (String distType) {
		this.distanceType = distType;
	}

	public void setDebug(boolean isDebug) {
		this.isDebug = isDebug;
	}

	public int[] getLabels() {
		return clusterLabels;
	}
	
	public int getLabel(int index) {
		return clusterLabels[index];
	}

	public DoubleMatrix1D[] getCenters() {
		DoubleMatrix1D[] centersArray = new DoubleMatrix1D[centers.size()];
		for (int i = 0; i < centers.size(); ++i) {
			centersArray[i] = centers.get(i);
		}
		return centersArray;
	}

	public double[][] getCentersArray() {
		DoubleMatrix1D[] centersMat = new DoubleMatrix1D[centers.size()];
		double[][] centerArray = new double[centers.size()][];
		for (int i = 0; i < centers.size(); ++i) {
			centerArray[i] = centers.get(i).toArray();
		}
		return centerArray;
	}

	
	public List<DoubleMatrix1D> getCentersList() {
		return centers;
	}
	
	public int getClusterNum() {
		return clusterNum;
	}

	public void setClusterNum(int clusterNum) {
		this.clusterNum = clusterNum;
	}

	public int getRandomSeed() {
		return randomSeed;
	}
	
	public int getNumInstances() {
		return this.data.size();
	}

	public void setRandomSeed(int randomSeed) {
		this.randomSeed = randomSeed;
		this.random = new Uniform(0, 1, randomSeed);
	}

	public void setInitMethod(String initMethod) {
		this.initMethod = initMethod;
	}

	public boolean isCenterSparse() {
		return isCenterSparse;
	}

	public void setCenterSparse(boolean isCenterSparse) {
		this.isCenterSparse = isCenterSparse;
	}

//	public static void do_kemeans_cluster(String data_matrix_address, int cluster_num, String cluster_result_address) throws IOException{
//		
//		List<DoubleMatrix1D> feature = InformationTheoreticCoClustering.read_data_matrix(data_matrix_address);
//		System.out.println(feature.size());
//		GeneralKmeans kmeans = new GeneralKmeans(feature, cluster_num,"maxmin",0);
//		kmeans.estimate();
//		int[] label = kmeans.getLabels();
//		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(cluster_result_address)));
//		for (int i = 0; i < label.length; ++i) {
////			System.out.print(label[i] + " ");
//			writer.write(label[i]+"");
//			writer.newLine();
//		}
//		writer.flush();
//		writer.close();
//		
//	}
	
	public static void do_kemeans_cluster(List<DoubleMatrix1D> feature, int cluster_num, List<Integer> label_list) throws IOException{
		
		System.out.println(feature.size());
		GeneralKmeans kmeans = new GeneralKmeans(feature,cluster_num,"maxmin",0);
		kmeans.estimate();
		int[] label = kmeans.getLabels();
		for(int i=0; i<label.length; i++)
			label_list.add(label[i]);
	}
	
	public static int[] do_kemeans_cluster(List<DoubleMatrix1D> feature, int cluster_num) throws IOException{
		
		System.out.println(feature.size());
		GeneralKmeans kmeans = new GeneralKmeans(feature,cluster_num,"maxmin",0);
		kmeans.estimate();
		int[] label = kmeans.getLabels();
		return label;
		
	}
	
	// mini test
	public static void main (String[] args) {

//		double[][] feature = {
//				  {0.05, 0.05, 0.05, 0, 0, 0},
//				  {0.05, 0.05, 0.05, 0, 0, 0},
//				  {0, 0, 0, 0.05, 0.05, 0.05},
//				  {0, 0, 0, 0.05, 0.05, 0.05},
//				  {0.04, 0.04, 0, 0.04, 0.04, 0.04},
//				  {0.04, 0.04, 0.04, 0, 0.04, 0.04},
//				  };	
////		double[][] feature = {
////				  {0.05, 0.05, 0.05, 0, 0, 0},
////				  {0.05, 0.05, 0.05, 0, 0, 0},
////				  {0.07, 0.03, 0.03, 0, 0, 0},
////				  {0.07, 0.03, 0.03, 0.02, 0, 0},
////				  {0, 0, 0.02, 0.04, 0.03, 0.05},
////				  {0, 0, 0, 0.04, 0.03, 0.05},
////				  {0, 0, 0, 0.05, 0.05, 0.05},
////				  {0, 0, 0, 0.05, 0.05, 0.05},
////				  };	
//		GeneralKmeans kmeans = new GeneralKmeans(feature, 3);
//		kmeans.estimate();
//		int[] label = kmeans.getLabels();;
//		for (int i = 0; i < label.length; ++i) {
//			System.out.print(label[i] + " ");
//		}
		
		// step 1: use concept-score pair to build a dictionary mapping
		// 0-->concept1; 1-->concept2....
		
		// DoubleMatrix1D is a vector; SparseDoubleMatrix1D is a sparse vector
		List<String> labelList = new ArrayList<String>();
		List<DoubleMatrix1D> dataMat = new ArrayList<DoubleMatrix1D>();
		int dataNum = 10;
		int dimension = 1000;
		for (int i = 0; i < dataNum; ++i) {
			DoubleMatrix1D vec = new SparseDoubleMatrix1D(dimension);
			vec.setQuick(1, 0.1);
			vec.setQuick(2, 33);
			dataMat.add(vec);
		} 
		int cNum = 3; // number of clusters you want to cluster
		String method = "maxmin"; // initialization method
		int seed = 0; // seed of random generation
		GeneralKmeans kmeans = new GeneralKmeans(dataMat, cNum, method, seed);
		kmeans.estimate();
		int[] label = kmeans.getLabels();;
		for (int i = 0; i < label.length; ++i) {
			System.out.print(label[i] + " ");
		}
	}


}
