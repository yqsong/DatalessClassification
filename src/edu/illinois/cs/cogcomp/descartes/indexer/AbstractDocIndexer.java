/**
 * 
 */
package edu.illinois.cs.cogcomp.descartes.indexer;

import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.illinois.cs.cogcomp.descartes.AnalyzerFactory;
import edu.illinois.cs.cogcomp.descartes.similarity.UnNormalizedLuceneSimilarity;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;

/**
 * @author Vivek Srikumar
 * 
 */
public abstract class AbstractDocIndexer {
	public class Stats {
		public int numPages;
		public int numIndexed;
	}

    protected final String fname;
    protected final String indexDir;
    protected final IndexWriter indexer;

    public AbstractDocIndexer(String fname, String indexDir, String configFile, String analyzerType)
	    throws Exception {
	this.fname = fname;
	this.indexDir = indexDir;
	if (IOManager.isDirectoryExist(this.indexDir)) {
	    System.out.println("The directory " + this.indexDir
		    + " already exists in the system. "
		    + "It will be deleted now.");
	    IOManager.deleteDirectory(this.indexDir);
	}
	Analyzer analyzer = AnalyzerFactory.initialize(analyzerType);

	Directory dir = FSDirectory.open(new File(this.indexDir));

	indexer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);

	indexer.setSimilarity(new UnNormalizedLuceneSimilarity());

    }

    public abstract Stats index() throws Exception;
}
