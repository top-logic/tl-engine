/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.text;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.sl.draw.DrawTextFragment;
import org.apache.poi.sl.draw.DrawTextParagraph;
import org.apache.poi.sl.draw.DrawTextShape;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextShape;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import com.top_logic.presentation.export.DrawingWriter;
import com.top_logic.presentation.export.TextWriter;
import com.top_logic.presentation.model.ParagraphSpec;
import com.top_logic.presentation.model.RunStyle;
import com.top_logic.presentation.model.TextSpec;

/**
 * Measures text as it is laid out in a PowerPoint text box.
 *
 * <p>
 * The measurement runs the text layout of Apache POI itself, the same code that breaks lines when
 * POI renders a slide into an image. Text measured here therefore takes the same number of lines
 * as the rendered text, as long as the rendering {@link Graphics2D} is configured by
 * {@link #applyRenderingHints(Graphics2D)}: It uses the {@link #RENDERING_HINTS} and lays out text
 * through the {@link TextDrawFactory}, which applies the character spacing POI ignores.
 * </p>
 *
 * <p>
 * All lengths are given in points (1/72 inch), the unit of POI's slide coordinates.
 * </p>
 */
public final class TextMeasure {

	/**
	 * The width in points of the text box in which text is laid out that is not wrapped.
	 *
	 * <p>
	 * The value is close to the largest width POI can store for a shape (an <code>int</code>
	 * number of EMU, 12700 per point).
	 * </p>
	 */
	public static final double UNBOUNDED_WIDTH = 100_000;

