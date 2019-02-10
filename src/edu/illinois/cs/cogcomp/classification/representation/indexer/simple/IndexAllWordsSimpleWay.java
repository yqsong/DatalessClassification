package edu.illinois.cs.cogcomp.classification.representation.indexer.simple;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.PriorityQueue;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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

import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.KeyValuePair;


/**
 * yqsong@illinois.edu
 */

public class IndexAllWordsSimpleWay {
	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
		IndexAllWordsSimpleWay indexer = new IndexAllWordsSimpleWay();
		indexer.sortTermDataToDB();
	}
	
	private static IndexReader originalWikiIndexReader = null;
	static IndexWriter indexPruneWordWriter = null;
	
	static String inputFile = "data/WikiLuceneIndex_word500_link30";
	static String outputWordIndexFile = "data/wikiLuceneIndex_word500_link30_wordindex_1000concepts_direct_search";

	HashMap<String, Float> idfMap = new HashMap<String, Float>(3000000);
	HashMap<String, Integer> termHash = new HashMap<String, Integer>(3000000);

	public void retrieveIDF () {
	    TermEnum tnum;
		try {
			Directory originalWikiIndexDir = FSDirectory.open(new File(inputFile));
			originalWikiIndexReader = IndexReader.open(originalWikiIndexDir,true);
			
			Directory outputPruneWordIndexDir = FSDirectory.open(new File(outputWordIndexFile));
			indexPruneWordWriter = new IndexWriter(outputPruneWordIndexDir, new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.UNLIMITED); 

			tnum = originalWikiIndexReader.terms();
			int numDocs = originalWikiIndexReader.numDocs();
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
		    	idf = (float)(Math.log((numDocs+Double.MIN_NORMAL)/(double)(tfreq+Double.MIN_NORMAL))); 	
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
	
	public void sortTermDataToDB() {
		try {
			
			retrieveIDF ();
			
			System.out.println("Total: " + idfMap.keySet().size() + " words");
			
			IndexSearcher searcher = new IndexSearcher(originalWikiIndexReader);
		
		    
		    int count = 0;
		    Calendar cal = Calendar.getInstance();
		    long startTime = cal.getTimeInMillis();
		 
		    for(String tk : idfMap.keySet()){
		    	
		    	count++;
		    	
		    	if (count % 100 == 0) {
		    		System.out.println("Modified words: " + count);
		    	}
		    	
//		    	System.out.println("Modified words: " + count + "\tword: " + tk);
		    	Calendar cal1 = Calendar.getInstance();
		    	long singleStartTime = cal1.getTimeInMillis();
		    	
		    	Query query = new TermQuery(new Term("contents", tk));  
		    	TopDocs docs = searcher.search(query, null, 3000);
		    	ScoreDoc[] hits = docs.scoreDocs;
		    	
		    	PriorityQueue<KeyValuePair> pqHeap = new PriorityQueue<KeyValuePair> ();
		    	
		    	String vectorTitle = "";
		    	String vectorID = "";
		    	
		    	for (int i = 0; i < hits.length; ++i) {
		    		int docID = hits[i].doc;
		    		Document doc = originalWikiIndexReader.document(docID);
		    		String wikiID = doc.get("id");
		    		String wikiTitle = doc.get("title");
		    		wikiTitle = wikiTitle.replace(",", " ").replace(";", " ").replace("\\s+", " " );
		    		
		    		vectorID += wikiID + "," + hits[i].score + ";";
		    		vectorTitle += wikiTitle + "," + hits[i].score + ";";
		    	}
		    	
		    	
		    	
		    	Document doc = new Document();
    			doc.add(new Field("termStr", tk, Field.Store.YES, Field.Index.NOT_ANALYZED));
    			doc.add(new Field("idVector", vectorID, Field.Store.YES, Field.Index.NOT_ANALYZED));
    			doc.add(new Field("titleVector", vectorTitle, Field.Store.YES, Field.Index.NOT_ANALYZED));
    			doc.add(new Field("idf", idfMap.get(tk).toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    			indexPruneWordWriter.addDocument(doc);
    			
    			Calendar cal2 = Calendar.getInstance();
		    	long endTime = cal2.getTimeInMillis();
		    	long second = (endTime - startTime)/1000;
		    	
		    	if (count % 100 == 0) {
		    		System.out.println("Modified words: " + count + ", Elipsed time: " + second + " seconds");
		    	}
		    	
		    }
		    
		    indexPruneWordWriter.optimize();
		    indexPruneWordWriter.close();
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
