/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Handler for XML that will accumulate the Elements String in a StringBuffer.
 * <p>
 *  You should subclass this handler and override the endElement() function.
 *  Then you can fetch the accumulated String via {@link #getString()}.
 *</p>
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class ElementHandler extends DefaultHandler 
{
    /** This buffer accumulates the characters received between two tags */
    private StringBuffer    buf;
    
    /** String last returned by getString() */
    private String          last;
          
    /**
     * Overriden to create the StringBuffer.
     */
    @Override
	public void startDocument() throws SAXException {
        buf  = new StringBuffer();
        last = null;
        super.startDocument();
    }

    /** Overriden to release the StringBuffer and the last parsed String */   
    @Override
	public void endDocument()  
            throws SAXException  {
        buf  = null;
        last = null;
    }
    
    /** Accumulate the given charactes in the buffer */
    @Override
	public void characters(char[] ch, int start, int length)
        throws SAXException {
        buf.append(ch,start,length);
    }
    
    /** Reset the String Buffer for the next usage.
     *  See (incomplete :-) Documentation of superclass 
     *  for parameters (ignored here).
     */
    @Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)  
        throws SAXException {
        buf.setLength(0);
        last = null;
    }
    
    protected String getStringDestructive() {
        String theString = this.getString();

        this.last = null;

        return (theString);
    }
    /** Return the String accumulated so far, usually called by endElement */
    public String getString() {
        if (last == null) {
            last = buf.toString();
            buf.setLength(0);
        }
        return last;
    }
    

}
