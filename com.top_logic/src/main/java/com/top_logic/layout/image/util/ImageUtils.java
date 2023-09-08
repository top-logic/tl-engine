/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
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
		switch(imageType) {
			case JPG: {
				return createJpgImage(image, imageType);
			}
			case PNG: {
				return createPngImage(image, imageType);
			}
		}

		throw new TopLogicException(I18NConstants.ERROR_UNSUPPORTED_IMAGE__TYPE_SUPPORTED.fill(imageType,
			Arrays.asList(ImageType.JPG, ImageType.PNG)));
	}

	/**
	 * Scales an image in that way, that it fits into given width and height, by keeping the image's
	 * aspect ratio.
	 * 
	 * @return scaled image
	 */
	public static BufferedImage scaleToBoundary(BufferedImage inputImage, int maxWidth, int maxHeight) {
		// scale w, h to keep aspect constant
		double outputAspect = 1.0 * maxWidth / maxHeight;
		double inputAspect = 1.0 * inputImage.getWidth() / inputImage.getHeight();
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

		BufferedImage outputImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = outputImage.createGraphics();
		g2.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(
			RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(inputImage, 0, 0, imageWidth, imageHeight, null);
		g2.dispose();
		return outputImage;
	}

	/**
	 * Scales an image by a given scaling factor. Scaling factor of <code>1</code> means no scaling.
	 * 
	 * @return scaled image
	 */
	public static BufferedImage scaleByFactor(BufferedImage inputImage, double zoomFactor) {
		int width = (int) Math.round(zoomFactor * inputImage.getWidth());
		int height = (int) Math.round(zoomFactor * inputImage.getHeight());
		return scaleToBoundary(inputImage, width, height);
	}

	private static InMemoryBinaryData getOutputStream(ImageType imageType) {
		return new InMemoryBinaryData(imageType.getMimeType());
	}

	private static BinaryData createJpgImage(BufferedImage image, ImageType imageType) {
		ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName(imageType.getFormatName()).next();
		ImageWriteParam jpgParameters = jpgWriter.getDefaultWriteParam();
		jpgParameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		jpgParameters.setCompressionQuality(1f);

		InMemoryBinaryData outputData = getOutputStream(imageType);
		IIOImage renderImage = new IIOImage(image, null, null);
		jpgWriter.setOutput(new MemoryCacheImageOutputStream(outputData));
		try {
			jpgWriter.write(null, renderImage, jpgParameters);
		} catch (IOException ex) {
			throw wrap(ex);
		}
		jpgWriter.dispose();
		return outputData;
	}

	private static BinaryData createPngImage(BufferedImage image, ImageType imageType) {
		InMemoryBinaryData outputData = getOutputStream(imageType);
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
