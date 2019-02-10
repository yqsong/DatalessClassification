/**
 * 
 */
package edu.illinois.cs.cogcomp.descartes.similarity;

import java.util.ArrayList;

import edu.illinois.cs.cogcomp.descartes.indexer.TRECDocIndexer;
import edu.illinois.cs.cogcomp.descartes.retrieval.IResult;
import edu.illinois.cs.cogcomp.descartes.retrieval.ISearcher;
import edu.illinois.cs.cogcomp.descartes.retrieval.SearcherFactory;

/**
 * Computes the jaccard similarity between two documents based on the overlap of
 * "concepts". To use this, one needs to supply an {@link ISearcher}, which
 * could be created using {@link SearcherFactory}.
 * <p>
 * Here is some example code to compute the similarity between two strings.
 * <p>
 * 
 * <pre>
 * String str1 = &quot;Barack Obama&quot;;
 * String str2 = &quot;president of the United States&quot;;
 * 
 * // first create the searcher that points to an index  
 * ISearcher searcher = SearcherFactory.getStandardSearcher(indexDirectory);
 * 
 * // Now, create an instance of the similarity computer that 
 * // uses the searcher. Here, we specify that we want to use 
 * // 1000 concepts. 1000 is a good number if we use Wikipedia
 * // (as ESA did.) For other indexes, we might want to try out
 * // other numbers.
 * 
 * DocumentJaccardSimilarity similarityComputer = new DocumentJaccardSimilarity(
 * 	searcher, 1000);
 * 
 * // We can compute the &quot;semantic similarity&quot; between
 * // the two strings.
 * double similarity = similarityComputer.getSimilarity(str1, str2);
 * 
 * // Now that we have this, we can do something with it.
 * </pre>
 * 
 * Note that in order to use this, we need to have access to an index that was
 * created using the {@link TRECDocIndexer}.
 * 
 * @see ISearcher
 * @see SearcherFactory
 * @see TRECDocIndexer
 * 
 * @author Vivek Srikumar
 */
public class DocumentJaccardSimilarity implements ISimilarity {

    protected final ISearcher searcher;
    protected int numResults;

    public DocumentJaccardSimilarity(ISearcher searcher, int numResults) {
	this.searcher = searcher;
	this.numResults = numResults;
    }

    public double getSimilarity(String str1, String str2) throws Exception {
	ArrayList<IResult> concepts1 = searcher.search(str1, numResults);
	ArrayList<IResult> concepts2 = searcher.search(str2, numResults);

	int sum = concepts1.size() + concepts2.size();

	concepts1.retainAll(concepts2);
	int intersection = concepts1.size();

	double union = sum - intersection;
	return intersection / union;
    }

}
