/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.convert.converters;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;

import com.top_logic.convert.ConverterMapping;

/**
 * This TextPlainConverter is only capable to convert plain text
 * into plain text.
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class TextPlainConverter extends AbstractTextBasedConverter {

    /**
     * This converter is a TextPlainConverter, it is only
     * capable to convert plain text into plain text.
     * 
     * @param   collection to add it's mapping to.
     * 
     * @see     com.top_logic.convert.converters.AbstractFormatConverter#fillMimeTypeMappings(Collection)
     */
    @Override
	protected void fillMimeTypeMappings(Collection<ConverterMapping> collection) {
        collection.add(new ConverterMapping(TXT_MIMETYPE, TXT_MIMETYPE));
    }


	@Override
	protected InputStream internalConvert(InputStream input, String sourceMimeType, String destinationMimeType)
			throws FormatConverterException {
		return input;
	}


	@Override
	protected Reader internalConvert(InputStream inData, String sourceMimeType) throws FormatConverterException {
		return new InputStreamReader(inData);
	}
}
