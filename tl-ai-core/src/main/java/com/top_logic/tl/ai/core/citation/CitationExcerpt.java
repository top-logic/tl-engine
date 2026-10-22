/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.tl.ai.tool.api.json.JsonTree;

/**
 * What a citation's popover shows: the source in the reader's words, never in the tool's.
 *
 * <p>
 * A tool result is a transport format - JSON with Markdown inside it, or a page converted to
 * Markdown and wrapped in the untrusted-content envelope. Cutting a window out of it by character
 * count put {@code "body": "Die **Versand**…\\n\\n"} in front of a consultant (#418). Every excerpt
 * this class produces is therefore parsed rather than sliced: a JSON result is read as JSON and
 * shown as the cited object's own fields or as its prose, a text result is cut on sentence
 * boundaries, and Markdown syntax and JSON escapes are rendered away in both cases.
 * </p>
 *
 * @see CitationValidator
 */
public final class CitationExcerpt {

	/** How much of a text source to keep on either side of what was cited. */
	private static final int MARGIN = 220;

	/** Longest excerpt a popover can show without becoming a document of its own. */
	private static final int MAX = 480;

	/** Longest single value in a field listing. */
	private static final int FIELD_MAX = 160;

	/** A tool result with more fields than this is a table, not a source. */
	private static final int MAX_FIELDS = 10;

	/**
	 * From this length on, a string field is the source's prose and is shown as such rather than as
	 * one line of a field listing.
	 */
	private static final int PROSE_MIN = 120;

	/** How many times a payload may be quoted inside a payload before we stop unwrapping. */
	private static final int MAX_NESTING = 3;

	/**
	 * Every envelope a run wraps untrusted content in.
	 *
	 * <p>
	 * {@code UntrustedEnvelope} is a barrier addressed to the model, and the tool's own answer is
	 * what is inside it. A popover that shows the tag shows the transport; one that does not look
	 * past it never finds the JSON a tool actually returned.
	 * </p>
	 *
	 * @see com.top_logic.tl.ai.tool.api.UntrustedEnvelope
	 */
	private static final String ENVELOPE = "</?untrusted_[A-Za-z_]*[^>]*>";

	/** What a cut excerpt is marked with, leading and trailing. */
	private static final String ELLIPSIS = "…";

	private CitationExcerpt() {
		// Utility
	}

	/**
	 * The passage in the context it was read in.
	 *
	 * @param passage
	 *        the sentence the model copied from the source, already verified against
	 *        {@code evidence}
	 * @param evidence
	 *        this turn's tool results
	 * @return a readable window of the source containing {@link #plain(String) plain}
	 *         {@code passage}, or the passage alone when the window cannot be located
	 */
	public static String around(String passage, List<String> evidence) {
		String needle = plain(passage);
		if (needle.isEmpty()) {
			return "";
		}
		for (String blob : evidence) {
			if (blob == null || !blob.contains(passage)) {
				continue;
			}
			// In a JSON result the passage sits inside one string value. That value is the source;
			// the braces and field names around it are the transport.
			String carrier = plain(stringHolding(blob, passage));
			int at = carrier.indexOf(needle);
			if (at < 0) {
				// The passage straddles markup that rendering removed. The carrier is still the
				// right text, so show its beginning rather than nothing.
				return cut(carrier, 0, Math.min(carrier.length(), MAX));
			}
			return window(carrier, at, at + needle.length());
		}
		return needle;
	}

	/**
	 * What the popover shows for a citation that named no usable passage.
	 *
	 * @param id
	 *        the cited id, as it appears in {@code evidence}
	 * @param label
	 *        the label the model gave the source, used when nothing else can be found
	 * @param structured
	 *        whether the kind was declared {@code passage="false"}, i.e. an object with fields
	 *        rather than prose
	 * @param evidence
	 *        this turn's tool results
	 * @return field lines, a prose window, or {@code label} - never a slice of the raw result; empty for
	 *         a structured source no evidence carries as fields
	 */
	public static String ofSource(String id, String label, boolean structured, List<String> evidence) {
		if (id == null || id.isEmpty()) {
			return plain(label);
		}
		for (String blob : evidence) {
			if (blob == null || !blob.contains(id)) {
				continue;
			}
			Object tree = unwrap(parse(blob), 0);
			if (tree != null) {
				Map<String, Object> cited = locate(tree, id, true);
				if (cited == null) {
					cited = locate(tree, id, false);
				}
				if (cited != null) {
					String shown = structured ? "" : prose(cited);
					return shown.isEmpty() ? fields(cited) : shown;
				}
				// Valid JSON, but nothing in it carries this id as a value of its own. The id was
				// evidenced somewhere inside a string; the text window finds it.
			}
			if (structured) {
				// A record has fields or nothing to show. A window of a text result around its id is
				// the transport - "Event [type:...] [id:...] Next event [type:...]" - not the record.
				continue;
			}
			String text = plain(blob);
			int at = text.indexOf(plain(id));
			if (at >= 0) {
				return window(text, at, at + plain(id).length());
			}
			return cut(text, 0, Math.min(text.length(), MAX));
		}
		// The popover's head names the source already; a record without fields adds nothing to it.
		return structured ? "" : plain(label);
	}

