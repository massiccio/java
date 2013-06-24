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
public class CoefficientOfVariation extends Variance {



	// -----------------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------------

	/**
	 * Creates a new <code>Cov</code> instance.
	 */
	public CoefficientOfVariation() {
		super();
	}


	// -----------------------------------------------------------------------
	// Methods
	// -----------------------------------------------------------------------

	

	/**
	 * Gets the squared coefficient of variation cov<sup>2</sup>.
	 * 
	 * @return The squared coefficient of variation.
	 */
	public double cov2() {
		if (size() < 10L) {
			return 1d;
		}
//		return (((size() * this.sumOfSquares) / (super.aggregate * super.aggregate)) - 1.d);
		return super.variance() / (super.mean() * super.mean());
	}


	/**
	 * Gets the coefficient of variation cov.
	 * 
	 * @return The coefficient of variation.
	 */
	public double cov() {
		return Math.sqrt(cov2());
	}
	
	
	
	public static void main(String[] args) {
		ExponentialRDG exp = new ExponentialRDG(4.0);
		CoefficientOfVariation cov = new CoefficientOfVariation();
		for (int i = 0; i < 100000; i++) {
			cov.add(exp.generateDeviate());
		}
		
		System.out.printf("mean %.10f, var %.10f, cov2 %.10f\n", cov.mean(), cov.variance(), cov.cov2());
	}

}