/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views.source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.xml.TagUtil;

/**
 * The elements of a view file with the character range each one covers.
 *
 * <p>
 * A DOM forgets how a file was written: its comments, its quotes, the line breaks inside a start
 * tag. An edit of a hand-written view must change one element and copy every other character, so
 * this reads the elements, their attributes and the text of leaf elements together with their
 * positions, and nothing else.
 * </p>
 *
 * <p>
 * An element is addressed by the path of tag names from the document element, e.g.
 * {@code /view/app-shell/content/sidebar/nav-item[2]}: a position (from 1) where siblings share the
 * tag. {@link #find(String)} also takes an attribute instead of the position,
 * {@code nav-item[id=events]}.
 * </p>
 */
public final class ViewSource {

	private static final Pattern STEP = Pattern.compile("([^\\[\\]/]+)(?:\\[(?:(\\d+)|([^=\\]]+)=([^\\]]*))\\])?");

	/** Attributes the outline shows: those that say what an element is or where it reads from. */
	private static final List<String> NAMING = List.of("id", "name", "attribute", "view", "channel", "input",
		"selection", "types", "type", "to", "label");

	private final String _text;

	private int _pos;

	private Element _root;

	private int[] _lineStarts;

	private ViewSource(String text) {
		_text = text;
	}

	/**
	 * Reads the elements of a text.
	 *
	 * @throws IllegalArgumentException
	 *         When the text is not well-formed XML; the message names the line.
	 */
	public static ViewSource parse(String text) {
		ViewSource source = new ViewSource(text);
		source.document();
		return source;
	}

	/** The document element. */
	public Element root() {
		return _root;
	}

	/**
	 * The element at an address.
	 *
	 * @throws IllegalArgumentException
	 *         When nothing is there; the message names what is.
	 */
	public Element find(String address) {
		String path = address == null ? "" : address.strip();
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		String[] steps = path.split("/");
		Element current = null;
		for (String step : steps) {
			Matcher match = STEP.matcher(step.strip());
			if (!match.matches()) {
				throw new IllegalArgumentException("'" + step + "' is not a step of an address, write tag, tag[2] or tag[id=x].");
			}
			List<Element> candidates = current == null ? List.of(_root) : current.children();
			Element found = select(candidates, match.group(1), match.group(2), match.group(3), match.group(4));
			if (found == null) {
				List<String> offered = new ArrayList<>();
				candidates.forEach(c -> offered.add(c.address()));
				throw new IllegalArgumentException("Nothing at " + address + ": '" + step + "' is not "
					+ (current == null ? "the document element" : "a child of " + current.address())
					+ ". There: " + offered);
			}
			current = found;
		}
		return current;
	}

	private static Element select(List<Element> candidates, String tag, String position, String key, String value) {
		int count = 0;
		for (Element candidate : candidates) {
			if (!candidate.tag().equals(tag)) {
				continue;
			}
			count++;
			if (key != null) {
				if (value.strip().equals(candidate.attribute(key.strip()))) {
					return candidate;
				}
			} else if (position == null || Integer.parseInt(position) == count) {
				return candidate;
			}
		}
		return null;
	}

	/** The line (from 1) of a character offset. */
	public int lineOf(int offset) {
		if (_lineStarts == null) {
			List<Integer> starts = new ArrayList<>();
			starts.add(0);
			for (int n = 0; n < _text.length(); n++) {
				if (_text.charAt(n) == '\n') {
					starts.add(n + 1);
				}
			}
			_lineStarts = starts.stream().mapToInt(Integer::intValue).toArray();
		}
		int found = Arrays.binarySearch(_lineStarts, offset);
		return found >= 0 ? found + 1 : -found - 1;
	}

	/**
	 * One line per element: its line, its address, the attributes that name it, and the text of a
	 * label-like child ({@code <label><en>..</en><de>..</de></label>}) folded into its parent's line.
	 */
	public String outline() {
		StringBuilder out = new StringBuilder();
		outline(_root, out);
		return out.toString();
	}

	private void outline(Element element, StringBuilder out) {
		out.append(lineOf(element.start())).append(": ").append(element.address());
		for (String name : NAMING) {
			String value = element.attribute(name);
			if (value != null) {
				out.append(' ').append(name).append("=\"").append(clip(value)).append('"');
			}
		}
		if (element.children().isEmpty() && !element.content().isBlank()) {
			out.append(" = ").append(clip(element.content().strip()));
		}
		List<Element> nested = new ArrayList<>();
		for (Element child : element.children()) {
			if (isTextGroup(child)) {
				out.append(' ').append(child.tag()).append('(');
				for (Element language : child.children()) {
					out.append(language.tag()).append(": ").append(clip(language.content().strip())).append("; ");
				}
				out.setLength(out.length() - 2);
				out.append(')');
			} else {
				nested.add(child);
			}
		}
		out.append('\n');
		for (Element child : nested) {
			outline(child, out);
		}
	}

