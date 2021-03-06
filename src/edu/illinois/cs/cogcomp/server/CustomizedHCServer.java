package edu.illinois.cs.cogcomp.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts.AbstractTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.main.CustomizedLabelDataTree;

public class CustomizedHCServer extends AbstractTreeLabelData{
	private static final long serialVersionUID = 1L;
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
	public static HashMap<String, SparseVector> labels=new HashMap<String, SparseVector>();
	public static List<String> LabelList=new ArrayList<String>();
	public static DatalessResourcesConfig config=new DatalessResourcesConfig();


	public static void main(String[] args){
		
//		CustomizedLabelDataTree c=new CustomizedLabelDataTree();
//		c.readTreeHierarchy("");
	}
	
	@Override
	public void readTreeHierarchy(String fileTopicHierarchyPath) {
		treeIndex.put("root", new HashSet<String>());
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(config.LabelfilePath));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String s=line.replaceAll("\\d+.*", "").trim();
				String[] labels=s.split("\t");
				addlabel("root",labels[0]);
				for(int i=1;i<labels.length;i++){
					treeIndex.put(labels[i-1], new HashSet<String>());
					addlabel(labels[i-1],labels[i]);
				}
				//System.out.print("Label added "+child+" \n");
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
