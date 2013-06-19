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
 * Various mathematical routines related to the factorial function
 */
public class Factorial {

	private Factorial() {
		//
	}

	/**
	 * Returns ln(n!).
	 * 
	 * @param n An integer value
	 * @return ln(n!).
	 */
	public static double factln(int n) {
		if (n < 0)
			throw new IllegalArgumentException(
				"Negative factorial in routine factln");
		if (n <= 1)
			return 0.0;
		return GammaFunction.gammln(n + 1.0);
	}

	/**
	 * Computes ln(n!) using Stirling's approximation.
	 * 
	 * @param n The number
	 * @return The natural logarithm of factorial(n).
	 * @see <a
	 *      href="http://en.wikipedia.org/wiki/Stirling's_approximation">Stirling
	 *      's approximation</a>.
	 */
	public static final double stirlingApproxLn(int n) {
		if ((n == 0) || (n == 1)) {
			return 0;
		}
		return n * Math.log(n) - n + 0.5 * Math.log(2 * Math.PI * n);
	}

	/**
	 * Returns n!.
	 * 
	 * @param n An integer value
	 * @return n!.
	 */
	public static long factorial(int n) {
		return (long) (0.5 + Math.exp(factln(n)));
	}

	/**
	 * Computes x!/n! Even though an algorithm based on the gamma function can
	 * be used, this algorithm computes x * (x-1) * ... * (x-n+1).
	 * <p>
	 * It is required that x >= n.
	 * 
	 * @throws IllegalArgumentException If n or j are negative or if n < j.
	 * @see <a href="http://mathworld.wolfram.com/FallingFactorial.html>Falling
	 *      Factorial</a>
	 */
	public static long fallingFactorial(int x, int n) {
		if ((n < 0) || (x < 0))
			throw new IllegalArgumentException("Negative factorial argument");
		if (x < n)
			throw new IllegalArgumentException("n < j");

		int tmp = x;
		long res = 1L;
		while (tmp >= (x - n + 1)) {
			res *= tmp;
			tmp--;
		}
		return res;
	}

	/**
	 * Computes the ration between n factorial and j factorial.
	 * <p>
	 * The employed algorithm computes n!/j! as (j+1) * (j+2) *...* n.
	 * 
	 * @throws IllegalArgumentException If n or j are negative or if n < j.
	 */
	public static long factorialRatio(int n, int j) throws IllegalArgumentException {
		if ((n < 0) || (j < 0))
			throw new IllegalArgumentException("Negative factorial argument");
		if (n < j)
			throw new IllegalArgumentException("n < j");

		int tmp = j + 1;
		long res = 1L;
		while (tmp <= n) {
			res *= tmp;
			tmp++;
		}
		return res;
	}
}
