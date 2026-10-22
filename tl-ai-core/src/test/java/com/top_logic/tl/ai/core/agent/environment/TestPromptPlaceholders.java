/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Tests for {@link PromptPlaceholders}: which {@code {{name}}} a prompt may use, and what happens
 * to the ones it may not.
 */
public class TestPromptPlaceholders extends TestCase {

	/** Every name the harness offers is filled from the map. */
	public void testKnownPlaceholdersAreFilled() {
		String filled = PromptPlaceholders.fill(
			"Hello {{user}}, today is {{date}} and you run {{model}}.",
			Map.of("user", "Ada", "date", "2026-09-19", "model", "claude-opus-5"));

		assertEquals("Hello Ada, today is 2026-09-19 and you run claude-opus-5.", filled);
	}

	/** Spaces inside the braces are part of writing it by hand. */
	public void testSpacesInsideTheBracesAreAllowed() {
		assertEquals("Ada", PromptPlaceholders.fill("{{ user }}", Map.of("user", "Ada")));
	}

	/**
	 * A known name nobody answered becomes empty rather than staying in the prompt.
	 *
	 * <p>
	 * The harness offers the name, so a run in which nothing is known about it - a headless run has
	 * no page facts - must not leave the raw braces where the model reads them as an instruction.
	 * </p>
	 */
	public void testAKnownNameWithoutAValueIsEmptied() {
		assertEquals("Context: ", PromptPlaceholders.fill("Context: {{context}}", Map.of()));
	}

	/** An unknown name is reported, in order of first appearance and without repetition. */
	public void testUnknownNamesAreReported() {
		List<String> unknown = PromptPlaceholders.unknownIn(
			"{{projekt}} {{user}} {{projekt}} {{tenant}}");

		assertEquals(List.of("projekt", "tenant"), unknown);
	}

	/** A prompt that uses only known names is accepted. */
	public void testAWellFormedPromptHasNoUnknownNames() {
		assertEquals(List.of(), PromptPlaceholders.unknownIn(
			"{{user}} {{language}} {{date}} {{agent}} {{model}} {{application}} {{context}}"
				+ " {{preferences}} {{environment}}"));
	}

	/**
	 * A JSON example is not a placeholder.
	 *
	 * <p>
	 * Shipped prompts show the model JSON, and a nested object ends in {@code }}}. Refusing an agent
	 * over that would make the check the thing people work around.
	 * </p>
	 */
	public void testJsonBracesAreNotPlaceholders() {
		String json = "Answer with {\"a\": {\"b\": 1}} and nothing else.";

		assertEquals(List.of(), PromptPlaceholders.unknownIn(json));
		assertEquals(json, PromptPlaceholders.fill(json, Map.of()));
	}

	/** An unknown name that reaches the filler anyway is shown rather than mangled. */
	public void testAnUnknownNameSurvivesFilling() {
		assertEquals("{{projekt}}", PromptPlaceholders.fill("{{projekt}}", Map.of()));
	}

	/** A value containing a dollar or a backslash is inserted literally. */
	public void testAValueIsNotReadAsAReplacementPattern() {
		assertEquals("C:\\tmp $1", PromptPlaceholders.fill("{{user}}", Map.of("user", "C:\\tmp $1")));
	}

	/** {@code uses} answers for the one name the block placement depends on. */
	public void testUsesFindsTheEnvironmentPlaceholder() {
		assertTrue(PromptPlaceholders.uses("a {{environment}} b", PromptPlaceholders.ENVIRONMENT));
		assertFalse(PromptPlaceholders.uses("a {{context}} b", PromptPlaceholders.ENVIRONMENT));
		assertFalse(PromptPlaceholders.uses(null, PromptPlaceholders.ENVIRONMENT));
	}

	/** Null and empty are prompts too. */
	public void testNothingIsWellFormed() {
		assertEquals(List.of(), PromptPlaceholders.unknownIn(null));
		assertEquals(List.of(), PromptPlaceholders.unknownIn(""));
		assertNull(PromptPlaceholders.fill(null, Map.of()));
	}

	/** The error message names what a writer may use. */
	public void testKnownNamesReadAsAList() {
		String names = PromptPlaceholders.knownNames();

		assertTrue(names, names.contains("{{user}}"));
		assertTrue(names, names.contains("{{environment}}"));
	}
}
