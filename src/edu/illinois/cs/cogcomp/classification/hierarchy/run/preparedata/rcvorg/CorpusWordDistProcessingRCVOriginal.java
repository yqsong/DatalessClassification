package edu.illinois.cs.cogcomp.classification.hierarchy.run.preparedata.rcvorg;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.Random;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.jlis.CorpusDataProcessing;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv.RCVCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcvorg.RCVOriginalCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.DiskBasedWordEmbedding;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

public class CorpusWordDistProcessingRCVOriginal {
	
	public static void main(String[] args) {
		
		conceptualizeRCVTrain(50, "brownClusterWikiOld");
		conceptualizeRCVTrain(50, "brownClusterWiki");
		conceptualizeRCVTrain(100, "brownClusterWikiOld");
		conceptualizeRCVTrain(100, "brownClusterWiki");
		conceptualizeRCVTrain(200, "brownClusterWikiOld");
		conceptualizeRCVTrain(200, "brownClusterWiki");
		conceptualizeRCVTrain(500, "brownClusterWikiOld");
		conceptualizeRCVTrain(500, "brownClusterWiki");

		conceptualizeRCVTrain(50, "brownCluster20NGOld");
		conceptualizeRCVTrain(50, "brownCluster20NG");
		conceptualizeRCVTrain(100, "brownCluster20NGOld");
		conceptualizeRCVTrain(100, "brownCluster20NG");
		conceptualizeRCVTrain(200, "brownCluster20NGOld");
		conceptualizeRCVTrain(200, "brownCluster20NG");
		conceptualizeRCVTrain(500, "brownCluster20NGOld");
		conceptualizeRCVTrain(500, "brownCluster20NG");
		conceptualizeRCVTrain(1000, "brownCluster20NGOld");
		conceptualizeRCVTrain(1000, "brownCluster20NG");
		
		conceptualizeRCVTrain(100, "brownClusterRatinovOld");
		conceptualizeRCVTrain(100, "brownClusterRatinov");
		conceptualizeRCVTrain(320, "brownClusterRatinovOld");
		conceptualizeRCVTrain(320, "brownClusterRatinov");
		conceptualizeRCVTrain(1000, "brownClusterRatinovOld");
		conceptualizeRCVTrain(1000, "brownClusterRatinov");
		conceptualizeRCVTrain(3200, "brownClusterRatinovOld");
		conceptualizeRCVTrain(3200, "brownClusterRatinov");

		conceptualizeRCVTrain(50, "word2vector");
		conceptualizeRCVTrain(100, "word2vector");
		conceptualizeRCVTrain(200, "word2vector");
		conceptualizeRCVTrain(500, "word2vector");
		conceptualizeRCVTrain(1000, "word2vector");
		
		conceptualizeRCVTrain(25, "ratinovEmbedding");
		conceptualizeRCVTrain(50, "ratinovEmbedding");
		conceptualizeRCVTrain(100, "ratinovEmbedding");
		conceptualizeRCVTrain(200, "ratinovEmbedding");

		conceptualizeRCVTrain(0, "senna");
		
//		for (int i = 0; i < 1; ++i) {
//			conceptualizeRCVTest(50, "brownClusterWikiOld", i);
//			conceptualizeRCVTest(50, "brownClusterWiki", i);
//			conceptualizeRCVTest(100, "brownClusterWikiOld", i);
//			conceptualizeRCVTest(100, "brownClusterWiki", i);
//			conceptualizeRCVTest(200, "brownClusterWikiOld", i);
//			conceptualizeRCVTest(200, "brownClusterWiki", i);
//
//			conceptualizeRCVTest(50, "brownCluster20NGOld", i);
//			conceptualizeRCVTest(50, "brownCluster20NG", i);
//			conceptualizeRCVTest(100, "brownCluster20NGOld", i);
//			conceptualizeRCVTest(100, "brownCluster20NG", i);
//			conceptualizeRCVTest(200, "brownCluster20NGOld", i);
//			conceptualizeRCVTest(200, "brownCluster20NG", i);
//			conceptualizeRCVTest(500, "brownCluster20NGOld", i);
//			conceptualizeRCVTest(500, "brownCluster20NG", i);
//			conceptualizeRCVTest(1000, "brownCluster20NGOld", i);
//			conceptualizeRCVTest(1000, "brownCluster20NG", i);
//			
//			conceptualizeRCVTest(100, "brownClusterRatinovOld", i);
//			conceptualizeRCVTest(100, "brownClusterRatinov", i);
//			conceptualizeRCVTest(320, "brownClusterRatinovOld", i);
//			conceptualizeRCVTest(320, "brownClusterRatinov", i);
//			conceptualizeRCVTest(1000, "brownClusterRatinovOld", i);
//			conceptualizeRCVTest(1000, "brownClusterRatinov", i);
//			conceptualizeRCVTest(3200, "brownClusterRatinovOld", i);
//			conceptualizeRCVTest(3200, "brownClusterRatinov", i);
//
//			conceptualizeRCVTest(50, "word2vector", i);
//			conceptualizeRCVTest(100, "word2vector", i);
//			conceptualizeRCVTest(200, "word2vector", i);
//			conceptualizeRCVTest(500, "word2vector", i);
//			conceptualizeRCVTest(1000, "word2vector", i);
//			
//			conceptualizeRCVTest(25, "ratinovEmbedding", i);
//			conceptualizeRCVTest(50, "ratinovEmbedding", i);
//			conceptualizeRCVTest(100, "ratinovEmbedding", i);
//			conceptualizeRCVTest(200, "ratinovEmbedding", i);
//
//			conceptualizeRCVTest(0, "senna", i);
//		}
	}

