package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.jlis;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.AbstractTreeNode;
import edu.illinois.cs.cogcomp.indsup.mc.LabeledMulticlassData;
import edu.illinois.cs.cogcomp.indsup.mc.MultiClassTrainer;
import edu.illinois.cs.cogcomp.indsup.mc.MulticlassModel;

/**
 * yqsong@illinois.edu
 */

public class ClassifierJLISTreeNode  extends AbstractTreeNode {

	private Set<ClassifierJLISTreeNode> children;
	private LabeledMulticlassData labeledData;
	private MulticlassModel model;
	private boolean isLeafNode;
	
	ClassifierJLISTreeNode(HashSet<ClassifierJLISTreeNode> children, String label, LabeledMulticlassData labeledData, int depth, boolean leaf) {
		this.children = children;
		this.labelString = label;
		this.depth = depth;
		this.labeledData = labeledData;
		this.isLeafNode = leaf;
	}
	
	public boolean isLeafNode () {
		return isLeafNode;
	}
	
	public LabeledMulticlassData getLabeledData () {
		return labeledData;
	}
	
	public MulticlassModel getModel () {
		return model;
	}
	
	public Set<ClassifierJLISTreeNode> getChildren () {
		return this.children;
	}
	
	public void trainModelForNode (double C, int nThreads) {
		try {
			if (labeledData != null) {
			    Calendar cal = Calendar.getInstance();
			    long startTime = cal.getTimeInMillis();
				System.out.println("  [Training:] multi class svm for node: " + labelString + "; children num: " +
						children.size() + "; data num: " + labeledData.sp.input_list.size());
				
				model = MultiClassTrainer.trainMultiClassModel(C, nThreads, labeledData);
				
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
