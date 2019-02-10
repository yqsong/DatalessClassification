package edu.illinois.cs.cogcomp.classification.representation.word2vec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * yqsong@illinois.edu
 */

public class ImportWordDistributionToIndex {
	
	public static void main (String[] args) {

//		ImportWordDistributionToIndex.exportWord2Vector(1000);
		
//		exportSenna();
		
//		exportBrownClusterWiki(50);
//		exportBrownClusterWiki_old(50);
//		exportBrownClusterWiki(100);
//		exportBrownClusterWiki_old(100);
//		exportBrownClusterWiki(200);
//		exportBrownClusterWiki_old(200);
//		exportBrownClusterWiki(500);
//		exportBrownClusterWiki_old(500);
		
//		exportBrownCluster20NG(50);
//		exportBrownCluster20NG_old(50);
//		exportBrownCluster20NG(100);
//		exportBrownCluster20NG_old(100);
//		exportBrownCluster20NG(200);
//		exportBrownCluster20NG_old(200);
//		exportBrownCluster20NG(500);
//		exportBrownCluster20NG_old(500);
//		exportBrownCluster20NG(1000);
//		exportBrownCluster20NG_old(1000);
		
//		exportBrownClusterRatinov(100);
//		exportBrownClusterRatinov_old(100);
//		exportBrownClusterRatinov(320);
//		exportBrownClusterRatinov_old(320);
//		exportBrownClusterRatinov(1000);
//		exportBrownClusterRatinov_old(1000);
//		exportBrownClusterRatinov(3200);
//		exportBrownClusterRatinov_old(3200);

//		exportEmbeddingRatinov(25);
//		exportEmbeddingRatinov(50);
//		exportEmbeddingRatinov(100);
//		exportEmbeddingRatinov(200);
		
//		exportWord2Vector(50);
//		exportWord2Vector(100);
//		exportWord2Vector(200);
//		exportWord2Vector(500);
//		exportWord2Vector(1000);

	}
	
