/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @(#)file LoadGenerator.java
 * @(#)author Michele Mazzucco
 * @(#)version 0.1
 * @(#)created Mar 29, 2012
 * @(#)lastedit Mar 29, 2012
 */

package http;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import stats.Counter;
import stats.Deviate;

/*
 * Copyright (C) 2013 Michele Mazzucco
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Generates load according to a distribution whose parameters are passed
 * as parameters.
 */
public class LoadGenerator {

	private static final int SIZE = 100000; // limits the number of URLs

	private final Thread mainThread;

	private final Crawler crawler;

	protected final Logger log;

	protected long requests;

	private long start;
	
	/** Guard, main loop. */
	protected volatile boolean go;


	/**
	 * Constructor, using the specified log. If null, uses an annonymous
	 * logger.
	 * 
	 * @throws IOException If fails to create the selector.
	 */
	public LoadGenerator(Logger log) throws IOException {
		mainThread = Thread.currentThread();
		go = true;

		this.log = log;
		crawler = new Crawler(log);

		requests = 0L;
	}

	/**
	 * Takes the timestamp used to estimate a number of metrics.
	 */
	protected final void start() {
		this.start = System.currentTimeMillis();
	}

	/**
	 * For loop.
	 * 
	 * @param domain The domain to test. Should include HTTP!
	 * @param urls
	 * @param mean
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	private void benchmark(String domain, String[] urls, double mean, double ca2)
			throws MalformedURLException, URISyntaxException {

		Runtime.getRuntime().addShutdownHook(new Hook()); // add shutdown hook

		final Deviate deviate = new Deviate(mean, ca2);
		log.log(Level.INFO, "Starting while loop");

		final long minute = 60 * 1000L;
		long requestsMinute = 0L;
		final double minSec = 60.0;

		start();
		int counter = 0;
		long nextLogAt = System.currentTimeMillis() + minute;
		while (this.go) {
			long now = System.currentTimeMillis();
			if (now > nextLogAt) {
				String msg = String.format("Arr. rate minute %.3f",
						(requestsMinute / minSec));
				log.info(msg);
				nextLogAt = now + minute;
				requestsMinute = 0L;
			}

			// XXX: access all the URLs in sequence. See Clarkent for an
			// example using random URLs
			String myURL = domain + urls[counter];			
			
			URL url = new URL(myURL);
			URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(),
					url.getQuery(), null);
			crawler.download(uri, null);
			this.requests++;
			requestsMinute++;

			long sleep = (long) deviate.generateDeviate();
			final long nextRequestAt = now + sleep;
			do {
				sleep = nextRequestAt - now; // no change at 1st iteration
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					Thread.interrupted(); // clear interrupted status
				}
				now = System.currentTimeMillis();
			} while (nextRequestAt > now);

			counter++;
			if (counter == urls.length)
				counter = 0;
		}

	}

	/**
	 * Downloads the specified URI with the specified listener attached to it
	 * (the latter can be null).
	 */
	public void download(URI uri, Listener l) {
		this.crawler.download(uri, l);
	}

	
	/**
	 * Stops the crawler by calling {@link Crawler#release()}.
	 */
	protected final void stopCrawler() {
		this.crawler.release();
	}

	
	/**
	 * Code executed by the shutdown hook
	 */
	protected void shutdown() {
		this.go = false;
		stopCrawler();
		this.mainThread.interrupt();

		final long responses = crawler.stream.getCounter();
		final int errors = crawler.stream.getErrors();
		log.log(Level.INFO, "========================");
		String msg = String
				.format("Requests %d, responses %d, errors %d, connections %d, writes %d\n",
						requests, responses, errors,
						crawler.connections, crawler.writes);
		log.log(Level.INFO, msg);

		double interval = (System.currentTimeMillis() - start) / 1000.0;

		msg = String.format("Arr rate. %.3f jobs/sec\n", (requests / interval));
		log.log(Level.INFO, msg);
		msg = String.format("Throughput %.3f jobs/sec\n",
				(responses / interval));
		log.log(Level.INFO, msg);
		msg = String.format("Throughput %.3f KB/sec\n",
				(crawler.stream.getDownloadedBytes() / (interval * 1024)));
		log.log(Level.INFO, msg);
		

		log.log(Level.INFO, "========================");
		Iterator<Entry<Integer, Counter>> it = crawler.stream.getStatusCodes();
		while (it.hasNext()) {
			Entry<Integer, Counter> next = it.next();
			msg = String.format("HTTP code %d: %d\n", next.getKey(), next
					.getValue().getCounter());
			log.log(Level.INFO, msg);
		}
		log.log(Level.INFO, "========================");
	}

	/**
	 * Loads the URLs from the specified file.
	 * 
	 * @param path The file containing the URLs, one per line.
	 * @return The list of URLs
	 * @throws IOException If an error occurs.
	 */
	public static String[] loadUrls(String path) throws IOException {
		List<String> urls = new LinkedList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line;
		while ((line = reader.readLine()) != null) {
			urls.add(line);
		}
		reader.close();
		return urls.toArray(new String[urls.size()]);
	}

	/**
	 * java -cp . http.LoadGenerator 10 http/high2.load http://wikipedia.org
	 * 
	 * @param args Arrival rate (single value), path to the file containing the
	 *            URLS, domain, and squared coefficient of variation of
	 *            interarrival intervals (optional).
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws IOException,
			URISyntaxException {
		Logger log = Logger.getAnonymousLogger();
		log.setUseParentHandlers(false);
		log.setLevel(Level.INFO);

		MillisecondLogFormatter formatter = new MillisecondLogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(formatter);
		handler.setLevel(Level.INFO);

		FileHandler fileHanlder = new FileHandler("load_generator.log");
		fileHanlder.setFormatter(formatter);
		fileHanlder.setLevel(Level.FINE);

		log.addHandler(handler);
		log.addHandler(fileHanlder);

		System.out
				.println("java -cp . http.LoadGenerator <lambda> <file> <domain> <[ca2=1]>");
		if (args.length < 3) {
			System.exit(1);
		}

		final double lambda = Double.parseDouble(args[0]);
		final String file = args[1];
		String domain = args[2];
		if (!domain.startsWith("http://")) {
			domain = "http://" + domain;
			log.warning("Domain fixed to " + domain);
		}

		double ca2 = 1.0;
		if (args.length > 3) {
			ca2 = Double.parseDouble(args[3]);
		}

		System.out.printf("- Lambda %.2f\n", lambda);
		System.out.printf("- Traces %s\n", file);
		System.out.printf("- Domain %s\n", domain);
		System.out.printf("- ca2 %.2f\n", ca2);

		String[] urls = loadUrls(file);

		// initialize everything here, so the CPU will not be wasted
		// afterwards to generate random numbers.

		SecureRandom random = new SecureRandom();

		String[] randomUrls = new String[SIZE];
		final int urlSize = urls.length;

		for (int i = 0; i < SIZE; i++) {
			randomUrls[i] = urls[random.nextInt(urlSize)];
		}
		LoadGenerator lg = new LoadGenerator(log);
		final double mean = 1000.0 / lambda; // ms
		lg.benchmark(domain, randomUrls, mean, ca2);
	}

	protected class Hook extends Thread {

		/**
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			shutdown();
		}

	}

}
