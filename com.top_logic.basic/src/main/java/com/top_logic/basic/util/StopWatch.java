/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/**
 * Utility to measure elapsed time.
 * 
 * <p>
 * A {@link StopWatch} reads its time from a clock delivering nanoseconds. By default, this is the
 * wall clock {@link System#nanoTime()}. A watch created with {@link #createThreadCpuWatch()}
 * measures the CPU time of the current thread instead, see
 * {@link ThreadMXBean#getCurrentThreadCpuTime()}. Other clocks can be passed to
 * {@link #StopWatch(LongSupplier)}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StopWatch {

	private final LongSupplier _clock;

	private final boolean _threadCpuTime;

	private boolean _running = false;
	private long _nanos = 0L;

	/**
	 * Creates a stopped {@link StopWatch} measuring wall-clock time with {@link System#nanoTime()}.
	 */
	public StopWatch() {
		this(System::nanoTime);
	}

	/**
	 * Creates a stopped {@link StopWatch} reading its time from the given clock.
	 * 
	 * @param clock
	 *        The time source delivering nanoseconds. Only differences between two values of the
	 *        clock are evaluated, the origin is arbitrary.
	 */
	public StopWatch(LongSupplier clock) {
		this(clock, false);
	}

	private StopWatch(LongSupplier clock, boolean threadCpuTime) {
		_clock = clock;
		_threadCpuTime = threadCpuTime;
	}

	/**
	 * Whether this watch measures the CPU time of the current thread.
	 * 
	 * <p>
	 * This is the case for a watch created with {@link #createThreadCpuWatch()}, if the JVM supports
	 * measuring thread CPU time. Otherwise, the watch measures wall-clock time or the time of the
	 * clock passed to {@link #StopWatch(LongSupplier)}.
	 * </p>
	 */
	public boolean isThreadCpuTime() {
		return _threadCpuTime;
	}

	/**
	 * Starts the watch.
	 */
	public StopWatch start() {
		if (_running) {
			throw new IllegalStateException("Already started.");
		}
		_nanos -= _clock.getAsLong();
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
		_nanos += _clock.getAsLong();
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
			return _nanos + _clock.getAsLong();
		} else {
			return _nanos;
		}
	}

	/**
	 * Time in milliseconds between {@link #start()} and {@link #stop()}.
	 */
	public long getElapsedMillis() {
		if (_running) {
			return (_nanos + _clock.getAsLong()) / (1000 * 1000);
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
	 * Creates a stopped {@link StopWatch} measuring the CPU time of the current thread.
	 * 
	 * <p>
	 * The CPU time of a thread excludes the time the thread waits for a free CPU on a loaded
	 * machine, and the work done by other threads. Therefore, the watch must be started, stopped,
	 * and read on the same thread.
	 * </p>
	 * 
	 * <p>
	 * If the JVM does not support measuring the CPU time of the current thread, or thread CPU time
	 * measurement is disabled and cannot be enabled, the watch measures wall-clock time with
	 * {@link System#nanoTime()}. {@link #isThreadCpuTime()} tells which clock is used.
	 * </p>
	 * 
	 * @see ThreadMXBean#getCurrentThreadCpuTime()
	 */
	public static StopWatch createThreadCpuWatch() {
		ThreadMXBean threads = ManagementFactory.getThreadMXBean();
		if (enableThreadCpuTime(threads)) {
			return new StopWatch(threads::getCurrentThreadCpuTime, true);
		}
		return new StopWatch();
	}

	/**
	 * Creates a {@link #start() started} {@link StopWatch} measuring the CPU time of the current
	 * thread.
	 * 
	 * @see #createThreadCpuWatch()
	 */
	public static StopWatch createStartedThreadCpuWatch() {
		return createThreadCpuWatch().start();
	}

	private static boolean enableThreadCpuTime(ThreadMXBean threads) {
		if (!threads.isCurrentThreadCpuTimeSupported()) {
			return false;
		}
		if (!threads.isThreadCpuTimeEnabled()) {
			try {
				threads.setThreadCpuTimeEnabled(true);
			} catch (UnsupportedOperationException | SecurityException ex) {
				return false;
			}
		}
		return threads.isThreadCpuTimeEnabled();
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
	 *        elapsed time in nanoseconds, e.g. {@link #getElapsedNanos()}
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
	 *        elapsed time in nanoseconds, e.g. {@link #getElapsedNanos()}
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
