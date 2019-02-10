package edu.illinois.cs.cogcomp.classification.hierarchy.run;

import de.bwaldvogel.liblinear.SolverType;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.DiskBasedComplexESA;

/**
 * yqsong@illinois.edu
 */

public class ClassifierConstant {
	public static String complexVectorType = DiskBasedComplexESA.searchTypes[1];
	public static double cutOff = 0.1;
	
	public static SolverType solver = SolverType.L2R_LR_DUAL;//MCSVM_CS;//L2R_LR_DUAL; 
	// L2R_L2LOSS_SVC very good / L2R_LR and MCSVM_CS
	public static double classifierMLThreshold = 1.01;
	public static int leastK = 1;
	public static int maxK = 100;
	
	public static boolean isServer = true;
	public static boolean isBreakConcepts = false;
	
	public static double trainingRate = 0.005;

	public static int maxIterTopicModel = 100;
	
	public static String systemNewLine = System.getProperty("line.separator");

}
