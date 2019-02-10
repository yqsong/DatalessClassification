/**
 * 
 */
package edu.illinois.cs.cogcomp.descartes;

import edu.illinois.cs.cogcomp.descartes.retrieval.ISearcher;
import edu.illinois.cs.cogcomp.descartes.retrieval.SearcherFactory;
import edu.illinois.cs.cogcomp.descartes.similarity.DocumentJaccardSimilarity;

/**
 * @author Vivek Srikumar
 * 
 *         Jul 15, 2009
 */
public class DescartesTest {

    public static void main(String[] args) throws Exception {
	String indexDirectory = "";
	String str1 = "", str2 = "";
	if (args.length == 3) {
	    str1 = args[0];
	    str2 = args[1];

	    indexDirectory = args[2];
	} else {
	    System.out
		    .println("Usage: DescartesTest string1 string2 index-directory");
	    System.exit(-1);
	}

	ISearcher searcher = SearcherFactory
		.getStandardSearcher(indexDirectory);

	DocumentJaccardSimilarity similarityComputer = new DocumentJaccardSimilarity(
		searcher, 1000);

	System.out.println("Similarity: "
		+ similarityComputer.getSimilarity(str1, str2));

    }

}
