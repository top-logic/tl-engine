/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util.retry;

import com.top_logic.basic.col.Maybe;

/**
 * The result of a {@link Retry} attempt. Either a success or a failure.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class RetryResult<S, T> {

	private static final RetryResult<Object, Object> FAILURE_WITHOUT_REASON =
		new RetryFailure<>(null);

	private static final RetryResult<Object, Object> SUCCESS_WITHOUT_VALUE = new RetrySuccess<>(null);

	RetryResult() {
		// Reduce visibility
	}

	/** Creates a {@link RetryResult} for a success with the given result value. */
	public static final <S, T> RetryResult<S, T> createSuccess(S value) {
		if (value == null) {
			return createSuccess();
		}
		return new RetrySuccess<>(value);
	}

	/**
	 * Returns the singleton {@link RetryResult} that represents a success without result value.
	 */
	@SuppressWarnings("unchecked")
	public static final <S, T> RetryResult<S, T> createSuccess() {
		return (RetryResult<S, T>) SUCCESS_WITHOUT_VALUE;
	}

	/** Creates a {@link RetryResult} for a failure with the given failure reason. */
	public static final <S, T> RetryResult<S, T> createFailure(T reason) {
		if (reason == null) {
			return createFailure();
		}
		return new RetryFailure<>(reason);
	}

	/**
	 * Returns the singleton {@link RetryResult} that represents a failure without failure
	 * reason.
	 */
	@SuppressWarnings("unchecked")
	public static final <S, T> RetryResult<S, T> createFailure() {
		return (RetryResult<S, T>) FAILURE_WITHOUT_REASON;
	}

	/** Was the attempt a success? */
	public abstract boolean isSuccess();

	/**
	 * The result of this successful attempt.
	 * @throws IllegalStateException
	 *         If this attempt was not successful.
	 */
	public abstract S getValue() throws IllegalStateException;

	/**
	 * The reason why this attempt failed. Never <code>null</code>. If the {@link Maybe}
	 *         {@link Maybe#hasValue() has a value}, that value is guaranteed to be non-null.
	 * @throws IllegalStateException
	 *         If this attempt did not fail.
	 */
	public abstract T getReason() throws IllegalStateException;

}