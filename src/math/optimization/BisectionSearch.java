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
package math.optimization;

/**
 * The bisection (or binary search) method in mathematics is a root-finding
 * method which repeatedly bisects an interval and then selects a subinterval in
 * which a root must lie for further processing.
 */
public class BisectionSearch {

	/**
	 * Tolerance in determining convergence upon a root
	 */
	private static final double TOLERANCE = 1e-9;

	private BisectionSearch() {
		// avoid instantiation
	}

	/**
	 * Bisection (binary search) method.
	 * 
	 * @param fun The function to evaluate.
	 * @param lowerBound The lower bound.
	 * @param upperBound The upper bound.
	 * @return
	 */
	public static double bisect(RootFunction fun, double lowerBound,
			double upperBound) {
		double mid = (upperBound - lowerBound) / 2.0;

		int i = 0; // no of iterations
		final int maxIterations = 1000;
		double preMid = 0.0;
		do {
			i++;
			if (Math.abs(preMid - mid) < TOLERANCE) {
				break;
			}
			preMid = mid;

			double fl = fun.evaluate(lowerBound);
			double fm = fun.evaluate(mid);
			double fu = fun.evaluate(upperBound);

			if (fl >= 0 && fm <= 0) {
				upperBound = mid;
				mid = (upperBound + lowerBound) / 2.0;
				continue;
			}
			if (fm >= 0 && fu <= 0) {
				lowerBound = mid;
				mid = (upperBound + lowerBound) / 2.0;
				continue;
			}
			if (fu >= 0) {
				lowerBound = upperBound;
				upperBound *= 2.0; // double the upper bound
				mid = (upperBound + lowerBound) / 2.0;
				continue;
			}
			if (fl <= 0) {
				upperBound = lowerBound;
				lowerBound /= 2.0; // halve the lower bound
				mid = (upperBound + lowerBound) / 2.0;
				continue;
			}
		} while (i < maxIterations);

		System.out.println(i);
		return preMid;
	}
}
