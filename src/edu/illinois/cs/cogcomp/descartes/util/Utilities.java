package edu.illinois.cs.cogcomp.descartes.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.util.Version;

public class Utilities {

    private static Set<String> stopWords;
    private static CharArraySet charStopWords;

    public static Set<String> getStopWords() {
	if (stopWords == null) {
	    stopWords = new HashSet<String>();

	    stopWords.addAll(Arrays.asList("I", "a", "about", "an", "are",
		    "as", "at", "be", "by", "com", "de", "en", "for", "from",
		    "how", "in", "is", "it", "la", "of", "on", "or", "that",
		    "the", "this", "to", "was", "what", "when", "where", "who",
		    "will", "with", "und", "the", "www"));
	}
	return stopWords;
    }
    
    // public static Set<String> getStopWords(String configFile)
    // throws ConfigurationException {
    // if (stopWords == null) {
    //
    // stopWords = new HashSet<String>();
    // PropertiesConfiguration config = new PropertiesConfiguration(
    // configFile);
    // String s = config.getString("descartes.indexer.stopwords");
    //
    // stopWords.addAll(Arrays.asList(s.split(",+")));
    //
    // }
    // return stopWords;
    // }

}
