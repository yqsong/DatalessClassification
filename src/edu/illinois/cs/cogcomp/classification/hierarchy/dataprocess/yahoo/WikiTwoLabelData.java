package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.yahoo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts.AbstractTreeLabelData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig;
import edu.illinois.cs.cogcomp.classification.representation.esa.simple.SimpleESALocal;

public class WikiTwoLabelData extends AbstractTreeLabelData {

	private static final long serialVersionUID = 1L;

	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
	public static HashMap<String, SparseVector> labels=new HashMap<String, SparseVector>();
	public static List<String> LabelList=new ArrayList<String>();
	protected SimpleESALocal esa =  new SimpleESALocal();
	public static double[] cutoff = new double[4];
	public static FileWriter fw ;
	
	public static void main(String[] args) throws IOException{
		//fw = new FileWriter("data/wikitest/labelcluster/0.05+0.1.txt");
		WikiTwoLabelData c=new WikiTwoLabelData();		
		c.readTreeHierarchy("");

	}
	

	
	@Override
	public void readTreeHierarchy(String fileTopicHierarchyPath) {
		treeIndex.put("root", new HashSet<String>());
		cutoff[0]=0.03;
		cutoff[1]=0.08;
		processLabelData();
		try {
			recursion(LabelList,3,"root");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void recursion(List<String> LabelList, int level, String parent) throws IOException{
		if(level==1){;
			for(int i=0;i<LabelList.size();i++){
				treeIndex.get(parent).add(LabelList.get(i));
				parentIndex.put(LabelList.get(i),parent);
				treeLabelNameHashMap.put(LabelList.get(i), LabelList.get(i));
				//fw.write("Level "+(5-level)+" "+LabelList.get(i)+" ");
				//System.out.print("Level "+(5-level)+" "+LabelList.get(i)+" ");
			}
			return;
		}

		List<List<Integer>> c=computeCluster(LabelList,cutoff[3-level]);	
		
		for (int i = 0; i <c.size(); i++) {
			String category=new String();
			for (int j = 0; j <c.get(i).size(); j++) {
				String s=LabelList.get(c.get(i).get(j));
				category=category+s+",";
			}
			treeIndex.get(parent).add(category);
			treeIndex.put(category, new HashSet<String>());
			parentIndex.put(category,parent);
			treeLabelNameHashMap.put(category, category);
			List<String> v=getstringarray(c.get(i),LabelList);
			//fw.write("Level "+(4-level)+" "+category+"\n");
			//System.out.print("Level "+(5-level)+" "+category+"\n");
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
		String filePath="data/wikitest/WikiCate.txt";

		try {		
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			while ((line = reader.readLine()) != null) {
				//System.out.print(line+"\n");
				if (line.isEmpty() == true && line.equals("") == true){ 
						//System.out.print("return at line 265\n");
					continue;
				}	
				String[] tokens = line.split("\t");
				if (tokens.length != 4) {
						//System.out.print("return at line 270 and token length is"+tokens.length+"\n");
						//System.out.print(tokens[0]+"\n"+tokens[1]+"\n"+tokens[2]+"\n"+tokens[3]+"\n");
						continue;
				}
				String child = tokens[1].trim().toLowerCase();
				String conceptStr = tokens[3].trim();
				String[] concepts = conceptStr.split(";");
				List<String> conceptsList = new ArrayList<String>();
				List<Double> scores = new ArrayList<Double>();
				for (int i = 0; i < concepts.length; ++i) {
					String[] subTokens = concepts[i].split(",");
					String id = subTokens[0];
					double value = Double.parseDouble(subTokens[1]);	
					conceptsList.add(id);
					scores.add(value);
				}
				labels.put(child, new SparseVector(conceptsList, scores, false, conceptWeights)) ;
				LabelList.add(child);
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Finished processing LabelData");
	}
	
public void processLabelData2(){
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("data/wikitest/t.txt"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String child=line.replaceAll("\\d+.*", "").trim();
				System.out.print(child+"\n");
				List<ConceptData> concepts = null;
				List<String> conceptsList = new ArrayList<String>();
				List<Double> scores = new ArrayList<Double>();
				try {
					concepts = esa.getConcepts(500, child);
				} catch (Exception e) {
					e.printStackTrace();
				}
				for (int i = 0; i < concepts.size(); i++) {
					conceptsList.add(concepts.get(i).concept + "");
					scores.add(concepts.get(i).score);
				}
				labels.put(child, new SparseVector(conceptsList, scores, false, conceptWeights)) ;	
				LabelList.add(child);
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