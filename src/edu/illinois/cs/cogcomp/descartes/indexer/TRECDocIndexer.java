package edu.illinois.cs.cogcomp.descartes.indexer;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Date;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import edu.illinois.cs.cogcomp.descartes.util.IOManager;

/**
 * 
 * @author Quang Do, Vivek Srikumar
 * 
 */
public class TRECDocIndexer extends AbstractDocIndexer {
    // =======
    // Constants
    private static final String DOC_TAG = "DOC";
    private static final int COUNT_NUM = 10000;

    public TRECDocIndexer(String fname, String indexDir, String configFile)
	    throws Exception {
	super(fname, indexDir, configFile, "standard");
    }

    public Stats index() throws Exception {
	BufferedReader reader = IOManager.openReader(fname);

	String line;
	boolean startDoc = false;
	ArrayList<String> arrLines = new ArrayList<String>();
	int i = 0;
	int k = 0;
	while ((line = reader.readLine()) != null) {

	    line = line.trim();
	    if (line.length() == 0)
		continue;
	    if (line.equals("<" + DOC_TAG + ">")) {
		startDoc = true;

	    } else if (line.equals("</" + DOC_TAG + ">")) {
		startDoc = false;
		DocInfo docInfo = new DocInfo();
		boolean isValid = docInfo.parse(arrLines);
		if (isValid) {
		    Document doc = makeDocument(docInfo);
		    indexer.addDocument(doc);
		    i++;
		}
		arrLines = null;
		arrLines = new ArrayList<String>();
		k++;
		if ((k % COUNT_NUM) == 0) {
		    System.out
			    .println("Indexed " + i + " articles out of " + k);
		}
	    }
	    if (startDoc)
		arrLines.add(line);

	}
	IOManager.closeReader(reader);
	System.out
		.println("Finished indexing. " + i + " articles was indexed.");
	System.out.println("Optimizing.");
	indexer.optimize();
	System.out.println("Finished optimizing");
	indexer.close();
	System.out.println("Done.");
	return null;
    }

    // =======
    public Document makeDocument(DocInfo docInfo) {
	Document doc = new Document();
	// Id
	Fieldable idField = new Field("id", docInfo.getId(), Field.Store.YES,
		Field.Index.NO);
	doc.add(idField);
	// Title
	Fieldable titleField = new Field("title", docInfo.getTitle(),
		Field.Store.YES, Field.Index.ANALYZED);
	doc.add(titleField);
	// Text
	Fieldable textField = new Field("text", docInfo.getText(),
		Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES);
	doc.add(textField);
	// Category
	Fieldable categoryField = new Field("category", docInfo.getCategory(),
		Field.Store.YES, Field.Index.NO);
	doc.add(categoryField);

	return doc;
    }

    // ========
    public static void main(String[] args) throws Exception {
	if (args.length != 2) {
	    System.out
		    .println("Usage: [program] <config-file> <input TREC file> <output index directory> ");
	    System.exit(1);
	}
	TRECDocIndexer docIndexer = new TRECDocIndexer(args[1], args[2],
		args[0]);
	Date start = new Date();
	docIndexer.index();
	Date end = new Date();
	System.out.println("Time: " + (end.getTime() - start.getTime())
		/ (float) 1000 + " secs.");
    }
}
