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
 * @(#)file RDG.java
 * @(#)author <a href="mailto:michelemazzucco@gmail.com">Michele Mazzucco</a>
 * @(#)version 0.1
 * @(#)created Mar 8, 2010
 * @(#)lastedit May 25, 2015
 */

/**
 * Random deviate generator.
 */
public interface RDG {

    /**
     * Generates a random deviate.
     * 
     * @return A random deviate.
     */
    public double generateDeviate();
    
    
    /**
     * Gets the average value.
     * 
     * @return The average value.
     */
    public double getMean();
    
    
    /**
     * Gets the squared coefficient of variation, i.e., the variance divided
     * by the square of the mean.
     * 
     * @return The squared coefficient of deviation.
     */
    public double getScv();
    
    
    /**
     * Gets the rate, <i>i.e.</i>, 1 / {@link #getMean()}.
     * 
     * @return The rate.
     * @see #getMean()
     * @return
     */
    public double getRate();
}