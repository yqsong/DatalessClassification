/**
 * 
 */
package edu.illinois.cs.cogcomp.descartes;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.descartes.retrieval.ISearcher;
import edu.illinois.cs.cogcomp.descartes.retrieval.SearcherFactory;

/**
 * @author Vivek Srikumar
 * 
 */
public class DatalessExperiment {

	private final String class2File;
	private final String class1File;
	private final int nThreads;
	private final String class2Proto;
	private final String class1Proto;
	private final DatalessClassifier classifier;
	private double start;

	public DatalessExperiment(String indexDir, String class1Proto,
			String class1File, String class2Proto, String class2File,
			int nThreads) throws Exception {
		this.class1Proto = class1Proto;
		this.class1File = class1File;
		this.class2Proto = class2Proto;
		this.class2File = class2File;
		this.nThreads = nThreads;

		ISearcher searcher = SearcherFactory.getStandardSearcher(indexDir);
		classifier = new DatalessClassifier(searcher, 1000, Arrays.asList(
				class1Proto, class2Proto));

	}

	public double run(long timeoutSeconds) throws FileNotFoundException,
			InterruptedException, ExecutionException {
		ExecutorService pool = Executors.newFixedThreadPool(nThreads);

		List<FutureTask<Boolean>> tasks = new ArrayList<FutureTask<Boolean>>();

		start = System.currentTimeMillis();

		int i = 0;
		for (String line : LineIO.read(class1File)) {

			FutureTask<Boolean> task = makeTask(i++, class1Proto, line);

			pool.execute(task);

			tasks.add(task);
		}

		for (String line : LineIO.read(class2File)) {

			FutureTask<Boolean> task = makeTask(i++, class2Proto, line);

			pool.execute(task);

			tasks.add(task);
		}

		pool.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);

		pool.shutdown();

		double correct = 0;
		double total = 0;
		for (FutureTask<Boolean> task : tasks) {
			if (task.get())
				correct++;
			total++;
		}

		return correct / total;
	}

	/**
	 * @param line
	 * @return
	 */
	private FutureTask<Boolean> makeTask(final int id, final String trueLabel,
			final String line) {
		return new FutureTask<Boolean>(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {

				String classLabel = classifier.getLabel(line.replaceAll(
						"[^a-zA-Z0-9 ]", ""));

				if (id % 100 == 0) {
					long end = System.currentTimeMillis();
					System.out.println(id + " examples done. Took "
							+ (end - start) / 1000 + "s.");
				}

				return trueLabel.equals(classLabel);
			}
		});
	}
}
