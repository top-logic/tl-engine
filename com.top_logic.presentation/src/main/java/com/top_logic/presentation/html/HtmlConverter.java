/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.html;

import static com.top_logic.presentation.data.PresentationData.*;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.sl.usermodel.AutoNumberingScheme;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.image.util.ImageUtils;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextUtil;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.presentation.data.PresentationData;

/**
 * Converts HTML into the description of slide content.
 *
 * <p>
 * The result is a column ({@link PresentationData#TYPE_COLUMN}) with {@link PresentationData#SPLIT},
 * whose children are the blocks of the HTML document in their order:
 * </p>
 *
 * <ul>
 * <li>Consecutive flowing content (paragraphs, headings, lists, quotations, preformatted text)
 * forms a text box ({@link PresentationData#TYPE_TEXT_BOX}) with {@link PresentationData#SPLIT}
 * and automatic height. Headings <code>h1</code> to <code>h6</code> are bold and larger than the
 * base size, <code>b</code>/<code>strong</code>, <code>i</code>/<code>em</code>, <code>u</code>
 * and <code>a</code> become formatted runs, <code>code</code> and <code>pre</code> use the
 * {@link #MONOSPACE_FONT}. A heading is kept with the content following it
 * ({@link PresentationData#KEEP_WITH_NEXT}). A <code>br</code> breaks the line within its paragraph. List items get
 * the indentation level of their list and a bullet, items of an ordered list a numbered bullet
 * ({@link PresentationData#AUTO_NUMBER}) with their number. A quotation is indented by one
 * level.</li>
 * <li>A <code>table</code> becomes a table ({@link PresentationData#TYPE_TABLE}). Its leading rows
 * in <code>thead</code> or consisting of <code>th</code> cells are header rows. A cell spanning
 * several columns or rows is followed by empty cells in the columns and rows it spans.</li>
 * <li>An <code>img</code> becomes an image ({@link PresentationData#TYPE_IMAGE}) of automatic
 * height, as wide as its <code>width</code> attribute, or its own width (a pixel taken as 3/4
 * point). Its <code>src</code> names an image of the given image map, either directly or as image
 * reference of a stored structured text (see {@link I18NStructuredTextUtil#getImageRefID(String)}),
 * or is a <code>data:</code> URI. An image that cannot be resolved is left out with a logged
 * warning.</li>
 * </ul>
 *
 * <p>
 * Text of unknown elements flows on as text. White space is collapsed as in a browser, except
 * within <code>pre</code>.
 * </p>
 */
public final class HtmlConverter {

	/**
	 * The font of <code>code</code> and <code>pre</code> elements.
	 */
	public static final String MONOSPACE_FONT = "Courier New";

	/**
	 * The base font size in points, if none is given.
	 */
	public static final double DEFAULT_SIZE = 16;

	/**
	 * The size of a pixel of an HTML image in points.
	 */
	private static final double POINTS_PER_PIXEL = 0.75;

	/**
	 * The font sizes of the headings <code>h1</code> to <code>h6</code> relative to the base size.
	 */
	private static final double[] HEADING_SCALE = { 1.6, 1.4, 1.25, 1.1, 1.0, 0.9 };

	/**
	 * The space after a paragraph relative to the base size.
	 */
	private static final double PARAGRAPH_SPACE = 0.5;

	/**
	 * The space before a heading relative to its size.
	 */
	private static final double HEADING_SPACE = 0.5;

	/**
	 * The bullet characters of unordered lists by their nesting depth (repeating).
	 */
	private static final String[] BULLETS = { "•", "–" };

	/**
	 * The smallest and largest weight of a table column, its number of characters.
	 */
	private static final int MIN_COLUMN_WEIGHT = 4;

	private static final int MAX_COLUMN_WEIGHT = 40;

	private static final Pattern WHITE_SPACE = Pattern.compile("[ \\t\\n\\r\\f]+");

	private static final Pattern LENGTH = Pattern.compile("\\s*(\\d+(?:\\.\\d+)?)\\s*(px)?\\s*");

