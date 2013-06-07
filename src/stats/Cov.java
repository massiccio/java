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
 * Cov
 * 
 * @(#)file Cov.java
 * @(#)author <a href="mailto:michelemazzucco@gmail.com">Michele Mazzucco</a>
 * @(#)version 0.1
 * @(#)created Jun 19, 2008
 * @(#)created Jun 19, 2008
 */

/**
 * This class is used to compute the variance (Var(X)), standard deviation (&sigma;),
 * coefficient of variation (cov &sub) and the squared of the coefficient of
 * variation (cov<sup>2</sup>).
 */
public class Cov extends Average {

	// -----------------------------------------------------------------------
	// Instance fields
	// -----------------------------------------------------------------------

	/** The sum of squares. */
	private double sumOfSquares;


	// -----------------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------------

	/**
	 * Creates a new <code>Cov</code> instance.
	 */
	public Cov() {
		super();
		sumOfSquares = 0.d;
	}


	// -----------------------------------------------------------------------
	// Methods
	// -----------------------------------------------------------------------

	/**
	 * Adds the specified value to the statistics.
	 * 
	 * @param value The value to add.
	 */
	@Override
	public void add(double value) {
		super.add(value);
		this.sumOfSquares += (value * value);
	}


	/**
	 * Gets the squared coefficient of variation cov<sup>2</sup>.
	 * 
	 * @return The squared coefficient of variation.
	 */
	public double getCov2() {
		if (size() < 10L) {
			return 1d;
		}
		return (((size() * this.sumOfSquares) / (super.aggregate * super.aggregate)) - 1.d);
	}


	/**
	 * Gets the coefficient of variation cov.
	 * 
	 * @return The coefficient of variation.
	 */
	public double getCov() {
		return Math.sqrt(getCov2());
	}


	/**
	 * Computes Var(X).
	 * 
	 * @return The variance.
	 */
	public double getVariance() {
		long population = super.size();
		return (this.sumOfSquares / population) - (mean() * mean());
	}


	/**
	 * Computes and returns the standard deviation &sigma;.
	 * @return The standard deviation.
	 */
	public double getStdDev() {
		return (getCov() * mean());
	}

}