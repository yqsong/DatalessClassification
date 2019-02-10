package edu.illinois.cs.cogcomp.classification.representation.indexer.complex;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


/**
 * yqsong@illinois.edu
 */

public class IndexWords1stStep {

	private static IndexReader reader = null;
	private static IndexWriter indexWriter = null;
	private static IndexSearcher searcher = null;
	private static Directory fsdir = null;
	private static Directory outputDir = null;
	
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	HashMap<String, Float> idfMap = new HashMap<String, Float>(1000000);
	HashMap<String, Integer> termHash = new HashMap<String, Integer>(1000000);
	
	DecimalFormat df = new DecimalFormat("#.########");
	
	static int WINDOW_SIZE = 100;
	static float WINDOW_THRES= 0.005f;

	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
		IndexWords1stStep indexer = new IndexWords1stStep();
		indexer.indexWords();
	}
	
	public IndexWords1stStep () {
		try {
			System.out.println("Register to lucene...");
			fsdir = FSDirectory.open(new File("data/WikiLuceneIndex_word500_link30"));
	    	outputDir = FSDirectory.open(new File("data/WikiLuceneIndex_word500_link30_wordindex_storeall_new_modifyTF"));
			reader = IndexReader.open(fsdir,true);
			indexWriter = new IndexWriter(outputDir, new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.UNLIMITED); 
			
			System.out.println("Done...");
	    } catch (Exception ex) {
	    	System.out.println("Cannot create index..." + ex.getMessage());
	    	System.exit(-1);
	    }
	}
	
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
	
	public void indexWords() {
		retrieveIDF ();
		
		int maxid = reader.maxDoc();
		int wikiID = -1;
    	TermFreqVector tv = null;
	    String[] terms;
	    String term = "";
	    double sum = 0;
	    float idf, tf, tfidf;
	    
	    Calendar cal = Calendar.getInstance();
	    long startTime = cal.getTimeInMillis();
	    
	    for(int i = 0; i < maxid; i++){
	    	if (i % 1000 == 0) {
	    		System.out.println("Processed: " + i + " out of " + maxid + " documents..");
	    		
	    		Calendar cal1 = Calendar.getInstance();
	    		long endTime = cal1.getTimeInMillis();
	    		long second = (endTime - startTime)/1000;
	    		System.out.println("Elipsed time: " + second + " seconds");
	    	}

    		try {
		    	if(!reader.isDeleted(i)){
		    		wikiID = Integer.valueOf(reader.document(i).getField("id").stringValue());
		    			    		
		    		tv = reader.getTermFreqVector(i, "contents");
		    		HashMap<String, Float> tfidfMap = new HashMap<String, Float>();
		    		
	    			terms = tv.getTerms();
	    			int[] fq = tv.getTermFrequencies();
		    		sum = 0.0;	   
		    		tfidfMap.clear();
		    		// for all terms of a document
		    		for(int k=0;k<terms.length;k++){
		    			term = terms[k];
		    			if(!idfMap.containsKey(term))
		    				continue;
		    			tf = (float) (1.0 + Math.log(fq[k]));
		    			idf = idfMap.get(term);
		    			tfidf = (float) (tf * idf);
		    			tfidfMap.put(term, tfidf);
		    			sum += tfidf * tfidf;
		    		}
		    		sum = Math.sqrt(sum);
		    		// for all terms of a document
		    		for(int k=0;k<terms.length;k++){
		    			term = terms[k];
		    			tf = (float) ( 1.0 + Math.log(fq[k]) );
		    			if(!idfMap.containsKey(term))
		    				continue;
		    			double tfidf1 = (float) (tfidfMap.get(term));
		    			double tfidf2 = (float) (tfidfMap.get(term) / sum);

		    			Document doc = new Document();
		    			doc.add(new Field("termID", termHash.get(term)+"", Field.Store.YES, Field.Index.NOT_ANALYZED));
		    			doc.add(new Field("termStr", term, Field.Store.YES, Field.Index.NOT_ANALYZED));
		    			doc.add(new Field("wikiID", wikiID+"", Field.Store.YES, Field.Index.NOT_ANALYZED));
		    			doc.add(new Field("tf", df.format(tf)+"", Field.Store.YES, Field.Index.NOT_ANALYZED));
		    			doc.add(new Field("tfidf", df.format(tfidf1)+"", Field.Store.YES, Field.Index.NOT_ANALYZED));
		    			doc.add(new Field("tfidfnorm", df.format(tfidf2)+"", Field.Store.YES, Field.Index.NOT_ANALYZED));
						indexWriter.addDocument(doc);
		    		}
	    		}
    		}
    		catch(Exception e){
    			e.printStackTrace();
    			System.out.println("ERR: " + wikiID + " " + tv);
    			continue;
    		}
	    		
	    }
	    try {
	    	indexWriter.optimize();
	    	indexWriter.close();
	    	System.out.println("Finished");
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	   
	}
	
	private class TermData  implements Comparable<TermData> {
		public int termID;
		public String termStr;
		public int wikiID;
		public double tfidf;
		@Override
		public int compareTo(TermData termData) {
			if (tfidf > termData.tfidf)
				return 1;
			else if ((tfidf < termData.tfidf))
				return -1;
			else
				return 0;
		}
	}
}
