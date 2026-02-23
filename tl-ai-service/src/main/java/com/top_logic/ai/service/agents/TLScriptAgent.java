/*
  * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
  *
  * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
  */
package com.top_logic.ai.service.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * TLScript Specialist Agent.
 * <p>
 * Analyzes, explains, validates, corrects, and authors TLScript expressions with mandatory
 * function verification. Uses TLScript function tools and model exploration to provide
 * accurate assistance with TLScript syntax and semantics.
 * </p>
 */
public interface TLScriptAgent {

	/**
	 * Answers user questions about TLScript.
	 *
	 * @param userQuestion
	 *        The user's question about TLScript.
	 * @return The answer.
	 */
	@UserMessage("""
			{{userQuestion}}
			""")

	@Agent("TLScript Specialist: analyzes, explains, validates, corrects, and authors TLScript expressions with mandatory function verification")
	String answer(@V("userQuestion") String userQuestion);

}
