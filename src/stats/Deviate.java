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
 * @(#)file Deviate.java
 * @(#)author Michele Mazzucco
 * @(#)version 0.1
 * @(#)created Apr 12, 2012
 * @(#)lastedit Apr 12, 2012
 */

/**
 * This object can be used to create random numbers genererated eiter according
 * an Exponential of Log-Normal distribution.
 */
public class Deviate {
    
    
    private RDG rdg;
    
    /**
     * 
     */
    public Deviate(double mean, double scv) {
        if (scv == 1.0) {
            rdg = new ExponentialRDG(mean);
        } else {
            rdg = new LogNormalRDG(mean, scv);
        }
    }

    
    /**
     * Generates a new random value.
     * 
     * @return A random double.
     */
    public double generateDeviate() {
       return this.rdg.generateDeviate();
    }
    
    
    /**
     * Sets the new mean for exponentially distributed deviates.
     * 
     * @param mean The mean to set.
     */
    public void set(double mean) {
        this.rdg = new ExponentialRDG(mean);
    }
    
    
    /**
     * Sets the new mean and squared coefficient of deviation.
     * 
     * @param mean The mean value.
     * @param scv The squared coefficient of deviation.
     */
    public void set(double mean, double scv) {
        if (scv == 1.0) {
            set(mean);
        } else {
            this.rdg = new LogNormalRDG(mean, scv);
        }
    }
    
}
