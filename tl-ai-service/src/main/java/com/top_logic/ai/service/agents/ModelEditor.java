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
 * Agent for creating and modifying TopLogic model elements from natural language requirements.
 */
public interface ModelEditor {

	/**
	 * Creates TopLogic model elements from natural language requirements.
	 *
	 * @param modelRequirements
	 *        The model creation requirements in natural language.
	 * @return A summary of the creation process.
	 */
	@UserMessage("Create or modify TopLogic model elements based on these requirements: {{modelRequirements}}")
	@Agent("Creates TopLogic model elements from natural language requirements")
	String createModel(@V("modelRequirements") String modelRequirements);
}
