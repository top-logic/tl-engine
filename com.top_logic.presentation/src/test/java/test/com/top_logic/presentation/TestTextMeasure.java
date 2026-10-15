/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.presentation;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawTextFragment;
import org.apache.poi.sl.draw.DrawTextParagraph;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;

import com.top_logic.presentation.script.PptTextFunctions;
import com.top_logic.presentation.text.TextMeasure;
import com.top_logic.presentation.text.TextMetrics;
import com.top_logic.util.error.TopLogicException;

/**
 * Test case for the TL-Script function <code>pptMeasureText()</code> and {@link TextMeasure}.
 */
@SuppressWarnings("javadoc")
public class TestTextMeasure extends AbstractSearchExpressionTest {

	private static final String FONT = "SansSerif";

	private static final String TEXT =
		"The quick brown fox jumps over the lazy dog. Pack my box with five dozen liquor jugs. "
			+ "How vexingly quick daft zebras jump.";

	public void testSingleLine() throws Exception {
		Map<?, ?> result = measure("pptMeasureText('Hello world', font: 'SansSerif', size: 12)");

		assertEquals(1.0, number(result, PptTextFunctions.LINES));
		assertTrue(number(result, PptTextFunctions.WIDTH) > 0);
		assertTrue(number(result, PptTextFunctions.LINE_HEIGHT) >= 12);
		assertTrue(number(result, PptTextFunctions.HEIGHT) >= number(result, PptTextFunctions.LINE_HEIGHT));
		assertEquals(Boolean.TRUE, result.get(PptTextFunctions.FONT_AVAILABLE));
	}

	public void testWrapping() throws Exception {
		Map<?, ?> wide = measure("pptMeasureText('" + TEXT + "', font: 'SansSerif', size: 14, width: 1000)");
		Map<?, ?> narrow = measure("pptMeasureText('" + TEXT + "', font: 'SansSerif', size: 14, width: 150)");

		assertEquals(1.0, number(wide, PptTextFunctions.LINES));
		assertTrue(number(narrow, PptTextFunctions.LINES) > 2);
		assertTrue(number(narrow, PptTextFunctions.WIDTH) <= 150 + 1);
		assertTrue(number(narrow, PptTextFunctions.HEIGHT) > number(wide, PptTextFunctions.HEIGHT));
		assertEquals(number(wide, PptTextFunctions.LINE_HEIGHT), number(narrow, PptTextFunctions.LINE_HEIGHT), 0.01);
	}

	public void testNoWrappingWithoutWidth() throws Exception {
		Map<?, ?> unbounded = measure("pptMeasureText('" + TEXT + "', font: 'SansSerif', size: 14)");

		assertEquals(1.0, number(unbounded, PptTextFunctions.LINES));
		assertTrue(number(unbounded, PptTextFunctions.WIDTH) > 500);
	}

	public void testHeightGrowsWithLines() throws Exception {
		double previousHeight = 0;
		for (int lines = 1; lines <= 4; lines++) {
			String text = String.join("\n", Collections.nCopies(lines, "Line"));
			TextMetrics metrics = TextMeasure.measure(text, FONT, 12, false, false, null);

			assertEquals(lines, metrics.lines());
			assertTrue(metrics.height() > previousHeight);
			previousHeight = metrics.height();
		}
	}

	public void testLineBreak() throws Exception {
		Map<?, ?> single = measure("pptMeasureText('Hello world', size: 12)");
		Map<?, ?> broken = measure("pptMeasureText('Hello\\nworld', size: 12)");
		Map<?, ?> emptyLine = measure("pptMeasureText('Hello\\n\\nworld', size: 12)");

		assertEquals(2.0, number(broken, PptTextFunctions.LINES));
		assertEquals(3.0, number(emptyLine, PptTextFunctions.LINES));
		assertTrue(number(broken, PptTextFunctions.WIDTH) < number(single, PptTextFunctions.WIDTH));

		// An empty line is as high as a line with text.
		double lineAdvance = number(broken, PptTextFunctions.HEIGHT) - number(single, PptTextFunctions.HEIGHT);
		double emptyLineAdvance = number(emptyLine, PptTextFunctions.HEIGHT) - number(broken, PptTextFunctions.HEIGHT);
		assertEquals(lineAdvance, emptyLineAdvance, 0.01);
	}

	public void testBold() throws Exception {
		Map<?, ?> regular = measure("pptMeasureText('Some words in a text', size: 20)");
		Map<?, ?> bold = measure("pptMeasureText('Some words in a text', size: 20, bold: true)");

		assertTrue(number(bold, PptTextFunctions.WIDTH) >= number(regular, PptTextFunctions.WIDTH));
	}

	public void testFontSize() throws Exception {
		Map<?, ?> small = measure("pptMeasureText('Some words', size: 10)");
		Map<?, ?> large = measure("pptMeasureText('Some words', size: 20)");

		assertTrue(number(large, PptTextFunctions.WIDTH) > number(small, PptTextFunctions.WIDTH));
		assertTrue(number(large, PptTextFunctions.LINE_HEIGHT) > number(small, PptTextFunctions.LINE_HEIGHT));
	}

