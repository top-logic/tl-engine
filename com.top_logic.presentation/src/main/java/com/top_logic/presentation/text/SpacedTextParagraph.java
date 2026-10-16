/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.text;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.font.TextMeasurer;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.common.usermodel.fonts.FontInfo;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawFontManager;
import org.apache.poi.sl.draw.DrawTextFragment;
import org.apache.poi.sl.draw.DrawTextParagraph;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextParagraph.TextAlign;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.xslf.model.CharacterPropertyFetcher;
import org.apache.poi.xslf.usermodel.XSLFTextRun;

/**
 * {@link DrawTextParagraph} applying the character spacing and kerning of its text runs.
 *
 * <p>
 * PowerPoint widens (or narrows) the distance between the characters of a run by its character
 * spacing (attribute <code>spc</code>, e.g. the letter-spaced titles of a template), and kerns the
 * characters of a run whose font size is at least its kerning size (attribute <code>kern</code>,
 * e.g. <code>kern="1200"</code> in the default text styles of a template, kerning text of 12
 * points and more). POI ignores both. This paragraph lays out such runs with a font that carries
 * the spacing as {@link TextAttribute#TRACKING} and the kerning as {@link TextAttribute#KERNING},
 * so that both the line breaks and the drawn glyphs follow them. Paragraphs without character
 * spacing and kerning are laid out by POI unchanged.
 * </p>
 *
 * <p>
 * Upper case text (attribute <code>cap="all"</code>) needs no special treatment: POI converts the
 * text to upper case before breaking it into lines.
 * </p>
 *
 * <p>
 * Lines are broken at the exact {@link #getWrappingWidth(boolean, Graphics2D) wrapping width}, and
 * the first line of a paragraph following another one is placed at the line spacing below the last
 * line of the previous paragraph, see {@link #draw(Graphics2D)}.
 * </p>
 */
public class SpacedTextParagraph extends DrawTextParagraph {

	/**
	 * The width in points POI adds to the {@link #getWrappingWidth(boolean, Graphics2D) wrapping
	 * width} when breaking lines.
	 */
	private static final double WRAPPING_SLACK = 1;

	/**
	 * The number of units of the kerning size (attribute <code>kern</code>) per point.
	 */
	private static final double KERNING_UNITS_PER_POINT = 100;

	private double _x;

	private double _y;

	/**
	 * The distance by which the paragraph is moved up when it is drawn, see
	 * {@link #draw(Graphics2D)}.
	 */
	private double _shift;

	/**
	 * Creates a {@link SpacedTextParagraph}.
	 */
	public SpacedTextParagraph(TextParagraph<?, ?, ?> paragraph) {
		super(paragraph);
	}

	@Override
	public void setPosition(double x, double y) {
		_x = x;
		_y = y;
		_shift = 0;
		super.setPosition(x, y);
	}

	/**
	 * Draws the paragraph at its position.
	 *
	 * <p>
	 * In PowerPoint, the baseline of the first line of a paragraph following another paragraph is
	 * the line spacing below the baseline of the last line of the previous paragraph (plus the
	 * space after and before the paragraphs), as for two lines within a paragraph. POI places the
	 * first line of such a paragraph additionally by the descent of the font lower. This paragraph
	 * is therefore drawn higher by the descent of its first line, which is the descent of the last
	 * line of the previous paragraph for a text in one font, and its {@link #getY() height} is
	 * reduced accordingly.
	 * </p>
	 */
	@Override
	public void draw(Graphics2D graphics) {
		double shift = isFirstParagraph() || lines.isEmpty() ? 0 : lines.get(0).getLayout().getDescent();
		super.setPosition(_x, _y - shift);
		super.draw(graphics);
		_shift = shift;
	}

	/**
	 * The vertical position before the paragraph is drawn, its height afterwards, measured from
	 * its position.
	 */
	@Override
	public double getY() {
		return super.getY() - _shift;
	}

	/**
	 * The width at which the lines of the paragraph are broken.
	 *
	 * <p>
	 * POI adds one point to this width when breaking lines, so that a line may be up to one point
	 * wider than the text box. PowerPoint and {@code LibreOffice} break at the exact width of the text box
	 * (minus its insets and the paragraph margins), so a word reaching into this additional point
	 * is moved to the next line or broken there. This width compensates the additional point.
	 * </p>
	 */
	@Override
	protected double getWrappingWidth(boolean firstLine, Graphics2D graphics) {
		return super.getWrappingWidth(firstLine, graphics) - WRAPPING_SLACK;
	}

