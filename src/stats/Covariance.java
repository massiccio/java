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
 * Class to be used to compute the covariance between two sample populations.
 */
public class Covariance {

	private final double[] xx;

	private final double[] yy;

	/**
	 * Creates a new Covariance object with the specified samples.
	 * 
	 * @throws IllegalArgumentException If the two arrays have different length.
	 */
	public Covariance(double[] xx, double[] yy) throws IllegalArgumentException {
		if (xx.length != yy.length) {
			throw new IllegalArgumentException(
				"The two arrays must have the same length");
		}
		this.xx = xx;
		this.yy = yy;
	}

	/**
	 * Computes the covariance.
	 */
	public double covariance() {
		final double meanX = Average.mean(this.xx);
		final double meanY = Average.mean(this.yy);

		double sum = 0.0;
		for (int i = 0; i < this.xx.length; i++) {
			sum += (this.xx[i] - meanX) * (this.yy[i] - meanY);
		}
		final double val = sum / (this.xx.length - 1);
		return val;
	}

	/**
	 * Computes the covariance between the provided sample pupulations.
	 * 
	 * @throws IllegalArgumentException If the two arrays have different length.
	 */
	public static double covariance(double[] xx, double[] yy)
		throws IllegalArgumentException {
		return new Covariance(xx, yy).covariance();
	}

}
