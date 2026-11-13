/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.InMemoryBinaryData;
import com.top_logic.util.error.TopLogicException;

/**
 * Utility for image operations.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ImageUtils {

	/**
	 * @see #createImage(InputStream)
	 */
	public static BufferedImage createImage(BinaryContent imageData) {
		try {
			return ImageIO.read(imageData.getStream());
		} catch (IOException ex) {
			throw wrap(ex);
		}
	}

	/**
	 * Create manipulable image from raw data. This method usually will be called first in an image
	 * processing workflow:
	 * 
	 * <ol>
	 * <li>{@link #createImage(InputStream)}</li>
	 * <li>Image processing (e.g. scaling)</li>
	 * <li>{@link #getImageData(BufferedImage, ImageType)}</li>
	 * </ol>
	 * 
	 * @throws IOException
	 *         if data of {@link InputStream} cannot be retrieved.
	 */
	public static BufferedImage createImage(InputStream inputStream) throws IOException {
		return ImageIO.read(inputStream);
	}

	/**
	 * The size of the image in the given data in pixels.
	 *
	 * <p>
	 * Only the image header is read, the pixel data is not decoded.
	 * </p>
	 *
	 * @param imageData
	 *        The encoded image, e.g. a PNG or JPEG file.
	 * @return The width and height of the (first) image in pixels, or <code>null</code> if the data
	 *         is not in an image format readable by {@link ImageIO}.
	 * @throws IOException
	 *         If reading the data fails.
	 */
	public static Dimension getImageSize(BinaryContent imageData) throws IOException {
		try (InputStream in = imageData.getStream();
				ImageInputStream imageIn = ImageIO.createImageInputStream(in)) {
			return getImageSize(imageIn);
		}
	}

	/**
	 * The MIME type of the image format the given data is encoded in, judged by its content.
	 *
	 * <p>
	 * Only the header of the data is read, so the result does not depend on a file name or on a
	 * content type a client claims.
	 * </p>
	 *
	 * @param imageData
	 *        The data to inspect.
	 * @return The MIME type of the format, e.g. <code>image/png</code>, or <code>null</code> if the
	 *         data is not in an image format readable by {@link ImageIO}.
	 * @throws IOException
	 *         If reading the data fails.
	 */
	public static String getImageMimeType(BinaryContent imageData) throws IOException {
		try (InputStream in = imageData.getStream();
				ImageInputStream imageIn = ImageIO.createImageInputStream(in)) {
			if (imageIn == null) {
				return null;
			}
			Iterator<ImageReader> readers = ImageIO.getImageReaders(imageIn);
			if (!readers.hasNext()) {
				return null;
			}
			ImageReader reader = readers.next();
			try {
				String[] mimeTypes = reader.getOriginatingProvider().getMIMETypes();
				return mimeTypes == null || mimeTypes.length == 0 ? null : mimeTypes[0];
			} finally {
				reader.dispose();
			}
		}
	}

	/**
	 * The size of the image read from the given stream in pixels.
	 *
	 * <p>
	 * Only the image header is read, the pixel data is not decoded. The given stream is not closed.
	 * </p>
	 *
	 * @param imageIn
	 *        The stream to read the image from, may be <code>null</code> if
	 *        {@link ImageIO#createImageInputStream(Object)} found no stream implementation for the
	 *        input.
	 * @return The width and height of the (first) image in pixels, or <code>null</code> if the data
	 *         is not in an image format readable by {@link ImageIO}.
	 * @throws IOException
	 *         If reading the data fails.
	 */
	public static Dimension getImageSize(ImageInputStream imageIn) throws IOException {
		if (imageIn == null) {
			return null;
		}
		Iterator<ImageReader> readers = ImageIO.getImageReaders(imageIn);
		if (!readers.hasNext()) {
			return null;
		}
		ImageReader reader = readers.next();
		try {
			reader.setInput(imageIn, true, true);
			return new Dimension(reader.getWidth(0), reader.getHeight(0));
		} finally {
			reader.dispose();
		}
	}

	/**
	 * Get image data, encoded in specified {@link ImageType}, from transient manipulable image.
	 * This method usually will be called last in an image processing workflow:
	 * 
	 * <ol>
	 * <li>{@link #createImage(InputStream)}</li>
	 * <li>Image processing (e.g. scaling)</li>
	 * <li>{@link #getImageData(BufferedImage, ImageType)}</li>
	 * </ol>
	 */
	public static BinaryData getImageData(BufferedImage image, ImageType imageType) {
		return getImageData(image, imageType, BinaryData.NO_NAME);
	}

	/**
	 * Get image data with the given name, encoded in specified {@link ImageType}, from transient
	 * manipulable image.
	 * 
	 * @see #getImageData(BufferedImage, ImageType)
	 */
	public static BinaryData getImageData(BufferedImage image, ImageType imageType, String name) {
		switch(imageType) {
			case JPG: {
				return createJpgImage(image, imageType, name);
			}
			case PNG: {
				return createPngImage(image, imageType, name);
			}
		}

		throw new TopLogicException(I18NConstants.ERROR_UNSUPPORTED_IMAGE__TYPE_SUPPORTED.fill(imageType,
			Arrays.asList(ImageType.JPG, ImageType.PNG)));
	}

	/**
	 * Scales an image in that way, that it fits into given width and height, by keeping the image's
	 * aspect ratio.
	 * 
	 * <p>
	 * An image that already fits into the given boundary is returned unchanged, it is never
	 * enlarged. The transparency of an image with an alpha channel is kept.
	 * </p>
	 * 
	 * @return The scaled image, or the given image, if it already fits.
	 */
	public static BufferedImage scaleToBoundary(BufferedImage inputImage, int maxWidth, int maxHeight) {
		int inputWidth = inputImage.getWidth();
		int inputHeight = inputImage.getHeight();
		if (inputWidth <= maxWidth && inputHeight <= maxHeight) {
			return inputImage;
		}

		// scale w, h to keep aspect constant
		double outputAspect = 1.0 * maxWidth / maxHeight;
		double inputAspect = 1.0 * inputWidth / inputHeight;
		int imageWidth, imageHeight;
		if (outputAspect < inputAspect) {
			// width is limiting factor; adjust height to keep aspect
			imageWidth = maxWidth;
			imageHeight = (int) Math.round(maxWidth / inputAspect);
		} else {
			// height is limiting factor; adjust width to keep aspect
			imageWidth = (int) Math.round(maxHeight * inputAspect);
			imageHeight = maxHeight;
		}

		return scale(inputImage, Math.max(1, imageWidth), Math.max(1, imageHeight));
	}

	/**
	 * Scales an image by a given scaling factor. Scaling factor of <code>1</code> means no scaling.
	 * 
	 * @return scaled image
	 */
	public static BufferedImage scaleByFactor(BufferedImage inputImage, double zoomFactor) {
		int width = (int) Math.round(zoomFactor * inputImage.getWidth());
		int height = (int) Math.round(zoomFactor * inputImage.getHeight());
		return scale(inputImage, Math.max(1, width), Math.max(1, height));
	}

	/**
	 * Scales the given image to exactly the given size.
	 * 
	 * <p>
	 * When shrinking by more than half, the image is first halved repeatedly, before it is scaled
	 * bilinearly to the target size. A single bilinear step only looks at the four nearest source
	 * pixels and would produce a jagged result when shrinking e.g. a photo to an icon.
	 * </p>
	 */
	private static BufferedImage scale(BufferedImage inputImage, int width, int height) {
		int imageType = inputImage.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB
			: BufferedImage.TYPE_INT_RGB;

		BufferedImage current = inputImage;
		int currentWidth = inputImage.getWidth();
		int currentHeight = inputImage.getHeight();
		while (currentWidth / 2 >= width && currentHeight / 2 >= height) {
			currentWidth = currentWidth / 2;
			currentHeight = currentHeight / 2;
			current = draw(current, currentWidth, currentHeight, imageType);
		}
		return draw(current, width, height, imageType);
	}

	private static BufferedImage draw(BufferedImage inputImage, int width, int height, int imageType) {
		BufferedImage outputImage = new BufferedImage(width, height, imageType);
		Graphics2D g2 = outputImage.createGraphics();
		g2.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(
			RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(
			RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.drawImage(inputImage, 0, 0, width, height, null);
		g2.dispose();
		return outputImage;
	}

	/**
	 * Copies the given image to an image without alpha channel, filling transparent areas with
	 * white.
	 */
	private static BufferedImage withoutAlpha(BufferedImage image) {
		if (!image.getColorModel().hasAlpha()) {
			return image;
		}
		BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = result.createGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, image.getWidth(), image.getHeight());
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
		return result;
	}

	private static InMemoryBinaryData getOutputStream(ImageType imageType, String name) {
		return new InMemoryBinaryData(imageType.getMimeType(), name);
	}

	private static BinaryData createJpgImage(BufferedImage image, ImageType imageType, String name) {
		ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName(imageType.getFormatName()).next();
		ImageWriteParam jpgParameters = jpgWriter.getDefaultWriteParam();
		jpgParameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		jpgParameters.setCompressionQuality(1f);

		InMemoryBinaryData outputData = getOutputStream(imageType, name);
		// JPEG has no alpha channel.
		IIOImage renderImage = new IIOImage(withoutAlpha(image), null, null);
		jpgWriter.setOutput(new MemoryCacheImageOutputStream(outputData));
		try {
			jpgWriter.write(null, renderImage, jpgParameters);
		} catch (IOException ex) {
			throw wrap(ex);
		}
		jpgWriter.dispose();
		return outputData;
	}

	private static BinaryData createPngImage(BufferedImage image, ImageType imageType, String name) {
		InMemoryBinaryData outputData = getOutputStream(imageType, name);
		try {
			ImageIO.write(image, imageType.getFormatName(), outputData);
		} catch (IOException ex) {
			throw wrap(ex);
		}
		return outputData;
	}

	private static TopLogicException wrap(IOException ex) {
		return new TopLogicException(I18NConstants.ERROR_CREATING_IMAGE_DATA__DETAILS.fill(ex.getMessage()), ex);
	}

}
