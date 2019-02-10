package edu.illinois.cs.cogcomp.descartes.indexer;

import java.util.ArrayList;

/**
 * @author Quang Do
 */
public class DocInfo {
    // ======
    // Constants
    private static final String ID_TAG = "<ID>";
    private static final String TITLE_TAG = "<TITLE>";
    private static final String TEXT_TAG = "<TEXT>";
    private static final String DESC_TAG = "<CATEGORY>";

    private static final int ID_LEN = ID_TAG.length();
    private static final int TITLE_LEN = TITLE_TAG.length();
    private static final int TEXT_LEN = TEXT_TAG.length();
    private static final int DESC_LEN = DESC_TAG.length();

    private static final String[] INVALID_BEGIN_TITLE = { "wikipedia:",
	    "category:", "template:", "image:", "portal:", "list of", "talk:" };
    private static final String[] INVALID_BEGIN_TEXT = { "#REDIRECT",
	    "#redirect", "#Redirect" };

    // ======
    // Variables
    private String id;

    private String title;
    private String text;
    private String description;

    /**
     * @param id
     * @param title
     * @param text
     * @param description
     */
    public DocInfo(String id, String title, String text, String description) {
	super();
	this.id = id;
	this.title = title;
	this.text = text;
	this.description = description;
    }

    public DocInfo() {
	id = "";
	title = "";
	text = "";
	description = "";
    }

    public boolean parse(ArrayList<String> arrLines) {

	int n = arrLines.size();
	for (int i = 0; i < n; i++) {
	    String line = arrLines.get(i);
	    try {

		line = line.trim();
		if (line.startsWith(ID_TAG)) {
		    id = line.substring(ID_LEN, line.length() - (ID_LEN + 1));
		} else if (line.startsWith(TITLE_TAG)) {
		    title = line.substring(TITLE_LEN, line.length()
			    - (TITLE_LEN + 1));
		} else if (line.startsWith(TEXT_TAG)
			&& line.endsWith("</TEXT>")) {
		    text = line.substring(TEXT_LEN, line.length()
			    - (TEXT_LEN + 1));
		} else if (line.startsWith(DESC_TAG)) {
		    description = line.substring(DESC_LEN, line.length()
			    - (DESC_LEN + 1));
		}
	    } catch (Exception ex) {
		System.out.println(id + "\t" + line);
		System.out.println(ex);
		System.exit(-1);
	    }
	}

	text = text.toLowerCase();
	title = title.toLowerCase();
	boolean isValid = isDocumentValid();

	if (isValid)
	    text = title + " " + text;

	return isValid;
    }

    private boolean isDocumentValid() {
	// Check title

	if (title.length() == 0)
	    return false;

	int n = INVALID_BEGIN_TITLE.length;
	for (int i = 0; i < n; i++) {
	    if (title.startsWith(INVALID_BEGIN_TITLE[i]))
		return false;
	}
	// Check text

	if (text.length() == 0)
	    return false;

	// n = INVALID_BEGIN_TEXT.length;
	// for (int i = 0; i < n; i++)
	// {
	// if (text.startsWith(INVALID_BEGIN_TEXT[i]))
	// return false;
	// }
	// Everything is OK!
	return true;
    }

    public String getId() {
	return id;
    }

    public String getTitle() {
	return title;
    }

    public String getText() {
	return text;
    }

    public String getDescription() {
	return description;
    }

    public String getCategory() {
	return description;
    }
}