	public static void exportEmbeddingRatinov (int dim) {
		try {
//			String inputTextStr = "/shared/saruman/yqsong/word2vector-convertor/vectors-enwiki9.txt";
//			String outputDirStr = "/shared/saruman/yqsong/data/wordDist/enwiki9";
			
			String inputTextStr = "/shared/shelley/yqsong/Ratinov/embeddings-scaled.EMBEDDING_SIZE=" + dim + ".txt";
			String outputDirStr = "/shared/saruman/yqsong/data/wordDist/rcv_ratinov_embedding_" + dim;
			Directory indexDir = FSDirectory.open(new File(outputDirStr));
			IndexWriter writer = new IndexWriter (indexDir, new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.LIMITED); 
			
			FileReader reader = new FileReader (inputTextStr);
			BufferedReader bf = new BufferedReader (reader);
			
			String line = "";
			line = bf.readLine();
			String[] tokens = null;
			
			int count = 0;
			while ((line = bf.readLine()) != null) {
				if (count % 10000 == 0) {
					System.out.println("Processed " + count + " words..");
				}
				count++;
				if (line == "") {
					continue;
				}
				tokens = line.trim().split(" ");
				
				String word = tokens[0].trim();
				word = word.replaceAll("[^a-zA-Z\\s]", "");
				
				String feature = "";
				for (int i = 1; i < tokens.length; ++i) {
					feature += tokens[i].trim() + " ";
				}
				
				if (word.equals("") == false) {
					Document doc = new Document();
					doc.add(new Field("word", word,
			                Field.Store.YES,
			                Field.Index.ANALYZED));
					doc.add(new Field("feature", feature,
			                Field.Store.YES,
			                Field.Index.NOT_ANALYZED));
					
					writer.addDocument(doc);
				}
			}
			
			bf.close();
			reader.close();
			
			writer.optimize();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void exportBrownClusterRatinov (int clusterNum) {
		try {
			
			String inputTextStr = "/shared/shelley/yqsong/Ratinov/brown-rcv1.clean.tokenized-CoNLL03.txt-c" + clusterNum + "-freq1.txt";
			String outputDirStr = "/shared/saruman/yqsong/data/wordDist/brown_cluster_Ratinov_" + clusterNum + "";
			Directory indexDir = FSDirectory.open(new File(outputDirStr));
			IndexWriter writer = new IndexWriter (indexDir, new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.LIMITED); 
			
			
			FileReader reader = new FileReader (inputTextStr);
			BufferedReader bf = new BufferedReader (reader);
			
			String line = "";
			String[] tokens = null;
			
			int count = 0;
			
			reader = new FileReader (inputTextStr);
			bf = new BufferedReader (reader);
			
			line = "";
			tokens = null;
			
			count = 0;
			while ((line = bf.readLine()) != null) {
				if (count % 10000 == 0) {
					System.out.println("Processed " + count + " words..");
				}
				count++;
				if (line == "") {
					continue;
				}
				tokens = line.trim().split("\t");
				if (tokens.length != 3) {
					continue;
				}
				
				String feature = tokens[0].trim();
				String word = tokens[1].trim();
				
				if (word.equals("mac")) {
					int stop = 0;
					int a = stop;
				}
				
				char[] featureChar = feature.toCharArray();
				
				String featureVector = "";
				int clusterID = 0;
				for (int i = 0; i < featureChar.length; ++i) {
					if (featureChar[i] == '0') {
						clusterID = 1 + clusterID * 2;
					} else {
						clusterID = 2 + clusterID * 2;
					}
					
					featureVector += clusterID + ":" + 1 + " ";
				}
				
				Document doc = new Document();
				doc.add(new Field("word", word,
		                Field.Store.YES,
		                Field.Index.ANALYZED));
				doc.add(new Field("feature", featureVector,
		                Field.Store.YES,
		                Field.Index.NOT_ANALYZED));
				
				writer.addDocument(doc);
			}
			
			bf.close();
			reader.close();
			
			writer.optimize();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void exportBrownClusterRatinov_old (int clusterNum) {
		try {
			
			String inputTextStr = "/shared/shelley/yqsong/Ratinov/brown-rcv1.clean.tokenized-CoNLL03.txt-c" + clusterNum + "-freq1.txt";
			String outputDirStr = "/shared/saruman/yqsong/data/wordDist/brown_cluster_Ratinov_" + clusterNum + "_old";
			Directory indexDir = FSDirectory.open(new File(outputDirStr));
			IndexWriter writer = new IndexWriter (indexDir, new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.LIMITED); 
			
			
			FileReader reader = new FileReader (inputTextStr);
			BufferedReader bf = new BufferedReader (reader);
			
			String line = "";
			String[] tokens = null;
			
			int dimMax = 0;
			int count = 0;
			while ((line = bf.readLine()) != null) {
				if (count % 10000 == 0) {
					System.out.println("Scanned " + count + " words..");
				}
				count++;
				if (line == "") {
					continue;
				}
				tokens = line.trim().split("\t");
				if (tokens.length != 3) {
					continue;
				}
				
				String feature = tokens[0].trim();
				String word = tokens[1].trim();
				
				char[] featureChar = feature.toCharArray();
				
				if (dimMax < featureChar.length) {
					dimMax = featureChar.length;
				}
				
			}
			
			bf.close();
			reader.close();
			
			
			reader = new FileReader (inputTextStr);
			bf = new BufferedReader (reader);
			
			line = "";
			tokens = null;
			
			count = 0;
			while ((line = bf.readLine()) != null) {
				if (count % 10000 == 0) {
					System.out.println("Processed " + count + " words..");
				}
				count++;
				if (line == "") {
					continue;
				}
				tokens = line.trim().split("\t");
				if (tokens.length != 3) {
					continue;
				}
				
				String feature = tokens[0].trim();
				String word = tokens[1].trim();
				
				if (word.equals("mac")) {
					int stop = 0;
					int a = stop;
				}
				
				char[] featureChar = feature.toCharArray();
				
				String featureVector = "";
				int index = 0;
				for (int i = 0; i < featureChar.length; ++i) {
					if (featureChar[i] == '0') {
						featureVector += "-1" + " ";
					} else {
						featureVector += "1" + " ";
					}
					index++;
				}
				
				for (int i = index; i < dimMax + 1; ++i) {
					featureVector += "0" + " ";
				}
				
				String[] idScoreArray = featureVector.trim().split(" ");
				
				Document doc = new Document();
				doc.add(new Field("word", word,
		                Field.Store.YES,
		                Field.Index.ANALYZED));
				doc.add(new Field("feature", featureVector,
		                Field.Store.YES,
		                Field.Index.NOT_ANALYZED));
				
				writer.addDocument(doc);
			}
			
			bf.close();
			reader.close();
			
			writer.optimize();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void exportBrownCluster20NG (int clusterNum) {
		try {
			
			String inputTextStr = "/shared/shelley/yqsong/brown-cluster-master/20newsgroups-plainText-c" + clusterNum + "-p1.out/paths";
			String outputDirStr = "/shared/saruman/yqsong/data/wordDist/brown_cluster_20NG_" + clusterNum;
			Directory indexDir = FSDirectory.open(new File(outputDirStr));
			IndexWriter writer = new IndexWriter (indexDir, new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.LIMITED); 
			
			
			FileReader reader = new FileReader (inputTextStr);
			BufferedReader bf = new BufferedReader (reader);
			
			String line = "";
			String[] tokens = null;
			
			int count = 0;
			
			reader = new FileReader (inputTextStr);
			bf = new BufferedReader (reader);
			
			line = "";
			tokens = null;
			
			count = 0;
			while ((line = bf.readLine()) != null) {
				if (count % 10000 == 0) {
					System.out.println("Processed " + count + " words..");
				}
				count++;
				if (line == "") {
					continue;
				}
				tokens = line.trim().split("\t");
				if (tokens.length != 3) {
					continue;
				}
				
				String feature = tokens[0].trim();
				String word = tokens[1].trim();
				
				if (word.equals("mac")) {
					int stop = 0;
					int a = stop;
				}
				
				char[] featureChar = feature.toCharArray();
				
				String featureVector = "";
				int clusterID = 0;
				for (int i = 0; i < featureChar.length; ++i) {
					if (featureChar[i] == '0') {
						clusterID = 1 + clusterID * 2;
					} else {
						clusterID = 2 + clusterID * 2;
					}
					
					featureVector += clusterID + ":" + 1 + " ";
				}
				
				Document doc = new Document();
				doc.add(new Field("word", word,
		                Field.Store.YES,
		                Field.Index.ANALYZED));
				doc.add(new Field("feature", featureVector,
		                Field.Store.YES,
		                Field.Index.NOT_ANALYZED));
				
				writer.addDocument(doc);
			}
			
			bf.close();
			reader.close();
			
			writer.optimize();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void exportBrownCluster20NG_old (int clusterNum) {
		try {
			
			String inputTextStr = "/shared/shelley/yqsong/brown-cluster-master/20newsgroups-plainText-c" + clusterNum + "-p1.out/paths";
			String outputDirStr = "/shared/saruman/yqsong/data/wordDist/brown_cluster_20NG_" + clusterNum + "_old";
			Directory indexDir = FSDirectory.open(new File(outputDirStr));
			IndexWriter writer = new IndexWriter (indexDir, new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.LIMITED); 
			
			
			FileReader reader = new FileReader (inputTextStr);
			BufferedReader bf = new BufferedReader (reader);
			
			String line = "";
			String[] tokens = null;
			
			int dimMax = 0;
			int count = 0;
			while ((line = bf.readLine()) != null) {
				if (count % 10000 == 0) {
					System.out.println("Scanned " + count + " words..");
				}
				count++;
				if (line == "") {
					continue;
				}
				tokens = line.trim().split("\t");
				if (tokens.length != 3) {
					continue;
				}
				
				String feature = tokens[0].trim();
				String word = tokens[1].trim();
				
				char[] featureChar = feature.toCharArray();
				
				if (dimMax < featureChar.length) {
					dimMax = featureChar.length;
				}
				
			}
			
			bf.close();
			reader.close();
			
			
			reader = new FileReader (inputTextStr);
			bf = new BufferedReader (reader);
			
			line = "";
			tokens = null;
			
			count = 0;
			while ((line = bf.readLine()) != null) {
				if (count % 10000 == 0) {
					System.out.println("Processed " + count + " words..");
				}
				count++;
				if (line == "") {
					continue;
				}
				tokens = line.trim().split("\t");
				if (tokens.length != 3) {
					continue;
				}
				
				String feature = tokens[0].trim();
				String word = tokens[1].trim();
				
				if (word.equals("mac")) {
					int stop = 0;
					int a = stop;
				}
				
				char[] featureChar = feature.toCharArray();
				
				String featureVector = "";
				int index = 0;
				for (int i = 0; i < featureChar.length; ++i) {
					if (featureChar[i] == '0') {
						featureVector += "-1" + " ";
					} else {
						featureVector += "1" + " ";
					}
					index++;
				}
				
				for (int i = index; i < dimMax + 1; ++i) {
					featureVector += "0" + " ";
				}
				
				String[] idScoreArray = featureVector.trim().split(" ");
				
				Document doc = new Document();
				doc.add(new Field("word", word,
		                Field.Store.YES,
		                Field.Index.ANALYZED));
				doc.add(new Field("feature", featureVector,
		                Field.Store.YES,
		                Field.Index.NOT_ANALYZED));
				
				writer.addDocument(doc);
			}
			
			bf.close();
			reader.close();
			
			writer.optimize();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void exportBrownClusterWiki (int clusterNum) {
		try {
			
			String inputTextStr = "/shared/shelley/yqsong/brown-cluster-master/export_text_lines_vivek-c" + clusterNum + "-p1.out/paths";
			String outputDirStr = "/shared/saruman/yqsong/data/wordDist/brown_cluster_" + clusterNum;
			Directory indexDir = FSDirectory.open(new File(outputDirStr));
			IndexWriter writer = new IndexWriter (indexDir, new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.LIMITED); 
			
			
			FileReader reader = new FileReader (inputTextStr);
			BufferedReader bf = new BufferedReader (reader);
			
			String line = "";
			String[] tokens = null;
			
			int count = 0;
			
			reader = new FileReader (inputTextStr);
			bf = new BufferedReader (reader);
			
			line = "";
			tokens = null;
			
			count = 0;
			while ((line = bf.readLine()) != null) {
				if (count % 10000 == 0) {
					System.out.println("Processed " + count + " words..");
				}
				count++;
				if (line == "") {
					continue;
				}
				tokens = line.trim().split("\t");
				if (tokens.length != 3) {
					continue;
				}
				
				String feature = tokens[0].trim();
				String word = tokens[1].trim();
				
				if (word.equals("mac")) {
					int stop = 0;
					int a = stop;
				}
				
				char[] featureChar = feature.toCharArray();
				
				String featureVector = "";
				int clusterID = 0;
				for (int i = 0; i < featureChar.length; ++i) {
					if (featureChar[i] == '0') {
						clusterID = 1 + clusterID * 2;
					} else {
						clusterID = 2 + clusterID * 2;
					}
					
					featureVector += clusterID + ":" + 1 + " ";
				}
				
				Document doc = new Document();
				doc.add(new Field("word", word,
		                Field.Store.YES,
		                Field.Index.ANALYZED));
				doc.add(new Field("feature", featureVector,
		                Field.Store.YES,
		                Field.Index.NOT_ANALYZED));
				
				writer.addDocument(doc);
			}
			
			bf.close();
			reader.close();
			
			writer.optimize();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void exportBrownClusterWiki_old (int clusterNum) {
		try {
			
			String inputTextStr = "/shared/shelley/yqsong/brown-cluster-master/export_text_lines_vivek-c" + clusterNum + "-p1.out/paths";
			String outputDirStr = "/shared/saruman/yqsong/data/wordDist/brown_cluster_" + clusterNum + "_old";
			Directory indexDir = FSDirectory.open(new File(outputDirStr));
			IndexWriter writer = new IndexWriter (indexDir, new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.LIMITED); 
			
			
			FileReader reader = new FileReader (inputTextStr);
			BufferedReader bf = new BufferedReader (reader);
			
			String line = "";
			String[] tokens = null;
			
			int dimMax = 0;
			int count = 0;
			while ((line = bf.readLine()) != null) {
				if (count % 10000 == 0) {
					System.out.println("Scanned " + count + " words..");
				}
				count++;
				if (line == "") {
					continue;
				}
				tokens = line.trim().split("\t");
				if (tokens.length != 3) {
					continue;
				}
				
				String feature = tokens[0].trim();
				String word = tokens[1].trim();
				
				char[] featureChar = feature.toCharArray();
				
				if (dimMax < featureChar.length) {
					dimMax = featureChar.length;
				}
				
			}
			
			bf.close();
			reader.close();
			
			
			reader = new FileReader (inputTextStr);
			bf = new BufferedReader (reader);
			
			line = "";
			tokens = null;
			
			count = 0;
			while ((line = bf.readLine()) != null) {
				if (count % 10000 == 0) {
					System.out.println("Processed " + count + " words..");
				}
				count++;
				if (line == "") {
					continue;
				}
				tokens = line.trim().split("\t");
				if (tokens.length != 3) {
					continue;
				}
				
				String feature = tokens[0].trim();
				String word = tokens[1].trim();
				
				if (word.equals("mac")) {
					int stop = 0;
					int a = stop;
				}
				
				char[] featureChar = feature.toCharArray();
				
				String featureVector = "";
				int index = 0;
				for (int i = 0; i < featureChar.length; ++i) {
					if (featureChar[i] == '0') {
						featureVector += "-1" + " ";
					} else {
						featureVector += "1" + " ";
					}
					index++;
				}
				
				for (int i = index; i < dimMax + 1; ++i) {
					featureVector += "0" + " ";
				}
				
				String[] idScoreArray = featureVector.trim().split(" ");
				
				Document doc = new Document();
				doc.add(new Field("word", word,
		                Field.Store.YES,
		                Field.Index.ANALYZED));
				doc.add(new Field("feature", featureVector,
		                Field.Store.YES,
		                Field.Index.NOT_ANALYZED));
				
				writer.addDocument(doc);
			}
			
			bf.close();
			reader.close();
			
			writer.optimize();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public static void exportWord2Vector (int dim) {
		try {
//			String inputTextStr = "/shared/saruman/yqsong/word2vector-convertor/vectors-enwiki9.txt";
//			String outputDirStr = "/shared/saruman/yqsong/data/wordDist/enwiki9";
			
			String inputTextStr = "/shared/shelley/yqsong/word2vector-convertor/vectors-enwikitext_vivek" + dim + ".txt";
			String outputDirStr = "/shared/saruman/yqsong/data/wordDist/enwiki_vivek_" + dim;
			Directory indexDir = FSDirectory.open(new File(outputDirStr));
			IndexWriter writer = new IndexWriter (indexDir, new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.LIMITED); 
			
			FileReader reader = new FileReader (inputTextStr);
			BufferedReader bf = new BufferedReader (reader);
			
			String line = "";
			line = bf.readLine();
			String[] tokens = line.split(" ");
			int termNum = Integer.parseInt(tokens[0].trim());
			int dimNum = Integer.parseInt(tokens[1].trim());
			
			int count = 0;
			while ((line = bf.readLine()) != null) {
				if (count % 10000 == 0) {
					System.out.println("Processed " + count + " words..");
				}
				count++;
				if (line == "") {
					continue;
				}
				tokens = line.trim().split(" ");
				if (tokens.length != dimNum + 1) {
					throw new Exception();
				}
				
				String word = tokens[0].trim();
				String feature = "";
				for (int i = 1; i < tokens.length; ++i) {
					feature += tokens[i].trim() + " ";
				}
				
				Document doc = new Document();
				doc.add(new Field("word", word,
		                Field.Store.YES,
		                Field.Index.ANALYZED));
				doc.add(new Field("feature", feature,
		                Field.Store.YES,
		                Field.Index.NOT_ANALYZED));
				
				writer.addDocument(doc);
			}
			
			bf.close();
			reader.close();
			
			writer.optimize();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void exportSenna () {
		try {
			String inputTextStr1 = "/shared/saruman/yqsong/senna/embeddings/embeddings.txt";
			String inputTextStr2 = "/shared/saruman/yqsong/senna/hash/words.lst";

			String outputDirStr = "/shared/saruman/yqsong/data/wordDist/senna";
			Directory indexDir = FSDirectory.open(new File(outputDirStr));
			IndexWriter writer = new IndexWriter (indexDir, new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.LIMITED); 
			
			FileReader reader1 = new FileReader (inputTextStr1);
			BufferedReader bf1 = new BufferedReader (reader1);
			FileReader reader2 = new FileReader (inputTextStr2);
			BufferedReader bf2 = new BufferedReader (reader2);
			
			String line1 = "";
			String line2 = "";
			
			int count = 0;
			while (line1 != null) {
				line1 = bf1.readLine();
				line2 = bf2.readLine();

				if (line1 == null && line2 == null) {
					break;
				}
				
				if (count % 10000 == 0) {
					System.out.println("Processed " + count + " words..");
				}
				count++;
				if (line1 == "") {
					continue;
				}
				
				Document doc = new Document();
				doc.add(new Field("word", line2.trim(),
		                Field.Store.YES,
		                Field.Index.ANALYZED));
				doc.add(new Field("feature", line1.trim(),
		                Field.Store.YES,
		                Field.Index.NOT_ANALYZED));
				
				writer.addDocument(doc);
			}
			
			bf1.close();
			reader1.close();
			bf2.close();
			reader2.close();
			
			writer.optimize();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
