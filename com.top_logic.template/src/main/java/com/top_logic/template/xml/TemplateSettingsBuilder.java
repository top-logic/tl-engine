/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.xml;

import java.nio.charset.Charset;

import com.top_logic.template.xml.TemplateSettings.OutputFormat;

/**
 * A builder for {@link TemplateSettings}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TemplateSettingsBuilder {

	private Charset _outputEncoding = TemplateSettings.DEFAULT_OUTPUT_ENCODING;

	private boolean _outputByteOrderMark = TemplateSettings.DEFAULT_OUTPUT_BYTE_ORDER_MARK;

	private final OutputFormat _outputFormat;

	private Boolean _outputXmlHeader;

	private boolean _ignoreWhitespaces = TemplateSettings.DEFAULT_IGNORE_WHITESPACES;

	/**
	 * Creates a new {@link TemplateSettingsBuilder}.
	 */
	protected TemplateSettingsBuilder(OutputFormat outputFormat) {
		if (outputFormat == null) {
			throw new NullPointerException("Output format must not be null.");
		}
		_outputFormat = outputFormat;
	}

	/** Build the {@link TemplateSettings}. */
	public TemplateSettings build() {
		return new TemplateSettings(this);
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

	/** @see #getOutputEncoding() */
	public void setOutputEncoding(Charset outputEncoding) {
		_outputEncoding = outputEncoding;
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

	/** @see #isOutputByteOrderMark() */
	public void setOutputByteOrderMark(boolean outputByteOrderMark) {
		_outputByteOrderMark = outputByteOrderMark;
	}

	/**
	 * Should an xml header be prepended to the generated file?
	 * <p>
	 * <ul>
	 * <li>Optional setting</li>
	 * <li>Default: <code>true</code> if the {@link #getOutputFormat()} is {@link OutputFormat#XML}</li>
	 * <li>Example: <code>&lt;?xml version='1.0' encoding='utf-8'?&gt;</code></li>
	 * </ul>
	 * Set to <code>false</code> if no xml header is required.
	 * </p>
	 */
	public boolean isOutputXmlHeader() {
		if (_outputXmlHeader == null) {
			return TemplateSettings.getDefaultOutputXmlHeader(getOutputFormat());
		}
		return _outputXmlHeader;
	}

	/** @see #isOutputXmlHeader() */
	public void setOutputXmlHeader(boolean outputXmlHeader) {
		_outputXmlHeader = outputXmlHeader;
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

	/** @see #isIgnoreWhitespaces() */
	public void setIgnoreWhitespaces(boolean ignoreWhitespaces) {
		_ignoreWhitespaces = ignoreWhitespaces;
	}

	/**
	 * The {@link OutputFormat} is mandatory and given in the constructor:
	 * {@link #TemplateSettingsBuilder(OutputFormat)}
	 */
	public OutputFormat getOutputFormat() {
		return _outputFormat;
	}

}
