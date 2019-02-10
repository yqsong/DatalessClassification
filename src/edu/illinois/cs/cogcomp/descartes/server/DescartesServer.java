package edu.illinois.cs.cogcomp.descartes.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.descartes.retrieval.IResult;
import edu.illinois.cs.cogcomp.descartes.retrieval.ISearcher;
import edu.illinois.cs.cogcomp.descartes.retrieval.SearcherFactory;

public class DescartesServer {

	private static Logger log = LoggerFactory.getLogger(DescartesServer.class);

	private static String indexDirectory;
	private static ISearcher searcher;

	public DescartesServer() throws Exception {

		synchronized (indexDirectory) {
			if (searcher == null) {
				searcher = SearcherFactory.getStandardSearcher(indexDirectory);
			}
		}
	}

	public String[] esa(String text, int numResults) throws Exception {

		log.info("Getting {} ESA results for text: {}",
				Integer.valueOf(numResults), text);

		ArrayList<IResult> list = searcher.search(text, numResults);

		List<String> out = new ArrayList<String>();
		for (IResult res : list) {
			out.add(res.getTitle());
		}

		return out.toArray(new String[out.size()]);
	}

	public double similarity(String text1, String text2, int numESAResults)
			throws Exception {

		log.info("Getting similarity using {} concepts. text1={}, text2={}",
				new Object[] { Integer.valueOf(numESAResults), text1, text2 });

		Set<String> s1 = getConcepts(text1, numESAResults);
		Set<String> s2 = getConcepts(text2, numESAResults);

		s2.retainAll(s1);

		return s2.size();

	}

	private Set<String> getConcepts(String text1, int numESAResults)
			throws Exception {
		final ArrayList<IResult> list = searcher.search(text1, numESAResults);

		final Set<String> out = new HashSet<String>();
		for (final IResult res : list) {
			out.add(res.getTitle());
		}
		return out;
	}

	public static void server(String indexDirectory, int port)
			throws IOException, XmlRpcException {

		log.info(
				"Starting descartes server at port {}, with index directory {}",
				Integer.valueOf(port), indexDirectory);

		DescartesServer.indexDirectory = indexDirectory;
		WebServer webServer = new WebServer(port);

		XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

		PropertyHandlerMapping phm = new PropertyHandlerMapping();

		phm.addHandler("DescartesServer", DescartesServer.class);

		xmlRpcServer.setHandlerMapping(phm);

		XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer
				.getConfig();
		serverConfig.setEnabledForExtensions(true);
		serverConfig.setContentLengthOptional(false);

		webServer.start();
		log.info("Descartes is ready!");

	}

}
