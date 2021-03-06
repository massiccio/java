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

package queueing;

/**
 * @(#)file MMnKBalEq.java
 * @(#)author Michele Mazzucco
 * @(#)version 0.1
 * @(#)created Mar 26, 2011
 * @(#)lastedit May 26, 2013
 */

/**
 * Class providing metrics for the M/M/n/K queue.
 * <p>
 * This implementation does not have numerical issues in dealing with very large
 * numbers of n, or K, see {@link #main(String[])} for an example.
 */
public class MMnKQueue {

	/** Number of servers. */
	public final int n;

	/**
	 * The queue threshold. When k jobs are in the system new jobs are
	 * discarded.
	 */
	public final int k;

	/** Load (&rho; = &lambda; / &mu;). */
	public final double load;

	/** Array of probabilities. */
	private double[] p;

	/** The average number of jobs in the system. */
	private double L;

	/**
	 * Constructor.
	 * 
	 * @param n The number of servers.
	 * @param k The queue threshold.
	 * @param load The load.
	 * @throws IllegalArgumentException If n < 1, &rho < 0, or n > k.
	 */
	public MMnKQueue(int n, int k, double load) throws IllegalArgumentException {
		if (n < 1) {
			throw new IllegalArgumentException("Need at least one server!");
		}
		if (n > k) {
			throw new IllegalArgumentException("n cannot be larger than k!");
		}
		if (load < 0.0) {
			throw new IllegalArgumentException("The load must be >= 0");
		}
		this.n = n;
		this.k = k;
		this.load = load;
		p = computeProbabilities();
		L = computeL();
	}

	/**
	 * Gets the average number of jobs in the system.
	 */
	public double getL() {
		return this.L;
	}

	/**
	 * Gets the array of probabilities.
	 * 
	 * @return An array of probabilities. The sum should be <i>approximately</i>
	 *         1 (apart from rounding errors). p[i] is the probability of being
	 *         in state <i>i</i>
	 */
	public double[] getSteadyStateProbabilities() {
		return this.p;
	}

	/**
	 * Computes the average number of jobs in the system.
	 * 
	 * @return The average number of jobs.
	 */
	private final double computeL() {
		double L = 0d;
		// avg. number of jobs present, see Eq. 11
		for (int j = 1; j < p.length; j++) {
			L += j * p[j];
		}
		return L;
	}

	/**
	 * Computes the stationary distribution of the system being in state
	 * <i>i</i>, i = 0...K.
	 * <p>
	 * <strong>This is a correct version, added on October 13, 2011.</strong>
	 * For a M/N/n/K queue with n = 2, K = 5, &rho; = 3, the following p[i]
	 * should be found
	 * <ul>
	 * <li>p[0] = 0.024653312788906
	 * <li>p[1] = 0.073959938366718
	 * <li>p[2] = 0.110939907550077
	 * <li>p[3] = 0.166409861325116
	 * <li>p[4] = 0.249614791987673
	 * <li>p[5] = 0.37442218798151
	 * </ul
	 * 
	 * @return The stationary distribution of the number of jobs present.
	 * @see Also <a
	 *      href="http://www.emis.de/journals/HOA/JAMDS/6/143.pdf">"Calculation
	 *      of Steady-State Probabilities of M/M Queues: Further Approaches"</a>
	 */
	private final double[] computeProbabilities() {
		double[] p = new double[k + 1];
		int rhoCeiled = (int) (Math.ceil(load) + 0.5d);

		double sum = 1d;
		if (rhoCeiled > n) { // most likely state is p_k
			p[k] = 1d;
			// start from p_k and move backwards
			for (int i = k - 1; i >= 0; i--) {
				if (i >= n) {
					p[i] = n * p[i + 1] / load;
				} else { // index less than n
					p[i] = (i + 1) * p[i + 1] / load;
				}
				sum += p[i];
			}
		} else { // most likely state is p_rhoCeiled
			p[rhoCeiled] = 1d;
			// solve from p[rhoCeiled -1]...p[0] and p[rhoCeiled+1]...p[k]

			// p[rhoCeiled-1]...p[0]
			for (int i = rhoCeiled - 1; i >= 0; i--) {
				p[i] = (i + 1) * p[i + 1] / load;
				sum += p[i];
			}
			// p[rhoCeiled+1]...p[k]
			for (int i = rhoCeiled + 1; i <= k; i++) {
				if (i < n) {
					p[i] = (p[i - 1] * load) / i;
				} else {
					p[i] = (p[i - 1] * load) / n;
				}
				sum += p[i];
			}
		}

		// normalize
		for (int i = 0; i <= k; i++) {
			p[i] /= sum;
		}

		return p;
	}

