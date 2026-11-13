/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.image.util;

import java.awt.image.BufferedImage;

import junit.framework.TestCase;

import com.top_logic.layout.image.util.ImageUtils;

/**
 * Test for {@link ImageUtils#scaleToBoundary(BufferedImage, int, int)}.
 */
@SuppressWarnings("javadoc")
public class TestImageUtils extends TestCase {

	public void testScaleDownKeepsAspect() {
		BufferedImage scaled = ImageUtils.scaleToBoundary(image(400, 200, BufferedImage.TYPE_INT_RGB), 32, 32);

		assertEquals(32, scaled.getWidth());
		assertEquals(16, scaled.getHeight());
	}

	public void testScaleDownByHeight() {
		BufferedImage scaled = ImageUtils.scaleToBoundary(image(100, 300, BufferedImage.TYPE_INT_RGB), 40, 60);

		assertEquals(20, scaled.getWidth());
		assertEquals(60, scaled.getHeight());
	}

	public void testNoEnlargement() {
		BufferedImage small = image(20, 10, BufferedImage.TYPE_INT_RGB);

		assertSame(small, ImageUtils.scaleToBoundary(small, 32, 32));
	}

	public void testKeepsTransparency() {
		BufferedImage input = image(400, 400, BufferedImage.TYPE_INT_ARGB);
		// Left half opaque red, right half fully transparent.
		for (int y = 0; y < 400; y++) {
			for (int x = 0; x < 200; x++) {
				input.setRGB(x, y, 0xFFFF0000);
			}
		}

		BufferedImage scaled = ImageUtils.scaleToBoundary(input, 32, 32);

		assertTrue(scaled.getColorModel().hasAlpha());
		assertEquals(0, scaled.getRGB(30, 16) >>> 24);
		assertEquals(0xFF, scaled.getRGB(2, 16) >>> 24);
	}

	public void testOpaqueStaysOpaque() {
		BufferedImage scaled = ImageUtils.scaleToBoundary(image(400, 400, BufferedImage.TYPE_INT_RGB), 32, 32);

		assertFalse(scaled.getColorModel().hasAlpha());
	}

	private static BufferedImage image(int width, int height, int type) {
		return new BufferedImage(width, height, type);
	}

}