	/**
	 * The rendering hints that determine the font metrics of the text layout.
	 *
	 * <p>
	 * Anti-aliasing and fractional metrics make the glyph advances independent of the scale in
	 * which the text is drawn, so that a slide rendered with an arbitrary resolution breaks its
	 * lines at the positions computed by the measurement.
	 * </p>
	 */
	public static final Map<RenderingHints.Key, Object> RENDERING_HINTS = Map.of(
		RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON,
		RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
		RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON,
		RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

	/**
	 * The smallest font size in points a PowerPoint text can have.
	 */
	public static final double MIN_FONT_SIZE = 1;

	/**
	 * The largest font size in points a PowerPoint text can have.
	 */
	public static final double MAX_FONT_SIZE = 4000;

	private static final TextWriter TEXT_WRITER = new TextWriter(DrawingWriter.WITHOUT_ROLES);

	/**
	 * The path of the text in error messages.
	 */
	private static final String TEXT = "text";

	private static final ThreadLocal<XSLFSlide> SCRATCH_SLIDE = ThreadLocal.withInitial(TextMeasure::createSlide);

	private TextMeasure() {
		// Static utility.
	}

	/**
	 * Sets the {@link #RENDERING_HINTS} to the given graphics and installs the
	 * {@link TextDrawFactory} as {@link Drawable#DRAW_FACTORY}.
	 *
	 * <p>
	 * A slide rendered into a graphics configured this way breaks its text into lines as computed
	 * by {@link #measure(TextShape)}. A caller installing its own draw factory afterwards must
	 * derive it from {@link TextDrawFactory}.
	 * </p>
	 */
	public static void applyRenderingHints(Graphics2D graphics) {
		graphics.addRenderingHints(RENDERING_HINTS);
		graphics.setRenderingHint(Drawable.DRAW_FACTORY, TextDrawFactory.INSTANCE);
	}

	/**
	 * Measures a text written in a single font.
	 *
	 * <p>
	 * The text is laid out like in a text box without insets filled by
	 * {@link #setText(XSLFTextShape, String, String, double, boolean, boolean, double, boolean)}.
	 * </p>
	 *
	 * @param text
	 *        The text to measure. Each line break (<code>\n</code>) starts a new paragraph.
	 * @param fontFamily
	 *        The name of the font family.
	 * @param fontSize
	 *        The font size in points, see {@link #isValidFontSize(double)}.
	 * @param bold
	 *        Whether the text is set in bold face.
	 * @param italic
	 *        Whether the text is set in italics.
	 * @param spacing
	 *        The additional space between two characters in points, negative to condense the text.
	 * @param caps
	 *        Whether the text is shown in upper case letters.
	 * @param width
	 *        The width in points available for the text, or <code>null</code> if the text is not
	 *        wrapped.
	 * @return The extent of the laid out text.
	 */
	public static TextMetrics measure(String text, String fontFamily, double fontSize, boolean bold,
			boolean italic, double spacing, boolean caps, Double width) {
		if (!isValidFontSize(fontSize)) {
			throw new IllegalArgumentException(
				"Font size must be between " + MIN_FONT_SIZE + "pt and " + MAX_FONT_SIZE + "pt: " + fontSize);
		}
		if (width != null && !(width.doubleValue() > 0)) {
			throw new IllegalArgumentException("Width must be positive: " + width);
		}

		XSLFSlide slide = SCRATCH_SLIDE.get();
		XSLFTextBox box = slide.createTextBox();
		try {
			box.setInsets(new Insets2D(0, 0, 0, 0));
			box.setWordWrap(true);
			box.setAnchor(new Rectangle2D.Double(0, 0, width == null ? UNBOUNDED_WIDTH : width.doubleValue(), 0));
			setText(box, text, fontFamily, fontSize, bold, italic, spacing, caps);
			return measure(box);
		} finally {
			slide.removeShape(box);
		}
	}

	/**
	 * Measures the text of the given shape as it is laid out in the shape's bounds.
	 *
	 * <p>
	 * The text is wrapped at the width of the shape's anchor minus its insets, the paragraph
	 * properties (indentation, line spacing, space before and after a paragraph) of the shape are
	 * applied.
	 * </p>
	 *
	 * @param shape
	 *        The text shape to measure.
	 * @return The extent of the laid out text.
	 */
	public static TextMetrics measure(TextShape<?, ?> shape) {
		RecordingDrawFactory factory = new RecordingDrawFactory();

		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		double height;
		try {
			applyRenderingHints(graphics);
			graphics.setRenderingHint(Drawable.DRAW_FACTORY, factory);
			height = new DrawTextShape(shape).drawParagraphs(graphics, 0, 0);
		} finally {
			graphics.dispose();
		}

		double width = 0;
		int lines = 0;
		double lineHeight = 0;
		for (RecordingTextParagraph paragraph : factory.getParagraphs()) {
			for (DrawTextFragment line : paragraph.getLines()) {
				lines++;
				width = Math.max(width, line.getLayout().getVisibleAdvance());
				lineHeight = Math.max(lineHeight, line.getHeight());
			}
		}
		return new TextMetrics(width, lines, height, lineHeight, fontsAvailable(shape));
	}

	/**
	 * Replaces the text of the given shape with a text in a single font.
	 *
	 * <p>
	 * Each line of the text becomes a paragraph of its own. An empty line keeps the font size, since
	 * the font of each paragraph is also set as properties of the paragraph end.
	 * </p>
	 *
	 * @param shape
	 *        The shape to fill.
	 * @param text
	 *        The text, <code>null</code> is the empty text.
	 * @param fontFamily
	 *        The name of the font family.
	 * @param fontSize
	 *        The font size in points, see {@link #isValidFontSize(double)}.
	 * @param bold
	 *        Whether the text is set in bold face.
	 * @param italic
	 *        Whether the text is set in italics.
	 * @param spacing
	 *        The additional space between two characters in points, negative to condense the text.
	 * @param caps
	 *        Whether the text is shown in upper case letters.
	 */
	public static void setText(XSLFTextShape shape, String text, String fontFamily, double fontSize, boolean bold,
			boolean italic, double spacing, boolean caps) {
		List<ParagraphSpec> paragraphs = new ArrayList<>();
		for (String line : lines(text)) {
			paragraphs.add(ParagraphSpec.plain(line));
		}
		RunStyle style = new RunStyle(Boolean.valueOf(bold), Boolean.valueOf(italic), null,
			caps ? Boolean.TRUE : null, Double.valueOf(fontSize), null, fontFamily,
			spacing != 0 ? Double.valueOf(spacing) : null);
		TEXT_WRITER.write(shape, new TextSpec(paragraphs, style, null, null, null, null, null), TEXT);
	}

	/**
	 * Whether the given font size can be used in a PowerPoint text.
	 *
	 * @param fontSize
	 *        The font size in points.
	 * @return Whether the size is between {@link #MIN_FONT_SIZE} and {@link #MAX_FONT_SIZE}.
	 */
	public static boolean isValidFontSize(double fontSize) {
		return fontSize >= MIN_FONT_SIZE && fontSize <= MAX_FONT_SIZE;
	}

	/**
	 * Whether a font of the given family is installed.
	 *
	 * <p>
	 * Text in a font that is not installed is drawn and measured in a replacement font.
	 * </p>
	 *
	 * @param fontFamily
	 *        The name of the font family, or the name of one of Java's logical fonts such as
	 *        {@link Font#SANS_SERIF}.
	 */
	public static boolean isFontAvailable(String fontFamily) {
		if (fontFamily == null || fontFamily.isBlank()) {
			return false;
		}
		// An unknown family name silently resolves to the logical font "Dialog".
		Font font = new Font(fontFamily, Font.PLAIN, 12);
		return !Font.DIALOG.equals(font.getFamily()) || Font.DIALOG.equalsIgnoreCase(fontFamily);
	}

	private static boolean fontsAvailable(TextShape<?, ?> shape) {
		for (TextParagraph<?, ?, ?> paragraph : shape.getTextParagraphs()) {
			for (var run : paragraph.getTextRuns()) {
				String fontFamily = run.getFontFamily();
				if (fontFamily != null && !isFontAvailable(fontFamily)) {
					return false;
				}
			}
		}
		return true;
	}

	private static String[] lines(String text) {
		if (text == null) {
			return new String[] { "" };
		}
		return text.split("\\r\\n?|\\n", -1);
	}

	private static XSLFSlide createSlide() {
		XMLSlideShow slideShow = new XMLSlideShow();
		return slideShow.createSlide();
	}

	/**
	 * {@link TextDrawFactory} recording the paragraphs it creates during the text layout.
	 */
	private static final class RecordingDrawFactory extends TextDrawFactory {

		private final List<RecordingTextParagraph> _paragraphs = new ArrayList<>();

		@Override
		public DrawTextParagraph getDrawable(TextParagraph<?, ?, ?> paragraph) {
			RecordingTextParagraph result = new RecordingTextParagraph(paragraph);
			_paragraphs.add(result);
			return result;
		}

		List<RecordingTextParagraph> getParagraphs() {
			return _paragraphs;
		}

	}

	/**
	 * {@link SpacedTextParagraph} giving access to the lines of its layout.
	 */
	private static final class RecordingTextParagraph extends SpacedTextParagraph {

		RecordingTextParagraph(TextParagraph<?, ?, ?> paragraph) {
			super(paragraph);
		}

		List<DrawTextFragment> getLines() {
			return lines;
		}

	}

}
