package edu.illinois.cs.cogcomp.classification.densification.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.densification.representation.SparseSimilarityCondensation;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ConceptTreeNode;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataless.representation.ml.ConceptTreeBottomUpML;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.DiskBasedComplexESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.simple.SimpleESALocal;
public class wiki_documents {
	public static void main (String[] args) throws Exception {
		DiskBasedComplexESA esa = new DiskBasedComplexESA ();
		String complexVectorType = "tfidfVector";
		FileWriter fw = new FileWriter("/shared/bronte/sling3/data/contents.txt");
		
		try {
			FileReader reader = new FileReader("/shared/bronte/sling3/data/contentInfo.txt");
			BufferedReader br = new BufferedReader(reader);
			
			String line = "";
			int count = 0;
			while ((line = br.readLine()) != null) {
				
				if (count % 100 == 0) {
					//System.out.println("Processed content " + count + " lines...");
				}
				
				String[] tokens = line.split("\t");

				if (tokens.length != 2) 
					continue;
				
				String dataID = tokens[0];
				String content = tokens[1].replaceAll("[^a-zA-Z\\s]", " ").replaceAll("\\s+", " ");;
				List<ConceptData> conceptList = esa.retrieveConcepts(content, 500, complexVectorType);
				fw.write(Integer.toString(count)+"\t");
				for(ConceptData data:conceptList){
					fw.write(data.concept+","+data.score+";");
				}
				fw.write(ClassifierConstant.systemNewLine);
				//System.out.println("DataId  " + dataID  + "content "+ content +"\n");
				
				count++;
			}
			br.close();
			reader.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished reading ContentData, Docnum");
	
	}
}
