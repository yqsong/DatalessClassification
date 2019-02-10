package edu.illinois.cs.cogcomp.classification.main;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.StopWords;

/**
 * Shaoshi Ling
 * sling3@illinois.edu
 */

public class DatalessResourcesConfig {
	
	public final static String CONST_DATA_RCV = "rcv";
	public final static String CONST_DATA_NYTIMES = "nytimes";
	public final static String CONST_DATA_CUSTOMIZED = "Customized";
	public final static String CONST_DATA_YAHOO = "YahooDir";
	public final static String CONST_DATA_20NG = "20newsgroups";
	public final static String CONST_DATA_SIMPLEWIKI = "WikiCate";
	public final static String CONST_DATA_CUSTOMIZEDHC = "CustomizedHC";
	public final static String CONST_PATH_WIKI = "data/testLabels.txt";
	public final static String CONST_PATH_HC_WIKI = "data/HCtestLabels.txt";
	
	public static Set<String> stopwordSet = new HashSet<String>();
	
	public static String simpleESADocumentIndex;
	public static String complexESAWordIndex;
	public static String memorybasedESA; 
	public static String complexWikiOriginalDocumentIndex;
	public static String pageIDMapping;
	public static int embeddingDimension;
	
	public static String memorybasedJohnVec; 
	public static String memorybasedW2V;
	public static String wikiTitleW2V;
	public static String wikiTitleJohnVec;
	
	public static String word2vecIndex;


	public static int level;
	public static double[] cutoff;

	public static String dataset = CONST_DATA_CUSTOMIZED;
	public static String LabelfilePath=CONST_PATH_WIKI;
	public static String HCLabelfilePath=CONST_PATH_HC_WIKI;
	public static String DatafilePath="data/contents.txt";
	public static String LabelResult="data/labelresults.txt";
	
	public DatalessResourcesConfig(){
		initialization();
	}
	
	public static void initialization () {
		// Read the configuration file
		String configFile = "conf/configurations.properties";
		PropertiesConfiguration config = null;
		try {
			config = new PropertiesConfiguration(configFile);
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
		}
		String stopwordStr = config.getString("cogcomp.indexer.stopwords");
		
		StopWords.rcvStopWords = StopWords.readStopWords (config.getString("cogcomp.rcv.doc.stopwords"));
		
		stopwordSet = new HashSet<String>();
		String[] stopWordArray = stopwordStr.split(",");
		for (String word : stopWordArray) {
			stopwordSet.add(word);
		}
		for (String word : StopWords.rcvStopWords) {
			if (stopwordSet.contains(word) == false) {
				stopwordSet.add(word);
			}
		}
		
		level = Integer.parseInt(config.getString("cousom.tree.depth", "2"));
		cutoff = new double[level];
		if (level > 0) {
			cutoff[0]=0.05;
		}
		if (level > 1) {
			cutoff[1]=0.1;
		}
		
		simpleESADocumentIndex = config.getString("cogcomp.esa.simple.wikiIndex", "data/wikiIndexLucene3.0.2_vivek/");
		complexWikiOriginalDocumentIndex = config.getString("cogcomp.esa.complex.wikiIndex", "data/WikiLuceneIndex_word500_link30/");
		complexESAWordIndex = config.getString("cogcomp.esa.complex.wordIndex", "data/WikiLuceneIndex_word500_link30_wordindex_1000concepts_prune_new_modifyTFandInlink/");
		pageIDMapping = config.getString("cogcomp.esa.complex.pageIDMapping", "data/wikipedia/wiki_structured/wikiPageIDMapping.txt");
		memorybasedESA = config.getString("cogcomp.esa.memory.wordIndex", "data/MemoryBasedESA.txt");
		embeddingDimension = Integer.parseInt(config.getString("cogcomp.esa.count", "500"));
		
		memorybasedJohnVec = config.getString("cogcomp.word2vec.john", "data/paragram_vectors.txt");
		memorybasedW2V = config.getString("cogcomp.word2vec.memory", "data/vectors-enwikitext_vivek200.txt");

		wikiTitleW2V = config.getString("cogcomp.word2vec.wikiTitle", "data/test/w2v.txt");
		wikiTitleJohnVec = config.getString("cogcomp.word2vec.john.wikiTitle", "data/test/john.txt");

		word2vecIndex = config.getString("cogcomp.word2vec.disk", "data/enwiki_vivek_200/");
		
		
		System.out.println("Configuration Done.");
	}
	
