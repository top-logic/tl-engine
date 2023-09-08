/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.xml.sax.SAXUtil;

/**
 * Verifies that XML-documents have the appropriate syntax.
 *
 * @author    Michael Eriksson
 */
public class XMLValidator {

    /**
	 * Validate the specified File.
	 *
	 * @param xml
	 *        the file; must not be null
	 *
	 * @throws IOException
	 *         if an IO problem occured
	 * @throws SAXException
	 *         if the parsing/validation failed.
	 *
	 *         #author Michael Eriksson
	 */
	public void validate(BinaryData xml) throws IOException,
                                             ParserConfigurationException, 
                                             SAXException {
        SAXParser theParser = this.getParser();
		theParser.parse(xml.getStream(), new DefaultHandler());

    }

    /**
     * Validate the given XML-String.
     *
     * @param aString must contain some valid XML.
     *
     * @throws IOException       if an IO problem occurred
     * @throws SAXException      if the parsing/validation failed.
     *
     * #author Michael Eriksson
     */
    public void validate (String aString) throws IOException,
                                             ParserConfigurationException, 
                                             SAXException {
        SAXParser theParser = this.getParser();
        
        theParser.parse (new InputSource(new StringReader(aString))
            , new DefaultHandler ());

    }

    /**
     * Get a validating parser.
     *
     * @return the parser; never null
     * #author Michael Eriksson
     */
    private SAXParser getParser () throws ParserConfigurationException, SAXException  {
		return SAXUtil.newSAXParserValidating();
    }

}
