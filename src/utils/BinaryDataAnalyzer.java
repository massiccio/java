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

package utils;

/**
 * BinaryDataAnalyzer
 * 
 * @(#)file BinaryDataAnalyzer
 * @(#)author <a href="mailto:michelemazzucco@gmail.com">Michele Mazzucco</a>
 * @(#)version 0.2
 * @(#)created Jan 29, 2010
 * @(#)lastedit May 24, 2013
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

/**
 * Computes the probability density function (PDF) and cumulative 
 * distribution function (CDF) of a binary file containing double values.
 * <p>
 * The produced text file contains three rows: the first one is the value
 * (bucket), the second is the frequency (PDF) and the third one is the
 * cumulative value (CDF).
 * <p>
 * The resulting file can be plotted easily with gnuplot, e.g.,
 * <p> 
 * plot "cdf.txt" u 1:2 w l
 * </p>
 * will plot the PDF, while
 * <p>
 * plot "cdf.txt" u 1:3 w l<p>
 * will plot the CDF.
 * 
 * The {@link #test(String)} method shows how to create an input file.
 */
public class BinaryDataAnalyzer {

	/**
	 * Chunk size for the memory mapped files (bytes).
	 */
    private static final int CHUNK_SIZE = 1024 * 1024 * 1024;
    
    private static final double BUCKETS = 1000d; 

    private final String input;

    private final String output;

    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;

    /** The number of values. */
    private long counter = 0L;
    
    /** The sum of values used to compute the average. */
    private double sum = 0d;

    private int[] buckets;
    private double bucketSize;


    
    
    /**
     * Class constructor.
     * 
     * @param input Path to the input file.
     * @param output Path to the output file.
     */
    public BinaryDataAnalyzer(String input, String output) {
        this.input = input;
        this.output = output;

        this.buckets = new int[1000]; // array of buckets
    }


    


    private final void computeDistribution(FileChannel fc) throws IOException {
        MappedByteBuffer mbb = null;
        final long size = fc.size();

        double range = this.max - this.min;
        this.bucketSize = range / BUCKETS;

        for (long startPosition = 0L; startPosition < size; startPosition += CHUNK_SIZE) {
            mbb = fc.map(MapMode.READ_ONLY, startPosition, Math.min(size
                    - startPosition, CHUNK_SIZE));
            fillBuckets(mbb);
            mbb.clear();
        }
    }


    /**
     * Writes the CDF and the density to file.
     * 
     * @param buckets The array of buckets.
     * @param bucketSize The size of each bucket.
     * @param population The population, <code>i.e.</code>, the number of
     *        values.
     * @throws FileNotFoundException If an error occurs.
     */
    private final void writeDistribution(long population)
            throws FileNotFoundException {
        PrintStream ps = new PrintStream(output);
        double cdf = 0d;
        for (int i = 0; i < this.buckets.length; i++) {
            ps.print(i * this.bucketSize);
            ps.print("\t");

            final double val = this.buckets[i];

            double frequency = val / (double) population;
            cdf += frequency;
            ps.print(frequency);
            ps.print("\t");
            ps.println(cdf);
        }

        ps.flush();
        ps.close();
    }


    /**
     * Fills the buckets
     * 
     * @param buckets The array of buckets.
     * @param bucketSize The bucket size.
     * @param mbb The byte buffer where the values are to be read.
     */
    private final void fillBuckets(ByteBuffer mbb) {
        DoubleBuffer db = mbb.asDoubleBuffer();
        while (db.hasRemaining()) {
            double tmp = db.get();
            this.counter++;
            this.sum += tmp;
            int index = (int) (tmp / this.bucketSize);
            index = index >= this.buckets.length - 1 ? this.buckets.length - 1
                    : index;

            try {
                this.buckets[index]++; // index starts at 0!
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.printf("Value %10.3f, index %d\n", tmp, index);
                throw e;
            }

        }
    }
    

    private final void minMax(ByteBuffer array) {
        final DoubleBuffer bb = array.asDoubleBuffer();
        while (bb.remaining() > 0) {
            final double val = bb.get();
            if (val < 0d) {
                throw new IllegalArgumentException("" + val);
            }

            int res = Double.compare(max, val);
            if (res < 0 && !Double.isNaN(val)) {
                this.max = val;
            }
            res = Double.compare(min, val);
            if (res > 0 && !Double.isNaN(val)) {
                this.min = val;
            }
        }
    }
    
    
    /**
     * Finds the maximum and minimum value of the values stored into the file
     * specified by the argument.
     * 
     * @param fc The file channel.
     * @throws IOException If an error occurs.
     */
    private final void findMaxAndMin(FileChannel fc) throws IOException {
        MappedByteBuffer mbb = null;
        final long size = fc.size();

        for (long startPosition = 0L; startPosition < size; startPosition += CHUNK_SIZE) {
        	// read in chunks
            mbb = fc.map(MapMode.READ_ONLY, startPosition, Math.min(size
                    - startPosition, CHUNK_SIZE));
            minMax(mbb);
            mbb.clear();
        }
    }
    

    /**
     * Performs the analysis.
     * 
     * @throws IOException If an error occurs.
     */
    public void analyseData() throws IOException {
        FileInputStream fis = null;
        try {
        	fis = new FileInputStream(input);        	
        	FileChannel fc = fis.getChannel();
            final long start = System.currentTimeMillis();

            final long size = fc.size();
            System.out.printf("File size %d\n", size);

            findMaxAndMin(fc);
            System.out.printf("Max %.3f \nMin %.3f\n", this.max, this.min);

            computeDistribution(fc);            
            System.out.println("Distribution created...");
            writeDistribution(size / 8); // double = 8 bytes
            
            System.out.printf("Mean value %.3f\n", (this.sum / this.counter));

            System.out.printf("Taken time (seconds) %.3f\n",
                    (System.currentTimeMillis() - start) / 1000d);

        } finally {
        	try {
        		fis.close();
        	} catch (IOException e) {
        		System.err.println(e.getMessage());
        	}
        }
    }


    
    /**
     * Test. Create a file with 1 GB of uniformly distributed random values.
     * 
     * @param f The file name
     * @throws IOException If an error occurs. 
     */
    public static void test(String f) throws IOException {
    	long size = CHUNK_SIZE;
    	
    	RandomAccessFile fos = null;
    	try { 
    		fos = new RandomAccessFile(f, "rw");
	    	FileChannel fc = fos.getChannel();
	    	MappedByteBuffer mbb = fc.map(MapMode.READ_WRITE, 0, size);
	    	final DoubleBuffer bb = mbb.asDoubleBuffer();
	        while (bb.remaining() > 0) {
	        	bb.put(Math.random());
	        }
    	} finally {
        	try {
        		fos.close();
        	} catch (IOException e) {
        		System.err.println(e.getMessage());
        	}
        }
    }
    
    

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        final String input = "test.tmp";
        final String output = "cdf.txt";
        
        //test(input);

        BinaryDataAnalyzer analyzer = new BinaryDataAnalyzer(input, output);
        analyzer.analyseData();
    	
    }

}
