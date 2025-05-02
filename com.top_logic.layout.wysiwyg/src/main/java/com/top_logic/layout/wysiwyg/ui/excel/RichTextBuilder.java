/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.excel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/**
 * Converter of simple HTML (without CSS styles, tables, enumerations, etc.) to rich text.
 * 
 * @see RichTextString
 */
public class RichTextBuilder {

	/**
	 * The maximum width in characters of the produced output.
	 */
	private static final int MAX_WIDTH = 80;

	/**
	 * Buffer of plain text that is extracted from the HTMl.
	 */
	private StringBuilder _text = new StringBuilder();

	/**
	 * The context workbook.
	 */
	private final Workbook _workbook;

	/**
	 * The number of characters in the currently written line.
	 */
	private int _lineWidth;

	/**
	 * The total width of the generated text in characters.
	 */
	private int _width;

	/**
	 * The currently active font.
	 */
	private Font _font;

	/**
	 * Counter to assign priorities to text runs.
	 */
	int _prio = 0;

	private List<RichTextBuilder.Run> _runs = new ArrayList<>();

	/**
	 * Creates a {@link RichTextBuilder}.
	 */
	public RichTextBuilder(Workbook workbook) {
		_workbook = workbook;
		_font = workbook.createFont();
	}

	/**
	 * The width of the text in characters.
	 */
	public int getWidth() {
		return Math.max(_width, _lineWidth);
	}

	/**
	 * Appends the given HTML node to this builder.
	 */
	public RichTextBuilder append(Node node) {
		appendNode(node);
		trimTrailing();
		return this;
	}

	private void appendNode(Node node) {
		if (node instanceof TextNode text) {
			String content = text.text();
			appendText(content);
		} else if (node instanceof Element element) {
			Font font =
				switch (element.tagName()) {
					case "strong", "b" -> makeFont(true, _font.getColor(), _font.getFontHeightInPoints(),
						_font.getFontName(),
						_font.getItalic(), _font.getStrikeout(), _font.getTypeOffset(), _font.getUnderline());
					case "em", "i" -> makeFont(_font.getBold(), _font.getColor(), _font.getFontHeightInPoints(),
						_font.getFontName(), true, _font.getStrikeout(), _font.getTypeOffset(), _font.getUnderline());
					case "u" -> makeFont(_font.getBold(), _font.getColor(), _font.getFontHeightInPoints(),
						_font.getFontName(),
						_font.getItalic(), _font.getStrikeout(), _font.getTypeOffset(), Font.U_SINGLE);
					case "strike", "s", "del" -> makeFont(_font.getBold(), _font.getColor(),
						_font.getFontHeightInPoints(), _font.getFontName(), _font.getItalic(), true,
						_font.getTypeOffset(), _font.getUnderline());
					case "sub" -> makeFont(_font.getBold(), _font.getColor(), _font.getFontHeightInPoints(),
						_font.getFontName(), _font.getItalic(), _font.getStrikeout(), Font.SS_SUB,
						_font.getUnderline());
					case "sup" -> makeFont(_font.getBold(), _font.getColor(), _font.getFontHeightInPoints(),
						_font.getFontName(), _font.getItalic(), _font.getStrikeout(), Font.SS_SUPER,
						_font.getUnderline());
					case "h1" -> makeFont(true, _font.getColor(), (short) 22, _font.getFontName(), _font.getItalic(),
						_font.getStrikeout(), _font.getTypeOffset(), _font.getUnderline());
					case "h2" -> makeFont(true, _font.getColor(), (short) 20, _font.getFontName(), _font.getItalic(),
						_font.getStrikeout(), _font.getTypeOffset(), _font.getUnderline());
					case "h3" -> makeFont(true, _font.getColor(), (short) 18, _font.getFontName(), _font.getItalic(),
						_font.getStrikeout(), _font.getTypeOffset(), _font.getUnderline());
					case "h4" -> makeFont(true, _font.getColor(), (short) 16, _font.getFontName(), _font.getItalic(),
						_font.getStrikeout(), _font.getTypeOffset(), _font.getUnderline());
					case "h5" -> makeFont(true, _font.getColor(), (short) 14, _font.getFontName(), _font.getItalic(),
						_font.getStrikeout(), _font.getTypeOffset(), _font.getUnderline());
					case "h6" -> makeFont(true, _font.getColor(), (short) 12, _font.getFontName(), _font.getItalic(),
						_font.getStrikeout(), _font.getTypeOffset(), _font.getUnderline());
					default -> null;
				};

			switch (element.tagName()) {
				case "br": {
					nl();
					break;
				}
				case "h1":
				case "h2":
				case "h3":
				case "h4":
				case "h5":
				case "h6":
				case "p": {
					cr();
					if (_text.length() > 0) {
						nl();
					}
					break;
				}
				case "div":
				case "li": {
					if (_lineWidth > 0) {
						cr();
					}
					break;
				}
			}

			Font before = _font;
			if (font != null) {
				_font = font;
			}

			int start = _text.length();
			descend(node);
			int stop = _text.length();

			switch (element.tagName()) {
				case "h1":
				case "h2":
				case "h3":
				case "h4":
				case "h5":
				case "h6":
				case "div":
				case "li":
				case "p": {
					nl();
					break;
				}
			}
			
			_font = before;
			if (font != null) {
				_runs.add(new Run(start, stop, font, _prio++));
			}
		} else {
			descend(node);
		}
	}

