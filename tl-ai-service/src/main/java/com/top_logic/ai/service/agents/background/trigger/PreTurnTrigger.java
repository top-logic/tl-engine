/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.trigger;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.LongDefault;

/**
 * Trigger that fires before a chat turn is processed.
 *
 * <p>
 * The background agent runs synchronously before the primary agent processes the user's message.
 * This is useful for content filtering, safety checks, or input validation. A
 * {@link #getTimeout() timeout} ensures the user is not blocked indefinitely, and a
 * {@link #getFailPolicy() fail policy} determines behavior when the timeout is exceeded.
 * </p>
 */
public interface PreTurnTrigger extends TriggerConfig {

	/** @see #getTimeout() */
	String TIMEOUT = "timeout";

	/** @see #getFailPolicy() */
	String FAIL_POLICY = "fail-policy";

	/**
	 * Policy for handling pre-turn trigger timeouts.
	 */
	enum FailPolicy {

		/**
		 * Allow the turn to proceed when the background agent times out.
		 *
		 * <p>
		 * This is the default policy, ensuring the user experience is not degraded by background
		 * agent failures.
		 * </p>
		 */
		OPEN,

		/**
		 * Block the turn when the background agent times out.
		 *
		 * <p>
		 * Use this policy for safety-critical checks that must complete before the turn proceeds.
		 * </p>
		 */
		CLOSED
	}

	/**
	 * The maximum time in milliseconds to wait for the background agent to complete before
	 * applying the {@link #getFailPolicy()}.
	 *
	 * <p>
	 * Defaults to 30000 (30 seconds).
	 * </p>
	 *
	 * @return The timeout in milliseconds.
	 */
	@Name(TIMEOUT)
	@LongDefault(30000)
	long getTimeout();

	/**
	 * The policy to apply when the background agent exceeds the {@link #getTimeout()}.
	 *
	 * <p>
	 * Defaults to {@link FailPolicy#OPEN}, allowing the turn to proceed even if the background
	 * agent has not completed.
	 * </p>
	 *
	 * @return The fail policy, never {@code null}.
	 */
	@Name(FAIL_POLICY)
	FailPolicy getFailPolicy();
}
