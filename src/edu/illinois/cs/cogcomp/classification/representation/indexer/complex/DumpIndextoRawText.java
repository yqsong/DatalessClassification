package edu.illinois.cs.cogcomp.classification.representation.indexer.complex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.TermData;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.DiskBasedComplexESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.SemanticGraphCut;

public class DumpIndextoRawText {
	
	private static IndexReader reader = null;
	private static IndexWriter indexWriter = null;
	private static IndexSearcher searcher = null;
	private static Directory fsdir = null;
	private static Directory outputDir = null;
	
	public static void main (String[] args){
		
		try {
			System.out.println("Register to lucene...");
			fsdir = FSDirectory.open(new File("/shared/bronte/sling3/data/WikiLuceneIndex_word500_link30_wordindex_1000concepts_prune_new_modifyTFandInlink/"));
	    	//outputDir = FSDirectory.open(new File("data/WikiLuceneIndex_word500_link30_wordindex_storeall_new_modifyTF"));
			reader = IndexReader.open(fsdir,true);
			getConceptVectorBasedonTFIDF(DiskBasedComplexESA.searchTypes[1]);
			System.out.println("Done...");
	    } catch (Exception ex) {
	    	System.out.println("Cannot create index..." + ex.getMessage());
	    	System.exit(-1);
	    }
		
	}
	
	HashMap<String, Float> idfMap = new HashMap<String, Float>(1000000);
	HashMap<String, Integer> termHash = new HashMap<String, Integer>(1000000);

	public void retrieveIDF () {
	    TermEnum tnum;
		try {
			tnum = reader.terms();
			int numDocs = reader.numDocs();
		    Term t;
		    int hashInt = 0;
		    String term = "";
		    int tfreq = 0;
		    float idf;
		    int wordCount = 0;
		    while(tnum.next()){
		    	t = tnum.term();
		    	term = t.text();
		    	tfreq = tnum.docFreq();	// get DF for the term
		    	// skip rare terms
		    	if(tfreq < 3){
		    		continue;
		    	}
		    	// idf = (float)(Math.log(numDocs/(double)(tfreq+1)) + 1.0);
		    	idf = (float)(Math.log((numDocs+Double.MIN_NORMAL)/(float)(tfreq+Double.MIN_NORMAL))); 	
		    	// idf = (float)(Math.log(numDocs/(double)(tfreq)) / Math.log(2)); 	
		    	idfMap.put(term, idf);
		    	termHash.put(term, hashInt++);
		    	wordCount++;
		    }
		    
			System.out.println("In total: " + wordCount + " terms.");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//ComplexESALocal.searchTypes[1]
	//termStr \t conceptID1,conceptScore1;...
	public static void getConceptVectorBasedonTFIDF(String vectorField){
    	try {
    		FileWriter writer = new FileWriter("/shared/bronte/sling3/data/MemoryBasedESA.txt");
    		int count=0;
    		int numDocs = reader.numDocs();
    		for (int i = 0; i < numDocs; ++i) {
    			Document doc = reader.document(i);
    			String termStr = doc.get("termStr").trim();
    			String idf = doc.get("idf");
    			String termVector = doc.get(vectorField).trim();
    			writer.write(termStr + "\t"+idf+"\t");
    			writer.write(termVector+ ClassifierConstant.systemNewLine);
    			if(count%50000==0) System.out.print("Processed word number "+count+"\n");
    			count++;
//    			String[] idScoreArray = termVector.split(";");
//    			for (int k = 0; k < idScoreArray.length; ++k) {
//    				String idScore = idScoreArray[k];
//    				String[] id_Score = idScore.trim().split(",");
//    				if (id_Score.length != 2) 
//    					continue;
//    				int wikiID = Integer.parseInt(id_Score[0]); 
//    				double score = Double.parseDouble(id_Score[1]);
//    			}
    		}
    		writer.close();
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
	}
	
}
