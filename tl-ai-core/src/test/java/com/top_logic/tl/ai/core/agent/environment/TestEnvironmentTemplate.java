/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * Tests for {@link EnvironmentTemplate}: the shipped wording, and how a section that has nothing to
 * say leaves no trace.
 */
public class TestEnvironmentTemplate extends TestCase {

	/** Where the shipped, localized template lives. */
	private static final String BUNDLE =
		"src/main/webapp/WEB-INF/conf/resources/tl-ai-core.app-messages_%s.properties";

	/** Values for every placeholder but the sections, which each test sets itself. */
	private static Map<String, String> values() {
		Map<String, String> values = new LinkedHashMap<>();
		values.put(PromptPlaceholders.APPLICATION, "Demo");
		values.put(PromptPlaceholders.USER, "Ada Lovelace");
		values.put(PromptPlaceholders.LANGUAGE, "English (en)");
		values.put(PromptPlaceholders.DATE, "2026-09-19 (Europe/Berlin)");
		values.put(PromptPlaceholders.AGENT, "Albert");
		values.put(PromptPlaceholders.MODEL, "claude-opus-5");
		values.put(PromptPlaceholders.CONTEXT, "");
		values.put(PromptPlaceholders.PREFERENCES, "");
		return values;
	}

	/** The shipped default carries everything the harness knows about the run. */
	public void testTheDefaultUsesEveryPlaceholderItCanFill() {
		String template = EnvironmentTemplate.DEFAULT;

		for (String name : PromptPlaceholders.NAMES) {
			if (PromptPlaceholders.ENVIRONMENT.equals(name)) {
				// The block cannot contain itself.
				assertFalse("The template must not name itself.",
					PromptPlaceholders.uses(template, name));
				continue;
			}
			assertTrue("The shipped template does not use {{" + name + "}}.",
				PromptPlaceholders.uses(template, name));
		}
	}

	/** It says which of the two sections wins, because they can disagree. */
	public void testTheDefaultStatesThatTheApplicationWins() {
		String template = EnvironmentTemplate.DEFAULT.toLowerCase();

		assertTrue("The template must tell the model that the application context outranks the"
			+ " user preferences.", template.contains("application context"));
		assertTrue(template.contains("authoritative"));
	}

	/**
	 * The language holds for every text the user sees, not only for the final answer. "Answer only
	 * in this language" let a model write its notes between tool calls in English in a German
	 * conversation: it read them as no part of the answer.
	 */
	public void testTheLanguageHoldsForTheNotesBetweenToolCalls() throws IOException {
		assertTrue(EnvironmentTemplate.DEFAULT, EnvironmentTemplate.DEFAULT
			.contains("Write every text the user sees in this language: the notes between tool calls"));
		assertTrue(shippedTemplate("de"), shippedTemplate("de")
			.contains("Schreiben Sie jeden Text, den der Benutzer sieht, in dieser Sprache: die Notizen"
				+ " zwischen Tool-Aufrufen"));
	}

	/**
	 * The English text an installation gets is the one this class documents.
	 *
	 * <p>
	 * The two exist because the shipped template is a localized resource, which is what an
	 * installation reads and a translator edits, while the constant is the last-resort fallback for
	 * a run that cannot reach it. Nothing but this test keeps them from drifting apart.
	 * </p>
	 */
	public void testTheShippedResourceMatchesTheDefault() throws IOException {
		assertEquals("The shipped English template drifted away from EnvironmentTemplate.DEFAULT.",
			EnvironmentTemplate.DEFAULT.strip(), shippedTemplate("en").strip());
	}

	/**
	 * Every language this module ships has the template, and each uses the same placeholders.
	 *
	 * <p>
	 * A translation that drops one silently removes what it named from every run in that language -
	 * the user's own preferences, say - and nothing in the product says so.
	 * </p>
	 */
	public void testEveryShippedLanguageCarriesTheSamePlaceholders() throws IOException {
		String english = shippedTemplate("en");
		for (String language : List.of("de")) {
			String translated = shippedTemplate(language);
			for (String name : PromptPlaceholders.NAMES) {
				assertEquals("The " + language + " template must use {{" + name + "}} exactly as the"
					+ " English one does.", Boolean.valueOf(PromptPlaceholders.uses(english, name)),
					Boolean.valueOf(PromptPlaceholders.uses(translated, name)));
			}
		}
	}

