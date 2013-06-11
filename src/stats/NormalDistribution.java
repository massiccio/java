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
 * Routines for normal distribution.
 * <p>
 * The algorithms for computing the CDF and the complementary CDF are due to
 * Marsaglia, while those for computing the inverse
 * 
 * @see <a href="http://www.jstatsoft.org/v11/i05/paper">George Marsaglia,
 *      Evaluating the Normal Distribution</a>
 */
public class NormalDistribution {

	private NormalDistribution() {
		//
	}

	/**
	 * Standard normal cdf evaluated using Marsaglia's algorithm.
	 */
	public static final double Phi(double x) {
		if (x < -8.0)
			return 0.0;
		if (x > 8.0)
			return 1.0;
		double s = x;
		double t = 0.0;
		double b = x;
		double q = x * x;
		int i = 1;
		while (Math.abs(s - t) > 0.000000001)
			s = (t = s) + (b *= q / (i += 2));
		return .5 + s * Math.exp(-.5 * q - .91893853320467274178);
	}

	/**
	 * Normal cdf with mean mu and stddev sigma
	 */
	public static double Phi(double z, double mu, double sigma) {
		return Phi((z - mu) / sigma);
	}

	/**
	 * Standard normal pdf.
	 * <p>
	 * Algorithm 26.2.1, page 931 in Abromowitz and Stegun, Handbook of
	 * Mathematical Functions.
	 */
	public static double phi(double x) {
		return Math.exp(-x * x / 2) / Math.sqrt(2 * Math.PI);
	}

	/**
	 * Normal pdf with mean mu and stddev sigma.
	 */
	public static double phi(double x, double mu, double sigma) {
		return phi((x - mu) / sigma) / sigma;
	}

	/**
	 * Tail values for the standard normal distribution (complementary CDF),
	 * i.e., 1 - &Phi;(x) = .5 * erfc(x/sqrt(2)) using Marsaglia's algorithm
	 */
	public static final double cPhi(double x) {
		double R[] = { 1.25331413731550025, .421369229288054473,
			.236652382913560671, .162377660896867462, .123131963257932296,
			.0990285964717319214, .0827662865013691773, .0710695805388521071,
			.0622586659950261958 };
		int i, j;
		i = j = (int) (0.5 * (Math.abs(x) + 1.0));
		double pwr = 1.0;
		double a = R[j];
		double z = 2 * j;
		double b = a * z - 1;
		double h = Math.abs(x) - z;
		double s = a + h * b;
		double t = a;
		double q = h * h;
//		for (i = 2; s != t; i += 2) {
		for (i = 2; Math.abs(s - t) > 0.000000001; i += 2) {
			a = (a + z * b) / i;
			b = (b + z * a) / (i + 1);
			pwr *= q;
			s = (t = s) + pwr * (a + h * b);
		}
		// .918938533... is log(PI*2)/2
		s = s * Math.exp(-0.5 * x * x - .91893853320467274178);
		if (x >= 0)
			return (double) s;
		return (double) (1. - s);
	}

	/**
	 * Compute z such that Phi(z) = y via bisection search.
	 */
	public static double PhiInverse(double y) {
		return PhiInverse(y, .00000001, -8, 8);
	}

	// bisection search
	private static double PhiInverse(double y, double delta, double lo,
		double hi) {
		double mid = lo + (hi - lo) / 2;
		if (hi - lo < delta)
			return mid;
		if (Phi(mid) > y)
			return PhiInverse(y, delta, lo, mid);
		else
			return PhiInverse(y, delta, mid, hi);
	}

	public static void main(String[] args) {
		System.out.println(Phi(0.1));
		System.out.println(cPhi(0.1));
		System.out.println(PhiInverse(0.5));
	}
}
