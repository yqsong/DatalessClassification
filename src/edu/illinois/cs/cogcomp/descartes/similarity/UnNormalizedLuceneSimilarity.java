/**
 * 
 */
package edu.illinois.cs.cogcomp.descartes.similarity;

import org.apache.lucene.search.DefaultSimilarity;

/**
 * @author Vivek Srikumar
 * 
 *         Nov 25, 2008
 * 
 */
public class UnNormalizedLuceneSimilarity extends DefaultSimilarity {

    private static final long serialVersionUID = -6275200561103624935L;

    @Override
    public float lengthNorm(String fieldName, int numTerms) {
	if (numTerms > 0)
	    return 1;
	else
	    return 0;
    }
}
