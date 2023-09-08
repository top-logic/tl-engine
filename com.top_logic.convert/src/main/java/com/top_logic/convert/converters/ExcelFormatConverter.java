/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.convert.converters;

import java.io.InputStream;
import java.util.Collection;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import com.top_logic.convert.ConverterMapping;

/**
 * This Converter converts excel data into plain text.
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class ExcelFormatConverter extends AbstractStringBasedConverter {

    public static final String EXCEL_MIMETYPE = "application/vnd.ms-excel";
    
    /**
     * This method extracts all the text from a given excel-data stream into a string.
     * 
     * @param   inData  the excel stream
     * @return  the extracted text
     */
    @Override
	protected String getContentFromStream(InputStream inData) throws FormatConverterException {
        StringBuffer theContent = new StringBuffer();
        try {
            Workbook workbook = Workbook.getWorkbook(inData);
            Sheet[] sheets = workbook.getSheets(); 
            for (int i = 0; i < sheets.length; i++) {
                Sheet sheet = sheets[i];
                int nbCol = sheet.getColumns();
                for (int j = 0; j < nbCol; j++) {
                    Cell[] cells = sheet.getColumn(j);
                    for (int k = 0; k < cells.length; k++) {
                        theContent.append(cells[k].getContents() + " ");
                    }
                }
            }
        } catch (Exception e) {
            throw new FormatConverterException(e);
        } 
        return theContent.toString();
    }
    
    @Override
	protected void fillMimeTypeMappings(Collection<ConverterMapping> collection) {
        collection.add(new ConverterMapping(EXCEL_MIMETYPE,AbstractFormatConverter.TXT_MIMETYPE));
    }

}
