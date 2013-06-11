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
 * @(#)file DownloadImpl.java
 * @(#)author Michele Mazzucco
 * @(#)version 0.1
 * @(#)created Mar 29, 2012
 * @(#)lastedit Mar 29, 2012
 */

package http;

import java.nio.ByteBuffer;

/**
 * This class represents a download.
 * <p>
 * This class is not thread safe. The state of this class is changed only by the
 * {@linkplain Crawler} in charge of the download.
 * <p>
 * In order to reduce the number of memory allocations, the buffer used to store
 * downloaded data is initialized to 300 KB by default. The
 * {@link #DownloadImpl(String, int, String, Listener, int)} constructor allows
 * to specify the buffer size. This way one can decrease the memory wastage
 * (i.e., if the downloads are small) at the expenses of slightly higher CPU
 * consumption (as buffer reallocations will occur more frequently).
 */
class DownloadImpl implements Download {

	private static final int BUFFER_SIZE = 300 * 1024;

	final String host; // Final fields are immutable for thread-saftey

	final int port;

	final String path;

	final Listener listener;

	private volatile Status status; // Volatile fields may be changed
									// concurrently

	private volatile byte[] data; // array where data is written
	private volatile int bytesWritten; // number of bytes written

	/** Timestamp of CONNECTED event. */
	private volatile long start;

	/** Timestap of DONE event or ERROR. */
	private volatile long stop;

	/**
	 * Creates a new object with the specified state and buffer initialized at
	 * 300 KB.
	 * 
	 * @param host The host.
	 * @param port The port.
	 * @param path The path.
	 * @param listener Listener, for observer pattern (can be null).
	 */
	DownloadImpl(String host, int port, String path, Listener listener) {
		this(host, port, path, listener, BUFFER_SIZE);
	}

	/**
	 * Creates a new object with the specified state.
	 * 
	 * @param host The host.
	 * @param port The port.
	 * @param path The path.
	 * @param bufferSize The buffer size for downloads.
	 * @param listener Listener, for observer pattern (can be null).
	 */
	DownloadImpl(String host, int port, String path, Listener listener,
			int bufferSize) {
		this.host = host;
		this.port = port;
		this.path = path;
		this.listener = listener;
		this.status = Status.UNCONNECTED; // Set initial status
		data = new byte[bufferSize];
		bytesWritten = 0;
	}

	// These are the basic getter methods
	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getPath() {
		return path;
	}

	public Status getStatus() {
		return status;
	}

	/**
	 * @see Download#setStatus(LoadGeneratorAsync.Status)
	 */
	@Override
	public void setStatus(Status status) {
		if (status == Status.CONNECTED) {
			this.start = System.currentTimeMillis();
		} else if (status == Status.ERROR || status == Status.DONE) {
			this.stop = System.currentTimeMillis();
		}

		this.status = status;
	}

	/**
	 * @see Download#getListener()
	 */
	@Override
	public Listener getListener() {
		return this.listener;
	}

	public byte[] getData() {
		return data;
	}

	/**
	 * Gets the response time experienced by this download, in milliseconds
	 * precision.
	 */
	public long getResponseTime() {
		return this.stop - this.start;
	}

	/**
	 * Gets the amount of bytes downloaded.
	 */
	public int getDataLength() {
		return this.bytesWritten;
	}

	/**
	 * Return the HTTP status code for the download. Throws
	 * IllegalStateException if status is not Status.DONE
	 */
	public int getHttpStatus() {
		if (status != Status.DONE)
			throw new IllegalStateException("Connection status: "
					+ status.toString());
		// In HTTP 1.1, the return code is in ASCII bytes 10-12.
		return (data[9] - '0') * 100 + (data[10] - '0') * 10 + (data[11] - '0')
				* 1;
	}

	/**
	 * Adds data to the internal buffer. Resizes the buffer if it is necessary.
	 */
	public void addData(ByteBuffer buffer) {
		if (status != Status.CONNECTED) { // only called during download
			throw new IllegalStateException("Download in " + status.toString()
					+ " status");
		}

		int oldlen = data.length; // How many existing bytes
		int numbytes = buffer.remaining(); // How many new bytes
		final int written = this.bytesWritten; // how many bytes are already in
												// the buffer
		if (numbytes > oldlen - written) {
			// create new buffer
			int newlen = (oldlen + Math.max(BUFFER_SIZE, numbytes));
			byte[] newdata = new byte[newlen]; // Create new array
			System.arraycopy(data, 0, newdata, 0, oldlen); // Copy old bytes
			buffer.get(newdata, oldlen, numbytes); // Copy new bytes
			data = newdata; // Save new array
		} else {
			// the current buffer is enough to hold the new data
			buffer.get(this.data, written, numbytes);
		}
		this.bytesWritten += numbytes;
	}
}