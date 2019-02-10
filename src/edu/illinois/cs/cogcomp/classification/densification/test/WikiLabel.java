package edu.illinois.cs.cogcomp.classification.densification.test;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import edu.illinois.cs.cogcomp.classification.densification.representation.SparseSimilarityCondensation;
import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.yahoo.WikiCateTopicHierarchy;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.HashSort;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.SparseVector;
import edu.illinois.cs.cogcomp.classification.representation.esa.complex.DiskBasedComplexESA;
import edu.illinois.cs.cogcomp.classification.representation.esa.simple.SimpleESALocal;
import edu.illinois.cs.cogcomp.clustering.GeneralKmeans;

public class WikiLabel {
	public static HashMap<String, Double> conceptWeights = new HashMap<String, Double>();
	public static HashMap<String, SparseVector> labels=new HashMap<String, SparseVector>();
	public static List<String> LabelList=new ArrayList<String>();
	public static HashMap<Pair , Double> results=new HashMap<Pair, Double>();
	public static double ClassifierConstant=0.1;
	static DiskBasedComplexESA esa = new DiskBasedComplexESA ();
	static HashMap<DoubleMatrix1D, HashMap<String, Double>> topicVectorMap = new HashMap<DoubleMatrix1D, HashMap<String, Double>>();
	static HashMap<DoubleMatrix1D, Double> topicNormMap = new HashMap<DoubleMatrix1D, Double>();
	static List<DoubleMatrix1D> dataMat = new ArrayList<DoubleMatrix1D>();
	static HashMap<String, Integer> index = new HashMap<String, Integer>();
	static HashMap<Integer,String> inverseindex = new HashMap<Integer,String>();
	static SparseSimilarityCondensation vectorCondensation;
	int index_number;
	
	public static void main(String[] args) throws Exception {
		WikiLabel w=new WikiLabel();
		w.build_index();
		w.read_label();
		String matchingType = SparseSimilarityCondensation.matchingTypes[0];
		vectorCondensation = new SparseSimilarityCondensation(
				 matchingType, 0.85, 0.03, false); 

		//w.processLabelData();
		//w.computeCluster(LabelList);
		/*

		compare(vectorCondensation,"w2v");
		matchingSource = SparseSimilarityCondensation.matchingDist[1];
		matchingType = SparseSimilarityCondensation.matchingTypes[0];
		SparseSimilarityCondensation vectorCondensation2 = new SparseSimilarityCondensation(
				matchingSource, matchingType, 0.85, 0.03); 
		compare(vectorCondensation2,"john");
		*/
		
		w.kmeans(60);
		w.kmeans(80);
		w.kmeans(100);
	}
	
	
	public void kmeans(int cNum) throws IOException{
		HashMap<Integer, List<String>> clusters=new HashMap<Integer, List<String>>();
		//int cNum = 40; // number of clusters you want to cluster
		String method = "maxmin"; // initialization method
		int seed = 0; // seed of random generation
		GeneralKmeans kmeans = new GeneralKmeans(dataMat, cNum, method, seed);
		kmeans.set(this);
		kmeans.estimate();
		int[] label = kmeans.getLabels();;
		for (int i = 0; i < label.length; ++i) {
			System.out.print(label[i] + " ");
			if(clusters.containsKey(label[i]))
				clusters.get(label[i]).add(LabelList.get(i));
			else {
				List<String> l=new ArrayList<String>();
				l.add(LabelList.get(i));
				clusters.put(label[i],l);
			}
		}
		FileWriter fw = new FileWriter("/shared/bronte/sling3/data/test/"+cNum+"-cluster.txt");
		for(int i:clusters.keySet()){
			fw.write("Group "+i+" : "+clusters.get(i).toString()+"\n");
		}		
		fw.close();
		
	}
	public static void labelmatrix() throws IOException{
		WikiLabel w=new WikiLabel();
		w.read_label();
	

		//w.processLabelData();
		//w.computeCluster(LabelList);
		String matchingType = SparseSimilarityCondensation.matchingTypes[0];
		SparseSimilarityCondensation vectorCondensation = new SparseSimilarityCondensation(
				 matchingType, 0.85, 0.03, false); 
		compare(vectorCondensation,"w2v");
		matchingType = SparseSimilarityCondensation.matchingTypes[0];
		SparseSimilarityCondensation vectorCondensation2 = new SparseSimilarityCondensation(
				 matchingType, 0.85, 0.03, false); 
		compare(vectorCondensation2,"john");
	}
	
