/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.util.Iterator;
import java.util.Random;


/**
 * Implements an truncated exponential backoff.
 * The start value, backoff factor and max value can be configured.
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ExponentialBackoff implements Iterator<Double> {
	
	private final double _backoffFactor;
	
	private final double _max;

	private final Random _random;

	private double _next;
	
	/**
	 * Start must not be greater than max.
	 * <p>
	 * The seed for the random number generator used in sleep is initialized with a probably unique
	 * value: {@link Random#Random()}
	 * </p>
	 * 
	 * @param start
	 *        The first value returned by {@link #next()}. Value Range: start > 0 and start < max.
	 * @param backoffFactor
	 *        The factor from one call to {@link #next()} to the next call of it. Value Range:
	 *        backoffFactor > 1
	 * @param max
	 *        The maximum value returned by {@link #next()}. Value Range: max > 0.
	 */
	public ExponentialBackoff(double start, double backoffFactor, double max) {
		checkParameter(start, backoffFactor, max);
		_next = start;
		_backoffFactor = backoffFactor;
		_max = max;
		_random = new Random();
	}

	/**
	 * Start must not be greater than max.
	 * 
	 * @param start
	 *        The first value returned by {@link #next()}. Value Range: start > 0 and start < max.
	 * @param backoffFactor
	 *        The factor from one call to {@link #next()} to the next call of it. Value Range:
	 *        backoffFactor > 1
	 * @param max
	 *        The maximum value returned by {@link #next()}. Value Range: max > 0.
	 * @param seed
	 *        The seed for the random number generator used in {@link #sleep()}.
	 */
	public ExponentialBackoff(double start, double backoffFactor, double max, long seed) {
		checkParameter(start, backoffFactor, max);
		_next = start;
		_backoffFactor = backoffFactor;
		_max = max;
		_random = new Random(seed);
	}

	private void checkParameter(double start, double backoffFactor, double max) {
		if (start <= 0) {
			throw new IllegalArgumentException("Start (" + start + ") must be greater than 0.");
		}
		if (max <= 0) {
			throw new IllegalArgumentException("Max (" + max + ") must be greater than 0.");
		}
		if (backoffFactor <= 1) {
			throw new IllegalArgumentException("BackoffFactor (" + backoffFactor + ") must be greater than 1.");
		}
		if (start > max) {
			String message = "The start (" + start + ") value must not be greater than the max value (" + max + ")!";
			throw new IllegalArgumentException(message);
		}
	}
	
	@Override
	public Double next() {
		double current = _next;
		_next *= _backoffFactor;
		_next = Math.min(_next, _max);
		return current;
	}
	
	/**
	 * Always <code>true</code>.
	 * 
	 * @see Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return true;
	}
	
	/**
	 * @throws UnsupportedOperationException Always
	 * 
	 * @see Iterator#remove()
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("This method makes no sense in this class.");
	}

	/**
	 * Convenience shortcut that calls {@link #next()}, multiplies it with
	 * {@link Random#nextFloat()}, and {@link Thread#sleep(long) sleeps} that amount of
	 * milliseconds.
	 * <p>
	 * If an {@link InterruptedException} is thrown, the method immediately returns.
	 * </p>
	 */
	public void sleep() {
		try {
			Thread.sleep(Math.round(_random.nextFloat() * next()));
		} catch (InterruptedException ex) {
			return;
		}
	}

}
