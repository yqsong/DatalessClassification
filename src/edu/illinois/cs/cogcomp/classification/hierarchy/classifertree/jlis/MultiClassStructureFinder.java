package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.jlis;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.indsup.inference.AbstractLossSensitiveStructureFinder;
import edu.illinois.cs.cogcomp.indsup.inference.IInstance;
import edu.illinois.cs.cogcomp.indsup.inference.IStructure;
import edu.illinois.cs.cogcomp.indsup.learning.WeightVector;
import edu.illinois.cs.cogcomp.indsup.mc.MultiClassInstance;

/**
 * yqsong@illinois.edu
 */

public class MultiClassStructureFinder extends AbstractLossSensitiveStructureFinder{

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * loss matrix: first dimension is gold, the second dimension is prediction
	 * loss_matrix[i][i] = 0
	 * loss_matrix[i][j] represents the cost of predict j while the gold lab is i
	 */	
	public double[][] distance_matrix = null;
	
	public MultiClassStructureFinder(){
		
	}
	
	public MultiClassStructureFinder(double[][] loss_matrix){
		this.distance_matrix = loss_matrix;
	}

	public double[] getLossSensitiveStructureProbabilities(
			WeightVector weight, IInstance ins)
			throws Exception {
		
		MultiClassInstance mi = (MultiClassInstance) ins;
		
		int best_output = -1;
		double best_score = Double.NEGATIVE_INFINITY;
		
		double[] prob = new double[mi.number_of_class];
		for(int i=0; i < mi.number_of_class ; i ++){
			LabeledMultiClassStructure cand = new LabeledMultiClassStructure(mi, i);
			double score = weight.dotProduct(cand.getFeatureVector());
			
			prob[i] = score;
			
			if (score > best_score){
				best_output = i;
				best_score = score;
			}
		}
		
		double distance = 0;

        int nr_class = mi.number_of_class;
        int nr_w;
        if (nr_class == 2)
            nr_w = 1;
        else
            nr_w = nr_class;

		for (int i = 0; i < nr_w; i++)
			prob[i] = 1 / (1 + Math.exp(-prob[i]));

        if (nr_class == 2) // for binary classification
        	prob[1] = 1. - prob[0];
        else {
            double sum = 0;
            for (int i = 0; i < nr_class; i++)
                sum += prob[i];

            for (int i = 0; i < nr_class; i++)
            	prob[i] = prob[i] / sum;
        }
        
		assert best_output >= 0 ;
		return prob;		
	}

	
	@Override
	public Pair<IStructure, Double> getLossSensitiveBestStructure(
			WeightVector weight, IInstance ins, IStructure goldStructure)
			throws Exception {
		
		MultiClassInstance mi = (MultiClassInstance) ins;
		LabeledMultiClassStructure lmi = (LabeledMultiClassStructure) goldStructure;
		
		int best_output = -1;
		double best_score = Double.NEGATIVE_INFINITY;
		
		for(int i=0; i < mi.number_of_class ; i ++){
			LabeledMultiClassStructure cand = new LabeledMultiClassStructure(mi, i);
			double score = weight.dotProduct(cand.getFeatureVector());
			
			if (i != lmi.output){
				if(distance_matrix == null)
					score += 1.0;
				else{
					score += distance_matrix[lmi.output][i];
				}
			}								
			
			if (score > best_score){
				best_output = i;
				best_score = score;
			}
		}
		
		double distance = 0;

		if (best_output != lmi.output){
		    if(distance_matrix == null)
			distance = 1.0;
		    else
			distance = distance_matrix[lmi.output][best_output];		    
		}
			
		assert best_output >= 0 ;
		return new Pair<IStructure, Double>(new LabeledMultiClassStructure(mi, best_output),distance);		
	}

	@Override
	public IStructure getBestStructure(WeightVector weight,
			IInstance ins) throws Exception {
		MultiClassInstance mi = (MultiClassInstance) ins;
		
		int best_output = -1;
		double best_score = Double.NEGATIVE_INFINITY;
		
		for(int i=0; i < mi.number_of_class ; i ++){
			LabeledMultiClassStructure cand = new LabeledMultiClassStructure(mi, i);
			double score = weight.dotProduct(cand.getFeatureVector());
			if (score > best_score){
				best_output = i;
				best_score = score;
			}
		}
				
		assert best_output >= 0 ;
		LabeledMultiClassStructure result = new LabeledMultiClassStructure(mi, best_output);
		result.score = best_score;
		return result;
	}
	
	

}