	public static void compare(SparseSimilarityCondensation vectorCondensation, String vector) throws IOException{
		FileWriter fw2 = new FileWriter("/shared/bronte/sling3/data/test/"+vector+"_label_similarity_sorted.txt");
		int count=0;
		for (String l : LabelList){
			for (String d : LabelList){
				Pair p=new Pair(l,d);

				double similarity = vectorCondensation.similarityWithMaxMatching(topicVectorMap.get(l), topicVectorMap.get(d), topicNormMap.get(l), topicNormMap.get(d));
				System.out.println(count+" "+"Finished label comparison"+": "+l+"--"+d+"--"+similarity+"\n");
				results.put(p,similarity);	
				count++;
			}
		}

		TreeMap<Pair, Double> sorted = HashSort.sortByValues(results);
		for(Pair p: sorted.keySet()){
			fw2.write(p.s1+" "+p.s2+" "+results.get(p)+"\n");
		}	

		fw2.close();
	}
	
	public double distance(DoubleMatrix1D v1, DoubleMatrix1D v2){
		List<ConceptData> vectorTopic=getConceptDataList(v1);
		HashMap<String, Double> vector1 = getVectorMap(vectorTopic);
		double normTopic1 = getNorm (vector1);
		List<ConceptData> vectorTopic2=getConceptDataList(v2);
		HashMap<String, Double> vector2 = getVectorMap(vectorTopic2);
		double normTopic2 = getNorm (vector2);
		
		return vectorCondensation.similarityWithMaxMatching(vector1, vector2, normTopic1, normTopic2);
		
	}
	
	public List<ConceptData> getConceptDataList(DoubleMatrix1D v1){
		IntArrayList arg0 = new IntArrayList() ;
		DoubleArrayList arg1=new DoubleArrayList();
		v1.getNonZeros(arg0, arg1);
		List<ConceptData> vectorTopic=new ArrayList<ConceptData>() ;
		for(int i=0;i<arg0.size();i++){
			String concept=inverseindex.get(arg0.get(i));
			ConceptData data=new ConceptData(concept,arg1.get(i));
			vectorTopic.add(data);
		}
		return vectorTopic;
	}
	
	public static HashMap<String, Double> getVectorMap (List<ConceptData> conceptList) {
		HashMap<String, Double> vectorMap = new HashMap<String, Double>();
		
		for (int i = 0; i < conceptList.size(); ++i) {
			vectorMap.put(conceptList.get(i).concept, conceptList.get(i).score);
		}
		
		return vectorMap;
	}
	
	public  void writeclusterlabel( ) throws Exception{
		FileWriter fw3 = new FileWriter("data/wikitest/labelcluster/constant"+ClassifierConstant+".txt");
		List<List<Integer>>c=computeCluster(LabelList);
		 for (int i = 0; i <c.size(); i++) {
			 fw3.write("Grouo"+i+" :\n");   
			 for (int j=0;j<c.get(i).size();j++){
				 String s=LabelList.get(c.get(i).get(j));
				 fw3.write(j+" "+s+"      ");  
	         }
			 fw3.write("\n\n");
		 }
		fw3.close();
	
	}
	
	public  void printclusterlabel(List<List<Integer>> c,List<String> v ) throws Exception{
		 for (int i = 0; i <c.size(); i++) {
			 System.out.print("Grouop"+i+" :\n");   
			 for (int j=0;j<c.get(i).size();j++){
				 String s=v.get(c.get(i).get(j));
				 System.out.print(j+" "+s+"\n");  
	         }
			 System.out.print("\n\n");
		 }
	
	}
	
