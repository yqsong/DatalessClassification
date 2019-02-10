package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.jlis.mc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.jlis.AbstractClassifierJLISTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.jlis.ClassifierJLISTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.jlis.LabeledMultiClassStructure;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiClassClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.indsup.mc.LabeledMulticlassData;

/**
 * yqsong@illinois.edu
 */

public class ClassifierJLISTreeTopDownMC extends AbstractClassifierJLISTree implements InterfaceMultiClassClassificationTree {
	
	private static final long serialVersionUID = 1L;

	public ClassifierJLISTreeTopDownMC (String data) {
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
		docLibSVMFormat = "nullLabel " + docLibSVMFormat;
		
		labelResult = retrieveLabel (docLibSVMFormat, root, labelResult);
		return labelResult;
	}
	
	public LabelResultMC retrieveLabel (String docLibSVMFormat, ClassifierJLISTreeNode rootNode, LabelResultMC labelResult) {

		ClassifierJLISTreeNode maxChild = null;
		Set<ClassifierJLISTreeNode> children = rootNode.getChildren();
		if (rootNode.getChildren().size() == 0) {
			labelResult.isToLeaf = true;
		} else {
			List<String> lines = new ArrayList<String>();
			lines.add(docLibSVMFormat);
			LabeledMulticlassData testRes = this.jlisData.readMultiClassDataLibSVMStr (lines, this.jlisData.getFeatureNum(), rootNode.getLabeledData().label_mapping);
			
			labelResult.isToLeaf = false;
			
			LabeledMultiClassStructure prediction = null;
			String outputLabelStr = "";
			double outputLabelScore = 0;
			try {
				prediction = (LabeledMultiClassStructure) 
						rootNode.getModel().s_finder.getBestStructure(rootNode.getModel().wv, testRes.sp.input_list.get(0));
				int outputLabel = prediction.output;
				outputLabelScore = prediction.score;
				for (String key : rootNode.getLabeledData().label_mapping.keySet()) {
					Integer value = rootNode.getLabeledData().label_mapping.get(key);
					if (outputLabel == value) {
						outputLabelStr = key;
					}
				}
				for (ClassifierJLISTreeNode child : children) {
					if (child.getLabelString().equals(outputLabelStr) == true) {
						maxChild = child;
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (maxChild != null) {
				labelResult.labels.add(new LabelKeyValuePair(maxChild.getDepth() + ":" + outputLabelStr, outputLabelScore));
				retrieveLabel (docLibSVMFormat, maxChild, labelResult);
			}
		}
		return labelResult;
	}




	
	

}
