package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.jlis.mc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.jlis.AbstractClassifierJLISTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.jlis.LabeledMultiClassStructure;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiClassClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.indsup.mc.LabeledMulticlassData;
import edu.illinois.cs.cogcomp.indsup.mc.MultiClassTrainer;
import edu.illinois.cs.cogcomp.indsup.mc.MulticlassModel;

/**
 * yqsong@illinois.edu
 */

public class ClassifierJLISTreeBottomUpMC extends AbstractClassifierJLISTree implements InterfaceMultiClassClassificationTree {
	
	private static final long serialVersionUID = 1L;

	public ClassifierJLISTreeBottomUpMC (String data) {
		super(data);
	}

	protected MulticlassModel overallModel;
	
	@Override
	public LabelResultMC labelDocument(SparseVector docContent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LabelResultMC labelDocument(String docContent) {
		LabelResultMC labelResult = new LabelResultMC();
		String docLibSVMFormat = this.jlisData.convertTestDocContentToTFIDF (docContent, true, true);
		docLibSVMFormat = "nullLabel " + docLibSVMFormat;
		
		
		List<String> lines = new ArrayList<String>();
		lines.add(docLibSVMFormat);
		LabeledMulticlassData testRes = this.jlisData.readMultiClassDataLibSVMStr (lines, this.jlisData.getFeatureNum(), globalLeafLabelIDMap);
		
		labelResult.isToLeaf = false;
		
		LabeledMultiClassStructure prediction = null;
		String outputLabelStr = "";
		double outputLabelScore = 0;
		try {
			prediction = (LabeledMultiClassStructure) 
					overallModel.s_finder.getBestStructure(overallModel.wv, testRes.sp.input_list.get(0));
			int outputLabel = prediction.output;
			outputLabelScore = prediction.score;
			for (String key : globalLeafLabelIDMap.keySet()) {
				Integer value = globalLeafLabelIDMap.get(key);
				if (outputLabel == value) {
					outputLabelStr = key;
				}
			}

			labelResult.isToLeaf = true;
			String labelStr = outputLabelStr;
			while (labelStr != null) {
				labelResult.labels.add(new LabelKeyValuePair(labelStr, outputLabelScore));
				labelStr = treeLabelData.getTreeParentIndex().get(labelStr);
			}
			
			for (int i = 0; i < labelResult.labels.size(); ++i) {
				labelResult.labels.get(i).setLabel( (labelResult.labels.size() - i - 1) + ":" + labelResult.labels.get(i).getLabel() );
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return labelResult;
	}

	
	@Override
	public void trainAllTreeNodes() {
		System.out.println("[Training:] leaf nodes... ");
		//training 
		LabeledMulticlassData labeledData = 
				this.jlisData.readMultiClassDataLibSVMStr(globalLeafDocLines, this.jlisData.getFeatureNum(), globalLeafLabelIDMap);
		try {
			if (labeledData != null) {
			    Calendar cal = Calendar.getInstance();
			    long startTime = cal.getTimeInMillis();
				System.out.println("  [Training:] multi class svm for all leaves: " 
						+ globalLeafLabelIDMap.size() + "; data num: " + labeledData.sp.input_list.size());
				
				overallModel = MultiClassTrainer.trainMultiClassModel(C, nThreads, labeledData);
				
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
