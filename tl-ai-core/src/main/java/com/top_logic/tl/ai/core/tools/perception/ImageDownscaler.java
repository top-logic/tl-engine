/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.perception;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.top_logic.basic.Logger;

/**
 * Shrinks oversized images before they are placed into a model's context.
 *
 * <p>
 * Vision models downsample anything larger than roughly {@value #DEFAULT_MAX_EDGE} pixels on the
 * longest edge anyway, so sending the original wastes bandwidth and, for base64 payloads, runs into
 * per-request size limits without buying any detail. Images at or below the cap are passed through
 * byte-identical.
 * </p>
 */
public final class ImageDownscaler {

	/**
	 * Longest-edge pixel cap. Matches the resolution above which the common vision models
	 * downsample server-side.
	 */
	public static final int DEFAULT_MAX_EDGE = 1568;

	/**
	 * Largest pixel count this class is willing to <b>decode</b>.
	 *
	 * <p>
	 * A decompression bomb is a tiny file declaring enormous dimensions - a few kilobytes of PNG can
	 * expand to a gigapixel canvas - and {@link ImageIO#read} allocates from the header, so decoding one
	 * raises {@link OutOfMemoryError} rather than an exception a caller could handle. The budget is
	 * expressed in pixels because that, not the file size, is what the allocation follows: an
	 * {@code ARGB} raster costs four bytes per pixel, so 25 megapixels is about 100 MB of transient
	 * heap. That is far above anything this tool legitimately sees (a 4K screenshot is 8 MP, a 300 dpi
	 * A4 scan 9 MP) and far below what kills a server.
	 * </p>
	 *
	 * @see #headerPixelCount(byte[])
	 */
	public static final long MAX_PIXELS = 25_000_000L;

	private ImageDownscaler() {
		// Static utility.
	}

	/**
	 * The outcome of a downscale attempt.
	 *
	 * @param data
	 *        The image bytes to send: the re-encoded ones when {@link #resized()}, otherwise the
	 *        input unchanged.
	 * @param contentType
	 *        The content type of {@link #data()}, which may differ from the input's when the image
	 *        had to be re-encoded.
	 * @param width
	 *        Pixel width of {@link #data()}, or {@code -1} when the image could not be decoded.
	 * @param height
	 *        Pixel height of {@link #data()}, or {@code -1} when the image could not be decoded.
	 * @param resized
	 *        Whether the image was actually re-encoded at a smaller size.
	 */
	public record Scaled(byte[] data, String contentType, int width, int height, boolean resized) {

		/** Whether the pixel dimensions of this image are known. */
		public boolean hasDimensions() {
			return width > 0 && height > 0;
		}
	}

	/**
	 * Downscales the given image to {@link #DEFAULT_MAX_EDGE} on its longest edge.
	 *
	 * @see #downscale(byte[], String, int)
	 */
	public static Scaled downscale(byte[] data, String contentType) {
		return downscale(data, contentType, DEFAULT_MAX_EDGE);
	}

