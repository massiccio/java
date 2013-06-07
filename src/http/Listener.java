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
 * @(#)file Listener.java
 * @(#)author Michele Mazzucco
 * @(#)version 0.1
 * @(#)created Mar 29, 2012
 * @(#)lastedit Mar 29, 2012
 */

package http;

/**
 * Observer pattern.
 */
public interface Listener {
	
	/**
	 * Notifies this listener that the Download object specified as argument
	 * has completed.
	 * 
	 * @param download The completed request.
	 */
    public void done(Download download);

    /**
	 * Notifies this listener that the Download object specified as argument
	 * has completed abnormally.
	 * 
	 * @param download The request.
	 * @param throwable The exception.
	 */
    public void error(Download download, Throwable throwable);
}