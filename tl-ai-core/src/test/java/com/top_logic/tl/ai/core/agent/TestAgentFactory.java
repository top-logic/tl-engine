/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;


import junit.framework.TestCase;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.agent.environment.UserMemoryConfig;
import com.top_logic.tl.ai.core.agent.environment.UserMemorySettings;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

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

	/**
	 * An installation that keeps no memories offers neither memory tool, however an agent
	 * definition names them - and offers everything else as before.
	 */
	public void testSwitchedOffMemoryIsNotOffered() {
		assertTrue("On by default.", AgentFactory.isOffered(namedTool("remember")));
		assertTrue(AgentFactory.isOffered(namedTool("recall")));

		UserMemoryConfig off = TypedConfiguration.newConfigItem(UserMemoryConfig.class);
		off.update(off.descriptor().getProperty(UserMemoryConfig.ENABLED), Boolean.FALSE);
		try (UserMemorySettings.Overridden overridden = UserMemorySettings.override(off)) {
			assertFalse(AgentFactory.isOffered(namedTool("remember")));
			assertFalse(AgentFactory.isOffered(namedTool("recall")));
			assertTrue("The switch is about memory and nothing else.",
				AgentFactory.isOffered(namedTool("askUser")));
		}
	}

	/** A {@link Tool} that is nothing but its name, which is all {@code isOffered} reads. */
	private static Tool namedTool(String name) {
		return ToolStubs.builder(name).description("A stub for the catalog gate.").schema(null)
			.mode(ToolMode.STRUCTURAL).execute((args, context) -> {
				throw new UnsupportedOperationException("Never executed.");
			}).build();
	}

}