	public static Set<String> getStopwordSet() {
		return stopwordSet;
	}

	public static void setStopwordSet(Set<String> stopwordSet) {
		DatalessResourcesConfig.stopwordSet = stopwordSet;
	}

	public static String getSimpleESADocumentIndex() {
		return simpleESADocumentIndex;
	}

	public static void setSimpleESADocumentIndex(String simpleESADocumentIndex) {
		DatalessResourcesConfig.simpleESADocumentIndex = simpleESADocumentIndex;
	}

	public static String getComplexESAWordIndex() {
		return complexESAWordIndex;
	}

	public static void setComplexESAWordIndex(String complexESAWordIndex) {
		DatalessResourcesConfig.complexESAWordIndex = complexESAWordIndex;
	}

	public static String getMemorybasedESA() {
		return memorybasedESA;
	}

	public static void setMemorybasedESA(String memorybasedESA) {
		DatalessResourcesConfig.memorybasedESA = memorybasedESA;
	}

	public static String getComplexWikiOriginalDocumentIndex() {
		return complexWikiOriginalDocumentIndex;
	}

	public static void setComplexWikiOriginalDocumentIndex(
			String complexWikiOriginalDocumentIndex) {
		DatalessResourcesConfig.complexWikiOriginalDocumentIndex = complexWikiOriginalDocumentIndex;
	}

	public static String getPageIDMapping() {
		return pageIDMapping;
	}

	public static void setPageIDMapping(String pageIDMapping) {
		DatalessResourcesConfig.pageIDMapping = pageIDMapping;
	}

	public static int getCount() {
		return embeddingDimension;
	}

	public static void setCount(int count) {
		DatalessResourcesConfig.embeddingDimension = count;
	}

	public static String getMemorybasedJohnVec() {
		return memorybasedJohnVec;
	}

	public static void setMemorybasedJohnVec(String memorybasedJohnVec) {
		DatalessResourcesConfig.memorybasedJohnVec = memorybasedJohnVec;
	}

	public static String getMemorybasedW2V() {
		return memorybasedW2V;
	}

	public static void setMemorybasedW2V(String memorybasedW2V) {
		DatalessResourcesConfig.memorybasedW2V = memorybasedW2V;
	}

	public static String getWikiTitleW2V() {
		return wikiTitleW2V;
	}

	public static void setWikiTitleW2V(String wikiTitleW2V) {
		DatalessResourcesConfig.wikiTitleW2V = wikiTitleW2V;
	}

	public static String getWikiTitleJohnVec() {
		return wikiTitleJohnVec;
	}

	public static void setWikiTitleJohnVec(String wikiTitleJohnVec) {
		DatalessResourcesConfig.wikiTitleJohnVec = wikiTitleJohnVec;
	}

	public static String getWord2vecIndex() {
		return word2vecIndex;
	}

	public static void setWord2vecIndex(String word2vecIndex) {
		DatalessResourcesConfig.word2vecIndex = word2vecIndex;
	}

	public String getLabelfilePath() {
		return LabelfilePath;
	}

	public void setLabelfilePath(String labelfilePath) {
		LabelfilePath = labelfilePath;
	}

	public String getDatafilePath() {
		return DatafilePath;
	}

	public void setDatafilePath(String datafilePath) {
		DatafilePath = datafilePath;
	}

	public String getLabelResult() {
		return LabelResult;
	}

	public void setLabelResult(String labelResult) {
		LabelResult = labelResult;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public double[] getCutoff() {
		return cutoff;
	}

	public void setCutoff(double[] cutoff) {
		this.cutoff = cutoff;
	}

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	public static String getConstDataRcv() {
		return CONST_DATA_RCV;
	}

	public static String getConstDataNytimes() {
		return CONST_DATA_NYTIMES;
	}

	public static String getConstDataCustomized() {
		return CONST_DATA_CUSTOMIZED;
	}

	public static String getConstDataYahoo() {
		return CONST_DATA_YAHOO;
	}

	public static String getConstData20ng() {
		return CONST_DATA_20NG;
	}

	public static String getConstDataSimplewiki() {
		return CONST_DATA_SIMPLEWIKI;
	}

	public static String getConstDataCustomizedhc() {
		return CONST_DATA_CUSTOMIZEDHC;
	}

	public static String getConstPathWiki() {
		return CONST_PATH_WIKI;
	}

}
