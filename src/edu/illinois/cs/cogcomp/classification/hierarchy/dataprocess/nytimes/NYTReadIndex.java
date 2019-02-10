package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.nytimes;

import java.io.File;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * yqsong@illinois.edu
 */

public class NYTReadIndex {

	public static void main (String[] args) {
		try {
			String inputDirStr = "D:/yqsong/data/nytimes/NYTIndex_Year/2006";
			Directory inputDir = FSDirectory.open(new File(inputDirStr));
			IndexReader reader = IndexReader.open(inputDir, true);
			int maxDocNum = 1000;//reader.maxDoc();
			for (int i = 0; i < maxDocNum; ++i) {
				if (reader.isDeleted(i) == false) { 
					Document doc = reader.document(i);
					System.out.println(doc.get("Taxonomic Classifiers"));
					
//					List<Fieldable> fields = doc.getFields();
//					for (int j = 0; j < fields.size(); ++j) {
//						Fieldable field = fields.get(j);
//						System.out.println("IsIndexed: " + field.isIndexed()
//								+ "; IsStored: " + field.isStored()
//								+ "; IsTokenized: " + field.isTokenized()
//								+ "; IsTermVectorStored: " + field.isTermVectorStored());
//						System.out.println("Field: " + field.name());
//						System.out.println(doc.get(field.name()));
//						System.out.println();
//					}
//					System.out.println();
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