	private void descend(Node node) {
		for (Node child : node.childNodes()) {
			appendNode(child);
		}
	}

	/**
	 * Look-up or create a font with the given properties.
	 */
	private Font makeFont(boolean bold, short color, short fontHeight, String fontName, boolean italic,
			boolean strikeout, short typeOffset, byte underline) {
		Font font =
			_workbook.findFont(bold, color, fontHeight, fontName, italic, strikeout, typeOffset, underline);
		if (font == null) {
			font = _workbook.createFont();
			font.setBold(bold);
			font.setColor(color);
			font.setFontHeightInPoints(fontHeight);
			font.setFontName(fontName);
			font.setItalic(italic);
			font.setTypeOffset(typeOffset);
			font.setUnderline(underline);
		}
		return font;
	}

	/**
	 * Appends the given text to the text buffer.
	 * 
	 * <p>
	 * breaks lines so that the maximum width does not exceed {@link #MAX_WIDTH} characters.
	 */
	private void appendText(String content) {
		content = content.replaceAll("\\s+", " ");

		int offset = 0;
		int length = content.length();
		while (offset < length) {
			int chunkLen = Math.min(MAX_WIDTH - _lineWidth, length - offset);

			boolean mustWrap = offset + chunkLen < length;
			if (mustWrap) {
				int wrap = chunkLen;
				while (wrap >= 0 && !Character.isWhitespace(content.charAt(offset + wrap))) {
					wrap--;
				}
				if (wrap > 0) {
					chunkLen = wrap;
				} else {
					mustWrap = false;
				}
			}

			_text.append(content.subSequence(offset, offset + chunkLen));
			_lineWidth += chunkLen;
			offset += chunkLen;

			if (mustWrap) {
				nl();
				
				// Skip space.
				offset++;
			}
		}
	}

	/**
	 * Resets the current line, if it only contains white space, or move to a new line.
	 */
	private void cr() {
		int lineStart = _text.length() - _lineWidth;
		if (_text.substring(lineStart).isBlank()) {
			_text.setLength(lineStart);
			_lineWidth = 0;
		} else {
			nl();
		}
	}

	/**
	 * Removes trailing white space and newline characters at the end.
	 */
	private void trimTrailing() {
		while (_text.length() > 0 && Character.isWhitespace(_text.charAt(_text.length() - 1))) {
			_text.setLength(_text.length() - 1);
		}
	}

	/**
	 * Moves to the next line.
	 */
	private void nl() {
		_text.append("\n");
		_width = Math.max(_width, _lineWidth);
		_lineWidth = 0;
	}

	/**
	 * The plain text content.
	 */
	public RichTextString getText() {
		RichTextString result = _workbook.getCreationHelper().createRichTextString(_text.toString());

		if (!_runs.isEmpty()) {
			applyFont(result);
		}

		return result;
	}

	private void applyFont(RichTextString result) {
		List<Run> normalized = new ArrayList<>();

		Collections.sort(_runs);

		PriorityQueue<Run> pending = new PriorityQueue<>();
		for (int n = 0, cnt = _runs.size(); n < cnt; n++) {
			Run next = _runs.get(n);

			while (true) {
				Run other = pending.poll();
				if (other == null) {
					break;
				}

				if (other.start >= next.stop) {
					// No more matches.
					pending.offer(other);
					break;
				}

				if (other.start < next.start) {
					Run run = other.stop <= next.start ? other : other.before(next.start);
					normalized.add(run);
				}

				if (other.stop > next.stop) {
					pending.offer(other.after(next.stop));
				}
			}

			pending.offer(next);
		}

		for (RichTextBuilder.Run run : pending) {
			normalized.add(run);
		}

		for (Run run : normalized) {
			result.applyFont(run.start, run.stop, run.font);
		}
	}

	/**
	 * A region of text that should be styled using a given font.
	 */
	private static final class Run implements Comparable<Run> {
		/**
		 * The first character.
		 */
		final int start;

		/**
		 * The position after the last character.
		 */
		final int stop;

		/**
		 * The font to assign.
		 */
		final Font font;

		/**
		 * A priority for deciding which run wins, if multiple runs cover the same character.
		 */
		final int prio;

		public Run(int start, int stop, Font font, int prio) {
			super();
			this.start = start;
			this.stop = stop;
			this.font = font;
			this.prio = prio;
		}

		@Override
		public int compareTo(Run other) {
			int startCompare = Integer.compare(start, other.start);
			if (startCompare != 0) {
				return startCompare;
			}

			return Integer.compare(prio, other.prio);
		}

		public Run before(int pos) {
			return new Run(start, pos, font, prio);
		}

		public Run after(int pos) {
			return new Run(pos, stop, font, prio);
		}
	}

}