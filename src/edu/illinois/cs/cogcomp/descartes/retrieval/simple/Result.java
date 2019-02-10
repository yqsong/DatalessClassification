/**
 * 
 */
package edu.illinois.cs.cogcomp.descartes.retrieval.simple;

import edu.illinois.cs.cogcomp.descartes.retrieval.IResult;

/**
 * @author Vivek Srikumar
 * 
 */
public class Result implements IResult {

    private String id;
    private double score;
    private String doc;
    private String title;

    public Result(String id, String title, String doc, double score) {
	this.id = id;
	this.title = title;
	this.score = score;
	this.doc = doc;
    }

    public String getId() {
	return id;
    }

    public double getScore() {
	return score;
    }

    public String getTitle() {
	return title;
    }

    public String getDocument() {
	return doc;
    }

    @Override
    public boolean equals(Object that) {
	if (this == that)
	    return true;

	if (!(that instanceof IResult))
	    return false;

	IResult thatResult = (IResult) that;
	return this.id.equals(thatResult.getId());
    }

    @Override
    public int hashCode() {
	return this.id.hashCode();
    }

}
