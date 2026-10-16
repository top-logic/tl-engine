/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.export;

import java.awt.geom.Rectangle2D;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;

import com.top_logic.presentation.model.Overflow;
import com.top_logic.presentation.model.ParagraphSpec;
import com.top_logic.presentation.model.RunSpec;
import com.top_logic.presentation.model.TextSpec;
import com.top_logic.presentation.text.TextMeasure;
import com.top_logic.presentation.text.TextMetrics;
import com.top_logic.util.error.TopLogicException;

/**
 * Writes a text into a shape or table cell and fits it into the available space according to its
 * {@link TextSpec#overflow()}.
 *
 * <p>
 * Whether a text fits is decided by {@link #fits(XSLFTextShape, boolean)}, which measures the
 * written shape with {@link TextMeasure#measure(org.apache.poi.sl.usermodel.TextShape)}, so that
 * the decision matches the rendered slide.
 * </p>
 *
 * <ul>
 * <li>{@link Overflow#SHRINK}: All font sizes of the text are multiplied by the largest factor
 * with which the text fits, found by bisection. No font size is reduced below the smallest font
 * size of the template profile. If the text does not fit at that size, it is cut off as with
 * {@link Overflow#ELLIPSIS} in the reduced size.</li>
 * <li>{@link Overflow#ELLIPSIS}: The text is cut off after the last word with which it fits,
 * followed by {@link #ELLIPSIS}. Paragraphs after the cut are left out.</li>
 * <li>{@link Overflow#ERROR}: A text that does not fit is reported as error.</li>
 * <li>{@link Overflow#VISIBLE}: The text is written as it is.</li>
 * </ul>
 *
 * <p>
 * The resulting font sizes are written explicitly, the shape gets no automatic fitting by
 * PowerPoint.
 * </p>
 */
final class TextFitting {

	/**
	 * The character marking a text that has been cut off.
	 */
	static final String ELLIPSIS = "…";

	/**
	 * The amount in points by which a measured text may exceed the available space without
	 * counting as overflow, compensating rounding errors.
	 */
	private static final double TOLERANCE = 0.01;

	/**
	 * The number of bisection steps when searching the font size factor.
	 */
	private static final int SCALE_STEPS = 8;

	/**
	 * The number of steps per point in which reduced font sizes are written.
	 */
	private static final double SIZE_STEPS = 10;

	private final TextWriter _writer;

	private final double _minFontSize;

	/**
	 * Creates a {@link TextFitting}.
	 *
	 * @param writer
	 *        Writes the text.
	 * @param minFontSize
	 *        The smallest font size in points to which a text is shrunk.
	 */
	TextFitting(TextWriter writer, double minFontSize) {
		_writer = writer;
		_minFontSize = minFontSize;
	}

	/**
	 * Replaces the text of the given shape and fits it into the shape.
	 *
	 * @param shape
	 *        The shape, whose anchor gives the available space.
	 * @param text
	 *        The text to write.
	 * @param bounded
	 *        Whether the height of the shape limits the text. The text of a table cell is only
	 *        limited by the width, since the row grows with its text.
	 * @param path
	 *        The path of the text in the description, for error messages.
	 * @param firstParagraph
	 *        The index of the first paragraph of the text in the description, if the text is a
	 *        part of a text continued over several slides.
	 */
	void write(XSLFTextShape shape, TextSpec text, boolean bounded, String path, int firstParagraph) {
		Overflow overflow = text.overflow() != null ? text.overflow() : Overflow.SHRINK;
		_writer.write(shape, text, path, firstParagraph);
		if (overflow == Overflow.VISIBLE || isBlank(text) || fits(shape, bounded)) {
			return;
		}
		if (overflow == Overflow.ERROR) {
			throw new TopLogicException(I18NConstants.ERROR_TEXT_OVERFLOW__PATH.fill(path));
		}

		double scale = 1;
		if (overflow == Overflow.SHRINK) {
			double minScale = minScale(shape);
			if (minScale < 1) {
				if (fitsScaled(shape, text, minScale, bounded, path, firstParagraph)) {
					double fitting = minScale;
					double tooLarge = 1;
					for (int step = 0; step < SCALE_STEPS; step++) {
						double candidate = (fitting + tooLarge) / 2;
						if (fitsScaled(shape, text, candidate, bounded, path, firstParagraph)) {
							fitting = candidate;
						} else {
							tooLarge = candidate;
						}
					}
					writeScaled(shape, text, fitting, path, firstParagraph);
					return;
				}
				scale = minScale;
			}
		}
		cutOff(shape, text, scale, bounded, path, firstParagraph);
	}

