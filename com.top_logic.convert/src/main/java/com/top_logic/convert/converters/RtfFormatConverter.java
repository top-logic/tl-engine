/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.convert.converters;

import java.io.InputStream;
import java.util.Collection;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

import com.top_logic.convert.ConverterMapping;

/**
 * This class converts rtf data into plain text
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class RtfFormatConverter extends AbstractStringBasedConverter {

    /** The MIME type supported by this DocumentFilter */
    public static final String RTF_MIMETYPE = "text/rtf"; 
    
    /**
     * Helpermethod to convert the rtf stream into a string representation
     */
    @Override
	protected String getContentFromStream(InputStream input) throws FormatConverterException {
        try {           
            DefaultStyledDocument sd = new DefaultStyledDocument();
            RTFEditorKit kit = new RTFEditorKit();
            kit.read(input, sd, 0);
            String content = sd.getText(0, sd.getLength());
            return content;
        } catch (Exception e) {
            throw new FormatConverterException(e);
        } 
    }
    
    @Override
	protected void fillMimeTypeMappings(Collection<ConverterMapping> collection) {
        collection.add(new ConverterMapping(RTF_MIMETYPE,AbstractFormatConverter.TXT_MIMETYPE));
    }

}