	/**
	 * Computes the probability that the response time exceeds x.
	 * 
	 * @param x The target response time.
	 * @param mu The service rate.
	 * @return The probability that the response time exceeds x, given that the
	 *         service rate is &mu;.
	 * @see Eq. 12 <a href=
	 *      "http://www.cs.ncl.ac.uk/publications/inproceedings/papers/991.pdf"
	 *      >here</a>.
	 */
	public double calculateP(double x, double mu) {
		// Stores probabilities P(Wj>x) for each 0 <= j <= k-1
		double[] P = new double[this.k + 1];

		final double exp = Math.exp(-1.0 * mu * x);
		if (n == 1) {
			P[0] = exp;

			for (int j = 1; j < k; j++) {
				P[j] = 0.0;
				double sum = 0.0;
				for (int i = 0; i <= j; i++) {
					// XXX: this step can be improved by dividing (mu*x)
					// by i=0..j and summing the result
					double addOn = Math.pow(mu * x, (double) i);
					if (i != 0) {
						for (int m = 1; m <= i; m++)
							addOn /= (double) m;
					} // dividing addOn by factorial(i);
					sum += addOn;
				}
				P[j] += exp * sum;
			}
		} else {
			// No queueing: the response time is exponentially distributed
			for (int j = 0; j < n; j++)
				P[j] = exp;

			// Some queueing: conditional the response time is distributed as
			// the convolution of an Erlang distribution with parameters
			// (j -n +1, n*mu) and an exponential distribution with parameter mu
			final double expn = Math.exp(-1.0 * (double) n * mu * x);
			for (int j = n; j <= k; j++) {
				P[j] = exp
					* Math.pow(((double) n / (double) (n - 1)),
						(double) (j - n + 1));

				double sum = 0.0;
				for (int i = 0; i <= j - n; i++) {
					double mult = Math.pow((double) n / (double) (n - 1),
						(double) (j - n + 1));
					double addOn = Math.pow((double) n * mu * x, (double) i)
						- mult
						* Math.pow((double) (n - 1) * mu * x, (double) i);
					if (i != 0) {
						// XXX: this factorial is likely to be the cause of NaN
						// for small values
						for (int m = 1; m <= i; m++)
							addOn /= (double) m;
					} // dividing addOn by factorial(i);

					sum += addOn;
				}
				P[j] += expn * sum;
			}
		}

		// We have obtained the conditional response time, e.g., P(W > x)
		// given that there are i jobs in the system and given mu and lambda.
		// Now let's obtain the probability that the response time exceeds x.

		double prob = 0.0;
		for (int i = 0; i < P.length; i++) {
			// System.out.println("P[W_" + i + "]>x = " + P[i]);
			if (!Double.isNaN(P[i])) {
				// Some probabilities might be NaN. This happens when
				// p[i] is really small, e.g., 9.79745930930059E-44
				prob += P[i] * this.p[i];
			}
		}
		return prob;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int n = 1; // servers
		int k = 10 * n; // queue threshold
		double load = 0.99 * n; // lambda / mu

		MMnKQueue mmnk = new MMnKQueue(n, k, load);

		double[] p1 = mmnk.getSteadyStateProbabilities();

		double sum1 = 0d;

		double max = 0d; // probability
		int maxIndex = 0; // most likely state
		for (int i = 0; i <= k; i++) {
			sum1 += p1[i];

			// System.out.printf("State [%d], probability [%.10f]\n", i, p1[i]);
			if (Double.compare(p1[i], max) > 0d) {
				max = p1[i];
				maxIndex = i;
			}
		}

		System.out.printf("Sum of probabilities (should be 1) %.20f\n", sum1);

		System.out.printf("Most likely state [%d] with probability [%.10f]\n",
			maxIndex, max);

		double x = 2.0;
		double mu = 0.9;
		System.out.printf("Probability that the response time exceeds %.2f sec"
			+ " given that the service rate is %.2f [%.10f]\n", x, mu,
			mmnk.calculateP(x, mu));
	}

}