	/**
	 * Whether the text of the given shape fits into the shape.
	 *
	 * <p>
	 * The text fits, if no word is broken within, a text that is not wrapped is not wider than the
	 * shape, and, if the height is bounded, the text is not higher than the shape, each without
	 * the insets of the shape.
	 * </p>
	 *
	 * @param shape
	 *        The shape with its text.
	 * @param bounded
	 *        Whether the height of the shape limits the text.
	 */
	static boolean fits(XSLFTextShape shape, boolean bounded) {
		TextMetrics metrics = TextMeasure.measure(shape);
		if (metrics.wordBroken()) {
			return false;
		}
		Rectangle2D anchor = shape.getAnchor();
		Insets2D insets = shape.getInsets();
		if (!shape.getWordWrap() && metrics.width() > anchor.getWidth() - insets.left - insets.right + TOLERANCE) {
			return false;
		}
		return !bounded || metrics.height() <= anchor.getHeight() - insets.top - insets.bottom + TOLERANCE;
	}

	private boolean fitsScaled(XSLFTextShape shape, TextSpec text, double scale, boolean bounded, String path,
			int firstParagraph) {
		writeScaled(shape, text, scale, path, firstParagraph);
		return fits(shape, bounded);
	}

	private void writeScaled(XSLFTextShape shape, TextSpec text, double scale, String path, int firstParagraph) {
		_writer.write(shape, text, path, firstParagraph);
		if (scale < 1) {
			applyScale(shape, scale);
		}
	}

	/**
	 * The smallest factor for the font sizes of the given shape, with which no font size falls
	 * below the smallest font size.
	 */
	private double minScale(XSLFTextShape shape) {
		double smallest = Double.MAX_VALUE;
		for (XSLFTextParagraph paragraph : shape.getTextParagraphs()) {
			for (XSLFTextRun run : paragraph.getTextRuns()) {
				smallest = Math.min(smallest, fontSize(paragraph, run));
			}
		}
		if (smallest == Double.MAX_VALUE) {
			return 1;
		}
		return Math.min(1, _minFontSize / smallest);
	}

	/**
	 * Multiplies all font sizes of the freshly written text of the given shape by the given
	 * factor.
	 */
	private void applyScale(XSLFTextShape shape, double scale) {
		for (XSLFTextParagraph paragraph : shape.getTextParagraphs()) {
			double paragraphSize = paragraph.getDefaultFontSize().doubleValue();
			for (XSLFTextRun run : paragraph.getTextRuns()) {
				run.setFontSize(Double.valueOf(scaled(fontSize(paragraph, run), scale)));
			}
			CTTextParagraph xml = paragraph.getXmlObject();
			CTTextCharacterProperties end = xml.isSetEndParaRPr() ? xml.getEndParaRPr() : xml.addNewEndParaRPr();
			end.setSz((int) Math.round(scaled(paragraphSize, scale) * DrawingWriter.POINT_100));
		}
	}

	private static double fontSize(XSLFTextParagraph paragraph, XSLFTextRun run) {
		Double size = run.getFontSize();
		return size != null ? size.doubleValue() : paragraph.getDefaultFontSize().doubleValue();
	}

	private double scaled(double size, double scale) {
		double result = Math.floor(size * scale * SIZE_STEPS) / SIZE_STEPS;
		return Math.max(Math.max(Math.min(size, _minFontSize), TextMeasure.MIN_FONT_SIZE), result);
	}

