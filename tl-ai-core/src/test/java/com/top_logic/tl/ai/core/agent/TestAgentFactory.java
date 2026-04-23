/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.tools.registry.ToolRegistryService;

/**
 * Tests for {@link AgentFactory} utility methods and constants.
 */
public class TestAgentFactory extends TestCase {

	/**
	 * Verifies the qualified tool name convention: names from {@link ToolRegistryService} use the format
	 * {@code factory-name.toolMethodName}, which contains exactly one dot separator.
	 */
	public void testQualifiedToolNameFormat() {
		String qualified = "model-browser.listModules";
		assertTrue("Qualified names must contain a dot", qualified.contains("."));
		assertEquals("factory-name is the prefix before the dot", "model-browser",
			qualified.substring(0, qualified.indexOf('.')));
		assertEquals("tool method name is the suffix after the dot", "listModules",
			qualified.substring(qualified.indexOf('.') + 1));

		// A bare factory name would not resolve in ToolRegistryService
		String bareFactory = "model-browser";
		assertFalse("Bare factory names have no dot and won't resolve",
			bareFactory.contains("."));
	}

	/** */
	public void testReplacePlaceholderOrAppend_replaces() {
		String prompt = "Before\n<<AVAILABLE_TOOLS>>\nAfter";
		String result = AgentFactory.replacePlaceholderOrAppend(prompt, "<<AVAILABLE_TOOLS>>", "TOOLS HERE");
		assertEquals("Before\nTOOLS HERE\nAfter", result);
	}

	/** */
	public void testReplacePlaceholderOrAppend_appendsWhenMissing() {
		String prompt = "No placeholder here";
		String result = AgentFactory.replacePlaceholderOrAppend(prompt, "<<AVAILABLE_TOOLS>>", "TOOLS HERE");
		assertEquals("No placeholder here\n\n---\n\nTOOLS HERE", result);
	}

	/** */
	public void testReplacePlaceholderOrAppend_removesPlaceholderWhenEmpty() {
		String prompt = "Before\n<<AVAILABLE_TOOLS>>\nAfter";
		String result = AgentFactory.replacePlaceholderOrAppend(prompt, "<<AVAILABLE_TOOLS>>", "");
		assertEquals("Before\n\nAfter", result);
	}

	/** */
	public void testReplacePlaceholderOrAppend_noChangeWhenBothEmpty() {
		String prompt = "No placeholder here";
		String result = AgentFactory.replacePlaceholderOrAppend(prompt, "<<AVAILABLE_TOOLS>>", "");
		assertEquals("No placeholder here", result);
	}

	/** */
	public void testPlaceholderConstants() {
		assertEquals("<<AVAILABLE_TOOLS>>", AgentFactory.TOOLS_PLACEHOLDER);
		assertEquals("<<AVAILABLE_DELEGATES>>", AgentFactory.DELEGATES_PLACEHOLDER);
	}

}
