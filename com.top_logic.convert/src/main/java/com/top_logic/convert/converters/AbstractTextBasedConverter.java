/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.convert.converters;

import java.io.InputStream;
import java.io.Reader;

/**
 * {@link AbstractFormatConverter} That checks that the mime types are supported
 * by this converter and delegates to actual implementation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractTextBasedConverter extends AbstractFormatConverter {

	@Override
	public InputStream convert(InputStream input, String sourceMimeType, String destinationMimeType)
			throws FormatConverterException {
		if (!supports(sourceMimeType, destinationMimeType)) {
			throw new FormatConverterException("Conversion from" + sourceMimeType + " to " + destinationMimeType
					+ " is not supported by this Converter");
		}
		return internalConvert(input, sourceMimeType, destinationMimeType);
	}

	/**
	 * Does the actual conversion. It is guaranteed that the mime types are
	 * adequate.
	 */
	protected abstract InputStream internalConvert(InputStream input, String sourceMimeType, String destinationMimeType)
			throws FormatConverterException;

	/**
	 * Checks that the conversion to
	 * {@link AbstractFormatConverter#TXT_MIMETYPE} is possible.
	 * 
	 * @see com.top_logic.convert.converters.FormatConverter#convert(java.io.InputStream,
	 *      java.lang.String)
	 */
	@Override
	public Reader convert(InputStream input, String sourceMimeType) throws FormatConverterException {
		if (!supports(sourceMimeType, AbstractFormatConverter.TXT_MIMETYPE)) {
			throw new FormatConverterException("Conersion from" + sourceMimeType + " to "
					+ AbstractFormatConverter.TXT_MIMETYPE + " is not supported by this Converter");
		}
		return internalConvert(input, sourceMimeType);
	}

	/**
	 * Does the actual conversion. It is guaranteed that the mime types are
	 * adequate.
	 */
	protected abstract Reader internalConvert(InputStream inData, String sourceMimeType)
			throws FormatConverterException;

}
