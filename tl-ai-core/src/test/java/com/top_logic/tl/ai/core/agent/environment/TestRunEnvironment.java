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
