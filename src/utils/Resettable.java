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
 * QoSP project.
 * 
 * @(#)file Resettable.java
 * @(#)author Michele Mazzucco
 * @(#)version 0.1
 * @(#)created 12 Oct 2007
 * @(#)lastedit 12 Oct 2007
 */
package utils;

/**
 * The <code>Resettable</code> interface allows to reset the internal state
 * of an object.
 * 
 * @author <a href="mailto:Michele.Mazzucco@ncl.ac.uk">Michele Mazzucco</a>
 */
public interface Resettable {

    /**
     * Resets the internal state of this object.
     */
    void reset();
}
