package edu.illinois.cs.cogcomp.descartes.util;

import java.util.ArrayList;

public class PageParser {

    private static final String[] INVALID_BEGIN_TITLE = { "wikipedia:",
	    "category:", "template:", "image:", "portal:", "list of", "talk:" };

    private static final String TITLE_TAG = "title";
    private static final String REVISION_TAG = "revision";
    private static final String ID_TAG = "id";
    private static final String TEXT_TAG = "text";

    private String title;
    private String id;
    private String text;

    public PageParser() {
	title = "";
	id = "";
	text = "";
    }

    public boolean parse(ArrayList<String> pageContent, int lowerBoundLength,
	    int numOfLink) {
	int n = pageContent.size();
	int i = 0;
	boolean startRevision = false;
	boolean startText = false;
	boolean startId = false;
	String textString = "";
	while (i < n) {
	    String line = pageContent.get(i);

	    if (line.startsWith("<" + TITLE_TAG + ">")) {
		int posB = line.indexOf('>');
		int posE = line.indexOf("</" + TITLE_TAG + ">");
		if (posB == -1 || posE == -1) {
		    System.out.println("ERROR: Wrong title format." + "\n"
			    + pageContent);
		    System.exit(1);
		}
		title = line.substring(posB + 1, posE);
	    } else if (line.equals("<" + REVISION_TAG + ">")) {
		startRevision = true;
		startId = true;
	    } else if (line.startsWith("<" + ID_TAG + ">") && startRevision
		    && startId) {
		int posB = line.indexOf('>');
		int posE = line.indexOf("</" + ID_TAG + ">");
		if (posB == -1 || posE == -1) {
		    System.out.println("ERROR: Wrong id format." + "\n"
			    + pageContent);
		    System.exit(1);
		}
		id = line.substring(posB + 1, posE);
		startId = false;
	    } else if (line.startsWith("<" + TEXT_TAG) && startRevision) {
		textString += line + " ";
		startText = true;
		if (line.endsWith("/>")) {
		    text = "";
		    break;
		}
	    } else if (line.equals("</" + REVISION_TAG + ">")) {
		startRevision = false;
		if (startText) {
		    System.out.println("ERROR: Wrong text format." + "\n"
			    + pageContent);
		    System.exit(1);
		}
	    } else if (startText) {
		textString += line + " ";
	    }
	    if (line.endsWith("</" + TEXT_TAG + ">")) {
		startText = false;
		int posB = textString.indexOf('>');
		int posE = textString.indexOf("</" + TEXT_TAG + ">");
		text = textString.substring(posB + 1, posE).toLowerCase();
	    }
	    i++;
	}
	boolean isValid = formatText(lowerBoundLength, numOfLink);
	return isValid;
    }

    private boolean formatText(int lowerBoundLength, int numOfLink) {

	StringBuffer bufString = new StringBuffer(text);

	bufString = cleanUpDoubleBrackets(bufString);

	int posB = bufString.indexOf("[[");
	int count = 0;
	while (posB != -1) {
	    int posCat = bufString.indexOf("Category:", posB + 1);
	    if (posCat == posB + 2)
		break;
	    count++;
	    posB = bufString.indexOf("[[", posB + 1);
	}
	if (count < numOfLink)
	    return false;

	text = bufString.toString();

	text = formatText(text);

	title = formatText(title);

	if (title.trim().length() == 0)
	    return false;

	for (String s : INVALID_BEGIN_TITLE)
	    if (title.toLowerCase().startsWith(s))
		return false;

	if (text.length() == 0)
	    return false;

	bufString = new StringBuffer(text);

	// Clean up the link references in the article
	bufString = cleanUpLinkReference(bufString);

	// Clean up the comments in the article
	bufString = cleanUpComments(bufString);

	text = bufString.toString();

	text = text.replaceAll("\\s+", " ");

	// --------------
	// Test length
	String[] tokens = text.split("\\s+");
	if (tokens.length < lowerBoundLength)
	    return false;
	return true;
    }

