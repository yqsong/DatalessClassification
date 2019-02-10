package edu.illinois.cs.cogcomp.classification.densification.representation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class WikiMinHeapByPQ {

	PriorityQueue<WikiPageKeyValuePair> pqHeap = new PriorityQueue<WikiPageKeyValuePair> ();
	
	int heapSize;
	
	public void initializeQueue (List<WikiPageKeyValuePair> kvpList) {
		heapSize = kvpList.size();
    	for (int i = 0; i < heapSize; ++i) {
    		pqHeap.add(kvpList.get(i));
    	}
	}
	
	public void add (WikiPageKeyValuePair kvp) {
		if (kvp.score > pqHeap.peek().score) {
			pqHeap.remove();
			pqHeap.add(kvp);
		}
	}

	// from large to small
	public List<WikiPageKeyValuePair> sort () {
    	List<WikiPageKeyValuePair> kvpList = new ArrayList<WikiPageKeyValuePair>();
    	while (pqHeap.size() > 0) {
    		kvpList.add(pqHeap.remove());
    	}
    	Collections.reverse(kvpList);
    	return kvpList;
	}
}
