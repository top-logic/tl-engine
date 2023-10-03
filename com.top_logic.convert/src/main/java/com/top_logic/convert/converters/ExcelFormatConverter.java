/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.convert.converters;

import java.io.InputStream;
import java.util.Collection;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;

import com.top_logic.convert.ConverterMapping;

/**
 * This Converter converts excel data into plain text.
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class ExcelFormatConverter extends AbstractStringBasedConverter {

	public static final String EXCEL_MIMETYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    
    /**
     * This method extracts all the text from a given excel-data stream into a string.
     * 
     * @param   inData  the excel stream
     * @return  the extracted text
     */
    @Override
	protected String getContentFromStream(InputStream inData) throws FormatConverterException {
		try {
			OPCPackage thePack = OPCPackage.open(inData);
			XSSFExcelExtractor theExtr = new XSSFExcelExtractor(thePack);
			String contents = theExtr.getText();
			return contents;
        } catch (Exception e) {
            throw new FormatConverterException(e);
		}
    }
    
    @Override
	protected void fillMimeTypeMappings(Collection<ConverterMapping> collection) {
        collection.add(new ConverterMapping(EXCEL_MIMETYPE,AbstractFormatConverter.TXT_MIMETYPE));
    }

}
