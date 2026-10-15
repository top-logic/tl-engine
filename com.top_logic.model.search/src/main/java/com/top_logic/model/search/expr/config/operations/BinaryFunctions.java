/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.awt.Dimension;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.image.util.ImageUtils;

/**
 * TL-Script functions describing a binary value.
 *
 * <p>
 * A binary value is the contents of an uploaded file, a document attribute, or the result of a
 * function such as <code>binary()</code>. Its name is available through <code>label()</code>, while
 * the functions here give access to the other properties of the value.
 * </p>
 */
@ScriptPrefix("binary")
public class BinaryFunctions extends TLScriptFunctions {

	/**
	 * Key of the image width in pixels in the result of {@link #imageSize(BinaryDataSource)}.
	 */
	public static final String IMAGE_WIDTH = "width";

	/**
	 * Key of the image height in pixels in the result of {@link #imageSize(BinaryDataSource)}.
	 */
	public static final String IMAGE_HEIGHT = "height";

	/**
	 * The content type of the given binary value.
	 *
	 * <p>
	 * The content type is the MIME type of the contents, such as <code>image/png</code> or
	 * <code>application/pdf</code>. It may carry additional parameters, such as
	 * <code>text/plain; charset=utf-8</code>, so a check for a certain kind of contents is best
	 * written as <code>$data.binaryContentType().stringStartsWith("image/")</code>.
	 * </p>
	 *
	 * @param data
	 *        The binary value to inspect.
	 * @return The content type of the value, or <code>null</code> if no value is given.
	 */
	@Label("Content type of a binary value")
	@SideEffectFree
	public static String contentType(@Mandatory BinaryDataSource data) {
		return data == null ? null : data.getContentType();
	}

	/**
	 * The size of the given binary value in bytes.
	 *
	 * <p>
	 * Not every binary value knows its size in advance. A value whose contents are produced on
	 * demand reports <code>-1</code>.
	 * </p>
	 *
	 * @param data
	 *        The binary value to inspect.
	 * @return The number of bytes of the value, <code>-1</code> if the size is not known, or
	 *         <code>null</code> if no value is given.
	 */
	@Label("Size of a binary value")
	@SideEffectFree
	public static Long size(@Mandatory BinaryDataSource data) {
		return data == null ? null : Long.valueOf(data.getSize());
	}

	/**
	 * The size of the image in the given binary value in pixels.
	 *
	 * <p>
	 * The size is read from the image header, the image itself is not decoded. Supported are the
	 * common bitmap formats such as PNG, JPEG, GIF and BMP. The result is a dictionary with the
	 * width of the image in pixels under the key {@link #IMAGE_WIDTH} and its height under the key
	 * {@link #IMAGE_HEIGHT}.
	 * </p>
	 *
	 * @param data
	 *        The binary value containing an encoded image.
	 * @return A dictionary with the width and height of the image in pixels, or <code>null</code> if
	 *         no value is given or the value is not a readable image.
	 */
	@Label("Size of an image")
	@SideEffectFree
	public static Map<String, Object> imageSize(@Mandatory BinaryDataSource data) {
		if (data == null) {
			return null;
		}
		Dimension size;
		try {
			size = ImageUtils.getImageSize(data.toData());
		} catch (IOException ex) {
			Logger.debug("Not a readable image: " + data.getName(), ex, BinaryFunctions.class);
			return null;
		}
		if (size == null) {
			return null;
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(IMAGE_WIDTH, Double.valueOf(size.width));
		result.put(IMAGE_HEIGHT, Double.valueOf(size.height));
		return result;
	}

}
