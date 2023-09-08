/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.office;

import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;

import junit.framework.Test;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.xml.XMLAttributeHelper;
import com.top_logic.basic.xml.sax.SAXUtil;


/**
 * Test case to evaluate the interesting content of an expression via SAX parsing
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class SAXParserTest extends BasicTestCase {
    public SAXParserTest (String aName) {
        super(aName);
    }
    
    private static Pattern expPattern = Pattern.compile ("<exp type=\\\"([^>]*)\\\">([^<]*)</exp>");
    private static Pattern expPatternStyle = Pattern.compile ("<exp type=\\\"([^>]*)\\\" style=\\\"([^>]*)\\\">([^<]*)</exp>");

    private static String expression1 = "<exp type=\"static\">SEPPL_HEAD</exp>"; 
    private static String expression2 = "<exp type=\"script\" style=\"font-size:18px\">person.getName();</exp>";
    
    public void testParsing () throws Exception {
		SAXParser theParser = SAXUtil.newSAXParser();
        ExpressionHandler handler = new ExpressionHandler();
        
        startTime();
        
        Reader aReader = getReader(expression1);
        theParser.parse (new InputSource (aReader), handler);
        aReader.close();
        logTime("saxparsing without");
        
        assertEquals ("static",handler.typeValue);
        assertEquals("SEPPL_HEAD",handler.getString());
        assertNull(handler.styleValue);
    }
    
    public void testParsingStyle () throws Exception {
		SAXParser theParser = SAXUtil.newSAXParser();
        ExpressionHandler handler = new ExpressionHandler();

        startTime();
        
        Reader aReader = getReader(expression2);
        theParser.parse (new InputSource (aReader), handler);
        aReader.close();
        logTime("saxparsing with stylesheet");

        assertEquals ("script",handler.typeValue);
        assertEquals ("font-size:18px",handler.styleValue);
        assertEquals("person.getName();",handler.getString());
    }

    public void testParsingRepeated () throws Exception {
		SAXParser theParser = SAXUtil.newSAXParser();
        ExpressionHandler handler = new ExpressionHandler();

        startTime();
        for (int i = 0; i < 1000; i++) {
            Reader aReader = getReader(expression2);
            theParser.parse (new InputSource (aReader), handler);
            aReader.close();
        }
        logTime("saxparsing with stylesheet 1000 times: ");

        assertEquals ("script",handler.typeValue);
        assertEquals ("font-size:18px",handler.styleValue);
        assertEquals("person.getName();",handler.getString());
    }
    public void testMatchingRepeated () throws Exception {
        String theExpression = expression2;

        startTime();
        String type     = null;
        String style    = null;
        String content  = null;
        
        Matcher matcher = null;
        for (int i = 0; i < 1000; i++) {
            matcher = expPatternStyle.matcher(theExpression);
            if (matcher.find() && matcher.groupCount() == 3) { // first the complex expression with style
                type = matcher.group(1);
                style = matcher.group(2);
                content = matcher.group(3);
            } else { // we didn't find it so use the simpler one
                matcher = expPattern.matcher(theExpression);
                if (matcher.find() && matcher.groupCount() == 2) {
                    type = matcher.group(1);
                    content = matcher.group(2);
                } else {
                    fail ("no match");
                }
            }
        }
        logTime("parsing with matcher 1000 times: ");

        assertEquals ("script",type);
        assertEquals ("font-size:18px",style);
        assertEquals("person.getName();",content);
    }
    private Reader getReader (String aString) {
        return new StringReader (aString);
    }
    
    /** Return the suite of Tests to perform */
    public static Test suite () {
        return OfficeTestSetup.createOfficeTestSetup(SAXParserTest.class);
    }
 
    public static void main(String[] args) {
        BasicTestCase.SHOW_TIME = true;
        junit.textui.TestRunner.run(suite());
    }

    private class ExpressionHandler extends DefaultHandler {
        public String typeValue;
        public String styleValue;
        
        /** This buffer accumulates the characters received between two tags */
        private StringBuffer    buf;
        
              
        public ExpressionHandler() {
			super();
		}

		/**
         * Overriden to create the StringBuffer.
         */
        @Override
		public void startDocument() throws SAXException {
            if (buf == null) {
                buf  = new StringBuffer();
            }
        }
        
        /** Accumulate the given charactes in the buffer */
        @Override
		public void characters(char[] ch, int start, int length)
            throws SAXException {
            buf.append(ch,start,length);
        }
        
        /** Return the String accumulated so far, usually called by endElement */
        public String getString() {
            if (buf != null) {
                return buf.toString();
            }
            return null;
        }
        /**
         * @see com.top_logic.basic.xml.ElementHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
		public void startElement(String someUri, String someLocalName, String someName, Attributes attributes) throws SAXException {
            buf.setLength(0);
            typeValue = XMLAttributeHelper.getAsStringOptional(attributes, "type");
            styleValue = XMLAttributeHelper.getAsStringOptional(attributes, "style");
        }
//        /**
//         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
//         */
//        public void endElement(String someUri, String someLocalName, String someName) throws SAXException {
//        }
    }
}
