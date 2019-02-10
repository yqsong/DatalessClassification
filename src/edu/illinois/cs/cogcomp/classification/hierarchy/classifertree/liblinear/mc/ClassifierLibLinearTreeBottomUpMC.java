package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.mc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.liblinear.AbstractClassifierLibLinearTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.classificationinterface.InterfaceMultiClassClassificationTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelResultMC;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

/**
 * yqsong@illinois.edu
 */

public class ClassifierLibLinearTreeBottomUpMC extends AbstractClassifierLibLinearTree implements InterfaceMultiClassClassificationTree{
	
	private static final long serialVersionUID = 1L;

	public ClassifierLibLinearTreeBottomUpMC (String data) {
		super(data);
	}

	protected Model overallModel;
	
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
	        
			String outputLabelStr = "";
			double outputLabelScore = 0;
			
	        double[] decValues = new double[globalLeafLabelIDMap.size()];
	        double yTest = Linear.predictValues(overallModel, xTest, decValues);
	        int outputLabel = (int) yTest;

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
		double eps = 0.01;
		trainAllTreeNodes(eps);
	}

	public void trainAllTreeNodes(double eps) {
		System.out.println("[Training:] leaf nodes... ");
		//training 
		List<Double> yList = new ArrayList<Double>();;
		List<FeatureNode[]> xList = new ArrayList<FeatureNode[]>();
		
		HashMap<String, Integer> categorySizes = new HashMap<String, Integer>();

		for (int i = 0; i < globalLeafDocLines.size(); ++i) {
			String libSVMFormatStr = globalLeafDocLines.get(i);

			String[] tokens = libSVMFormatStr.split(" ");

			// initialize labal for libLinear
			yList.add((double)globalLeafLabelIDMap.get(tokens[0].trim()));
			
			if (categorySizes.containsKey(tokens[0].trim()) == false) {
				categorySizes.put(tokens[0].trim(), 0);
			}
			categorySizes.put(tokens[0].trim(), categorySizes.get(tokens[0].trim()) + 1);
			// initialize data for libLinear
			FeatureNode[] feature = new FeatureNode[tokens.length - 1];
			for (int j = 1; j < tokens.length; ++j) {
				String[] subTokens = tokens[j].trim().split(":");
				int index = Integer.parseInt(subTokens[0].trim());
				double value = Double.parseDouble(subTokens[1].trim());
				feature[j - 1] = new FeatureNode(index, value);
			}
			xList.add(feature);
		}
		
		Feature[][] xArray = new Feature[yList.size()][];
		double[] yArray = new double[yList.size()];
		for (int i = 0; i < yList.size(); ++i) {
			yArray[i] = yList.get(i);
			xArray[i] = xList.get(i);
		}
		Problem problem = new Problem();
		problem.l = yList.size(); // number of training examples
		problem.n = jlisData.getDictSize(); // number of features
		problem.x = xArray; // feature nodes
		problem.y = yArray; // target values
		
		for (String category : categorySizes.keySet()) {
			System.out.print("[" + category + ":" + categorySizes.get(category) + "],");
		}
		System.out.print(System.getProperty("line.separator").toString());
		
		try {
			if (problem != null) {
			    Calendar cal = Calendar.getInstance();
			    long startTime = cal.getTimeInMillis();
				System.out.println("  [Training:] svm for all leaves: " 
						+ globalLeafLabelIDMap.size() + "; data num: " + problem.l + "; feature num: " + problem.n);
				
				SolverType solver = ClassifierConstant.solver; // 
				Parameter parameter = new Parameter(solver, C, eps);
				Linear.disableDebugOutput();
				overallModel = Linear.train(problem, parameter);

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
