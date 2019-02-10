package edu.illinois.cs.cogcomp.descartes.util;

/*
 * Author: Quang Do
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.compress.bzip2.CBZip2InputStream;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class WikiPreprocessor {

    private static final String PAGE_TAG = "page";
    private static final int BUFFER_SIZE = 100;
    private static final int DOC_COUNT = 10000;

    private String fileName;
    private ArrayList<PageParser> arrPages;
    private BufferedWriter writer;
    private String outputFileName;

    private int lowerBoundLength;
    private int numOfLink;

    public WikiPreprocessor(String fileName, String configFile)
	    throws ConfigurationException {
		this.fileName = fileName;
		arrPages = new ArrayList<PageParser>();
		writer = null;
	
		// Read the configuration file
		PropertiesConfiguration config = new PropertiesConfiguration(configFile);
		String stringLowerBoundLength = config
			.getString("descartes.parser.lowerboundlength");
	
		lowerBoundLength = Integer.parseInt(stringLowerBoundLength);
		numOfLink = config.getInt("descartes.parser.numoflinks");
	
		System.out.println("Configuration:");
		System.out.println("\tlowerBoundLength = " + lowerBoundLength);
		System.out.println("\tnumOfLink = " + numOfLink);
    }

    public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
		writer = IOManager.openWriter(this.outputFileName);
    }

    public void parse() {
	try {

	    File file = new File(fileName);

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
				    lowerBoundLength, numOfLink);
	
			    if (isValid) {
				addToBuffer(pageParser);
				validCount++;
			    }
	
			    pageContent = new ArrayList<String>();
			    count++;
			    if (count % DOC_COUNT == 0) {
				System.out.println("Parsed " + validCount + " out of "
					+ count + " documents.");
			    }
			}
	    }
	    IOManager.closeReader(reader);
	    bz2Stream.close();
	    fileStream.close();

	    if (arrPages.size() > 0) {
	    	outputPages();
	    }
	    
	    if (writer != null)
		IOManager.closeWriter(writer);
	    System.out.println("Total: " + validCount + " out of " + count
		    + " documents were parsed.");
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
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
		StringBuffer outString = new StringBuffer("");
		outString.append("<DOC>\n");
		outString.append("<ID>" + page.getId() + "</ID>\n");
		outString.append("<TITLE>" + page.getTitle() + "</TITLE>\n");
		outString.append("<TEXT>" + page.getText() + "</TEXT>\n");
		outString.append("</DOC>\n");
		if (writer != null) {
		    writer.write(outString.toString());
		} else {
		    System.out.println(outString.toString());
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}