	@Override
	protected void breakText(Graphics2D graphics) {
		super.breakText(graphics);

		List<SpacedRange> spacedRanges = spacedRanges(graphics);
		if (spacedRanges.isEmpty()) {
			return;
		}
		AttributedString text = attributedText();
		if (text == null) {
			// The text could not be reconstructed from the lines, keep POI's layout.
			return;
		}
		for (SpacedRange range : spacedRanges) {
			applySpacing(text, range);
		}
		layoutLines(graphics, text);
	}

	/**
	 * The ranges of {@link #rawText} belonging to text runs with character spacing or kerning.
	 *
	 * <p>
	 * The runs are mapped to the text in the same way as POI builds the text of the paragraph.
	 * </p>
	 */
	private List<SpacedRange> spacedRanges(Graphics2D graphics) {
		List<SpacedRange> result = new ArrayList<>();
		DrawFontManager fontManager = DrawFactory.getInstance(graphics).getFontManager(graphics);
		int position = 0;
		for (TextRun run : paragraph) {
			String runText = getRenderableText(graphics, run);
			if (runText == null || runText.isEmpty()) {
				continue;
			}
			FontInfo fontInfo = run.getFontInfo(null);
			runText = fontManager.mapFontCharset(graphics, fontInfo, runText);
			int begin = position;
			position += runText.length();

			if (run instanceof XSLFTextRun xslfRun) {
				double spacing = xslfRun.getCharacterSpacing();
				boolean kerning = isKerned(xslfRun);
				if (spacing != 0 || kerning) {
					result.add(new SpacedRange(begin, position, spacing, kerning));
				}
			}
		}
		if (position != rawText.length()) {
			// Text not built from the runs as expected.
			return List.of();
		}
		return result;
	}

	/**
	 * The attributed text of the whole paragraph, reassembled from the lines POI has created.
	 *
	 * @return The text, or <code>null</code> if the lines do not cover {@link #rawText}.
	 */
	private AttributedString attributedText() {
		AttributedString result = new AttributedString(rawText);
		int position = 0;
		for (int n = 0, cnt = lines.size(); n < cnt; n++) {
			AttributedCharacterIterator line = lines.get(n).getAttributedString().getIterator();
			int length = line.getEndIndex() - line.getBeginIndex();
			if (position + length > rawText.length()) {
				return null;
			}
			copyAttributes(line, result, position);
			position += length;

			boolean leadingBreak = n == 0 && rawText.startsWith("\n");
			if (!leadingBreak && position < rawText.length() && rawText.charAt(position) == '\n') {
				// The line break between two lines is not part of a line.
				position++;
			}
		}
		return position == rawText.length() ? result : null;
	}

	/**
	 * Whether the characters of the given run are kerned: The run has a kerning size (inherited
	 * from the text styles like any other character property), and its font size is not smaller.
	 */
	private static boolean isKerned(XSLFTextRun run) {
		Double kerningSize = new CharacterPropertyFetcher<Double>(run, (props, value) -> {
			if (props.isSetKern()) {
				value.accept(Double.valueOf(props.getKern() / KERNING_UNITS_PER_POINT));
			}
		}).fetchProperty(run.getParagraph().getParentShape());
		if (kerningSize == null || kerningSize.doubleValue() <= 0) {
			return false;
		}
		Double fontSize = run.getFontSize();
		return fontSize != null && fontSize.doubleValue() >= kerningSize.doubleValue();
	}

	private static void applySpacing(AttributedString text, SpacedRange range) {
		AttributedCharacterIterator it = text.getIterator(new Attribute[] { TextAttribute.FONT }, range.begin(),
			range.end());
		List<Runnable> updates = new ArrayList<>();
		for (char c = it.first(); c != AttributedCharacterIterator.DONE; c = it.setIndex(it.getRunLimit())) {
			Object value = it.getAttribute(TextAttribute.FONT);
			if (value instanceof Font font && font.getSize2D() > 0) {
				Map<Attribute, Object> attributes = new HashMap<>();
				if (range.spacing() != 0) {
					// Tracking is given in units of the font size.
					attributes.put(TextAttribute.TRACKING, Float.valueOf((float) (range.spacing() / font.getSize2D())));
				}
				if (range.kerning()) {
					attributes.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
				}
				Font spaced = font.deriveFont(attributes);
				int begin = it.getRunStart();
				int end = it.getRunLimit();
				updates.add(() -> text.addAttribute(TextAttribute.FONT, spaced, begin, end));
			}
		}
		updates.forEach(Runnable::run);
	}

