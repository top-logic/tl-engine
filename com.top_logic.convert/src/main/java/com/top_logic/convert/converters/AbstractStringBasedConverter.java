/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.convert.converters;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * {@link AbstractTextBasedConverter} that reads the contents to a String and
 * creates the actual output from it.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractStringBasedConverter extends AbstractTextBasedConverter {

	/**
	 * This method converts data into a given mime-type if this is supported.
	 * 
	 * @param input
	 *        the data stream
	 * @param sourceMimeType
	 *        the source mime-type
	 * @param destinationMimeType
	 *        the mime-type to convert to
	 * @return the content of the data as plain text in an reader
	 * 
	 * @throws FormatConverterException
	 *         if some exception occurred.
	 * 
	 * @see AbstractTextBasedConverter#convert(InputStream, String, String)
	 */
	@Override
	protected InputStream internalConvert(InputStream input, String sourceMimeType, String destinationMimeType)
			throws FormatConverterException {
		return new ByteArrayInputStream(getContentFromStream(input).getBytes());
	}

	/**
	 * This method converts data into a given mime-type if this is supported.
	 * 
	 * @param input
	 *        the data stream
	 * @param sourceMimeType
	 *        the source mime-type
	 * @return the content of the data as plain text in an reader
	 * 
	 * @throws FormatConverterException
	 *         if some exception occurred.
	 * 
	 * @see AbstractTextBasedConverter#convert(InputStream, String)
	 */
	@Override
	protected Reader internalConvert(InputStream input, String sourceMimeType) throws FormatConverterException {
		return new StringReader(getContentFromStream(input));
	}

	/**
	 * Helper method
	 * 
	 * @param input
	 *        the power point data stream
	 * @return a string representation of the power point data
	 */
	protected abstract String getContentFromStream(InputStream input) throws FormatConverterException;
}
