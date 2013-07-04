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

import stats.NormalDistribution;
import utils.DoubleArrayList;

/**
 * Functions for the Erlang-C model (M/M/n queue).
 * <p>
 * Exact routines are provided for computing the blocking probability, the
 * average waiting time as well as the minimum number of servers necessary to
 * handle a certain load with a certain blocking probability or average waiting
 * time.
 * <p>
 * Approximation algorithms are provided to estimate the blocking probability in
 * closed form, the average waiting time in closed form, and the average waiting
 * time for GI/G/n queues.
 * <p>
 * All methods throw {@link IllegalArgumentException} if the system is unstable.
 */
public class ErlangC {

	private static final double ERR = 10E-15;

	/** Number of servers. */
	public final int n;

	/** The arrival rate. */
	public final double lam;

	/** The average service time. */
	public final double b;

	/** Load (&rho; = &lambda; / &mu;). */
	public final double load;

	/**
	 * Creates a new M/M/n queue with the specified arguments.
	 * 
	 * @param n The number of servers.
	 * @param lam The arrival rate.
	 * @param b The average service time.
	 * @throws IllegalArgumentException If lam * b >= n.
	 */
	public ErlangC(int n, double lam, double b) throws IllegalArgumentException {
		this.n = n;
		this.lam = lam;
		this.b = b;
		load = lam * b;
		checkLoad(n, load);
		// p = computeProbabilities();
		// L = computeL();
	}

	private static final void checkLoad(double n, double load)
		throws IllegalArgumentException {
		if (load >= n) {
			throw new IllegalArgumentException("The system is overloaded");
		}
	}

	/**
	 * Computes the probability that all servers of an M/M/n queue with n trunks
	 * and traffic intensity load are busy (i.e., the probability that a job
	 * will have to wait).
	 * 
	 * @return The probability pn that a job will have to wait, 0.0 <= pn <= 1.0
	 */
	public static final double erlangC(int n, double load) {
		checkLoad(n, load);
		double pn = 0.0;
		if (load > 0.0) {
			double B = ErlangB.erlangB(n, load);
			pn = n * B / (n - load * (1 - B));
		}
		return pn;
	}

	/**
	 * Closed form approximation of the blocking probability in an M/M/n queue
	 * with n servers and traffic intensity load.
	 * <p>
	 * This algorithm is the implementation of Equation 2.3 in S. Halfin and W.
	 * Whitt, "Heavy-Traffic Limits for Queues with Many Exponential Servers",
	 * Operations Research, vol. 29, May-June 1981
	 * <p>
	 * <strong>Note: this approximation is not very good when the number of
	 * servers is small.</strong>
	 * 
	 * @see <a
	 *      href="http://www.columbia.edu/~ww2040/HalfinWW1981.pdf">Heavy-Traffic
	 *      Limits for Queues with Many Exponential Servers</a>
	 */
	public static final double erlangCApprox(double n, double load) {
		checkLoad(n, load);
		double eta = (1.0 - (load / n)) * Math.sqrt(n);
		final double phi = NormalDistribution.Phi(eta);

		return 1.0 / (1.0 + Math.sqrt(2d * Math.PI) * eta * phi
			* Math.exp((eta * eta) / 2.0));
	}

	/**
	 * Finds the minimum number of servers which are capable of serving the
	 * offered traffic with the given grade of service.
	 * 
	 * @param waitProb The probability that a job will have to wait.
	 * @return The minimum number of servers necessary
	 */
	public int findMinServersBlocking(double waitProb) {
		if ((waitProb == 1.0) || (load == 0.0)) {
			return 0;
		}

		double pn = 1.0; // blocking prob. Erlang C
		double B = 1.0; // blocking prob. Erlang B
		int n = 0;
		while (pn > waitProb) {
			n++;
			B = ErlangB.computeRecursive(n, load, B);
			pn = n * B / (n - load * (1 - B));
		}
		return n;
	}

	/**
	 * Determines the minimum number of servers necessary to handle the given
	 * load with the desired average waiting time.
	 * 
	 * @param lam The arrival rate.
	 * @param b The average service time.
	 * @param avgWait The average waiting time.
	 * @return The minimum number of servers necessary
	 */
	public int findMinServersWait(double avgWait) {
		if ((avgWait == 1.0) || (this.load == 0.0)) {
			return 0;
		}

		int n = (int) Math.ceil(load); // min number of servers necesasry to
										// ensure the system is stable
		double B = ErlangB.erlangB(n, load); // blocking prob. Erlang B
		double wait = Double.MAX_VALUE;
		// it is possible to employ bisection search here, but this while loop
		// is likely to be faster, as we are reducing the number of times
		// ErlangB is called
		while (wait > avgWait) {
			n++;
			B = ErlangB.computeRecursive(n, load, B);
			double pn = n * B / (n - load * (1 - B)); // blocking prob. M/M/n
			double mu = 1 / b;
			wait = pn / (n * mu - lam);
		}
		return n;
	}

	/**
	 * Determines the average waiting time, given the number of servers, average
	 * arrival rate and average service time.
	 * 
	 * @return The average waiting time.
	 */
	public final double avgWait() {
		return avgWait(this.n, this.lam, this.b);
	}

	/**
	 * Determines the average waiting time, given the number of servers, average
	 * arrival rate and average service time.
	 * 
	 * @return The average waiting time.
	 */
	public static double avgWait(int n, double lam, double b) {
		final double load = lam * b;
		double pn = erlangC(n, load); // blocking probability
		double mu = 1.0 / b;
		double w = pn / (n * mu - lam);
		return w;
	}

