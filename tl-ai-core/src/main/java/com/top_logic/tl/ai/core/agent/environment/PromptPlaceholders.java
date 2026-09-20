/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code {{name}}} placeholders a system prompt may contain, and how they are filled.
 *
 * <p>
 * The set is closed on purpose. A placeholder nobody fills would reach the model verbatim and read
 * as an instruction the harness failed to honour, so an unknown name is a configuration error
 * reported where the definition is read - see {@link #unknownIn(String)} - rather than a silent
 * pass-through at run time.
 * </p>
 *
 * @see RunEnvironment
 */
public class PromptPlaceholders {

	/** The acting account's display name. */
	public static final String USER = "user";

	/** The language the answer is expected in - the acting account's UI language. */
	public static final String LANGUAGE = "language";

	/** Today's date in the acting account's time zone, at day granularity. */
	public static final String DATE = "date";

	/** The display name of the agent this run executes. */
	public static final String AGENT = "agent";

	/** The model this run talks to. */
	public static final String MODEL = "model";

	/** The name of the application the run happens in. */
	public static final String APPLICATION = "application";

	/** The host application's stable facts, one per line. */
	public static final String CONTEXT = "context";

	/** What a {@code <turn-context>} block is, for the runs that get one. */
	public static final String TURN_CONTEXT = "turn_context";

	/** The acting account's own preferences text. */
	public static final String PREFERENCES = "preferences";

	/** What the agent kept about the acting account in earlier conversations. */
	public static final String MEMORIES = "memories";

	/** The whole environment block, composed from the template. */
	public static final String ENVIRONMENT = "environment";

	/**
	 * Every name a prompt may use, in the order the documentation lists them.
	 */
	public static final Set<String> NAMES = new LinkedHashSet<>(List.of(
		USER, LANGUAGE, DATE, AGENT, MODEL, APPLICATION, CONTEXT, TURN_CONTEXT, PREFERENCES, MEMORIES,
		ENVIRONMENT));

	/**
	 * A placeholder occurrence: {@code {{name}}}, optionally with spaces inside the braces.
	 *
	 * <p>
	 * The name is restricted to word characters, so a JSON example that happens to contain
	 * <code>{{</code> is not mistaken for a placeholder and does not fail an agent definition over
	 * text that was never meant as one.
	 * </p>
	 */
	private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*(\\w+)\\s*\\}\\}");

	private PromptPlaceholders() {
		// Static utility.
	}

	/**
	 * The placeholder names used in the given text that this class does not know, in order of first
	 * appearance and without repetition.
	 *
	 * @param text
	 *        The prompt to inspect; {@code null} is treated as empty.
	 * @return The offending names; empty when the text is well formed.
	 */
	public static List<String> unknownIn(String text) {
		if (text == null || text.isEmpty()) {
			return List.of();
		}
		Set<String> unknown = new LinkedHashSet<>();
		Matcher matcher = PLACEHOLDER.matcher(text);
		while (matcher.find()) {
			String name = matcher.group(1);
			if (!NAMES.contains(name)) {
				unknown.add(name);
			}
		}
		return List.copyOf(unknown);
	}

	/**
	 * Whether the given text uses the named placeholder.
	 */
	public static boolean uses(String text, String name) {
		if (text == null || text.isEmpty()) {
			return false;
		}
		Matcher matcher = PLACEHOLDER.matcher(text);
		while (matcher.find()) {
			if (name.equals(matcher.group(1))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Replaces every known placeholder in the text with its value.
	 *
	 * <p>
	 * A known name that the map does not answer is replaced with the empty string: the harness
	 * offers the name, so a run in which nothing is known about it must not leave the raw braces in
	 * the prompt. An unknown name is left as it stands - it was refused when the definition was
	 * read, and a text that reaches this method anyway is better shown than mangled.
	 * </p>
	 *
	 * @param text
	 *        The prompt to fill; {@code null} yields {@code null}.
	 * @param values
	 *        The value per placeholder name.
	 */
	public static String fill(String text, Map<String, String> values) {
		if (text == null || text.isEmpty()) {
			return text;
		}
		Matcher matcher = PLACEHOLDER.matcher(text);
		StringBuilder result = new StringBuilder(text.length());
		while (matcher.find()) {
			String name = matcher.group(1);
			String replacement;
			if (NAMES.contains(name)) {
				String value = values.get(name);
				replacement = value == null ? "" : value;
			} else {
				replacement = matcher.group();
			}
			matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(result);
		return result.toString();
	}

	/**
	 * The names this class knows, as a message fragment listing them for a person.
	 */
	public static String knownNames() {
		List<String> names = new ArrayList<>(NAMES.size());
		for (String name : NAMES) {
			names.add("{{" + name + "}}");
		}
		return String.join(", ", names);
	}
}