	/** Whether an element only holds short texts per language, like a label. */
	private static boolean isTextGroup(Element element) {
		if (element.children().isEmpty() || !element.attributes().isEmpty()) {
			return false;
		}
		for (Element child : element.children()) {
			if (!child.children().isEmpty() || child.tag().length() > 5) {
				return false;
			}
		}
		return true;
	}

	private static String clip(String text) {
		String line = text.replaceAll("\\s+", " ");
		return line.length() > 80 ? line.substring(0, 77) + "..." : line;
	}

	private void document() {
		while (true) {
			skipMisc();
			if (_pos >= _text.length()) {
				break;
			}
			if (_root != null) {
				throw error("Only one document element is allowed");
			}
			_root = element(null);
		}
		if (_root == null) {
			throw error("There is no document element");
		}
	}

	/** Skips blanks, comments, processing instructions and a doctype. */
	private void skipMisc() {
		while (_pos < _text.length()) {
			if (Character.isWhitespace(_text.charAt(_pos))) {
				_pos++;
			} else if (_text.startsWith("<!--", _pos)) {
				skipPast("-->");
			} else if (_text.startsWith("<?", _pos)) {
				skipPast("?>");
			} else if (_text.startsWith("<!DOCTYPE", _pos)) {
				skipPast(">");
			} else if (_text.charAt(_pos) != '<') {
				throw error("Text outside of the document element");
			} else {
				return;
			}
		}
	}

	private Element element(Element parent) {
		int start = _pos;
		_pos++;
		String tag = name();
		Map<String, Attribute> attributes = new LinkedHashMap<>();
		while (true) {
			skipBlanks();
			if (_pos >= _text.length()) {
				throw error("<" + tag + "> is not closed");
			}
			char c = _text.charAt(_pos);
			if (c == '/' || c == '>') {
				break;
			}
			int attributeStart = _pos;
			String name = name();
			skipBlanks();
			expect('=');
			skipBlanks();
			char quote = _pos < _text.length() ? _text.charAt(_pos) : 0;
			if (quote != '"' && quote != '\'') {
				throw error("The value of " + name + " is not quoted");
			}
			int close = _text.indexOf(quote, _pos + 1);
			if (close < 0) {
				throw error("The value of " + name + " is not closed");
			}
			String value = unescape(_text.substring(_pos + 1, close));
			_pos = close + 1;
			if (attributes.put(name, new Attribute(name, value, attributeStart, _pos)) != null) {
				throw error("Attribute " + name + " is given twice");
			}
		}
		Element element = new Element(parent, tag, attributes, start);
		if (_text.charAt(_pos) == '/') {
			_pos++;
			expect('>');
			element._end = _pos;
			return element;
		}
		_pos++;
		StringBuilder content = new StringBuilder();
		while (true) {
			if (_pos >= _text.length()) {
				throw error("<" + tag + "> is not closed");
			}
			if (_text.startsWith("</", _pos)) {
				int endTag = _pos;
				_pos += 2;
				String closing = name();
				skipBlanks();
				expect('>');
				if (!closing.equals(tag)) {
					_pos = endTag;
					throw error("</" + closing + "> closes <" + tag + ">");
				}
				element._endTagStart = endTag;
				element._end = _pos;
				element._content = content.toString();
				return element;
			}
			if (_text.startsWith("<!--", _pos)) {
				skipPast("-->");
			} else if (_text.startsWith("<![CDATA[", _pos)) {
				int from = _pos + 9;
				skipPast("]]>");
				content.append(_text, from, _pos - 3);
			} else if (_text.startsWith("<?", _pos)) {
				skipPast("?>");
			} else if (_text.charAt(_pos) == '<') {
				element._children.add(element(element));
			} else {
				int next = _text.indexOf('<', _pos);
				int to = next < 0 ? _text.length() : next;
				content.append(unescape(_text.substring(_pos, to)));
				_pos = to;
			}
		}
	}

	private String name() {
		int from = _pos;
		while (_pos < _text.length()) {
			char c = _text.charAt(_pos);
			if (Character.isWhitespace(c) || c == '=' || c == '>' || c == '/' || c == '<') {
				break;
			}
			_pos++;
		}
		if (from == _pos) {
			throw error("A name is missing");
		}
		return _text.substring(from, _pos);
	}

