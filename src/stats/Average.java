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
 * Average
 * 
 * @(#)file Average.java
 * @(#)author <a href="mailto:michelemazzucco@gmail.com">Michele Mazzucco</a>
 * @(#)version 0.1
 * @(#)created Jun 19, 2008
 * @(#)lastedit May 25, 2013
 */


/**
 * Utility allowing one to compute the arithmetic mean, maximum, minimum, sum,
 * and range of a set of values.
 * <p>
 * Values can be added to class container using the {@link #add()} method. The
 * state can be reset by means of the {@link #reset()} method.
 * 
 */
public class Average extends Counter {

	protected double aggregate;
	
	private double max;
	
	private double min;

	public Average() {
		super();
		reset();
	}

	/**
	 * Resets the internal state of this object.
	 */
	public void reset() {
		super.reset();
		aggregate = 0L;
		max = 0.d;
		min = 0.d;
	}

	/**
	 * Adds the specified value.
	 * 
	 * @param value
	 *            The value to add.
	 */
	public void add(double value) {
		super.increment();
		this.aggregate += value;

		if (super.counter == 1L) { // Added to prevent miscalculations with minimum
								// and maximum.
			this.min = value;
			this.max = value;
		} else {
			this.max = value > this.max ? value : this.max;
			this.min = value < this.min ? value : this.min;
		}
	}

	/**
	 * Calculates average of all recorded Values
	 * 
	 * @return Average as double
	 * */
	public double mean() {
		if (super.counter == 0L) {
			return 0.d;
		}
		return (this.aggregate / super.counter);
	}

	public double sum() {
		if (super.counter == 0L) {
			return 0.d;
		}
		return this.aggregate;
	}

	/**
	 * Returns number of recorded values.
	 * 
	 * @return Size of range as long.
	 */
	public final long size() {
		return super.getCounter();
	}

	public final double getMax() {
		return this.max;
	}
	
	public final double getAggregate() {
		return this.aggregate;
	}
	

	public final double getMin() {
		return this.min;
	}

	public final double getRange() {
		return getMax() - getMin();
	}
}