	/**
	 * Breaks the given text into lines like {@link DrawTextParagraph#breakText(Graphics2D)}, but
	 * at break positions chosen by {@link #lineEnd(TextMeasurer, BreakIterator, int, int, double)}.
	 */
	private void layoutLines(Graphics2D graphics, AttributedString text) {
		lines.clear();

		DrawFactory factory = DrawFactory.getInstance(graphics);
		String noBreaks = rawText.replaceAll("[\\r\\n]", " ");
		AttributedString textNoBreaks = new AttributedString(noBreaks);
		copyAttributes(text.getIterator(), textNoBreaks, 0);

		AttributedCharacterIterator it = text.getIterator();
		AttributedCharacterIterator itNoBreaks = textNoBreaks.getIterator();
		LineBreakMeasurer measurer = new LineBreakMeasurer(it, graphics.getFontRenderContext());
		TextMeasurer advances = new TextMeasurer(it, graphics.getFontRenderContext());
		BreakIterator breaks = BreakIterator.getLineInstance();
		breaks.setText(rawText);
		while (true) {
			int startIndex = measurer.getPosition();

			// Adds the slack POI adds, which getWrappingWidth() compensates.
			double wrappingWidth = getWrappingWidth(lines.isEmpty(), graphics) + WRAPPING_SLACK;
			if (wrappingWidth < 0) {
				wrappingWidth = 1;
			}

			TextLayout layout;
			int endIndex;
			if (startIndex == 0 && rawText.startsWith("\n")) {
				layout = measurer.nextLayout((float) wrappingWidth, 1, false);
				endIndex = 1;
			} else {
				int nextBreak = rawText.indexOf('\n', startIndex + 1);
				if (nextBreak == -1) {
					nextBreak = it.getEndIndex();
				}

				int lineEnd = lineEnd(advances, breaks, startIndex, nextBreak, wrappingWidth);
				layout = measurer.nextLayout(Float.MAX_VALUE, lineEnd, false);
				if (layout == null) {
					break;
				}

				endIndex = measurer.getPosition();
				if (endIndex < it.getEndIndex() && rawText.charAt(endIndex) == '\n') {
					measurer.setPosition(endIndex + 1);
				}

				TextAlign align = paragraph.getTextAlign();
				if (align == TextAlign.JUSTIFY || align == TextAlign.JUSTIFY_LOW) {
					layout = layout.getJustifiedLayout((float) wrappingWidth);
				}
			}

			AttributedString lineText = new AttributedString(itNoBreaks, startIndex, endIndex);
			DrawTextFragment line = factory.getTextFragment(layout, lineText);
			lines.add(line);

			if (endIndex == it.getEndIndex()) {
				break;
			}
		}
	}

	/**
	 * The end of the line starting at the given index.
	 *
	 * <p>
	 * The line ends at the last break opportunity up to the given limit at which the text of the
	 * line, without trailing white space, is not wider than the given width. If not even the first
	 * word fits, the line ends after the last character that fits, but contains at least one
	 * character.
	 * </p>
	 *
	 * <p>
	 * The widths are measured with the kerning and character spacing of the text, which Java's
	 * {@link LineBreakMeasurer} ignores when choosing the break position.
	 * </p>
	 */
	private int lineEnd(TextMeasurer advances, BreakIterator breaks, int start, int limit, double width) {
		int result = -1;
		for (int candidate = breaks.following(start); candidate != BreakIterator.DONE;
				candidate = breaks.next()) {
			int end = Math.min(candidate, limit);
			if (!fits(advances, start, end, width)) {
				break;
			}
			result = end;
			if (candidate >= limit) {
				break;
			}
		}
		if (result > start) {
			return result;
		}
		int end = start + 1;
		while (end < limit && fits(advances, start, end + 1, width)) {
			end++;
		}
		return end;
	}

	private boolean fits(TextMeasurer advances, int start, int end, double width) {
		int visibleEnd = end;
		while (visibleEnd > start && Character.isWhitespace(rawText.charAt(visibleEnd - 1))) {
			visibleEnd--;
		}
		return visibleEnd == start || advances.getAdvanceBetween(start, visibleEnd) <= width;
	}

	/**
	 * Copies all attributes of the given text to the given target starting at the given offset.
	 */
	private static void copyAttributes(AttributedCharacterIterator source, AttributedString target, int offset) {
		int begin = source.getBeginIndex();
		for (char c = source.first(); c != AttributedCharacterIterator.DONE; c = source.setIndex(source.getRunLimit())) {
			int runStart = source.getRunStart();
			int runLimit = source.getRunLimit();
			for (Map.Entry<Attribute, Object> entry : source.getAttributes().entrySet()) {
				target.addAttribute(entry.getKey(), entry.getValue(), offset + runStart - begin,
					offset + runLimit - begin);
			}
		}
	}

	/**
	 * A range of the paragraph text with character spacing or kerning.
	 *
	 * @param begin
	 *        The index of the first character.
	 * @param end
	 *        The index after the last character.
	 * @param spacing
	 *        The additional space between two characters in points.
	 * @param kerning
	 *        Whether the characters are kerned.
	 */
	private record SpacedRange(int begin, int end, double spacing, boolean kerning) {
		// Pure data.
	}

}
