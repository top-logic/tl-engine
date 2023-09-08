/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.convert.converters;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.top_logic.convert.ConverterMapping;

/**
 * This {@link FormatConverter} is able to convert pdf data into plain
 * text. It supports the mime-type mapping "application/pdf -> text/plain"
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class PdfFormatConverter extends AbstractStringBasedConverter {

    public static final String PDF_MIMETYPE = "application/pdf"; 
    
    @Override
	protected void fillMimeTypeMappings(Collection<ConverterMapping> collection) {
        collection.add(new ConverterMapping(PDF_MIMETYPE, TXT_MIMETYPE));
    }

    /**
     * This method extracts all the text from a given pdf data stream to a String
     * 
     * @param   input the pdf data stream
     * @return  string representation of the text
     */
    @Override
	protected String getContentFromStream(InputStream input) throws FormatConverterException{
		try {
			RandomAccessRead buffer = new RandomAccessReadBuffer(input);
			PDFParser parser = new PDFParser(buffer);
			try (PDDocument pdfDocument = parser.parse()) {
				PDFTextStripper stripper = new PDFTextStripper();
				return stripper.getText(pdfDocument);
			}
		} catch (IOException ex) {
			throw new FormatConverterException(ex);
		}
    }
}
