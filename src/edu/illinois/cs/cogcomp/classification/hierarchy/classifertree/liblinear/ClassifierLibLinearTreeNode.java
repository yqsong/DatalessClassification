package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.AbstractTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

/**
 * yqsong@illinois.edu
 */

public class ClassifierLibLinearTreeNode extends AbstractTreeNode {

	private Set<ClassifierLibLinearTreeNode> children;
	private Problem problem;
	private Model model;
	private boolean isLeafNode;
	private Map<String, Integer> labelMapping;
	
	public ClassifierLibLinearTreeNode(HashSet<ClassifierLibLinearTreeNode> children, String label, Problem problem, Map<String, Integer> labelMapping, int treedepth, boolean leaf) {
		this.children = children;
		this.labelString = label;
		this.depth = treedepth;
		this.problem = problem;
		this.isLeafNode = leaf;
		this.labelMapping = labelMapping;
	}
	
	public boolean isLeafNode () {
		return isLeafNode;
	}
	
	public Map<String, Integer> getLabelMapping () {
		return labelMapping;
	}
	
	public Model getModel () {
		return model;
	}
	
	
	public Set<ClassifierLibLinearTreeNode> getChildren () {
		return this.children;
	}
	
	
	public void trainModelForNode (double C, double eps) {
		try {
			if (problem != null) {
			    Calendar cal = Calendar.getInstance();
			    long startTime = cal.getTimeInMillis();
				System.out.println("  [Training:] multi class svm for node: " + labelString + "; children num: " +
						children.size() + "; data num: " + problem.l + "; feature num: " + problem.n);
				
				SolverType solver = ClassifierConstant.solver; // 
				Parameter parameter = new Parameter(solver, C, eps);
				
				if (problem.l > 0) {
					Linear.disableDebugOutput();
					model = Linear.train(problem, parameter);
				}
				
				Calendar cal1 = Calendar.getInstance();
	    		long endTime = cal1.getTimeInMillis();
	    		long second = (endTime - startTime)/1000;
				System.out.println("  [Training:] finished," + " time: " + second + " seconds");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
