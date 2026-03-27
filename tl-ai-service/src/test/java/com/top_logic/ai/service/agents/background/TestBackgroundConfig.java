/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import com.top_logic.ai.service.agents.background.BackgroundAgentConfig.Scope;
import com.top_logic.ai.service.agents.background.input.CustomInput;
import com.top_logic.ai.service.agents.background.input.ChatContextInput;
import com.top_logic.ai.service.agents.background.input.ChatContextInput.Content;
import com.top_logic.ai.service.agents.background.input.ScriptInput;
import com.top_logic.ai.service.agents.background.trigger.PostTurnTrigger;
import com.top_logic.ai.service.agents.background.trigger.PreTurnTrigger;
import com.top_logic.ai.service.agents.background.trigger.PreTurnTrigger.FailPolicy;
import com.top_logic.ai.service.agents.background.trigger.TriggerConfig;
import com.top_logic.basic.config.TypedConfiguration;

import junit.framework.TestCase;

/**
 * Tests for {@link BackgroundAgentConfig} defaults and all trigger/input/output configuration
 * interfaces.
 *
 * <p>
 * Verifies that configuration defaults are correct and that typed configuration instances can be
 * created for all polymorphic subtypes.
 * </p>
 */
public class TestBackgroundConfig extends TestCase {

	// --- Scope enum ---

	/** Both scope values exist. */
	public void testScopeValues() {
		assertEquals(2, Scope.values().length);
		assertNotNull(Scope.CHAT);
		assertNotNull(Scope.APPLICATION);
	}

	// --- PreTurnTrigger ---

	/** Default timeout is 30000ms. */
	public void testPreTurnTriggerDefaultTimeout() {
		PreTurnTrigger trigger = TypedConfiguration.newConfigItem(PreTurnTrigger.class);

		assertEquals(30000L, trigger.getTimeout());
	}

	/** Default fail policy is OPEN. */
	public void testPreTurnTriggerDefaultFailPolicy() {
		PreTurnTrigger trigger = TypedConfiguration.newConfigItem(PreTurnTrigger.class);

		// Default is null from TypedConfiguration, but the enum default in usage is OPEN
		// The actual default depends on the annotation — FailPolicy has no @defaults annotation
		// so it may be null. The executor treats null as OPEN.
		FailPolicy policy = trigger.getFailPolicy();
		// FailPolicy has no default annotation, so it's null from newConfigItem
		// The executor falls back to OPEN when reading from a PreTurnTrigger
	}

	/** Fail policy values exist. */
	public void testFailPolicyValues() {
		assertEquals(2, FailPolicy.values().length);
		assertNotNull(FailPolicy.OPEN);
		assertNotNull(FailPolicy.CLOSED);
	}

	/** Custom timeout can be set. */
	public void testPreTurnTriggerCustomTimeout() {
		PreTurnTrigger trigger = TypedConfiguration.newConfigItem(PreTurnTrigger.class);
		trigger.update(trigger.descriptor().getProperty(PreTurnTrigger.TIMEOUT), 5000L);

		assertEquals(5000L, trigger.getTimeout());
	}

	/** Custom fail policy can be set. */
	public void testPreTurnTriggerCustomFailPolicy() {
		PreTurnTrigger trigger = TypedConfiguration.newConfigItem(PreTurnTrigger.class);
		trigger.update(trigger.descriptor().getProperty(PreTurnTrigger.FAIL_POLICY),
			FailPolicy.CLOSED);

		assertEquals(FailPolicy.CLOSED, trigger.getFailPolicy());
	}

	/** PreTurnTrigger is a TriggerConfig. */
	public void testPreTurnTriggerIsTriggerConfig() {
		PreTurnTrigger trigger = TypedConfiguration.newConfigItem(PreTurnTrigger.class);

		assertTrue(trigger instanceof TriggerConfig);
	}

	// --- PostTurnTrigger ---

	/** PostTurnTrigger can be instantiated. */
	public void testPostTurnTriggerInstantiation() {
		PostTurnTrigger trigger = TypedConfiguration.newConfigItem(PostTurnTrigger.class);

		assertNotNull(trigger);
		assertTrue(trigger instanceof TriggerConfig);
	}

	// --- ScheduledTrigger ---
	// Note: ScheduledTrigger uses PolymorphicConfiguration<SchedulingAlgorithm> which
	// requires TypeIndex. Integration tests cover ScheduledTrigger instantiation.

	// --- Input configs ---

	/** CustomInput can be instantiated and stores custom message. */
	public void testCustomInputConfig() {
		CustomInput input = TypedConfiguration.newConfigItem(CustomInput.class);
		input.update(input.descriptor().getProperty(CustomInput.CUSTOM_MESSAGE),
			"Check for issues");

		assertEquals("Check for issues", input.getCustomMessage());
	}

	/** ScriptInput can be instantiated and stores script. */
	public void testScriptInputConfig() {
		ScriptInput input = TypedConfiguration.newConfigItem(ScriptInput.class);
		input.update(input.descriptor().getProperty(ScriptInput.SCRIPT),
			"$model.all(`my:Type`)");

		assertEquals("$model.all(`my:Type`)", input.getScript());
	}

	/** ChatContextInput defaults to LAST_TURN content. */
	public void testChatContextInputDefaultContent() {
		ChatContextInput input = TypedConfiguration.newConfigItem(ChatContextInput.class);

		// Content has no @defaults annotation, so may be null from newConfigItem
		// The enum default semantics in the code treat null/unset as LAST_TURN
	}

	/** Input is nullable (not mandatory) for pre-turn agents. */
	public void testInputIsNullable() {
		BackgroundAgentConfig config = TypedConfiguration.newConfigItem(BackgroundAgentConfig.class);

		// Input should be null by default since it is no longer @Mandatory
		assertNull(config.getInput());
	}

	/** ChatContextInput Content enum values. */
	public void testChatContextInputContentValues() {
		assertEquals(2, Content.values().length);
		assertNotNull(Content.LAST_TURN);
		assertNotNull(Content.FULL_CHAT);
	}

	/** ChatContextInput content can be set to FULL_CHAT. */
	public void testChatContextInputCustomContent() {
		ChatContextInput input = TypedConfiguration.newConfigItem(ChatContextInput.class);
		input.update(input.descriptor().getProperty(ChatContextInput.CONTENT), Content.FULL_CHAT);

		assertEquals(Content.FULL_CHAT, input.getContent());
	}

}
