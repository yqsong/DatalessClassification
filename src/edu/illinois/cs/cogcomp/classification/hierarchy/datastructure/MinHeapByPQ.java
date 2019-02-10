package edu.illinois.cs.cogcomp.classification.hierarchy.datastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * yqsong@illinois.edu
 */

public class MinHeapByPQ {

	PriorityQueue<LabelKeyValuePair> pqHeap = new PriorityQueue<LabelKeyValuePair> ();
	
	int heapSize;
	
	public void initializeQueue (List<LabelKeyValuePair> kvpList) {
		heapSize = kvpList.size();
    	for (int i = 0; i < heapSize; ++i) {
    		pqHeap.add(kvpList.get(i));
    	}
	}
	
	public void add (LabelKeyValuePair kvp) {
		if (kvp.getScore() > pqHeap.peek().getScore()) {
			pqHeap.remove();
			pqHeap.add(kvp);
		}
	}

	// from large to small
	public List<LabelKeyValuePair> sort () {
    	List<LabelKeyValuePair> kvpList = new ArrayList<LabelKeyValuePair>();
    	while (pqHeap.size() > 0) {
    		kvpList.add(pqHeap.remove());
    	}
    	Collections.reverse(kvpList);
    	return kvpList;
	}
}
