/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parts of an artifact a reader can ask for by name: the headings of a Markdown text, the members of
 * a JSON object.
 *
 * <p>
 * Lets an agent read one requirement of a specification or one entry of a token set instead of the
 * whole artifact. An {@link com.top_logic.ai.a2a.A2AArtifactSource} uses it for content it has no
 * better structure for.
 * </p>
 */
public final class Sections {

	private Sections() {
		// Static utility.
	}

	/**
	 * The section titles of a content: Markdown headings for a text, the member names (top level) for a
	 * JSON object.
	 */
	public static List<String> list(Object content) {
		List<String> result = new ArrayList<>();
		if (content instanceof String text) {
			for (String line : text.split("\n")) {
				String title = title(line);
				if (title != null) {
					result.add(title);
				}
			}
		} else if (content instanceof Map<?, ?> map) {
			for (Object key : map.keySet()) {
				result.add(String.valueOf(key));
			}
		}
		return result;
	}

	/**
	 * One section of a content.
	 *
	 * <p>
	 * Text: the first Markdown heading whose title is the section or contains it (ignoring case), with
	 * everything up to the next heading of the same or a higher level. JSON: a member of the top-level
	 * object, or a path {@code a/b/0}.
	 * </p>
	 *
	 * @return The section, or <code>null</code> if there is none of that name.
	 */
	public static Object extract(Object content, String section) {
		if (section == null || section.isBlank()) {
			return content;
		}
		String wanted = section.trim();
		if (content instanceof String text) {
			return markdownSection(text, wanted);
		}
		Object current = content;
		for (String step : wanted.replaceFirst("^/", "").split("/")) {
			if (current instanceof Map<?, ?> map) {
				current = map.containsKey(step) ? map.get(step) : findIgnoringCase(map, step);
			} else if (current instanceof List<?> list) {
				try {
					int index = Integer.parseInt(step);
					current = index >= 0 && index < list.size() ? list.get(index) : null;
				} catch (NumberFormatException ex) {
					current = null;
				}
			} else {
				current = null;
			}
			if (current == null) {
				return null;
			}
		}
		return current;
	}

	private static Object findIgnoringCase(Map<?, ?> map, String key) {
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (String.valueOf(entry.getKey()).equalsIgnoreCase(key)) {
				return entry.getValue();
			}
		}
		return null;
	}

	private static String markdownSection(String text, String wanted) {
		String[] lines = text.split("\n", -1);
		String needle = wanted.toLowerCase();
		int start = -1;
		int level = 0;
		// An exact title first, then a title that contains the wanted text ("REQ-004" finds
		// "REQ-004 Sperrung bei Überfälligkeit").
		for (int pass = 0; pass < 2 && start < 0; pass++) {
			for (int i = 0; i < lines.length; i++) {
				int lineLevel = headingLevel(lines[i]);
				if (lineLevel == 0) {
					continue;
				}
				String title = title(lines[i]).toLowerCase();
				if (pass == 0 ? title.equals(needle) : title.contains(needle)) {
					start = i;
					level = lineLevel;
					break;
				}
			}
		}
		if (start < 0) {
			return null;
		}
		StringBuilder out = new StringBuilder(lines[start]);
		for (int i = start + 1; i < lines.length; i++) {
			int lineLevel = headingLevel(lines[i]);
			if (lineLevel > 0 && lineLevel <= level) {
				break;
			}
			out.append('\n').append(lines[i]);
		}
		return out.toString().strip();
	}

	/**
	 * The level of a Markdown ATX heading (number of leading #), 0 for no heading.
	 */
	static int headingLevel(String line) {
		String heading = heading(line);
		int level = 0;
		while (level < heading.length() && heading.charAt(level) == '#') {
			level++;
		}
		return level > 0 && level <= 6 && level < heading.length() && heading.charAt(level) == ' ' ? level : 0;
	}

	/**
	 * The title of a heading line, or <code>null</code>.
	 */
	static String title(String line) {
		int level = headingLevel(line);
		return level == 0 ? null : heading(line).substring(level).trim();
	}

	/**
	 * A line without indentation and without a list bullet: models write "- ## Title" as often as
	 * "## Title".
	 */
	private static String heading(String line) {
		String result = line.stripLeading();
		if (result.startsWith("- ") || result.startsWith("* ")) {
			result = result.substring(2).stripLeading();
		}
		return result;
	}
}
