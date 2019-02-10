package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.mc;

import java.util.Random;
import java.util.Set;

import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.AbstractClassifierLibLinearTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.ClassifierLibLinearTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiClassClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;

/**
 * yqsong@illinois.edu
 */

public class ClassifierLibLinearTreeTopDownMC extends AbstractClassifierLibLinearTree implements InterfaceMultiClassClassificationTree{
	
	private static final long serialVersionUID = 1L;

	public ClassifierLibLinearTreeTopDownMC (String data) {
		super(data);
	}
	
	@Override
	public void trainAllTreeNodes(){
		System.out.println("[Training:] all nodes... ");
		//training 
		for (int i = 0; i < this.allNodeList.size(); ++i) {
			allNodeList.get(i).trainModelForNode(C, nThreads);
		}
	}
	
	////////////////////////////////////////////////////////////////
	// Test data
	////////////////////////////////////////////////////////////////
	protected Random random = new Random();
	
	@Override
	public LabelResultMC labelDocument(SparseVector docContent) {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public LabelResultMC labelDocument (String docContent) {
		LabelResultMC labelResult = new LabelResultMC();
		String docLibSVMFormat = this.jlisData.convertTestDocContentToTFIDF (docContent, true, true);
		String[] tokens = docLibSVMFormat.trim().split(" ");
		// initialize data for libLinear
		FeatureNode[] xTest = new FeatureNode[tokens.length];
		for (int j = 0; j < tokens.length; ++j) {
			String[] subTokens = tokens[j].trim().split(":");
			if (subTokens.length < 2) {
				xTest[j] = new FeatureNode(1, 0);
				continue;
			}
			int index = Integer.parseInt(subTokens[0].trim());
			double value = Double.parseDouble(subTokens[1].trim());
			xTest[j] = new FeatureNode(index, value);
		}
		
		labelResult = retrieveLabel (xTest, root, labelResult);
		return labelResult;
	}
	
	public LabelResultMC retrieveLabel (FeatureNode[] xTest, ClassifierLibLinearTreeNode rootNode, LabelResultMC labelResult) {

		ClassifierLibLinearTreeNode maxChild = null;
		Set<ClassifierLibLinearTreeNode> children = rootNode.getChildren();
		if (rootNode.getChildren().size() == 0) {
			labelResult.isToLeaf = true;
		} else {
	        
			String outputLabelStr = "";
			double outputLabelScore = 0;
			int outputLabel = 0;
	        double[] decValues = new double[rootNode.getLabelMapping().size()];
	        if (rootNode.getModel() == null) {
	        	for (int i = 0; i < decValues.length; ++i) {
	        		decValues[i] = random.nextDouble();
	        	}
	        } else {
		        double yTest = Linear.predictValues(rootNode.getModel(), xTest, decValues);
		        outputLabel = (int) yTest;
		        outputLabelScore = decValues[outputLabel];
	        }
	        

			for (String key : rootNode.getLabelMapping().keySet()) {
				Integer value = rootNode.getLabelMapping().get(key);
				if (outputLabel == value) {
					outputLabelStr = key;
				}
			}
			for (ClassifierLibLinearTreeNode child : children) {
				if (child.getLabelString().equals(outputLabelStr) == true) {
					maxChild = child;
				}
			}
			if (maxChild != null) {
				labelResult.labels.add(new LabelKeyValuePair(maxChild.getDepth() + ":" + outputLabelStr, outputLabelScore));
				retrieveLabel (xTest, maxChild, labelResult);
			}
		}
		return labelResult;
	}



	
	

}
