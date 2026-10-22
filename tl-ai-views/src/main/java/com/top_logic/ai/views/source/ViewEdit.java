/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views.source;

import com.top_logic.ai.views.source.ViewSource.Attribute;
import com.top_logic.ai.views.source.ViewSource.Element;

/**
 * One change of one element of a view file, made in the text.
 *
 * <p>
 * Every character outside the changed element stays as it was: the comments, the indentation and
 * the attribute order of a hand-written view survive an edit. What the edit inserts is indented
 * like its new neighbours.
 * </p>
 */
public sealed interface ViewEdit {

	/**
	 * Applies the edit.
	 *
	 * @return The changed text, well-formed.
	 * @throws IllegalArgumentException
	 *         When the address names nothing, or the result is not well-formed.
	 */
	String apply(String text);

	/**
	 * Sets an attribute; a <code>null</code> value removes it.
	 *
	 * @param at
	 *        The element's address.
	 * @param name
	 *        The attribute's name.
	 * @param value
	 *        Its new value, or <code>null</code>.
	 */
	record Set(String at, String name, String value) implements ViewEdit {
		@Override
		public String apply(String text) {
			Element element = element(text, at);
			Attribute existing = element.attributes().get(name);
			String written = value == null ? null : name + "=\"" + ViewSource.escapeAttribute(value) + "\"";
			if (existing != null) {
				int from = existing.start();
				if (written == null) {
					while (from > 0 && Character.isWhitespace(text.charAt(from - 1))) {
						from--;
					}
					return checked(text.substring(0, from) + text.substring(existing.end()));
				}
				return checked(text.substring(0, from) + written + text.substring(existing.end()));
			}
			if (written == null) {
				return text;
			}
			int insert = element.attributes().isEmpty() ? element.start() + 1 + element.tag().length()
				: last(element).end();
			return checked(text.substring(0, insert) + " " + written + text.substring(insert));
		}

		private static Attribute last(Element element) {
			Attribute last = null;
			for (Attribute attribute : element.attributes().values()) {
				last = attribute;
			}
			return last;
		}
	}

	/**
	 * Replaces an element, with everything inside it, by other XML.
	 *
	 * @param at
	 *        The element's address.
	 * @param xml
	 *        The XML that takes its place.
	 */
	record Replace(String at, String xml) implements ViewEdit {
		@Override
		public String apply(String text) {
			Element element = element(text, at);
			String indent = indentAt(text, element.start());
			return checked(text.substring(0, element.start()) + indented(xml, indent) + text.substring(element.end()));
		}
	}

	/**
	 * Removes an element with everything inside it, and its line when it stood alone on it.
	 *
	 * @param at
	 *        The element's address.
	 */
	record Remove(String at) implements ViewEdit {
		@Override
		public String apply(String text) {
			Element element = element(text, at);
			if (element.parent() == null) {
				throw new IllegalArgumentException("The document element cannot be removed; delete the view instead.");
			}
			int from = element.start();
			int to = element.end();
			int lineStart = lineStart(text, from);
			int lineEnd = lineEndAfter(text, to);
			if (text.substring(lineStart, from).isBlank() && lineEnd > to) {
				from = lineStart;
				to = lineEnd;
			}
			return checked(text.substring(0, from) + text.substring(to));
		}
	}

	/**
	 * Inserts XML as the last child of an element, or before or after it.
	 *
	 * @param at
	 *        The element's address.
	 * @param xml
	 *        The XML to insert.
	 * @param position
	 *        Where to insert it, relative to the element.
	 */
	record Insert(String at, String xml, Position position) implements ViewEdit {
		@Override
		public String apply(String text) {
			Element element = element(text, at);
			String indent = indentAt(text, element.start());
			switch (position) {
				case BEFORE:
					return checked(text.substring(0, element.start()) + indented(xml, indent) + "\n" + indent
						+ text.substring(element.start()));
				case AFTER:
					return checked(text.substring(0, element.end()) + "\n" + indent + indented(xml, indent)
						+ text.substring(element.end()));
				default:
					String inner = indent + unit(text);
					if (element.isEmptyTag()) {
						int close = element.end() - 2;
						while (Character.isWhitespace(text.charAt(close - 1))) {
							close--;
						}
						return checked(text.substring(0, close) + ">\n" + inner + indented(xml, inner) + "\n" + indent
							+ "</" + element.tag() + ">" + text.substring(element.end()));
					}
					int endTag = element.endTagStart();
					int lineStart = lineStart(text, endTag);
					if (text.substring(lineStart, endTag).isBlank()) {
						return checked(text.substring(0, lineStart) + inner + indented(xml, inner) + "\n"
							+ text.substring(lineStart));
					}
					return checked(text.substring(0, endTag) + "\n" + inner + indented(xml, inner) + "\n" + indent
						+ text.substring(endTag));
			}
		}
	}

	/**
	 * Where {@link Insert} puts its XML.
	 */
	enum Position {
		/** As the last child. */
		INTO,
		/** As the previous sibling. */
		BEFORE,
		/** As the next sibling. */
		AFTER;
	}

	private static Element element(String text, String at) {
		return ViewSource.parse(text).find(at);
	}

	private static String checked(String text) {
		ViewSource.parse(text);
		return text;
	}

	/** The blanks in front of an offset on its line, "" when other text stands there. */
	private static String indentAt(String text, int offset) {
		int start = lineStart(text, offset);
		String before = text.substring(start, offset);
		return before.isBlank() ? before : "";
	}

	private static int lineStart(String text, int offset) {
		int start = offset;
		while (start > 0 && text.charAt(start - 1) != '\n') {
			start--;
		}
		return start;
	}

	/** Offset after the line break ending the line of an offset when only blanks follow; else the offset. */
	private static int lineEndAfter(String text, int offset) {
		int end = offset;
		while (end < text.length() && (text.charAt(end) == ' ' || text.charAt(end) == '\t' || text.charAt(end) == '\r')) {
			end++;
		}
		return end < text.length() && text.charAt(end) == '\n' ? end + 1 : offset;
	}

	/** One level of indentation as the file writes it. */
	private static String unit(String text) {
		return text.contains("\n\t") ? "\t" : "  ";
	}

	/**
	 * The XML with its own common indentation replaced by {@code indent} on every line but the
	 * first, which goes where the caller puts it.
	 */
	private static String indented(String xml, String indent) {
		String[] lines = xml.strip().split("\r?\n", -1);
		int common = Integer.MAX_VALUE;
		for (int n = 1; n < lines.length; n++) {
			if (!lines[n].isBlank()) {
				common = Math.min(common, lines[n].length() - lines[n].stripLeading().length());
			}
		}
		StringBuilder out = new StringBuilder(lines[0]);
		for (int n = 1; n < lines.length; n++) {
			out.append('\n');
			if (!lines[n].isBlank()) {
				// The lines keep their indentation relative to the least indented of them.
				out.append(indent).append(lines[n].substring(common));
			}
		}
		return out.toString();
	}
}
