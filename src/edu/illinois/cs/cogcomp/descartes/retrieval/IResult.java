/**
 * 
 */
package edu.illinois.cs.cogcomp.descartes.retrieval;

/**
 * This specifies the interface for a result of a search that is performed using
 * an {@link ISearcher}. Each instance of this class corresponds to a "concept"
 * in the ESA world. For example, the standard ESA representation corresponds to
 * getting the titles of Wikipedia articles -- this can be obtained using the
 * {@link IResult#getTitle()} function of the list of results returned by
 * {@link ISearcher#search(String, int)}.
 * 
 * @author Vivek Srikumar
 * 
 */
public interface IResult
{
	String getId();

	String getTitle();

	double getScore();

	String getDocument();

}
