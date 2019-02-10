package edu.illinois.cs.cogcomp.classification.representation.esa.simple;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.StopWords;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.representation.QueryPreProcessor;
import edu.illinois.cs.cogcomp.classification.representation.esa.AbstractESA;
import edu.illinois.cs.cogcomp.descartes.AnalyzerFactory;
import edu.illinois.cs.cogcomp.descartes.retrieval.IResult;
import edu.illinois.cs.cogcomp.descartes.retrieval.simple.Searcher;

/**
 * yqsong@illinois.edu
 */

public class SimpleESALocal extends AbstractESA {
	Searcher searcher;

	public SimpleESALocal() {
		initializeLocalESA (AnalyzerFactory.defaultAnalyzerName, new String[]{"title", "text"});
	}
	
	public static void main(String[] args) throws Exception {
		DatalessResourcesConfig.initialization();
		SimpleESALocal esa = new SimpleESALocal();
		
		while (true) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String testStr = "";
			try {
				testStr = br.readLine();
			} catch (IOException ioe) {
				System.out.println("IO error trying to read your name!");
				System.exit(1);
			}

			int count = 0;
			List<ConceptData> cDoc = esa.getConcepts(500, testStr);
			for (ConceptData key : cDoc) {
				System.out.println(key.concept + ", \t" + String.format("%.4f", key.score));
				count++;
			}
			System.out.println("Count: " + count);
		}
	}
	
	private void initializeLocalESA (String languageName, String[] fieldNames) {
		Set<String> stopwordSet = DatalessResourcesConfig.stopwordSet;
		
		searcher = new Searcher(fieldNames, false, stopwordSet, languageName);
		
		try {
			searcher.open(DatalessResourcesConfig.simpleESADocumentIndex);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done.");
	}
	
	public List<ConceptData> getConcepts(int conceptNum, String document) throws Exception {
		
		document = QueryPreProcessor.process(document);

		List<ConceptData> concepts = new ArrayList<ConceptData>();
		
		int count = 0;
		StringBuffer sb = new StringBuffer();
		for (String s : document.split("\\s")) {
			sb.append(s + " ");
			count++;
		}
		
		ArrayList<IResult> search = null;
		try {
			search = searcher.search(document, conceptNum);
//			esa = (Object[]) client.execute("DescartesServer.esa", params);
		} catch (Exception e) {
			throw e;
		}
		if (search != null) {
			for (IResult res : search) {
				concepts.add(new ConceptData(res.getTitle().replaceAll(",", "").replaceAll(";", "").replaceAll("\t", ""), res.getScore()));
//				put(res.getTitle().replaceAll(",", "").replaceAll(";", "").replaceAll("\t", ""), res.getScore());
			}
		}
		
		return concepts;
	}
	
	public List<String> getDocuments(int retrievedNum, String document) throws Exception {

		document = QueryPreProcessor.process(document);

		List<String> retrivedDocuments = new ArrayList<String>();
		
		int count = 0;
		StringBuffer sb = new StringBuffer();
		for (String s : document.split("\\s")) {
			sb.append(s + " ");
			count++;
		}
		
		ArrayList<IResult> search = null;
		try {
			search = searcher.search(document, retrievedNum);
//			esa = (Object[]) client.execute("DescartesServer.esa", params);
		} catch (Exception e) {
			throw e;
		}
		if (search != null) {
			for (IResult res : search) {
				retrivedDocuments.add(res.getDocument().replaceAll("[^a-zA-Z\\s]", ""));
				
//				put(res.getTitle().replaceAll(",", "").replaceAll(";", "").replaceAll("\t", ""), res.getScore());
			}
		}
		
		return retrivedDocuments;
	}

	@Override
	public List<ConceptData> retrieveConcepts(String document, int numConcepts,
			String complexVectorType) throws Exception {
		return getConcepts(numConcepts, document);
	}

	@Override
	public List<ConceptData> retrieveConcepts(String document, int numConcepts)
			throws Exception {
		return getConcepts(numConcepts, document);
	}
	
}
