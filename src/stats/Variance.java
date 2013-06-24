/**
 * Jun 24, 2013
 * Variance.java
 */
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

package stats;

/**
 * Class providing methods to compute the unbiased variance of a population
 * sample.
 */
public class Variance extends Average {

	// -----------------------------------------------------------------------
	// Instance fields
	// -----------------------------------------------------------------------

	/** The sum of squares. */
	private double sumOfSquares;

	// -----------------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------------

	/**
	 * Default constructor.
	 */
	public Variance() {
		super();
		sumOfSquares = 0.0;
	}

	/**
	 * Creates a new Variance object initialized with the specified population.
	 */
	public Variance(double[] array) {
		this();
		for (double value : array) {
			add(value);
		}
	}

	// -----------------------------------------------------------------------
	// Methods
	// -----------------------------------------------------------------------

	@Override
	public void add(double value) {
		super.add(value);
		this.sumOfSquares += (value * value);
	}

	/**
	 * Computes Var(X) (unbiased).
	 * 
	 * @return The variance.
	 */
	public final double variance() {
		final long population = super.counter;
		if (population == 0L || population == 1L) {
			return 0.0;
		}
		final double m = super.aggregate;
		double ss = this.sumOfSquares - (m * m / population);
		return ss / (population - 1);
	}

	/**
	 * Computes the unbiased variance of the specified input.
	 */
	public static final double variance(double[] array) {
		return new Variance(array).variance();
	}

	/**
	 * Computes and returns the standard deviation &sigma;.
	 * 
	 * @return The standard deviation.
	 */
	public final double standardDeviation() {
		return Math.sqrt(variance());
	}

	public static void main(String[] args) {
		double[] x = { 2, 3, 5, 6 };
		System.out.println(variance(x));
	}
}
