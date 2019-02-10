package edu.illinois.cs.cogcomp.classification.main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts.AbstractTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.representation.esa.simple.SimpleESALocal;

/**
 * Shaoshi Ling
 * sling3@illinois.edu
 */

public class CustomizedLabelDataHCTree extends AbstractTreeLabelData{
	private static final long serialVersionUID = 1L;
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
	public static HashMap<String, SparseVector> labels=new HashMap<String, SparseVector>();
	public static List<String> LabelList=new ArrayList<String>();
	public static DatalessResourcesConfig config=new DatalessResourcesConfig();


	public static void main(String[] args){
		
		CustomizedLabelDataHCTree c=new CustomizedLabelDataHCTree();
		c.readTreeHierarchy("");
	}
	
	@Override
	public void readTreeHierarchy(String fileTopicHierarchyPath) {
		treeIndex.put("root", new HashSet<String>());
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(config.HCLabelfilePath));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String s=line.replaceAll("\\d+.*", "").trim();
				String[] labels=s.split("\t");
				addlabel("root",labels[0]);
				for(int i=1;i<labels.length;i++){
					treeIndex.put(labels[0], new HashSet<String>());
					addlabel(labels[0],labels[i]);
					System.out.print(" Label added "+labels[i]+" \n");
				}
				System.out.print("Parent Label added "+labels[0]+" \n");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addlabel( String parent,String category){
			treeIndex.get(parent).add(category);
			parentIndex.put(category,parent);
			treeLabelNameHashMap.put(category, category);
	}


	@Override
	public void readTopicDescription(String topicDescriptionPath) {
		
	}
	



}
