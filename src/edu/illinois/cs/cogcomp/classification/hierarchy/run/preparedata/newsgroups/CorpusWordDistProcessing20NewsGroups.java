package edu.illinois.cs.cogcomp.classification.hierarchy.run.preparedata.newsgroups;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.Random;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.jlis.CorpusDataProcessing;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups.NewsgroupsCorpusConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.representation.word2vec.DiskBasedWordEmbedding;

/**
 * yqsong@illinois.edu
 */

public class CorpusWordDistProcessing20NewsGroups {
	
	public static void main(String[] args) {
		
//		conceptualize20NewsGroups(50, "brownClusterWikiOld");
//		conceptualize20NewsGroups(50, "brownClusterWiki");
//		conceptualize20NewsGroups(100, "brownClusterWikiOld");
//		conceptualize20NewsGroups(100, "brownClusterWiki");
//		conceptualize20NewsGroups(200, "brownClusterWikiOld");
//		conceptualize20NewsGroups(200, "brownClusterWiki");
//		conceptualize20NewsGroups(500, "brownClusterWikiOld");
//		conceptualize20NewsGroups(500, "brownClusterWiki");
		
//		conceptualize20NewsGroups(50, "brownCluster20NGOld");
//		conceptualize20NewsGroups(50, "brownCluster20NG");
//		conceptualize20NewsGroups(100, "brownCluster20NGOld");
//		conceptualize20NewsGroups(100, "brownCluster20NG");
//		conceptualize20NewsGroups(200, "brownCluster20NGOld");
//		conceptualize20NewsGroups(200, "brownCluster20NG");
//		conceptualize20NewsGroups(500, "brownCluster20NGOld");
//		conceptualize20NewsGroups(500, "brownCluster20NG");
//		conceptualize20NewsGroups(1000, "brownCluster20NGOld");
//		conceptualize20NewsGroups(1000, "brownCluster20NG");
//		
//		conceptualize20NewsGroups(100, "brownClusterRatinovOld");
//		conceptualize20NewsGroups(100, "brownClusterRatinov");
//		conceptualize20NewsGroups(320, "brownClusterRatinovOld");
//		conceptualize20NewsGroups(320, "brownClusterRatinov");
//		conceptualize20NewsGroups(1000, "brownClusterRatinovOld");
//		conceptualize20NewsGroups(1000, "brownClusterRatinov");
//		conceptualize20NewsGroups(3200, "brownClusterRatinovOld");
//		conceptualize20NewsGroups(3200, "brownClusterRatinov");
//
//		conceptualize20NewsGroups(50, "word2vector");
//		conceptualize20NewsGroups(100, "word2vector");
//		conceptualize20NewsGroups(200, "word2vector");
//		conceptualize20NewsGroups(500, "word2vector");
//		conceptualize20NewsGroups(1000, "word2vector");
//		
//		conceptualize20NewsGroups(25, "ratinovEmbedding");
//		conceptualize20NewsGroups(50, "ratinovEmbedding");
//		conceptualize20NewsGroups(100, "ratinovEmbedding");
//		conceptualize20NewsGroups(200, "ratinovEmbedding");
//
//		conceptualize20NewsGroups(0, "senna");
	}

	public static void conceptualize20NewsGroups (int num, String methodStr) 	{
		//22970 documents
		int seed = 0;
		Random random = new Random(seed);
		double trainingRate = 0.5;
		CorpusWordDistProcessing20NewsGroups corpusContentProc = new CorpusWordDistProcessing20NewsGroups();
		NewsgroupsCorpusConceptData ngData = new NewsgroupsCorpusConceptData();
	
		String inputIndexDir = "data/20newsgroups/textindex";

		ngData.readCorpusContentOnly(inputIndexDir, random, trainingRate);

		CorpusDataProcessing corpusDataProcessing = new CorpusDataProcessing();
		HashMap<String, String> dataLibSVMFormat = 
				corpusDataProcessing.initializeTrainingDocumentFeatures (ngData.getCorpusContentMap(), true, true);

		String outputFile = "";
		String method = "";
		
		if (methodStr.equals("brownClusterWikiOld")) {
			method = "browncluster" + num + "_old";
			outputFile = "data/20newsgroups/output/20newsgroups.word.dist.features.browncluster.enwiki.old" + num;
		}
		if (methodStr.equals("brownClusterWiki")) {
			method = "browncluster" + num + "";
			outputFile = "data/20newsgroups/output/20newsgroups.word.dist.features.browncluster.enwiki." + num;
		}

		if (methodStr.equals("brownCluster20NGOld")) {
			method = "browncluster" + num + "_old_20NG";
			outputFile = "data/20newsgroups/output/20newsgroups.word.dist.features.browncluster.20NG.old" + num;
		}
		if (methodStr.equals("brownCluster20NG")) {
			method = "browncluster" + num + "_20NG";
			outputFile = "data/20newsgroups/output/20newsgroups.word.dist.features.browncluster.20NG." + num;
		}
		
		if (methodStr.equals("brownClusterRatinovOld")) {
			method = "browncluster" + num + "_old_Ratinov";
			outputFile = "data/20newsgroups/output/20newsgroups.word.dist.features.browncluster.ratinov.old" + num;
		}
		if (methodStr.equals("brownClusterRatinov")) {
			method = "browncluster" + num + "_Ratinov";
			outputFile = "data/20newsgroups/output/20newsgroups.word.dist.features.browncluster.ratinov." + num;
		}
		
		////////////////////////////////////////////////////////////
		if (methodStr.equals("ratinovEmbedding")) {
			method = "ratinovEmbedding" + num;
			outputFile = "data/20newsgroups/output/20newsgroups.word.dist.features.ratinov.embedding." + num;
		}
		////////////////////////////////////////////////////////////
		if (methodStr.equals("word2vector")) {
			method = "word2vector" + num;
			outputFile = "data/20newsgroups/output/20newsgroups.word.dist.features.enwiki.vivek." + num;
		}
		////////////////////////////////////////////////////////////
		if (methodStr.equals("senna")) {
			method = "senna";
			outputFile = "data/20newsgroups/output/20newsgroups.word.dist.features.enwiki.senna";
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
