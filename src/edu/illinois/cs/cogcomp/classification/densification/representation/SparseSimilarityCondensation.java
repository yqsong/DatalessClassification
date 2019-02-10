package edu.illinois.cs.cogcomp.classification.densification.representation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.SingularValueDecomposition;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.decomposition.DenseDoubleSingularValueDecomposition;
import cern.colt.matrix.tdouble.impl.DenseColumnDoubleMatrix2D;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.StopWords;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.DiskBasedWordEmbedding;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.MemoryBasedJohnVec;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.MemoryBasedWordEmbedding;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.WordEmbeddingInterface;
import edu.illinois.cs.cogcomp.descartes.retrieval.simple.Searcher;

public class SparseSimilarityCondensation {
	private static IndexReader contentReader = null;

	public static HashMap<String, String> pageTitleIDMap = new HashMap<String, String>();
	public static HashMap<String, String> pageIDTitleMap = new HashMap<String, String>();

	public static WordEmbeddingInterface word2vecEmbd = null;

    HashMap<String, DenseVector> wikiIDVectorMap = new HashMap<String, DenseVector>();

	public String selectedMatchingType = "setKernel1";
	public static String[] matchingTypes = new String[] {"maxMatching", "setKernel1", "setKernel2", "matrixTrace", "hungarian"}; 
    
    public double similarityThreshold = 0.0; // threshold for max matching
    public double sigma2 = 0.03; // sigma for set kernel; average matching 
    public int topKSV = 10;
    
	public SparseSimilarityCondensation (String matchingType, double threshold, double sig2, boolean onTheFlyTitleVectors) {
		if (onTheFlyTitleVectors) {
			word2vecEmbd = new DiskBasedWordEmbedding();
			initializationWithVectorsOnTheFly(matchingType, threshold, sig2);
		} else {
			initializationWithPrecomputedVectors (matchingType, threshold, sig2);
		}
	}
	
	public SparseSimilarityCondensation (String matchingType, double threshold, double sig2, List<String> titleList, WordEmbeddingInterface embedding) {
		word2vecEmbd = embedding;
		initializationWithVectorsOnTheFly (matchingType, threshold, sig2, titleList);
	}
	
	public SparseSimilarityCondensation (String matchingType, double threshold, double sig2, int count, int type) {
		word2vecEmbd = new DiskBasedWordEmbedding(count,type);
		initializationWithVectorsOnTheFly(matchingType, threshold, sig2);
	}
	
    // load precomputed word2vec title representation
	public void initializationWithPrecomputedVectors (String matchingType, double threshold, double sig2) {
		similarityThreshold = threshold;
		sigma2 = sig2;
		selectedMatchingType = matchingType;
		
		String stopWordsFile = "data/rcvTest/english.stop";
		StopWords.rcvStopWords = StopWords.readStopWords (stopWordsFile);

		System.out.print("Using word2vector");
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(DatalessResourcesConfig.wikiTitleW2V));        
			String line = null;
			try {
				int count=0;
				while ((line = reader.readLine()) != null) {
					
					String[] arr = line.split("\t");
					String wikiID = arr[0];
					String wikiTitle=arr[1];
					double[] vec = new double[200];
					for(int i=2; i < arr.length; i++) {
						vec[i-2] = Double.parseDouble(arr[i]);
					}
					DenseVector vector = new DenseVector(vec);
					wikiIDVectorMap.put(wikiID, vector);
					if (count%50000==0)
					 System.out.println("Cached wiki tilte "+count);
					count++;
				    }               
				}
			catch (Exception e) {
				e.printStackTrace();
		 	}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.print("Finished reading word2vector");