	/**
	 * The beginning of what a tool read of a source, for a citation that named no passage.
	 *
	 * @param text
	 *        what the tool read, e.g. a page converted to Markdown
	 * @return the first sentences of {@code text}, {@link #plain(String) plain}, cut on a sentence
	 *         boundary; empty for an empty text
	 */
	public static String opening(String text) {
		String plain = plain(text);
		if (plain.isEmpty()) {
			return "";
		}
		return cut(plain, 0, sentenceEnd(plain, Math.min(plain.length(), MAX), 0));
	}

	/**
	 * Text as a reader sees it rather than as a tool wrote it: JSON escapes resolved, Markdown
	 * rendered away, whitespace collapsed.
	 */
	public static String plain(String raw) {
		if (raw == null || raw.isEmpty()) {
			return "";
		}
		String text = unescape(raw);
		text = text.replaceAll(ENVELOPE, " ");
		text = text.replaceAll("(?m)^\\s*(```|~~~).*$", " ");
		text = text.replaceAll("!\\[([^\\]]*)\\]\\([^)]*\\)", "$1");
		text = text.replaceAll("\\[([^\\]]*)\\]\\([^)]*\\)", "$1");
		text = text.replaceAll("(?m)^\\s{0,3}#{1,6}\\s*", " ");
		text = text.replaceAll("(?m)^\\s{0,3}>\\s?", " ");
		text = text.replaceAll("(?m)^\\s{0,3}([-*+]|\\d{1,3}[.)])\\s+", " ");
		text = text.replaceAll("\\*\\*([^*]+)\\*\\*", "$1");
		text = text.replaceAll("__([^_\\n]+)__", "$1");
		text = text.replaceAll("~~([^~]+)~~", "$1");
		text = text.replaceAll("\\*([^*\\s][^*]*?)\\*", "$1");
		text = text.replace("`", "");
		text = text.replaceAll("\\s+", " ").trim();
		// A window can cut through a Markdown link, and the replacement above could then not undo
		// it. Drop what is left of the opening bracket.
		int dangling = text.lastIndexOf('[');
		if (dangling >= 0 && text.indexOf(')', dangling) < 0) {
			text = text.substring(0, dangling).trim();
		}
		return text;
	}

	/**
	 * The cited object's fields, one {@code label: value} line each.
	 *
	 * <p>
	 * This is what a source without prose has to say. Nested objects are left out: they are the
	 * tool's structure, not the reader's answer.
	 * </p>
	 */
	private static String fields(Map<String, Object> object) {
		StringBuilder out = new StringBuilder();
		int lines = 0;
		for (Map.Entry<String, Object> field : object.entrySet()) {
			if (lines >= MAX_FIELDS) {
				break;
			}
			String value = scalar(field.getValue());
			if (value.isEmpty()) {
				continue;
			}
			if (out.length() > 0) {
				out.append('\n');
			}
			out.append(field.getKey()).append(": ").append(value);
			lines++;
		}
		return out.toString();
	}

	/** The object's longest string field when it is long enough to be the source's prose. */
	private static String prose(Map<String, Object> object) {
		String longest = "";
		for (Object value : object.values()) {
			if (value instanceof String text) {
				String rendered = plain(text);
				if (rendered.length() > longest.length()) {
					longest = rendered;
				}
			}
		}
		return longest.length() >= PROSE_MIN ? cut(longest, 0, Math.min(longest.length(), MAX)) : "";
	}

