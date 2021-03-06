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
 * Jun 16, 2013
 * ErlangA.java
 */
package queueing;

import utils.DoubleArrayList;

/**
 * Functions for the Erlang-A model (M/M/n+M queue).
 * <p>
 * A number of metrics can be expressed by means of the incomplete Gamma
 * function. However, since that grows really quickly, causing overflows, an
 * algorithm which is numerically stable is employed.
 */
public class ErlangA {

	private static final double MAX_ITERATIONS = 10E+6;

	private static final double ERR = 10E-15;

	/** Number of servers. */
	private final int n;

	/** Load (&rho; = &lambda; / &mu;). */
	private final double rho;

	/** The arrival rate. */
	private final double lam;

	/** The service rate. */
	private final double mu;

	/** The abandonment rate. The average patience is defined as 1/&theta;. */
	private final double theta;

	/** Steady-state distribution of probabilities. */
	private double[] p;

	/** Steady-state distribution of jobs. */
	private double[] jobs;

	/** Cached value of {@link Axy}. */
	private final double axy;

	/**
	 * Cached value of pn, i.e., probability that there are n jobs in the
	 * system.
	 */
	private final double pn;

	/**
	 * Constructor.
	 * 
	 * @param n The number of servers.
	 * @param lam The arrival rate.
	 * @param mu The service rate.
	 * @param theta The abandonment rate.
	 */
	public ErlangA(int n, double lam, double mu, double theta)
			throws IllegalArgumentException {
		if (n < 1) {
			throw new IllegalArgumentException("Need at least one server!");
		}
		if ((lam < 0.0) || (mu < 0.0)) {
			throw new IllegalArgumentException("Load parameters must be >= 0");
		}
		if (theta <= 0.0) {
			// if theta = 0 it becomes a M/M/n queue
			throw new IllegalArgumentException(
					"The abandonment rate must be > 0");
		}
		this.n = n;
		this.lam = lam;
		this.mu = mu;
		this.rho = lam / mu;
		this.theta = theta;

		double x = this.n * this.mu / this.theta;
		double y = this.lam / this.theta;

		this.axy = axy(x, y);
		this.pn = pn();
		this.p = computeProbabilitiesP0();
		this.jobs = computeJobsDistribution();
	}

	/**
	 * Computes the steady-state probability distribution starting from p0.
	 */
	private final double[] computeProbabilitiesP0() {
		DoubleArrayList list = new DoubleArrayList(this.n);
		final double p0 = p0();
		list.add(p0);
		for (int i = 1; i <= n; i++) { // compute p1...pn
			double tmp = p0;
			for (int j = 1; j <= i; j++) {
				tmp *= rho / j; // compute rho^j / j!
			}
			// p0 * rho^j / j!
			list.add(tmp); // add p1...pn to the list
		}

		double pn = list.get(this.n); // pn is computed by itself, check
		assert (pn - this.pn <= 10E-6);

		// compute p_n+1... stop when the error is smaller than 10^-15
		double pj = 0.0;
		int j = n;

		double tmp = p0; // compute p0 * rho^n / n!
		for (int i = 1; i <= this.n; i++) {
			tmp *= rho / i;
		}
		do { // prod k=n+1...j (lam / (n*mu + (k-n)*theta) * tmp
			pj = tmp;
			j++;
			for (int k = n + 1; k <= j; k++) {
				pj *= this.lam / (this.n * this.mu + (k - this.n) * this.theta);
			}
			list.add(pj);
		} while (pj > ERR); // stop when prob. j is about 0

		return list.toArray();
	}

	/**
	 * Computes the steady-state jobs distribution.
	 */
	private final double[] computeJobsDistribution() {
		double[] res = new double[this.p.length];

		for (int i = 1; i < res.length; i++) {
			res[i] = i * this.p[i];
		}
		return res;
	}

	/**
	 * Gets the steady-state probability distribution.
	 * <p>
	 * The sum should be <i>approximately</i> 1 (apart from rounding errors).
	 * p[i] is the probability of being in state <i>i</i>
	 */
	public double[] getSteadyStateProbabilities() {
		return this.p;
	}

	/**
	 * Returns the steady-state jobs distribution.
	 */
	public double[] getSteadyStateJobsDistribution() {
		return this.jobs;
	}

	/**
	 * Computes the probability that the system is empty.
	 */
	private final double p0() {
		double tmp = 1.0;
		for (int i = 1; i <= n; i++) {
			tmp *= i / this.rho;
		}
		return this.pn * tmp;
	}

