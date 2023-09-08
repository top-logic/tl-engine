/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.convert.converters;

import java.io.InputStream;
import java.util.Collection;

import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.extractor.XSLFExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;

import com.top_logic.convert.ConverterMapping;

/**
 * This class converts powerpoint data into plain text
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class PowerPointXFormatConverter extends AbstractStringBasedConverter {

    /** The MIME type supported by this DocumentFilter */
    public static final String POWER_POINT_X_MIMETYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation";

    @Override
	protected String getContentFromStream(InputStream input) throws FormatConverterException {
        try {
        	OPCPackage thePack = OPCPackage.open(input);
			try (POITextExtractor theExtr = new XSLFExtractor(new XMLSlideShow(thePack))) {
				String contents = theExtr.getText();
//        	System.out.println(contents);
				return contents;
			}
        } catch (Exception e) {
            throw new FormatConverterException(e);
        }       
    }
    
    @Override
	protected void fillMimeTypeMappings(Collection<ConverterMapping> collection) {
    	ConverterMapping mapping = new ConverterMapping(POWER_POINT_X_MIMETYPE, AbstractFormatConverter.TXT_MIMETYPE);
		collection.add(mapping);
    }

}