	/** One field's value as a line of text, or empty when it has none to show. */
	private static String scalar(Object value) {
		if (value == null) {
			return "";
		}
		if (value instanceof String text) {
			return trim(plain(text), FIELD_MAX);
		}
		if (value instanceof Number || value instanceof Boolean) {
			return String.valueOf(value);
		}
		if (value instanceof List<?> items) {
			StringBuilder joined = new StringBuilder();
			for (Object item : items) {
				if (item instanceof Map || item instanceof List) {
					continue;
				}
				String rendered = scalar(item);
				if (rendered.isEmpty()) {
					continue;
				}
				if (joined.length() > 0) {
					joined.append(", ");
				}
				joined.append(rendered);
			}
			return trim(joined.toString(), FIELD_MAX);
		}
		return "";
	}

	/**
	 * The innermost object of {@code tree} that carries {@code id} as one of its own values.
	 *
	 * <p>
	 * Children are searched first, so the cited record wins over the envelope that holds it.
	 * </p>
	 *
	 * @param exact
	 *        whether a value has to be the id, rather than merely contain it
	 */
	private static Map<String, Object> locate(Object node, String id, boolean exact) {
		if (node instanceof Map<?, ?> map) {
			for (Object value : map.values()) {
				Map<String, Object> hit = locate(value, id, exact);
				if (hit != null) {
					return hit;
				}
			}
			for (Object value : map.values()) {
				if (matches(value, id, exact)) {
					@SuppressWarnings("unchecked")
					Map<String, Object> self = (Map<String, Object>) map;
					return self;
				}
			}
			return null;
		}
		if (node instanceof List<?> items) {
			for (Object item : items) {
				Map<String, Object> hit = locate(item, id, exact);
				if (hit != null) {
					return hit;
				}
			}
		}
		return null;
	}

	private static boolean matches(Object value, String id, boolean exact) {
		if (value == null || value instanceof Map || value instanceof List) {
			return false;
		}
		String text = String.valueOf(value);
		return exact ? text.equals(id) : text.contains(id);
	}

