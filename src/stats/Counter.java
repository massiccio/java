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
 * @(#)file Counter.java
 * @(#)author Michele Mazzucco
 * @(#)version 0.1
 * @(#)created Mar 29, 2012
 * @(#)lastedit May 25, 2013
 */


/**
 * Counter
 */
public class Counter {

    protected long counter;

    /**
     * Creates a new Counter object initialized at 0.
     */
    public Counter() {
        counter = 1L;
    }
    
    
    /**
     * Increments the value of counter by one.
     */
    public void increment() {
    	this.counter++;
    }
    
    /**
     * Gets the number of times the {@link #increment()} has been called.
     * 
     * @return The value of counter.
     */
    public long getCounter() {
		return counter;
	}
    
    protected void reset() {
    	this.counter = 0L;
    }
    
}