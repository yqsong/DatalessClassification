package edu.illinois.cs.cogcomp.classification.hierarchy.dataless.sample;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.AbstractConceptTree;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeTopDownML;
import edu.illinois.cs.cogcomp.descartes.util.Utilities;

/**
 * yqsong@illinois.edu
 */

public class PrepareDataSearchFrom20Newsgroups {
	
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
	private static IndexSearcher searcher;;

	public static void main (String[] args) {
//		process20NewsgroupsDataBy20NG (10);
		process20NewsgroupsDataBy20NG (50);
//		process20NewsgroupsDataBy20NG (100);
//		process20NewsgroupsDataBy20NG (200);
//		process20NewsgroupsDataBy20NG (500);
	}
	
	
	public static void process20NewsgroupsDataBy20NG (int topK) {
		String fileInputIndexDirStr = "data/20newsgroups/textindex";
		String fileTopicHierarchyPath = "";
		String fileOutputPath = "data/20newsgroups/output/20newsgroups.datadriven.search.top" + topK + ".data";
		AbstractConceptTree tree = new ConceptTreeTopDownML("20newsgroups", null, conceptWeights, false);
		System.out.println("process tree...");
		tree.treeLabelData.readTreeHierarchy(fileTopicHierarchyPath);
		ConceptTreeNode rootNode = tree.initializeTree("root", 0);
		tree.setRootNode(rootNode);
		tree.aggregateChildrenDescription(rootNode);
		
		process20NewsgroupsDataBy20NG(tree, topK, fileInputIndexDirStr, fileOutputPath);
	}
	
	public static void process20NewsgroupsDataBy20NG (AbstractConceptTree tree, int retrievedNum, String inputIndexDirStr, String outputFile) {
	    Version AnalyzerVersion = Version.LUCENE_24;
		
		FileWriter writer = null;
		try {
			Directory inputDir =  FSDirectory.open(new File(inputIndexDirStr));
			IndexReader readeroutput = IndexReader.open(inputDir, true);
			searcher = new IndexSearcher(readeroutput);
			Analyzer analyzer = new StandardAnalyzer(AnalyzerVersion, new HashSet<String>());
			
			writer = new FileWriter(outputFile);
			List<ConceptTreeNode> treeNodeList = tree.getTreeNodeList();
			
			double avgAcc = 0;
			int labelNum = 0;
			for (int i = 0; i < treeNodeList.size(); ++i) {
				ConceptTreeNode node = treeNodeList.get(i);
				String label = node.getLabelString();
				String description = node.getLabelDescriptioinString();
				
		    	Query query = new TermQuery(new Term("plain", description)); 
		    	QueryParser parser = new QueryParser(AnalyzerVersion, "plain", analyzer);
				query = parser.parse(description);
		    	TopDocs docs = searcher.search(query, null, Double.MAX_EXPONENT);
		    	ScoreDoc[] hits = docs.scoreDocs;
				

				int correct = 0;
				int count = 0;
				for (int j = 0; j < Math.min(hits.length, retrievedNum); ++j) {
		    		int docID = hits[j].doc;
		    		Document doc = readeroutput.document(docID);
		    		String text = doc.get("plain");
		    		
		    		String trueLabel = doc.get("newsgroup");
		    		
		    		if (label.equals(trueLabel) == true) {
		    			correct++;
		    		}
		    		count++;
		    		
					text = text.replaceAll("\n", " ");
					text = text.replaceAll("\r", " ");
					text = text.replaceAll("\t", " ");
					text = text.replaceAll("\\pP", " ");
					//>|<=+`~!@#$%^&*()-_{}
//					text = text.replaceAll("[>|<=+@#$%^&*()_{}]", " ");
					text = text.replaceAll("[>|<=+`~!@#$%^&*()_{}]", " ");
					text = text.replaceAll("[1-9]", " ");
					text = text.replaceAll("\\s+", " ").toLowerCase();
					
					writer.write(label + "_" + i + "_" + j + "\t" + label + "\t" + text); 
					writer.write(System.getProperty("line.separator").toString()); 
				}
				
				if ((double)correct/count != 0) {
					System.out.print ("Search label: [" + label + "] with description: [" + description + "]");
					System.out.print (", Hit doc num: " + hits.length);
					System.out.println(", Top " + retrievedNum + " accuracy : " + String.format("%.4f", (double)correct/count));
					avgAcc += (double)correct/count;
					labelNum++;
				}
			}
			System.out.println("Average Accrucy : " + String.format("%.4f", avgAcc/labelNum));
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		
	}
}
