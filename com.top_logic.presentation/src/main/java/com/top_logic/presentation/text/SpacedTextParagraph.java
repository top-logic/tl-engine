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
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.util.ArrayList;
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
import org.apache.poi.xslf.usermodel.XSLFTextRun;

/**
 * {@link DrawTextParagraph} applying the character spacing of its text runs.
 *
 * <p>
 * PowerPoint widens (or narrows) the distance between the characters of a run by its character
 * spacing (attribute <code>spc</code>, e.g. the letter-spaced titles of a template). POI ignores
 * this spacing. This paragraph lays out runs having a character spacing with a font that carries
 * the spacing as {@link TextAttribute#TRACKING}, so that both the line breaks and the drawn glyphs
 * follow the spacing. Paragraphs without character spacing are laid out by POI unchanged.
 * </p>
 *
 * <p>
 * Upper case text (attribute <code>cap="all"</code>) needs no special treatment: POI converts the
 * text to upper case before breaking it into lines.
 * </p>
 */
public class SpacedTextParagraph extends DrawTextParagraph {

	/**
	 * Creates a {@link SpacedTextParagraph}.
	 */
	public SpacedTextParagraph(TextParagraph<?, ?, ?> paragraph) {
		super(paragraph);
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
	 * The ranges of {@link #rawText} belonging to text runs with character spacing.
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
				if (spacing != 0) {
					result.add(new SpacedRange(begin, position, spacing));
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

	private static void applySpacing(AttributedString text, SpacedRange range) {
		AttributedCharacterIterator it = text.getIterator(new Attribute[] { TextAttribute.FONT }, range.begin(),
			range.end());
		List<Runnable> updates = new ArrayList<>();
		for (char c = it.first(); c != AttributedCharacterIterator.DONE; c = it.setIndex(it.getRunLimit())) {
			Object value = it.getAttribute(TextAttribute.FONT);
			if (value instanceof Font font && font.getSize2D() > 0) {
				// Tracking is given in units of the font size.
				Font spaced = font.deriveFont(
					Map.of(TextAttribute.TRACKING, Float.valueOf((float) (range.spacing() / font.getSize2D()))));
				int begin = it.getRunStart();
				int end = it.getRunLimit();
				updates.add(() -> text.addAttribute(TextAttribute.FONT, spaced, begin, end));
			}
		}
		updates.forEach(Runnable::run);
	}

	/**
	 * Breaks the given text into lines like {@link DrawTextParagraph#breakText(Graphics2D)}.
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
		while (true) {
			int startIndex = measurer.getPosition();

			// One additional pixel compensates rounding errors, as in POI.
			double wrappingWidth = getWrappingWidth(lines.isEmpty(), graphics) + 1;
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

				layout = measurer.nextLayout((float) wrappingWidth, nextBreak, true);
				if (layout == null) {
					// A single word does not fit, break within the word.
					layout = measurer.nextLayout((float) wrappingWidth, nextBreak, false);
				}
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
	 * A range of the paragraph text with character spacing.
	 *
	 * @param begin
	 *        The index of the first character.
	 * @param end
	 *        The index after the last character.
	 * @param spacing
	 *        The additional space between two characters in points.
	 */
	private record SpacedRange(int begin, int end, double spacing) {
		// Pure data.
	}

}
