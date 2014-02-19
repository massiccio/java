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
 * Implements the <a
 * href="http://mathworld.wolfram.com/Legendre-GaussQuadrature.html"
 * >Legendre-Gauss Quadrature</a> formula.
 * 
 * @author <a href="mailto:michelemazzucco@gmail.com">Michele Mazzucco</a>
 */
public class GaussQuadrature {
	
	
	private GaussQuadrature() {
		// avoid instantiation
	}

	/**
	 * Computes the integral of the function func between a and b using the
	 * Gauss-Legendre integration method. The function is evaluated ten times at
	 * interior points in the range of integration.
	 * <p>
	 * The weights and abscissas coefficients are those reported at page 916
	 * (Chapter 25) of Abramowitz and Stegun "Handbook of Mathematical Functions
	 * with formulas, graphs, and mathematical tables".
	 * 
	 * @param func The function to integrate.
	 * @param a The lower bound of integration.
	 * @param b The upper bound of integration.
	 * @return The integral of func between a and b.
	 */
	public static double integrate(UnivariateRealFunction func, double a, double b) {
		// The abscissas and weights, +/- x_i, w_i
		final double[] x = { 0.148874338981631, 0.433395394129247,
				0.679409568299024, 0.865063366688985, 0.973906528517172 };
		// sum w = 2 (five numbers, each used twice)
		final double[] w = { 0.295524224714753, 0.269266719309996,
				0.219086362515982, 0.149451349150581, 0.066671344308688 };

		final double xm = 0.5 * (b + a);
		final double xr = 0.5 * (b - a);
		double s = 0.0;
		for (int j = 0; j < w.length; j++) {
			double dx = xr * x[j];
			// step described here
			// http://en.wikipedia.org/wiki/Gaussian_quadrature#Change_of_interval
			s += w[j] * (func.value(xm + dx) + func.value(xm - dx));
		}
		return s *= xr; // Scale the answer to the range of integration.
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final UnivariateRealFunction func = new UnivariateRealFunction() {

			public double value(double x) {
				return (x - 1.0) * (x - 0.5) * x * (x + 0.5) * (x + 1.0);
			}
		};

		double val = integrate(func, 0.0, 1.0);
		System.out.println(val);
	}

}
