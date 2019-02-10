package edu.illinois.cs.cogcomp.classification.hierarchy.dataprocess.newsgroups;

import java.util.HashMap;

/**
 * yqsong@illinois.edu
 */

public class NewsgroupsTopicHierarchy {
	
	static public HashMap<String, HashMap<String, String>> topicHierarchy;
	static public HashMap<String, String> topicMapping1 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping2 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping3 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping4 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping5 = new HashMap<String, String>();
	static public HashMap<String, String> topicMapping6 = new HashMap<String, String>();

	static public HashMap<String, String> topicMapping = new HashMap<String, String>();

	public NewsgroupsTopicHierarchy () {
		
		topicMapping1.put("talk.politics.guns", "politics guns");
		topicMapping1.put("talk.politics.mideast", "politics mideast");
		topicMapping1.put("talk.politics.misc", "politics");

		topicMapping2.put("alt.atheism", "atheism");
		topicMapping2.put("soc.religion.christian", "society religion christianity christian");
		topicMapping2.put("talk.religion.misc", "religion");

		topicMapping3.put("comp.sys.ibm.pc.hardware", "computer systems ibm pc hardware");
		topicMapping3.put("comp.sys.mac.hardware", "computer systems mac macintosh apple hardware");
		topicMapping3.put("comp.graphics", "computer graphics");
		topicMapping3.put("comp.windows.x", "computer windows x windowsx");
		topicMapping3.put("comp.os.ms.windows.misc", "computer os operating system microsoft windows");
		
		topicMapping4.put("rec.autos", "cars");
		topicMapping4.put("rec.motorcycles", "motorcycles");
		topicMapping4.put("rec.sport.baseball", "baseball");
		topicMapping4.put("rec.sport.hockey", "hockey");

		topicMapping5.put("sci.electronics", "science electronics");
		topicMapping5.put("sci.crypt", "science cryptography");
		topicMapping5.put("sci.med", "science medicine");
		topicMapping5.put("sci.space", "science space");
		
		topicMapping6.put("misc.forsale", "for sale discount");
		
		
//		topicMapping1.put("talk.politics.guns", "gun fbi guns weapon compound ");
//		topicMapping1.put("talk.politics.mideast", "israel arab jews jewish muslim ");
//		topicMapping1.put("talk.politics.misc", "gay homosexual sexual ");
//
//		topicMapping2.put("alt.atheism", "atheist christian atheism god islamic ");
//		topicMapping2.put("soc.religion.christian", "christian god christ church bible jesus ");
//		topicMapping2.put("talk.religion.misc", "christian morality jesus god religion horus ");
//
//		topicMapping3.put("comp.sys.ibm.pc.hardware", "bus pc motherboard bios board computer dos ");
//		topicMapping3.put("comp.sys.mac.hardware", "mac apple powerbook ");
//		topicMapping3.put("comp.graphics", "graphics image gif animation tiff ");
//		topicMapping3.put("comp.windows.x", "window motif xterm sun windows ");
//		topicMapping3.put("comp.os.ms.windows.misc", "windows dos microsoft ms driver drivers card printer ");
//		
//		topicMapping4.put("rec.autos", "car ford auto toyota honda nissan bmw ");
//		topicMapping4.put("rec.motorcycles", "bike motorcycle yamaha ");
//		topicMapping4.put("rec.sport.baseball", "baseball ball hitter ");
//		topicMapping4.put("rec.sport.hockey", "hockey wings espn ");
//
//		topicMapping5.put("sci.electronics", "circuit electronics radio signal battery ");
//		topicMapping5.put("sci.crypt", "encryption key crypto algorithm security ");
//		topicMapping5.put("sci.med", "doctor medical disease medicine patient ");
//		topicMapping5.put("sci.space", "space orbit moon earth sky solar ");
//		
//		topicMapping6.put("misc.forsale", "sale offer shipping  forsale sell price brand obo ");

		topicMapping.put("talk.politics.guns", "politics guns");
		topicMapping.put("talk.politics.mideast", "politics mideast");
		topicMapping.put("talk.politics.misc", "politics");

		topicMapping.put("alt.atheism", "atheism");
		topicMapping.put("soc.religion.christian", "society religion christianity christian");
		topicMapping.put("talk.religion.misc", "religion");

		topicMapping.put("comp.sys.ibm.pc.hardware", "computer systems ibm pc hardware");
		topicMapping.put("comp.sys.mac.hardware", "computer systems mac macintosh apple hardware");
		topicMapping.put("comp.graphics", "computer graphics");
		topicMapping.put("comp.windows.x", "computer windows x windowsx");
		topicMapping.put("comp.os.ms.windows.misc", "computer os operating system microsoft windows");
		
		topicMapping.put("rec.autos", "cars");
		topicMapping.put("rec.motorcycles", "motorcycles");
		topicMapping.put("rec.sport.baseball", "baseball");
		topicMapping.put("rec.sport.hockey", "hockey");

		topicMapping.put("sci.electronics", "science electronics");
		topicMapping.put("sci.crypt", "science cryptography");
		topicMapping.put("sci.med", "science medicine");
		topicMapping.put("sci.space", "science space");
		
		topicMapping.put("misc.forsale", "for sale discount");

		topicHierarchy = new HashMap<String, HashMap<String, String>>();
		topicHierarchy.put("politics", topicMapping1);
		topicHierarchy.put("religion", topicMapping2);
		topicHierarchy.put("computer", topicMapping3);
		topicHierarchy.put("autos.sports", topicMapping4);
		topicHierarchy.put("science", topicMapping5);
		topicHierarchy.put("sales", topicMapping6);
	}
	
	public HashMap<String, HashMap<String, String>> getTopicHierarchy () {
		return topicHierarchy;
	}

	

}
