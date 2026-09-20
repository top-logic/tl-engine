/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The text of the environment block, before its placeholders are filled.
 *
 * <p>
 * The shipped template lives in configuration, localized, so that an installation can reword it
 * without a build - see {@link EnvironmentConfig#getTemplate()}. {@link #DEFAULT} is the same text
 * in English and is what an application that configures none falls back to; a test pins the two
 * together so the shipped configuration cannot drift away from this constant.
 * </p>
 *
 * <p>
 * The template names {@code {{context}}}, {@code {{turn_context}}}, {@code {{preferences}}} and
 * {@code {{memories}}} as whole sections rather than as bare values: each of them renders its own
 * heading, or nothing at all when there is nothing to say. A section that is switched off or empty
 * therefore leaves no orphaned heading behind.
 * </p>
 */
public class EnvironmentTemplate {

	/**
	 * The shipped English template.
	 *
	 * <p>
	 * It states which of the two sections wins, because they can disagree: the application knows
	 * the situation, the user states a wish, and a user wish must not reinterpret what the
	 * application says is the case.
	 * </p>
	 */
	public static final String DEFAULT = """
		<environment>
		You run inside the TopLogic application "{{application}}" as the assistant of one user.
		User: {{user}}. Address the user the way they ask to be addressed in their preferences.
		Language: {{language}}. Answer only in this language.
		Date: {{date}}.
		Agent: {{agent}}. Model: {{model}}.
		What the application states below is authoritative. Where the application context and the user preferences disagree, follow the application context.
		{{context}}
		{{turn_context}}
		{{preferences}}
		{{memories}}
		</environment>""";

	/** A line holding one placeholder and nothing else. */
	private static final Pattern LINE_PLACEHOLDER =
		Pattern.compile("\\s*\\{\\{\\s*(\\w+)\\s*\\}\\}\\s*");

	private EnvironmentTemplate() {
		// Static utility.
	}

	/**
	 * Fills the template and tidies the result.
	 *
	 * <p>
	 * {@code {{environment}}} is not filled here: the block cannot contain itself, and a template
	 * that names it would otherwise recurse. It is simply dropped.
	 * </p>
	 *
	 * @param template
	 *        The template text.
	 * @param values
	 *        The value per placeholder name.
	 * @return The composed block, without trailing whitespace and without the blank lines an empty
	 *         section leaves behind.
	 */
	public static String render(String template, Map<String, String> values) {
		String pruned = dropEmptySections(template, values);
		return tidy(PromptPlaceholders.fill(pruned, values));
	}

	/**
	 * A line that is nothing but a placeholder with nothing to say is not a line.
	 *
	 * <p>
	 * That is how a section disappears whole: {@code {{context}}}, {@code {{turn_context}}},
	 * {@code {{preferences}}} and {@code {{memories}}} stand on lines of their own and render their
	 * own heading, so an account that wrote no preferences - or an installation that switched them
	 * off - leaves no heading and no gap. A placeholder inside a sentence is left to
	 * {@link PromptPlaceholders#fill}, because the rest of that sentence is still meant.
	 * </p>
	 */
	private static String dropEmptySections(String template, Map<String, String> values) {
		if (template == null || template.isEmpty()) {
			return template;
		}
		StringBuilder result = new StringBuilder(template.length());
		boolean first = true;
		for (String line : template.split("\n", -1)) {
			Matcher alone = LINE_PLACEHOLDER.matcher(line);
			if (alone.matches() && isEmpty(values.get(alone.group(1)))) {
				continue;
			}
			if (!first) {
				result.append('\n');
			}
			first = false;
			result.append(line);
		}
		return result.toString();
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	/**
	 * Removes trailing spaces per line and collapses the runs of blank lines that an empty section
	 * leaves, so the block a run sends is the block a reader would write by hand.
	 */
	static String tidy(String text) {
		if (text == null) {
			return null;
		}
		String[] lines = text.split("\n", -1);
		StringBuilder result = new StringBuilder(text.length());
		boolean previousBlank = false;
		for (String line : lines) {
			String stripped = line.stripTrailing();
			boolean blank = stripped.isEmpty();
			if (blank && previousBlank) {
				continue;
			}
			if (result.length() > 0) {
				result.append('\n');
			}
			result.append(stripped);
			previousBlank = blank;
		}
		return result.toString().strip();
	}
}
