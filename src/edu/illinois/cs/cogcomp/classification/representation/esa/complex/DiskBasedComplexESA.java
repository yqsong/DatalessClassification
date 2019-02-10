package edu.illinois.cs.cogcomp.classification.representation.esa.complex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.TermData;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.representation.QueryPreProcessor;
import edu.illinois.cs.cogcomp.classification.representation.esa.AbstractESA;

/**
 * yqsong@illinois.edu
 */

public class DiskBasedComplexESA extends AbstractESA {
	
	
	private static IndexReader reader = null;
	private static IndexSearcher searcher = null;
	private static Directory inputDir = null;
	public static String tfType = "log";
	
	public static boolean isUseGraphCut = false;
	
	public static String[] searchTypes = new String[] {
		"tfVector",
		"tfidfVector",
		"tfidfnormVector",
		
		"tfboostVector",
		"tfidfboostVector",
		"tfidfboostnormVector",
		
		"simpleVivek"
	};
	
	private HashMap<String, String> pageIdTitleMapping;
	
	public static void main(String[] args) throws Exception {
		DatalessResourcesConfig.initialization();
		 
		DiskBasedComplexESA esa= new DiskBasedComplexESA ();

		List<ConceptData> vectorTopic1 = esa.retrieveConcepts("In article C vIr L r shuksan ds boeing com fredd shuksan Fred Dickey writes CarolinaFan uiuc cka uxa cso uiuc edu wrote I have been active ", 500, "tfidfVector");
		for (int i=0;i<vectorTopic1.size();i++) System.out.print(vectorTopic1.get(i).concept+","+vectorTopic1.get(i).score+";");
		
	}
	
	public DiskBasedComplexESA () {
		
		try {
			File inputFile = new File (DatalessResourcesConfig.complexESAWordIndex);
			System.out.println("Register to lucene: " + inputFile.getAbsolutePath());
			inputDir = FSDirectory.open(inputFile);
			reader = IndexReader.open(inputDir, true);
			searcher = new IndexSearcher(reader);
			System.out.println("Done.");
			
			pageIdTitleMapping = new HashMap<String, String>();
			
			File mappingFile = new File (DatalessResourcesConfig.pageIDMapping);
			System.out.println("Read mapping file: " + mappingFile.getAbsolutePath());
			FileReader mappReader = new FileReader(mappingFile);
			BufferedReader bf = new BufferedReader(mappReader);
			String line = "";
			while ((line = bf.readLine()) != null) {
				if (line.equals("") == true) 
					continue;
				String[] tokens = line.split("\t");
				if (tokens.length != 2)
					continue;
				if (pageIdTitleMapping.containsKey(tokens[0].trim()) == false) {
					pageIdTitleMapping.put(tokens[0], tokens[1]);
				}
			}
			System.out.println("Done.");
		} catch (Exception e) {
	    	e.printStackTrace();
	    	System.exit(-1);
	    }
		
	}

