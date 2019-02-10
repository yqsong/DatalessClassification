/**
 * 
 */
package edu.illinois.cs.cogcomp.descartes.retrieval;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.descartes.AnalyzerFactory;
import edu.illinois.cs.cogcomp.descartes.indexer.TRECDocIndexer;
import edu.illinois.cs.cogcomp.descartes.retrieval.simple.Searcher;
import edu.illinois.cs.cogcomp.descartes.util.Utilities;

/**
 * A utility class for creating searchers.
 * 
 * @author Vivek Srikumar
 * 
 *         Jan 9, 2009
 * 
 */
public class SearcherFactory {

	private static Logger log = LoggerFactory.getLogger(SearcherFactory.class);

	public static boolean isUseWhiteSpaceAnalyzer = false;
	
	public static ISearcher getSearcher(String indexDirectory, String configFile)
			throws IOException, ConfigurationException {
		boolean ngrams;

		String[] fields;

		PropertiesConfiguration config = new PropertiesConfiguration(configFile);

		if (config.getBoolean("descartes.retriever.useTitleAndText")) {
			fields = new String[] { "text", "title" };
		} else {
			fields = new String[] { "text" };
		}

		ngrams = config.getBoolean("cogcomp.retriever.useNGrams");

		Searcher searcher = new Searcher(fields, ngrams,
				Utilities.getStopWords(), AnalyzerFactory.defaultAnalyzerName);

		searcher.open(indexDirectory);

		return searcher;
	}

	public static ISearcher getSearcher(String indexDirectory,
			boolean useBothTextAndTitle, boolean ngrams) throws Exception {
		String[] fields;

		if (useBothTextAndTitle) {
			fields = new String[] { "text", "title" };
		} else {
			fields = new String[] { "text" };
		}

		Searcher searcher = new Searcher(fields, ngrams,
				Utilities.getStopWords(), AnalyzerFactory.defaultAnalyzerName);

		searcher.open(indexDirectory);

		log.info(
				"Created searcher with the following configuration: "
						+ "\n\tIndex directory: {},"
						+ "\n\tUse text and titles: {},"
						+ "\n\tUse ngrams: {}," + "\n\tStopwords: {}",
				new Object[] { indexDirectory,
						Boolean.valueOf(useBothTextAndTitle),
						Boolean.valueOf(ngrams), Utilities.getStopWords() });

		return searcher;

	}

	/**
	 * Get an {@link ISearcher} for <code>indexDirectory</code> with the
	 * standard settings:
	 * <ul>
	 * <li>Does not use both text and title (because it is easier to just index
	 * the text and the title together.)
	 * <li>Uses bigrams and trigrams for search (just like the original ESA
	 * implementation.)
	 * </ul>
	 * See the documentation for {@link ISearcher} for more on how to use the
	 * returned object.
	 * 
	 * @param indexDirectory
	 *            The directory where the index was created, using
	 *            {@link TRECDocIndexer}.
	 * @return An {@link ISearcher} pointing to the index.
	 * 
	 * @see ISearcher
	 * @throws IOException
	 */
	public static ISearcher getStandardSearcher(String indexDirectory)
			throws Exception {
		return getSearcher(indexDirectory, false, true);
	}

}
