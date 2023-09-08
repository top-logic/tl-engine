/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.xml;

import java.nio.charset.Charset;

import com.top_logic.basic.StringServices;

/**
 * Stores the settings for a template expansion.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TemplateSettings {

	/**
	 * The format of the generated file. Either {@link #TEXT} or {@link #XML}.
	 */
	public static enum OutputFormat {

		/** Output is plain text. */
		TEXT,
		/**
		 * Output is <a href="https://en.wikipedia.org/wiki/XML">XML (Extensible Markup
		 * Language)</a>.
		 */
		XML;

		/**
		 * Parse the {@link OutputFormat} from the given {@link String}.
		 * 
		 * @throws IllegalArgumentException
		 *         If the String does not represent an {@link OutputFormat}.
		 */
		public static OutputFormat parse(String outputFormat) {
			if (StringServices.equals(outputFormat, "text")) {
				return TEXT;
			}
			if (StringServices.equals(outputFormat, "xml")) {
				return XML;
			}
			throw new IllegalArgumentException("This is not a valid output format: '" + outputFormat + "'");
		}
	}

	/** The default value for {@link #getOutputEncoding()}. */
	public static final Charset DEFAULT_OUTPUT_ENCODING = StringServices.CHARSET_UTF_8;

	/** The default value for {@link #isOutputByteOrderMark}. */
	public static final boolean DEFAULT_OUTPUT_BYTE_ORDER_MARK = true;

	/** The default value for {@link #isIgnoreWhitespaces()}. */
	public static final boolean DEFAULT_IGNORE_WHITESPACES = true;

	private final Charset _outputEncoding;

	private final boolean _outputByteOrderMark;

	private final OutputFormat _outputFormat;

	private final boolean _outputXmlHeader;

	private final boolean _ignoreWhitespaces;

	/**
	 * Creates a new {@link TemplateSettings}.
	 */
	protected TemplateSettings(TemplateSettingsBuilder builder) {
		_outputEncoding = builder.getOutputEncoding();
		_outputByteOrderMark = builder.isOutputByteOrderMark();
		_outputFormat = builder.getOutputFormat();
		_outputXmlHeader = builder.isOutputXmlHeader();
		_ignoreWhitespaces = builder.isIgnoreWhitespaces();
	}

	/**
	 * Returns the default value for {@link #isOutputXmlHeader()}.
	 * <p>
	 * The default value depends on the {@link OutputFormat}.
	 * </p>
	 */
	public static boolean getDefaultOutputXmlHeader(OutputFormat outputFormat) {
		return outputFormat.equals(OutputFormat.XML);
	}

	/**
	 * The {@link Charset} for the output encoding.
	 * <ul>
	 * <li>Optional setting</li>
	 * <li>Default: UTF-8</li>
	 * </ul>
	 */
	public Charset getOutputEncoding() {
		return _outputEncoding;
	}

	/**
	 * Should a byte order mark (BOM) be prepended to the generated file?
	 * <ul>
	 * <li>Optional setting</li>
	 * <li>Default: true</li>
	 * </ul>
	 */
	public boolean isOutputByteOrderMark() {
		return _outputByteOrderMark;
	}

	/**
	 * Should an xml header be prepended to the generated file?
	 * <ul>
	 * <li>Optional setting</li>
	 * <li>Default: true if the {@link #getOutputFormat()} is {@link OutputFormat#XML}</li>
	 * <li>Example: <code>&lt;?xml version='1.0' encoding='utf-8'?&gt;</code></li>
	 * </ul>
	 * Set to false if no xml header is required.
	 */
	public boolean isOutputXmlHeader() {
		return _outputXmlHeader;
	}

	/**
	 * Shall whitespaces next to tags and comments be ignored?
	 * <ul>
	 * <li>Optional setting</li>
	 * <li>Default: true</li>
	 * <li>Whitespaces outside the body-tag are always ignored.</li>
	 * <li>Whitespaces inside of text-tags are never ignored.</li>
	 * <li>Whitespaces in parameters are never ignored.</li>
	 * </ul>
	 */
	public boolean isIgnoreWhitespaces() {
		return _ignoreWhitespaces;
	}

	/**
	 * The {@link OutputFormat} is mandatory.
	 */
	public OutputFormat getOutputFormat() {
		return _outputFormat;
	}

}
