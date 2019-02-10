package edu.illinois.cs.cogcomp.descartes.indexer;

import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import info.bliki.wiki.dump.WikiXMLParser;
import info.bliki.wiki.model.WikiModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import edu.illinois.cs.cogcomp.descartes.util.PageParser;
import edu.illinois.cs.cogcomp.wiki.parsing.Utils;
import edu.illinois.cs.cogcomp.wiki.parsing.processors.LinkAnnotationConverter;

abstract class DemoArticleFilter implements IArticleFilter {

	private final ThreadPoolExecutor parsing;

	public DemoArticleFilter(int threadCount) {
		parsing = Utils.getBoundedThreadPool(threadCount);
	}

	@Override
	public void process(WikiArticle page, Siteinfo siteinfo)
			throws SAXException {
		// System.out.println(page.getTitle());
		// System.out.println(page.getText());
		if (page.isMain() && !StringUtils.isEmpty(page.getText())
				&& !Utils.isSpecialTitle(page.getTitle())) {

			parsing.execute(new PageWorker(page.getText()) {
				@Override
				void callback(PageParser pageParser) {
					DemoArticleFilter.this.processAnnotation(pageParser);
				}
			});

		}
	}

	abstract void processAnnotation(PageParser pageParser);

	abstract class PageWorker implements Runnable {

		private String page;

		public PageWorker(String rawtext) {
			this.page = rawtext;
		}

		@Override
		public void run() {
			PageParser pageParser = new PageParser();
			ArrayList<String> content = new ArrayList<>();
			for (String str : page.split("\\n")) {
				content.add(str);
			}
			boolean isValid = pageParser.parse(content, 100, 10);

			if (isValid) {
				System.out.println("Valid");
			}
			callback(pageParser);
		}

		abstract void callback(PageParser pageParser);
	}

	public void finishUp() {
		parsing.shutdown();
		try {
			parsing.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: Parser <XML-FILE>");
			System.exit(-1);
		}
		String bz2Filename = args[0];
		try {
			DemoArticleFilter handler = new DemoArticleFilter(10) {

				@Override
				void processAnnotation(PageParser pageParser) {
				}

			};
			WikiXMLParser wxp = new WikiXMLParser(bz2Filename, handler);
			wxp.parse();
			handler.finishUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}