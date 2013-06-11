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
 * @(#)file Download.java
 * @(#)author Michele Mazzucco
 * @(#)version 0.1
 * @(#)created Mar 29, 2012
 * @(#)lastedit Mar 29, 2012
 */

package http;

import java.nio.ByteBuffer;


public interface Download {
	
	/**
	 * Hostname we're downloading from.
	 */
    public String getHost();

    public int getPort(); // Defaults to port 80

    public String getPath(); // includes query string as well

    /**
     * Gets the status of the download.
     */
    public Status getStatus();

    /**
     * Sets the status of the download.
     */
    void setStatus(Status status);

    /**
     * Download data, including response headers.
     */
    public byte[] getData();

    /**
     * Gets the HTTP status.
     * Only call when status is DONE.
     */
    public int getHttpStatus();

    /**
     * Gets the attached listener, if any, null otherwise.
     */
    Listener getListener();

    /**
     * Reads more data.
     */
    void addData(ByteBuffer buffer);
}