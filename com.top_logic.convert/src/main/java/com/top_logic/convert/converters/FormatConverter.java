/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.convert.converters;

import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;

import com.top_logic.convert.ConverterMapping;

/**
 * Interface to convert input streams from one MIME-Type into another MIME-Type.
 * 
 * @author <a href="mailto:tma@top-logic.com>tma</a>
 */
public interface FormatConverter {

	/**
	 * This method checks if an input stream of the MIME-Type (the
	 * sourceMimeType) can be converted into a stream of another MIME-Type (the
	 * destinationMimeType).
	 * 
	 * Conversion may still fail due to other reasons, then this method throws a
	 * {@link FormatConverterException}.
	 * 
	 * Word documents MUST be supported.
	 * 
	 * @param    sourceMimeType         The source mime-type may not be null.
	 * @param    destinationMimeType    The destination mime-type may not be null.
	 * @return   true if and only if the conversion is supported, otherwise false.
	 */
    public boolean supports(String sourceMimeType, String destinationMimeType);
	
    /**
     * Same as above, but the destination mime-type should be text/plain.
     * 
     * @param  mimeType the source-mime-type to convert from
     * @return true if and only if the conversion, represented by the mapping is supported.
     */
	public boolean supports(String mimeType);

	/**
	 * Same as above, but needs a {@link ConverterMapping} as parameter.
	 * 
	 * @param  aMapping that represents the wished conversion
	 * @return true if and only if the conversion, represented by the mapping is supported.
	 */
	public boolean supports(ConverterMapping aMapping);
	
	/**
	 * This method returns an iterator, containing all {@link ConverterMapping} 
	 * this Converter supports.
	 */
	public Iterator<ConverterMapping> getConverterMappings();
	
	/**
	 * Before calling this method you must be sure that this conversion is supported by 
     * calling {@link #supports(String)} otherwise a {@link FormatConverterException} will be
     * thrown.
     * 
	 * @param    input     The data to be converted.
	 * @param    sourceMimeType  The MIME-type of the data to be converted.
	 * @param    destinationMimeType    The MIME-type of the data to be returned.
	 * @return   The converted data in a stream.
	 * @throws   FormatConverterException    If converting fails for a reason.
	 */
	public InputStream convert(InputStream input, String sourceMimeType, String destinationMimeType) throws FormatConverterException;

	
	/**
	 * This Method returns a reader with the content of the InputStream as plain text
	 * with the current system encoding.
	 * 
	 * @param  input       the stream to get plain text from
	 * @param  sourceMimeType    the current mimetype of the streamed data
	 * @return a reader with plain text representation of inData
	 */
	public Reader convert(InputStream input, String sourceMimeType) throws FormatConverterException;
	
	/*
	 * Add convenience methods, later
	 * 
	 * File convertToTmpFile(InputStream inData, String inMime, String outMime);
	 * 
	 * CharSequence covertToChar(InputStream inData, String inMime, String
	 * outMime);
	 * 
	 */
}
