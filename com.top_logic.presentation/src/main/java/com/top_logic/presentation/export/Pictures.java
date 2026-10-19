/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.export;

import java.awt.geom.Dimension2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.poi.xslf.draw.SVGImageRenderer;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.util.Dimension2DDouble;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.util.error.TopLogicException;

/**
 * Adds images of a presentation description to a presentation.
 */
final class Pictures {

	/**
	 * The content type of SVG images.
	 */
	static final String SVG_CONTENT_TYPE = "image/svg+xml";

	private static final String SVG_EXTENSION = ".svg";

	private static final String SVG_ELEMENT = "<svg";

	/**
	 * The number of bytes at the start of a file in which an SVG element is searched.
	 */
	private static final int SVG_PROBE_LENGTH = 1024;

	/**
	 * The picture types by the format names of the {@link ImageIO} readers.
	 */
	private static final Map<String, PictureType> RASTER_TYPES = Map.of(
		"png", PictureType.PNG,
		"jpeg", PictureType.JPEG,
		"jpg", PictureType.JPEG,
		"gif", PictureType.GIF,
		"bmp", PictureType.BMP,
		"tif", PictureType.TIFF,
		"tiff", PictureType.TIFF);

	private Pictures() {
		// Static utility.
	}

	/**
	 * An image added to a presentation.
	 *
	 * @param data
	 *        The image part of the presentation.
	 * @param size
	 *        The size of the image in pixels, which determines its aspect ratio.
	 */
	record Picture(XSLFPictureData data, Dimension2D size) {

		/**
		 * Whether the image is a vector graphics in SVG format.
		 */
		boolean isSvg() {
			return data.getType() == PictureType.SVG;
		}

	}

	/**
	 * Adds the given image to the presentation.
	 *
	 * <p>
	 * An image already contained in the presentation is reused.
	 * </p>
	 *
	 * @param slideShow
	 *        The presentation.
	 * @param image
	 *        A raster image readable by {@link ImageIO} (PNG, JPEG, GIF, BMP, TIFF) or an SVG image.
	 * @param path
	 *        The path of the image in the description, for error messages.
	 */
	static Picture add(XMLSlideShow slideShow, BinaryDataSource image, String path) {
		byte[] bytes = bytes(image);
		if (isSvg(image, bytes)) {
			Dimension2D size = svgSize(bytes);
			if (size == null) {
				throw unsupported(image, path);
			}
			return new Picture(slideShow.addPicture(bytes, PictureType.SVG), size);
		}
		try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
			Iterator<ImageReader> readers = in == null ? null : ImageIO.getImageReaders(in);
			if (readers == null || !readers.hasNext()) {
				throw unsupported(image, path);
			}
			ImageReader reader = readers.next();
			try {
				PictureType type = RASTER_TYPES.get(reader.getFormatName().toLowerCase(Locale.ROOT));
				if (type == null) {
					throw unsupported(image, path);
				}
				reader.setInput(in, true, true);
				Dimension2D size = new Dimension2DDouble(reader.getWidth(0), reader.getHeight(0));
				return new Picture(slideShow.addPicture(bytes, type), size);
			} finally {
				reader.dispose();
			}
		} catch (IOException ex) {
			throw new TopLogicException(I18NConstants.ERROR_UNSUPPORTED_IMAGE__PATH_NAME.fill(path, image.getName()),
				ex);
		}
	}

	private static byte[] bytes(BinaryDataSource image) {
		try (InputStream in = image.toData().getStream()) {
			return in.readAllBytes();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static boolean isSvg(BinaryDataSource image, byte[] bytes) {
		if (SVG_CONTENT_TYPE.equals(image.getContentType())) {
			return true;
		}
		String name = image.getName();
		if (name != null && name.toLowerCase(Locale.ROOT).endsWith(SVG_EXTENSION)) {
			return true;
		}
		String start = new String(bytes, 0, Math.min(bytes.length, SVG_PROBE_LENGTH), StandardCharsets.UTF_8);
		return start.stripLeading().startsWith("<") && start.contains(SVG_ELEMENT);
	}

	private static Dimension2D svgSize(byte[] bytes) {
		SVGImageRenderer renderer = new SVGImageRenderer();
		try {
			renderer.loadImage(bytes, SVG_CONTENT_TYPE);
		} catch (IOException | RuntimeException ex) {
			return null;
		}
		Dimension2D size = renderer.getDimension();
		return size.getWidth() > 0 && size.getHeight() > 0 ? size : null;
	}

	private static TopLogicException unsupported(BinaryDataSource image, String path) {
		return new TopLogicException(I18NConstants.ERROR_UNSUPPORTED_IMAGE__PATH_NAME.fill(path, image.getName()));
	}

}
