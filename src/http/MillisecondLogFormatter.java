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
 * @(#)file MillisecondLogFormatter.java
 * @(#)author Michele Mazzucco
 * @(#)version 0.1
 * @(#)created Mar 30, 2012
 * @(#)lastedit Mar 30, 2012
 */

package http;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Formatter that adds the date with millisecond precision to logs.
 * <p>
 * Example:
 * <p>
 * 01:46:46.624 - [http.MillisecondLogFormatter.main] - [SEVERE] - A severe
 * message.
 * 
 * @author <a href="mailto:michelemazzucco@gmail.com">Michele Mazzucco</a>
 * 
 */
public class MillisecondLogFormatter extends Formatter {

	// Create a DateFormat to format the logger timestamp.
	private static final DateFormat df = new SimpleDateFormat("hh:mm:ss.SSS");

	private Date dat;

	/**
	 * Constructor
	 */
	public MillisecondLogFormatter() {
		super();
		dat = new Date();
	}

	/**
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	@Override
	public String format(LogRecord record) {
		StringBuilder builder = new StringBuilder(1000);
		dat.setTime(record.getMillis());
		builder.append(df.format(dat)).append(" - ");
		builder.append("[").append(record.getSourceClassName()).append(".");
		builder.append(record.getSourceMethodName()).append("] - ");
		builder.append("[").append(record.getLevel()).append("] - ");
		builder.append(formatMessage(record));
		builder.append("\n");
		return builder.toString();
	}

	/**
	 * Example.
	 */
	public static void main(String[] args) {
		Logger logger = Logger.getLogger(MillisecondLogFormatter.class
				.getName());
		logger.setUseParentHandlers(false);

		MillisecondLogFormatter formatter = new MillisecondLogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(formatter);

		logger.addHandler(handler);
		logger.info("Example of creating custom formatter.");
		logger.warning("A warning message.");
		logger.severe("A severe message.");
	}

}