	/**
	 * Downscales the given image so that neither edge exceeds {@code maxEdge}, preserving the
	 * aspect ratio.
	 *
	 * <p>
	 * Never throws: an image this JVM has no {@link ImageIO} reader for ({@code WebP}, AVIF, ...) or a
	 * corrupt payload is returned unchanged, with unknown dimensions. Downstream code then sends
	 * the original bytes, which is the best available answer.
	 * </p>
	 *
	 * @param data
	 *        The encoded image.
	 * @param contentType
	 *        The image's content type, e.g. {@code "image/png"}. May be {@code null}.
	 * @param maxEdge
	 *        Pixel cap for the longest edge; must be positive.
	 * @return The image to send, never {@code null}.
	 */
	public static Scaled downscale(byte[] data, String contentType, int maxEdge) {
		if (maxEdge <= 0) {
			throw new IllegalArgumentException("maxEdge must be positive, got: " + maxEdge);
		}
		BufferedImage source = read(data);
		if (source == null) {
			return new Scaled(data, contentType, -1, -1, false);
		}
		int width = source.getWidth();
		int height = source.getHeight();
		int longest = Math.max(width, height);
		if (longest <= maxEdge) {
			return new Scaled(data, contentType, width, height, false);
		}

		double factor = (double) maxEdge / longest;
		int targetWidth = Math.max(1, (int) Math.round(width * factor));
		int targetHeight = Math.max(1, (int) Math.round(height * factor));

		String outputType = outputContentType(contentType);
		boolean jpeg = "image/jpeg".equals(outputType);
		BufferedImage target = new BufferedImage(targetWidth, targetHeight,
			jpeg ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = target.createGraphics();
		try {
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
			if (jpeg) {
				// JPEG has no alpha channel; without an opaque backdrop transparent pixels
				// would be written as black.
				graphics.setColor(Color.WHITE);
				graphics.fillRect(0, 0, targetWidth, targetHeight);
			}
			graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
		} finally {
			graphics.dispose();
		}

		byte[] encoded = encode(target, jpeg ? "jpeg" : "png");
		if (encoded == null) {
			return new Scaled(data, contentType, width, height, false);
		}
		return new Scaled(encoded, outputType, targetWidth, targetHeight, true);
	}

	/**
	 * The content type the downscaled image is re-encoded as.
	 *
	 * <p>
	 * JPEG stays JPEG (photographs compress far better lossily); everything else becomes PNG,
	 * which every vision model accepts and which {@link ImageIO} can always write.
	 * </p>
	 */
	private static String outputContentType(String contentType) {
		String normalized = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
		return normalized.startsWith("image/jpeg") || normalized.startsWith("image/jpg")
			? "image/jpeg"
			: "image/png";
	}

	/**
	 * The pixel count the image's own <em>header</em> declares, read without decoding a single row.
	 *
	 * <p>
	 * This is the guard against a decompression bomb: the caller can refuse an image whose declared
	 * dimensions are absurd before any allocation follows from them. Reading the header uses the same
	 * {@link ImageIO} readers as the decode, so a format this JVM cannot read answers "unknown" here
	 * exactly as it does there.
	 * </p>
	 *
	 * @param data
	 *        The encoded image; may be {@code null}.
	 * @return The declared {@code width * height}, or {@code -1} when the dimensions cannot be
	 *         determined (no reader for the format, or a corrupt header).
	 */
	public static long headerPixelCount(byte[] data) {
		if (data == null || data.length == 0) {
			return -1L;
		}
		try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
			if (stream == null) {
				return -1L;
			}
			Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
			if (!readers.hasNext()) {
				return -1L;
			}
			ImageReader reader = readers.next();
			try {
				reader.setInput(stream, true, true);
				return (long) reader.getWidth(0) * (long) reader.getHeight(0);
			} finally {
				reader.dispose();
			}
		} catch (IOException | RuntimeException ex) {
			Logger.info("Cannot read the image header: " + ex.getMessage(), ImageDownscaler.class);
			return -1L;
		}
	}

	/**
	 * Whether decoding this image would exceed {@link #MAX_PIXELS}, judged from its header alone.
	 *
	 * @param data
	 *        The encoded image; may be {@code null}.
	 * @return {@code true} only when the header declares more pixels than the budget allows; an image
	 *         whose dimensions cannot be determined is not accused of anything.
	 */
	public static boolean exceedsPixelBudget(byte[] data) {
		return headerPixelCount(data) > MAX_PIXELS;
	}

	private static BufferedImage read(byte[] data) {
		if (data == null || data.length == 0) {
			return null;
		}
		if (exceedsPixelBudget(data)) {
			// Belt and braces: callers refuse such an image outright (with a message the model can act
			// on), but nothing here may allocate a gigapixel raster just because someone forgot to ask.
			Logger.warn("Refusing to decode an image of " + headerPixelCount(data) + " pixels (budget "
				+ MAX_PIXELS + "), sending it unchanged.", ImageDownscaler.class);
			return null;
		}
		try {
			return ImageIO.read(new ByteArrayInputStream(data));
		} catch (IOException | RuntimeException ex) {
			Logger.info("Cannot decode image for downscaling, sending it unchanged: " + ex.getMessage(),
				ImageDownscaler.class);
			return null;
		} catch (OutOfMemoryError ex) {
			// The header check above bounds what is allocated here, so this is the residual case of a
			// header that does not describe what the decoder ends up allocating. Sending the image
			// unchanged is a worse answer than downscaling it, and a far better one than dying.
			Logger.warn("Ran out of memory decoding an image of " + data.length
				+ " bytes, sending it unchanged.", ImageDownscaler.class);
			return null;
		}
	}

	private static byte[] encode(BufferedImage image, String formatName) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			if (!ImageIO.write(image, formatName, buffer)) {
				Logger.info("No ImageIO writer for format '" + formatName
					+ "', sending the image unchanged.", ImageDownscaler.class);
				return null;
			}
		} catch (IOException | RuntimeException ex) {
			Logger.info("Cannot re-encode downscaled image, sending it unchanged: " + ex.getMessage(),
				ImageDownscaler.class);
			return null;
		}
		return buffer.toByteArray();
	}
}