	/**
	 * The pattern of a length in pixels in a <code>style</code> attribute, the property name
	 * inserted with {@link String#format(String, Object...)}.
	 */
	private static final String STYLE_LENGTH = "(?:^|;)\\s*%s\\s*:\\s*(\\d+(?:\\.\\d+)?)\\s*px";

	private static final Pattern DATA_URI = Pattern.compile("data:([^;,]*)(;base64)?,(.*)", Pattern.DOTALL);

	private static final String IMAGE_NAME = "image";

	private final Map<String, BinaryData> _images;

	private final double _baseSize;

	private final List<Object> _blocks = new ArrayList<>();

	private List<Object> _paragraphs = new ArrayList<>();

	private List<Map<String, Object>> _runs = new ArrayList<>();

	/**
	 * The paragraph properties of the paragraph being collected.
	 */
	private Map<String, Object> _paragraph = map();

	/**
	 * Whether the paragraph being collected ends with a space, or has no text yet.
	 */
	private boolean _afterSpace = true;

	private Inline _inline = Inline.PLAIN;

	private int _quoteDepth;

	private final List<ListState> _lists = new ArrayList<>();

	private boolean _pre;

	/**
	 * Whether the content of a table cell is converted, which takes no images and tables.
	 */
	private boolean _inCell;

	/**
	 * Character formatting of the HTML elements enclosing a text.
	 *
	 * @param bold
	 *        Whether the text is bold.
	 * @param italic
	 *        Whether the text is italic.
	 * @param underline
	 *        Whether the text is underlined.
	 * @param mono
	 *        Whether the text uses the {@link HtmlConverter#MONOSPACE_FONT}.
	 * @param hyperlink
	 *        The link target, <code>null</code> for none.
	 */
	private record Inline(boolean bold, boolean italic, boolean underline, boolean mono, String hyperlink) {

		static final Inline PLAIN = new Inline(false, false, false, false, null);

		Map<String, Object> toRun(String text) {
			Map<String, Object> result = map();
			result.put(TEXT, text);
			if (bold) {
				result.put(BOLD, Boolean.TRUE);
			}
			if (italic) {
				result.put(ITALIC, Boolean.TRUE);
			}
			if (underline) {
				result.put(UNDERLINE, Boolean.TRUE);
			}
			if (mono) {
				result.put(FONT, MONOSPACE_FONT);
			}
			PresentationData.put(result, HYPERLINK, hyperlink);
			return result;
		}

	}

	/**
	 * An open list.
	 *
	 * @param scheme
	 *        The numbering scheme of an ordered list, <code>null</code> for an unordered list.
	 * @param next
	 *        The number of the next item.
	 */
	private record ListState(AutoNumberingScheme scheme, int next) {
		// Pure data.
	}

	private HtmlConverter(Map<String, BinaryData> images, double baseSize) {
		_images = images;
		_baseSize = baseSize;
	}

	/**
	 * Converts the given HTML into a description of slide content.
	 *
	 * @param html
	 *        The HTML source, <code>null</code> for none.
	 * @param images
	 *        The images the <code>src</code> attributes of the image elements refer to by name.
	 * @param textStyle
	 *        The character formatting of the text boxes and tables, see
	 *        {@link com.top_logic.presentation.model.DescriptionReader#readRunStyle}.
	 * @param baseSize
	 *        The font size of normal text, <code>null</code> for {@link #DEFAULT_SIZE}.
	 * @param gap
	 *        The distance between the blocks, <code>null</code> for the gap of the template
	 *        profile.
	 * @return The description of a column with split.
	 */
	public static Map<String, Object> convert(String html, Map<String, BinaryData> images,
			Map<String, Object> textStyle, Double baseSize, Double gap) {
		double size = baseSize == null ? DEFAULT_SIZE : baseSize.doubleValue();
		List<Object> blocks;
		if (html == null || html.isBlank()) {
			blocks = new ArrayList<>();
		} else {
			HtmlConverter converter = new HtmlConverter(images, size);
			Document document = Jsoup.parseBodyFragment(html);
			converter.convertChildren(document.body());
			converter.flushText();
			blocks = converter._blocks;
		}
		Map<String, Object> style = map();
		style.putAll(textStyle);
		style.put(SIZE, Double.valueOf(size));
		for (Object block : blocks) {
			@SuppressWarnings("unchecked")
			Map<String, Object> element = (Map<String, Object>) block;
			Object type = element.get(TYPE);
			if (TYPE_TEXT_BOX.equals(type)) {
				element.putAll(style);
			} else if (TYPE_TABLE.equals(type)) {
				for (Object row : (List<?>) element.get(ROWS)) {
					@SuppressWarnings("unchecked")
					List<Object> cells = (List<Object>) row;
					for (int n = 0; n < cells.size(); n++) {
						Map<String, Object> cell = map();
						cell.put(TEXT, cells.get(n));
						cell.putAll(style);
						cells.set(n, cell);
					}
				}
			}
		}
		Map<String, Object> result = map();
		result.put(TYPE, TYPE_COLUMN);
		result.put(CHILDREN, blocks);
		PresentationData.put(result, GAP, gap);
		result.put(SPLIT, Boolean.TRUE);
		return result;
	}

