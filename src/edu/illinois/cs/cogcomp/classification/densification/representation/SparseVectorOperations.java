package edu.illinois.cs.cogcomp.classification.densification.representation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class SparseVectorOperations<T> {
	public static String systemNewLine = System.getProperty("line.separator");
	
//	private static IndexReader reader = null;
//	private static Directory fsdir = null;

	public static HashMap<String, String> pageTitleIDMap = new HashMap<String, String>();
	public static HashMap<String, String> pageIDTitleMap = new HashMap<String, String>();
	HashMap<String, Integer> pageLengthMap = new HashMap<String, Integer>();
	HashMap<String, Integer> pageCateNumMap = new HashMap<String, Integer>();
	HashMap<String, Integer> pageInlinkNumMap = new HashMap<String, Integer>();
	HashSet<String> pageIDs = new HashSet<String>();

	HashMap<String, Integer> clusterCateNumMap = new HashMap<String, Integer>();

	
    public static String[] weightTypes = new String[] {"uniform", "pageLength", "cateNum", "inlinkNum", "clusterSize"}; 
	
    
    public static void  main (String[] args) throws Exception {

    }
    
	public SparseVectorOperations (String weightType) {

		
		String pageTitlePath = "data/wiki_structured/wikiPageIDMapping.txt";
		getLegitimatePageIDs (pageTitlePath);

		if (weightType.equals(weightTypes[0])) {
		} else if (weightType.equals(weightTypes[1])) {
			
			String pageLengthPath = "data/wiki_structured/pageLength.txt";
			getPageLength (pageLengthPath);
			
		} else if (weightType.equals(weightTypes[2])) {

			String categoryInformationPath = "data/wiki_structured/pageCateNum.txt";
			getPageCateNum(categoryInformationPath);
			
		} else if (weightType.equals(weightTypes[3])) {

			String inlinkInformationPath = "data/wiki_structured/pageInlinkNum.txt";
			getPageInlinkNum(inlinkInformationPath);

		} 
	}
	
	public SparseVectorOperations (String weightType, int clusterType) {
		this (weightType);

		
		if (weightType.equals(weightTypes[4])) {
			int threshold1 = 0;
			int threshold2 = 100000000;
			if (clusterType == 3) {
				threshold1 = 10;
				threshold2 = 100;
			}
			String[] similarityTypes = new String[] {"vectors", "vectors_shyam", "content_random", "categoryInfo"};
			String clusterFile = "data/wikipedia_titleClusters/clusters_" + similarityTypes[clusterType] + ".txt";
			loadClusters (clusterFile, threshold1, threshold2);
		
			String inlinkInformationPath = "data/wiki_structured/pageInlinkNum.txt";
			getPageInlinkNum(inlinkInformationPath);

			for (int i = 0; i < clusterList.size(); ++i) {
				int countCateNum = 0;
				for (int j = 0; j < clusterList.get(i).size(); ++j) {
					String pageID = clusterList.get(i).get(j);
//					if (pageCateNumMap.containsKey(pageID)) {
//						countCateNum += pageCateNumMap.get(pageID);
//					}
					if (pageInlinkNumMap.containsKey(pageID)) {
						countCateNum += pageInlinkNumMap.get(pageID);
					}
				}
				clusterCateNumMap.put(i + "", countCateNum);
			}
			
		}

	}
	
	List<List<String>> clusterList = new ArrayList<List<String>>();
	HashMap<String, HashSet<String>> clusterIDMap = new HashMap<String, HashSet<String>>();

	
	public double probabilisticInference (HashMap<T, Double> vector1, HashMap<T, Double> vector2, String weightedType) {
//		double sumV1 = 0;
//		for (T key : vector1.keySet()) {
//			sumV1 += vector1.get(key);
//		}
//		for (T key : vector1.keySet()) {
//			vector1.put(key, vector1.get(key)/sumV1);
//		}
//	        
//		double sumV2 = 0;
//		for (T key : vector2.keySet()) {
//			sumV2 += vector2.get(key);
//		}
//		for (T key : vector2.keySet()) {
//			vector2.put(key, vector2.get(key)/sumV2);
//		}

	        
		double dot = 0;
		for (T key : vector1.keySet()) {
			double value1 = vector1.get(key);
			
			if (vector2.containsKey(key) == true) {
				
				double value2 = vector2.get(key);
				
				String newKey = key + "";
				if (pageTitleIDMap.containsKey(key)) {
					newKey = pageTitleIDMap.get(key);
				}

//				try {
//					int keyInt = Integer.parseInt((String)newKey);
//					int b = keyInt;
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
			
				
				double weight = 1;
				if (weightedType.equals("pageLength")) {
					if (pageLengthMap.containsKey(newKey)) {
						weight = (double) 1 * Math.log((double) pageLengthMap.get(newKey) + 1);
					}
				} else if (weightedType.equals("cateNum")) {
					if (pageCateNumMap.containsKey(newKey)) {
						weight = (double) 1 * Math.log((double) pageCateNumMap.get(newKey) + 1);
					}
				} else if (weightedType.equals("inlinkNum")) {
					if (pageInlinkNumMap.containsKey(newKey)) {
						weight = (double) 1 * Math.log((double) pageInlinkNumMap.get(newKey) + 1);
					}
				} if (weightedType.equals("clusterSize")) {
//					if (clusterList.size() > 0 && clusterList.size() > keyInt) {
//						weight = (double) 1 * Math.log((double) clusterList.get(keyInt).size() + 1);
//					}
					if (clusterCateNumMap.containsKey(newKey)) {
						weight = (double) 1 * Math.log((double) clusterCateNumMap.get(newKey) + 1);
					}
				}
				
                double tempValue = (value1) * (value2);

				dot += tempValue * weight;
			} 
		}
		
		return (dot);
		
//		for (T key : vector1.keySet()) {
//			double value1 = vector1.get(key);
//			
//			if (vector2.containsKey(key) == true) {
//				
//				double value2 = vector2.get(key);
//				
//	            double tempValue = Math.abs(value1 - value2);
//
//				dot += tempValue;
//			} else {
//				dot +=  Math.abs(value1 );
//			}
//		}
//		
//		for (T key : vector2.keySet()) {
//			double value2 = vector2.get(key);
//			
//			if (vector1.containsKey(key) == true) {
//				
//			} else {
//				dot +=  Math.abs(value2);
//			}
//		}
//		
//		return (1/dot);
	}
	

	public void loadClusters (String filePath, int threshold1, int threshold2) {
		try {
			FileReader reader = new FileReader(filePath);
			BufferedReader br = new BufferedReader(reader);
			
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(":");
				String idStr = tokens[1];
				String[] ids = idStr.split(",");
				List<String> idList = new ArrayList<String>();
				for (int i = 0; i < ids.length; ++i) {
					idList.add(ids[i]);
				}
				clusterList.add(idList);
			}
			br.close();
			reader.close();
			
			for (int i = 0; i < clusterList.size(); ++i) {
				if (clusterList.get(i).size() >= threshold1 && clusterList.get(i).size() <= threshold2) {
					for (int j = 0; j < clusterList.get(i).size(); ++j) {
						String pageID = clusterList.get(i).get(j);
						if (clusterIDMap.containsKey(pageID) == false) {
							clusterIDMap.put(pageID, new HashSet<String>());
						}
						clusterIDMap.get(pageID).add(i + "");
					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void getPageLength (String pageLengthPath) {
		
		try {
			FileReader reader = new FileReader(pageLengthPath);
			BufferedReader br = new BufferedReader(reader);
			
			String line = "";
			int count = 0;
			while ((line = br.readLine()) != null) {
				
				if (count % 100000 == 0) {
					System.out.println("Processed pagelength " + count + " lines...");
				}
				count++;
				
				String[] tokens = line.split("\t");
				
				String pageID = tokens[0];
				int inlinkID = Integer.parseInt(tokens[1]);
				if (pageIDs.contains(pageID)) {
					pageLengthMap.put(pageID, inlinkID);					
				}
			}
			br.close();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished reading");
		
	}
	
	public void getPageInlinkNum (String inlinkInformationPath) {
		
		try {
			FileReader reader = new FileReader(inlinkInformationPath);
			BufferedReader br = new BufferedReader(reader);
			
			String line = "";
			int count = 0;
			while ((line = br.readLine()) != null) {
				
				if (count % 100000 == 0) {
					System.out.println("Processed inlinks " + count + " lines...");
				}
				count++;
				
				String[] tokens = line.split("\t");
				
				String pageID = tokens[0];
				int inlinkNum = Integer.parseInt(tokens[1]);
				if (pageIDs.contains(pageID)) {
					
					pageInlinkNumMap.put(pageID, inlinkNum);
					
				}
			}
			br.close();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished reading");
		
	}
	
	public void getPageCateNum (String categoryInformationPath) {
		
		try {
			FileReader reader = new FileReader(categoryInformationPath);
			BufferedReader br = new BufferedReader(reader);
			
			String line = "";
			int count = 0;
			while ((line = br.readLine()) != null) {
				
				if (count % 100000 == 0) {
					System.out.println("Processed category " + count + " lines...");
				}
				count++;
				
				String[] tokens = line.split("\t");
				
				String pageID = tokens[0];
				int cateNum = Integer.parseInt(tokens[1]);
				if (pageIDs.contains(pageID)) {
					
					pageCateNumMap.put(pageID, cateNum);
					
				}
			}
			br.close();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished reading");
	}
	
	
	public HashSet<String> getLegitimatePageIDs (String pageLengthPath) {
		try {
			FileReader reader = new FileReader(pageLengthPath);
			BufferedReader br = new BufferedReader(reader);
			
			String line = "";
			int count = 0;
			while ((line = br.readLine()) != null) {
				
				if (count % 100000 == 0) {
					System.out.println("Processed title " + count + " lines...");
				}
				count++;
				
				String[] tokens = line.split("\t");
				if (tokens.length != 2)
					continue;
				
				String pageID = tokens[0];
				String title = tokens[1];
				pageTitleIDMap.put(title.trim(), pageID);	
				pageIDTitleMap.put(pageID, title.trim());
				pageIDs.add(pageID.trim());
			}
			br.close();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished reading");
		
		System.out.println("There are " + pageIDs.size() + " titles");
		System.out.println("There are " + pageTitleIDMap.size() + " titles");
		System.out.println("There are " + pageIDTitleMap.size() + " titles");
		
		return pageIDs;
		
//		int maxid = reader.maxDoc();
//	    
//	    Calendar cal = Calendar.getInstance();
//	    long startTime = cal.getTimeInMillis();
//	    
//	    for(int i = 0; i < maxid; i++){
//	    	if (i % 100000 == 0) {
//	    		System.out.print("Processed page: " + i + " out of " + maxid + " documents..");
//	    		
//	    		Calendar cal1 = Calendar.getInstance();
//	    		long endTime = cal1.getTimeInMillis();
//	    		long second = (endTime - startTime)/1000;
//	    		System.out.println("  Elipsed time: " + second + " seconds");
//	    	}
//
//    		try {
//		    	if(!reader.isDeleted(i)){
//		    		String wikiTitle = reader.document(i).getField("title").stringValue();
//		    		int wikiID = Integer.valueOf(reader.document(i).getField("id").stringValue());
//		    		pageIDs.add(wikiID + "");
//	    		}
//    		}
//    		catch(Exception e){
//    			e.printStackTrace();
//    			continue;
//    		}
//    		
//	    }
//	    
//	    return pageIDs;
	    
	}
	

	
	public double getNorm (HashMap<T, Double> vector) {
		double norm = 0;
		for (T key : vector.keySet()) {
			double value = vector.get(key);
			norm += value * value;
		}
		return norm = Math.sqrt(norm);
	}
	
	public double cosine (HashMap<T, Double> vector1, HashMap<T, Double> vector2) {
		double norm1 = getNorm(vector1);
		double norm2 = getNorm(vector2);
		double dot = 0;
		if (vector1.size() < vector2.size()) {
			for (T key : vector1.keySet()) {
				if (vector2.containsKey(key) == true) {
					double value1 = vector1.get(key);
					double value2 = vector2.get(key);
					dot += value1 * value2;
				}
			}
		} else {
			for (T key : vector2.keySet()) {
				if (vector1.containsKey(key) == true) {
					double value1 = vector1.get(key);
					double value2 = vector2.get(key);
					dot += value1 * value2;
				}
			}
		}
		
		return dot / (norm1 + Double.MIN_NORMAL) / (norm2 + Double.MIN_NORMAL);
	}
	
	public double jaccard (HashMap<T, Double> vector1, HashMap<T, Double> vector2) {
		Set<T> set1 = new HashSet<T>(vector1.keySet());
		Set<T> set2 = (Set<T>) vector2.keySet();
		set1.retainAll(set2);
		int overlap = set1.size();
		return ((double) overlap) / (set1.size() + set2.size());
	}

    public double SkewDivergence(HashMap<T, Double> vector1, HashMap<T, Double> vector2, double gamma)
    {
        double result = 0.0;

//        //combine two vectors and get a middle
        HashMap<T, Double> middleVector = new HashMap<T, Double>();
        
        double sumV1 = 0;
        for (T key : vector1.keySet()) {
        	sumV1 += vector1.get(key);
        }
        for (T key : vector1.keySet()) {
        	vector1.put(key, vector1.get(key)/sumV1);
        }
        
        double sumV2 = 0;
        for (T key : vector2.keySet()) {
        	sumV2 += vector2.get(key);
        }
        for (T key : vector2.keySet()) {
        	vector2.put(key, vector2.get(key)/sumV2);
        }

        for (T key : vector2.keySet())
        {
        	double value2 = vector2.get(key);
        	if (vector1.containsKey(key) == true) {
            	double value1 = vector1.get(key);
            	middleVector.put(key, (gamma * value1 + (1 - gamma) * value2));
        	}
        	else {
        		middleVector.put(key, (1 - gamma) * value2);
        	}
        }
        for (T key : vector1.keySet()) {
        	if (middleVector.containsKey(key) == false) {
        		double value1 = vector1.get(key);
        		middleVector.put(key, gamma * value1);
        	}
        }

        
        double kld1 = KLDivergence(vector1, middleVector);
        result = (Double.MIN_VALUE + kld1);

        if (result == 0)
        	return 0;
        else
        	return 1/result;
    }
    
    public double JensenShannon(HashMap<T, Double> vector1, HashMap<T, Double> vector2)
    {
        double result = 0.0;

//        //combine two vectors and get a middle
        HashMap<T, Double> middleVector = new HashMap<T, Double>();
        
        double sumV1 = 0;
        for (T key : vector1.keySet()) {
        	sumV1 += vector1.get(key);
        }
        for (T key : vector1.keySet()) {
        	vector1.put(key, vector1.get(key)/sumV1);
        }
        
        double sumV2 = 0;
        for (T key : vector2.keySet()) {
        	sumV2 += vector2.get(key);
        }
        for (T key : vector2.keySet()) {
        	vector2.put(key, vector2.get(key)/sumV2);
        }

        for (T key : vector2.keySet())
        {
        	double value2 = vector2.get(key);
        	if (vector1.containsKey(key) == true) {
            	double value1 = vector1.get(key);
            	middleVector.put(key, (value1 + value2) / 2);
        	}
        	else {
        		middleVector.put(key, value2 / 2);
        	}
        }
        for (T key : vector1.keySet()) {
        	if (middleVector.containsKey(key) == false) {
        		double value1 = vector1.get(key);
        		middleVector.put(key, value1 / 2);
        	}
        }

        
//        result = (Double.MIN_VALUE + (KLDivergence(vector1, vector2)) / 2);
//        result = (Double.MIN_VALUE + (KLDivergence(vector2, vector1)) / 2);

//      result = (Double.MIN_VALUE + (KLDivergence(vector1, middleVector)) / 2);
//      result = (Double.MIN_VALUE + (KLDivergence(vector2, middleVector)) / 2);
        
        double kld1 = KLDivergence(vector1, middleVector);
        double kld2 = KLDivergence(vector2, middleVector);
        result = (Double.MIN_VALUE + (kld1 + kld2) / 2);

        if (result == 0)
        	return 0;
        else
        	return 1/result;
    }

    private double KLDivergence(HashMap<T, Double> vector1, HashMap<T, Double> vector2)
    {
        double result = 0.0;
        if (vector1.size() == 0 || vector2.size() == 0)
            return 0;
        double tempValue = 0.0; //save p(i)*log(p(i)/q(i))
        for (T key : vector2.keySet()) //traverse the longer vector
        {
        	double value2 = vector2.get(key);
            if (vector1.containsKey(key) == true) //find key in another vector
            {
            	double value1 = vector1.get(key);
                tempValue = value1 * Math.log(value1 / value2) / Math.log(2);
                
				String newKey = key + "";
				if (pageTitleIDMap.containsKey(key)) {
					newKey = pageTitleIDMap.get(key);
				}
				
                result += tempValue;
            }
        }

        return result;
    }
	
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    
//	public static HashSet<String> staticGetLegitimatePageIDs () {
//		
//		String intputIndex = "/shared/shelley/yqsong/data/wikipedia_new/WikiLuceneIndex_word500_link30";
//
//		try {
//			System.out.println("Register to lucene...");
//			fsdir = FSDirectory.open(new File(intputIndex));
//			reader = IndexReader.open(fsdir,true);
//
//	    } catch (Exception ex) {
//	    	System.out.println("Cannot create index..." + ex.getMessage());
//	    	System.exit(-1);
//	    }
//		
//		HashSet<String> pageIDs = new HashSet<String>();
//		int maxid = reader.maxDoc();
//	    
//	    Calendar cal = Calendar.getInstance();
//	    long startTime = cal.getTimeInMillis();
//	    
//	    for(int i = 0; i < maxid; i++){
//	    	if (i % 100000 == 0) {
//	    		System.out.print("Processed page: " + i + " out of " + maxid + " documents..");
//	    		
//	    		Calendar cal1 = Calendar.getInstance();
//	    		long endTime = cal1.getTimeInMillis();
//	    		long second = (endTime - startTime)/1000;
//	    		System.out.println("  Elipsed time: " + second + " seconds");
//	    	}
//
//    		try {
//		    	if(!reader.isDeleted(i)){
//		    		String wikiTitle = reader.document(i).getField("title").stringValue();
//		    		int wikiID = Integer.valueOf(reader.document(i).getField("id").stringValue());
//		    		pageIDs.add(wikiID + "");
//	    		}
//    		}
//    		catch(Exception e){
//    			e.printStackTrace();
//    			continue;
//    		}
//    		
//	    }
//	    
//	    return pageIDs;
//	    
//	}
//    
//	public static void cachePageCateNum (String categoryInformationPath) {
//		HashMap<String, Integer> pageCateNumMap = new HashMap<String, Integer>();
//		HashSet<String> pageIDs = staticGetLegitimatePageIDs ();
//		try {
//			String intputIndex = "/shared/shelley/yqsong/data/wikipedia_new/WikiLuceneIndex_word500_link30";
//
//			try {
//				System.out.println("Register to lucene...");
//				fsdir = FSDirectory.open(new File(intputIndex));
//				reader = IndexReader.open(fsdir,true);
//
//		    } catch (Exception ex) {
//		    	System.out.println("Cannot create index..." + ex.getMessage());
//		    	System.exit(-1);
//		    }
//			
//			FileWriter writer = new FileWriter ("/shared/shelley/yqsong/data/wikipedia/pageCateNum.txt");
//			 
//			FileReader reader = new FileReader(categoryInformationPath);
//			BufferedReader br = new BufferedReader(reader);
//			
//			String line = "";
//			int count = 0;
//			while ((line = br.readLine()) != null) {
//				
//				if (count % 1000000 == 0) {
//					System.out.println("Processed category " + count + " lines...");
//				}
//				count++;
//				
//				String[] tokens = line.split("\t");
//				
//				String category = tokens[0];
//				String pageID = tokens[1];
//				if (pageIDs.contains(pageID)) {
//					
//					if (pageCateNumMap.containsKey(pageID) == false) {
//						pageCateNumMap.put(pageID, 1);
//					} else {
//						pageCateNumMap.put(pageID, pageCateNumMap.get(pageID) + 1);
//					}
//					
//				}
//			}
//			br.close();
//			reader.close();
//			
//			for (String key : pageCateNumMap.keySet()) {
//		    	writer.write(key + "\t" + pageCateNumMap.get(key));
//		    	writer.write(systemNewLine);
//		    }
//		    writer.flush();
//		    writer.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println("Finished reading");
//	}
//	
//	
//	public static void cachePageInlinkNum (String inlinkInformationPath) {
//		
//		try {
//			String intputIndex = "/shared/shelley/yqsong/data/wikipedia_new/WikiLuceneIndex_word500_link30";
//
//			try {
//				System.out.println("Register to lucene...");
//				fsdir = FSDirectory.open(new File(intputIndex));
//				reader = IndexReader.open(fsdir,true);
//
//		    } catch (Exception ex) {
//		    	System.out.println("Cannot create index..." + ex.getMessage());
//		    	System.exit(-1);
//		    }
//			
//			HashMap<String, Integer> pageInlinkNumMap = new HashMap<String, Integer>();
//			
//			HashSet<String> pageIDs = staticGetLegitimatePageIDs ();
//			
//		    FileWriter writer = new FileWriter ("/shared/shelley/yqsong/data/wikipedia/pageInlinkNum.txt");
//
//			FileReader reader = new FileReader(inlinkInformationPath);
//			BufferedReader br = new BufferedReader(reader);
//			
//			String line = "";
//			int count = 0;
//			while ((line = br.readLine()) != null) {
//				
//				if (count % 1000000 == 0) {
//					System.out.println("Processed inlinks " + count + " lines...");
//				}
//				count++;
//				
//				String[] tokens = line.split("\t");
//				
//				String pageID = tokens[0];
//				String inlinkID = tokens[1];
//				if (pageIDs.contains(pageID)) {
//					
//					if (pageInlinkNumMap.containsKey(pageID) == false) {
//						pageInlinkNumMap.put(pageID, 1);
//					} else {
//						pageInlinkNumMap.put(pageID, pageInlinkNumMap.get(pageID) + 1);
//					}
//					
//				}
//			}
//			br.close();
//			reader.close();
//			
//		    for (String key : pageInlinkNumMap.keySet()) {
//		    	writer.write(key + "\t" + pageInlinkNumMap.get(key));
//		    	writer.write(systemNewLine);
//		    }
//		    writer.flush();
//		    writer.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println("Finished reading");
//		
//
//	}
//    
//    
//	public static void cachePageLength () throws Exception {
//		String intputIndex = "/shared/shelley/yqsong/data/wikipedia_new/WikiLuceneIndex_word500_link30";
//
//		try {
//			System.out.println("Register to lucene...");
//			fsdir = FSDirectory.open(new File(intputIndex));
//			reader = IndexReader.open(fsdir,true);
//
//	    } catch (Exception ex) {
//	    	System.out.println("Cannot create index..." + ex.getMessage());
//	    	System.exit(-1);
//	    }
//		
//    	int maxid = reader.maxDoc();
//	    
//	    Calendar cal = Calendar.getInstance();
//	    long startTime = cal.getTimeInMillis();
//	    
//	    FileWriter writer = new FileWriter ("/shared/shelley/yqsong/data/wikipedia/pageLength.txt");
//	    HashSet<String> pageIDs = new HashSet<String>();
//		HashMap<String, Integer> pageLengthMap = new HashMap<String, Integer>();
//	    for(int i = 0; i < maxid; i++){
//	    	if (i % 100000 == 0) {
//	    		System.out.print("Processed page lenght: " + i + " out of " + maxid + " documents..");
//	    		
//	    		Calendar cal1 = Calendar.getInstance();
//	    		long endTime = cal1.getTimeInMillis();
//	    		long second = (endTime - startTime)/1000;
//	    		System.out.println("  Elipsed time: " + second + " seconds");
//	    	}
//
//    		try {
//		    	if(!reader.isDeleted(i)){
//		    		String wikiTitle = reader.document(i).getField("title").stringValue();
//		    		int wikiID = Integer.valueOf(reader.document(i).getField("id").stringValue());
//		    		pageIDs.add(wikiID + "");
//		    		
//		    		TermFreqVector tv = reader.getTermFreqVector(i, "contents");
//	    			String[] terms = tv.getTerms();
//	    			int[] fq = tv.getTermFrequencies();
//	    			int totalNum = 0;
//	    			for (int j = 0; j < fq.length; ++j) {
//	    				totalNum += fq[j];
//	    			}
//	    			
//	    			pageLengthMap.put(wikiID + "", totalNum);
//
//	    		}
//    		}
//    		catch(Exception e){
//    			e.printStackTrace();
//    			continue;
//    		}
//    		
//	    }
//	    
//	    for (String key : pageLengthMap.keySet()) {
//	    	writer.write(key + "\t" + pageLengthMap.get(key));
//	    	writer.write(systemNewLine);
//	    }
//	    writer.flush();
//	    writer.close();
//	}
//	
//	public static void cachePageTitleMap () throws Exception {
//		String intputIndex = "/shared/shelley/yqsong/data/wikipedia_new/WikiLuceneIndex_word500_link30";
//
//		try {
//			System.out.println("Register to lucene...");
//			fsdir = FSDirectory.open(new File(intputIndex));
//			reader = IndexReader.open(fsdir,true);
//
//	    } catch (Exception ex) {
//	    	System.out.println("Cannot create index..." + ex.getMessage());
//	    	System.exit(-1);
//	    }
//		
//    	int maxid = reader.maxDoc();
//	    
//	    Calendar cal = Calendar.getInstance();
//	    long startTime = cal.getTimeInMillis();
//	    
//	    FileWriter writer = new FileWriter ("/shared/shelley/yqsong/data/wikipedia/pageTitleMap.txt");
//	    for(int i = 0; i < maxid; i++){
//	    	if (i % 100000 == 0) {
//	    		System.out.print("Processed page lenght: " + i + " out of " + maxid + " documents..");
//	    		
//	    		Calendar cal1 = Calendar.getInstance();
//	    		long endTime = cal1.getTimeInMillis();
//	    		long second = (endTime - startTime)/1000;
//	    		System.out.println("  Elipsed time: " + second + " seconds");
//	    	}
//
//    		try {
//		    	if(!reader.isDeleted(i)){
//		    		String wikiTitle = reader.document(i).getField("title").stringValue();
//		    		int wikiID = Integer.valueOf(reader.document(i).getField("id").stringValue());
//		    		wikiTitle = wikiTitle.replaceAll(",", "").replaceAll(";", "").replaceAll("\t", "");
//		    		writer.write(wikiID + "\t" + wikiTitle);
//			    	writer.write(systemNewLine);
//
//	    		}
//    		}
//    		catch(Exception e){
//    			e.printStackTrace();
//    			continue;
//    		}
//    		
//	    }
//
//	    writer.flush();
//	    writer.close();
//	}
}
