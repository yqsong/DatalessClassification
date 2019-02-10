package edu.illinois.cs.cogcomp.classification.representation.esa.simple;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;

public class DatalessServer {
	
	private static final String NAME = DatalessServer.class.getCanonicalName();
	private static final String PUBLIC_NAME = DatalessServer.class.getSimpleName();
	
	static SimpleESALocal esa = new SimpleESALocal();
	
	public static void main(String args[]) throws Exception{
		ServerSocket serverSocket = new ServerSocket(1235);
		while(true){
			System.out.println("Dataless Server Running.....");
			Socket connectionSocket = serverSocket.accept();
			System.out.println("Server Accepted Connection.....");
			ObjectInputStream objInpStream = new ObjectInputStream(connectionSocket.getInputStream());
			ObjectOutputStream objOutStream = new ObjectOutputStream(connectionSocket.getOutputStream());
			while(true){
				Object inObj ;
				try{
					inObj = objInpStream.readObject();
				} catch (EOFException e){
					System.out.println("Server client communication ended. Restarting server.");
					break;
				}
				Object outObj = processClientRequest(inObj, Integer.parseInt(args[0]));
				objOutStream.writeObject(outObj);
				Runtime runtime = Runtime.getRuntime();
				int mb = 1024*1024;
				System.out.println("Used Memory - " + (runtime.totalMemory() - runtime.freeMemory()) / mb);
				System.out.println("Free Memory - " + runtime.freeMemory() / mb);
				System.out.println("Total Memory - " + runtime.totalMemory() / mb);
				System.out.println("Max Memory - " + runtime.maxMemory() / mb);
			}
		}
	}
	
	private static Object processClientRequest(Object inObj, int conceptsNumber) throws Exception{
		String inpString = (String) inObj;
		List <ConceptData> concepts;
		try {
			concepts = esa.getConcepts(conceptsNumber, inpString);
		} catch (Exception e) {
			System.out.println("Error: " + NAME + ": SimpleESALocal coudn't get concepts. SimpleESALocal.getConcepts failed.");
			e.printStackTrace();
			throw e;
		}
		
		TreeMap<String, Double> formattedConcepts = new TreeMap<String, Double>();
		for(ConceptData concept : concepts){
			formattedConcepts.put(concept.concept, concept.score);
		}
		
		return (Object) formattedConcepts;
	}
}

