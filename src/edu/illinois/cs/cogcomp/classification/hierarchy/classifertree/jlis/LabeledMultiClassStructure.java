package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.jlis;

import edu.illinois.cs.cogcomp.indsup.inference.IStructure;
import edu.illinois.cs.cogcomp.indsup.learning.FeatureVector;
import edu.illinois.cs.cogcomp.indsup.mc.MultiClassInstance;

/**
 * yqsong@illinois.edu
 */

public class LabeledMultiClassStructure implements IStructure{
	public final MultiClassInstance input;
	public int output = -1;
	public double score;
	
	public LabeledMultiClassStructure(MultiClassInstance x, int y){
		input = x;
		output = y;
		assert output > -1;
	}

	@Override
	public FeatureVector getFeatureVector() {
		//System.out.println("input: " + input.base_fv);
		return input.base_fv.copyWithShift(output*input.base_n_fea);
	}

	@Override
	public String toString() {		
		return "" + output + " " + input.base_fv.toString();
	}

	@Override
	public boolean equals(Object aThat) {
		// check for self-comparison
		if (this == aThat)
			return true;

		if (!(aThat instanceof LabeledMultiClassStructure))
			return false;

		// cast to native object is now safe
		LabeledMultiClassStructure that = (LabeledMultiClassStructure) aThat;
	

		if (!this.input.equals(that.input))
			return false;
		else {
			if (this.output != that.output)
				return false;
			return true;
		}
	}


	@Override
	public int hashCode() {
		return output + 13 * input.hashCode();
	}

	

}
