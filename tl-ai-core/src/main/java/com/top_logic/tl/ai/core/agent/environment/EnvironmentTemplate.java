/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import java.util.Map;

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
 * The template names {@code {{context}}} and {@code {{preferences}}} as whole sections rather than
 * as bare values: each of them renders its own heading, or nothing at all when there is nothing to
 * say. A section that is switched off or empty therefore leaves no orphaned heading behind.
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
		{{preferences}}
		</environment>""";

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
		String filled = PromptPlaceholders.fill(template, values);
		return tidy(filled);
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
