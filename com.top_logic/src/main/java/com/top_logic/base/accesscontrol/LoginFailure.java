/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.base.accesscontrol;

/**
 * Representation of a login failure.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LoginFailure {

	private static final int MILLISECONDS = 1;

	private static final int SECONDS = 1000 * MILLISECONDS;

	private static final long MINUTES = 60 * SECONDS;

	/**
	 * Number of retries without delay.
	 */
	private static final int DIRECT_RETRY_CNT = 3;

	/**
	 * Initial delay after {@link #DIRECT_RETRY_CNT} failures.
	 */
	private static final double INITIAL_DELAY = 5 * SECONDS;

	/**
	 * The maximum delay.
	 */
	private static final long MAX_DELAY = 10 * MINUTES;

	private final String _key;

	private long _lastFailure;

	private int _cntFailures;

	/**
	 * Creates a {@link LoginFailure}.
	 *
	 * @param key
	 *        See {@link #getKey()}.
	 */
	public LoginFailure(String key) {
		_key = key;
	}

	/**
	 * The lookup key identifying this failure record, typically a combination of user name and
	 * client IP address.
	 */
	public String getKey() {
		return _key;
	}

	/**
	 * Whether this entry can be dropped since no further failed login occurred during an
	 * extended period of time.
	 */
	public boolean timedOut() {
		return now() > noRetryBefore() + MAX_DELAY;
	}

	/**
	 * The required wait time in milliseconds before the next login retry is accepted.
	 */
	public long retryDelay() {
		int failureCnt = getFailureCnt();
		if (failureCnt < DIRECT_RETRY_CNT) {
			return 0;
		}

		return Math.min(MAX_DELAY, (long) (INITIAL_DELAY * Math.pow(1.5, failureCnt - DIRECT_RETRY_CNT)));
	}

	/**
	 * Whether the required {@link #retryTimeout()} has been elapsed.
	 */
	public boolean allowRetry() {
		return now() >= noRetryBefore();
	}

	/**
	 * The timeout in milliseconds the user has to wait before the next login retry is accepted.
	 */
	public long retryTimeout() {
		return noRetryBefore() - now();
	}

	/**
	 * The absolute time in milliseconds since epoch when the next login retry can be performed.
	 */
	private synchronized long noRetryBefore() {
		return _lastFailure + retryDelay();
	}

	/**
	 * Adds another login failure.
	 */
	public synchronized void incFailures() {
		_cntFailures++;
		_lastFailure = now();
	}

	/**
	 * The current system time in milliseconds since epoch.
	 */
	private long now() {
		long now = System.currentTimeMillis();
		return now;
	}

	/**
	 * The number of failed logins without an intermediate successful login.
	 */
	public synchronized int getFailureCnt() {
		return _cntFailures;
	}
}
