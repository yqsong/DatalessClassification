/**
 * 
 */
package edu.illinois.cs.cogcomp.descartes.similarity;

/**
 * @author Vivek Srikumar
 * 
 */
public interface ISimilarity {
    double getSimilarity(String str1, String str2) throws Exception;
}