	private void convertChildren(Element element) {
		for (Node child : element.childNodes()) {
			convertNode(child);
		}
	}

	private void convertNode(Node node) {
		if (node instanceof TextNode text) {
			addText(text.getWholeText());
		} else if (node instanceof Element element) {
			convertElement(element);
		}
	}

	private void convertElement(Element element) {
		String tag = element.normalName();
		switch (tag) {
			case HTMLConstants.BR:
				lineBreak();
				break;
			case HTMLConstants.BOLD, HTMLConstants.STRONG:
				inline(element, new Inline(true, _inline.italic(), _inline.underline(), _inline.mono(),
					_inline.hyperlink()));
				break;
			case HTMLConstants.ITALICS, HTMLConstants.EM, HTMLConstants.CITE, HTMLConstants.VAR, HTMLConstants.DFN:
				inline(element, new Inline(_inline.bold(), true, _inline.underline(), _inline.mono(),
					_inline.hyperlink()));
				break;
			case HTMLConstants.U, HTMLConstants.INS:
				inline(element, new Inline(_inline.bold(), _inline.italic(), true, _inline.mono(),
					_inline.hyperlink()));
				break;
			case HTMLConstants.CODE, HTMLConstants.KBD, HTMLConstants.SAMP, HTMLConstants.TT:
				inline(element, new Inline(_inline.bold(), _inline.italic(), _inline.underline(), true,
					_inline.hyperlink()));
				break;
			case HTMLConstants.ANCHOR:
				String href = element.attr(HTMLConstants.HREF_ATTR).trim();
				inline(element, new Inline(_inline.bold(), _inline.italic(), _inline.underline(), _inline.mono(),
					href.isEmpty() || href.toLowerCase(Locale.ROOT).startsWith("javascript:") ? _inline.hyperlink()
						: href));
				break;
			case HTMLConstants.H1, HTMLConstants.H2, HTMLConstants.H3, HTMLConstants.H4, HTMLConstants.H5,
					HTMLConstants.H6:
				heading(element, tag.charAt(1) - '1');
				break;
			case HTMLConstants.PARAGRAPH, HTMLConstants.DIV, HTMLConstants.SECTION, HTMLConstants.ARTICLE,
					HTMLConstants.HEADER, HTMLConstants.FOOTER, HTMLConstants.MAIN, HTMLConstants.ASIDE,
					HTMLConstants.NAV, HTMLConstants.FIGURE, HTMLConstants.FIGCAPTION, HTMLConstants.ADDRESS,
					HTMLConstants.CENTER, HTMLConstants.DETAILS, HTMLConstants.SUMMARY, HTMLConstants.DL,
					HTMLConstants.DT, HTMLConstants.HR:
				block(element);
				break;
			case HTMLConstants.DD:
				_quoteDepth++;
				block(element);
				_quoteDepth--;
				break;
			case HTMLConstants.BLOCKQUOTE:
				endParagraph();
				_quoteDepth++;
				block(element);
				_quoteDepth--;
				break;
			case HTMLConstants.PRE:
				pre(element);
				break;
			case HTMLConstants.UL:
				list(element, null, 1);
				break;
			case HTMLConstants.OL:
				list(element, scheme(element.attr(HTMLConstants.TYPE_ATTR)),
					intAttr(element, HTMLConstants.START_ATTR, 1));
				break;
			case HTMLConstants.LI:
				listItem(element);
				break;
			case HTMLConstants.TABLE:
				if (_inCell) {
					block(element);
				} else {
					table(element);
				}
				break;
			case HTMLConstants.TR:
				block(element);
				break;
			case HTMLConstants.TD, HTMLConstants.TH:
				convertChildren(element);
				addText(" ");
				break;
			case HTMLConstants.IMG:
				if (!_inCell) {
					image(element);
				}
				break;
			case HTMLConstants.SCRIPT, HTMLConstants.STYLE_ELEMENT, HTMLConstants.HEAD, HTMLConstants.TITLE,
					HTMLConstants.TEMPLATE, HTMLConstants.NOSCRIPT:
				break;
			default:
				convertChildren(element);
				break;
		}
	}