	/**
	 * Computes A(x, y) = (x e<sup>y</sup> / y<sup>x</sup>) * &gamma;(x,y) as 1
	 * + sum j=1...inf y<sup>j</sup> / prod k = 1...k (x+k)
	 */
	private static final double axy(double x, double y) {
		double res = 1.0;
		double tmp = 1.0;
		int j;
		for (j = 1; j <= MAX_ITERATIONS; j++) {
			tmp *= y / (x + j);
			res += tmp;
			if (tmp < ERR) {
				break;
			}
		}
		if (j > MAX_ITERATIONS) {
			System.err.printf("axy did not converge, found %.10f\n", res);
		}
		
		return res;
	}

	/**
	 * Computes the probability that all servers are busy and no jobs are
	 * waiting, i.e., the probability that there are exactly n jobs in the
	 * system.
	 */
	private final double pn() {
		final double erlangB = ErlangB.erlangB(this.n, this.rho);
		double tmp = 1.0 + erlangB * (this.axy - 1.0);
		return erlangB / tmp;
	}

	/**
	 * Computes the probability that a job will have to wait, P(W>0).
	 */
	public double waitingProbability() {
		return this.axy * this.pn;
	}

	/**
	 * Computes the abandonment probability of delayed jobs, P(Ab|W>0).
	 */
	public double abandonProbIfDelayed() {
		return (1.0 / (this.rho * this.axy)) + 1.0 - (1.0 / this.rho);
	}

	/**
	 * Computes the average waiting time of delayed jobs, E[W|W>0].
	 */
	public double meanWaitingIfDelayed() {
		return 1.0 / this.theta
				* (1.0 - (1.0 / this.rho) + 1.0 / (this.rho * this.axy));
	}

	/**
	 * Computes the probability that a job will abandon the system, P(Ab).
	 */
	public double abandonmentProbability() {
		return abandonProbIfDelayed() * waitingProbability();
	}

	/**
	 * Computes the average waiting time, E[W].
	 * <p>
	 * This value is computed as P(Ab) / &theta;. The same result can be found
	 * as E[W|W>0] * P(W>0).
	 */
	public double meanWaitingTime() {
		// we have P(ab) = theta * E[W]
		return abandonmentProbability() / this.theta;
	}

	/**
	 * Computes the steady-state average queue length, E[Q].
	 */
	public double avgQueueLen() {
		return this.lam * meanWaitingTime();
	}

	/**
	 * Computes the steady-state average number of jobs inside the system
	 * (either queueing or being served), E[L].
	 */
	public double getL() {
		double sum = 0.0;
		for (int i = 1; i < this.jobs.length; i++) {
			sum += this.jobs[i];
		}
		return sum;
	}

	/**
	 * Computes the system throughput, E[T].
	 */
	public double getThroughput() {
		return Math.min(this.n * this.mu, this.lam
				* (1.0 - abandonmentProbability()));
	}

	/**
	 * Computes the probability of being served of a job which, on arrival,
	 * finds all servers busy and <i>i</i> jobs in the queue, i.e., <i>n+i</i>
	 * jobs in the system.
	 * 
	 * @param i Number of jobs in the queue.
	 * @return The probability of being served.
	 * @throws IllegalArgumentException If i < 0.
	 */
	public double probService(int i) throws IllegalArgumentException {
		if (i < 0) {
			throw new IllegalArgumentException("");
		}
		double tmp = this.n * this.mu;
		return tmp / (tmp + this.theta * (i + 1));
	}

	/**
	 * /** Computes the probability that a job which, on arrival, finds all
	 * servers busy and <i>i</i> jobs in the queue, i.e., <i>n+i</i> jobs in the
	 * system, abandons the system.
	 * 
	 * @param i Number of jobs in the queue.
	 * @return The probability of abandonment.
	 * @throws IllegalArgumentException If i < 0.
	 */
	public double probAbandonment(int i) throws IllegalArgumentException {
		if (i < 0) {
			throw new IllegalArgumentException("");
		}
		return 1.0 - probAbandonment(i);
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int n = 20;
		double lam = 19.0;
		double mu = 1;
		double theta = 0.000001;
		ErlangA er = new ErlangA(n, lam, mu, theta);

		System.out.printf("P(W>0) %.10f\n", er.waitingProbability());
		// double[] p = er.getSteadyStateProbabilities();
		// for (int i = 0; i < p.length; i++) {
		// System.out.printf("%d %.10f\n", i, p[i]);
		// }
		System.out.printf("P(Ab) %.10f\n", er.abandonmentProbability());
		System.out.printf("E[W] %.10f\n", er.meanWaitingTime());
		System.out.printf("E[W|W>0] %.10f\n", er.meanWaitingIfDelayed());
		System.out.printf("E[Q] %.10f\n", er.avgQueueLen());
		System.out.printf("E[L] %.10f\n", er.getL());
		System.out.printf("Throughput %.10f\n", er.getThroughput());
	}
}
