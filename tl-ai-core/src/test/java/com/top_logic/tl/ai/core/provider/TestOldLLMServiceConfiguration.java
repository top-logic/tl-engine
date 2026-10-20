/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.tl.ai.core.agent.ServiceConfigFixture;

/**
 * Tests that an application's configuration of the former {@link LLMService} is still read - and
 * refused with a sentence naming where each of its sections now belongs (Gitea #691).
 *
 * <p>
 * The engine has no alias for a renamed service: without the refusal such a file would fail the boot
 * with "class not found" and name no new place, and a file that the engine did not fail on would be
 * read by nobody, so the application would run on tl-ai's defaults without a word.
 * </p>
 */
public class TestOldLLMServiceConfiguration extends TestCase {

	/** Reading a configuration resolves classes through the {@code TypeIndex}. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestOldLLMServiceConfiguration.class));
	}

	/** An application's LLMService file from before the split, which sets every section there was. */
	private static final String OLD = ServiceConfigFixture.file(LLMService.class.getName(), """
					<instance class="com.top_logic.tl.ai.core.provider.LLMService">
						<providers>
							<provider class="com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig"
								api-key="unencrypted:test"
								models="app-model"
								name="anthropic"
							/>
						</providers>
						<agent-execution chat-entry-point="App_Agent" default-model="app-model" default-provider="anthropic">
							<agent-overrides>
								<agent name="App_Agent" max-turns="60"/>
							</agent-overrides>
						</agent-execution>
						<environment user-preferences="false" max-preferences-length="300">
							<template><en>Hello.</en></template>
							<application><en>Demo</en></application>
						</environment>
						<user-memory enabled="false"/>
						<run-limits chat-workers="8" chat-queue="2" concurrent-runs-per-account="3"
							token-budget="1000" budget-window-hours="12"
						>
							<role-budgets>
								<role-budget name="tl.admin" tokens="0"/>
							</role-budgets>
						</run-limits>
						<object-ids>
							<hints>
								<hint pattern="ORD-[0-9]+" explanation="is an order number."/>
							</hints>
						</object-ids>
						<security-context class="com.top_logic.tl.ai.core.security.DefaultSecurityContextFactory"/>
						<prompt-contributors>
							<contributor class="com.top_logic.tl.ai.core.agent.TextPromptContributor">
								<text><en>House rule.</en></text>
							</contributor>
						</prompt-contributors>
						<failover quota-ttl-seconds="60"/>
						<model-catalog>
							<llm-configuration>
								<model name="app-model" context-window="1000" max-output="100"/>
							</llm-configuration>
						</model-catalog>
					</instance>""");

	/** Every section is named with its new place, and the message names the four services. */
	public void testAnOldFileIsReadAndRefused() throws Exception {
		String refusal = LLMService.refusal(ServiceConfigFixture.read(LLMService.class, OLD));

		assertNotNull(refusal);
		for (String move : new String[] {
			"<providers> to LLMProviderService <providers>",
			"<model-catalog> to LLMProviderService <model-catalog>",
			"<failover> to LLMProviderService <failover>",
			"<agent-execution default-model> to AgentRuntimeService default-model",
			"<agent-execution default-provider> to AgentRuntimeService default-provider",
			"<agent-execution chat-entry-point> to AgentRuntimeService chat-entry-point",
			"<agent-execution><agent-overrides> to AgentRuntimeService <agent-overrides>",
			"<security-context> to AgentRuntimeService <security-context>",
			"<run-limits chat-workers> to AgentRuntimeService <capacity chat-workers>",
			"<run-limits chat-queue> to AgentRuntimeService <capacity chat-queue>",
			"<run-limits concurrent-runs-per-account> to AgentRuntimeService <capacity concurrent-runs-per-account>",
			"<run-limits token-budget> to AgentPolicyService <budget token-budget>",
			"<run-limits budget-window-hours> to AgentPolicyService <budget budget-window-hours>",
			"<run-limits><role-budgets> to AgentPolicyService <budget><role-budgets>",
			"<environment><template> to AgentApplicationService <environment><template>",
			"<environment><application> to AgentApplicationService <environment><application>",
			"<environment user-preferences> to AgentPolicyService user-preferences",
			"<environment max-preferences-length> to AgentPolicyService max-preferences-length",
			"<object-ids> to AgentApplicationService <object-ids>",
			"<prompt-contributors> to AgentApplicationService <prompt-contributors>",
			"<user-memory> to AgentPolicyService <user-memory>",
		}) {
			assertTrue("Expected '" + move + "' in: " + refusal, refusal.contains(move));
		}
		assertTrue(refusal, refusal.contains(LLMService.MIGRATION_PAGE));
		assertTrue(refusal, refusal.contains("com.top_logic.tl.ai.core.agent.AgentPolicyService"));
	}

	/** Only what the file sets is named: a file with providers alone is told about providers alone. */
	public void testOnlyWhatIsSetIsNamed() throws Exception {
		String refusal = LLMService.refusal(ServiceConfigFixture.read(LLMService.class,
			ServiceConfigFixture.file(LLMService.class.getName(), """
					<instance class="com.top_logic.tl.ai.core.provider.LLMService">
						<agent-execution default-model="app-model"/>
						<run-limits/>
						<environment/>
					</instance>""")));

		assertTrue(refusal, refusal.contains("<agent-execution default-model> to AgentRuntimeService default-model"));
		assertFalse("An empty element is not a setting: " + refusal, refusal.contains("<run-limits"));
		assertFalse(refusal, refusal.contains("<environment"));
	}

	/** A file that sets nothing - empty elements only - is no reason to stop the application. */
	public void testAnEmptyConfigurationIsNotRefused() throws Exception {
		assertNull(LLMService.refusal(ServiceConfigFixture.read(LLMService.class,
			ServiceConfigFixture.file(LLMService.class.getName(), """
					<instance class="com.top_logic.tl.ai.core.provider.LLMService">
						<model-catalog/>
						<run-limits/>
					</instance>"""))));
		assertNull(LLMService.refusal(null));
	}
}
