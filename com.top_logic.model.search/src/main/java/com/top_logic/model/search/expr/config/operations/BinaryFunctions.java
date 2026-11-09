/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.image.util.ImageUtils;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.util.error.TopLogicException;

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
	 * Name of the content type parameter that names the character encoding of a text value, see
	 * {@link #text(BinaryDataSource, String)}.
	 */
	public static final String CHARSET_PARAMETER = "charset";

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
	public static Double size(@Mandatory BinaryDataSource data) {
		return data == null ? null : SearchExpression.toNumber(data.getSize());
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

	/**
	 * The contents of the given binary value as text.
	 *
	 * <p>
	 * The bytes of the value are decoded in the given character encoding. Without an explicit
	 * encoding, the parameter {@link #CHARSET_PARAMETER} of the value's content type is used, for
	 * example <code>ISO-8859-1</code> for a value of type
	 * <code>text/plain; charset=ISO-8859-1</code>. If the content type names no encoding either,
	 * the contents are decoded as <code>UTF-8</code>.
	 * </p>
	 *
	 * <p>
	 * The text is the decoded contents as they are: no escape sequences are interpreted, so a
	 * backslash followed by <code>n</code> in the contents stays a backslash followed by
	 * <code>n</code>.
	 * </p>
	 *
	 * <p>
	 * Example: <code>webResource("/images/logo.svg").binaryText()</code>
	 * </p>
	 *
	 * @param data
	 *        The binary value to read.
	 * @param charset
	 *        The name of the character encoding of the contents, for example <code>UTF-8</code>
	 *        or <code>ISO-8859-1</code>. If not given, the encoding is taken from the content type
	 *        of the value.
	 * @return The contents of the value as text, or <code>null</code> if no value is given.
	 */
	@Label("Contents of a binary value as text")
	@SideEffectFree
	public static String text(@Mandatory BinaryDataSource data, String charset) {
		if (data == null) {
			return null;
		}
		Charset encoding = toCharset(StringServices.isEmpty(charset) ? contentTypeCharset(data) : charset);
		try {
			return StreamUtilities.readAllFromStream(data, encoding.name());
		} catch (IOException ex) {
			throw new TopLogicException(I18NConstants.ERROR_READING_BINARY_FAILED__NAME_MSG.fill(data.getName(),
				ex.getMessage()), ex);
		}
	}

	private static String contentTypeCharset(BinaryDataSource data) {
		String contentType = data.getContentType();
		if (StringServices.isEmpty(contentType)) {
			return null;
		}
		try {
			return new MimeType(contentType).getParameter(CHARSET_PARAMETER);
		} catch (MimeTypeParseException ex) {
			Logger.debug("Content type without readable character encoding: " + contentType, ex,
				BinaryFunctions.class);
			return null;
		}
	}

	private static Charset toCharset(String charset) {
		if (StringServices.isEmpty(charset)) {
			return StandardCharsets.UTF_8;
		}
		try {
			return Charset.forName(charset);
		} catch (IllegalCharsetNameException | UnsupportedCharsetException ex) {
			throw new TopLogicException(I18NConstants.ERROR_UNKNOWN_CHARSET__NAME.fill(charset), ex);
		}
	}

}
