/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.xml.DispatchingHandler;
import com.top_logic.basic.xml.ElementHandler;
import com.top_logic.basic.xml.XMLAttributeHelper;
import com.top_logic.basic.xml.sax.SAXUtil;

/**
 * Testcase for the {@link com.top_logic.basic.xml.DispatchingHandler}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestDispatchingHandler extends TestCase {

    /**
     * Constructor for TestDispatchingHandler.
     */
    public TestDispatchingHandler(String arg0) {
        super(arg0);
    }

    /** Main Testcase for now. */
    public void testMain() throws Exception {
		File xml = new File(
			ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/xml/TestDispatchingHandler_test.xml");
		SAXParser theParser = SAXUtil.newSAXParserNamespaceAware();
        
        TestedDispatchingHandler handler = new TestedDispatchingHandler();

        theParser.parse (xml, handler);
        int i = 0;
        assertEquals("Text in inner Tag"    ,handler.strings.get(i++));

        String theString = (String) handler.strings.get(i++);
        assertTrue(theString, theString.startsWith("Start of middle Tag"));
        assertTrue(theString, theString.endsWith  ("End of middle Tag"));

        theString = (String) handler.strings.get(i++);
        assertTrue(theString, theString.startsWith("Start of root Tag"));
        assertTrue(theString, theString.endsWith  ("End of root Tag"));

        i = 0;
        assertEquals("root"                 ,handler.attribs.get(i++));
        assertEquals("mittel"               ,handler.attribs.get(i++));
        assertEquals("innen"                ,handler.attribs.get(i++));

        i = 0;
        assertEquals("test.com.top_logic.basic.xml.TestDispatchingHandler" 
                                            ,handler.instructions.get(i++));
        assertEquals("TEST=\"yes\"" 
                                            ,handler.instructions.get(i++));
        
        i = 0;
        assertEquals("tl"                        ,handler.prefixes.get(i++));
        assertEquals("http://top-logic.com/test" ,handler.prefixes.get(i++));
        assertEquals("tl"                        ,handler.prefixes.get(i++));
    }

    public void testBroken() throws Exception {
		File xml = new File(ModuleLayoutConstants.SRC_TEST_DIR
			+ "/test/com/top_logic/basic/xml/TestDispatchingHandler_test_broken.xml");
		SAXParser theParser = SAXUtil.newSAXParserNamespaceAware();
        TestedDispatchingHandler handler = new TestedDispatchingHandler();

        // test broken xml file
        try {
			xml = new File(ModuleLayoutConstants.SRC_TEST_DIR
				+ "/test/com/top_logic/basic/xml/TestDispatchingHandler_test_broken.xml");
            theParser.parse (xml, handler);
        }
        catch (SAXParseException expected) { /*expected*/ }
        
        handler.strings.clear(); 
        // reset so broken data does not affect us.
        
        // test good xml file after a broken file
		xml = new File(
			ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/xml/TestDispatchingHandler_test.xml");
        theParser.parse (xml, handler);
        int i = 0;
        assertEquals("Text in inner Tag"    ,handler.strings.get(i++));

        String theString = (String) handler.strings.get(i++);
        assertTrue(theString, theString.startsWith("Start of middle Tag"));
        assertTrue(theString, theString.endsWith  ("End of middle Tag"));

        theString = (String) handler.strings.get(i++);
        assertTrue(theString, theString.startsWith("Start of root Tag"));
        assertTrue(theString, theString.endsWith  ("End of root Tag"));

        i = 0;
        assertEquals("root"                 ,handler.attribs.get(i++));
        assertEquals("mittel"               ,handler.attribs.get(i++));
        assertEquals("innen"                ,handler.attribs.get(i++));

        i = 0;
        assertEquals("test.com.top_logic.basic.xml.TestDispatchingHandler" 
                                            ,handler.instructions.get(i++));
        assertEquals("TEST=\"yes\"" 
                                            ,handler.instructions.get(i++));
        
        i = 0;
        assertEquals("tl"                        ,handler.prefixes.get(i++));
        assertEquals("http://top-logic.com/test" ,handler.prefixes.get(i++));
        assertEquals("tl"                        ,handler.prefixes.get(i++));

    }

    /** Inner class to to the actual testing.
     * 
     * This is NOT how this class should actually be used,
     * it works best when combined with an Dispatching Hanlder,
     * see TestDispatchingHandler.
     * 
     */
    static class TestedDispatchingHandler extends DispatchingHandler {
        
        /** Ann array of string that where finally parsed */
        List strings = new ArrayList();
        
        /** Ann array of attributes that where finally parsed */
        List attribs = new ArrayList();

        /** Ann array of processing instructions that where finally parsed */
        List instructions = new ArrayList();

        /** Ann array of namespace mappings that where finally parsed */
        List prefixes = new ArrayList();

        /** Register the InnerHandlr for the actual work ...*/
        public TestedDispatchingHandler() {
            // Handling of namesspaces could be better ...
            this.registerHandler("tl:wurz" , new TestedElementHandler(1));
            this.registerHandler("middle"  , new TestedElementHandler(2));
            this.registerHandler("inner"   , new TestedElementHandler(3));
            
            ElementHandler h1 = new TestedElementHandler(4);
            ElementHandler h2 = new TestedElementHandler(5);
            
            Map extraHandlers = new HashMap(2);
            extraHandlers.put("ignore", h1);
            extraHandlers.put("egal"  , h2);
            this.registerHandlers(extraHandlers);
            
            this.unregisterHandler("ignore");
            
        }
        
        /** Forwarded to current Handler for Testing.
         *  
         * (This is not default since in TopLogic we use attruibutes only)
         */
         @Override
		public void characters(char[] ch, int start, int length) throws SAXException {
             this.getCurrent().characters(ch, start, length);
         }


         /** Record values in instructions List. */
         @Override
		public void processingInstruction(String target, String data) throws SAXException {
             instructions.add(target);
             instructions.add(data);
         } 

         /**
          * Overriden to gain some coveragae
          */
        @Override
		public void startPrefixMapping(String prefix, String uri)
                   throws SAXException {
            prefixes.add(prefix);
            prefixes.add(uri);
        }

        /**
          * Record values in prefixes List.
          */
         @Override
		public void endPrefixMapping(String prefix) throws SAXException {
             prefixes.add(prefix);
         }


        /** (double) Inner class to to the actual testing.
         * 
         * This is NOT how this class should actually be used,
         * it works best when combined with an Dispatching Hanlder,
         * see TestDispatchingHandler.
         * 
         */
        class TestedElementHandler extends ElementHandler {
        	
            /** Expected depth of Stack */
            int depth; 
            
            /** @param aDepth expected depth of Stack */
            public TestedElementHandler(int aDepth) {
            	depth = aDepth;
            }
            
            /**
             * Overriden to harvest the Attribute.
             */
            @Override
			public void startElement(
                String uri, String localName,  String qName, Attributes attributes)
                                                        throws SAXException {
                attribs.add(XMLAttributeHelper.getAsStringOptional(attributes, "a")); 
                assertEquals(depth, getStackSize());
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
    }

    
    /**
     * the suite of tests to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestDispatchingHandler.class);
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
