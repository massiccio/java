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

package http;


/**
 * @(#)file Stream.java
 * @(#)author Michele Mazzucco
 * @(#)version 0.1
 * @(#)created Mar 29, 2012
 * @(#)lastedit Mar 29, 2012
 */


import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import stats.Average;
import stats.Counter;

/**
 * Statistics. Private to each Crawler.
 */
class Stream {

    private PrintWriter logger;

    private long counter; // success + errors

    private int errors;

    private FileOutputStream fos;

    /** Hash table used to store metrics regarding HTTP return codes. */
    private Map<Integer, Counter> statusCodes;

    /** Average response time, in milliseconds. */
    private Average responseTimes;
    
    /** Average file sizes, in bytes. */
    private Average fileSizes;


    /**
     * Constructor
     */
    public Stream() throws FileNotFoundException {
        fos = new FileOutputStream("client.log");
        logger = new PrintWriter(new BufferedOutputStream(fos, 4096), false);
        counter = 0L;
        errors = 0;
        statusCodes = new HashMap<Integer, Counter>();

        logger.println("# File created on " + new Date().toString());
        logger.println("# Event no. HTTP code, resp. time, bytes");

        responseTimes = new Average();
        fileSizes = new Average();
    }

    /**
     * Close this stream and releases the resources.
     */
    public void close() {
        if (this.responseTimes.size() > 0) {
            this.logger.print("# Total responses ");
            this.logger.println(this.responseTimes.size());
            this.logger.print("# Avg. resp. time (ms) ");
            this.logger.println(this.responseTimes.mean());
            this.logger.print("# Avg. file size (bytes) ");
            this.logger.println(this.fileSizes.mean());
        }

        this.logger.flush();
        try {
            this.fos.getChannel().force(true);
        } catch (IOException e) {
            // ignore
        }
        try {
            this.fos.close();
        } catch (IOException e) {
            // ignore
        }

        this.logger.close();
        
        System.out.printf("\nAvg. resp time %.3f ms\n", this.responseTimes.mean());
    }

    /**
     * Log an error.
     */
    public void logError(Download val) {
        this.errors++;
        log(val);
    }
    
    /**
     * Gets the number of errors occurred.
     */
    public int getErrors() {
    	return this.errors;
    }
    
    
    /**
     * Gets the total number of events.
     */
    public long getCounter() {
    	return this.counter;
    }
    
    
    /**
     * Gets the total amount of downloaded data, in bytes.
     */
    public double getDownloadedBytes() {
    	return this.fileSizes.getAggregate();
    }
    

    /**
     * Logs the data about the executed request.
     * 
     * @param download The request.
     */
    public void log(Download download) {
        this.counter++;

        DownloadImpl val = (DownloadImpl) download;
        final long respTime = val.getResponseTime();
        final long bytes = val.getDataLength();

        this.logger.print(this.counter);
        this.logger.append("\t");
        this.logger.print(val.getHttpStatus());
        this.logger.append("\t");
        this.logger.print(respTime);
        this.logger.append("\t");
        this.logger.print(bytes);
        this.logger.append("\n");

        updateStatusCode(val.getHttpStatus());
        this.responseTimes.add(respTime);
        this.fileSizes.add(bytes);
    }

    private void updateStatusCode(int respCode) {
        // update status codes
        synchronized (statusCodes) {
            Integer key = Integer.valueOf(respCode);
            if (statusCodes.containsKey(key)) {
                statusCodes.get(key).increment();
            } else {
                statusCodes.put(key, new Counter());
            }
        }
    }
    
    
    /**
     * Creates a copy of the status and returns an iterator.
     * 
     * @return An iterator to a copy of the status.
     */
    public Iterator<Entry<Integer, Counter>> getStatusCodes() {
    	Map<Integer, Counter> copy = new HashMap<Integer, Counter>(this.statusCodes);
    	return copy.entrySet().iterator();
    }

}