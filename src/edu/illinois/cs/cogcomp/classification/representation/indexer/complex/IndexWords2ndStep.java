package edu.illinois.cs.cogcomp.classification.representation.indexer.complex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeMap;

import org.apache.log4j.Logger;
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

import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.KeyValuePair;


/**
 * yqsong@illinois.edu
 */

public class IndexWords2ndStep {

	private static IndexReader originalWikiIndexReader = null;
	private static IndexWriter indexPruneWordWriter = null;
	private static IndexSearcher searcher = null;
	private static Directory originalWikiIndexDir = null;
	private static Directory wikiWordIndexDir = null;
	private static Directory outputPruneWordIndexDir = null;
	
	
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	HashMap<String, Float> idfMap = new HashMap<String, Float>(3000000);
	HashMap<String, Integer> termHash = new HashMap<String, Integer>(3000000);
	
	DecimalFormat df = new DecimalFormat("#.########");
	
	static int HEAP_SIZE = 1000;
	static int stopThreshold = 500000;


	static HashMap<Integer, HashSet<Integer>> conceptInlinkMap = new HashMap<Integer, HashSet<Integer>>();
	int maxInlinkNum = 0;
	
	static String outputStopwordList = "data/stopwordList.txt";
	static String inputFile = "data/WikiLuceneIndex_word500_link30";
	static String inputWordIndexFile = "data/wikipedia_new/WikiLuceneIndex_word500_link30_wordindex_storeall_new_modifyTF";
	static String outputWordIndexPruneLucene = "data/WikiLuceneIndex_word500_link30_wordindex_1000concepts_prune_new_modifyTFandInlink";
	static String pageInLinkFile = "data/wiki_structured/page_inlinks.txt";

	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
		IndexWords2ndStep indexer = new IndexWords2ndStep();

