/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.context;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The elements of an XML document together with where each one stands in the text.
 *
 * <p>
 * For changing one element of a configuration file a developer keeps in git and leaving every other
 * character as it was: comments, attribute layout, blank lines, the order of attributes. A DOM or a
 * configuration round trip would print the whole file anew. It understands what a configuration
 * file contains - elements, attributes, text, comments, CDATA sections, processing instructions and a
 * DOCTYPE without an internal subset - and refuses anything it does not understand rather than
 * guessing.
 * </p>
 */
final class XmlSpans {

	private XmlSpans() {
		// Only static operations.
	}

	/**
	 * An element and its position in the text it was read from.
	 */
	static final class Element {

		private final String _name;

		private final int _start;

		private final Map<String, String> _attributes = new LinkedHashMap<>();

		private final List<Element> _children = new ArrayList<>();

		private int _startTagEnd;

		private int _contentEnd;

		private int _end;

		private boolean _empty;

		Element(String name, int start) {
			_name = name;
			_start = start;
		}

		/** The tag name, with its prefix if it has one. */
		String name() {
			return _name;
		}

		/** Where the start tag begins: the index of its {@code <}. */
		int start() {
			return _start;
		}

		/** Where the content begins: just behind the start tag. */
		int startTagEnd() {
			return _startTagEnd;
		}

		/** Where the content ends: the index of the end tag's {@code <}; the end for an empty element. */
		int contentEnd() {
			return _contentEnd;
		}

		/** Just behind the end tag, or behind the start tag of an empty element. */
		int end() {
			return _end;
		}

		/** Whether it is written as one empty tag, {@code <name/>}. */
		boolean isEmpty() {
			return _empty;
		}

		/** The value of the given attribute, with its references resolved, or <code>null</code>. */
		String attribute(String name) {
			return _attributes.get(name);
		}

		/** The child elements, in document order. */
		List<Element> children() {
			return _children;
		}

		/** The child elements with the given name, in document order. */
		List<Element> children(String name) {
			return _children.stream().filter(child -> child._name.equals(name)).toList();
		}

		/** The first child element with the given name, or <code>null</code>. */
		Element child(String name) {
			for (Element child : _children) {
				if (child._name.equals(name)) {
					return child;
				}
			}
			return null;
		}

		/**
		 * The text this element contains, with CDATA sections taken as they are, references resolved
		 * and comments left out.
		 */
		String text(String xml) {
			StringBuilder result = new StringBuilder();
			int pos = _startTagEnd;
			while (pos < _contentEnd) {
				if (xml.startsWith("<![CDATA[", pos)) {
					int end = find(xml, "]]>", pos);
					result.append(xml, pos + 9, end);
					pos = end + 3;
				} else if (xml.startsWith("<!--", pos)) {
					pos = find(xml, "-->", pos) + 3;
				} else if (xml.startsWith("<?", pos)) {
					pos = find(xml, "?>", pos) + 2;
				} else if (xml.charAt(pos) == '<') {
					// A child element: neither it nor its content is part of this element's text.
					pos = childAt(pos)._end;
				} else {
					int next = xml.indexOf('<', pos);
					int end = next < 0 || next > _contentEnd ? _contentEnd : next;
					result.append(decode(xml.substring(pos, end)));
					pos = end;
				}
			}
			return result.toString();
		}

		private Element childAt(int start) {
			for (Element child : _children) {
				if (child._start == start) {
					return child;
				}
			}
			throw new IllegalStateException("No child element at character " + start + ".");
		}
	}

	/**
	 * Reads the elements of the given document.
	 *
	 * @return The document element.
	 * @throws IllegalArgumentException
	 *         When the text is not a well-formed document of the kind described above.
	 */
	static Element parse(String xml) {
		Deque<Element> open = new ArrayDeque<>();
		Element root = null;
		int pos = 0;
		int length = xml.length();
		while (true) {
			int lt = xml.indexOf('<', pos);
			if (lt < 0) {
				break;
			}
			if (xml.startsWith("<!--", lt)) {
				pos = find(xml, "-->", lt) + 3;
			} else if (xml.startsWith("<![CDATA[", lt)) {
				pos = find(xml, "]]>", lt) + 3;
			} else if (xml.startsWith("<?", lt)) {
				pos = find(xml, "?>", lt) + 2;
			} else if (xml.startsWith("<!", lt)) {
				int end = find(xml, ">", lt);
				if (xml.substring(lt, end).indexOf('[') >= 0) {
					throw new IllegalArgumentException("A DOCTYPE with an internal subset is not supported.");
				}
				pos = end + 1;
			} else if (xml.startsWith("</", lt)) {
				int end = find(xml, ">", lt);
				String name = xml.substring(lt + 2, end).strip();
				if (open.isEmpty() || !open.peek()._name.equals(name)) {
					throw new IllegalArgumentException("Unexpected end tag </" + name + "> at character " + lt + ".");
				}
				Element closed = open.pop();
				closed._contentEnd = lt;
				closed._end = end + 1;
				pos = end + 1;
			} else {
				int nameEnd = lt + 1;
				while (nameEnd < length && !isSpace(xml.charAt(nameEnd)) && xml.charAt(nameEnd) != '/'
					&& xml.charAt(nameEnd) != '>') {
					nameEnd++;
				}
				Element element = new Element(xml.substring(lt + 1, nameEnd), lt);
				if (element._name.isEmpty()) {
					throw new IllegalArgumentException("An element without a name at character " + lt + ".");
				}
				pos = readAttributes(xml, nameEnd, element);
				if (open.isEmpty()) {
					if (root != null) {
						throw new IllegalArgumentException("A second document element <" + element._name + ">.");
					}
					root = element;
				} else {
					open.peek()._children.add(element);
				}
				if (element._empty) {
					element._contentEnd = pos;
					element._end = pos;
				} else {
					open.push(element);
				}
			}
		}
		if (root == null) {
			throw new IllegalArgumentException("No document element.");
		}
		if (!open.isEmpty()) {
			throw new IllegalArgumentException("The element <" + open.peek()._name + "> is not closed.");
		}
		return root;
	}

