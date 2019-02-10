package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.nytimes;

import java.io.File;
import java.util.Random;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts.AbstractCorpusConceptData;

/**
 * yqsong@illinois.edu
 */

public class NYTimesCorpusConceptData extends AbstractCorpusConceptData {

	public NYTimesCorpusConceptData () {
		super();
	}
	
	@Override
	public void readCorpusContentOnly(String file, Random random, double trainingRate) {
		try {
			Directory inputDir = FSDirectory.open(new File(file));
			IndexReader reader = IndexReader.open(inputDir, true);
			int maxDocNum = reader.maxDoc();
			for (int i = 0; i < maxDocNum; ++i) {
				if (reader.isDeleted(i) == false) { 
					if (i % 10000 == 0) {
						System.out.println ("[Read NYTimes Data: ] " + i + "docs ..");
					}
					Document doc = reader.document(i);
					String url = doc.get("Url");
					String headLead = doc.get("Headline") + " " + doc.get("Lead Paragraph"); //doc.get("Body");
					String headBody = doc.get("Headline") + " " + doc.get("Body"); //doc.get("Body");
					String body = headLead.replaceAll("\t", " ").replaceAll("\n", " ").replaceAll("\r", " ").replaceAll("\\s+", " ");
					if (url != null && body != null) {
						corpusContentMap.put(url, body);
						if (random.nextDouble() < trainingRate) {
				     		corpusContentMapTraining.put(url, body);
			     		} else {
				     		corpusContentMapTest.put(url, body);
			     		}
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void readCorpusContentOnly(String file, int readNum, Random random, double trainingRate) {
		try {
			Directory inputDir = FSDirectory.open(new File(file));
			IndexReader reader = IndexReader.open(inputDir, true);
			for (int i = 0; i < readNum; ++i) {
				if (reader.isDeleted(i) == false) { 
					if (i % 10000 == 0) {
						System.out.println ("[Read NYTimes Data: ] " + i + "docs ..");
					}
					Document doc = reader.document(i);
					String url = doc.get("Url");
//					String headLead = doc.get("Headline") + " " + doc.get("Lead Paragraph"); //doc.get("Body");
					String headBody = doc.get("Headline") + " " + doc.get("Body"); //doc.get("Body");
					String body = headBody.replaceAll("\t", " ").replaceAll("\n", " ").replaceAll("\r", " ").replaceAll("\\s+", " ");
					if (url != null && body != null) {
						corpusContentMap.put(url, body);
						if (random.nextDouble() < trainingRate) {
				     		corpusContentMapTraining.put(url, body);
			     		} else {
				     		corpusContentMapTest.put(url, body);
			     		}
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
