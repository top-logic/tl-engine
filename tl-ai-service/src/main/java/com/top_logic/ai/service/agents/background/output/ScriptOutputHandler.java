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
 */
public interface ScriptOutputHandler extends OutputHandler {

	/** @see #getScript() */
	String SCRIPT = "script";

	/**
	 * The TL-Script expression to evaluate.
	 *
	 * <p>
	 * The first argument is the raw LLM response string. Use {@code log()} to write to the
	 * application log.
	 * </p>
	 */
	@Name(SCRIPT)
	@Mandatory
	@DefaultContainer
	Expr getScript();
}
