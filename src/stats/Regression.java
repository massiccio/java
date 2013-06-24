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
 * @(#)file Regression.java
 * @(#)author Michele Mazzucco
 * @(#)version 0.2
 * @(#)created Mar 7, 2011
 * @(#)lastedit June 24, 2013
 */

package stats;

/**
 * A regression is a statistical analysis assessing the association between two
 * variables. It is used to find the relationship between two variables.
 * <p>
 * Regression equation y = a + bx
 * <p>
 * Given two arrays X[] and Y[], this class finds the intercept point of the
 * regression line and the y axis (a) and the slope of the regression line which
 * satisfy the above equation and minimizes the squares of the errors (least
 * squares).
 * <p>
 * This is the implementation of the algorithm described <a href=
 * "http://en.wikipedia.org/wiki/Simple_linear_regression#Fitting_the_regression_line"
 * >here</a>.
 * <p>
 * More details <a
 * href="http://easycalculation.com/statistics/learn-regression.php">here</a>.
 * 
 * @author <a href="mailto:michelemazzucco@gmail.com">Michele Mazzucco</a>
 */
public class Regression {

	/** The intercept. */
	private double alpha;

	/** The slope. */
	private double beta;

	/**
	 * Constructor.
	 */
	public Regression() {
		alpha = 0d;
		beta = 0d;
	}

	/**
	 * Fits the regression line {@link http
	 * ://en.wikipedia.org/wiki/Simple_linear_regression
	 * #Fitting_the_regression_line}
	 * 
	 * @param x The array of x values.
	 * @param y The array of y values.
	 */
	public void regression(double[] x, double[] y)
		throws IllegalArgumentException {
		if (x.length != y.length) {
			throw new IllegalArgumentException(
				"The two arrays must have the same length");
		}
		// http://easycalculation.com/statistics/learn-regression.php
		// double[] x = {60, 61, 62, 63, 65};
		// double[] y = {3.1, 3.6, 3.8, 4, 4.1};

		// double corrCoefficient = Stat.corrCoeff(x, y);
		// System.out.println(corrCoefficient);

		// Variance varX = new Variance(x);
		// Variance varY = new Variance(y);

		// double sdX = varX.standardDeviation();
		// double sdY = varY.standardDeviation();

		// this.beta = corrCoefficient * (sdY / sdX);
		// this.alpha = varY.mean() - beta * varX.mean();

		// ALTERNATIVE method
		// double covarianceXY = Stat.covariance(x, y);
		// double varX = Stat.variance(x);
		// beta = covarianceXY / varX;
		// alpha = Stat.mean(y) - beta * Stat.mean(x);

		double covarianceXY = Covariance.covariance(x, y);
		Variance varX = new Variance(x);
		this.beta = covarianceXY / varX.variance();
		this.alpha = Average.mean(y) - (this.beta * varX.mean());
	}

	/**
	 * Gets the intercept.
	 * 
	 * @return the alpha
	 */
	public double getAlpha() {
		return alpha;
	}

	/**
	 * Gets the slope.
	 * 
	 * @return the beta
	 */
	public double getBeta() {
		return beta;
	}

	/**
	 * Computes the value of y at the specified x point using the linear model y
	 * = a + bx.
	 */
	public double getValueAt(double x) {
		return this.alpha + this.beta * x;
	}

	public static void main(String[] args) {
		// see example at
		// http://www.wolframalpha.com/input/?i=linear+fit+{1.3%2C+2.2}%2C{2.1%2C+5.8}%2C{3.7%2C+10.2}%2C{4.2%2C+11.8}

		double[] x = { 1.3, 2.1, 3.7, 4.2, 8, 12, 15 };
		double[] y = { 2.2, 5.8, 10.2, 11.8, 20, 21, 22 };

		// http://www.sciencebuddies.org/science-fair-projects/project_data_analysis_variance_std_deviation.shtml
		// double[] x = { 3, 4, 4, 5, 6, 8};
		// double[] y = {1, 2, 4, 5, 7, 11};

		Variance varX = new Variance(x);
		Variance varY = new Variance(y);
		System.out.printf("var x %.3f, mean x %.3f, n x %d\n", varX.variance(),
			varX.mean(), varX.size());
		System.out.printf("var y %.3f, mean y %.3f, n y %d\n", varY.variance(),
			varY.mean(), varY.size());
		Regression r = new Regression();
		r.regression(x, y);
		System.out.printf("y = a + bx: a = %10.5f, b= %10.5f\n", r.getAlpha(),
			r.getBeta());
	}

}