	/**
	 * The same tree with every string value that is itself JSON parsed in place.
	 *
	 * <p>
	 * A tool that wraps its answer - {@code {"success":true,"result":"{\"id\":…}"}} is what the
	 * TL-Script tool returns - carries the source one escaping deeper than the envelope. Unwrapping
	 * it here means the rest of this class sees the record either way, instead of showing a reader
	 * the braces of a payload that happened to be quoted.
	 * </p>
	 */
	private static Object unwrap(Object node, int depth) {
		if (depth > MAX_NESTING) {
			return node;
		}
		if (node instanceof String text) {
			Object nested = parse(text);
			return nested == null ? node : unwrap(nested, depth + 1);
		}
		if (node instanceof Map<?, ?> map) {
			Map<String, Object> out = new LinkedHashMap<>();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				out.put(String.valueOf(entry.getKey()), unwrap(entry.getValue(), depth));
			}
			return out;
		}
		if (node instanceof List<?> items) {
			List<Object> out = new ArrayList<>(items.size());
			for (Object item : items) {
				out.add(unwrap(item, depth));
			}
			return out;
		}
		return node;
	}

	/** {@code blob} parsed as JSON, or <code>null</code> when it is not JSON. */
	private static Object parse(String blob) {
		String trimmed = blob.replaceAll(ENVELOPE, " ").trim();
		if (trimmed.isEmpty() || (trimmed.charAt(0) != '{' && trimmed.charAt(0) != '[')) {
			return null;
		}
		try {
			return JsonTree.parseValue(trimmed);
		} catch (IOException | RuntimeException ex) {
			return null;
		}
	}

	/**
	 * The JSON string value of {@code blob} that contains {@code passage}, or {@code blob} itself
	 * when it is not JSON or the passage spans more than one value.
	 */
	private static String stringHolding(String blob, String passage) {
		Object tree = parse(blob);
		String found = tree == null ? null : stringHolding(tree, passage);
		return found == null ? blob : found;
	}

	private static String stringHolding(Object node, String passage) {
		if (node instanceof String text) {
			return text.contains(passage) ? text : null;
		}
		if (node instanceof Map<?, ?> map) {
			for (Object value : map.values()) {
				String hit = stringHolding(value, passage);
				if (hit != null) {
					return hit;
				}
			}
		} else if (node instanceof List<?> items) {
			for (Object item : items) {
				String hit = stringHolding(item, passage);
				if (hit != null) {
					return hit;
				}
			}
		}
		return null;
	}

	/**
	 * A readable window of {@code text} around the range {@code [from, to)}, cut on sentence
	 * boundaries and marked where it was cut.
	 */
	private static String window(String text, int from, int to) {
		int start = sentenceStart(text, Math.max(0, from - MARGIN), from);
		int end = sentenceEnd(text, Math.min(text.length(), to + MARGIN), to);
		if (end - start > MAX) {
			// Keep what was cited; a passage longer than the budget keeps its own beginning.
			end = sentenceEnd(text, Math.min(text.length(), start + MAX), Math.min(to, start + MAX));
		}
		return cut(text, start, end);
	}

	/** {@code text} between {@code start} and {@code end}, with an ellipsis where it was cut. */
	private static String cut(String text, int start, int end) {
		String body = text.substring(start, end).trim();
		if (body.isEmpty()) {
			return "";
		}
		return (start > 0 ? ELLIPSIS + " " : "") + body + (end < text.length() ? " " + ELLIPSIS : "");
	}

	/**
	 * Where to begin a window that opens no later than {@code limit}: after the last sentence that
	 * ended before it, or at the next word boundary.
	 */
	private static int sentenceStart(String text, int at, int limit) {
		if (at <= 0) {
			return 0;
		}
		for (int i = at; i < limit; i++) {
			if (endsSentence(text, i)) {
				return skipSpace(text, i + 1);
			}
		}
		int space = text.indexOf(' ', at);
		return space >= 0 && space < limit ? skipSpace(text, space) : at;
	}

	/**
	 * Where to end a window that must reach at least {@code limit}: at the end of the sentence
	 * {@code at} falls into, or at the last word boundary before it.
	 */
	private static int sentenceEnd(String text, int at, int limit) {
		if (at >= text.length()) {
			return text.length();
		}
		for (int i = at - 1; i >= limit; i--) {
			if (endsSentence(text, i)) {
				return i + 1;
			}
		}
		int space = text.lastIndexOf(' ', at);
		return space > limit ? space : at;
	}

	private static boolean endsSentence(String text, int at) {
		if (at < 0 || at >= text.length()) {
			return false;
		}
		char c = text.charAt(at);
		if (c != '.' && c != '!' && c != '?' && c != '…') {
			return false;
		}
		return at + 1 >= text.length() || text.charAt(at + 1) == ' ';
	}

	private static int skipSpace(String text, int at) {
		int i = at;
		while (i < text.length() && text.charAt(i) == ' ') {
			i++;
		}
		return i;
	}

	/** {@code text} shortened to {@code max} characters on a word boundary. */
	private static String trim(String text, int max) {
		if (text.length() <= max) {
			return text;
		}
		int space = text.lastIndexOf(' ', max);
		int end = space > max / 2 ? space : max;
		return text.substring(0, end).trim() + " " + ELLIPSIS;
	}

	/**
	 * JSON string escapes resolved, so that a result that was never parsed still reads as text.
	 *
	 * <p>
	 * A tool result that is JSON is parsed and needs none of this. One that only looks like JSON -
	 * a log line, a fragment - is shown as text, and a literal {@code \n} in a popover is exactly
	 * what #418 is about.
	 * </p>
	 */
	private static String unescape(String raw) {
		if (raw.indexOf('\\') < 0) {
			return raw;
		}
		StringBuilder out = new StringBuilder(raw.length());
		for (int i = 0; i < raw.length(); i++) {
			char c = raw.charAt(i);
			if (c != '\\' || i + 1 >= raw.length()) {
				out.append(c);
				continue;
			}
			char next = raw.charAt(++i);
			switch (next) {
				case 'n', 'r' -> out.append('\n');
				case 't' -> out.append(' ');
				case 'b', 'f' -> out.append(' ');
				case '"' -> out.append('"');
				case '/' -> out.append('/');
				case '\\' -> out.append('\\');
				case 'u' -> {
					if (i + 4 < raw.length()) {
						try {
							out.append((char) Integer.parseInt(raw.substring(i + 1, i + 5), 16));
							i += 4;
						} catch (NumberFormatException ex) {
							out.append('\\').append(next);
						}
					} else {
						out.append('\\').append(next);
					}
				}
				default -> out.append('\\').append(next);
			}
		}
		return out.toString();
	}
}
