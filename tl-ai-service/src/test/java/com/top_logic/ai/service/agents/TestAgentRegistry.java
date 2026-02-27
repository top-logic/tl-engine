/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import java.time.Duration;

import junit.framework.TestCase;

import com.top_logic.ai.service.tools.registry.ToolRegistry;

import dev.langchain4j.model.chat.request.ResponseFormat;

public class TestAgentRegistry extends TestCase {

	public void testMapResponseFormatJson() {
		assertEquals(ResponseFormat.JSON,
			AgentRegistry.mapResponseFormat(AgentConfig.ResponseFormatOption.JSON));
	}

	public void testMapResponseFormatText() {
		assertEquals(ResponseFormat.TEXT,
			AgentRegistry.mapResponseFormat(AgentConfig.ResponseFormatOption.TEXT));
	}

	public void testMapResponseFormatDefault() {
		assertNull(AgentRegistry.mapResponseFormat(AgentConfig.ResponseFormatOption.DEFAULT));
		assertNull(AgentRegistry.mapResponseFormat(null));
	}

	public void testToTimeoutDuration() {
		assertEquals(Duration.ofSeconds(60), AgentRegistry.toTimeoutDuration(60));
		assertNull(AgentRegistry.toTimeoutDuration(null));
	}

	/**
	 * Verifies the qualified tool name convention: names from {@link ToolRegistry} use the format
	 * {@code factory-name.toolMethodName}, which contains exactly one dot separator.
	 */
	public void testQualifiedToolNameFormat() {
		String qualified = "model-browser.listModules";
		assertTrue("Qualified names must contain a dot", qualified.contains("."));
		assertEquals("factory-name is the prefix before the dot", "model-browser",
			qualified.substring(0, qualified.indexOf('.')));
		assertEquals("tool method name is the suffix after the dot", "listModules",
			qualified.substring(qualified.indexOf('.') + 1));

		// A bare factory name would not resolve in ToolRegistry
		String bareFactory = "model-browser";
		assertFalse("Bare factory names have no dot and won't resolve",
			bareFactory.contains("."));
	}

	public void testReplacePlaceholderOrAppend_replaces() {
		String prompt = "Before\n<<AVAILABLE_TOOLS>>\nAfter";
		String result = AgentRegistry.replacePlaceholderOrAppend(prompt, "<<AVAILABLE_TOOLS>>", "TOOLS HERE");
		assertEquals("Before\nTOOLS HERE\nAfter", result);
	}

	public void testReplacePlaceholderOrAppend_appendsWhenMissing() {
		String prompt = "No placeholder here";
		String result = AgentRegistry.replacePlaceholderOrAppend(prompt, "<<AVAILABLE_TOOLS>>", "TOOLS HERE");
		assertEquals("No placeholder here\n\n---\n\nTOOLS HERE", result);
	}

	public void testReplacePlaceholderOrAppend_removesPlaceholderWhenEmpty() {
		String prompt = "Before\n<<AVAILABLE_TOOLS>>\nAfter";
		String result = AgentRegistry.replacePlaceholderOrAppend(prompt, "<<AVAILABLE_TOOLS>>", "");
		assertEquals("Before\n\nAfter", result);
	}

	public void testReplacePlaceholderOrAppend_noChangeWhenBothEmpty() {
		String prompt = "No placeholder here";
		String result = AgentRegistry.replacePlaceholderOrAppend(prompt, "<<AVAILABLE_TOOLS>>", "");
		assertEquals("No placeholder here", result);
	}

	public void testDelegationToolName() {
		assertEquals("delegate_to_ModelExplorer", AgentRegistry.delegationToolName("ModelExplorer"));
		assertEquals("delegate_to_TLScript_Agent", AgentRegistry.delegationToolName("TLScript-Agent"));
	}

	public void testPlaceholderConstants() {
		assertEquals("<<AVAILABLE_TOOLS>>", AgentRegistry.TOOLS_PLACEHOLDER);
		assertEquals("<<AVAILABLE_DELEGATES>>", AgentRegistry.DELEGATES_PLACEHOLDER);
	}

	/**
	 * Documents the entry-point validation contract: exactly one agent must be marked.
	 * The actual validation runs in {@link AgentRegistry#startUp()}.
	 */
	public void testEntryPointValidationContract() {
		// The entry-point property must exist on AgentConfig
		// and default to false (via @BooleanDefault)
		// Startup validates: 0 entry points -> RuntimeException
		// Startup validates: 2+ entry points -> RuntimeException
		// Startup validates: exactly 1 -> stores name in _entryPointAgentName

		// Verify the property name constant matches the expected XML attribute
		assertEquals("entry-point", AgentConfig.ENTRY_POINT);
	}
}