	/**
	 * Computes the average number of jobs in the system when the lam * b
	 * Erlangs are offered to n trunks.
	 */
	public static final double getL(int n, double lam, double b) {
		final double load = lam * b;
		return (load / (n - load)) * erlangC(n, load) + load;
		// could use also Little's Law: L= lam * getW()
		// return lam * getAvgResp(n, lam, b);
	}

	/**
	 * Computes the average response time when the lam * b Erlangs are offered
	 * to n trunks using the formula w + b, where w is the average waiting time.
	 * 
	 * @see #avgWait(int, double, double)
	 */
	public final double avgResp() {
		return avgWait() + this.b;
	}

	/**
	 * Determines the average waiting time in a GI/G/n queue, given the number
	 * of servers, average arrival rate, average service time, and squared
	 * coefficients of variation (variance over the square of the mean) of
	 * interarrival intervals and service times.
	 * <p>
	 * This routine implements the approximation algorithm described in W.
	 * Whitt, "Approximations for the GI/G/m Queue", Production and Operations
	 * Management, Vol. 2, No. 2, pp. 114-161, 1993.
	 * 
	 * @param n The number of servers.
	 * @param lam The average arrival rate.
	 * @param b The average service time.
	 * @param ca2 The squared coefficient of variation of the interarrival
	 *            intervals.
	 * @param ca2 The squared coefficient of variation of the service times.
	 * @return The average waiting time.
	 * @see "Approximations for the GI/G/m Queue"
	 */
	public static final double ggnAvgWait(int n, double lam, double b,
		double ca2, double cs2) {
		return avgWait(n, lam, b) * ((ca2 + cs2) / 2.0);
	}

	/**
	 * Computes the average waiting time in closed form for the given
	 * parameters.
	 * <p>
	 * This version comes from <i>Queueing models of computer systems</i> (eq. D
	 * and E).
	 * <p>
	 * A.O. Allen, "Queueing Models of Computer Systems," Computer, vol. 13, no.
	 * 4, pp. 13-24, Apr. 1980
	 * 
	 * @param lam The arrival rate.
	 * @param b The average service time.
	 * @param n The number of servers.
	 * @return The estimated average waiting time for the given parameters.
	 */
	public static double avgWaitApprox(int n, double lam, double b) {
		final double load = lam * b;
		final double pn = erlangCApprox(n, load);

		return pn * b / (n - load);
	}

	/**
	 * Computes the stationary distribution of the system being in state
	 * <i>i</i>, i = 0...inf.
	 * <p>
	 * This algorithm computes the probabilities with an accuracy of 10^-9.
	 * 
	 * @see #ERR
	 */
	private final double[] computeProbabilities() {
		// most likely state
		final int rhoCeiled = (int) (Math.ceil(this.load) + 0.5d);
		// stop when prob j is "small"
		DoubleArrayList p = new DoubleArrayList(rhoCeiled + 1);

		for (int i = 0; i < rhoCeiled; i++) {
			p.add(i, 0.0);
		}
		p.add(rhoCeiled, 1.0);
		double sum = 1.0;

		// solve from p[rhoCeiled -1]...p[0] and p[rhoCeiled+1]...p[k]
		// p[rhoCeiled-1]...p[0]
		for (int j = rhoCeiled - 1; j >= 0; j--) {
			double pj = (j + 1) * p.get(j + 1) / load;
			p.set(j, pj);
			sum += pj;
		}

		// solve for p[rhoCeiled+1]... Stop when p[j] becomes "small"
		int j = rhoCeiled + 1;
		double pj;
		do {
			pj = 0.0;
			if (j < n) {
				pj = p.get(j - 1) * load / j;
			} else {
				pj = p.get(j - 1) * load / n;
			}
			p.add(j, pj);
			sum += pj;
			j++;
		} while (pj >= ERR);

		double[] probs = p.toArray();
		// normalize
		for (int i = 0; i < probs.length; i++) {
			probs[i] /= sum;
		}
		return probs;
	}

	/**
	 * Gets the average number of jobs in the system.
	 * <p>
	 * This method computes the steady-state average number of jobs in the
	 * system (waiting and being served) from the stationary distribution of
	 * number of jobs in the system. The correctness of the returned result can
	 * be verified using Little's Law, e.g., L = lam * W, where W is the average
	 * response time.
	 */
	public final double getL() {
		return getL(this.n, this.lam, this.b);
	}

	/**
	 * Gets the array of probabilities.
	 * 
	 * @return An array of probabilities. The sum should be <i>approximately</i>
	 *         1 (apart from rounding errors). p[i] is the probability of being
	 *         in state <i>i</i>
	 */
	public final double[] getSteadyStateProbabilities() {
		return computeProbabilities();
	}

	public static void main(String[] args) {
		final int n = 10;
		final double lam = 8.0;
		final double b = 1.0;
		// System.out.println(getL(n, lam, b));
		// System.out.println(meanWaitingTime(n, lam, b));
		// avg. resp. time

		ErlangC erlangC = new ErlangC(n, lam, b);
		double W = erlangC.avgResp();
		// System.out.println(W);

		// no. jobs in system -- little's law
		double L = W * lam;
		System.out.println(L + " " + erlangC.getL());

		System.out.println(erlangC.getL());
		System.out.println(lam * erlangC.avgResp());
	}

}