	public void testUnknownFont() throws Exception {
		Map<?, ?> result = measure("pptMeasureText('Hello', font: 'No Such Font Family 4711', size: 12)");

		assertEquals(Boolean.FALSE, result.get(PptTextFunctions.FONT_AVAILABLE));
		assertEquals(1.0, number(result, PptTextFunctions.LINES));
		assertFalse(TextMeasure.isFontAvailable("No Such Font Family 4711"));
		assertTrue(TextMeasure.isFontAvailable("Dialog"));
		assertTrue(TextMeasure.isFontAvailable("serif"));
	}

	public void testInvalidArguments() throws Exception {
		assertInvalid("pptMeasureText('Hello', size: 0)");
		assertInvalid("pptMeasureText('Hello', size: 12, width: 0)");
		assertInvalid("pptMeasureText('Hello', size: 12, width: -10)");
	}

	/**
	 * Lays out the text in a text box with insets on a slide that is rendered in double
	 * resolution, and compares the lines drawn by POI with the measurement.
	 */
	public void testMatchesRendering() throws Exception {
		for (double boxWidth : new double[] { 120, 200, 333, 480 }) {
			for (double fontSize : new double[] { 11, 14, 18 }) {
				assertMatchesRendering(TEXT + "\nSecond paragraph.\n\nAfter an empty line.", boxWidth, fontSize);
			}
		}
	}

	private void assertMatchesRendering(String text, double boxWidth, double fontSize) throws Exception {
		try (XMLSlideShow slideShow = new XMLSlideShow()) {
			XSLFSlide slide = slideShow.createSlide();
			XSLFTextBox box = slide.createTextBox();
			box.setAnchor(new Rectangle2D.Double(37, 51, boxWidth, 400));
			box.setWordWrap(true);
			TextMeasure.setText(box, text, FONT, fontSize, false, false);

			Insets2D insets = box.getInsets();
			TextMetrics measured = TextMeasure.measure(text, FONT, fontSize, false, false,
				Double.valueOf(boxWidth - insets.left - insets.right));

			double scale = 2;
			BufferedImage image = new BufferedImage((int) (720 * scale), (int) (540 * scale),
				BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = image.createGraphics();
			Recorder recorder = new Recorder();
			try {
				TextMeasure.applyRenderingHints(graphics);
				graphics.setRenderingHint(Drawable.DRAW_FACTORY, recorder);
				graphics.scale(scale, scale);
				slide.draw(graphics);
			} finally {
				graphics.dispose();
			}

			String context = "width " + boxWidth + ", size " + fontSize;
			assertEquals(context, recorder.lines(), measured.lines());
			assertEquals(context, recorder.width(), measured.width(), 0.5);
			assertEquals(context, measured.height(), textHeight(box), 0.01);

			TextMetrics boxMetrics = TextMeasure.measure(box);
			assertEquals(context, measured.lines(), boxMetrics.lines());
			assertEquals(context, measured.width(), boxMetrics.width(), 0.01);
			assertEquals(context, measured.height(), boxMetrics.height(), 0.01);
		}
	}

	/**
	 * The text height computed by POI for a graphics configured like the renderer's.
	 */
	private static double textHeight(XSLFTextBox box) {
		Graphics2D graphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
		try {
			TextMeasure.applyRenderingHints(graphics);
			return box.getTextHeight(graphics);
		} finally {
			graphics.dispose();
		}
	}

	private void assertInvalid(String script) throws Exception {
		try {
			eval(script);
			fail("Expected failure: " + script);
		} catch (TopLogicException ex) {
			// Expected.
		}
	}

	private Map<?, ?> measure(String script) throws Exception {
		return (Map<?, ?>) eval(script);
	}

	private static double number(Map<?, ?> result, String key) {
		Object value = result.get(key);
		assertTrue("Not a Double: " + key + "=" + value, value instanceof Double);
		return ((Double) value).doubleValue();
	}

	/**
	 * {@link DrawFactory} recording the lines drawn when rendering a slide.
	 */
	private static final class Recorder extends DrawFactory {

		private final List<RecordingParagraph> _paragraphs = new ArrayList<>();

		@Override
		public DrawTextParagraph getDrawable(TextParagraph<?, ?, ?> paragraph) {
			RecordingParagraph result = new RecordingParagraph(paragraph);
			_paragraphs.add(result);
			return result;
		}

		int lines() {
			return _paragraphs.stream().mapToInt(p -> p.lines().size()).sum();
		}

		double width() {
			return _paragraphs.stream()
				.flatMap(p -> p.lines().stream())
				.mapToDouble(l -> l.getLayout().getVisibleAdvance())
				.max().orElse(0);
		}
	}

	private static final class RecordingParagraph extends DrawTextParagraph {

		RecordingParagraph(TextParagraph<?, ?, ?> paragraph) {
			super(paragraph);
		}

		List<DrawTextFragment> lines() {
			return lines;
		}
	}

	public static Test suite() {
		return suite(TestTextMeasure.class);
	}

}
