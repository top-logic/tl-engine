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
import java.util.zip.CRC32;

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

	/**
	 * <b>The decompression bomb.</b> A PNG header declaring a gigapixel canvas is refused from its
	 * header, before anything allocates a raster from those numbers.
	 *
	 * <p>
	 * The payload is a header alone on purpose: this fixture must not be an image the JVM could
	 * actually decode, because decoding it is precisely what the guard prevents (and what raises the
	 * {@link OutOfMemoryError} the surrounding {@code catch} clauses were never going to catch). What is
	 * asserted is therefore the verdict taken from the declared dimensions - which is what a real bomb,
	 * whose pixel data <em>is</em> present and compresses to a few kilobytes, is judged by too.
	 * </p>
	 */
	public void testRefusesADecompressionBombFromItsHeader() {
		byte[] bomb = pngHeaderDeclaring(60_000, 60_000);

		assertEquals("the header is read without decoding", 3_600_000_000L,
			ImageDownscaler.headerPixelCount(bomb));
		assertTrue("above the pixel budget", ImageDownscaler.exceedsPixelBudget(bomb));

		ImageDownscaler.Scaled scaled = ImageDownscaler.downscale(bomb, "image/png");

		assertFalse("nothing was decoded, so no dimensions are reported", scaled.hasDimensions());
		assertFalse(scaled.resized());
		assertSame(bomb, scaled.data());
	}

	/** An ordinary image is under the budget and is decoded as before. */
	public void testOrdinaryImageIsWithinThePixelBudget() throws IOException {
		assertFalse(ImageDownscaler.exceedsPixelBudget(png(800, 600)));
		assertEquals(480_000L, ImageDownscaler.headerPixelCount(png(800, 600)));
	}

	/** A payload with no readable header is not accused of exceeding anything. */
	public void testUnknownDimensionsAreNotTreatedAsABomb() {
		byte[] data = "not an image at all".getBytes(StandardCharsets.UTF_8);

		assertEquals(-1L, ImageDownscaler.headerPixelCount(data));
		assertFalse(ImageDownscaler.exceedsPixelBudget(data));
		assertEquals(-1L, ImageDownscaler.headerPixelCount(null));
		assertFalse(ImageDownscaler.exceedsPixelBudget(new byte[0]));
	}

	/**
	 * A PNG signature plus a valid {@code IHDR} chunk declaring the given dimensions, and nothing else.
	 * Enough for any {@code ImageIO} reader to answer the image's size, which is all a header check
	 * needs - and all a bomb needs to make a decoder allocate.
	 */
	static byte[] pngHeaderDeclaring(int width, int height) {
		byte[] header = new byte[] { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A };
		byte[] ihdr = new byte[] { 'I', 'H', 'D', 'R',
			(byte) (width >>> 24), (byte) (width >>> 16), (byte) (width >>> 8), (byte) width,
			(byte) (height >>> 24), (byte) (height >>> 16), (byte) (height >>> 8), (byte) height,
			8, // bit depth
			6, // colour type: RGBA
			0, 0, 0 // compression, filter, interlace
		};
		CRC32 crc = new CRC32();
		crc.update(ihdr);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(header, 0, header.length);
		writeInt(out, ihdr.length - 4);
		out.write(ihdr, 0, ihdr.length);
		writeInt(out, (int) crc.getValue());
		return out.toByteArray();
	}

	private static void writeInt(ByteArrayOutputStream out, int value) {
		out.write(value >>> 24);
		out.write(value >>> 16);
		out.write(value >>> 8);
		out.write(value);
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
