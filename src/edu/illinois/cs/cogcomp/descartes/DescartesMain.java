package edu.illinois.cs.cogcomp.descartes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.core.utilities.commands.CommandDescription;
import edu.illinois.cs.cogcomp.core.utilities.commands.CommandIgnore;
import edu.illinois.cs.cogcomp.core.utilities.commands.InteractiveShell;
import edu.illinois.cs.cogcomp.descartes.indexer.WikiDocIndexer;
import edu.illinois.cs.cogcomp.descartes.retrieval.IResult;
import edu.illinois.cs.cogcomp.descartes.retrieval.ISearcher;
import edu.illinois.cs.cogcomp.descartes.retrieval.SearcherFactory;
import edu.illinois.cs.cogcomp.descartes.server.DescartesServer;

public class DescartesMain {

	private static Logger log = LoggerFactory.getLogger(DescartesMain.class);

	@CommandDescription(description = "index wiki-file index-dir configFile\n"
			+ "This creates the index using the preprocessed Wikipedia data.")
	public static void index(String wikiFile, String indexDir, String configFile)
			throws Exception {
		WikiDocIndexer indexer = new WikiDocIndexer(wikiFile, indexDir,
				configFile, AnalyzerFactory.defaultAnalyzerName);
		indexer.index();
	}

	@CommandDescription(description = "esa index-dir num-results\n"
			+ "This command launches an interactive shell which \n"
			+ "prompts for input text and extracts the ESA using \n"
			+ "the Wikipedia index at index-dir.")
	public static void esa(String indexDir, String _numResults)
			throws Exception {
		ISearcher searcher = SearcherFactory.getStandardSearcher(indexDir);

		int numResults = Integer.parseInt(_numResults);
		boolean quit = false;
		do {
			System.out.print("Input text (_ to quit): ");
			String line = System.console().readLine();

			if (line.trim().equals("_")) {
				quit = true;
			} else {
				line = line.replaceAll("[^a-zA-Z0-9 ]", "");
				String query = line.trim();
				System.out.println(query);

				ArrayList<IResult> list = searcher.search(query, numResults);
				System.out.println();
				for (int i = 0; i < list.size(); i++) {
					System.out.println((i + 1) + ". " + list.get(i).getTitle()
							+ " [" + list.get(i).getScore() + "]");
				}
				System.out.println();
			}

		} while (!quit);
	}

	@CommandDescription(description = "dataless indexDir 20NGBowDir\n"
			+ "Replicates the results of the dataless classification for sci.crypt "
			+ "vs. sci.electronics. "
			+ "This function exists to make sure the code works.")
	public static void dataless(String indexDir, String twentyNGDataDir)
			throws Exception {
		// ISearcher searcher = SearcherFactory.getStandardSearcher(indexDir);

		String crypt = "science cryptography";
		String electronics = "science electronics";

		int nThreads = Math.min(8, Runtime.getRuntime().availableProcessors());

		DatalessExperiment experiment = new DatalessExperiment(indexDir, crypt,
				twentyNGDataDir + File.separatorChar + "sci.crypt.BOW",
				electronics, twentyNGDataDir + File.separatorChar
						+ "sci.electronics.BOW", nThreads);

		double accuracy = experiment.run(1000);

		System.out.println("Accuracy = " + accuracy);
	}

	@CommandDescription(description = "startServer indexDir port\n"
			+ "Starts an XML-RPC server at the specified port. The "
			+ "server exposes the following functions of the class "
			+ "DescartesServer:\n\n"
			+ "\t1. List<String> esa(String text, int numResults)\n"
			+ "\t2. double similarity(String text1, String text2, int numESAResults)\n"
			+ "\n\n To get started with the client, create a "
			+ "client that connects to http://server-address:port and call "
			+ "'DescartesServer.esa' or 'DescartesServer.similarity'")
	public static void startServer(String indexDir, String port)
			throws IOException, XmlRpcException {
		DescartesServer.server(indexDir, Integer.parseInt(port));
	}

	@CommandIgnore
	public static void main(String[] args) {
		InteractiveShell<DescartesMain> shell = new InteractiveShell<DescartesMain>(
				DescartesMain.class);

		if (args.length == 0)
			shell.showDocumentation();
		else {
			long start_time = System.currentTimeMillis();
			try {
				shell.runCommand(args);
			} catch (Exception e) {
				e.printStackTrace();
			}
			log.info("Took {} secs.",
					(System.currentTimeMillis() - start_time) / 1000.0);
		}
	}
}