	/**
	 * The shipped sentence about the per-turn block is the one {@link TurnContext#NOTE} documents.
	 *
	 * <p>
	 * Same reason as the template above: the resource is what an installation reads and a translator
	 * edits, the constant is the fallback for a run that cannot reach the bundle, and nothing but
	 * this test keeps the two together.
	 * </p>
	 */
	public void testTheShippedTurnContextNoteMatchesTheConstant() throws IOException {
		assertEquals("The shipped English note drifted away from TurnContext.NOTE.",
			TurnContext.NOTE.strip(), shipped("en", EnvironmentSettings.TURN_CONTEXT_RESOURCE).strip());
	}

	/** Every language this module ships says what a {@code <turn-context>} block is. */
	public void testEveryShippedLanguageExplainsTheTurnContextBlock() throws IOException {
		for (String language : List.of("en", "de")) {
			String note = shipped(language, EnvironmentSettings.TURN_CONTEXT_RESOURCE);

			assertTrue("The " + language + " note must name the block it is about: " + note,
				note.contains(TurnContext.OPEN_TAG));
		}
	}

	/** The template as the given language's resource bundle holds it. */
	private static String shippedTemplate(String language) throws IOException {
		return shipped(language, EnvironmentSettings.TEMPLATE_RESOURCE);
	}

	/** One resource of the given language's shipped bundle. */
	private static String shipped(String language, String key) throws IOException {
		Path bundle = moduleRoot().resolve(String.format(BUNDLE, language));
		assertTrue("Not found: " + bundle, Files.isRegularFile(bundle));
		Properties properties = new Properties();
		try (Reader reader =
			Files.newBufferedReader(bundle, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		String text = properties.getProperty(key);
		assertNotNull("No '" + key + "' in " + bundle, text);
		return text;
	}

	/** A run with facts and preferences renders both sections. */
	public void testBothSectionsAreRendered() {
		Map<String, String> values = values();
		values.put(PromptPlaceholders.CONTEXT, "Application context:\n- Project: Airport");
		values.put(PromptPlaceholders.PREFERENCES, "User preferences:\nKeep it short.");

		String block = EnvironmentTemplate.render(EnvironmentTemplate.DEFAULT, values);

		assertTrue(block, block.contains("- Project: Airport"));
		assertTrue(block, block.contains("Keep it short."));
		assertTrue(block, block.startsWith("<environment>"));
		assertTrue(block, block.endsWith("</environment>"));
	}

	/**
	 * A section with nothing to say leaves no blank line behind.
	 *
	 * <p>
	 * This is what makes the administrator's switch and an empty preference text invisible rather
	 * than a gap in the middle of the block.
	 * </p>
	 */
	public void testAnEmptySectionLeavesNoGap() {
		String block = EnvironmentTemplate.render(EnvironmentTemplate.DEFAULT, values());

		assertFalse("An empty section must not leave a blank line:\n" + block,
			block.contains("\n\n"));
		assertFalse(block, block.contains("{{"));
	}

	/** The block never contains the placeholder for itself. */
	public void testTheBlockDoesNotContainItself() {
		Map<String, String> values = values();
		String block = EnvironmentTemplate.render(
			"<environment>{{environment}} {{user}}</environment>", values);

		assertEquals("<environment> Ada Lovelace</environment>", block);
	}

	/** Trailing spaces and runs of blank lines are removed; a single one survives. */
	public void testTidyKeepsOneBlankLine() {
		assertEquals("a\n\nb", EnvironmentTemplate.tidy("a   \n\n\n\nb   \n\n"));
	}

	/** The directory of the tl-ai-core module, whatever the working directory is. */
	private static Path moduleRoot() throws IOException {
		String marker = String.format(BUNDLE, "en");
		for (Path candidate = Path.of("").toAbsolutePath(); candidate != null; candidate =
			candidate.getParent()) {
			if (Files.isRegularFile(candidate.resolve(marker))) {
				return candidate;
			}
			Path module = candidate.resolve("tl-ai-core");
			if (Files.isRegularFile(module.resolve(marker))) {
				return module;
			}
		}
		throw new IOException("No tl-ai-core module found at or above " + Path.of("").toAbsolutePath());
	}
}
