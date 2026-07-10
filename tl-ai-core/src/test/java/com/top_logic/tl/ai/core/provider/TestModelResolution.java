/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.Map;

import junit.framework.TestCase;

/**
 * Tests for per-agent model resolution logic.
 */
public class TestModelResolution extends TestCase {

	/** */
	public void testPerAgentOverridesDefault() {
		Map<String, String> agentModels = Map.of("TL_Orchestrator", "claude-opus-4-20250514");
		String defaultModel = "claude-sonnet-4-20250514";

		String resolved = resolveModel("TL_Orchestrator", agentModels, defaultModel);
		assertEquals("claude-opus-4-20250514", resolved);
	}

	/** */
	public void testFallsBackToDefault() {
		Map<String, String> agentModels = Map.of("TL_Orchestrator", "claude-opus-4-20250514");
		String defaultModel = "claude-sonnet-4-20250514";

		String resolved = resolveModel("TL_ModelExplorer", agentModels, defaultModel);
		assertEquals("claude-sonnet-4-20250514", resolved);
	}

	/** */
	public void testNoModelThrows() {
		Map<String, String> agentModels = Map.of();

		try {
			resolveModel("TL_Orchestrator", agentModels, null);
			fail("Expected IllegalStateException");
		} catch (IllegalStateException e) {
			assertTrue(e.getMessage().contains("TL_Orchestrator"));
		}
	}

	/** */
	public void testEmptyDefaultWithNoOverrideThrows() {
		Map<String, String> agentModels = Map.of();

		try {
			resolveModel("TL_Orchestrator", agentModels, "");
			fail("Expected IllegalStateException");
		} catch (IllegalStateException e) {
			assertTrue(e.getMessage().contains("TL_Orchestrator"));
		}
	}

	/**
	 * Extracted resolution logic mirroring LLMService.resolveModel().
	 */
	static String resolveModel(String agentName, Map<String, String> agentModels, String defaultModel) {
		String perAgent = agentModels.get(agentName);
		if (perAgent != null && !perAgent.isBlank()) {
			return perAgent;
		}
		if (defaultModel != null && !defaultModel.isBlank()) {
			return defaultModel;
		}
		throw new IllegalStateException(
			"No model assigned to agent '" + agentName + "' and no default model configured.");
	}
}