		indexer.sortTermDataToDB(outputStopwordList);
	}
	
	public IndexWords2ndStep () {
		try {

			System.out.println("Register to lucene...");
			originalWikiIndexDir = FSDirectory.open(new File(inputFile));
	    	wikiWordIndexDir = FSDirectory.open(new File(inputWordIndexFile));
			
			outputPruneWordIndexDir = FSDirectory.open(new File(outputWordIndexPruneLucene));
			indexPruneWordWriter = new IndexWriter(outputPruneWordIndexDir, new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.UNLIMITED); 

			System.out.println("Done...");
			
			System.out.println("Read inlinks and outlinks...");
			readInLinksandOutLinks();
			System.out.println("Done...");
	    } catch (Exception ex) {
	    	System.out.println("Cannot create index..." + ex.getMessage());
	    	System.exit(-1);
	    }
	}
	
	public void readInLinksandOutLinks()	{
		FileReader reader;
		BufferedReader br;
		int count = 0;
		try {
			reader = new FileReader(pageInLinkFile);
			br = new BufferedReader(reader);
			
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\t");
				if (tokens.length != 2) {
					continue;
				}
				if (count % 1000000 == 0) {
					System.out.println("Read " + count + " inlinnks");
				}
				count ++;
				
				int id = Integer.parseInt(tokens[0].trim());
				int inlink = Integer.parseInt(tokens[1].trim());
				
				if (conceptInlinkMap.containsKey(id) == true) {
					if (conceptInlinkMap.get(id).contains(inlink) == false) {
						conceptInlinkMap.get(id).add(inlink);
					}
				} else {
					conceptInlinkMap.put(id, new HashSet<Integer>());
					conceptInlinkMap.get(id).add(inlink);
				}
			}
			br.close();
			reader.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (int id : conceptInlinkMap.keySet()) {
			if (conceptInlinkMap.get(id).size() > maxInlinkNum) {
				maxInlinkNum = conceptInlinkMap.get(id).size();
			}
		}
		
	}
	
	
	public void retrieveIDF () {
	    TermEnum tnum;
		try {
			originalWikiIndexReader = IndexReader.open(originalWikiIndexDir,true);
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
	

	void sortTermDataToDB(String outputStopwordList) {
		try {
			
			retrieveIDF ();
			
			System.out.println("Total: " + idfMap.keySet().size() + " words");
			
			IndexReader readeroutput = IndexReader.open(wikiWordIndexDir, true);
			searcher = new IndexSearcher(readeroutput);
		
		    
		    int count = 0;
		    Calendar cal = Calendar.getInstance();
		    long startTime = cal.getTimeInMillis();
		 
		    for(String tk : idfMap.keySet()){
		    	
		    	System.out.println(tk);
		    	
		    	count++;
		    	
		    	if (count % 100 == 0) {
		    		System.out.println("[Modified words]: " + count);
		    	}
		    	
		    	Calendar cal1 = Calendar.getInstance();
		    	long singleStartTime = cal1.getTimeInMillis();
		    	
		    	// begin analysis
		    	
		    	String[] sortedFieldArray = new String[] {"tf", "tfidf", "tfidfnorm"};

		    	String[] tfVectorArray = new String[sortedFieldArray.length];
		    	String[] tfVectorBoostedArray = new String[sortedFieldArray.length];
		    	
		    	Query query = new TermQuery(new Term("termStr", tk));  
		    	TopDocs docs = searcher.search(query, null, 2000000);
		    	ScoreDoc[] hits = docs.scoreDocs;
		    	
//		    	System.out.println("Hits: " + hits.length);
		    	
		    	if (hits.length > stopThreshold || tk.length() <= 2) {
		    		System.out.println("[Stop word]: " + tk + "\t" + hits.length);
		    		continue;
		    	}
		    	
		    	for (int fieldIter = 0; fieldIter < sortedFieldArray.length; ++fieldIter) {
		    		String sortField = sortedFieldArray[fieldIter];
		    		
			    	PriorityQueue<KeyValuePair> pqHeap = new PriorityQueue<KeyValuePair> ();
			    	
			    	Hashtable<Integer, Double> wikiIDFHash = new Hashtable<Integer, Double>();
			    	for (int i = 0; i < Math.min(hits.length, HEAP_SIZE); ++i) {
			    		int docID = hits[i].doc;
			    		Document doc = readeroutput.document(docID);
//		    			int termID = Integer.parseInt(doc.get("termID"));
//		    			String termStr = doc.get("termStr");
		    			int wikiID = Integer.parseInt(doc.get("wikiID"));
		    			double tfidf = Double.parseDouble(doc.get(sortField));
		    			
		    			if (tfidf > 0) {
			    			wikiIDFHash.put(wikiID, tfidf);
			    			KeyValuePair kvp = new KeyValuePair(wikiID, tfidf);
			    			pqHeap.add(kvp);
		    			}
			    	}
			    	if (hits.length > HEAP_SIZE) {
			    		for (int i = HEAP_SIZE; i < hits.length; ++i) {
			    			int docID = hits[i].doc;
				    		Document doc = readeroutput.document(docID);
//			    			int termID = Integer.parseInt(doc.get("termID"));
//			    			String termStr = doc.get("termStr");
			    			int wikiID = Integer.parseInt(doc.get("wikiID"));
			    			double tfidf = Double.parseDouble(doc.get(sortField));
			    			
			    			wikiIDFHash.put(wikiID, tfidf);
			    			if (tfidf > pqHeap.peek().value) {
			    				KeyValuePair kvp = new KeyValuePair(wikiID, tfidf);
			    				pqHeap.remove();
				    			pqHeap.add(kvp);
			    			}
			    		}
			    	}
			    	
			    	List<KeyValuePair> kvpList = new ArrayList<KeyValuePair>();
			    	while (pqHeap.size() > 0) {
			    		kvpList.add(pqHeap.remove());
			    	}
			    	Collections.reverse(kvpList);
			    	
					String tfVector = "";
			    	for(KeyValuePair kvp : kvpList){
			    		int index = kvp.key;
			    		double score = kvp.value;
			    		tfVector += index + ",";
		    			tfVector += score + ";";
			    	}
			    	
			    	tfVectorArray[fieldIter] = tfVector;
			    	
			    	
			    	// boost concepts with links
			    	HashMap<Integer, Double> oldScores = new HashMap<Integer, Double>();
			    	HashMap<Integer, Double> newScores = new HashMap<Integer, Double>();
			    	HashSet<Integer> idSet = new HashSet<Integer>();
			    	HashMap<Integer, Double> conceptWeightMap = new HashMap<Integer, Double>();
			    	for (int i = 0; i < kvpList.size(); ++i) {
			    		int id = kvpList.get(i).key;
			    		double value = kvpList.get(i).value;
			    		idSet.add(id);
			    		
			    		double conceptInlinkWeight = 1;
			    		if (conceptInlinkMap.get(id) != null && conceptInlinkMap.get(id).size() > 10) {
			    			conceptInlinkWeight = Math.log10(conceptInlinkMap.get(id).size());
			    		}
			    		oldScores.put(id, value * conceptInlinkWeight);
			    		
			    		conceptWeightMap.put(id, conceptInlinkWeight);
			    	}
			    	
			    	//check links
			    	HashMap<Integer, HashSet<Integer>> idLinks = new HashMap<Integer, HashSet<Integer>>();
			    	for (Integer id : idSet) {
			    		HashSet<Integer> inlinkSet = conceptInlinkMap.get(id);
			    		if (idLinks.containsKey(id) == false) {
			    			idLinks.put(id, new HashSet<Integer>());
			    		}
			    		if (inlinkSet != null && inlinkSet.size() > 0) {
			    			for (Integer linkID : inlinkSet) {
				    			if (idSet.contains(linkID) == true) {
				    				if (idLinks.containsKey(linkID) == false) {
						    			idLinks.put(linkID, new HashSet<Integer>());
						    		}
					    			
					    			if (idLinks.get(id).contains(linkID) == false) {
					    				idLinks.get(id).add(linkID);
					    			}
					    			if (idLinks.get(linkID).contains(id) == false) {
					    				idLinks.get(linkID).add(id);
					    			}
				    			}
				    		}
			    		}
			    		
			    	}
			    	// generate new scores
			    	for (Integer id : idSet) {
			    		double value = oldScores.get(id);
			    		HashSet<Integer> linkIds = idLinks.get(id);
			    		if (linkIds != null && linkIds.size() > 0) {
			    			for (Integer linkId : linkIds) {
			    				if (linkId != id) {
			    					value += 0.5 * oldScores.get(linkId);
			    				}
			    			}
			    		}
			    		newScores.put(id, value);
			    	}
			    	
			    	String boostedVector = "";
			    	TreeMap<Integer, Double> sortedMap = HashSort.sortByValues(newScores);
			    	for (Integer id : sortedMap.keySet()) {
			    		double value = newScores.get(id);
			    		
			    		boostedVector += id + ",";
			    		boostedVector += value + ";";
			    	}
			    	
			    	tfVectorBoostedArray[fieldIter] = boostedVector;
			    	
		    	}
		    	

		    	Document doc = new Document();
		    	
    			doc.add(new Field("termStr", tk, Field.Store.YES, Field.Index.NOT_ANALYZED));
    			
    			doc.add(new Field("tfVector", tfVectorArray[0].toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    			doc.add(new Field("tfidfVector", tfVectorArray[1].toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    			doc.add(new Field("tfidfnormVector", tfVectorArray[2].toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    			
    			doc.add(new Field("tfboostVector", tfVectorBoostedArray[0].toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    			doc.add(new Field("tfidfboostVector", tfVectorBoostedArray[1].toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    			doc.add(new Field("tfidfboostnormVector", tfVectorBoostedArray[2].toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    			
    			doc.add(new Field("idf", idfMap.get(tk).toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    			indexPruneWordWriter.addDocument(doc);
    			
    			Calendar cal2 = Calendar.getInstance();
		    	long endTime = cal2.getTimeInMillis();
		    	long second = (endTime - startTime)/1000;
		    	
		    	if (count % 100 == 0) {
		    		System.out.println("[Elipsed time]: " + second + " seconds");
		    	}
		    	
		    }
		    
		    indexPruneWordWriter.optimize();
		    indexPruneWordWriter.close();
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