	private void skipBlanks() {
		while (_pos < _text.length() && Character.isWhitespace(_text.charAt(_pos))) {
			_pos++;
		}
	}

	private void skipPast(String end) {
		int found = _text.indexOf(end, _pos);
		if (found < 0) {
			throw error("'" + end + "' is missing");
		}
		_pos = found + end.length();
	}

	private void expect(char c) {
		if (_pos >= _text.length() || _text.charAt(_pos) != c) {
			throw error("'" + c + "' expected");
		}
		_pos++;
	}

	private IllegalArgumentException error(String message) {
		return new IllegalArgumentException("Not well-formed XML, line " + lineOf(_pos) + ": " + message + ".");
	}

	/** The text of an attribute value or of character data. */
	public static String unescape(String text) {
		if (text.indexOf('&') < 0) {
			return text;
		}
		StringBuilder out = new StringBuilder();
		int n = 0;
		while (n < text.length()) {
			char c = text.charAt(n);
			int semicolon = c == '&' ? text.indexOf(';', n) : -1;
			if (semicolon < 0) {
				out.append(c);
				n++;
				continue;
			}
			String entity = text.substring(n + 1, semicolon);
			switch (entity) {
				case "lt" -> out.append('<');
				case "gt" -> out.append('>');
				case "amp" -> out.append('&');
				case "quot" -> out.append('"');
				case "apos" -> out.append('\'');
				default -> {
					if (entity.startsWith("#x")) {
						out.appendCodePoint(Integer.parseInt(entity.substring(2), 16));
					} else if (entity.startsWith("#")) {
						out.appendCodePoint(Integer.parseInt(entity.substring(1)));
					} else {
						out.append('&').append(entity).append(';');
					}
				}
			}
			n = semicolon + 1;
		}
		return out.toString();
	}

	/** An attribute value written into a double-quoted attribute. */
	public static String escapeAttribute(String value) {
		return TagUtil.encodeXMLAttribute(value);
	}

	/**
	 * An attribute of an element: its value and the range from its name to its closing quote.
	 *
	 * @param name
	 *        The attribute's name.
	 * @param value
	 *        The attribute's value, decoded.
	 * @param start
	 *        The offset of its name.
	 * @param end
	 *        The offset after its closing quote.
	 */
	public record Attribute(String name, String value, int start, int end) {
		// Read from the text.
	}

	/**
	 * An element of a {@link ViewSource}: from its {@code <} to the {@code >} of its end tag.
	 */
	public static final class Element {

		private final Element _parent;

		private final String _tag;

		private final Map<String, Attribute> _attributes;

		private final int _start;

		private final List<Element> _children = new ArrayList<>();

		private int _endTagStart = -1;

		private int _end;

		private String _content = "";

		Element(Element parent, String tag, Map<String, Attribute> attributes, int start) {
			_parent = parent;
			_tag = tag;
			_attributes = attributes;
			_start = start;
		}

		/** The tag name. */
		public String tag() {
			return _tag;
		}

		/** The enclosing element, <code>null</code> for the document element. */
		public Element parent() {
			return _parent;
		}

		/** The attributes in their written order. */
		public Map<String, Attribute> attributes() {
			return _attributes;
		}

		/** The value of an attribute, or <code>null</code>. */
		public String attribute(String name) {
			Attribute attribute = _attributes.get(name);
			return attribute == null ? null : attribute.value();
		}

		/** The child elements. */
		public List<Element> children() {
			return _children;
		}

		/** The character data directly inside, CDATA included; "" for an empty element. */
		public String content() {
			return _content;
		}

		/** Offset of the {@code <} of the start tag. */
		public int start() {
			return _start;
		}

		/** Offset of the {@code </} of the end tag, -1 for an empty-element tag. */
		public int endTagStart() {
			return _endTagStart;
		}

		/** Offset just after the element. */
		public int end() {
			return _end;
		}

		/** Whether it is written as {@code <tag .../>}. */
		public boolean isEmptyTag() {
			return _endTagStart < 0;
		}

		/** The path of this element, see {@link ViewSource}. */
		public String address() {
			if (_parent == null) {
				return "/" + _tag;
			}
			int same = 0;
			int position = 0;
			for (Element sibling : _parent._children) {
				if (sibling._tag.equals(_tag)) {
					same++;
					if (sibling == this) {
						position = same;
					}
				}
			}
			return _parent.address() + "/" + _tag + (same > 1 ? "[" + position + "]" : "");
		}
	}
}
