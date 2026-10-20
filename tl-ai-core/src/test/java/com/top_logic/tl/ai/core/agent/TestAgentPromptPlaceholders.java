/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import junit.framework.TestCase;

import com.top_logic.basic.ConfigurationError;

/**
 * Tests the load-time refusal of a system prompt that names a placeholder the harness cannot fill.
 *
 * <p>
 * Such a placeholder would reach the model verbatim and read as an instruction the harness failed to
 * honour, and the typo behind it is invisible in the answer - which is why this is refused while the
 * definitions are read rather than logged while the run happens.
 * </p>
 *
 * @see com.top_logic.tl.ai.core.agent.environment.PlaceholderConstraint
 */
public class TestAgentPromptPlaceholders extends TestCase {

	/** Every prompt this product ships is accepted. */
	public void testTheHarnessesOwnPlaceholdersAreAccepted() {
		TopLogicAgentDefinitions.checkPlaceholders("TL_SingleAgent",
			"You help {{user}} in {{language}}, today {{date}}.\n{{environment}}");
	}

	/** A prompt naming no placeholder at all is the normal case and is accepted. */
	public void testAPromptWithoutPlaceholdersIsAccepted() {
		TopLogicAgentDefinitions.checkPlaceholders("TL_SingleAgent", "You are a helpful assistant.");
		TopLogicAgentDefinitions.checkPlaceholders("TL_SingleAgent", null);
	}

	/** A name the harness does not know fails the definition, naming the agent and the typo. */
	public void testAnUnknownPlaceholderFailsTheDefinition() {
		try {
			TopLogicAgentDefinitions.checkPlaceholders("MyAgent", "Work for {{usr}} on {{projekt}}.");
			fail("An unknown placeholder must fail the agent definition.");
		} catch (ConfigurationError expected) {
			String message = expected.getMessage();
			assertTrue(message, message.contains("MyAgent"));
			assertTrue(message, message.contains("{{usr}}"));
			assertTrue(message, message.contains("{{projekt}}"));
			assertTrue("The message must say what may be used instead.", message.contains("{{user}}"));
		}
	}
}
