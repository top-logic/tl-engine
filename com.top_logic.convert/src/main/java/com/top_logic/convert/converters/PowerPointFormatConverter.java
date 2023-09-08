/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.convert.converters;

import java.io.InputStream;
import java.util.Collection;

import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.sl.usermodel.SlideShowFactory;

import com.top_logic.convert.ConverterMapping;

/**
 * This class converts powerpoint data into plain text
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class PowerPointFormatConverter extends AbstractStringBasedConverter {

    /** The MIME type supported by this DocumentFilter */
    public static final String POWER_POINT_MIMETYPE = "application/vnd.ms-powerpoint";

    @Override
	protected String getContentFromStream(InputStream input) throws FormatConverterException {
        try {
			try (POITextExtractor powerPointExtractor = new SlideShowExtractor<>(SlideShowFactory.create(input))) {
				String contents = powerPointExtractor.getText();
//        	System.out.println(contents);
				return contents;
			}
        } catch (Exception e) {
            throw new FormatConverterException(e);
        }       
    }
    
    @Override
	protected void fillMimeTypeMappings(Collection<ConverterMapping> collection) {
	    collection.add(new ConverterMapping(POWER_POINT_MIMETYPE, AbstractFormatConverter.TXT_MIMETYPE));
    }

}