	public List<ConceptData> retrieveConcepts(String query, int topK, String vectorField) {

		query = QueryPreProcessor.process(query);
		
		HashMap<Integer, Double> conceptVector = getConceptVectorBasedonSegmentation(query, topK, vectorField);
		
		List<ConceptData> concepts = new ArrayList<ConceptData>();
		try {
			if (conceptVector == null || conceptVector.size() == 0) 
				return concepts;
			
			for (Integer key : conceptVector.keySet()) {
				double value = conceptVector.get(key);
				ConceptData concept = new ConceptData(key + "", value);
				concepts.add(concept);
			}
			
			Collections.sort(concepts);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return concepts;
	}
	
	public List<ConceptData> retrieveConceptNames(String query, int topK, String vectorField) {
		
		query = QueryPreProcessor.process(query);
		
		HashMap<Integer, Double> conceptVector = getConceptVectorBasedonSegmentation(query, topK, vectorField);
		
		List<ConceptData> concepts = new ArrayList<ConceptData>();
		try {
			if (conceptVector == null || conceptVector.size() == 0) 
				return concepts;
			
			for (Integer key : conceptVector.keySet()) {
				double value = conceptVector.get(key);
				ConceptData concept = new ConceptData(pageIdTitleMapping.get(key+"").replaceAll(",", "").replaceAll(";", "").replaceAll("\t", ""), value);
				concepts.add(concept);
			}
			
			Collections.sort(concepts);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return concepts;
	}
	
	public List<ConceptData> translateIDToConceptString (List<ConceptData> conceptList) {
		List<ConceptData> newList = new ArrayList<ConceptData>();
		for (int i = 0; i < conceptList.size(); ++i) {
			String key = conceptList.get(i).concept;
			double value = conceptList.get(i).score;
			ConceptData concept = new ConceptData(pageIdTitleMapping.get(key+"").replaceAll(",", "").replaceAll(";", "").replaceAll("\t", ""), value);
			newList.add(concept);
		}
		
		return newList;
	}
	
	//doc: word1 word2 word1 word3 ...
	//concept1(word1)*tfidf(word1) + concept1(word2)*tfidf(word2)...
	//concept2(word1)*tfidf(word1) + concept2(word2)*tfidf(word2)...
	public HashMap<Integer, Double> getConceptVectorBasedonSegmentation(String query, int topK, String vectorField){

		query = QueryPreProcessor.process(query);
		
		String[] tokens = query.split("\\s+");
    	List<String> newTokens = new ArrayList<String>();
    	for (int i = 0; i < tokens.length; i++) {
    		String word = tokens[i].toLowerCase().trim();
    		newTokens.add(word);
    	}
    	
//    	for (int i = 0; i < newTokens.size(); ++i) {
//    		System.out.println(newTokens.get(i) + "\t");
//    	}
//    	System.out.println();
//    	System.out.println();
    	
		List<HashMap<Integer, Double>> conceptMapList = new ArrayList<HashMap<Integer, Double>>();
		List<Double> weightList = new ArrayList<Double>();
		List<String> termList = new ArrayList<String>();
    	
    	HashMap<String, Double> tfidfMap = new HashMap<String, Double>();
    	for  (int i = 0; i < newTokens.size(); ++i) { 
    		String token = newTokens.get(i);
    		if (tfidfMap.containsKey(token) == false) {
    			tfidfMap.put(token, 1.0);
    		} else {
    			tfidfMap.put(token, tfidfMap.get(token) + 1);
    		}
    	}
    	
    	if(tfidfMap.size() == 0){
        	return null;
        }
    	
    	try {
	    	double vsum = 0;
	    	double maxValue = Collections.max(tfidfMap.values());
	        for(String strTerm : tfidfMap.keySet()) { 
	        	double tf = tfidfMap.get(strTerm);
//	        	double tf = 1.0 + Math.log(tfidfMap.get(strTerm));
				if (tfType.equals("boolean")) {
					tf = 1;
				} else if (tfType.equals("log")) {
					tf = 1 + Math.log(tf);
				} else if (tfType.equals("aug")) {
					tf = 0.5 + (tf * 0.5) / maxValue;
				}
				
	            Query luceneQuery = new TermQuery(new Term("termStr", strTerm));  
		    	TopDocs docs = searcher.search(luceneQuery, null, Double.MAX_EXPONENT);
		    	ScoreDoc[] hits = docs.scoreDocs;
		    	for (int j = 0; j < hits.length; ++j) {
		    		int docID = hits[j].doc;
		    		Document doc = reader.document(docID);
	    			String termStr = doc.get("termStr");
	    			double tfidf = Double.parseDouble(doc.get("idf")) * tf;
	    			if (termStr.equals(strTerm) == false)
	    				continue;
	    			
	    			vsum += tfidf * tfidf;
	    			tfidfMap.put(termStr, tfidf);
		    	}
		        vsum = Math.sqrt(vsum);
	        }
	        
	        for(String strTerm : tfidfMap.keySet()){
	        	double tfidf = tfidfMap.get(strTerm);
	        	tfidfMap.put(strTerm, tfidf / vsum);
	        }
	        
	        for(String strTerm : tfidfMap.keySet()) { 
	            Query luceneQuery = new TermQuery(new Term("termStr", strTerm));  
		    	TopDocs docs = searcher.search(luceneQuery, null, Double.MAX_EXPONENT);
		    	ScoreDoc[] hits = docs.scoreDocs;
		    	for (int j = 0; j < hits.length; ++j) {
		    		int docID = hits[j].doc;
		    		Document doc = reader.document(docID);
	    			String termStr = doc.get("termStr").trim();
	    			String termVector = doc.get(vectorField).trim();
//	    			double tfidf = Double.parseDouble(doc.get("idf").trim()) * tf;
	    			if (termStr.equals(strTerm) == false)
	    				continue;
	    			String[] idScoreArray = termVector.split(";");
	    			
	    			double maxScore = 0;
	    			HashMap<Integer, Double> termConceptMap = new HashMap<Integer, Double>();
	    			for (int k = 0; k < idScoreArray.length; ++k) {
	    				String idScore = idScoreArray[k];
	    				String[] id_Score = idScore.trim().split(",");
	    				if (id_Score.length != 2) 
	    					continue;
	    				int wikiID = Integer.parseInt(id_Score[0]); 
	    				double score = Double.parseDouble(id_Score[1]);
	    				
	    				if (termConceptMap.containsKey(wikiID) == false) {
	    					termConceptMap.put(wikiID, score);
	    				} 
	    			}
	    			
	    			if (termConceptMap.size() > 0 && tfidfMap.containsKey(termStr) == true && tfidfMap.get(termStr) > 0) {
	    				conceptMapList.add(termConceptMap);
		    			weightList.add(tfidfMap.get(termStr));
		    			termList.add(termStr);
	    			}
		    	}
	        }
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	HashMap<Integer, Double> conceptMap = new HashMap<Integer, Double>();
		if (isUseGraphCut == true) {
			conceptMap = SemanticGraphCut.combineVectorsGraph(conceptMapList, weightList, termList);
		} else {
			conceptMap = SemanticGraphCut.combineVectorsSimple(conceptMapList, weightList);
		}
    	
        List<TermData> termDataList = new ArrayList<TermData>();
        for (Integer key : conceptMap.keySet()) {
        	double value = conceptMap.get(key);
        	TermData termData = new TermData();
        	termData.termID = key;
        	termData.tfidf = value;
        	termDataList.add(termData);
        }
        Collections.sort(termDataList);
        
        HashMap<Integer, Double> newConceptVector = new HashMap<Integer, Double>();
		for( int i = termDataList.size() - 1; i >= Math.max(termDataList.size() - topK, 0) && termDataList.get(i).tfidf > 0; i-- ) {
			newConceptVector.put(termDataList.get(i).termID, termDataList.get(i).tfidf / newTokens.size() );
		}
		
		return newConceptVector;
	}
	
	
	public Hashtable<Integer, Double> getConceptVectorBasedonTFIDF(HashMap<String, Double> queryTFIDF, int topK, String vectorField){
		List<HashMap<Integer, Double>> conceptMapList = new ArrayList<HashMap<Integer, Double>>();
		List<Double> weightList = new ArrayList<Double>();
		List<String> termList = new ArrayList<String>();
    	try {
        
	        for(String strTerm : queryTFIDF.keySet()) { 
	            Query luceneQuery = new TermQuery(new Term("termStr", strTerm));  
		    	TopDocs docs = searcher.search(luceneQuery, null, Double.MAX_EXPONENT);
		    	ScoreDoc[] hits = docs.scoreDocs;
		    	for (int j = 0; j < hits.length; ++j) {
		    		int docID = hits[j].doc;
		    		Document doc = reader.document(docID);
	    			String termStr = doc.get("termStr").trim();
	    			String termVector = doc.get(vectorField).trim();
//	    			double tfidf = Double.parseDouble(doc.get("idf").trim()) * tf;
	    			if (termStr.equals(strTerm) == false)
	    				continue;
	    			String[] idScoreArray = termVector.split(";");
	    			
	    			double maxScore = 0;
	    			HashMap<Integer, Double> termConceptMap = new HashMap<Integer, Double>();
	    			for (int k = 0; k < idScoreArray.length; ++k) {
	    				String idScore = idScoreArray[k];
	    				String[] id_Score = idScore.trim().split(",");
	    				if (id_Score.length != 2) 
	    					continue;
	    				int wikiID = Integer.parseInt(id_Score[0]); 
	    				double score = Double.parseDouble(id_Score[1]);
	    				
	    				
	    				if (termConceptMap.containsKey(wikiID) == false) {
	    					termConceptMap.put(wikiID, score);
	    				} 
	    			}
	    			if (termConceptMap.size() > 0 && queryTFIDF.containsKey(termStr) == true && queryTFIDF.get(termStr) > 0) {
	    				conceptMapList.add(termConceptMap);
		    			weightList.add(queryTFIDF.get(termStr));
		    			termList.add(termStr);
	    			}
		    	}
		        
	        }
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	
		HashMap<Integer, Double> conceptMap = new HashMap<Integer, Double>();
		if (isUseGraphCut == true) {
			conceptMap = SemanticGraphCut.combineVectorsGraph(conceptMapList, weightList, termList);
		} else {
			conceptMap = SemanticGraphCut.combineVectorsSimple(conceptMapList, weightList);
		}
		
    	
        List<TermData> termDataList = new ArrayList<TermData>();
        for (Integer key : conceptMap.keySet()) {
        	double value = conceptMap.get(key);
        	TermData termData = new TermData();
        	termData.termID = key;
        	termData.tfidf = value;
        	termDataList.add(termData);
        }
        Collections.sort(termDataList);
        
        Hashtable<Integer, Double> newConceptVector = new Hashtable<Integer, Double>();
		for( int i = termDataList.size() - 1; i >= Math.max(termDataList.size() - topK, 0) && termDataList.get(i).tfidf > 0; i-- ) {
			newConceptVector.put(termDataList.get(i).termID, termDataList.get(i).tfidf / queryTFIDF.size() );
		}
		
		return newConceptVector;
	}

	@Override
	public List<ConceptData> retrieveConcepts(String document, int numConcepts)
			throws Exception {
		return retrieveConcepts(document, numConcepts, searchTypes[1]);
	}
}