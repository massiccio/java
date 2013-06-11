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

package http;


/**
 * @(#)file Status.java
 * @(#)author Michele Mazzucco
 * @(#)version 0.1
 * @(#)created Mar 29, 2012
 * @(#)lastedit Mar 29, 2012
 */

/**
 * Download status.
 */
public class Status {
    
	/** We haven't connected to the server yet. */
    public static final Status UNCONNECTED = new Status("Unconnected");

    /** We're connected to the server, sending request or receiving response. */
    public static final Status CONNECTED = new Status("Connected");

    /** Response has been received. Response may have been an HTTP error. */
    public static final Status DONE = new Status("Done");

    /** Something went wrong: bad hostname, for example. */
    public static final Status ERROR = new Status("Error");

    private final String name;

    private Status(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}