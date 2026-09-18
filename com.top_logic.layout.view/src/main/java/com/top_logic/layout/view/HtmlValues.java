/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Conversion of a value into the HTML source that displays it.
 *
 * <p>
 * The values a model can hold HTML in: the source itself as text, a fragment that writes itself
 * when it is displayed, and a document of content type {@code text/html} delivered as binary data.
 * Any other value has no HTML representation and is reported as such.
 * </p>
 *
 * <p>
 * The result is the source as the value carries it. It is not checked: whether it may be shown is
 * decided by the caller, which knows where the source is about to end up.
 * </p>
 */
public class HtmlValues {

	/** The content type of an HTML document. */
	public static final String HTML_CONTENT_TYPE = "text/html";

	/** Name of the content type parameter naming the encoding of a document. */
	private static final String CHARSET_PARAMETER = "charset";

	/** The encoding an HTML document is read with when it does not declare one. */
	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	private HtmlValues() {
		// Only static members.
	}

	/**
	 * The HTML source displaying the given value.
	 *
	 * @param value
	 *        The value to display, or {@code null}.
	 * @return The HTML source, the empty string for {@code null}.
	 * @throws TopLogicException
	 *         If the value has no HTML representation, or its representation cannot be read.
	 */
	public static String toHtml(Object value) {
		if (value == null) {
			return "";
		}
		if (value instanceof CharSequence source) {
			return source.toString();
		}
		if (value instanceof HTMLFragment fragment) {
			return render(fragment);
		}
		if (value instanceof BinaryDataSource data) {
			return read(data);
		}
		throw new TopLogicException(
			I18NConstants.ERROR_HTML_UNSUPPORTED_VALUE__TYPE.fill(value.getClass().getName()));
	}

	/**
	 * The HTML source the given fragment writes.
	 */
	private static String render(HTMLFragment fragment) {
		StringWriter buffer = new StringWriter();
		try (TagWriter out = new TagWriter(buffer)) {
			fragment.write(displayContext(), out);
		} catch (IOException ex) {
			throw new TopLogicException(I18NConstants.ERROR_HTML_NOT_RENDERABLE, ex);
		}
		return buffer.toString();
	}

	/**
	 * The context a fragment is written in: the one of the running interaction, where there is one,
	 * and a context of its own otherwise.
	 */
	private static DisplayContext displayContext() {
		if (DefaultDisplayContext.hasDisplayContext()) {
			return DefaultDisplayContext.getDisplayContext();
		}
		return new DummyDisplayContext();
	}

	/**
	 * The HTML source of the given document.
	 */
	private static String read(BinaryDataSource data) {
		MimeType contentType = contentType(data);
		if (!HTML_CONTENT_TYPE.equalsIgnoreCase(contentType.getBaseType())) {
			throw new TopLogicException(
				I18NConstants.ERROR_HTML_UNSUPPORTED_CONTENT_TYPE__NAME_TYPE.fill(data.getName(),
					contentType.getBaseType()));
		}

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			data.deliverTo(buffer);
		} catch (IOException ex) {
			throw new TopLogicException(I18NConstants.ERROR_HTML_NOT_READABLE__NAME.fill(data.getName()), ex);
		}
		return buffer.toString(charset(contentType));
	}

	/**
	 * The content type of the given document.
	 */
	private static MimeType contentType(BinaryDataSource data) {
		String contentType = StringServices.nonNull(data.getContentType());
		try {
			return new MimeType(contentType);
		} catch (MimeTypeParseException ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_HTML_UNSUPPORTED_CONTENT_TYPE__NAME_TYPE.fill(data.getName(), contentType), ex);
		}
	}

	/**
	 * The encoding the given content type declares, {@link #DEFAULT_CHARSET} when it declares none
	 * this machine knows.
	 */
	private static Charset charset(MimeType contentType) {
		String charset = contentType.getParameter(CHARSET_PARAMETER);
		if (charset == null || charset.isEmpty()) {
			return DEFAULT_CHARSET;
		}
		try {
			return Charset.forName(charset);
		} catch (IllegalCharsetNameException | UnsupportedCharsetException ex) {
			return DEFAULT_CHARSET;
		}
	}

}
