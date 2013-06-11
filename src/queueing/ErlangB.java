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

import math.GammaFunction;
import math.optimization.BisectionSearch;
import math.optimization.RootFunction;

/**
 * @author <a href="mailto:michelemazzucco@gmail.com">Michele Mazzucco</a>
 */
public class ErlangB {

	private ErlangB() {
		//
	}

	/**
	 * Computes the blocking probability of an Erlang-B queue with n trunks and
	 * traffic intensity load.
	 * 
	 * @return The blocking probability pn, 0.0 <= pn <= 1.0.
	 */
	public static final double erlangB(int n, double load) {
		double pn = 1.0;
		for (int i = 1; i <= n; i++) {
			pn = computeRecursive(i, load, pn);
		}
		return pn;
	}

	protected static final double computeRecursive(double n, double load,
		double pn_1) {
		return (load * pn_1) / (n + load * pn_1);
	}

	/**
	 * Computes the blocking probability of an Erlang-B queue with n trunks and
	 * traffic intensity load.
	 * <p>
	 * n is not integer.
	 * 
	 * @param n The number of servers.
	 * @param load The offered load.
	 * @return The blocking probability pn, 0.0 <= pn <= 1.0
	 * @see Jerzy Kubasik, Eq. 8 of <a href=
	 *      "http://www.i-teletraffic.org/fileadmin/ITCBibDatabase/1985/kubasik852.pdf"
	 *      >On some numerical methods for the computation of Erlang and Engset
	 *      functions</a>
	 */
	public static final double erlangBNonInt(double n, double load) {
		if (load == 0.0) {
			return 0.0;
		}
		if (n == 0.0) {
			return 1.0;
		}
		final int nInt = (int) Math.floor(n);
		if (nInt == n) {
			return erlangB(nInt, load);
		}

		// first part of (8), e^ load / load^(-n)
		double tmp = Math.exp(load * Math.log(Math.E) - n * Math.log(load));
		// see http://mathworld.wolfram.com/RegularizedGammaFunction.html
		// so either incompleteGammaComplement(n+1.0, load) * gamma(n+1.0);, or
		double tmp1 = GammaFunction.upperIncomplete(n + 1.0, load);
		tmp *= tmp1;
		return 1.0 / tmp;
	}

	/**
	 * Computes the blocking probability of an Erlang-B queue with n trunks and
	 * traffic intensity load.
	 * <p>
	 * This routine employs the approximation of the Erlang loss formula in a
	 * continuos form. The algorithm uses the same recursive scheme as the one
	 * dealing with an integer number of servers.
	 * 
	 * @param n The number of servers.
	 * @param load The offered load.
	 * @return The blocking probability pn, 0.0 <= pn <= 1.0
	 * @see Eq. 4 and 5 of
	 *      "Modeling of systems with overlfow multi-rate traffic".
	 */
	public static final double erlangBApprox(double n, double load) {
		int nInt = (int) Math.floor(n);
		if (nInt == n) {
			return erlangB(nInt, load);
		}

		double s = n - nInt;

		double numerator = (2.0 - s) * load + load * load;
		double denominator = s + 2 * load + load * load;
		double tmp = numerator / denominator; // eq. 5

		// eq. 4
		double pn = tmp;
		for (int i = 1; i <= nInt; i++) {
			pn = computeRecursive(i + s, load, pn);
		}
		return pn;
	}

	/**
	 * Computes the blocking probability of an Erlang-B queue with n trunks and
	 * traffic intensity load in closed form using Rapp approximation (which
	 * employs a parabola)
	 * <p>
	 * This routine employs the approximates of the Erlang loss formula by a
	 * parabola using Rapp's algorithm:
	 * <p>
	 * E(n, load) = c<sub>0</sub> - c<sub>1</sub> n + c<sub>2</sub>
	 * n<sup>2</sup> where <lu>
	 * <li>c<sub>0</sub> = 1
	 * <li>c<sub>1</sub> = (load+2) + ((1+load)<sup>2</sup> + load)
	 * <li>c<sub>2</sub> = 1 / ((1 + load) * ((1+load)<sup>2</sup> + load))
	 * </lu>
	 * 
	 * @param n The number of servers.
	 * @param load The offered load.
	 * @return The blocking probability pn, 0.0 <= pn <= 1.0
	 */
	public static final double rappAprrox(double n, double load) {
		double c0 = 1d;

		double tmp = (1d + load) * (1d + load);
		double c1 = -((2d + load) / (tmp + load));
		double c2 = 1d / ((1d + load) * (tmp + load));
		final double res = c0 + (c1 * n) + (c2 * (n * n));
		return res;
	}

	/**
	 * Finds the minimum number of servers which are capable of serving the
	 * offered traffic with the given grade of service.
	 * 
	 * @param load The offered load.
	 * @param blockingProb The maximum desired blocking probability.
	 * @return The minimum number of servers necessary
	 */
	public static int findMinServers(double load, double blockingProb) {
		// since the Erlang-B formula is convex for n > 1, we might use
		// the bisection (binary search) method. However it is more convenient
		// to apply the recursive formula

		if ((blockingProb == 1.0) || (load == 0.0)) {
			return 0;
		}

		double pn = 1.0;
		int n = 0;
		while (pn > blockingProb) {
			n++;
			pn = computeRecursive(n, load, pn);
		}
		return n;
	}

	/**
	 * Computes the maximum amount of traffic that can be allowed, given the
	 * amount of servers and blocking probability.
	 * 
	 * @param n The number of servers.
	 * @param blockingProb The blocking probability.
	 * @return The maximum amount of traffic.
	 */
	public static double findMaxLoad(final int n, final double blockingProb) {
		ErlangBFunction fun = new ErlangBFunction(n, blockingProb);

		return BisectionSearch.bisect(fun, 0.0, n);
	}

	public static void main(String[] args) {
		double n = 2.3;
		double load = 2.1;
		// System.out.println(erlangBOld(5, 4));
		// System.out.println(erlangB(5, 4));
		// System.out.println(findMinServers(4, 0.1999));
		System.out.println(erlangBApprox(n, load));
		System.out.println(erlangBNonInt(n, load));
		System.out.println(rappAprrox(n, load));

		// double max = findMaxLoad(10, 0.4);
		// System.out.println(max);
		// System.out.println(erlangB(10, max));
	}

	static class ErlangBFunction implements RootFunction {

		private final int n; // no. of servers
		private final double pn; // blocking probability

		ErlangBFunction(int n, double pn) {
			this.n = n;
			this.pn = pn;
		}

		public double evaluate(double x) {
			return this.pn - erlangB(n, x);
		}
	}
}
