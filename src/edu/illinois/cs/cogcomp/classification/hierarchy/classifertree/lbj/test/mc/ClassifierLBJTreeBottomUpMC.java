package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.mc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import LBJ2.classify.DiscretePrimitiveStringFeature;
import LBJ2.classify.FeatureVector;
import LBJ2.learn.Lexicon;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.AbstractClassifierLBJTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.ConfigurableClassifier;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiClassClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;

/**
 * yqsong@illinois.edu
 */

public class ClassifierLBJTreeBottomUpMC extends AbstractClassifierLBJTree implements InterfaceMultiClassClassificationTree {
	
	private static final long serialVersionUID = 1L;

	public ClassifierLBJTreeBottomUpMC (String data) {
		super(data);
		
		model = new ConfigurableClassifier(learningMethod);
		
		System.out.println("Debug model null");
	}

	protected ConfigurableClassifier model;

	@Override
	public LabelResultMC labelDocument(SparseVector docContent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public LabelResultMC labelDocument(String docContent) {
		LabelResultMC labelResult = new LabelResultMC();
		String docLibSVMFormat = this.jlisData.convertTestDocContentToTFIDF (docContent, true, true);

		try {
			
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

			FeatureVector vector =
			        new FeatureVector((Object[]) dataSample, this.model.getLexicon(), this.model.getLabelLexicon());
			vector.sort();
			
			String outputLabel = "";
			double outputLabelScore = 1;
			
//			ScoreSet scoreSet = model.scores(vector);
//			Score[] scoreArray = scoreSet.toArray();
//			for (int i = 0; i < scoreArray.length; ++i) {
//				if (outputLabelScore <  scoreArray[i].score) {
//					outputLabelStr = scoreArray[i].value;
//					outputLabelScore = scoreArray[i].score;
//				}
//			}
			
			outputLabel = model.discreteValue(dataSample);
			
			String outputLabelStr = "";
			for (String key : globalLeafLabelIDMap.keySet()) {
				Integer value = globalLeafLabelIDMap.get(key);
				if (outputLabel.equals(value + "")) {
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
		List<FeatureVector> vectorList = new ArrayList<FeatureVector>();
		List<Object> rawDataList = new ArrayList<Object>();

		Lexicon featureLexicon = jlisData.getGlobalLexicon();
		Lexicon labelLexicon = model.getLabelLexicon();
		
		Map<String, Integer> labelsMapping = new HashMap<String, Integer>();
		for (int i = 0; i < this.globalInverseLeafLabelIDMap.size(); ++i) {
			DiscretePrimitiveStringFeature feature = new DiscretePrimitiveStringFeature(
					"traininglabelpackage",
					"CorpusDataProcessing",
					this.globalInverseLeafLabelIDMap.get(i),
					this.globalInverseLeafLabelIDMap.get(i),
					(short) i,
					(short) globalInverseLeafLabelIDMap.size()
					);
			labelLexicon.lookup(feature, true);
		}


		for (int i = 0; i < globalLeafDocLines.size(); ++i) {
			String libSVMFormatStr = globalLeafDocLines.get(i);

			String[] tokens = libSVMFormatStr.split(" ");
			int[] indexArray = new int[tokens.length - 1];
			double[] valueArray = new double[tokens.length - 1];
			
			for (int j = 1; j < tokens.length; ++j) {
				String[] subTokens = tokens[j].trim().split(":");
				int index = Integer.parseInt(subTokens[0].trim());
				double value = Double.parseDouble(subTokens[1].trim());
				indexArray[j - 1] = index;
				valueArray[j - 1] = value;
			}

			int[] labelIndexArray = new int[1];
			double[] labelValueArray = new double[1];
			labelIndexArray[0] = globalLeafLabelIDMap.get(tokens[0].trim());
			labelValueArray[0] = 1;

			
			Object[] dataSample = new Object[4];
			dataSample[0] = indexArray;
			dataSample[1] = valueArray;
			dataSample[2] = labelIndexArray;
			dataSample[3] = labelValueArray;

			FeatureVector vector =
			        new FeatureVector((Object[]) dataSample, featureLexicon, labelLexicon);
			vector.sort();
			vectorList.add(vector);
			
			rawDataList.add(dataSample);
		}
		
		Object[] dataArray = new Object[vectorList.size()];
		FeatureVector[] vectorArray = new FeatureVector[vectorList.size()];
		for (int i = 0; i < vectorList.size(); ++i) {
			vectorArray[i] = vectorList.get(i);
			dataArray[i] = rawDataList.get(i);
		}
		
		try {
			if (vectorArray.length > 0) {
			    Calendar cal = Calendar.getInstance();
			    long startTime = cal.getTimeInMillis();
				System.out.println("  [Training:] lbj for all leaves: " 
						+ globalLeafLabelIDMap.size() + "; data num: " + vectorArray.length);
				

				model.learn(dataArray);
				
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
