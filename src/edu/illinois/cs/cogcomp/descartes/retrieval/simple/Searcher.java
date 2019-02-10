/**
 * 
 */
package edu.illinois.cs.cogcomp.descartes.retrieval.simple;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.descartes.AnalyzerFactory;
import edu.illinois.cs.cogcomp.descartes.retrieval.IResult;
import edu.illinois.cs.cogcomp.descartes.retrieval.ISearcher;
import edu.illinois.cs.cogcomp.descartes.similarity.UnNormalizedLuceneSimilarity;
import edu.illinois.cs.cogcomp.descartes.util.Utilities;

/**
 * @author Vivek Srikumar
 * 
 */
public class Searcher implements ISearcher {
    public static final Version AnalyzerVersion = Version.LUCENE_24;

	protected IndexSearcher searcher;
	protected Directory memoryDirectory;
	
	protected Analyzer analyzer;

	protected String[] fields;
	protected boolean addNGrams;

	private Logger log = LoggerFactory.getLogger(Searcher.class);

	public Searcher(String[] fields, boolean ngrams, Set<String> stopWords, String analyzerType) {

		this.fields = fields;

		this.addNGrams = ngrams;

		this.analyzer = AnalyzerFactory.initialize(analyzerType);
		
	}
	
	public Searcher(String[] fields, boolean ngrams, Set<String> stopWords, Analyzer specifiedAnalyzer) {

		this.fields = fields;

		this.addNGrams = ngrams;

		this.analyzer = specifiedAnalyzer;
	}

	/**
	 * Opens the index created by the Lucene indexer
	 */
	public void open(String indexDirectory) throws IOException {

		File indexDir = new File(indexDirectory);
		

		
		if (!indexDir.exists() || !indexDir.isDirectory()) {
			throw new IOException(indexDir
					+ "does not exist or is not a directory");
		}
		Directory fileDirectory = FSDirectory.open(indexDir);
//		memoryDirectory = new RAMDirectory(fileDirectory);

		searcher = new IndexSearcher(fileDirectory);

		searcher.setSimilarity(new UnNormalizedLuceneSimilarity());

		log.info("Opened index located at " + indexDirectory);

	}
	
	public void open(RAMDirectory memoryDirectory) throws IOException {

		searcher = new IndexSearcher(memoryDirectory);

		searcher.setSimilarity(new UnNormalizedLuceneSimilarity());

		log.info("Opened index located at " + memoryDirectory);

	}

	/**
	 * Searches the index for the query and returns an ArrayList of results
	 */
	public ArrayList<IResult> search(String queryText, int numResults)
			throws Exception {

		ArrayList<IResult> results = new ArrayList<IResult>();

		if (queryText.replaceAll("\\s+", "").length() == 0)
			return results;

		// log.info("Incoming query: " + queryText);

		Query query = makeQuery(queryText);

		// log.info("Done making query. Total time: " + timer.getTimeMillis());

		// log.info(query.toString());

		TopDocs searchResults = searcher.search(query, null, numResults);

		// log.info("Done retrieving results. Total time: "
		// + timer.getTimeMillis());

		for (int i = 0; i < searchResults.scoreDocs.length; i++) {
			Document doc = searcher.doc(searchResults.scoreDocs[i].doc);

			results.add(new Result(Integer
					.toString(searchResults.scoreDocs[i].doc),
					doc.get("title"), doc.get("text"),
					searchResults.scoreDocs[i].score));

		}

		return results;
	}
	
	public List<Integer> searchDocIDs(String queryText, int numResults)
			throws Exception {

		ArrayList<Integer> results = new ArrayList<Integer>();

		if (queryText.replaceAll("\\s+", "").length() == 0)
			return results;

		// log.info("Incoming query: " + queryText);

		Query query = makeQuery(queryText);

		// log.info("Done making query. Total time: " + timer.getTimeMillis());

		// log.info(query.toString());

		TopDocs searchResults = searcher.search(query, null, numResults);

		// log.info("Done retrieving results. Total time: "
		// + timer.getTimeMillis());

		for (int i = 0; i < searchResults.scoreDocs.length; i++) {
			results.add(searchResults.scoreDocs[i].doc);
		}

		return results;
	}



	/**
	 * @param queryText
	 * @return
	 * @throws ParseException
	 * @throws Exception
	 */
	protected Query makeQuery(String queryText) throws Exception {

		BooleanQuery bq = makeInitialQuery(queryText);

		if (addNGrams) {
			bq = addNGrams(queryText, bq);

		}

		// System.out.println(bq.toString());

		return bq;
	}

	/**
	 * @param queryText
	 * @return
	 * @throws ParseException
	 */
	protected BooleanQuery makeInitialQuery(String queryText)
			throws ParseException {
		queryText = queryText.replaceAll("-", " ");
		queryText = queryText.replaceAll("\"", "");
		queryText = queryText.replaceAll("\'", "");

		String finalQuery = queryText;

		Query query = null;

		if (fields.length == 1) {
			QueryParser parser = new QueryParser(AnalyzerVersion,
					fields[0], analyzer);
			query = parser.parse(QueryParser.escape(finalQuery));
		} else {

			MultiFieldQueryParser parser = new MultiFieldQueryParser(
					AnalyzerVersion, fields, analyzer);
			query = parser.parse(QueryParser.escape(finalQuery));
		}

		BooleanQuery.setMaxClauseCount(30000);
		BooleanQuery bq = new BooleanQuery();

		bq.add(query, BooleanClause.Occur.SHOULD);
		return bq;
	}

	/**
	 * @param queryText
	 * @param bq
	 */
	protected BooleanQuery addNGrams(String queryText, BooleanQuery bq) {
		String[] words = queryText.split("\\s+");

		Term termHandler = new Term("text", "");

		// add bigrams
		for (int i = 0; i < words.length - 1; i++) {

			PhraseQuery pq = new PhraseQuery();
			pq.add(termHandler.createTerm(words[i]));
			pq.add(termHandler.createTerm(words[i + 1]));

			bq.add(pq, BooleanClause.Occur.SHOULD);

		}

		// add trigrams
		for (int i = 0; i < words.length - 2; i++) {

			PhraseQuery pq = new PhraseQuery();
			pq.add(termHandler.createTerm(words[i]));
			pq.add(termHandler.createTerm(words[i + 1]));
			pq.add(termHandler.createTerm(words[i + 2]));

			bq.add(pq, BooleanClause.Occur.SHOULD);

		}

		return bq;
	}

	/**
	 * @param hitDoc
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean isResultValid(Document hitDoc) {
		if (hitDoc.get("text").split(" ").length > 5)
			return true;

		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {
		searcher.close();

	}

	public IndexReader getIndexReader() {

		return searcher.getIndexReader();
	}
}