    private StringBuffer cleanUpComments(StringBuffer bufString) {
	int posB;
	posB = bufString.indexOf("<!--");
	while (posB != -1) {
	    int posE = bufString.indexOf("-->");
	    while (posE != -1 && posE < posB) {
		posE = bufString.indexOf("-->", posE + 1);
	    }
	    if (posE == -1) {
		// System.out.println(
		// "ERROR: Wrong text format with <!-- and -->.");
		// System.out.println(bufString);
		break;
	    }
	    bufString = bufString.delete(posB, posE + 3);
	    posB = bufString.indexOf("<!--");
	}
	return bufString;
    }

    private StringBuffer cleanUpLinkReference(StringBuffer bufString) {
	int posB;
	posB = bufString.indexOf("<ref");
	int len = 0;
	while (posB != -1) {
	    int posE = bufString.indexOf("</ref>");
	    while (posE != -1 && posE < posB) {
		bufString.delete(posE, posE + 6);
		posE = bufString.indexOf("</ref>");
	    }
	    int posT = bufString.indexOf("/>", posB + 1);
	    if (posT != -1 && posT < posE && posT > posB) {
		posE = posT;
		len = 2;
	    } else {
		len = 6;
	    }
	    if (posE == -1 && posT != -1) {
		posE = posT;
		len = 2;
	    }
	    if (posE == -1) {
		// System.out.println("ERROR: Wrong text format with </ref>");
		// System.out.println(bufString);
		break;
	    }
	    try {
		bufString.delete(posB, posE + len);
	    } catch (Exception e) {
		e.printStackTrace();
		System.out.println(bufString.toString());
		System.exit(1);
	    }
	    posB = bufString.indexOf("<ref");
	}

	return bufString;
    }

    private StringBuffer cleanUpDoubleBrackets(StringBuffer bufString) {
	int posB = bufString.indexOf("{{");
	while (posB != -1) {
	    int posE = bufString.indexOf("}}", posB + 1);
	    int posT = bufString.indexOf("{{", posB + 1);
	    while (posT != -1 && posT < posE) {
		posE = bufString.indexOf("}}", posE + 1);
		posT = bufString.indexOf("{{", posT + 1);
	    }
	    if (posE == -1) {
		// System.out.println("ERROR: Wrong text format with \"}}\"." +
		// "\n" + text);
		break;
	    }
	    bufString = bufString.delete(posB, posE + 2);
	    posB = bufString.indexOf("{{");
	}
	return bufString;
    }

    // ========
    private String formatText(String input) {

	String output = input;

	output = output.replaceAll("\\{\\|.*?\\|\\}", " ");

	output = output.replaceAll("&lt;", "<");
	output = output.replaceAll("&gt;", ">");
	output = output.replaceAll("&quot;", "\"");
	output = output.replaceAll("&amp;", "&");
	output = output.replaceAll("&ndash;", "-");
	output = output.replaceAll("&mdash;", "-");
	output = output.replaceAll("&nbsp;", " ");
	output = output.replaceAll("&reg;", " ");
	output = output.replaceAll("<blockquote>", " ");
	output = output.replaceAll("</blockquote>", " ");
	output = output.replaceAll("<nowiki></nowiki>", "");
	output = output.replaceAll("<br />", "");
	output = output.replaceAll("<br/>", "");

	output = output.replaceAll("<math>.*?</math>", " ");

//	output = output.replaceAll("[!\"#$%&'*+,-./;<=>?@\\^_`{}~]", " ");

	output = output.replaceAll("\\s+", " ");
	output = output.trim();

	return output;
    }

    // ========
    public String getTitle() {
	return title;
    }

    // ========
    public String getId() {
	return id;
    }

    // ========
    public String getText() {
	return text;
    }
}
