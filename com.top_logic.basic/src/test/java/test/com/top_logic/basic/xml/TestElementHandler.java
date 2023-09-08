/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.xml.ElementHandler;
import com.top_logic.basic.xml.XMLAttributeHelper;
import com.top_logic.basic.xml.sax.SAXUtil;

/**
 * Testcase for the {@link com.top_logic.basic.xml.ElementHandler}.
 * 
 * This is NOT how the ElementHandler class should actually be used,
 * it works best when combined with an Dispatching Hanlder,
 * see TestDispatchingHandler.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestElementHandler extends TestCase {

    /**
     * Constructor for TestElementHandler.
     */
    public TestElementHandler(String arg0) {
        super(arg0);
    }

    /** Main Testcase for now. */
    public void testMain() throws Exception {
		File xml =
			new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/xml/TestElementHandler_test.xml");

		SAXParser theParser = SAXUtil.newSAXParser();
        TestedElementHandler handler = new TestedElementHandler();

        try {
            handler.getString();
            fail("Must not getString() before start Document");
        } catch (NullPointerException expected) { /* expected */ }

        theParser.parse (xml, handler);
        int i = 0;
        assertEquals(""                     ,handler.strings.get(i++));
        assertEquals("Start of root Tag"    ,handler.strings.get(i++));
        assertEquals("Start of middle Tag"  ,handler.strings.get(i++));
        assertEquals("Text in inner Tag"    ,handler.strings.get(i++));
        assertEquals("Text in inner Tag"    ,handler.strings.get(i++));
        assertEquals("Text in inner Tag"    ,handler.strings.get(i++));
        
        i = 0;
        assertEquals("root"                 ,handler.attribs.get(i++));
        assertEquals("mittel"               ,handler.attribs.get(i++));
        assertEquals("innen"                ,handler.attribs.get(i++));

        try {
            handler.getString();
            fail("Must not getString() after end of Document");
        } catch (NullPointerException expected) { /* expected */  }
    }

    /** Inner class to to the actual testing.
     * 
     * This is NOT how this class should actually be used,
     * it works best when combined with an Dispatching Hanlder,
     * see TestDispatchingHandler.
     * 
     */
    static class TestedElementHandler extends ElementHandler {
        
        /** Ann array of string that where finally parsed */
        List strings = new ArrayList();
        
        /** Ann array of attributes that where finally parsed */
        List attribs = new ArrayList();

        /**
         * Overriden to harvest the accumulated Strings.
         */
        @Override
		public void startElement(
            String uri, String localName,  String qName, Attributes attributes)
                                                    throws SAXException {
            attribs.add(XMLAttributeHelper.getAsStringOptional(attributes, "a"));                                                      
            String theString = getString();
            strings.add(theString.trim());
            super.startElement(uri, localName, qName, attributes);
        }

        /**
         * Overriden to harvest the accumulated Strings.
         */
        @Override
		public void endElement(String uri, String localName, String qName)
                                                    throws SAXException {
            String theString = getString();
            strings.add(theString.trim());
            Assert.assertEquals(theString,  getString());
        }
    }
    
    /**
     * the suite of tests to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestElementHandler.class);
        // TestSuite suite = new TestSuite ();
        // suite.addTest (new TestTagWriter("testThis"));
        return suite;
    }

    /**
     * Main function fo direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
