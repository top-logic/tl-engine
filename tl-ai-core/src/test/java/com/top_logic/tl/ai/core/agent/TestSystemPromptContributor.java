/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.provider.AgentModelAssignment;
import com.top_logic.tl.ai.core.provider.LLMService;

/**
 * Tests for the {@link SystemPromptContributor} seam: an installation adds standing instructions to
 * every agent's system prompt without editing an agent (Gitea #381).
 */
public class TestSystemPromptContributor extends TestCase {

	/**
	 * Boots the module the typed configuration needs to resolve a polymorphic default.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestSystemPromptContributor.class));
	}

	private static final AgentDefinition AGENT = new AgentDefinition("albert", null, "", null,
		"You are Albert.", List.of(), List.of(), List.of(), List.of(), AgentSource.CONFIG);

	private static final AgentModelAssignment ASSIGNMENT = new AgentModelAssignment("test-model",
		null, null, null, null, null, null, null, null, null, null, null, null, null, null);

	/**
	 * An installation that configures no contributor gets the prompt it had before the seam existed.
	 */
	public void testNoContributorChangesNothing() {
		assertEquals("You are Albert.",
			AgentFactory.appendContributions(AGENT, ASSIGNMENT, "You are Albert.", List.of()));
	}

	/**
	 * The contribution lands after the agent's own prompt, separated by a blank line.
	 */
	public void testContributionIsAppended() {
		String prompt = AgentFactory.appendContributions(AGENT, ASSIGNMENT, "You are Albert.",
			List.of((agent, assignment) -> "House rule: never quote a price."));

		assertEquals("You are Albert.\n\nHouse rule: never quote a price.", prompt);
	}

	/**
	 * Several contributors are appended in configuration order.
	 */
	public void testContributionsKeepConfigurationOrder() {
		String prompt = AgentFactory.appendContributions(AGENT, ASSIGNMENT, "Base.",
			List.of((agent, assignment) -> "First.", (agent, assignment) -> "Second."));

		assertEquals("Base.\n\nFirst.\n\nSecond.", prompt);
	}

	/**
	 * A contributor is told which agent and which model the run is.
	 */
	public void testContributorIsToldAboutTheRun() {
		String prompt = AgentFactory.appendContributions(AGENT, ASSIGNMENT, "Base.",
			List.of((agent, assignment) -> agent.name() + " on " + assignment.model()));

		assertEquals("Base.\n\nalbert on test-model", prompt);
	}

	/**
	 * A contributor with nothing to say for this run adds no blank lines.
	 */
	public void testNothingToSayAddsNothing() {
		String prompt = AgentFactory.appendContributions(AGENT, ASSIGNMENT, "Base.",
			List.of((agent, assignment) -> null, (agent, assignment) -> "   "));

		assertEquals("Base.", prompt);
	}

	/**
	 * A contributor that fails costs its own section and not the run.
	 */
	public void testFailingContributorIsSkipped() {
		String prompt = AgentFactory.appendContributions(AGENT, ASSIGNMENT, "Base.",
			List.of((agent, assignment) -> {
				throw new IllegalStateException("No client for this account.");
			}, (agent, assignment) -> "Still here."));

		assertEquals("Base.\n\nStill here.", prompt);
	}

	/**
	 * Nothing is configured by default.
	 */
	public void testDefaultIsEmpty() {
		LLMService.Config<?> config = TypedConfiguration.newConfigItem(LLMService.Config.class);

		assertTrue("No contributor is shipped.", config.getPromptContributors().isEmpty());
	}

}
