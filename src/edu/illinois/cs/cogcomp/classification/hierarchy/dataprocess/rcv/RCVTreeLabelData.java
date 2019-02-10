package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.rcv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;

import edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.abstracts.AbstractTreeLabelData;

/**
 * yqsong@illinois.edu
 */

public class RCVTreeLabelData extends AbstractTreeLabelData {
	
	private static final long serialVersionUID = 1L;

	public RCVTreeLabelData () {
		super();
	}

	public void readLabels (String fileTopicHierarchyPath, String topicDescriptionPath) {
		readTreeHierarchy(fileTopicHierarchyPath);
		readTopicDescription(topicDescriptionPath);
	}

	@Override
	public void readTreeHierarchy(String fileTopicHierarchyPath) {
		try {
			System.out.println("read tree from file...");
			BufferedReader reader = new BufferedReader(new FileReader(fileTopicHierarchyPath));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split("\\s+");
				String parent = tokens[1].trim().toLowerCase();
				String child = tokens[3].trim().toLowerCase();
				int index = line.indexOf("child-description:");
				String[] subtokens = line.substring(index).trim().toLowerCase().split(":");
				String childName = subtokens[1].trim().replace("/", " ");
				if (treeIndex.containsKey(parent) == true) {
					if (treeIndex.get(parent).contains(child) == false)
						treeIndex.get(parent).add(child);
				} else {
					treeIndex.put(parent, new HashSet<String>());
					treeIndex.get(parent).add(child);
				}
				if (parentIndex.containsKey(child) == false) {
					parentIndex.put(child, parent);
				}
				if (treeLabelNameHashMap.containsKey(child) == false) {
					treeLabelNameHashMap.put(child, childName);
				}
			}
			reader.close();
			System.out.println("read tree finished");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void readTopicDescription(String topicDescriptionPath) {
		try {
	    	FileReader reader = new FileReader(topicDescriptionPath);
	    	BufferedReader bf = new BufferedReader(reader);
	    	String line = "";
	    	while ((line = bf.readLine()) != null) {
	    		String[] splitArray = line.trim().split("\\s+");
	    		String topicID = splitArray[0].trim().toLowerCase();
	    		String topicName = "";
	    		
	    		if (splitArray.length > 0) {
	    			for (int i = 1; i < splitArray.length; ++i) {
	    				topicName += " " + splitArray[i].trim().toLowerCase();
	    			}
	    		}
	    		
	    		if (topicDescriptionHashMap.containsKey(topicID) == false) {
	    			topicDescriptionHashMap.put(topicID, topicName);
	    		} 
	    	}
	    	bf.close();
	    	reader.close();
	    	
	      } catch (Exception e ) 
	      {
	    	e.printStackTrace();
	      };
	}
}
