/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.convert.converters;

import java.io.InputStream;
import java.util.Collection;

import org.textmining.extraction.TextExtractor;
import org.textmining.extraction.word.WordTextExtractorFactory;

import com.top_logic.convert.ConverterMapping;

/**
 * This {@link FormatConverter} is able to convert microsoft Word data 
 * into plain text. It supports mime-type mapping "application/word -> text/plain"
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class MsWordFormatConverter extends AbstractStringBasedConverter {

    public static final String MSWORD_MIMETYPE = "application/msword";
    
    /**
     * This method extracts all the text from a given word data stream into a string.
     * 
     * @param   inData the word stream
     * @return  the extracted text
     */
    @Override
	protected String getContentFromStream(InputStream inData) throws FormatConverterException {
        WordTextExtractorFactory fac = new WordTextExtractorFactory();
        try {       
            TextExtractor wte = fac.textExtractor(inData);
            String theContent = wte.getText();
            return theContent;
        } catch (Exception e) {
            throw new FormatConverterException(e);
        }
    }
    
    /** 
     * This method fills the collection with the convertermapping "application/msword text/plain"
     * 
     * @see com.top_logic.convert.converters.AbstractFormatConverter#fillMimeTypeMappings(Collection)
     */
    @Override
	protected void fillMimeTypeMappings(Collection<ConverterMapping> collection) {
        collection.add(new ConverterMapping(MSWORD_MIMETYPE, TXT_MIMETYPE));
    }


}
