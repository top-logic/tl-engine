/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.presentation;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.List;

import javax.imageio.ImageIO;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.presentation.render.SlideRenderer;
import com.top_logic.presentation.text.TextMeasure;
import com.top_logic.presentation.text.TextMetrics;
import com.top_logic.util.error.TopLogicException;

/**
 * Test case for the TL-Script functions <code>pptRender()</code> and
 * <code>pptRenderLayouts()</code> and for {@link SlideRenderer}.
 *
 * <p>
 * The fixture is the TopLogic corporate template with 10 sample slides and 14 layouts.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestRender extends AbstractSearchExpressionTest {

	private static final String TEMPLATE = "toplogic-template.pptx";

	private static final int SLIDE_COUNT = 10;

	private static final int LAYOUT_COUNT = 14;

	/** Layout with a salmon helper raster (fill color #FFBCAD). */
	private static final int GRID_LAYOUT = 10;

	/** Content layout without helper raster. */
	private static final int CONTENT_LAYOUT = 11;

	private static final Color SALMON = new Color(0xFFBCAD);

	private BinaryData template() {
		return BinaryDataFactory.createBinaryData(TestRender.class, TEMPLATE);
	}

	public void testRenderAllSlides() throws Exception {
		List<?> images = (List<?>) eval("t -> pptRender($t)", template());
		assertEquals(SLIDE_COUNT, images.size());
		for (int n = 0; n < SLIDE_COUNT; n++) {
			BinaryData png = (BinaryData) images.get(n);
			assertEquals(String.format("slide-%02d.png", n), png.getName());
			assertEquals(SlideRenderer.PNG_CONTENT_TYPE, png.getContentType());

			BufferedImage image = decode(png);
			assertEquals(1280, image.getWidth());
			assertEquals(720, image.getHeight());
			assertTrue(png.getName() + " is blank.", ratio(image, TestRender::isNonWhite) > 0.5);
		}
	}

	public void testRenderSelectedSlides() throws Exception {
		List<?> images = (List<?>) eval("t -> pptRender($t, [9, 1], 400)", template());
		assertEquals(2, images.size());
		assertEquals("slide-09.png", ((BinaryData) images.get(0)).getName());
		assertEquals("slide-01.png", ((BinaryData) images.get(1)).getName());
		for (Object png : images) {
			BufferedImage image = decode((BinaryData) png);
			assertEquals(400, image.getWidth());
			assertEquals(225, image.getHeight());
		}

		List<?> single = (List<?>) eval("t -> pptRender($t, slides: 3, width: 160)", template());
		assertEquals(1, single.size());
		assertEquals("slide-03.png", ((BinaryData) single.get(0)).getName());
		assertEquals(90, decode((BinaryData) single.get(0)).getHeight());
	}

	public void testRenderLayouts() throws Exception {
		List<?> images = (List<?>) eval("t -> pptRenderLayouts($t, [" + GRID_LAYOUT + ", " + CONTENT_LAYOUT + "], 640)",
			template());
		assertEquals(2, images.size());
		assertEquals("layout-10.png", ((BinaryData) images.get(0)).getName());
		assertEquals("layout-11.png", ((BinaryData) images.get(1)).getName());

		BufferedImage grid = decode((BinaryData) images.get(0));
		BufferedImage content = decode((BinaryData) images.get(1));
		assertEquals(640, grid.getWidth());
		assertEquals(360, grid.getHeight());

		double gridSalmon = ratio(grid, TestRender::isSalmon);
		double contentSalmon = ratio(content, TestRender::isSalmon);
		assertTrue("Grid layout shows no raster: " + gridSalmon, gridSalmon > 0.2);
		assertTrue("Content layout shows a raster: " + contentSalmon, contentSalmon < 0.001);

		// The placeholder prompts are visible (dark blue text on the light background).
		assertTrue(ratio(content, rgb -> brightness(rgb) < 160) > 0.005);
	}

	public void testRenderAllLayouts() throws Exception {
		List<?> images = (List<?>) eval("t -> pptRenderLayouts($t, width: 64)", template());
		assertEquals(LAYOUT_COUNT, images.size());
		assertEquals("layout-13.png", ((BinaryData) images.get(LAYOUT_COUNT - 1)).getName());
	}

	public void testInvalidArguments() throws Exception {
		assertInvalid("t -> pptRender($t, width: 15)");
		assertInvalid("t -> pptRender($t, width: 8001)");
		assertInvalid("t -> pptRender($t, [0, " + SLIDE_COUNT + "])");
		assertInvalid("t -> pptRender($t, -1)");
		assertInvalid("t -> pptRenderLayouts($t, " + LAYOUT_COUNT + ")");
		assertInvalid("t -> pptRenderLayouts($t, width: 0)");
		assertInvalid("t -> pptRender(binary(name: 'notes.txt', data: 'Hello', contentType: 'text/plain'))");
	}

	public void testMissingFonts() throws Exception {
		try (XMLSlideShow slideShow = new XMLSlideShow(template().getStream())) {
			SlideRenderer renderer = new SlideRenderer();
			renderer.render(slideShow.getSlides().get(1), 200);
			assertEquals(!TextMeasure.isFontAvailable("Open Sans"), renderer.getMissingFonts().contains("Open Sans"));
			assertFalse(renderer.getMissingFonts().contains("Dialog"));
		}
	}

	/**
	 * The rendered text has the number of lines computed by {@link TextMeasure}.
	 *
	 * <p>
	 * The text consists of lower case letters without ascenders, descenders and dots only, so that
	 * each rendered line is a single band of pixel rows, separated from the next by blank rows.
	 * </p>
	 */
	public void testLinesAsMeasured() throws Exception {
		String text = "ernst war am see um neun saure runen murmeln an rauer zone mars oase was nun";
		String font = "SansSerif";
		double fontSize = 24;
		try (XMLSlideShow slideShow = new XMLSlideShow()) {
			XSLFSlide slide = slideShow.createSlide();
			for (double boxWidth : new double[] { 120, 250, 400, 700 }) {
				XSLFTextBox box = slide.createTextBox();
				try {
					box.setInsets(new Insets2D(0, 0, 0, 0));
					box.setWordWrap(true);
					box.setAnchor(new Rectangle2D.Double(10, 10, boxWidth, 500));
					TextMeasure.setText(box, text, font, fontSize, false, false, 0, false);
					TextMetrics metrics = TextMeasure.measure(text, font, fontSize, false, false, 0, false, Double.valueOf(boxWidth));

					BufferedImage image = new SlideRenderer().render(slide, 1440);
					assertEquals("Box width " + boxWidth, metrics.lines(), textRows(image));
				} finally {
					slide.removeShape(box);
				}
			}
		}
	}

	/**
	 * Renders all sample slides of the fixture and logs time and memory, without assertions.
	 */
	public void testPerformance() throws Exception {
		for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
			pool.resetPeakUsage();
		}
		Runtime runtime = Runtime.getRuntime();
		System.gc();
		long usedBefore = runtime.totalMemory() - runtime.freeMemory();

		long start = System.nanoTime();
		List<?> images = (List<?>) eval("t -> pptRender($t, width: 1280)", template());
		long millis = (System.nanoTime() - start) / 1_000_000;

		long peak = 0;
		for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
			if (pool.getType() == MemoryType.HEAP) {
				peak += pool.getPeakUsage().getUsed();
			}
		}
		Logger.info("Rendered " + images.size() + " slides at 1280px in " + millis + "ms, heap used before "
			+ usedBefore / 1_000_000 + "MB, peak heap " + peak / 1_000_000 + "MB.", TestRender.class);
	}

	private void assertInvalid(String script) throws Exception {
		try {
			eval(script, template());
			fail("Expected failure: " + script);
		} catch (TopLogicException ex) {
			// Expected.
		}
	}

	private static BufferedImage decode(BinaryData png) throws IOException {
		try (InputStream in = png.getStream()) {
			BufferedImage result = ImageIO.read(in);
			assertNotNull(png.getName() + " is no image.", result);
			return result;
		}
	}

	/**
	 * The number of bands of pixel rows containing dark pixels.
	 */
	private static int textRows(BufferedImage image) {
		int bands = 0;
		boolean inBand = false;
		for (int y = 0; y < image.getHeight(); y++) {
			boolean dark = false;
			for (int x = 0; x < image.getWidth() && !dark; x++) {
				dark = brightness(image.getRGB(x, y)) < 128;
			}
			if (dark && !inBand) {
				bands++;
			}
			inBand = dark;
		}
		return bands;
	}

	private static double ratio(BufferedImage image, java.util.function.IntPredicate test) {
		long count = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if (test.test(image.getRGB(x, y))) {
					count++;
				}
			}
		}
		return (double) count / (image.getWidth() * image.getHeight());
	}

	private static boolean isNonWhite(int rgb) {
		return brightness(rgb) < 250;
	}

	private static boolean isSalmon(int rgb) {
		Color color = new Color(rgb);
		return Math.abs(color.getRed() - SALMON.getRed()) <= 8
			&& Math.abs(color.getGreen() - SALMON.getGreen()) <= 8
			&& Math.abs(color.getBlue() - SALMON.getBlue()) <= 8;
	}

	private static int brightness(int rgb) {
		Color color = new Color(rgb);
		return (color.getRed() + color.getGreen() + color.getBlue()) / 3;
	}

	public static Test suite() {
		return suite(TestRender.class);
	}

}
