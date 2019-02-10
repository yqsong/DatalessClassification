package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.nytimes;

import java.io.File;
import java.util.HashSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts.AbstractTreeLabelData;

/**
 * yqsong@illinois.edu
 */

public class NYTimesTreeLabelData extends AbstractTreeLabelData {
	
	private static final long serialVersionUID = 1L;

	public NYTimesTreeLabelData () {
		super();
	}


	@Override
	public void readTreeHierarchy(String fileTopicHierarchyPath) {
		String taxonomy = "";
		try {
			Directory inputDir = FSDirectory.open(new File(fileTopicHierarchyPath));
			IndexReader reader = IndexReader.open(inputDir, true);
			int maxDocNum = reader.maxDoc();
			for (int i = 0; i < maxDocNum; ++i) {
				if (reader.isDeleted(i) == false) {
					if (i % 10000 == 0) {
						System.out.println ("[Read NYTimes Taxonomy: ] " + i + "docs ..");
					}
					Document doc = reader.document(i);
					taxonomy = doc.get("Taxonomic Classifiers");
					if (taxonomy == null) 
						continue;
					String taxonomyLower = taxonomy.toLowerCase();
					if (taxonomyLower.contains("classifieds"))
						continue;
					
			//Top/News/New York and Region||Top/News/New York and Region/New Jersey
					String[] tokens = taxonomyLower.trim().replace("||", "\t").split("\t");
					for (int j = 0; j < tokens.length; ++j) {
						String taxonomyJ = tokens[j].trim();
						String[] taxonomyTokens = taxonomyJ.split("/");
						for (int k = 0; k < taxonomyTokens.length - 1; ++k) {
							if (parentIndex.containsKey(taxonomyTokens[k + 1]) == false) {
								parentIndex.put(taxonomyTokens[k + 1], taxonomyTokens[k]);
							} else if (parentIndex.get(taxonomyTokens[k + 1]).equals(taxonomyTokens[k]) == false
									&& taxonomyTokens[k + 1].equals(taxonomyTokens[k]) == false) {
								break;
							}
							
							if (treeIndex.containsKey(taxonomyTokens[k]) == true) {
								if (treeIndex.get(taxonomyTokens[k]).contains(taxonomyTokens[k + 1]) == false) {
									treeIndex.get(taxonomyTokens[k]).add(taxonomyTokens[k + 1]);
								}
							} else {
								treeIndex.put(taxonomyTokens[k], new HashSet<String>());
								treeIndex.get(taxonomyTokens[k]).add(taxonomyTokens[k + 1]);
							} 
							
							
							
							if (treeLabelNameHashMap.containsKey(taxonomyTokens[k]) == false) {
								treeLabelNameHashMap.put(taxonomyTokens[k], taxonomyTokens[k]);
							}
							if (treeLabelNameHashMap.containsKey(taxonomyTokens[k + 1]) == false) {
								treeLabelNameHashMap.put(taxonomyTokens[k + 1], taxonomyTokens[k + 1]);
							}
						}
					}

				}
			}
			reader.close();
		} catch (Exception e) {
			System.out.println(taxonomy);
			e.printStackTrace();
		}
	}

	@Override
	public void readTopicDescription(String topicDescriptionPath) {
		
	}
		
}
