package edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test;
import java.io.PrintStream;
import java.util.HashMap;

import LBJ2.classify.FeatureVector;
import LBJ2.classify.Score;
import LBJ2.classify.ScoreSet;
import LBJ2.learn.AdaBoost;
import LBJ2.learn.Learner;
import LBJ2.learn.Lexicon;
import LBJ2.learn.PassiveAggressive;
import LBJ2.learn.SparseAveragedPerceptron;
import LBJ2.learn.SparseConfidenceWeighted;
import LBJ2.learn.SparseMIRA;
import LBJ2.learn.SupportVectorMachine;
import LBJ2.util.FVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.classifertree.lbj.test.NaiveBayes.NaiveBayesVector;

/**
 * yqsong@illinois.edu
 */

public class ConfigurableClassifier extends Learner
{
	private static final long serialVersionUID = -7305926849227764363L;
	public Learner learner;

	public ConfigurableClassifier (String learningMethod) {

		if (learningMethod.equalsIgnoreCase("NaiveBayes")) {
			learner = new NaiveBayes();
		}

		if (learningMethod.equalsIgnoreCase("SupportVectorMachine")) {
			learner = new SupportVectorMachine(1000);
		}

		if (learningMethod.equalsIgnoreCase("AdaBoost")) {
			learner = new AdaBoost();
		}

//		if (learningMethod.equalsIgnoreCase("LinearThresholdUnit")) {
//			leaner = new LinearThresholdUnit();
//		}

		if (learningMethod.equalsIgnoreCase("PassiveAggressive")) {
			learner = new PassiveAggressive();
		}

		if (learningMethod.equalsIgnoreCase("SparseConfidenceWeighted")) {
			learner = new SparseConfidenceWeighted();
		}

		if (learningMethod.equalsIgnoreCase("SparseMIRA")) {
			learner = new SparseMIRA();
		}

		if (learningMethod.equalsIgnoreCase("SparseAveragedPerceptron")) {
			learner = new SparseAveragedPerceptron (0.1, 3.5);
		}

	    setLabeler(learner);
	    setExtractor(learner);
	    lexicon = new Lexicon();
	    labelLexicon = new Lexicon();
	    predictions = new FVector();
	    learner.setLexicon(lexicon);
	    learner.setLabelLexicon(labelLexicon);
	    
	}
	
	@Override
	public FeatureVector classify(Object o) {
		return learner.classify(o);
	}

//	public void learn(FeatureVector vector) {
//		Classifier saveExtractor = getExtractor();
//		Classifier saveLabeler = getLabeler();
//		leaner.setExtractor(leaner);
//		leaner.setLabeler(leaner);
//
//		leaner.learn((Object) vector);
//
//		leaner.setExtractor(saveExtractor);
//		leaner.setLabeler(saveLabeler);
//	}
//
//	public void learn(FeatureVector[] examples) {
//		for (int i = 0; i < examples.length; ++i)
//			leaner.learn(examples[i]);
//		doneLearning();
//	}
//	  
//	public void learn(Object[] examples) {
//		for (int i = 0; i < examples.length; ++i)
//			leaner.learn(examples[i]);
//		doneLearning();
//	}
	
	  
	public void learn(Object[] examples) {
		for (int i = 0; i < examples.length; ++i) {
			Object[] example = (Object[]) examples[i];
			learn((int[])example[0], (double[])example[1], (int[])example[2], (double[])example[3]);
		}
		doneLearning();
	}
	
	@Override
	public void learn(int[] exampleFeatures, double[] exampleValues,
			int[] exampleLabels, double[] labelValues) {
		learner.learn(exampleFeatures, exampleValues, exampleLabels, labelValues);
	}

	@Override
	public FeatureVector classify(int[] exampleFeatures, double[] exampleValues) {
		// TODO Auto-generated method stub
		return learner.classify(exampleFeatures, exampleValues);
	}

	@Override
	public ScoreSet scores(int[] exampleFeatures, double[] exampleValues) {
		// TODO Auto-generated method stub
		return learner.scores(exampleFeatures, exampleValues);
	}
	
	public ScoreSet scoresExplicit(int[] exampleFeatures, double[] exampleValues) {
		    ScoreSet s = new ScoreSet();

		    for (int l = 0; l < ((NaiveBayes) learner).network.size(); l++) {
		      NaiveBayesVector vector = (NaiveBayesVector) ((NaiveBayes) learner).network.get(l);
		      double score = vector.dot(exampleFeatures, exampleValues);
//		      System.out.println("      [Debug:] NaiveBayes Classifier Label Lexicon: " + (labelLexicon==null));
		      s.put(labelLexicon.lookupKey(l).getStringValue(), score);
		    }

		    Score[] original = s.toArray();
		    ScoreSet result = new ScoreSet();

		    // This code would clearly run quicker if you computed each exp(score)
		    // ahead of time, and divided them each by their sum.  However, each score
		    // is likely to be a very negative number, so exp(score) may not be
		    // numerically stable.  Subtracting two scores, however, hopefully leaves
		    // you with a "less negative" number, so exp applied to the subtraction
		    // hopefully behaves better.

//		    for (int i = 0; i < original.length; ++i) {
//		      double score = 1;
//		      
//		      if (Double.isNaN(original[i].score)) {
//		    	  result.put(original[i].value, 0);
//		      } else {
//		    	  for (int j = 0; j < original.length; ++j) {
//		    		  if (i == j) continue;
//				        
//		    		  if (Double.isNaN(original[j].score)) {
//		    			  score += 0;
//		    		  } else {
//		    			  score += Math.exp(original[j].score - original[i].score);
//		    		  }
//		    	  }
//		    	  result.put(original[i].value, 1 / score);
//		      }
//		    
//		    }
		    
		    double sum = 0;
		    HashMap<String, Double> scoreHash = new HashMap<String, Double>();
		    for (int i = 0; i < original.length; ++i) {
		    	double score = 1;
		    	
		    	if (Double.isNaN(original[i].score)) {
//		    		result.put(original[i].value, 0);
		    		scoreHash.put(original[i].value, 0.0);
		    	} else {
		    		for (int j = 0; j < original.length; ++j) {
		    			if (i == j) continue;
					        
		    			if (Double.isNaN(original[j].score)) {
		    				score += 0;
		    			} else {
		    				score += Math.exp(original[j].score - original[i].score);
		    			}
		    		}
		    		
//		    		score += Math.exp(0 - original[i].score);
		    		
//		    		result.put(original[i].value, 1 / score);
		    		scoreHash.put(original[i].value, 1 / score);
		    		
		    		sum += score;
		    	}
			    
		    }
		    for (String value : scoreHash.keySet()) {
		    	result.put(value, scoreHash.get(value));
		    }

		    return result;
	}
	
	public String discreteValue(Object example) {
		Object[] exampleArray = getExampleArray(example, false);
		return
				discreteValue((int[]) exampleArray[0], (double[]) exampleArray[1]);
	}
	
	public String discreteValue(int[] exampleFeatures, double[] exampleValues) {
		return learner.discreteValue(exampleFeatures, exampleValues);
	}


	@Override
	public void write(PrintStream out) {
		learner.write(out);
	}

}