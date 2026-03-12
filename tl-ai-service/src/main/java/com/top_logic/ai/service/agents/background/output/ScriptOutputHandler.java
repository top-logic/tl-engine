/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.output;

import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Output handler that evaluates a TL-Script expression with the agent's response.
 *
 * <p>
 * The script receives the raw LLM response string as its first argument. Use lambda syntax to bind
 * it:
 * </p>
 *
 * <pre>
 * response -&gt; $response.length() &gt; 0
 * </pre>
 */
public interface ScriptOutputHandler extends OutputHandler {

	/** @see #getScript() */
	String SCRIPT = "script";

	/**
	 * The TL-Script expression to evaluate.
	 *
	 * <p>
	 * The raw LLM response string is passed as the first argument. Use lambda syntax to access it,
	 * e.g. {@code response -> $response}.
	 * </p>
	 */
	@Name(SCRIPT)
	@Mandatory
	@DefaultContainer
	Expr getScript();
}
