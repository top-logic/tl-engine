/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.convert.converters;

import java.io.ByteArrayInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;

import org.jsoup.Jsoup;
import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import com.top_logic.convert.ConverterMapping;

/**
 * This class converts HTML, XHTML and XML into plain text
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class HTMLFormatConverter extends AbstractTextBasedConverter {

    /** The MIME type for normal HTML */
    public static final String HTML_MIMETYPE    = "text/html";
    
    /** The MIME type for XHTML */
    public static final String XHTML_MIMETYPE   = "application/xhtml+xml";

    /** The MIME type for XML */
    public static final String XML_MIMETYPE     = "text/xml";
    
    @Override
	protected InputStream internalConvert(InputStream input, String fromMime, String toMime) throws FormatConverterException {
		return new ByteArrayInputStream(parse(fromMime, input).text().getBytes());
    }

	@Override
	protected Reader internalConvert(InputStream inData, String fromMime) throws FormatConverterException {
		return new StringReader(parse(fromMime, inData).text());
	}

	private Document parse(String fromMime, InputStream input) throws IOError {
		Document doc;
		try {
			if (XML_MIMETYPE.equals(fromMime)) {
				doc = DataUtil.load(input, null, "", Parser.xmlParser());
			} else {
				doc = Jsoup.parse(input, null, "");
			}
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		return doc;
	}

    @Override
	protected void fillMimeTypeMappings(Collection<ConverterMapping> collection) {
        collection.add(new ConverterMapping(HTML_MIMETYPE,AbstractFormatConverter.TXT_MIMETYPE));
        collection.add(new ConverterMapping(XHTML_MIMETYPE,AbstractFormatConverter.TXT_MIMETYPE));
        collection.add(new ConverterMapping(XML_MIMETYPE,AbstractFormatConverter.TXT_MIMETYPE));
    }

}
