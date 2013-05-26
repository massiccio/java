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
 * @(#)file Utils.java
 * @(#)author Michele Mazzucco
 * @(#)version 0.1
 * @(#)created Nov 25, 2011
 * @(#)lastedit Nov 25, 2011
 */


import java.util.Arrays;

/**
 * Utilities
 */
public class Utils {


    /**
     * 
     */
    private Utils() {
        //
    }
    
    /**
     * Creates an array of length len, initialized at 0.
     * 
     * @param len The array length.
     * @return The array.
     */
    public static final double[] zeros(int len) {
        double[] tmp = new double[len];
        Arrays.fill(tmp, 0d);
        return tmp;
    }

    
}
