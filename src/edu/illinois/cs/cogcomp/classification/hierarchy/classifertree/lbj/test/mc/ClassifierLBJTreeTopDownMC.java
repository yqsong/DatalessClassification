package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.mc;

import java.util.Set;

import LBJ2.classify.FeatureVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.AbstractClassifierLBJTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.ClassifierLBJTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiClassClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;

/**
 * yqsong@illinois.edu
 */

public class ClassifierLBJTreeTopDownMC extends AbstractClassifierLBJTree implements InterfaceMultiClassClassificationTree {
	
	private static final long serialVersionUID = 1L;

	public ClassifierLBJTreeTopDownMC (String data) {
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
	@Override
	public LabelResultMC labelDocument(SparseVector docContent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public LabelResultMC labelDocument (String docContent) {
		LabelResultMC labelResult = new LabelResultMC();
		String docLibSVMFormat = this.jlisData.convertTestDocContentToTFIDF (docContent, true, true);

		// initialize data for lbj
		String[] tokens = docLibSVMFormat.split(" ");
		int[] indexArray = new int[tokens.length];
		double[] valueArray = new double[tokens.length];
		for (int i = 0; i < tokens.length; ++i) {
			String[] subTokens = tokens[i].trim().split(":");
			int index = Integer.parseInt(subTokens[0].trim());
			double value = Double.parseDouble(subTokens[1].trim());
	    	String indexStr = index + "";
	    	String valueStr = value + "";
			indexArray[i] = index;
			valueArray[i] = value;
		}

		int[] labelIndexArray = new int[1];
		double[] labelValueArray = new double[1];
		labelIndexArray[0] = 0;
		labelValueArray[0] = 0;
		
		Object[] dataSample = new Object[4];
		dataSample[0] = indexArray;
		dataSample[1] = valueArray;
		dataSample[2] = labelIndexArray;
		dataSample[3] = labelValueArray;


        
		labelResult = retrieveLabel (dataSample, root, labelResult);
		return labelResult;
	}
	
	public LabelResultMC retrieveLabel (Object[] dataSample, ClassifierLBJTreeNode rootNode, LabelResultMC labelResult) {

		ClassifierLBJTreeNode maxChild = null;
		Set<ClassifierLBJTreeNode> children = rootNode.getChildren();
		if (rootNode.getChildren().size() == 0) {
			labelResult.isToLeaf = true;
		} else {
			String outputLabel = "";
			double outputLabelScore = 1;
			
//			ScoreSet scoreSet = rootNode.getModel().scores(vector);
//			Score[] scoreArray = scoreSet.toArray();
//			for (int i = 0; i < scoreArray.length; ++i) {
//				if (outputLabelScore <  scoreArray[i].score) {
//					outputLabelStr = scoreArray[i].value;
//					outputLabelScore = scoreArray[i].score;
//				}
//			}
			
			FeatureVector vector =
			        new FeatureVector((Object[]) dataSample, rootNode.getModel().getLexicon(), rootNode.getModel().getLabelLexicon());
			vector.sort();
			
			outputLabel = rootNode.getModel().discreteValue(dataSample);

			String outputLabelStr = "";
			for (String key : rootNode.getLabelMapping().keySet()) {
				Integer value = rootNode.getLabelMapping().get(key);
				if (outputLabel.equals(value + "")) {
					outputLabelStr = key;
				}
			}

			
			for (ClassifierLBJTreeNode child : children) {
				if (child.getLabelString().equals(outputLabelStr) == true) {
					maxChild = child;
				}
			}
			if (maxChild != null) {
				labelResult.labels.add(new LabelKeyValuePair(maxChild.getDepth() + ":" + outputLabelStr, outputLabelScore));
				retrieveLabel (dataSample, maxChild, labelResult);
			}
		}
		return labelResult;
	}



	

}
