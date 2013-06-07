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
 * @(#)file Clarknet.java
 * @(#)author Michele Mazzucco
 * @(#)version 0.1
 * @(#)created Apr 12, 2012
 * @(#)lastedit Apr 12, 2012
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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import stats.Deviate;

/**
 * Generates load according to a trace file containing the arrival rates.
 */
public class Clarknet extends LoadGenerator {

    private static final long HOUR = 3600 * 1000; // 1 hour

    private static final float CA2 = 4f;
    
    private static final double LAMBDA_MULTIPLIER = 1.5;

    /**
     * @param log
     * @throws IOException
     */
	public Clarknet(Logger log) throws IOException {
        super(log);
    }

    /**
     * @param domain The domain to test. Should include HTTP!
     * @param urls
     * @param mean
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    private void benchmark(double[] lambdas, String[] urls, String domain)
            throws MalformedURLException, URISyntaxException {

        Runtime.getRuntime().addShutdownHook(new Hook()); // add shutdown hook
        
        final int urlSize = urls.length;
        SecureRandom random = new SecureRandom();

        int index = 0; // index lambdas
        long changeLambdaAt = System.currentTimeMillis() + HOUR;
        log.info("Changing lambda at " + new Date(changeLambdaAt).toString());
        final Deviate deviate = new Deviate(1000.0 / lambdas[index], CA2);
        log.log(Level.INFO, "Set lambda to " + lambdas[index]);
        
        log.log(Level.INFO, "Starting while loop");

        start();
        
        long requestsHour = 0L;
        Pattern p = Pattern.compile("^[^?]*.title=(\\S+)$");
        while (super.go) {
            if (System.currentTimeMillis() > changeLambdaAt) {
                if (index == lambdas.length - 1) {
                    super.go = false;
                    stopCrawler();
                    break;
                }
                index++;
                double newLambda = lambdas[index];
                if (newLambda > 70.0) {
					newLambda *= 0.98;
                }
                deviate.set(1000.0 / newLambda, CA2);
                changeLambdaAt += HOUR;
                
                double arrRateHour = requestsHour / 3600.0;
                String msg = String.format("Arr. rate last hour %.3f", arrRateHour);
                log.info(msg);
                
                log.log(Level.INFO, "Set lambda to " + lambdas[index]);
                
                requestsHour = 0L;
            }
            
            
            // URLs are generated using a uniform distribution
            // In a real setting one might want to use a more heavily tailed
            // distribution, i.e., Pareto
            String tmp = urls[random.nextInt(urlSize)];
            
            
            //String myURL = domain + tmp;
            // june 7, 2013
            // XXX: the following lines are a hack because the URL format
            // of wikipedia has changed
            // If another trace file is employed, the above line
            // can be used instead.
            Matcher m = p.matcher(tmp);
            String myURL = null;
			if (m.matches()) {
				myURL = domain + "/wiki/" + m.group(1);
			} else {
				myURL = domain + tmp;
			}
            
            URL url = new URL(myURL);
            URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(),
                    url.getQuery(), null);
            super.download(uri, null);
            this.requests++;
            requestsHour += 1;

            final long sleep = (long) deviate.generateDeviate();
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                Thread.interrupted(); // clear interrupted status
            }
        }
    }

    /**
     * Loads the file containing the arrival rates from the specified file.
     * 
     * @param path The file containing the lambdas, one per line.
     * @return The array of lambdas
     * @throws IOException If an error occurs.
     */
    public static double[] loadLambdas(String path, double multiplier) throws IOException {
        List<Double> lambdas = new LinkedList<Double>();
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line = null;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("#")) {
                continue;
            }
            lambdas.add(Double.valueOf(line));
        }
        reader.close();
        double[] res = new double[lambdas.size()];
        for (int i = 0; i < lambdas.size(); i++) {
            res[i] = (lambdas.get(i) * multiplier);
        }
        return res;
    }

    /**
     * Example
     * 
     * java -cp . http.Clarknet http/load_month.txt http/high2.load http://en.wikipedia.org/
     * 
     * 
     * @param args (1) file containing the lambdas (one value on each line, lines
     * starting with the '#' character are ignored
     * (2) file containing the urls (one on each line),
     * (3) domain, e.g., http://www.wikipedia.org
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

        FileHandler fileHanlder = new FileHandler("clarknet.log");
        fileHanlder.setFormatter(formatter);
        fileHanlder.setLevel(Level.CONFIG);

        log.addHandler(handler);
        log.addHandler(fileHanlder);

        System.out
                .println("java -cp . http.Clarknet <lambdas> <urls> <domain>");
        if (args.length != 3) {
            System.exit(1);
        }

        final double[] lambdas = loadLambdas(args[0], LAMBDA_MULTIPLIER);
        //double[] oneDay = new double[34];
        //System.arraycopy(lambdas, 243, oneDay, 0, oneDay.length);
        String[] urls = loadUrls(args[1]);

        String domain = args[2];
        if (!domain.startsWith("http://")) {
            domain = "http://" + domain;
            log.warning("Domain fixed to " + domain);
        }

        log.info("Lambdas " + args[0]);
        log.info("Trace file " + args[1]);
        log.info("Domain " + domain);

        Clarknet lg = new Clarknet(log);
        lg.benchmark(lambdas, urls, domain);
    }

}