	public static void conceptualizeRCVTrain (int num, String methodStr) 	{
		//22970 documents
		int seed = 0;
		Random random = new Random(seed);
		double trainingRate = 0.5;
		CorpusWordDistProcessingRCVOriginal corpusContentProc = new CorpusWordDistProcessingRCVOriginal();

		String inputData = "D:/yqsong/data/ReutersCorpusVolume1/rcvOrgTrainContent.txt";
		if (ClassifierConstant.isServer == true) {
			inputData = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/rcvOrgTrainContent.txt";
		}
		RCVOriginalCorpusConceptData ngData = new RCVOriginalCorpusConceptData();
		ngData.readCorpusContentOnly(inputData, random, trainingRate);

		
//		NewsgroupsCorpusConceptData ngData = new NewsgroupsCorpusConceptData();
//		String inputIndexDir = "/shared/saruman/yqsong/benchmark/20newsgroups/textindex";
//		if (ClassifierConstant.isServer == false) {
//			inputIndexDir = "D:/yqsong/data/20newsgroups/textindex";
//		}
//		ngData.readCorpusContentOnly(inputIndexDir, random, trainingRate);

		CorpusDataProcessing corpusDataProcessing = new CorpusDataProcessing();
		HashMap<String, String> dataLibSVMFormat = 
				corpusDataProcessing.initializeTrainingDocumentFeatures (ngData.getCorpusContentMap(), true, true);

		String outputFile = "";
		String method = "";
		
		if (methodStr.equals("brownClusterWikiOld")) {
			method = "browncluster" + num + "_old";
			if (ClassifierConstant.isServer == false) {
				outputFile = "D:/yqsong/data/ReutersCorpusVolume1/output_train/rcv_train.word.dist.features.browncluster.enwiki.old" + num;
			} else {
				outputFile = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/output_train/rcv_train.word.dist.features.browncluster.enwiki.old" + num;
			}
		}
		if (methodStr.equals("brownClusterWiki")) {
			method = "browncluster" + num + "";
			if (ClassifierConstant.isServer == false) {
				outputFile = "D:/yqsong/data/ReutersCorpusVolume1/output_train/rcv_train.word.dist.features.browncluster.enwiki." + num;
			} else {
				outputFile = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/output_train/rcv_train.word.dist.features.browncluster.enwiki." + num;
			}
		}

		
		if (methodStr.equals("brownCluster20NGOld")) {
			method = "browncluster" + num + "_old_20NG";
			if (ClassifierConstant.isServer == false) {
				outputFile = "D:/yqsong/data/ReutersCorpusVolume1/output_train/rcv_train.word.dist.features.browncluster.20NG.old" + num;
			} else {
				outputFile = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/output_train/rcv_train.word.dist.features.browncluster.20NG.old" + num;
			}
		}
		if (methodStr.equals("brownCluster20NG")) {
			method = "browncluster" + num + "_20NG";
			if (ClassifierConstant.isServer == false) {
				outputFile = "D:/yqsong/data/ReutersCorpusVolume1/output_train/rcv_train.word.dist.features.browncluster.20NG." + num;
			} else {
				outputFile = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/output_train/rcv_train.word.dist.features.browncluster.20NG." + num;
			}
		}
		
		
		if (methodStr.equals("brownClusterRatinovOld")) {
			method = "browncluster" + num + "_old_Ratinov";
			if (ClassifierConstant.isServer == false) {
				outputFile = "D:/yqsong/data/ReutersCorpusVolume1/output_train/rcv_train.word.dist.features.browncluster.ratinov.old" + num;
			} else {
				outputFile = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/output_train/rcv_train.word.dist.features.browncluster.ratinov.old" + num;
			}
		}
		if (methodStr.equals("brownClusterRatinov")) {
			method = "browncluster" + num + "_Ratinov";
			if (ClassifierConstant.isServer == false) {
				outputFile = "D:/yqsong/data/ReutersCorpusVolume1/output_train/rcv_train.word.dist.features.browncluster.ratinov." + num;
			} else {
				outputFile = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/output_train/rcv_train.word.dist.features.browncluster.ratinov." + num;
			}
		}
		

		
		////////////////////////////////////////////////////////////
		if (methodStr.equals("ratinovEmbedding")) {
			method = "ratinovEmbedding" + num;
			if (ClassifierConstant.isServer == false) {
				outputFile = "D:/yqsong/data/ReutersCorpusVolume1/output_train/rcv_train.word.dist.features.ratinov.embedding." + num;
			} else {
				outputFile = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/output_train/rcv_train.word.dist.features.ratinov.embedding." + num;
			}
		}
		////////////////////////////////////////////////////////////
		if (methodStr.equals("word2vector")) {
			method = "word2vector" + num;
			if (ClassifierConstant.isServer == false) {
				outputFile = "D:/yqsong/data/ReutersCorpusVolume1/output_train/rcv_train.word.dist.features.enwiki.vivek." + num;
			} else {
				outputFile = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/output_train/rcv_train.word.dist.features.enwiki.vivek." + num;
			}
		}
		////////////////////////////////////////////////////////////
		if (methodStr.equals("senna")) {
			method = "senna";
			if (ClassifierConstant.isServer == false) {
				outputFile = "D:/yqsong/data/ReutersCorpusVolume1/output_train/rcv_train.word.dist.features.enwiki.senna";
			} else {
				outputFile = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/output_train/rcv_train.word.dist.features.enwiki.senna";
			}
		}
		////////////////////////////////////////////////////////////
		
		System.out.println("[Embedding method:] " + method);

		boolean isDF = true;
		boolean isTFDIF = false;
		boolean isClustering = false;
		
		corpusContentProc.writeCorpusWordDistData(ngData.getCorpusContentMap(), dataLibSVMFormat,
				corpusDataProcessing.getInvDict(),
				outputFile + ".df",  
				method, isDF, isTFDIF, isClustering);

		isDF = false;
		isTFDIF = false;
		isClustering = false;
		
		corpusContentProc.writeCorpusWordDistData(ngData.getCorpusContentMap(), dataLibSVMFormat,
				corpusDataProcessing.getInvDict(),
				outputFile + ".tf", 
				method, isDF, isTFDIF, isClustering);
		
		isDF = false;
		isTFDIF = true;
		isClustering = false;
		
		corpusContentProc.writeCorpusWordDistData(ngData.getCorpusContentMap(), dataLibSVMFormat,
				corpusDataProcessing.getInvDict(),
				outputFile + ".tfidf", 
				method, isDF, isTFDIF, isClustering);

		
	}
	
	
	public static void conceptualizeRCVTest (int num, String methodStr, int partNum) 	{
		//22970 documents
		int seed = 0;
		Random random = new Random(seed);
		double trainingRate = 0.5;
		CorpusWordDistProcessingRCVOriginal corpusContentProc = new CorpusWordDistProcessingRCVOriginal();

		String inputData = "D:/yqsong/data/ReutersCorpusVolume1/rcvOrgTrainContent.txt";
		if (ClassifierConstant.isServer == true) {
			inputData = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/rcvOrgTrainContent.txt";
		}
		RCVOriginalCorpusConceptData ngData = new RCVOriginalCorpusConceptData();
		ngData.readCorpusContentOnly(inputData, random, trainingRate);

		
//		NewsgroupsCorpusConceptData ngData = new NewsgroupsCorpusConceptData();
//		String inputIndexDir = "/shared/saruman/yqsong/benchmark/20newsgroups/textindex";
//		if (ClassifierConstant.isServer == false) {
//			inputIndexDir = "D:/yqsong/data/20newsgroups/textindex";
//		}
//		ngData.readCorpusContentOnly(inputIndexDir, random, trainingRate);

		CorpusDataProcessing corpusDataProcessing = new CorpusDataProcessing();
		HashMap<String, String> dataLibSVMFormat = 
				corpusDataProcessing.initializeTrainingDocumentFeatures (ngData.getCorpusContentMap(), true, true);

		String outputFile = "";
		String method = "";
		
		if (methodStr.equals("brownClusterWikiOld")) {
			method = "browncluster" + num + "_old";
			if (ClassifierConstant.isServer == false) {
				outputFile = "D:/yqsong/data/ReutersCorpusVolume1/output_test/rcv_test_pt" + partNum + ".word.dist.features.enwiki.browncluster.old" + num;
			} else {
				outputFile = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/output_test/rcv_test_pt" + partNum + ".word.dist.features.enwiki.browncluster.old" + num;
			}
		}
		if (methodStr.equals("brownClusterWiki")) {
			method = "browncluster" + num + "";
			if (ClassifierConstant.isServer == false) {
				outputFile = "D:/yqsong/data/ReutersCorpusVolume1/output_test/rcv_test_pt" + partNum + ".word.dist.features.enwiki.browncluster" + num;
			} else {
				outputFile = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/output_test/rcv_test_pt" + partNum + ".word.dist.features.enwiki.browncluster" + num;
			}
		}

		
		if (methodStr.equals("brownCluster20NGOld")) {
			method = "browncluster" + num + "_old_20NG";
			if (ClassifierConstant.isServer == false) {
				outputFile = "D:/yqsong/data/ReutersCorpusVolume1/output_test/rcv_test_pt" + partNum + ".word.dist.features.20NG.browncluster.old" + num;
			} else {
				outputFile = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/output_test/rcv_test_pt" + partNum + ".word.dist.features.20NG.browncluster.old" + num;
			}
		}
		if (methodStr.equals("brownCluster20NG")) {
			method = "browncluster" + num + "_20NG";
			if (ClassifierConstant.isServer == false) {
				outputFile = "D:/yqsong/data/ReutersCorpusVolume1/output_test/rcv_test_pt" + partNum + ".word.dist.features.20NG.browncluster" + num;
			} else {
				outputFile = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/output_test/rcv_test_pt" + partNum + ".word.dist.features.20NG.browncluster" + num;
			}
		}
		
		
		if (methodStr.equals("brownClusterRatinovOld")) {
			method = "browncluster" + num + "_old_Ratinov";
			if (ClassifierConstant.isServer == false) {
				outputFile = "D:/yqsong/data/ReutersCorpusVolume1/output_test/rcv_test_pt" + partNum + ".word.dist.features.ratinov.browncluster.old" + num;
			} else {
				outputFile = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/output_test/rcv_test_pt" + partNum + ".word.dist.features.20NG.browncluster.old" + num;
			}
		}
		if (methodStr.equals("brownClusterRatinov")) {
			method = "browncluster" + num + "_Ratinov";
			if (ClassifierConstant.isServer == false) {
				outputFile = "D:/yqsong/data/ReutersCorpusVolume1/output_test/rcv_test_pt" + partNum + ".word.dist.features.ratinov.browncluster" + num;
			} else {
				outputFile = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/output_test/rcv_test_pt" + partNum + ".word.dist.features.20NG.browncluster" + num;
			}
		}
		

		
		////////////////////////////////////////////////////////////
		if (methodStr.equals("ratinovEmbedding")) {
			method = "ratinovEmbedding" + num;
			if (ClassifierConstant.isServer == false) {
				outputFile = "D:/yqsong/data/ReutersCorpusVolume1/output_test/rcv_test_pt" + partNum + ".word.dist.features.ratinov.embedding." + num;
			} else {
				outputFile = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/output_test/rcv_test_pt" + partNum + ".word.dist.features.ratinov.embedding." + num;
			}
		}
		////////////////////////////////////////////////////////////
		if (methodStr.equals("word2vector")) {
			method = "word2vector" + num;
			if (ClassifierConstant.isServer == false) {
				outputFile = "D:/yqsong/data/ReutersCorpusVolume1/output_test/rcv_test_pt" + partNum + ".word.dist.features.enwiki.vivek." + num;
			} else {
				outputFile = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/output_test/rcv_test_pt" + partNum + ".word.dist.features.enwiki.vivek." + num;
			}
		}
		////////////////////////////////////////////////////////////
		if (methodStr.equals("senna")) {
			method = "senna";
			if (ClassifierConstant.isServer == false) {
				outputFile = "D:/yqsong/data/ReutersCorpusVolume1/output_test/rcv_test_pt" + partNum + ".word.dist.features.enwiki.senna";
			} else {
				outputFile = "/shared/shelley/yqsong/benchmark/ReutersCorpusVolume1/output_test/rcv_test_pt" + partNum + ".word.dist.features.enwiki.senna";
			}
		}
		////////////////////////////////////////////////////////////
		
		System.out.println("[Embedding method:] " + method);

		boolean isDF = true;
		boolean isTFDIF = false;
		boolean isClustering = false;
		
		corpusContentProc.writeCorpusWordDistData(ngData.getCorpusContentMap(), dataLibSVMFormat,
				corpusDataProcessing.getInvDict(),
				outputFile + ".df",  
				method, isDF, isTFDIF, isClustering);

		isDF = false;
		isTFDIF = false;
		isClustering = false;
		
		corpusContentProc.writeCorpusWordDistData(ngData.getCorpusContentMap(), dataLibSVMFormat,
				corpusDataProcessing.getInvDict(),
				outputFile + ".tf", 
				method, isDF, isTFDIF, isClustering);
		
		isDF = false;
		isTFDIF = true;
		isClustering = false;
		
		corpusContentProc.writeCorpusWordDistData(ngData.getCorpusContentMap(), dataLibSVMFormat,
				corpusDataProcessing.getInvDict(),
				outputFile + ".tfidf", 
				method, isDF, isTFDIF, isClustering);

		
	}

	
	public void writeCorpusWordDistData (HashMap<String, String> corpusContentMap, 
			HashMap<String, String> dataLibSVMFormat,
			HashMap<Integer, String> dict,
			String file, String method, boolean isDF, boolean isTFIDF, boolean isClustering) {
		
		
		String content = "";
		try {
			int count = 0;
			FileWriter writer = new FileWriter(file);
			DiskBasedWordEmbedding wordDist = new DiskBasedWordEmbedding();
			for (String docID : corpusContentMap.keySet()) {
				count++;
				
				if (count % 100 == 0){
					System.out.println("[Word Dist Processing] Written " + count +  " documents with concepts");
				}
				
				HashMap<Integer, Double> features = null;
				content = corpusContentMap.get(docID);
				if (isTFIDF == false) {
					
					if (isClustering == false) {
						features = wordDist.getConceptVectorBasedonSegmentation(content, isDF);
					} else {
						int cNum = content.split("\\s+").length / 50 + 1;
						if (cNum > 100)
							cNum= 100;
//						features = wordDist.getConceptVectorBasedonKmeans(content, cNum);
					}
				} else {
					HashMap<String, Double> docTFIDF = new HashMap<String, Double>();
					String[] tokens = dataLibSVMFormat.get(docID).trim().split(" ");
					for (int i = 0; i < tokens.length; ++i) {
						String[] subTokens = tokens[i].trim().split(":");
						if (docTFIDF.containsKey(subTokens[0]) == false) {
							docTFIDF.put(dict.get(Integer.parseInt(subTokens[0].trim())), Double.parseDouble(subTokens[1].trim()));
						}
					}
					features = wordDist.getConceptVectorBasedonTFIDF(docTFIDF);
					
				}
				
				writer.write(docID + "\t" + content + "\t");
				if (features != null) {
//					for (int i = 0; i < features.length; ++i) {
//					writer.write(i + "," + features[i ] + ";");
					for (Integer i : features.keySet()) {
						writer.write(i + "," + features.get(i) + ";");
					}
				}
				writer.write("\n\r");
			}
			writer.close();
		} catch (Exception e) {
			System.out.println(content);
			e.printStackTrace();
		}
			
	}
}
