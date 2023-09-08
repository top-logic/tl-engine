/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util.retry;


/**
 * The {@link RetryResult} implementation representing a failure.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class RetryFailure<S, T> extends RetryResult<S, T> {

	private final T _reason;

	RetryFailure(T reason) {
		_reason = reason;
	}

	@Override
	public boolean isSuccess() {
		return false;
	}

	@Override
	public S getValue() {
		throw new IllegalStateException("There is no result value as the operation failed.");
	}

	@Override
	public T getReason() {
		return _reason;
	}

}