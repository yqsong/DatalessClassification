/**
 * 
 */
package edu.illinois.cs.cogcomp.descartes.retrieval;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Vivek Srikumar
 * 
 */
public interface ISearcher extends Closeable {
    public ArrayList<IResult> search(String query, int numResults)
	    throws Exception;

    public void open(String index) throws IOException;

}
