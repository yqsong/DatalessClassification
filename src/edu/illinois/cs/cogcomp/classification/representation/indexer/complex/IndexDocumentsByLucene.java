package edu.illinois.cs.cogcomp.classification.representation.indexer.complex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;

/**
 * yqsong@illinois.edu
 */

public class IndexDocumentsByLucene {
	  IndexWriter writer;
	  
	  DatabaseConfiguration dbConfig = new DatabaseConfiguration();
	  Wikipedia wiki;
		
	  Logger logger = Logger.getLogger(this.getClass().getName());
	  
	  int leastLinkNum = 30;
	  int leastWordNum = 500;
	  
	  public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
		  String s = "data/WikiLucenIndex_word500_link30";
		  IndexDocumentsByLucene indexer = new IndexDocumentsByLucene(s);
		  indexer.indexWikipedia();
	  }
	  
	  private IndexDocumentsByLucene(final String outputLuceneDir) {
			// read DB config
			InputStream is = IndexDocumentsByLucene.class.getResourceAsStream("conf/db.conf");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			try {
				String serverName = br.readLine();
				String mydatabase = br.readLine();
				String username = br.readLine(); 
				String password = br.readLine();
				br.close();
				
				dbConfig.setHost(serverName);
				dbConfig.setDatabase(mydatabase);
				dbConfig.setUser(username);
				dbConfig.setPassword(password);
				dbConfig.setLanguage(Language.chinese);
				
				System.out.println("Register to database...");
				wiki = new Wikipedia(dbConfig);
				System.out.println("Done.");
				
				System.out.println("Register to lucene...");
				Directory indexDir = FSDirectory.open(new File(outputLuceneDir));
				writer = new IndexWriter(indexDir, new StandardAnalyzer(Version.LUCENE_29), true, IndexWriter.MaxFieldLength.LIMITED); 
				System.out.println("Done...");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	  }

	  public void indexWikipedia() {
		  Iterable<Integer> pageIDs = wiki.getPageIds();
		  Calendar cal = Calendar.getInstance();
		  long startTime = cal.getTimeInMillis();
		    
		  int addCount = 0;
		  int totalCount = 0;
		  String plainText = "";
		  for (int pid : pageIDs) {
			  try {
				  totalCount++;
					
				  Page page = wiki.getPage(pid);
				  String title = page.getTitle().getPlainTitle();
				  String entity = page.getTitle().getEntity();
				  plainText = page.getPlainText();
				  boolean isDisambiguation = page.isDisambiguation();
				  boolean isRedirection = page.isRedirect();
				  boolean isDiscussion = page.isDiscussion();
				  int numInlink = page.getNumberOfInlinks();
				  int numOutlink = page.getNumberOfOutlinks();
				  int numCatetegory = page.getNumberOfCategories();
					
				  if (numInlink + numOutlink < leastLinkNum)
					  continue;
				  if (plainText.length() < leastWordNum)
					  continue;
				  if (isDisambiguation || isRedirection) {
					  continue;
				  }
					
				  plainText += " " + title;
					
				  Document doc = new Document();
				  doc.add(new Field("contents", 
							plainText,
			        		Field.Store.NO, Field.Index.ANALYZED, 
			        		Field.TermVector.WITH_OFFSETS));

				  doc.add(new Field("id", String.valueOf(pid),
			        		Field.Store.YES,
			        		Field.Index.NOT_ANALYZED));
			    
				  doc.add(new Field("title", title,
			                Field.Store.YES,
			                Field.Index.NOT_ANALYZED));

				  writer.addDocument(doc);
				  //System.out.println("Added: " + id);
				  addCount++;
			        
				  if (totalCount % 100 == 0) {
					  System.out.println("Processed: " + totalCount + " Added: " + addCount);
					  
					  Calendar cal1 = Calendar.getInstance();
					  long endTime = cal1.getTimeInMillis();
					  long second = (endTime - startTime)/1000;
					  System.out.println("Elipsed time: " + second + " seconds");
				  }
			  } catch (Exception e) {
				  System.err.println("Throw exception when processed: " + totalCount + " added: " + addCount);
//				  e.printStackTrace();
			  }
		  }
		  try {
			  writer.optimize();
			  writer.close();
		  } catch (CorruptIndexException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  } catch (IOException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
		  
		  System.out.println("");
		  System.out.println("************************");
		  System.out.println(addCount + " documents added.");
		  System.out.println("************************");
	  }

}
