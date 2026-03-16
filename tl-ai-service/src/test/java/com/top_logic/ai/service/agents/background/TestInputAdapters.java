/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import com.top_logic.ai.service.agents.background.context.ApplicationContext;
import com.top_logic.ai.service.agents.background.context.BackgroundContext;
import com.top_logic.ai.service.agents.background.context.ChatContext;
import com.top_logic.ai.service.agents.background.input.AutonomousInput;
import com.top_logic.ai.service.agents.background.input.AutonomousInputAdapter;
import com.top_logic.ai.service.agents.background.input.InputAdapter;
import com.top_logic.ai.service.agents.background.input.ScriptInput;
import com.top_logic.ai.service.agents.background.input.ScriptInputAdapter;
import com.top_logic.basic.config.TypedConfiguration;

import junit.framework.TestCase;

/**
 * Tests for input adapters: {@link AutonomousInputAdapter} and {@link ScriptInputAdapter}.
 *
 * <p>
 * Tests the adapters directly (not just through executor resolution) with various context types.
 * </p>
 */
public class TestInputAdapters extends TestCase {

	// --- AutonomousInputAdapter ---

	/** Adapter returns the configured custom message regardless of context. */
	public void testAutonomousAdapterReturnsMessage() {
		AutonomousInput config = TypedConfiguration.newConfigItem(AutonomousInput.class);
		config.update(config.descriptor().getProperty(AutonomousInput.CUSTOM_MESSAGE),
			"Review all pending changes");

		AutonomousInputAdapter adapter = new AutonomousInputAdapter(config);

		assertEquals("Review all pending changes",
			adapter.gatherInput(new BackgroundContext("agent", "scheduled")));
	}

	/** Adapter returns the same message for different context types. */
	public void testAutonomousAdapterIgnoresContextType() {
		AutonomousInput config = TypedConfiguration.newConfigItem(AutonomousInput.class);
		config.update(config.descriptor().getProperty(AutonomousInput.CUSTOM_MESSAGE), "fixed input");

		AutonomousInputAdapter adapter = new AutonomousInputAdapter(config);

		BackgroundContext base = new BackgroundContext("agent", "test");
		ChatContext chat = ChatContext.lastTurn("agent", "msg", "resp", null, null);
		ApplicationContext app = ApplicationContext.scheduled("agent");

		assertEquals("fixed input", adapter.gatherInput(base));
		assertEquals("fixed input", adapter.gatherInput(chat));
		assertEquals("fixed input", adapter.gatherInput(app));
	}

	/** Adapter handles multiline custom messages. */
	public void testAutonomousAdapterMultilineMessage() {
		AutonomousInput config = TypedConfiguration.newConfigItem(AutonomousInput.class);
		String multiline = "Line 1\nLine 2\nLine 3";
		config.update(config.descriptor().getProperty(AutonomousInput.CUSTOM_MESSAGE), multiline);

		AutonomousInputAdapter adapter = new AutonomousInputAdapter(config);

		assertEquals(multiline, adapter.gatherInput(new BackgroundContext("agent", "test")));
	}

	// --- ScriptInputAdapter ---

	/** Adapter returns the configured script expression (currently no evaluation). */
	public void testScriptAdapterReturnsScript() {
		ScriptInput config = TypedConfiguration.newConfigItem(ScriptInput.class);
		config.update(config.descriptor().getProperty(ScriptInput.SCRIPT),
			"$model.all(`my:Type`)");

		ScriptInputAdapter adapter = new ScriptInputAdapter(config);

		assertEquals("$model.all(`my:Type`)",
			adapter.gatherInput(new BackgroundContext("agent", "scheduled")));
	}

	/** Script adapter returns script for different context types. */
	public void testScriptAdapterIgnoresContextType() {
		ScriptInput config = TypedConfiguration.newConfigItem(ScriptInput.class);
		config.update(config.descriptor().getProperty(ScriptInput.SCRIPT), "testScript()");

		ScriptInputAdapter adapter = new ScriptInputAdapter(config);

		assertEquals("testScript()",
			adapter.gatherInput(ApplicationContext.scheduled("agent")));
		assertEquals("testScript()",
			adapter.gatherInput(ChatContext.lastTurn("agent", "m", "r", null, null)));
	}

	// --- InputAdapter interface contract ---

	/** Both adapters implement InputAdapter. */
	public void testAdaptersImplementInterface() {
		AutonomousInput aConfig = TypedConfiguration.newConfigItem(AutonomousInput.class);
		aConfig.update(aConfig.descriptor().getProperty(AutonomousInput.CUSTOM_MESSAGE), "msg");

		ScriptInput sConfig = TypedConfiguration.newConfigItem(ScriptInput.class);
		sConfig.update(sConfig.descriptor().getProperty(ScriptInput.SCRIPT), "script");

		assertTrue(new AutonomousInputAdapter(aConfig) instanceof InputAdapter);
		assertTrue(new ScriptInputAdapter(sConfig) instanceof InputAdapter);
	}

	// --- External adapter registration ---

	/** External adapter registration and resolution via the executor. */
	public void testExternalAdapterRegistrationAndGatherInput() {
		BackgroundAgentExecutor executor = new BackgroundAgentExecutor(null);

		// Register a custom adapter for a custom InputConfig type via lambda
		BackgroundAgentExecutor.registerInputAdapterFactory(
			CustomTestInput.class,
			config -> context -> "custom:" + config.getCustomValue());

		try {
			CustomTestInput config = TypedConfiguration.newConfigItem(CustomTestInput.class);
			config.update(config.descriptor().getProperty("custom-value"), "hello");

			InputAdapter adapter = executor.resolveInputAdapter(config);
			String input = adapter.gatherInput(new BackgroundContext("agent", "test"));

			assertEquals("custom:hello", input);
		} finally {
			// Clean up
			BackgroundAgentExecutor.registerInputAdapterFactory(
				CustomTestInput.class, c -> {
					throw new UnsupportedOperationException("cleaned up");
				});
		}
	}

	/**
	 * Test-only input config for external adapter registration tests.
	 */
	public interface CustomTestInput extends com.top_logic.ai.service.agents.background.input.InputConfig {

		/** Property name. */
		String CUSTOM_VALUE = "custom-value";

		/** The custom value. */
		@com.top_logic.basic.config.annotation.Name(CUSTOM_VALUE)
		String getCustomValue();
	}
}