	/**
	 * Writes the longest beginning of the given text, followed by {@link #ELLIPSIS}, that fits
	 * into the shape at the given font size factor.
	 */
	private void cutOff(XSLFTextShape shape, TextSpec text, double scale, boolean bounded, String path,
			int firstParagraph) {
		List<Cut> cuts = cuts(text.paragraphs());
		int fitting = 0;
		int tooLong = cuts.size();
		while (tooLong - fitting > 1) {
			int candidate = (fitting + tooLong) / 2;
			if (fitsScaled(shape, cut(text, cuts.get(candidate)), scale, bounded, path, firstParagraph)) {
				fitting = candidate;
			} else {
				tooLong = candidate;
			}
		}
		writeScaled(shape, cut(text, cuts.get(fitting)), scale, path, firstParagraph);
	}

	/**
	 * A position at which a text can be cut off.
	 *
	 * @param paragraph
	 *        The index of the paragraph that is cut.
	 * @param offset
	 *        The number of characters of the paragraph that are kept.
	 */
	private record Cut(int paragraph, int offset) {
		// Pure data.
	}

	/**
	 * All positions at which the given text can be cut off, in the order of the text: the start
	 * of each paragraph and each position between two words.
	 */
	private static List<Cut> cuts(List<ParagraphSpec> paragraphs) {
		List<Cut> result = new ArrayList<>();
		BreakIterator breaks = BreakIterator.getLineInstance();
		for (int n = 0; n < paragraphs.size(); n++) {
			result.add(new Cut(n, 0));
			String text = plainText(paragraphs.get(n));
			breaks.setText(text);
			for (int offset = breaks.following(0); offset != BreakIterator.DONE && offset < text.length();
					offset = breaks.next()) {
				result.add(new Cut(n, offset));
			}
		}
		return result;
	}

	/**
	 * Whether the given text shows no characters, so that there is nothing to fit.
	 */
	private static boolean isBlank(TextSpec text) {
		for (ParagraphSpec paragraph : text.paragraphs()) {
			if (!plainText(paragraph).isBlank()) {
				return false;
			}
		}
		return true;
	}

	private static String plainText(ParagraphSpec paragraph) {
		StringBuilder result = new StringBuilder();
		for (RunSpec run : paragraph.runs()) {
			result.append(run.text());
		}
		return result.toString();
	}

	/**
	 * The given text cut off at the given position, followed by {@link #ELLIPSIS}.
	 */
	private static TextSpec cut(TextSpec text, Cut cut) {
		List<ParagraphSpec> paragraphs = new ArrayList<>(text.paragraphs().subList(0, cut.paragraph()));
		paragraphs.add(cut(text.paragraphs().get(cut.paragraph()), cut.offset()));
		return text.withParagraphs(paragraphs);
	}

	private static ParagraphSpec cut(ParagraphSpec paragraph, int offset) {
		List<RunSpec> runs = new ArrayList<>();
		int remaining = offset;
		for (RunSpec run : paragraph.runs()) {
			if (remaining <= 0 && !runs.isEmpty()) {
				break;
			}
			String text = run.text();
			runs.add(new RunSpec(text.substring(0, Math.min(remaining, text.length())), run.style(), run.hyperlink()));
			remaining -= text.length();
		}
		// Removes the white space before the cut.
		while (runs.size() > 1 && runs.get(runs.size() - 1).text().isBlank()) {
			runs.remove(runs.size() - 1);
		}
		if (runs.isEmpty()) {
			runs.add(RunSpec.plain(ELLIPSIS));
		} else {
			RunSpec last = runs.get(runs.size() - 1);
			runs.set(runs.size() - 1,
				new RunSpec(last.text().stripTrailing() + ELLIPSIS, last.style(), last.hyperlink()));
		}
		return new ParagraphSpec(runs, paragraph.style(), paragraph.align(), paragraph.level(), paragraph.bullet(),
			paragraph.spaceBefore(), paragraph.spaceAfter(), paragraph.lineSpacing(), paragraph.keepWithNext());
	}

}