	private void inline(Element element, Inline style) {
		Inline before = _inline;
		_inline = style;
		convertChildren(element);
		_inline = before;
	}

	private void block(Element element) {
		endParagraph();
		convertChildren(element);
		endParagraph();
	}

	private void heading(Element element, int index) {
		endParagraph();
		double size = _baseSize * HEADING_SCALE[index];
		_paragraph.put(BOLD, Boolean.TRUE);
		_paragraph.put(SIZE, number(size));
		_paragraph.put(KEEP_WITH_NEXT, Boolean.TRUE);
		if (!_paragraphs.isEmpty()) {
			_paragraph.put(SPACE_BEFORE, number(size * HEADING_SPACE));
		}
		convertChildren(element);
		endParagraph();
		_paragraph = map();
	}

	private void pre(Element element) {
		endParagraph();
		boolean before = _pre;
		Inline inlineBefore = _inline;
		_pre = true;
		_inline = new Inline(_inline.bold(), _inline.italic(), _inline.underline(), true, _inline.hyperlink());
		convertChildren(element);
		_inline = inlineBefore;
		_pre = before;
		endParagraph();
	}

	private void list(Element element, AutoNumberingScheme scheme, int start) {
		endParagraph();
		List<Object> paragraphs = _paragraphs;
		int before = paragraphs.size();
		_lists.add(new ListState(scheme, start));
		convertChildren(element);
		endParagraph();
		_lists.remove(_lists.size() - 1);
		if (_lists.isEmpty() && paragraphs == _paragraphs && paragraphs.size() > before) {
			// A list is separated from the following paragraph like a paragraph.
			@SuppressWarnings("unchecked")
			Map<String, Object> last = (Map<String, Object>) paragraphs.get(paragraphs.size() - 1);
			last.put(SPACE_AFTER, number(_baseSize * PARAGRAPH_SPACE));
		}
	}

	private void listItem(Element element) {
		endParagraph();
		if (_lists.isEmpty()) {
			convertChildren(element);
			endParagraph();
			return;
		}
		int depth = _lists.size() - 1;
		ListState list = _lists.get(depth);
		if (list.scheme() != null) {
			int number = intAttr(element, HTMLConstants.VALUE_ATTR, list.next());
			_lists.set(depth, new ListState(list.scheme(), number + 1));
			Map<String, Object> bullet = map();
			bullet.put(AUTO_NUMBER, list.scheme().name());
			bullet.put(START, Double.valueOf(Math.max(1, number)));
			_paragraph.put(BULLET, bullet);
		} else {
			_paragraph.put(BULLET, BULLETS[depth % BULLETS.length]);
		}
		convertChildren(element);
		endParagraph();
		_paragraph = map();
	}

	/**
	 * The indentation level of a paragraph in the current context.
	 */
	private int level() {
		return Math.min(_lists.size() + _quoteDepth - (_lists.isEmpty() ? 0 : 1),
			com.top_logic.presentation.model.ParagraphSpec.MAX_LEVEL);
	}

