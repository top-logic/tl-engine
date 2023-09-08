/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util.retry;


/**
 * The {@link RetryResult} implementation representing a success.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class RetrySuccess<S, T> extends RetryResult<S, T> {

	private final S _value;

	RetrySuccess(S value) {
		_value = value;
	}

	@Override
	public boolean isSuccess() {
		return true;
	}

	@Override
	public S getValue() throws IllegalStateException {
		return _value;
	}

	@Override
	public T getReason() throws IllegalStateException {
		throw new IllegalStateException("There is no failure reason as the operation succeeded.");
	}

}