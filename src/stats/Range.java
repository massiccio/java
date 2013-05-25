/*
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

/*
 * Range
 * 
 * @(#)file Range.java
 * @(#)author <a href="mailto:michelemazzucco@gmail.com">Michele Mazzucco</a>
 * @(#)version 0.1
 * @(#)created Jun 19, 2008
 * @(#)created Jun 19, 2008
 */

public interface Range {

	/**
	 * Gets the maximum value.
	 * 
	 * @return The maximum value.
	 */
	double getMax();


	/**
	 * Gets the minimum value.
	 * 
	 * @return The minimum value.
	 */
	double getMin();


	/**
	 * Gets the range, <i>i.e.</i> {@link #getMax()} - {@link #getMin()}.
	 * 
	 * @return The range of values.
	 */
	double getRange();

}