	private static AutoNumberingScheme scheme(String type) {
		switch (type) {
			case HTMLConstants.OL_TYPE_LOWER_ALPHA_VALUE:
				return AutoNumberingScheme.alphaLcPeriod;
			case HTMLConstants.OL_TYPE_UPPER_ALPHA_VALUE:
				return AutoNumberingScheme.alphaUcPeriod;
			case HTMLConstants.OL_TYPE_LOWER_ROMAN_VALUE:
				return AutoNumberingScheme.romanLcPeriod;
			case HTMLConstants.OL_TYPE_UPPER_ROMAN_VALUE:
				return AutoNumberingScheme.romanUcPeriod;
			default:
				return AutoNumberingScheme.arabicPeriod;
		}
	}

	private static int intAttr(Element element, String name, int defaultValue) {
		String value = element.attr(name).trim();
		if (value.isEmpty()) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			return defaultValue;
		}
	}

	private void addText(String text) {
		if (text.isEmpty()) {
			return;
		}
		if (_pre) {
			String[] lines = text.split("\r\n|\r|\n", -1);
			for (int n = 0; n < lines.length; n++) {
				if (n > 0) {
					endPreLine();
				}
				appendRun(lines[n]);
			}
			return;
		}
		String normalized = WHITE_SPACE.matcher(text).replaceAll(" ");
		if (_afterSpace && normalized.startsWith(" ")) {
			normalized = normalized.substring(1);
		}
		if (normalized.isEmpty()) {
			return;
		}
		appendRun(normalized);
		_afterSpace = normalized.endsWith(" ");
	}

	/**
	 * Ends a line of preformatted text: each line is a paragraph of its own, so that a long
	 * preformatted text can continue on a further slide.
	 */
	private void endPreLine() {
		if (_runs.isEmpty()) {
			appendRun("");
		}
		addParagraph();
	}

	private void appendRun(String text) {
		Map<String, Object> run = _inline.toRun(text);
		if (!_runs.isEmpty()) {
			Map<String, Object> last = _runs.get(_runs.size() - 1);
			if (sameStyle(last, run)) {
				last.put(TEXT, last.get(TEXT) + text);
				return;
			}
		}
		_runs.add(run);
	}

	private static boolean sameStyle(Map<String, Object> run, Map<String, Object> other) {
		if (run.size() != other.size()) {
			return false;
		}
		for (Map.Entry<String, Object> entry : run.entrySet()) {
			if (!TEXT.equals(entry.getKey()) && !Objects.equals(entry.getValue(), other.get(entry.getKey()))) {
				return false;
			}
		}
		return true;
	}

	private void lineBreak() {
		trimTrailingSpace();
		appendRun("\n");
		_afterSpace = true;
	}

	private void trimTrailingSpace() {
		while (!_runs.isEmpty()) {
			Map<String, Object> last = _runs.get(_runs.size() - 1);
			String text = (String) last.get(TEXT);
			String trimmed = text.endsWith(" ") ? text.substring(0, text.length() - 1) : text;
			if (!trimmed.isEmpty()) {
				last.put(TEXT, trimmed);
				return;
			}
			_runs.remove(_runs.size() - 1);
		}
	}

	/**
	 * Ends the paragraph being collected, if it has text.
	 *
	 * <p>
	 * Without text, the properties of the next paragraph (e.g. the bullet of a list item) are kept
	 * for the content following.
	 * </p>
	 */
	private void endParagraph() {
		if (!_pre) {
			trimTrailingSpace();
		}
		boolean empty = true;
		for (Map<String, Object> run : _runs) {
			String text = (String) run.get(TEXT);
			if (_pre || !text.isBlank()) {
				empty = false;
				break;
			}
		}
		if (empty) {
			_runs.clear();
			_afterSpace = true;
			return;
		}
		addParagraph();
	}

	private void addParagraph() {
		Map<String, Object> paragraph = map();
		paragraph.put(RUNS, _runs);
		paragraph.putAll(_paragraph);
		int level = level();
		if (level > 0) {
			paragraph.put(LEVEL, Double.valueOf(level));
		}
		if (_lists.isEmpty() && !_pre) {
			paragraph.put(SPACE_AFTER, number(_baseSize * PARAGRAPH_SPACE));
		}
		_paragraphs.add(paragraph);
		_runs = new ArrayList<>();
		_afterSpace = true;
		_paragraph = map();
	}

	/**
	 * Adds the collected paragraphs as text box to the blocks.
	 */
	private void flushText() {
		endParagraph();
		if (_paragraphs.isEmpty()) {
			return;
		}
		Map<String, Object> textBox = map();
		textBox.put(TYPE, TYPE_TEXT_BOX);
		textBox.put(TEXT, _paragraphs);
		textBox.put(HEIGHT, AUTO);
		textBox.put(SPLIT, Boolean.TRUE);
		_blocks.add(textBox);
		_paragraphs = new ArrayList<>();
	}

	private void table(Element table) {
		flushText();
		List<List<Object>> rows = new ArrayList<>();
		List<int[]> spans = new ArrayList<>();
		int headerRows = 0;
		boolean inHeader = true;
		for (Element row : table.select(HTMLConstants.TR)) {
			if (row.closest(HTMLConstants.TABLE) != table) {
				continue;
			}
			List<Object> cells = new ArrayList<>();
			int rowIndex = rows.size();
			boolean allHeader = true;
			int column = 0;
			for (Element cell : row.children()) {
				if (!cell.normalName().equals(HTMLConstants.TD) && !cell.normalName().equals(HTMLConstants.TH)) {
					continue;
				}
				column = skipSpanned(spans, rowIndex, cells, column);
				allHeader &= cell.normalName().equals(HTMLConstants.TH);
				cells.add(cellText(cell));
				column++;
				int colspan = Math.max(1, intAttr(cell, HTMLConstants.COLSPAN_ATTR, 1));
				int rowspan = Math.max(1, intAttr(cell, HTMLConstants.ROWSPAN_ATTR, 1));
				for (int n = 1; n < colspan; n++) {
					cells.add("");
					column++;
				}
				if (rowspan > 1) {
					spans.add(new int[] { rowIndex + 1, rowIndex + rowspan - 1, column - colspan, colspan });
				}
			}
			skipSpanned(spans, rowIndex, cells, Integer.MAX_VALUE);
			if (cells.isEmpty()) {
				continue;
			}
			boolean header = row.parent() != null && row.parent().normalName().equals(HTMLConstants.THEAD) || allHeader;
			if (inHeader && header) {
				headerRows++;
			} else {
				inHeader = false;
			}
			rows.add(cells);
		}
		if (rows.isEmpty()) {
			return;
		}
		int columns = 0;
		for (List<Object> row : rows) {
			columns = Math.max(columns, row.size());
		}
		double[] lengths = new double[columns];
		for (List<Object> row : rows) {
			while (row.size() < columns) {
				row.add("");
			}
			for (int column = 0; column < columns; column++) {
				lengths[column] = Math.max(lengths[column], textLength(row.get(column)));
			}
		}
		List<Object> weights = new ArrayList<>();
		for (double length : lengths) {
			weights.add(Double.valueOf(Math.max(MIN_COLUMN_WEIGHT, Math.min(MAX_COLUMN_WEIGHT, length))));
		}
		Map<String, Object> result = map();
		result.put(TYPE, TYPE_TABLE);
		result.put(COLUMN_WEIGHTS, weights);
		result.put(ROWS, rows);
		if (headerRows > 0) {
			result.put(HEADER_ROWS, Double.valueOf(headerRows));
		}
		_blocks.add(result);
	}

	/**
	 * Adds empty cells for the cells of preceding rows spanning into the given row up to the given
	 * column.
	 *
	 * @return The column of the next cell.
	 */
	private static int skipSpanned(List<int[]> spans, int row, List<Object> cells, int column) {
		boolean found = true;
		while (found) {
			found = false;
			for (int[] span : spans) {
				if (span[0] <= row && row <= span[1] && span[2] == cells.size() && span[2] <= column) {
					for (int n = 0; n < span[3]; n++) {
						cells.add("");
					}
					found = true;
				}
			}
		}
		return cells.size();
	}

	private static double textLength(Object cell) {
		if (cell instanceof List<?> paragraphs) {
			double result = 0;
			for (Object paragraph : paragraphs) {
				double length = 0;
				for (Object run : (List<?>) ((Map<?, ?>) paragraph).get(RUNS)) {
					length += ((String) ((Map<?, ?>) run).get(TEXT)).length();
				}
				result = Math.max(result, length);
			}
			return result;
		}
		return cell.toString().length();
	}

	/**
	 * The text of a table cell as list of paragraphs, without images and nested tables.
	 */
	private Object cellText(Element cell) {
		HtmlConverter converter = new HtmlConverter(_images, _baseSize);
		converter._inCell = true;
		converter.convertChildren(cell);
		converter.endParagraph();
		List<Object> paragraphs = converter._paragraphs;
		for (Object paragraph : paragraphs) {
			((Map<?, ?>) paragraph).remove(SPACE_AFTER);
		}
		return paragraphs.isEmpty() ? "" : paragraphs;
	}

	private void image(Element element) {
		String src = element.attr(HTMLConstants.SRC_ATTR).trim();
		BinaryData data = resolveImage(src);
		if (data == null) {
			Logger.warn("Image '" + abbreviate(src) + "' of HTML content not found, it is left out.",
				HtmlConverter.class);
			return;
		}
		flushText();
		Map<String, Object> result = map();
		result.put(TYPE, TYPE_IMAGE);
		result.put(DATA, data);
		Double width = imageWidth(element, data);
		PresentationData.put(result, WIDTH, width);
		result.put(HEIGHT, AUTO);
		_blocks.add(result);
	}

	private BinaryData resolveImage(String src) {
		if (src.isEmpty()) {
			return null;
		}
		if (src.regionMatches(true, 0, "data:", 0, 5)) {
			return dataUri(src);
		}
		BinaryData result = _images.get(src);
		if (result == null && I18NStructuredTextUtil.isImageReference(src)) {
			result = _images.get(I18NStructuredTextUtil.getImageID(src));
		}
		return result;
	}

	private static BinaryData dataUri(String src) {
		Matcher matcher = DATA_URI.matcher(src);
		if (!matcher.matches() || matcher.group(2) == null) {
			return null;
		}
		String contentType = matcher.group(1).isEmpty() ? "application/octet-stream" : matcher.group(1);
		byte[] content;
		try {
			content = Base64.getMimeDecoder().decode(matcher.group(3).trim());
		} catch (IllegalArgumentException ex) {
			return null;
		}
		int slash = contentType.indexOf('/');
		String extension = slash < 0 ? "" : "." + contentType.substring(slash + 1).replace("svg+xml", "svg");
		return BinaryDataFactory.createBinaryData(content, contentType, IMAGE_NAME + extension);
	}

	/**
	 * The width of an image in points: its <code>width</code> attribute or style, its height
	 * attribute or style scaled by its aspect ratio, or its own width; <code>null</code> if
	 * unknown.
	 */
	private static Double imageWidth(Element element, BinaryData data) {
		Double width = length(element, HTMLConstants.WIDTH_ATTR);
		if (width != null) {
			return number(width.doubleValue() * POINTS_PER_PIXEL);
		}
		Dimension size = imageSize(data);
		if (size == null || size.width <= 0 || size.height <= 0) {
			return null;
		}
		Double height = length(element, HTMLConstants.HEIGHT_ATTR);
		double pixels = height != null ? height.doubleValue() * size.width / size.height : size.width;
		return number(pixels * POINTS_PER_PIXEL);
	}

	private static Double length(Element element, String name) {
		Matcher attribute = LENGTH.matcher(element.attr(name));
		if (attribute.matches()) {
			return Double.valueOf(attribute.group(1));
		}
		Matcher style = Pattern.compile(String.format(STYLE_LENGTH, name), Pattern.CASE_INSENSITIVE)
			.matcher(element.attr(HTMLConstants.STYLE_ATTR));
		if (style.find()) {
			return Double.valueOf(style.group(1));
		}
		return null;
	}

	private static Dimension imageSize(BinaryData data) {
		try {
			return ImageUtils.getImageSize(data);
		} catch (IOException | RuntimeException ex) {
			return null;
		}
	}

	private static String abbreviate(String src) {
		return src.length() > 80 ? src.substring(0, 80) + "…" : src;
	}

}
