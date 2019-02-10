package edu.illinois.cs.cogcomp.classification.densification.run;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.densification.representation.FileUtils;

public class Evaluate20NGSim {
	public static void main (String[] args) {
//		String inputFolder = "/shared/shelley/yqsong/data/20ngSim/output/concept1000/ESA-complex-tfidfVector/";
//		String inputFolder = "/shared/shelley/yqsong/data/20ngSim/output/concept1000/ESA-complex-setkernel-sourceType0-sig0.03/";
//		String inputFolder = "/shared/shelley/yqsong/data/20ngSim/output/concept1000/ESA-complex-maxmatching-symmetric-sourceType0-threshold0.85/";
		//String inputFolder = "/shared/shelley/yqsong/data/20ngSim/output/concept1000/ESA-complex-hungarian-sourceType0-threshold0.85-sigma0.0/";
		
		String inputFolder = "/shared/bronte/sling3/data/test/500ESA-complex-tfidfVector/";
		
		String inputFile = inputFolder + "rec.autos-rec.motorcycles-1-1-output.txt";
		System.out.println("[File]: " + inputFile);
		evaluation (inputFile);

//		inputFile = inputFolder + "rec.autos-rec.motorcycles-2-output.txt";
//		System.out.println("[File]: " + inputFile);
//		evaluation (inputFile);
//
//		inputFile = inputFolder + "rec.autos-rec.motorcycles-4-output.txt";
//		System.out.println("[File]: " + inputFile);
//		evaluation (inputFile);
//
//		inputFile = inputFolder + "rec.autos-rec.motorcycles-8-output.txt";
//		System.out.println("[File]: " + inputFile);
//		evaluation (inputFile);

		inputFile = inputFolder + "rec.autos-rec.motorcycles-16-1-output.txt";
		System.out.println("[File]: " + inputFile);
		evaluation (inputFile);
		
		inputFile = inputFolder + "rec.autos-sci.electronics-1-1-output.txt";
		System.out.println("[File]: " + inputFile);
		evaluation (inputFile);
		
//		inputFile = inputFolder + "rec.autos-sci.electronics-2-output.txt";
//		System.out.println("[File]: " + inputFile);
//		evaluation (inputFile);
//
//		inputFile = inputFolder + "rec.autos-sci.electronics-4-output.txt";
//		System.out.println("[File]: " + inputFile);
//		evaluation (inputFile);
//
//		inputFile = inputFolder + "rec.autos-sci.electronics-8-output.txt";
//		System.out.println("[File]: " + inputFile);
//		evaluation (inputFile);

		inputFile = inputFolder + "rec.autos-sci.electronics-16-1-output.txt";
		System.out.println("[File]: " + inputFile);
		evaluation (inputFile);

	}
	
	public static void evaluation (String inputFile) {
		List<String> goldLabels = new ArrayList<String>();
		List<HashMap<String, Double>> predMapList = new ArrayList<HashMap<String, Double>>();
		List<String> lineList = FileUtils.ReadWholeFileAsLines(inputFile);
		for (int i = 0; i < lineList.size(); ++i) {

			String line = lineList.get(i);
			if (line.equals("") == true)
				continue;
			
			String[] tokens = line.split("\t");
			if (tokens.length != 4) 
				continue;
			
			String docID = tokens[0];
			String trueLabel = tokens[1];
			String label1Pred = tokens[2];
			String label2Pred = tokens[3];
			
			HashMap<String, Double> labelMap = new HashMap<String, Double>();
			String[] tokens1 = label1Pred.split("--");
			String topic1 = tokens1[0];
			double score1 = Double.parseDouble(tokens1[1]);
			labelMap.put(topic1, score1);
			
			String[] tokens2 = label2Pred.split("--");
			String topic2 = tokens2[0];
			double score2 = Double.parseDouble(tokens2[1]);
			labelMap.put(topic2, score2);
			
			predMapList.add(labelMap);
			goldLabels.add(trueLabel);
		}
		
		evaluation (goldLabels, predMapList);
	}
	
	public static double evaluation (
			List<String> goldLabels, 
			List<HashMap<String, Double>> predMap) {
		
		HashSet<String> labelSet = new HashSet<String>();
		for (int i = 0; i < goldLabels.size(); ++i) {
			if (labelSet.contains(goldLabels.get(i)) == false) {
				labelSet.add(goldLabels.get(i));
			}
		}
		
		int count = 0;
		int correct = 0;
		
		count = 0;
		List<String> predLabelList = new ArrayList<String>();
		for (int i = 0; i < goldLabels.size(); ++i) {
			count++;
			
			HashMap<String, Double> predictionMap = predMap.get(i);
			double maxValue = 0;
			String predLabel = "";
			for (String label : predictionMap.keySet()) {
				if (predictionMap.get(label) > maxValue) {
					maxValue = predictionMap.get(label);
					predLabel = label;
				}
			}
			
			String trueLabel = goldLabels.get(i);
			if (predLabel.equals(trueLabel) == true) {
				correct++;
			}
			
			predLabelList.add(predLabel);
		}
		double acc = (double) correct / (double) count;
		System.out.println("  [Accuracy]: " + String.format("%.4f", acc));

		HashMap<String, Integer> tp = new HashMap<String, Integer>();
		HashMap<String, Integer> fp = new HashMap<String, Integer>();
		HashMap<String, Integer> fn = new HashMap<String, Integer>();
		
		for (String key : labelSet) {
			tp.put(key, 0);
			fp.put(key, 0);
			fn.put(key, 0);
		}
		for (String key : predLabelList) {
			if (tp.containsKey(key) == false) { 
				tp.put(key, 0);
				fp.put(key, 0);
				fn.put(key, 0);
			}
		}
		
		for (int i = 0; i < goldLabels.size(); ++i) {
			String labelName = predLabelList.get(i);
			String trueName = goldLabels.get(i);
			try {
				if (labelName.equals(trueName) == true) {
					tp.put(trueName, tp.get(trueName) + 1);
				} else {
					fp.put(labelName, fp.get(labelName) + 1);
					fn.put(trueName, fn.get(trueName) + 1);
				}
			} catch (Exception e) {
				System.out.println("True label: " + trueName + ", classified label: " + labelName);
				e.printStackTrace();
			}
		}
		
		double microF1 = 0;
		double macroF1 = 0;
		double tpAll = 0;
		double fpAll = 0; 
		double fnAll = 0;
		for (String key : labelSet) {
			double precision = (double) tp.get(key) / (tp.get(key) + fp.get(key) + Double.MIN_NORMAL);
			double recall = (double) tp.get(key) / (tp.get(key) + fn.get(key) + Double.MIN_NORMAL);
			double f1 = 2 * precision * recall / (precision + recall + Double.MIN_NORMAL);
			
			tpAll += tp.get(key);
			fpAll += fp.get(key);
			fnAll += fn.get(key);
			
			macroF1 += f1;
			
//			System.out.println("  [Label: " + key + "] precision: " + String.format("%.4f", precision) + ", recall: " + String.format("%.4f", recall) + ", f1: " + String.format("%.4f", f1));
		}
		double pAll = tpAll / (tpAll + fpAll);
		double rAll = tpAll / (tpAll + fnAll);
		microF1 = 2 * pAll * rAll / (pAll + rAll + Double.MIN_NORMAL);
		macroF1 /= labelSet.size();
		
		System.out.println("  [microF1]: " + String.format("%.4f", microF1) + ", [macroF1]: " + String.format("%.4f", macroF1));
		
		return acc;
	}
}
