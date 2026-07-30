/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.perception;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

/**
 * Tests for {@link ImageDownscaler}.
 */
public class TestImageDownscaler extends TestCase {

	/** An image above the cap is shrunk to the cap, keeping its aspect ratio. */
	public void testShrinksOversizedImage() throws IOException {
		byte[] png = png(3000, 2000);

		ImageDownscaler.Scaled scaled = ImageDownscaler.downscale(png, "image/png");

		assertTrue("Oversized image must be resized.", scaled.resized());
		assertEquals(ImageDownscaler.DEFAULT_MAX_EDGE, scaled.width());
		assertEquals(1045, scaled.height());
		assertEquals("image/png", scaled.contentType());
		assertTrue("Downscaled image must be smaller.", scaled.data().length < png.length);

		BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(scaled.data()));
		assertEquals(ImageDownscaler.DEFAULT_MAX_EDGE, decoded.getWidth());
		assertEquals(1045, decoded.getHeight());
	}

	/** A portrait image is capped on its long edge, which is the height. */
	public void testCapsTheLongEdgeOfAPortraitImage() throws IOException {
		ImageDownscaler.Scaled scaled = ImageDownscaler.downscale(png(1000, 4000), "image/png");

		assertTrue(scaled.resized());
		assertEquals(ImageDownscaler.DEFAULT_MAX_EDGE, scaled.height());
		assertEquals(392, scaled.width());
	}

	/** An image at or below the cap is passed through byte-identical. */
	public void testKeepsSmallImageUntouched() throws IOException {
		byte[] png = png(800, 600);

		ImageDownscaler.Scaled scaled = ImageDownscaler.downscale(png, "image/png");

		assertFalse("A small image must not be re-encoded.", scaled.resized());
		assertSame(png, scaled.data());
		assertEquals(800, scaled.width());
		assertEquals(600, scaled.height());
	}

	/** JPEG input stays JPEG so photographs are not blown up into lossless PNGs. */
	public void testKeepsJpegEncoding() throws IOException {
		ImageDownscaler.Scaled scaled = ImageDownscaler.downscale(jpeg(2400, 1200), "image/jpeg");

		assertTrue(scaled.resized());
		assertEquals("image/jpeg", scaled.contentType());
		assertNotNull(ImageIO.read(new ByteArrayInputStream(scaled.data())));
	}

	/** A payload no ImageIO reader understands is passed through instead of failing. */
	public void testPassesUndecodableDataThrough() {
		byte[] data = "not an image at all".getBytes(StandardCharsets.UTF_8);

		ImageDownscaler.Scaled scaled = ImageDownscaler.downscale(data, "image/webp");

		assertFalse(scaled.resized());
		assertSame(data, scaled.data());
		assertFalse("Dimensions of an undecodable image are unknown.", scaled.hasDimensions());
	}

	/** A custom cap is honoured. */
	public void testHonoursExplicitCap() throws IOException {
		ImageDownscaler.Scaled scaled = ImageDownscaler.downscale(png(1000, 500), "image/png", 200);

		assertTrue(scaled.resized());
		assertEquals(200, scaled.width());
		assertEquals(100, scaled.height());
	}

	/** */
	public void testRejectsNonPositiveCap() {
		try {
			ImageDownscaler.downscale(new byte[0], "image/png", 0);
			fail("Expected IllegalArgumentException.");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage().contains("maxEdge"));
		}
	}

	static byte[] png(int width, int height) throws IOException {
		return encode(image(width, height, BufferedImage.TYPE_INT_ARGB), "png");
	}

	private static byte[] jpeg(int width, int height) throws IOException {
		return encode(image(width, height, BufferedImage.TYPE_INT_RGB), "jpeg");
	}

	private static BufferedImage image(int width, int height, int type) {
		BufferedImage image = new BufferedImage(width, height, type);
		Graphics2D graphics = image.createGraphics();
		try {
			// A gradient rather than a flat fill, so PNG compression cannot collapse the
			// payload to a size that makes the "got smaller" assertions meaningless.
			for (int x = 0; x < width; x += 4) {
				graphics.setColor(new Color(x % 256, (x / 2) % 256, (x / 3) % 256));
				graphics.fillRect(x, 0, 4, height);
			}
		} finally {
			graphics.dispose();
		}
		return image;
	}

	private static byte[] encode(BufferedImage image, String format) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		assertTrue("No ImageIO writer for " + format, ImageIO.write(image, format, buffer));
		return buffer.toByteArray();
	}
}
