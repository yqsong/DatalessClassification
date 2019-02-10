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
import edu.illinois.cs.cogcomp.classification.representation.esa.AbstractESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.DiskBasedComplexESA;

/**
 * Shaoshi Ling
 * sling3@illinois.edu
 */

public class CustomizedLabelDataTree extends AbstractTreeLabelData {

	private static final long serialVersionUID = 1L;
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
	public HashMap<String, SparseVector> labels=new HashMap<String, SparseVector>();
	public List<String> LabelList=new ArrayList<String>();
	public int total_level=DatalessResourcesConfig.level;
	AbstractESA esa;
	public static void main(String[] args){
		
//		CustomizedLabelDataTree c=new CustomizedLabelDataTree();
//		c.readTreeHierarchy("");
	}
	
	public CustomizedLabelDataTree (AbstractESA esainput) {
		esa = esainput;
	}
	
	public void setESA (AbstractESA esainput) {
		esa = esainput;
	}
	
	@Override
	public void readTreeHierarchy(String fileTopicHierarchyPath) {
		treeIndex.put("root", new HashSet<String>());
		processLabelData();
		recursion(LabelList, total_level, "root");
	}
	
	public void recursion(List<String> LabelList, int level, String parent){
		if(level==1){;
			for(int i=0;i<LabelList.size();i++){
				treeIndex.get(parent).add(LabelList.get(i));
				parentIndex.put(LabelList.get(i),parent);
				treeLabelNameHashMap.put(LabelList.get(i), LabelList.get(i));
//				System.out.print(LabelList.get(i)+" and the parent is "+parentIndex.get(LabelList.get(i)) +"\n");
			}
			return;
		}

		List<List<Integer>> c=computeCluster(LabelList,DatalessResourcesConfig.cutoff[total_level-level]);	
		
		for (int i = 0; i <c.size(); i++) {
			String category="";
			for (int j = 0; j <c.get(i).size(); j++) {
				String s=LabelList.get(c.get(i).get(j));
				category=category+s+",";
			}
			treeIndex.get(parent).add(category);
			treeIndex.put(category, new HashSet<String>());
			parentIndex.put(category,parent);
			treeLabelNameHashMap.put(category, category);
			List<String> v=getstringarray(c.get(i),LabelList);
			recursion(v, level-1,category);

		}		
	}


	@Override
	public void readTopicDescription(String topicDescriptionPath) {
		
	}
	

	
	private List<String> getstringarray(List<Integer> list, List<String> LabelList){
		List<String> ret=new ArrayList<String>();
		for(int i=0;i<list.size();i++){
			ret.add(LabelList.get(list.get(i)));
			//System.out.print("sublist "+i+" "+LabelList.get(list.get(i))+"\n");
		}
		return ret;
	}
	
	   private List<List<Integer>> computeCluster(List<String> vectorList, double classifyconstant) {
//	        List<Double> vectorNormList = new ArrayList<Double>();
//	        for (int i = 0; i < vectorList.size(); ++i) {
//	            vectorNormList.add(norm(vectorList.get(i)));
//	        }
		   	List<List<Integer>> clusters = new ArrayList<List<Integer>>();
	        int entityNum = vectorList.size();
	        double[][] edgeWeight = new double[entityNum][entityNum]; 
	        for (int i = 0; i < vectorList.size(); i++) {
	            edgeWeight[i][i] = 1;
	            for (int j = i + 1; j < vectorList.size(); j++) {
//	                edgeWeight[i][j] = cosine(vectorList.get(i), vectorList.get(j), vectorNormList.get(i), vectorNormList.get(j));
	                edgeWeight[i][j] = cosine(labels.get(vectorList.get(i)),labels.get(vectorList.get(j)));
	                edgeWeight[j][i] = edgeWeight[i][j];
	            }
	        }
	        
	       

	        // flgList indicates whether an instance is alrealdy accepted by one cluster
	        List<Boolean> flgList = new ArrayList<Boolean>();
	        for (int i = 0; i < vectorList.size(); i++) {
	            flgList.add(true); // true means the i-th instance is not removed
	        }

	        for (int i = 0; i < vectorList.size(); i++) {
	            // if the i-th instance is already accepted by one cluster, then skip it
	            if (flgList.get(i) == false)
	                continue;

	            // build a new cluster
	            List<Integer> idxList = new ArrayList<Integer>();
	            Stack<Integer> idxStack = new Stack<Integer>();
	            idxStack.push(i);
	            while (idxStack.size() != 0) {
	                int k = idxStack.pop();
	                // add indices to the cluster
	                for (int j = 0; j < vectorList.size(); j++) {
	                    // skip if the j-th instance is already accepted by one cluster
	                    if (flgList.get(j) == false)
	                        continue;
	                    // add the j-th instance to idxList
	                    if (edgeWeight[k][j] > classifyconstant) {
	                        idxList.add(j);
	                        flgList.set(j, false);
	                        idxStack.push(j);
	                    }
	                }
	            }

	            clusters.add(idxList);
	        }
	        return clusters;
	    }
	   
	   
	public static double cosine (SparseVector v2, SparseVector v1) {
		double dot = 0;
		if (v1.keyValueMap.size() < v2.keyValueMap.size()) {
			for (String key : v1.keyValueMap.keySet()) {
				if (v2.keyValueMap.containsKey(key) == true) {
					double value1 = v1.keyValueMap.get(key);
					double value2 = v2.keyValueMap.get(key);
					double value3 = 1;
					if (conceptWeights.containsKey(key));
						value3 = conceptWeights.get(key);
					dot += value1 * value2 * value3;
				}
			}
		} else {
			for (String key : v2.keyValueMap.keySet()) {
				if (v1.keyValueMap.containsKey(key) == true) {
					double value1 = v1.keyValueMap.get(key);
					double value2 = v2.keyValueMap.get(key);
					double value3 = 1;
					if (conceptWeights.containsKey(key));
						value3 = conceptWeights.get(key);
					dot += value1 * value2 * value3;
				}
			}
		}
		
		return dot / v1.getNorm() / v2.getNorm();
	}
	
	public void processLabelData(){
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(DatalessResourcesConfig.LabelfilePath));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String child = line.replaceAll("\\s+", " ").trim();
				LabelList.add(child);
				System.out.print(child+"\n");
				List<ConceptData> concepts = new ArrayList<ConceptData>();
				List<String> conceptsList = new ArrayList<String>();
				List<Double> scores = new ArrayList<Double>();
				try {
					if (esa != null)
						concepts = esa.retrieveConcepts(child, 500);
				} catch (Exception e) {
					e.printStackTrace();
				}
				for (int i = 0; i < concepts.size(); i++) {
					conceptsList.add(concepts.get(i).concept + "");
					scores.add(concepts.get(i).score);
				}
				labels.put(child, new SparseVector(conceptsList, scores, false, conceptWeights)) ;	

				//System.out.print("Label added "+child+" \n");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		System.out.println("Finished processing LabelData");
	}

	


}