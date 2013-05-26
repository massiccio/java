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

package stats;

/** 
 * @(#)file ExponentialRDG.java
 * @(#)author Michele Mazzucco
 * @(#)version 0.3
 * @(#)created Jan 27, 2010
 * @(#)lastedit April 12, 2012
 */


import java.security.SecureRandom;
import java.util.Random;


/**
 * Producer of random numbers using an exponential distirbution with the
 * specified mean.
 * <p>
 * To use a different distribution simply override the
 * {@link #generateDeviate()} method.
 * 
 * @author <a href="mailto:michele@cs.ucy.ac.cy>Michele Mazzucco</a>
 * 
 */
public class ExponentialRDG implements RDG {

    /**
     * The PRNG.
     */
    protected static final Random random;

    static {
        random = new SecureRandom();
    }

    /**
     * The mean value.
     */
    protected final double mean;


    /**
     * Creates a new <code>Producer</code> with the specified mean (1 / rate).
     * <p>
     * <strong>The parameter is the mean, not the rate!</strong>
     * 
     * @param mean The mean.
     */
    public ExponentialRDG(double mean) {
        this.mean = mean;
    }


    /**
     * Gets the mean.
     * 
     * @return The mean value.
     */
    public final double getMean() {
        return this.mean;
    }
    
    
    /**
     * Gets the squared coefficient of variation.
     * 
     * @return The squared coefficient of variation.
     */
    public double getScv() {
        return 1d;
    }


    /**
     * Generates a random deviate.
     * <p>
     * The default implementation produces exponentially distributed deviates.
     * 
     * @return A random deviate.
     */
    public double generateDeviate() {
        return expD(this.mean);
    }



    /**
     * Generate exponential random variable with a given mean.
     * 
     * @param mean The mean.
     * @return An exponentially distributed random number with the given mean.
     */
    protected final double expD(double mean) {
        return -mean * Math.log(1 - nextRandom());
    }


    protected static final double nextRandom() {
        return random.nextDouble();
    }

    
    public final double getRate() {
        return 1d / getMean();
    }
}