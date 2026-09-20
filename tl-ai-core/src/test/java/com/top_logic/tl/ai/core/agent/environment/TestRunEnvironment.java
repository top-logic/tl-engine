/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Tests for {@link RunEnvironment}: where the block lands in a prompt, and what a run carries into
 * it.
 */
public class TestRunEnvironment extends TestCase {

	private static final String TEMPLATE = "<environment>\nUser: {{user}}.\n{{context}}\n"
		+ "{{preferences}}\n</environment>";

	private static RunEnvironment environment(String context, String preferences) {
		Map<String, String> values = new LinkedHashMap<>();
		values.put(PromptPlaceholders.USER, "Ada Lovelace");
		values.put(PromptPlaceholders.CONTEXT, context);
		values.put(PromptPlaceholders.PREFERENCES, preferences);
		return RunEnvironment.of(TEMPLATE, values);
	}

	/** A prompt that names no placeholder gets the block appended, which is every shipped one. */
	public void testAPromptWithoutPlaceholdersGetsTheBlockAppended() {
		String prompt = environment("", "").apply("You are a helpful assistant.");

		assertTrue(prompt, prompt.startsWith("You are a helpful assistant.\n\n<environment>"));
		assertTrue(prompt, prompt.endsWith("</environment>"));
	}

	/** A prompt that names {@code {{environment}}} decides where the block goes. */
	public void testAPromptWithThePlaceholderDecidesWhereTheBlockGoes() {
		String prompt = environment("", "").apply("Before.\n{{environment}}\nAfter.");

		assertTrue(prompt, prompt.startsWith("Before.\n<environment>"));
		assertTrue(prompt, prompt.endsWith("</environment>\nAfter."));
		assertEquals("The block must appear exactly once.", 1, count(prompt, "<environment>"));
	}

	/** The other placeholders are filled wherever the prompt uses them. */
	public void testThePromptsOwnPlaceholdersAreFilled() {
		String prompt = environment("", "").apply("Greet {{user}} by name. {{environment}}");

		assertTrue(prompt, prompt.startsWith("Greet Ada Lovelace by name."));
	}

	/** An agent with no prompt of its own still gets the block, and nothing else. */
	public void testAnEmptyPromptIsJustTheBlock() {
		assertEquals(environment("", "").block(), environment("", "").apply(null));
		assertEquals(environment("", "").block(), environment("", "").apply("   "));
	}

	/**
	 * The block is byte-identical for two runs told the same thing.
	 *
	 * <p>
	 * This is the property the providers' prompt caches depend on: the block sits in front of every
	 * request of a conversation, and one changed character there invalidates the cache for all of
	 * them.
	 * </p>
	 */
	public void testTheBlockIsByteIdenticalForTheSameRun() {
		assertEquals(environment("Context:\n- Project: Airport", "Keep it short.").block(),
			environment("Context:\n- Project: Airport", "Keep it short.").block());
	}

	/** A section with nothing to say is not in the block at all. */
	public void testAnEmptySectionIsAbsent() {
		String block = environment("", "").block();

		assertEquals("<environment>\nUser: Ada Lovelace.\n</environment>", block);
	}

	/** Only a stable fact reaches the context section; the others are carried and dropped. */
	public void testOnlyStableFactsAreRendered() {
		List<EnvironmentFact> facts = List.of(
			new EnvironmentFact("Project", "Airport expansion", true),
			new EnvironmentFact("Selection", "Invoice 4711", false),
			new EnvironmentFact("Tenant", "ACME", true));

		String section = RunEnvironment.contextSection(facts, "Application context:");

		assertTrue(section, section.contains("- Project: Airport expansion"));
		assertTrue(section, section.contains("- Tenant: ACME"));
		assertFalse("A fact that is not stable must not reach the system prompt: " + section,
			section.contains("Selection"));
	}

	/** Nothing but non-stable facts is nothing to say. */
	public void testOnlyUnstableFactsRenderNoSection() {
		assertEquals("", RunEnvironment.contextSection(
			List.of(new EnvironmentFact("Selection", "Invoice 4711", false)), "Application context:"));
		assertEquals("", RunEnvironment.contextSection(List.of(), "Application context:"));
		assertEquals("", RunEnvironment.contextSection(null, "Application context:"));
	}

	/**
	 * A run that will send a {@code <turn-context>} block is told what one is; a run that will not
	 * is told nothing about it (Gitea #494).
	 */
	public void testTheTurnContextNoteFollowsThePerTurnFacts() {
		List<EnvironmentFact> perTurn = List.of(
			new EnvironmentFact("Project", "Airport expansion", true),
			new EnvironmentFact("Selected invoice", "Hosting August", false));

		assertEquals("A run that sends a per-turn block says what it is.",
			TurnContext.NOTE, RunEnvironment.turnContextSection(perTurn, TurnContext.NOTE));
		assertEquals("Nothing per turn, no block, and therefore nothing to explain.",
			"", RunEnvironment.turnContextSection(
				List.of(new EnvironmentFact("Project", "Airport expansion", true)), TurnContext.NOTE));
		assertEquals("", RunEnvironment.turnContextSection(List.of(), TurnContext.NOTE));
		assertEquals("", RunEnvironment.turnContextSection(null, TurnContext.NOTE));
	}

	/**
	 * The two halves cannot disagree: whatever the note claims, the block is composed by the same
	 * predicate.
	 */
	public void testTheNoteIsThereExactlyWhenTheBlockIs() {
		List<List<EnvironmentFact>> cases = List.of(
			List.of(),
			List.of(new EnvironmentFact("Project", "Airport", true)),
			List.of(new EnvironmentFact("Selected invoice", "Hosting August", false)),
			List.of(new EnvironmentFact("Project", "Airport", true),
				new EnvironmentFact("Selected invoice", "Hosting August", false)));

		for (List<EnvironmentFact> facts : cases) {
			boolean explained = !RunEnvironment.turnContextSection(facts, TurnContext.NOTE).isEmpty();
			boolean sent = !TurnContext.render(facts, 1, "Turn", "Time", "14:32").isEmpty();

			assertEquals("Explained and sent must agree for " + facts, Boolean.valueOf(sent),
				Boolean.valueOf(explained));
		}
	}

	/** An installation that reworded the note to nothing is not given the shipped one back. */
	public void testAnEmptyNoteRendersNoSection() {
		List<EnvironmentFact> perTurn =
			List.of(new EnvironmentFact("Selected invoice", "Hosting August", false));

		assertEquals("", RunEnvironment.turnContextSection(perTurn, ""));
		assertEquals("", RunEnvironment.turnContextSection(perTurn, "   "));
		assertEquals("", RunEnvironment.turnContextSection(perTurn, null));
	}

	/** No preference text - the account wrote none, or the administrator switched them off. */
	public void testNoPreferenceTextRendersNoSection() {
		assertEquals("", RunEnvironment.preferencesSection(null, "User preferences:"));
		assertEquals("", RunEnvironment.preferencesSection("   ", "User preferences:"));
	}

	private static int count(String text, String part) {
		int found = 0;
		for (int at = text.indexOf(part); at >= 0; at = text.indexOf(part, at + part.length())) {
			found++;
		}
		return found;
	}
}
