/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.environment.EnvironmentFact;
import com.top_logic.tl.ai.core.agent.environment.RunFactsBinding;

/**
 * Tests how the facts of one run are assembled: what the agent states, plus what the entry point
 * bound for the run.
 *
 * @see RunFactsBinding
 */
public class TestRunFacts extends TestCase {

	private static AgentDefinition agent() {
		return new AgentDefinition("TestAgent", null, "d", null, "", List.of(), List.of(), List.of(),
			List.of(), AgentSource.CONFIG);
	}

	/** A headless run of an agent that states nothing is told nothing by the page either. */
	public void testAHeadlessRunHasNoPageFacts() {
		assertEquals(List.of(), AgentFactory.runFacts(agent()));
	}

	/** What a chat bound for the run reaches the agent that runs in it. */
	public void testTheBoundFactsReachTheRun() {
		EnvironmentFact project = new EnvironmentFact("Project", "Airport expansion", true);
		try (RunFactsBinding.Binding binding = RunFactsBinding.bind(List.of(project))) {
			assertEquals(List.of(project), AgentFactory.runFacts(agent()));
			assertEquals(List.of(project), RunFactsBinding.facts());
		}

		assertEquals("The binding must not outlive its scope - the chat runs on a cached thread"
			+ " pool, where the next run would inherit them.", List.of(), RunFactsBinding.facts());
	}

	/** A nested scope restores what it found rather than clearing the binding. */
	public void testANestedScopeRestoresTheOuterFacts() {
		EnvironmentFact outer = new EnvironmentFact("Project", "Airport", true);
		EnvironmentFact inner = new EnvironmentFact("Project", "Harbour", true);
		try (RunFactsBinding.Binding outerBinding = RunFactsBinding.bind(List.of(outer))) {
			try (RunFactsBinding.Binding innerBinding = RunFactsBinding.bind(List.of(inner))) {
				assertEquals(List.of(inner), RunFactsBinding.facts());
			}
			assertEquals(List.of(outer), RunFactsBinding.facts());
		}
	}

	/** Binding nothing is binding nothing, not binding an empty list nobody can tell apart. */
	public void testBindingNothingLeavesNoFacts() {
		try (RunFactsBinding.Binding binding = RunFactsBinding.bind(null)) {
			assertEquals(List.of(), RunFactsBinding.facts());
		}
		try (RunFactsBinding.Binding binding = RunFactsBinding.bind(List.of())) {
			assertEquals(List.of(), RunFactsBinding.facts());
		}
	}

	/** A fact must be called something. */
	public void testAFactNeedsAName() {
		try {
			new EnvironmentFact("  ", "value", true);
			fail("A fact without a name says nothing.");
		} catch (IllegalArgumentException expected) {
			// Expected.
		}
	}

	/** A value nobody computed is the empty one, never null. */
	public void testAMissingValueIsEmpty() {
		assertEquals("", new EnvironmentFact("Project", null, true).value());
	}
}
