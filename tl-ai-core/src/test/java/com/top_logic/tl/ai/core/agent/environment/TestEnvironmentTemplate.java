/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * Tests for {@link EnvironmentTemplate}: the shipped wording, and how a section that has nothing to
 * say leaves no trace.
 */
public class TestEnvironmentTemplate extends TestCase {

	/** Where the shipped, localized template lives. */
	private static final String SHIPPED =
		"src/main/webapp/WEB-INF/autoconf/com.top_logic.tl.ai.core.provider.LLMService.config.xml";

	/** The English text of the shipped template. */
	private static final Pattern ENGLISH_TEMPLATE =
		Pattern.compile("<template>\\s*<en><!\\[CDATA\\[(.*?)\\]\\]></en>", Pattern.DOTALL);

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
	 * The English text an installation gets is the one this class documents.
	 *
	 * <p>
	 * The two exist because the shipped template is localized configuration, which is what an
	 * installation edits, while the constant is what an application that configures none falls back
	 * to. Nothing but this test keeps them from drifting apart.
	 * </p>
	 */
	public void testTheShippedConfigurationMatchesTheDefault() throws IOException {
		Path shipped = moduleRoot().resolve(SHIPPED);
		assertTrue("Not found: " + shipped, Files.isRegularFile(shipped));
		Matcher english = ENGLISH_TEMPLATE.matcher(Files.readString(shipped, StandardCharsets.UTF_8));

		assertTrue("No <template><en> in " + shipped, english.find());
		assertEquals("The shipped English template drifted away from EnvironmentTemplate.DEFAULT.",
			EnvironmentTemplate.DEFAULT.strip(), english.group(1).strip());
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
		for (Path candidate = Path.of("").toAbsolutePath(); candidate != null; candidate =
			candidate.getParent()) {
			if (Files.isRegularFile(candidate.resolve(SHIPPED))) {
				return candidate;
			}
			Path module = candidate.resolve("tl-ai-core");
			if (Files.isRegularFile(module.resolve(SHIPPED))) {
				return module;
			}
		}
		throw new IOException("No tl-ai-core module found at or above " + Path.of("").toAbsolutePath());
	}
}
