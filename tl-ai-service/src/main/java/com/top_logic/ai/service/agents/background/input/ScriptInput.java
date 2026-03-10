/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.input;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Input configuration that uses a TL-Script expression to produce the agent's input.
 *
 * <p>
 * The configured {@link #getScript() script} is evaluated at runtime to generate the input string
 * that is passed to the background agent.
 * </p>
 */
public interface ScriptInput extends InputConfig {

	/** @see #getScript() */
	String SCRIPT = "script";

	/**
	 * The TL-Script expression that produces the agent's input.
	 *
	 * <p>
	 * The script is evaluated in the context of the background agent's execution and must return a
	 * string value.
	 * </p>
	 *
	 * @return The script expression, never {@code null}.
	 */
	@Name(SCRIPT)
	@Mandatory
	String getScript();
}
