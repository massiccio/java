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

package math;

/**
 * Error function.
 */
public class ErrorFunction {
	
	
	private ErrorFunction() {
		//
	}

	/**
	 * Returns the complementary error function erfc(x) withf ractional error
	 * everywhere less than 1.2 * 10^-7.
	 * <p>
	 * Routine from Numerical recipes in C, sec. 6.2 (page 221).
	 */
	public static double erfc(double x) {
		final double t = 1.0 / (1.0 + 0.5 * Math.abs(x));
		final double ans = t
			* Math.exp(-x
				* x
				- 1.26551223
				+ t
				* (1.00002368 + t
					* (0.37409196 + t
						* (0.09678418 + t
							* (-0.18628806 + t
								* (0.27886807 + t
									* (-1.13520398 + t
										* (1.48851587 + t
											* (-0.82215223 + t * 0.17087277)))))))));

		return x >= 0.0 ? ans : -ans;
	}

	/**
	 * Returns the error function erf(x) withf ractional error everywhere less
	 * than 1.2 * 10^-7.
	 */
	public static double erf(double x) {
		return 1.0 - erfc(x);
	}


}
