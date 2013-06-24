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
 * QoSP project.
 * 
 * @(#)file Interarrivals.java
 * @(#)author Michele Mazzucco
 * @(#)version 0.1
 * @(#)created 14 Feb 2008
 * @(#)lastedit 14 Feb 2008
 */

package stats;

import utils.Resettable;

/**
 * Utility class used to compute the coefficient of variation for interarrival
 * times.
 * 
 * @author <a href="michele.Mazzucco@ncl.ac.uk">Michele Mazzucco</a>
 */
public class Interarrivals implements Resettable {

    // -----------------------------------------------------------------------
    // Instance fields
    // -----------------------------------------------------------------------
    
    /**
     * The timestamp of the last arrival.
     */
    private double lastArrival;
    
    /**
     * The object storing the values.
     */
    private CoefficientOfVariation values;
    

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------
    
    /**
     * Creates a new <code>Interarrivals</code> object.
     */
    public Interarrivals() {
        values = new CoefficientOfVariation();
        lastArrival = Double.MIN_VALUE;
    }
    

    // -----------------------------------------------------------------------
    // Methods
    // -----------------------------------------------------------------------
    

    /**
     * Records an arrival event.
     */
    public void arrival(final double curTime) {
        if (this.lastArrival > Double.MIN_VALUE) {
            final double interarrival = (curTime - this.lastArrival);
            if (interarrival < 0) {
            	throw new IllegalStateException();
            }
            
            this.values.add(interarrival);
        }
        
        this.lastArrival = curTime;
    }
    

    /**
     * Gets the mean value of interarrival times.
     * 
     * @return The mean value.
     */
    public double mean() {
        return this.values.mean();
    }
    

    /**
     * Gets the squared coefficient of variation of interarrival times.
     * 
     * @return The coefficient of variation, or <i>1</i> if the population
     *         includes &lt; <i>10</i> values.
     */
    public double getCov2() {
        return this.values.cov2();
    }
    

    /**
     * Resets the internal state of this object.
     */
    public void reset() {    	
        this.values.reset();
        this.lastArrival = Double.MIN_VALUE;
    }
    
    
    /**
     * Gets the number of elements.
     */
    public long size() {
    	return this.values.size();
    }
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getName());
        sb.append('@');
        sb.append("lastArrival=").append(this.lastArrival);
        sb.append(", mean=").append(this.values.mean());
        sb.append(", sq. coeff. of variation=").append(this.values.cov2());
        return sb.toString();
    }
    
} // END Interarrivals
