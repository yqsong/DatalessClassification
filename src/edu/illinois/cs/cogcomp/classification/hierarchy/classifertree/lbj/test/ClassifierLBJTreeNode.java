package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import LBJ2.classify.FeatureVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.AbstractTreeNode;

/**
 * yqsong@illinois.edu
 */

public class ClassifierLBJTreeNode  extends AbstractTreeNode {

	private Set<ClassifierLBJTreeNode> children;
	private  FeatureVector[] labeledData;
	private ConfigurableClassifier model;
	private boolean isLeafNode;
	private Map<String, Integer> labelMapping;
	private Object[] rawData;

	ClassifierLBJTreeNode(HashSet<ClassifierLBJTreeNode> children, String label, String learningMethod, int depth, boolean leaf) {
		this.children = children;
		this.labelString = label;
		this.depth = depth;
		this.isLeafNode = leaf;
			model = new ConfigurableClassifier("naiveBayes");
			
//			model.setExtractor(model);
//			Classifier extractor = model.getExtractor();//ClassUtils.getClassifier(model.name);
//			if (extractor != null) {
//				System.out.println("    [Debug Training:] Extractor containingPackage: " + extractor.containingPackage + 
//						", name: " + extractor.name + 
//						", getInputType(): " + extractor.getInputType() + 
//						", getOutputType(): " + extractor.getOutputType() 
//						);
//			}
			
			
//			model.setLabeler(model);
//			Classifier labeler = model.getLabeler();//ClassUtils.getClassifier(model.name);
//			model.setLabeler(labeler);
//			if (labeler != null) {
//				System.out.println("    [Debug Training:] Labler containingPackage: " + labeler.containingPackage + 
//						", name: " + labeler.name + 
//						", getInputType(): " + labeler.getInputType() + 
//						", getOutputType(): " + labeler.getOutputType() 
//						);
//			}
			  
	}
	
	public void setLabelMapping (Map<String, Integer> labelMapping) {
		this.labelMapping = labelMapping;
	}
	
	public void setTrainingData (FeatureVector[] data, Object[] rawData) {
		this.labeledData = data;
		this.rawData = rawData;
	}
	
	public boolean isLeafNode () {
		return isLeafNode;
	}
	
	public  FeatureVector[] getLabeledData () {
		return labeledData;
	}
	
	public Map<String, Integer> getLabelMapping () {
		return labelMapping;
	}

	public ConfigurableClassifier getModel () {
		return model;
	}
	
	public Set<ClassifierLBJTreeNode> getChildren () {
		return this.children;
	}
	
	public void trainModelForNode (double C, int nThreads) {
		try {
			if (labeledData != null) {
			    Calendar cal = Calendar.getInstance();
			    long startTime = cal.getTimeInMillis();
				System.out.println("  [Training:] lbj for node: " + labelString + "; children num: " +
						children.size() + "; data num: " + labeledData.length);
				
				model.learn(rawData);
				
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
