/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.error.TopLogicException;

/**
 * Refusal of a credential check by
 * {@link LoginFailuresModule#checkRetry(String, com.top_logic.basic.util.ResKey1)} while the key
 * of the check is backing off after too many failures.
 *
 * <p>
 * The {@link #getErrorKey() message} tells the user how long to wait before the next attempt.
 * </p>
 */
public class RetryDeniedException extends TopLogicException {

	private final long _retryTimeout;

	/**
	 * Creates a {@link RetryDeniedException}.
	 *
	 * @param message
	 *        The message shown to the user.
	 * @param retryTimeout
	 *        See {@link #getRetryTimeout()}.
	 */
	public RetryDeniedException(ResKey message, long retryTimeout) {
		super(message);
		_retryTimeout = retryTimeout;
	}

	/**
	 * The time in milliseconds to wait before the next attempt is accepted.
	 */
	public long getRetryTimeout() {
		return _retryTimeout;
	}

}
