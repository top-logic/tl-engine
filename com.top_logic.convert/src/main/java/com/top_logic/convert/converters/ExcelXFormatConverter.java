/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
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
 * This class converts powerpoint data into plain text
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class ExcelXFormatConverter extends AbstractStringBasedConverter {

    /** The MIME type supported by this DocumentFilter */
    public static final String EXCEL_X_MIMETYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    
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
    	collection.add(new ConverterMapping(EXCEL_X_MIMETYPE, AbstractFormatConverter.TXT_MIMETYPE));
    }

}
