/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import com.top_logic.basic.Logger;

/**
 * Measures how long an operation, which is performed multiple times, takes.
 * <p>
 * Accumulates the results and logs them regularly. This is useful for operations which don't come
 * to an end, as they take forever.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class PerformanceLogger {

	private final String _name;

	private final long _logIntervalCount;

	private final StopWatch _stopWatch = new StopWatch();

	private long _measurementsCount = 0;

	/**
	 * Creates a new {@link PerformanceLogger}.
	 * 
	 * @param name
	 *        Used to distinguish the log messages of different instances.
	 * @param logIntervalCount
	 *        The accumulated time is logged, whenever a multiple of the log interval count has been
	 *        reached.
	 */
	public PerformanceLogger(String name, long logIntervalCount) {
		_name = name;
		_logIntervalCount = logIntervalCount;
	}

	/**
	 * Starts a measurement.
	 * <p>
	 * Starting twice without calling {@link #stop()} in between will cause an exception.
	 * </p>
	 */
	public void start() {
		_measurementsCount += 1;
		_stopWatch.start();
	}

	/**
	 * Stops the current measurement and logs it whenever a multiple of the log interval count has
	 * been reached.
	 * <p>
	 * Stopping when no measurement is active will cause an exception.
	 * </p>
	 */
	public void stop() {
		_stopWatch.stop();
		logMeasurement();
	}

	private void logMeasurement() {
		if ((_measurementsCount % _logIntervalCount) == 0) {
			logInfo(_name + " performed " + _measurementsCount + " operations which took " + _stopWatch + ".");
		}
	}

	/** Deletes the collected data. */
	public void clear() {
		_stopWatch.reset();
		_measurementsCount = 0;
	}

	private void logInfo(String message) {
		Logger.info(message, PerformanceLogger.class);
	}

}
