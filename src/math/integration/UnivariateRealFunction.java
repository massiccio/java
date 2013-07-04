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
 * Jul 2, 2013
 * GausQuadrature.java
 */
package math.integration;

/**
 * An interface representing a univariate real function.
 * 
 * @author <a href="mailto:michelemazzucco@gmail.com">Michele Mazzucco</a>
 */
public interface UnivariateRealFunction {

	/**
	 * Evaluates this function at point x.
	 * 
	 * @throws If an error occurs while evaluating this function at point x.
	 */
	public double value(double x) throws IllegalArgumentException;
}