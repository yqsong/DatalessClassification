package edu.illinois.cs.cogcomp.classification.densification.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.DiskBasedComplexESA;

public class read_result {
	public static void main (String[] args) throws Exception {
		int cor=0;
		int failed=0;
		try {
			FileReader reader = new FileReader("/shared/bronte/sling3/data/Wiki-test-resultw2v.txt");
			BufferedReader br = new BufferedReader(reader);

			String line = "";
			while ((line = br.readLine()) != null) {

				String [] tokens2=line.split("          ");
				String[] tokens = line.split(" ");

				
				String dataID = tokens[0];
				String r=tokens[1];
				String label = tokens[2].replaceAll("[^a-zA-Z\\s]", " ").replaceAll("\\s+", " ").toLowerCase();
				String label2= tokens2[1];
				System.out.print(dataID+" "+r+" "+label+" "+label2+"\n");
				Boolean b=label.equals(label2);
				if(b)
					cor++;
				else
					failed++;

			}
			br.close();
			reader.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished reading ContentData, Docnum");
		System.out.println("Correct  "+cor+" failed label"+failed);
	}
}
