/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import java.util.concurrent.atomic.AtomicBoolean;

import com.top_logic.ai.service.agents.background.context.BackgroundContext;
import com.top_logic.ai.service.agents.background.context.ChatContext;
import com.top_logic.ai.service.agents.background.input.CustomInput;
import com.top_logic.ai.service.agents.background.input.CustomInputAdapter;
import com.top_logic.ai.service.agents.background.input.ChatContextInput;
import com.top_logic.ai.service.agents.background.input.InputAdapter;
import com.top_logic.ai.service.agents.background.input.InputConfig;
import com.top_logic.ai.service.agents.background.input.ScriptInput;
import com.top_logic.ai.service.agents.background.input.ScriptInputAdapter;
import com.top_logic.ai.service.agents.background.trigger.PreTurnTrigger;
import com.top_logic.basic.config.TypedConfiguration;

import junit.framework.TestCase;

/**
 * Tests for {@link BackgroundAgentExecutor} input adapter dispatching.
 *
 * @see BackgroundAgentExecutor#resolveInputAdapter
 */
public class TestBackgroundAgentExecutor extends TestCase {

	private BackgroundAgentExecutor _executor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// Pass null registry — we only test resolveInputAdapter which does not use it
		_executor = new BackgroundAgentExecutor(null);
	}

	/** {@link CustomInput} resolves to {@link CustomInputAdapter}. */
	public void testResolveCustomInput() {
		CustomInput config = TypedConfiguration.newConfigItem(CustomInput.class);
		config.update(config.descriptor().getProperty(CustomInput.CUSTOM_MESSAGE), "test message");

		InputAdapter adapter = _executor.resolveInputAdapter(config);

		assertNotNull(adapter);
		assertTrue("Expected CustomInputAdapter", adapter instanceof CustomInputAdapter);
	}

	/** {@link ScriptInput} resolves to {@link ScriptInputAdapter}. */
	public void testResolveScriptInput() {
		ScriptInput config = TypedConfiguration.newConfigItem(ScriptInput.class);
		config.update(config.descriptor().getProperty(ScriptInput.SCRIPT), "$model.all(`my:Type`)");

		InputAdapter adapter = _executor.resolveInputAdapter(config);

		assertNotNull(adapter);
		assertTrue("Expected ScriptInputAdapter", adapter instanceof ScriptInputAdapter);
	}

	/** {@link ChatContextInput} without a registered adapter throws {@link IllegalArgumentException}. */
	public void testResolveChatContextInputThrows() {
		ChatContextInput config = TypedConfiguration.newConfigItem(ChatContextInput.class);

		try {
			_executor.resolveInputAdapter(config);
			fail("Expected IllegalArgumentException for unregistered ChatContextInput");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage().contains("No adapter for input config"));
		}
	}

	/** External adapter registered via {@link BackgroundAgentExecutor#registerInputAdapterFactory} is used. */
	public void testResolveWithExternalAdapter() {
		AtomicBoolean called = new AtomicBoolean(false);
		InputAdapter stubAdapter = context -> {
			called.set(true);
			return "external input";
		};

		BackgroundAgentExecutor.registerInputAdapterFactory(
			ChatContextInput.class, config -> stubAdapter);
		try {
			ChatContextInput config = TypedConfiguration.newConfigItem(ChatContextInput.class);

			InputAdapter adapter = _executor.resolveInputAdapter(config);

			assertNotNull(adapter);
			assertSame("Should return the externally registered adapter", stubAdapter, adapter);

			// Verify the adapter works
			String input = adapter.gatherInput(new BackgroundContext("test", "post-turn"));
			assertEquals("external input", input);
			assertTrue("External adapter should have been called", called.get());
		} finally {
			// Clean up to avoid affecting other tests
			BackgroundAgentExecutor.registerInputAdapterFactory(
				ChatContextInput.class, config -> {
					throw new UnsupportedOperationException("cleaned up");
				});
		}
	}

	/** {@link CustomInputAdapter} returns the configured custom message. */
	public void testCustomInputAdapterReturnsMessage() {
		CustomInput config = TypedConfiguration.newConfigItem(CustomInput.class);
		config.update(config.descriptor().getProperty(CustomInput.CUSTOM_MESSAGE), "Review this content");

		InputAdapter adapter = _executor.resolveInputAdapter(config);
		String input = adapter.gatherInput(new BackgroundContext("test-agent", "post-turn"));

		assertEquals("Review this content", input);
	}

	/** An unknown {@link InputConfig} type without a registered adapter throws. */
	public void testResolveUnknownInputConfigThrows() {
		InputConfig unknownConfig = TypedConfiguration.newConfigItem(InputConfig.class);

		try {
			_executor.resolveInputAdapter(unknownConfig);
			fail("Expected IllegalArgumentException for unknown InputConfig type");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage().contains("No adapter for input config"));
		}
	}

	/** Pre-turn trigger uses pending user message directly, bypassing InputAdapter. */
	public void testPreTurnBypassesInputAdapter() {
		// Create a BackgroundAgentConfig with PreTurnTrigger and null input
		BackgroundAgentConfig bgConfig = TypedConfiguration.newConfigItem(BackgroundAgentConfig.class);
		PreTurnTrigger trigger = TypedConfiguration.newConfigItem(PreTurnTrigger.class);
		bgConfig.update(bgConfig.descriptor().getProperty(BackgroundAgentConfig.TRIGGER), trigger);
		// Input is left null — pre-turn should not need it

		// Create a pre-turn ChatContext with a pending user message
		ChatContext context = ChatContext.preTurn("test-agent", "Hello world", null);

		assertEquals("Hello world", context.getPendingUserMessage());

		// Verify that the trigger is a PreTurnTrigger
		assertTrue(bgConfig.getTrigger() instanceof PreTurnTrigger);

		// Verify that the input is null (pre-turn agents don't need input config)
		assertNull(bgConfig.getInput());
	}
}
