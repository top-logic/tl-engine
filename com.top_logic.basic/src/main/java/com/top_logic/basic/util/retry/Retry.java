/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util.retry;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ExponentialBackoff;

/**
 * For retrying a {@link Computation} if it fails.
 * <p>
 * If a {@link ExponentialBackoff} is given in the constructor, it is used to sleep between the
 * attempts. <br/>
 * Takes a {@link Computation} and is a computation itself.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class Retry<S, T> implements Computation<RetryResult<S, List<T>>> {

	private final int _maxRetries;

	private final ExponentialBackoff _backoff;

	private final Computation<RetryResult<S, T>> _computation;

	/**
	 * Convenience method for creating a {@link Retry} via {@link #Retry(int, Computation)} and
	 * {@link #run() running} it immediately.
	 */
	public static <S, T> RetryResult<S, List<T>> retry(int maxAttempts,
			Computation<RetryResult<S, T>> computation) {
		return new Retry<>(maxAttempts, computation).run();
	}

	/**
	 * Convenience method for creating a {@link Retry} via
	 * {@link #Retry(int, ExponentialBackoff, Computation)} and {@link #run() running} it
	 * immediately.
	 */
	public static <S, T> RetryResult<S, List<T>> retry(int maxAttempts, ExponentialBackoff backoff,
			Computation<RetryResult<S, T>> computation) {
		return new Retry<>(maxAttempts, backoff, computation).run();
	}

	/**
	 * Create a {@link Retry} that does not sleep between the attempts.
	 * 
	 * @param maxAttempts
	 *        Has to be 2 or more.
	 * @param computation
	 *        Must not be <code>null</code>. Must not return <code>null</code>.
	 */
	public Retry(int maxAttempts, Computation<RetryResult<S, T>> computation) {
		this(maxAttempts, null, computation);
	}

	/**
	 * Create a {@link Retry}.
	 * 
	 * @param maxAttempts
	 *        Has to be 2 or more.
	 * @param backoff
	 *        The {@link ExponentialBackoff} that sleeping between the attempts. Is allowed to be
	 *        <code>null</code>, in which case there is no sleep between the attempts.
	 * @param computation
	 *        Must not be <code>null</code>. Must not return <code>null</code>.
	 */
	public Retry(int maxAttempts, ExponentialBackoff backoff,
			Computation<RetryResult<S, T>> computation) {
		checkParameters(maxAttempts, computation);
		_maxRetries = maxAttempts;
		_backoff = backoff;
		_computation = computation;
	}

	private void checkParameters(int maxAttempts, Computation<RetryResult<S, T>> computation) {
		if (maxAttempts < 2) {
			throw new IllegalArgumentException("The maximal number of attempts has to be at least 2.");
		}
		if (computation == null) {
			throw new NullPointerException("The computation must not be null.");
		}
	}

	@Override
	public RetryResult<S, List<T>> run() {
		List<T> failureReasons = null;
		for (int i = 0; i < _maxRetries; i++) {
			RetryResult<? extends S, ? extends T> result = _computation.run();
			if (result.isSuccess()) {
				return RetryResult.<S, List<T>> createSuccess(result.getValue());
			}
			if (result.getReason() != null) {
				if (failureReasons == null) {
					failureReasons = new ArrayList<>();
				}
				failureReasons.add(result.getReason());
			}
			if (_backoff != null) {
				_backoff.sleep();
			}
		}
		return RetryResult.createFailure(CollectionUtil.nonNull(failureReasons));
	}

}
