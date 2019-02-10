/**
 * 
 */
package edu.illinois.cs.cogcomp.descartes.indexer;

import info.bliki.wiki.dump.WikiArticle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.compress.bzip2.CBZip2InputStream;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.descartes.util.IOManager;
import edu.illinois.cs.cogcomp.descartes.util.PageParser;
import edu.illinois.cs.cogcomp.wiki.parsing.WikiDumpFilter;
import edu.illinois.cs.cogcomp.wiki.parsing.processors.InfoBox;
import edu.illinois.cs.cogcomp.wiki.parsing.processors.PageMeta;

/**
 * @author Vivek Srikumar
 * 
 */
public class WikiDocIndexer extends AbstractDocIndexer {

	private static final String PAGE_TAG = "page";
	private static final int BUFFER_SIZE = 100;
	private static final int DOC_COUNT = 1000;

	private int lowerBoundLength;
	private int numLinks;

	private ArrayList<PageParser> arrPages;

	/**
	 * @param fname
	 * @param indexDir
	 * @param configFile
	 * @throws Exception
	 */
	public WikiDocIndexer(String fname, String indexDir, String configFile, String analyzerType)
			throws Exception {
		super(fname, indexDir, configFile, analyzerType);

		arrPages = new ArrayList<PageParser>();

		// Read the configuration file
		PropertiesConfiguration config = new PropertiesConfiguration(configFile);
		String stringLowerBoundLength = config.getString("cogcomp.parser.lowerboundlength");

		lowerBoundLength = Integer.parseInt(stringLowerBoundLength);
		numLinks = config.getInt("cogcomp.parser.numoflink");

		System.out.println("Configuration:");
		System.out.println("\tlowerBoundLength = " + lowerBoundLength);
		System.out.println("\tnumOfLink = " + numLinks);
	}

	@Override
	public Stats index() throws Exception {
		File file = new File(fname);

		FileInputStream fileStream = new FileInputStream(file);

		// Ugly hack because the constructor of CBZip2InputStream does not
		// like seeing the two "magic" characters at the start of the
		// stream. This is documented here:
		// http://api.dpml.net/ant/1.7.0/org/apache/tools/bzip2/CBZip2InputStream.html#CBZip2InputStream(java.io.InputStream)
		fileStream.read();
		fileStream.read();

		CBZip2InputStream bz2Stream = new CBZip2InputStream(fileStream);

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				bz2Stream));

		String line;
		boolean startPage = false;
		ArrayList<String> pageContent = new ArrayList<String>();
		int count = 0;
		int validCount = 0;
		Date start = new Date();
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0)
				continue;

			if (("<" + PAGE_TAG + ">").equals(line)) {
				startPage = true;
			}

			if (startPage)
				pageContent.add(line);

			if (("</" + PAGE_TAG + ">").equals(line)) {
				startPage = false;
				PageParser pageParser = new PageParser();
				boolean isValid = pageParser.parse(pageContent,
						lowerBoundLength, numLinks);

				if (isValid) {
					addToBuffer(pageParser);
					validCount++;
				}

				pageContent = new ArrayList<String>();
				count++;
				if (count % DOC_COUNT == 0) {
					
					Date end = new Date();
					System.out.println("Parsed " + validCount + " out of "
							+ count + " documents with " + 
							(end.getTime() - start.getTime()) / (float) 1000 + " seconds..");
					
				}
			}
		}
		IOManager.closeReader(reader);
		bz2Stream.close();
		fileStream.close();

		if (arrPages.size() > 0) {
			outputPages();
		}

		System.out.println("Optimizing.");
		indexer.optimize();
		System.out.println("Finished optimizing");
		indexer.close();
		System.out.println("Done.");

		System.out.println("Total: " + validCount + " out of " + count
				+ " documents were parsed.");
		
		Stats stats = new Stats();
		stats.numPages = count;
		stats.numIndexed = validCount;
		return stats;
	}

	private void addToBuffer(PageParser pageParser) {
		arrPages.add(pageParser);
		if (arrPages.size() == BUFFER_SIZE) {
			outputPages();
			arrPages = null;
			arrPages = new ArrayList<PageParser>();
		}
	}

	private void outputPages() {
		try {
			int n = arrPages.size();
			for (int i = 0; i < n; i++) {
				PageParser page = arrPages.get(i);
				if (page == null)
					continue;

				Document doc = new Document();
				// Id
				Fieldable idField = new Field("id", page.getId(),
						Field.Store.YES, Field.Index.NO);
				doc.add(idField);
				// Title
				Fieldable titleField = new Field("title", page.getTitle(),
						Field.Store.YES, Field.Index.ANALYZED);
				doc.add(titleField);
				// Text
				Fieldable textField = new Field("text", page.getText(),
						Field.Store.YES, Field.Index.ANALYZED,
						Field.TermVector.YES);
				doc.add(textField);

				indexer.addDocument(doc);

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public Stats indexWikiAPI() throws Exception {
		final AtomicInteger count = new AtomicInteger(0);
		final AtomicInteger validCount = new AtomicInteger(0);
		String bz2Filename = fname;
		try {
			System.out.println("Started dump parsing");
			// indexer.addDocument(doc);
			WikiDumpFilter filter = new WikiDumpFilter() {
				@Override
				public void processAnnotation(WikiArticle page, PageMeta meta,
						TextAnnotation ta) {
					// Do anything you want to both annotations
					count.incrementAndGet();
					// System.out.println(page.getTitle() + " " + page.getId());
					if (page.getText().length() > lowerBoundLength
							&& meta.getLinks().size() > numLinks) {

						validCount.incrementAndGet();
						try {
							Document doc = new Document();
							// Id
							Fieldable idField = new Field("id", page.getId(),
									Field.Store.YES, Field.Index.NO);
							doc.add(idField);
							// Title
							Fieldable titleField = new Field("title",
									page.getTitle(), Field.Store.YES,
									Field.Index.ANALYZED);
							doc.add(titleField);
							// Text
							Fieldable textField = new Field("text",
									page.getText(), Field.Store.YES,
									Field.Index.ANALYZED, Field.TermVector.YES);
							doc.add(textField);
							indexer.addDocument(doc);
						} catch (IOException e) {
							System.out
									.println("Something went wrong in parsing");
							e.printStackTrace();
						}
					}
				}
				// Suppress progress output if you prefer
			};
			filter.silence();
	        // Start parsing
	        WikiDumpFilter.parseDump(bz2Filename, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Stats stats = new Stats();
		stats.numPages = count.get();
		stats.numIndexed = validCount.get();
		return stats;
	}


}