	private List<List<List<Integer>>> deepcluster( List<List<Integer>> clusterlist) throws Exception{
		FileWriter fw4 = new FileWriter("data/wikitest/labelcluster/two-level-clustered.txt");
		List<List<List<Integer>>> ret=new ArrayList<List<List<Integer>>>();
		for (int i = 0; i <clusterlist.size(); i++) {
			//System.out.print("\n"+"List"+i+"\n");
			fw4.write("Category"+i+" :\n");
			List<String> v=getstringarray(clusterlist.get(i));
			List<List<Integer>> temp=computeCluster(v);
			for (int j = 0; j <temp.size(); j++) {
				 fw4.write("\nGrouop"+j+" :\n");   
				 for (int k=0;k<temp.get(j).size();k++){
					 String s=v.get(temp.get(j).get(k));
					 fw4.write(k+" "+s+"    ");  
		         }
				// fw4.write("\n\n");
			 }
			fw4.write("\n\n");
			//printclusterlabel(temp,v);
			//ret.add(computeCluster(v));
		 }
		
		
		fw4.close();
		return ret;
		
	}
	
	private List<String> getstringarray(List<Integer> list){
		List<String> ret=new ArrayList<String>();
		for(int i=0;i<list.size();i++){
			ret.add(LabelList.get(list.get(i)));
			//System.out.print("sublist "+i+" "+LabelList.get(list.get(i))+"\n");
		}
		return ret;
	}
	
	   private List<List<Integer>> computeCluster(List<String> vectorList) {
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
	                    if (edgeWeight[k][j] > ClassifierConstant) {
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

	
	
	public void build_index(){
		String filePath="/shared/bronte/sling3/data/cateInfo.txt";
		
		int count=0;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split("\t");
				String child=tokens[0].replaceAll("[^A-Za-z0-9]", " ").trim();
				
				String label = child;

				List<ConceptData> vectorTopic = esa.retrieveConcepts(label, 500, "tfidfVector");
				
				for(ConceptData data:vectorTopic){
					if(!index.containsKey(data.concept)) {
						index.put(data.concept, count++);
						inverseindex.put(count,data.concept);
					}	
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		index_number=count;
		System.out.println("Finished INDEXING LabelData");
		
	}
	
	
	public void read_label() {
		
		String filePath="/shared/bronte/sling3/data/cateInfo.txt";
	
		int count=0;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split("\t");
				String child=tokens[0].replaceAll("[^A-Za-z0-9]", " ").trim();
				
				String label = child;
//				String[] words = child.split(" ");
//				for (String word : words) {
//					word = word.replaceAll("[^A-Za-z0-9]", "").trim();
//					label += word + " ";
//				}
//				label = label.trim();
				
				System.out.print(label+"\n");
				List<ConceptData> vectorTopic = esa.retrieveConcepts(label, 500, "tfidfVector");
				DoubleMatrix1D vec = new SparseDoubleMatrix1D(index_number);
				for(ConceptData data:vectorTopic){
					vec.setQuick(index.get(data.concept), data.score);

				}
				dataMat.add(vec);
				HashMap<String, Double> vector1 = getVectorMap(vectorTopic);
				topicVectorMap.put( vec, vector1);
				double normTopic1 = getNorm (vector1);
				topicNormMap.put(vec, normTopic1);
				LabelList.add(label);
				//System.out.print("Label added "+label+" \n");
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



	public static double getNorm (HashMap<String, Double> vector) {
		double norm = 0;
		for (String key : vector.keySet()) {
			double value = vector.get(key);
			norm += value * value;
		}
		norm = Math.sqrt(norm);
		return norm;
	}
	
	public HashMap<String, HashMap<String, String>> TopicHierarchy() {
		HashMap<String, HashMap<String, String>> topicHierarchy= new HashMap<String, HashMap<String, String>>();
		WikiLabel w=new WikiLabel();
		w.processLabelData();
		ClassifierConstant=0.05;
		List<List<Integer>>c=w.computeCluster(LabelList);
		for (int i = 0; i <c.size(); i++) {
			HashMap<String, String> topicMapping = new HashMap<String, String>();
			String category="";
			for (int j = 0; j <c.get(i).size(); j++) {
				 String s=LabelList.get(c.get(i).get(j));
				 topicMapping.put(s, s);
		         category=category+s+" ";
			}
			topicHierarchy.put(category,topicMapping);
			System.out.print(category+"\n");
		 }
		
		return topicHierarchy;
	}
}
