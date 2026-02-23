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
 * Agent for exploring the TopLogic application model.
 */
public interface ModelExplorer {
	/**
	 * Explores the TopLogic application model to answer a user question.
	 *
	 * @param userQuestion
	 *        The user's question about the model.
	 * @return The answer based on model exploration.
	 */
	@UserMessage("""
			Use the available model-exploration tools to answer the user question with tool-backed facts only.

			Name resolution (no guessing):
			- Resolve referenced module/type/part via searchModel() in the current scope.
			- If EXACTLY ONE exact-name match exists → use it and proceed.
			- If MULTIPLE exact-name matches exist → list those exact matches and ask which one; stop.
			- If NO exact-name match exists:
			  - 0 matches total → say not found (in current scope) and stop.
			  - otherwise → list candidates and ask which one; stop.

			Reply format:
			- Facts (bullets)
			- Next suggested query (optional)
			- Evidence: tool calls (name + key args) and the facts each supports

			Work-order input (from another agent): If the input is structured as "Goal: ... Task: ... Context: ... Expected output format: ...", treat those lines as the user question and follow the requested output format. Prefer structured, concise findings (elementIds, fields, annotations) when Expected output format requests them.

			User question: {{userQuestion}}
			""")

	@Agent("Explores the TopLogic application model (read-only) to answer user questions with grounded model evidence")
	String explore(@V("userQuestion") String userQuestion);

}