	private static int readAttributes(String xml, int pos, Element element) {
		int length = xml.length();
		while (true) {
			while (pos < length && isSpace(xml.charAt(pos))) {
				pos++;
			}
			if (pos >= length) {
				throw new IllegalArgumentException("The start tag <" + element._name + "> is not closed.");
			}
			if (xml.charAt(pos) == '>') {
				element._startTagEnd = pos + 1;
				return pos + 1;
			}
			if (xml.startsWith("/>", pos)) {
				element._empty = true;
				element._startTagEnd = pos + 2;
				return pos + 2;
			}
			int nameStart = pos;
			while (pos < length && !isSpace(xml.charAt(pos)) && xml.charAt(pos) != '=') {
				pos++;
			}
			String name = xml.substring(nameStart, pos);
			while (pos < length && isSpace(xml.charAt(pos))) {
				pos++;
			}
			if (pos >= length || xml.charAt(pos) != '=') {
				throw new IllegalArgumentException("The attribute " + name + " of <" + element._name + "> has no value.");
			}
			pos++;
			while (pos < length && isSpace(xml.charAt(pos))) {
				pos++;
			}
			char quote = pos < length ? xml.charAt(pos) : 0;
			if (quote != '"' && quote != '\'') {
				throw new IllegalArgumentException("The value of " + name + " in <" + element._name + "> is not quoted.");
			}
			int valueEnd = xml.indexOf(quote, pos + 1);
			if (valueEnd < 0) {
				throw new IllegalArgumentException("The value of " + name + " in <" + element._name + "> is not closed.");
			}
			element._attributes.put(name, decode(xml.substring(pos + 1, valueEnd)));
			pos = valueEnd + 1;
		}
	}

	private static int find(String xml, String token, int from) {
		int result = xml.indexOf(token, from);
		if (result < 0) {
			throw new IllegalArgumentException("Missing '" + token + "' after character " + from + ".");
		}
		return result;
	}

	private static boolean isSpace(char ch) {
		return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
	}

	/**
	 * The text with its character and entity references resolved.
	 */
	static String decode(String text) {
		if (text.indexOf('&') < 0) {
			return text;
		}
		StringBuilder result = new StringBuilder(text.length());
		int pos = 0;
		while (pos < text.length()) {
			char ch = text.charAt(pos);
			int semicolon = ch == '&' ? text.indexOf(';', pos) : -1;
			if (semicolon < 0) {
				result.append(ch);
				pos++;
				continue;
			}
			String reference = text.substring(pos + 1, semicolon);
			switch (reference) {
				case "lt" -> result.append('<');
				case "gt" -> result.append('>');
				case "amp" -> result.append('&');
				case "quot" -> result.append('"');
				case "apos" -> result.append('\'');
				default -> {
					if (reference.startsWith("#x")) {
						result.appendCodePoint(Integer.parseInt(reference.substring(2), 16));
					} else if (reference.startsWith("#")) {
						result.appendCodePoint(Integer.parseInt(reference.substring(1)));
					} else {
						throw new IllegalArgumentException("Unknown entity &" + reference + ";.");
					}
				}
			}
			pos = semicolon + 1;
		}
		return result.toString();
	}

	/**
	 * The text as the value of an attribute in double quotes.
	 */
	static String attributeValue(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;");
	}

	/**
	 * The text as the content of an element, in a CDATA section so that it stays exactly as written.
	 */
	static String cdata(String text) {
		return "<![CDATA[" + text.replace("]]>", "]]]]><![CDATA[>") + "]]>";
	}

	/**
	 * The white space in front of the given position, when nothing else stands between it and the
	 * start of its line; <code>null</code> otherwise.
	 */
	static String indentBefore(String xml, int pos) {
		int lineStart = lineStart(xml, pos);
		String before = xml.substring(lineStart, pos);
		return before.isBlank() ? before : null;
	}

	/**
	 * The index of the first character of the line the given position is in.
	 */
	static int lineStart(String xml, int pos) {
		int newline = xml.lastIndexOf('\n', pos - 1);
		return newline + 1;
	}

}
