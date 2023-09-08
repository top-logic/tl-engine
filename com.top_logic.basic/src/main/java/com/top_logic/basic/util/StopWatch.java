/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.util.concurrent.TimeUnit;

/**
 * Utility to measure timings with {@link System#nanoTime()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StopWatch {

	private boolean _running = false;
	private long _nanos = 0L;

	/**
	 * Starts the watch.
	 */
	public StopWatch start() {
		if (_running) {
			throw new IllegalStateException("Already started.");
		}
		_nanos -= System.nanoTime();
		_running = true;
		return this;
	}

	/**
	 * Stops the watch.
	 */
	public StopWatch stop() {
		if (! _running) {
			throw new IllegalStateException("Not started.");
		}
		_nanos += System.nanoTime();
		_running = false;
		return this;
	}
	
	/**
	 * Resets the watch to stopped, showing zero time. 
	 */
	public StopWatch reset() {
		_running = false;
		_nanos = 0L;
		return this;
	}

	/**
	 * {@link #reset()} and {@link #start()} this {@link StopWatch}.
	 */
	public StopWatch restart() {
		return reset().start();
	}

	/**
	 * Time in <b>nano</b>seconds between {@link #start()} and {@link #stop()}.
	 * 
	 * Please use {@link #getElapsedNanos()} instead, as its name is more clear.
	 */
	public long getElapsed() {
		return getElapsedNanos();
	}

	/**
	 * Time in nanoseconds between {@link #start()} and {@link #stop()}.
	 */
	public long getElapsedNanos() {
		if (_running) {
			return _nanos + System.nanoTime();
		} else {
			return _nanos;
		}
	}

	/**
	 * Time in milliseconds between {@link #start()} and {@link #stop()}.
	 */
	public long getElapsedMillis() {
		if (_running) {
			return (_nanos + System.nanoTime()) / (1000 * 1000);
		} else {
			return _nanos / (1000 * 1000);
		}
	}
	
	@Override
	public String toString() {
		return toStringNanos(getElapsedNanos());
	}
	
	/**
	 * Creates a {@link #start() started} {@link StopWatch}.
	 */
	public static StopWatch createStartedWatch() {
		return new StopWatch().start();
	}

	/**
	 * Convert time in milliseconds to a human readable representation.
	 * 
	 * @param millis
	 *        elapsed time in milliseconds
	 * @return a human readable representation of that time.
	 * 
	 * @see #toStringMillis(long, TimeUnit)
	 */
	public static String toStringMillis(long millis) {
		return toStringMillis(millis, TimeUnit.NANOSECONDS);
	}

	/**
	 * Convert time in milliseconds to a human readable representation.
	 * 
	 * @param millis
	 *        elapsed time in milliseconds
	 * @param precision
	 *        Definition of the displayed precision, e.g if <code>precision</code> is "minute", then
	 *        seconds and milliseconds are not display.
	 * @return a human readable representation of that time.
	 */
	public static String toStringMillis(long millis, TimeUnit precision) {
		return toStringNanos(millis * 1000 * 1000, precision);
	}

	/**
	 * Convert time in nanoseconds to a human readable representation.
	 * 
	 * @param nanos
	 *        elapsed time in nanoseconds as given by a difference between two calls to
	 *        {@link System#nanoTime()}
	 * @return a human readable representation of that time.
	 * 
	 * @see #toStringNanos(long, TimeUnit)
	 */
	public static String toStringNanos(long nanos) {
		return toStringNanos(nanos, TimeUnit.NANOSECONDS);
	}

	/**
	 * Convert time in nanoseconds to a human readable representation.
	 * 
	 * @param nanos
	 *        elapsed time in nanoseconds as given by a difference between two calls to
	 *        {@link System#nanoTime()}
	 * @param precision
	 *        Definition of the displayed precision, e.g if <code>precision</code> is "minute", then
	 *        seconds, milliseconds or nanos are not display.
	 * @return a human readable representation of that time.
	 */
	public static String toStringNanos(long nanos, TimeUnit precision) {
		// Split nanoseconds into days, hours, minutes, seconds, milliseconds and
		// nanoseconds.
		long millis = nanos / 1000000;
		long seconds = millis / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		nanos -= millis * 1000000;
		millis -= seconds * 1000;
		seconds -= minutes * 60;
		minutes -= hours * 60;
		hours -= days * 24;
		
		boolean hasDays = days > 0;
		boolean hasHours = hours > 0;
		boolean hasMinutes = minutes > 0;
		boolean hasSeconds = seconds > 0;
		boolean hasNanos = nanos > 0;
		boolean hasMillis = millis > 0;
		
		// The outputXxxx flag is true, if the current unit is non-null or a
		// greater unit is non-null.
		boolean outputHours = hasDays || hasHours;
		boolean outputMinutes = hasHours || hasMinutes;
		boolean outputSeconds = hasMinutes || hasSeconds;
		
		// The requireXxx flag is true, if the current unit is non-null, or
		// there is a greater non-null unit and not all smaller units are null.
		boolean requireMillis = hasMillis || hasNanos;
		boolean requireSeconds = hasSeconds || (outputSeconds && requireMillis);
		boolean requireMinutes = hasMinutes || (outputMinutes && requireSeconds);
		boolean requireHours = hasHours || (outputHours && requireMinutes);
		boolean requireDays = hasDays && TimeUnit.DAYS.compareTo(precision) >= 0;
		
		StringBuffer result = new StringBuffer(64);
	    boolean output = false;
		if (requireDays && TimeUnit.DAYS.compareTo(precision) >= 0) {
	        result.append(days);
	        result.append(" d");
	        output = true;
	    }
		if (requireHours && TimeUnit.HOURS.compareTo(precision) >= 0) {
	    	if (output) result.append(' ');
	        result.append(hours);
	        result.append(" h");
	        output = true;
	    }
		if (requireMinutes && TimeUnit.MINUTES.compareTo(precision) >= 0) {
	    	if (output) result.append(' ');
	        result.append(minutes);
	        result.append(" min");
	        output = true;
	    }
		if (requireSeconds && TimeUnit.SECONDS.compareTo(precision) >= 0) {
	    	if (output) result.append(' ');
	    	result.append(seconds);
	    	result.append(" s");
	    	output = true;
	    }
		if (requireMillis && TimeUnit.MILLISECONDS.compareTo(precision) >= 0) {
	    	if (output) result.append(' ');
	    	result.append(millis);
			if (hasNanos && TimeUnit.NANOSECONDS.compareTo(precision) >= 0) {
	    		result.append('.');
	    		String nanoString = Long.toString(1000000 + nanos).substring(1);
				result.append(nanoString);
	    	}
	    	result.append(" ms");
	    	output = true;
	    }
	    return result.toString();
	}

}
