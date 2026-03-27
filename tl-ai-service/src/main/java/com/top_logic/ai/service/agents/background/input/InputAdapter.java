/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.input;

import com.top_logic.ai.service.agents.background.context.BackgroundContext;

/**
 * Functional interface for gathering input for a background agent.
 *
 * <p>
 * Implementations adapt different {@link InputConfig} types into a uniform input gathering
 * mechanism.
 * </p>
 */
@FunctionalInterface
public interface InputAdapter {

	/**
	 * Gathers the input string to be passed to the background agent.
	 *
	 * @param context
	 *        The background execution context providing access to chat history, session data, and
	 *        other relevant information.
	 * @return The gathered input string, never {@code null}.
	 */
	String gatherInput(BackgroundContext context);
}