		if (pageTitleIDMap.size() == 0)  {
			
			getLegitimatePageIDs (DatalessResourcesConfig.pageIDMapping);			
		}

	}
	
	// load word2vec title representation on the fly
	public void initializationWithVectorsOnTheFly (String matchingType, double threshold, double sig2) {
		
		similarityThreshold = threshold;
		sigma2 = sig2;
		selectedMatchingType = matchingType;
		
		String stopWordsFile = "data/rcvTest/english.stop";
		StopWords.rcvStopWords = StopWords.readStopWords (stopWordsFile);

		String contentIndexPath = DatalessResourcesConfig.complexWikiOriginalDocumentIndex;//"/shared/shelley/yqsong/data/wikipedia_new/WikiLuceneIndex_word500_link30";
		Directory fsdir;
		try {
			fsdir = FSDirectory.open(new File(contentIndexPath));
			contentReader = IndexReader.open(fsdir,true);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		

		Calendar cal = Calendar.getInstance();
		long startTime = cal.getTimeInMillis();

		int maxid = contentReader.maxDoc();
			
		for(int i = 0; i < maxid; i++){
		    	
		   if (i % 10000 == 0) {
		    	System.out.print (" Cached: " + i + " titles.. " );
		    		
		    	Calendar cal1 = Calendar.getInstance();
		    	long endTime = cal1.getTimeInMillis();
		   		long second = (endTime - startTime)/1000;
		   		System.out.println("Elipsed time: " + second + " seconds");
		   	}
		   	
			String wikiTitle = "";
			String wikiID = "";
			try {
			   	if(!contentReader.isDeleted(i)){
			   		wikiTitle = contentReader.document(i).getField("title").stringValue();
		    		wikiID = contentReader.document(i).getField("id").stringValue();
	    		}
			    	
			   	if (wikiIDVectorMap.containsKey(wikiID) == false) {
					double[] densevector = word2vecEmbd.getDenseVectorBasedonSegmentation(wikiTitle, false);
					DenseVector vector = new DenseVector(densevector);

					wikiIDVectorMap.put(wikiID, vector);
			   	}

		    }
		    catch(Exception e){
	    		e.printStackTrace();
	    		continue;
	    	}
		}

		if (pageTitleIDMap.size() == 0)  {
			getLegitimatePageIDs (DatalessResourcesConfig.pageIDMapping);					
		}
	}
	
	
	public void initializationWithVectorsOnTheFly (String matchingType, double threshold, double sig2, List<String> titleList) {
		
		similarityThreshold = threshold;
		sigma2 = sig2;
		selectedMatchingType = matchingType;
		
		Calendar cal = Calendar.getInstance();
		long startTime = cal.getTimeInMillis();

		if (pageTitleIDMap.size() == 0)  {
			getLegitimatePageIDs (DatalessResourcesConfig.pageIDMapping);					
		}
		
		for(int i = 0; i < titleList.size(); i++){
		    	
		   if (i % 10000 == 0) {
		    	System.out.print (" Cached: " + i + " titles.. " );
		    		
		    	Calendar cal1 = Calendar.getInstance();
		    	long endTime = cal1.getTimeInMillis();
		   		long second = (endTime - startTime)/1000;
		   		System.out.println("Elipsed time: " + second + " seconds");
		   	}
		   	
			String wikiTitle = titleList.get(i);
			
			String wikiID = wikiTitle;
			if (pageTitleIDMap.containsKey(wikiTitle)) {
				wikiID = pageTitleIDMap.get(wikiTitle);
			} else {
				pageTitleIDMap.put(wikiTitle, wikiTitle);
				pageIDTitleMap.put(wikiTitle, wikiTitle);
			}
			
			if (wikiIDVectorMap.containsKey(wikiTitle) == false) {
				double[] densevector = word2vecEmbd.getDenseVectorBasedonSegmentation(wikiTitle, false);
				DenseVector vector = new DenseVector(densevector);

				wikiIDVectorMap.put(wikiID, vector);
		   	}
		}


	}
	
	//"/shared/bronte/sling3/data/test/w2v.txt"
	public void dumpvector(String outputFile) throws IOException{
		String stopWordsFile = "data/rcvTest/english.stop";
		StopWords.rcvStopWords = StopWords.readStopWords (stopWordsFile);

		String contentIndexPath = DatalessResourcesConfig.complexWikiOriginalDocumentIndex;//"/shared/shelley/yqsong/data/wikipedia_new/WikiLuceneIndex_word500_link30";
		Directory fsdir;
		try {
			fsdir = FSDirectory.open(new File(contentIndexPath));
			contentReader = IndexReader.open(fsdir,true);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		FileWriter writer1 = new FileWriter(outputFile);
		
		word2vecEmbd = new DiskBasedWordEmbedding();

		Calendar cal = Calendar.getInstance();
		long startTime = cal.getTimeInMillis();

		int maxid = contentReader.maxDoc();
			
		for(int i = 0; i < maxid; i++){
		    	
		   if (i % 10000 == 0) {
		    	System.out.print (" Cached: " + i + " titles.. " );
		    		
		    	Calendar cal1 = Calendar.getInstance();
		    	long endTime = cal1.getTimeInMillis();
		   		long second = (endTime - startTime)/1000;
		   		System.out.println("Elipsed time: " + second + " seconds");
		   	}
		   	
			String wikiTitle = "";
			String wikiID = "";
			try {
			   	if(!contentReader.isDeleted(i)){
			   		wikiTitle = contentReader.document(i).getField("title").stringValue();
		    		wikiID = contentReader.document(i).getField("id").stringValue();
	    		}
			    	
				double[] densevector = word2vecEmbd.getDenseVectorBasedonSegmentation(wikiTitle, false);
				DenseVector vector = new DenseVector(densevector);
				//titleVectorMap.put(wikiID, vector);
				
				writer1.write(wikiID + "\t" + wikiTitle + "\t");
				for (int j = 0; j < vector.vector.length; ++j) {
							writer1.write(vector.vector[j] + "\t");
						
				}
				writer1.write(ClassifierConstant.systemNewLine);
			    	
		    }
		    catch(Exception e){
	    		e.printStackTrace();
	    		continue;
	    	}
		}
		writer1.flush();
		writer1.close();
		
		FileWriter writer2 = new FileWriter("/shared/bronte/sling3/data/test/john.txt");
		MemoryBasedJohnVec johnVec = new MemoryBasedJohnVec();

		cal = Calendar.getInstance();
		startTime = cal.getTimeInMillis();

		maxid = contentReader.maxDoc();
			
		for(int i = 0; i < maxid; i++){
		    	
		    if (i % 10000 == 0) {
		    	System.out.print (" Cached: " + i + " titles.. " );
		    	
		    	Calendar cal1 = Calendar.getInstance();
		    	long endTime = cal1.getTimeInMillis();
		    	long second = (endTime - startTime)/1000;
		    	System.out.println("Elipsed time: " + second + " seconds");
		    }
		    	
			String wikiTitle = "";
			String wikiID = "";
		    try {
			    if(!contentReader.isDeleted(i)){
			    	wikiTitle = contentReader.document(i).getField("title").stringValue();
			    	wikiID = contentReader.document(i).getField("id").stringValue();
		    	}
			    	
				double[] densevector = johnVec.getDenseVector(wikiTitle);
				DenseVector vector = new DenseVector(densevector);

				//titleVectorMap.put(wikiID, vector);
				
				writer2.write(wikiID + "\t" + wikiTitle + "\t");
				
				for (int j = 0; j < vector.vector.length; ++j) {
					writer2.write(vector.vector[j] + "\t");
				}
				writer2.write(ClassifierConstant.systemNewLine);
		    }
		    catch(Exception e){
	    		e.printStackTrace();
	    		continue;
	    	}
		}
		writer2.flush();
		writer2.close();
		
	}

	public void getLegitimatePageIDs (String pageLengthPath) {
		try {
			FileReader reader = new FileReader(pageLengthPath);
			BufferedReader br = new BufferedReader(reader);
			
			String line = "";
			int count = 0;
			while ((line = br.readLine()) != null) {
				
				if (count % 100000 == 0) {
					System.out.println("Processed title " + count + " lines...");
				}
				count++;
				
				String[] tokens = line.split("\t");
				if (tokens.length != 2)
					continue;
				
				String pageID = tokens[0];
				String title = tokens[1];
				pageTitleIDMap.put(title.trim(), pageID);	
				pageIDTitleMap.put(pageID, title.trim());
			}
			br.close();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished reading");
		
		
	}
	
	public DenseVector getDenseVectorofTitle (String title) {
		DenseVector densevector = new DenseVector(new double[1]);
		
		if (wikiIDVectorMap.containsKey(title)) {
			densevector = wikiIDVectorMap.get(title);
		}
		
		return densevector;
	}
	
	public double similarityWithMaxMatching (HashMap<String, Double> vector1, HashMap<String, Double> vector2, double norm1, double norm2) {
		
		List<String> keyList1 = new ArrayList<String>(vector1.keySet());
		List<String> keyList2 = new ArrayList<String>(vector2.keySet());
		
		List<DenseVector> denseVecList1 = new ArrayList<DenseVector>();
		List<DenseVector> denseVecList2 = new ArrayList<DenseVector>();
		
		for (int i = 0; i < keyList1.size(); ++i) {
			String id = keyList1.get(i);
			if (pageTitleIDMap.containsKey(id)) {
				id = pageTitleIDMap.get(id);
			}
			if (pageIDTitleMap.containsKey(id)) {
				DenseVector vector = getDenseVectorofTitle (id);
				denseVecList1.add(vector);
			}
		}
		
		for (int i = 0; i < keyList2.size(); ++i) {
			String id = keyList2.get(i);
			if (pageTitleIDMap.containsKey(id)) {
				id = pageTitleIDMap.get(id);
			}
			if (pageIDTitleMap.containsKey(id)) {
				DenseVector vector = getDenseVectorofTitle (id);
				denseVecList2.add(vector);
			}
		}
		
		
		double[][] kernelMat = new double[keyList1.size()][keyList2.size()];
		for (int i = 0; i < denseVecList1.size(); ++i) {
			for (int j = 0; j < denseVecList2.size(); ++j) {
				if (selectedMatchingType.equals(matchingTypes[0])) {//max matching
					kernelMat[i][j] = denseVecList1.get(i).cosine(denseVecList2.get(j));
				} 
				else if (selectedMatchingType.equals(matchingTypes[1])) {//set kernel
					if (sigma2 <= 0.0) {
						kernelMat[i][j] = 
								denseVecList1.get(i).cosine(denseVecList2.get(j))
								* vector1.get(keyList1.get(i)) * vector2.get(keyList2.get(j));
					} else {
						kernelMat[i][j] = 
								Math.exp(0 - (1 - denseVecList1.get(i).cosine(denseVecList2.get(j)) ) / sigma2)
								* vector1.get(keyList1.get(i)) * vector2.get(keyList2.get(j));
					}
					
				} 
				else if (selectedMatchingType.equals(matchingTypes[2])) {//unbiased set kernel
					kernelMat[i][j] = Math.exp(0 - (1 - denseVecList1.get(i).cosine(denseVecList2.get(j)) ) / sigma2);
				} 
				else if (selectedMatchingType.equals(matchingTypes[3])) {//svd matching
					kernelMat[i][j] = denseVecList1.get(i).cosine(denseVecList2.get(j));
				} 
				else if (selectedMatchingType.equals(matchingTypes[4])) {//hungarian matching
					if (sigma2 <= 0.0) {
						kernelMat[i][j] = 
								denseVecList1.get(i).cosine(denseVecList2.get(j));
					} else {
						kernelMat[i][j] = 
								Math.exp(0 - (1 - denseVecList1.get(i).cosine(denseVecList2.get(j)) ) / sigma2);
					}
				
				}
				
				
			}
		}
	
		
		double similarity = 0;
		
		if (selectedMatchingType.equals(matchingTypes[0])) {
			//System.out.println("using max matching");
			for (int i = 0; i < denseVecList1.size(); ++i) {
				double maxValue = 0;
				int maxIndex = -1;
				for (int j = 0; j < denseVecList2.size(); ++j) {
					if (maxValue < kernelMat[i][j]) {
						maxValue = kernelMat[i][j];
						maxIndex = j;
					}
				}
				if (maxIndex > 0 && maxIndex < denseVecList2.size() && maxValue > similarityThreshold)
					similarity += maxValue
							* vector1.get(keyList1.get(i)) * vector2.get(keyList2.get(maxIndex));
			}
			
//			for (int i = 0; i < denseVecList2.size(); ++i) {
//				double maxValue = 0;
//				int maxIndex = -1;
//				for (int j = 0; j < denseVecList1.size(); ++j) {
//					if (maxValue < kernelMat[j][i]) {
//						maxValue = kernelMat[j][i];
//						maxIndex = j;
//					}
//				}
//				if (maxIndex > 0 && maxIndex < denseVecList1.size() && maxValue > similarityThreshold)
//					similarity += maxValue
//							* vector2.get(keyList2.get(i)) * vector1.get(keyList1.get(maxIndex));
//			}
			
			similarity = similarity / (Double.MIN_NORMAL + norm1) / (Double.MIN_NORMAL + norm2);
			//System.out.println("max matching: " + similarity+", norm1 "+norm1+ " , norm2 "+norm2);
		}
		if (selectedMatchingType.equals(matchingTypes[1])) {
			for (int i = 0; i < denseVecList1.size(); ++i) {
				for (int j = 0; j < denseVecList2.size(); ++j) {
					similarity += kernelMat[i][j];
				}
			}
			similarity = similarity / (Double.MIN_NORMAL + norm1) / (Double.MIN_NORMAL + norm2);
		}
		if (selectedMatchingType.equals(matchingTypes[2])) {
			int minLength = Math.min(denseVecList1.size(), denseVecList2.size());
			double[][] kernelMatNew = new double[denseVecList1.size()][denseVecList2.size()];
			for (int i = 0; i < minLength; ++i) {
				for (int j = 0; j < minLength; ++j) {
					kernelMatNew[i][j] = 1 + 1 - kernelMat[i][j] - kernelMat[j][i];
				}
			}
			for (int i = 0; i < minLength; ++i) {
				for (int j = 0; j < minLength; ++j) {
					similarity += (vector1.get(keyList1.get(i)) * vector2.get(keyList2.get(j)) * kernelMatNew[i][j]);
//					similarity += (kernelMatNew[i][j]);
				}
			}
			similarity /= (minLength * minLength);
			
			similarity = 1 / (similarity + Double.MIN_NORMAL);
			similarity = similarity / (Double.MIN_NORMAL + norm1) / (Double.MIN_NORMAL + norm2);
		}
		if (selectedMatchingType.equals(matchingTypes[3])) {
			int sizeMax = Math.max(denseVecList1.size(), denseVecList2.size());
			int sizeMin = Math.min(denseVecList1.size(), denseVecList2.size());
			
			if (sizeMin > 0) {
				try {
					DenseDoubleAlgebra alg = new DenseDoubleAlgebra();
					DenseColumnDoubleMatrix2D matSVD = new  DenseColumnDoubleMatrix2D(sizeMax, sizeMax);

					double[][] costMat = new double[keyList1.size()][keyList2.size()]; // new double[size][size];
					
					double maxValue = 0;
					for (int i = 0; i < kernelMat.length; ++i) {
						for (int j = 0; j < kernelMat[i].length; ++j) {
							if (kernelMat[i][j] > maxValue) {
								maxValue = kernelMat[i][j];
							}
						}
					}
					for (int i = 0; i < kernelMat.length; ++i) {
						for (int j = 0; j < kernelMat[i].length; ++j) {
							costMat[i][j] = maxValue - kernelMat[i][j];
						}
					}

					HungarianAlgorithm hungarian2 =new HungarianAlgorithm(costMat);
					int[] results2 = hungarian2.execute();
					
					for (int i = 0; i < kernelMat.length; ++i) {
						int indexNewI = results2[i];
						if (indexNewI >= 0 && indexNewI < kernelMat[i].length) {
//							System.out.println(kernelMat[i][indexNewI]);
							// move the column of indexNewI to column of i
							for (int r = 0; r < kernelMat.length; ++r) {
								double value = kernelMat[r][indexNewI] * vector1.get(keyList1.get(r)) * vector2.get(keyList2.get(indexNewI)) 
										/ (Double.MIN_NORMAL + norm1) / (Double.MIN_NORMAL + norm2);
								matSVD.set(r, i, value);
							}
						}
						else {
							for (int r = 0; r < matSVD.rows(); ++r) {
								double value = 0;
								matSVD.set(r, i, value);
							}
						}
						
					}
					
					HashSet<String> set1 = new HashSet<String>(vector1.keySet());
					HashSet<String> set2 = new HashSet<String>(vector2.keySet());
					set1.retainAll(set2);
//					System.out.println(set1.size());
					
					if (matSVD.rows() > 0 && matSVD.columns() > 0) {
						DenseDoubleSingularValueDecomposition s = alg.svd(matSVD);
						@SuppressWarnings("unused")
						DoubleMatrix2D U = s.getU();
						DoubleMatrix2D S = s.getS();
						@SuppressWarnings("unused")
						DoubleMatrix2D V = s.getV();
						
						//call original colt
//						DenseDoubleMatrix2D matA = new DenseDoubleMatrix2D(kernelMat);
//						SingularValueDecomposition s = new SingularValueDecomposition(matA);
//						cern.colt.matrix.DoubleMatrix2D U = s.getU();
//						cern.colt.matrix.DoubleMatrix2D S = s.getS();
//						cern.colt.matrix.DoubleMatrix2D V = s.getV();
						
						for (int k = 0; k < Math.min(Math.min(Math.min(topKSV, S.rows()), S.columns()), S.size()); ++k) {
								similarity += S.get(k, k);
						}
						
//						for (int k = 0; k < Math.min(S.columns(), S.rows()); ++k) {
//							if (S.get(k, k) > similarityThreshold) {
//								similarity += S.get(k, k);
//							}
//						}
						
						if (similarity > 1) {
							System.out.println("Similary larger than 1");
						}
							
						double cosine = cosine(vector1, vector2, norm1, norm2);
						System.out.println("Overlap: " + set1.size() + ", cosine: " + cosine + ", Similarity: " + similarity );
						
					}
					
				} catch (Exception e) {
					e.printStackTrace();
//					System.out.println();
				}
			}

				
		} 
		
		double sim = cosine (vector1, vector2, norm1, norm2);
		//System.out.println("Cosine " + sim);

		if (selectedMatchingType.equals(matchingTypes[4])) {
			int sizeMax = Math.max(denseVecList1.size(), denseVecList2.size());
			int sizeMin = Math.min(denseVecList1.size(), denseVecList2.size());
			
			if (sizeMin > 0) {
				double[][] costMat = new double[keyList1.size()][keyList2.size()]; // new double[size][size];
				
				double maxValue = 0;
				for (int i = 0; i < kernelMat.length; ++i) {
					for (int j = 0; j < kernelMat[i].length; ++j) {
						if (kernelMat[i][j] > maxValue) {
							maxValue = kernelMat[i][j];
						}
					}
				}
				for (int i = 0; i < kernelMat.length; ++i) {
					for (int j = 0; j < kernelMat[i].length; ++j) {
						costMat[i][j] = maxValue - kernelMat[i][j];
					}
				}
				
//				HungarianDouble hungarian1 = new HungarianDouble(costMat);
//				int[] results1 = hungarian1.getResult();
//				double similarity1 = 0;
//				for (int i = 0; i < results1.length; ++i) {
//					if (results1[i] > 0 && results1[i] < kernelMat[i].length) {
//						similarity1 += kernelMat[i][results1[i]] 
//								* vector1.get(keyList1.get(i)) * vector2.get(keyList2.get(results1[i]));	
//					}
//				}
				
				HungarianAlgorithm hungarian2 =new HungarianAlgorithm(costMat);
				int[] results2 = hungarian2.execute();
				double similarity2 = 0;
				for (int i = 0; i < results2.length; ++i) {
					if (results2[i] >= 0 && results2[i] < kernelMat[i].length) {
						
						if (kernelMat[i][results2[i]]  > similarityThreshold) {
							similarity2 += kernelMat[i][results2[i]] 
									* vector1.get(keyList1.get(i)) * vector2.get(keyList2.get(results2[i]));	
						}
					}
				}
				
				similarity = similarity2 / norm1 /norm2;
			}
			
		}
		
		return similarity;
	}
	
	public DensificationData getVectorOfVectors (HashMap<String, Double> vector1) {
		
		List<String> keyList1 = new ArrayList<String>(vector1.keySet());
		
		List<DenseVector> denseVecList1 = new ArrayList<DenseVector>();
		
		DensificationData data = new DensificationData();
		for (int i = 0; i < keyList1.size(); ++i) {
			String id = keyList1.get(i);
			if (pageTitleIDMap.containsKey(id)) {
				id = pageTitleIDMap.get(id);
			}
			data.vector.put(id, vector1.get(keyList1.get(i)));
			if (pageIDTitleMap.containsKey(id)) {
				DenseVector dvector = getDenseVectorofTitle (id);
				denseVecList1.add(dvector);
				data.vectorOfVectors.put(id, dvector.vector);
			}
		}
		
		return data;
	}
	
	public static double cosine (HashMap<String, Double> v1, HashMap<String, Double> v2, double norm1, double norm2) {
//		double norm1 = 0;
//		double norm2 = 0;
		double dot = 0;
		if (v1.size() < v2.size()) {
			for (String key : v1.keySet()) {
				if (v2.containsKey(key) == true) {
					double value1 = v1.get(key);
					double value2 = v2.get(key);
					dot += value1 * value2;
				}
			}
		} else {
			for (String key : v2.keySet()) {
				if (v1.containsKey(key) == true) {
					double value1 = v1.get(key);
					double value2 = v2.get(key);
					dot += value1 * value2;
				}
			}
		}
		
		return dot / (Double.MIN_NORMAL + norm1) / (Double.MIN_NORMAL + norm2);
	}

	public static double similarity(WordEmbeddingInterface word2vecEmbd, String topic, String content) {
		double[] densevector1 = word2vecEmbd.getDenseVectorBasedonSegmentation(topic, false);
		DenseVector vector1 = new DenseVector(densevector1);
		double[] densevector2 = word2vecEmbd.getDenseVectorBasedonSegmentation(content, false);
		DenseVector vector2 = new DenseVector(densevector2);
		double cos=vector1.cosine(vector2);
		return cos;
	}

}
