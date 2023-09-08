/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml;

import java.io.StringReader;

import javax.xml.parsers.SAXParser;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.XMLAttributeHelper;
import com.top_logic.basic.xml.sax.SAXUtil;

/**
 * Testscase for {@link XMLAttributeHelper}.
 * 
 * @author    <a href=mailto:cdo@top-logic.com>cdo</a>
 */
public  class TestXMLAttributeHelper extends TestCase {

    private static final String XML = 
        "<element string='s' bool='true' bool3='YES' int='10'" 
          + " class='java.lang.String'" 
          + " classThis='" + TestXMLAttributeHelper.class.getName() + "'" 
          + " single='com.top_logic.basic.col.filter.TrueFilter'" 
          + " single2='com.top_logic.basic.sql.DefaultStmCache'" 
          + " single3='" + BrokenSingleton.class .getName() + "'" 
          + " single4='" + BrokenSingleton2.class.getName() + "'" 
          + " singleX='com.broken.NoSuchClass' />";
    
    private Attributes attributes;
    
    /** 
     * Create a new TestXMLAttributeHelper (for Introspection tests).
     */
    protected TestXMLAttributeHelper() {
        // for testing , will try to call this via introspection.
    }
    
    /** 
     * Create a new TestXMLAttributeHelper for given function.
     */
    public TestXMLAttributeHelper(String function) {
        super(function);
    }

    /** Setup the test by parsing a piece of XML to set up {@link #attributes} */
    @Override
	protected void setUp() throws Exception {
		SAXParser theParser = SAXUtil.newSAXParser();
        theParser.parse(new InputSource(new StringReader(XML)), new DefaultHandler() {
            /** Store the Attributes in the TestXMLAttributeHelper */
            @Override
			public void startElement(String someUri, String someLocalName, String someName,
                                     Attributes someAttributes) throws SAXException {
                /* We have to clone the object because the sax parser will clear
                 * it on return of this method call. */
                TestXMLAttributeHelper.this.attributes = new AttributesImpl(someAttributes);
            }
        });
    }
    
    /** clear the attributes to realaes memory. */
    @Override
	protected void tearDown() throws Exception {
        this.attributes = null;
    }

    // Tests
    
    /** Make JCoverage happy by calling th CTor */
    public void testCTor() {
        XMLAttributeHelper strange = new XMLAttributeHelper() {
            // nothing actually done here.
        };
        strange.toString();
    }

    /** Test creating an Object as intace of a class from XML. */
    public void testInstanceOfClass() throws Exception {
        Object theObject = XMLAttributeHelper.getAsInstanceOfClass(this.attributes, "class");
        assertTrue(theObject instanceof String);

        theObject = XMLAttributeHelper.getAsInstanceOfClass(this.attributes, "noSuchzClass", NullPointerException.class);
        assertTrue(theObject instanceof NullPointerException);
        
		TagWriter out = new TagWriter();
        out.beginBeginTag("something");
        XMLAttributeHelper.writeClassAttributeFromClass(out, "class", String.class , Object.class);
        XMLAttributeHelper.writeClassAttributeFromClass(out, "klos",  String.class , String.class);
        XMLAttributeHelper.writeClassAttributeFromObject(out, "fool",  "fool"       , String.class);
		XMLAttributeHelper.writeClassAttributeFromObject(out, "grunt", Short.valueOf((short) 17), Object.class);
        out.endEmptyTag();
        
        String result = out.toString();
        assertEquals(result, "<something class=\"java.lang.String\" grunt=\"java.lang.Short\"/>");
    }
    
    /** Test creation of object via non-default CTor. */
    public void testInstanceOfClassParam() throws Exception {
        Object theObject = XMLAttributeHelper.getAsInstanceOfConfiguredClass(
                this.attributes, "classThis", null,
                    new Class[] {String.class} , 
                    new Object[] {"testInstanceOfClassParam"});
        assertTrue(theObject instanceof TestXMLAttributeHelper);

        theObject = XMLAttributeHelper.getAsInstanceOfConfiguredClass(
                this.attributes, "classThis", this.getClass(),
                    new Class[] {String.class} , 
                    new Object[] {"testInstanceOfClassParam"});
        assertTrue(theObject instanceof TestXMLAttributeHelper);

        theObject = XMLAttributeHelper.getAsInstanceOfConfiguredClass(
                this.attributes, "classThis", Integer.class,
                    new Class[] {String.class} , 
                    new Object[] {"testInstanceOfClassParam"});        
        assertTrue(theObject instanceof TestXMLAttributeHelper);
        
        try {
            XMLAttributeHelper.getAsInstanceOfConfiguredClass(
                this.attributes, "classThis", Integer.class,
                    new Class[] {} , 
                    new Object[] {});
        } catch (SAXException expected) { /* IllgalAccesException*/ }

        try {
            XMLAttributeHelper.getAsInstanceOfConfiguredClass(
                this.attributes, "classThis", Integer.class,
                    new Class[] {this.getClass()} , 
                    new Object[] {this});
        } catch (SAXException expected) { /* IllgalAccesException*/ }
        
    }

    /** Test creation of object via non-default CTor. */
    public void testInstanceOfClassObject() throws Exception {
        Object theObject = XMLAttributeHelper.getAsInstance(
                this.attributes, "classThat", this);
        assertSame(this, theObject);
        
        theObject =XMLAttributeHelper.getAsInstance(
                this.attributes, "classThat", (Object) null);
        assertNull(theObject);
    
        theObject = XMLAttributeHelper.getAsInstance(
                this.attributes, "class", this);
        assertTrue(theObject instanceof String);

        try {
            XMLAttributeHelper.getAsInstance(
                this.attributes, "classThis", (Object) null);
        } catch (SAXException expected) { /* IllgalAccesException*/ }
        
        try {
            XMLAttributeHelper.getAsInstance(
                this.attributes, "classThat", (Object) null);
        } catch (SAXException expected) { /* IllgalAccesException*/ }
    }
    
    public static final TestXMLAttributeHelper INSTANCE = new TestXMLAttributeHelper();

    /** Test fetching objects from singletons */
    public void testSingleton() throws Exception {
        assertSame(INSTANCE, XMLAttributeHelper.getAsInstanceOfClass(this.attributes, "version", TestXMLAttributeHelper.class));

        assertSame(FilterFactory.trueFilter(), XMLAttributeHelper.getAsInstanceOfClass(this.attributes, "single"));

        assertSame(FilterFactory.trueFilter(), XMLAttributeHelper.getAsInstance(this.attributes, "single", (Object) null));

        assertSame(FilterFactory.trueFilter(), XMLAttributeHelper.getAsInstance(this.attributes, "single", (Object) null));

        assertSame(this, XMLAttributeHelper.getAsInstance(this.attributes, "noSuchClass", this));

        assertSame(this, XMLAttributeHelper.getAsInstance(this.attributes, "noSuchClass", this));

        try {
           XMLAttributeHelper.getAsInstanceOfClass(this.attributes, "version");
			fail("Expected SAXException: No attribute 'version' set and neither default class nor default instance.");
        } catch (SAXException expected) { /* expected */ }
        
        try {
           XMLAttributeHelper.getAsInstanceOfClass(this.attributes, "single3");
			fail("Expected SAXException: No singleton is not public.");
        } catch (SAXException expected) { /* expected */ }

        try {
           XMLAttributeHelper.getAsInstanceOfClass(this.attributes, "single4");
			fail("Expected SAXException: getInstance() throws exception.");
        } catch (SAXException expected) { /* expected */ }
    }
    
    public void testGetAsStringAttributesStringString() {
        assertEquals("s", XMLAttributeHelper.getAsString(this.attributes, "string", "default"));
        assertEquals("default", XMLAttributeHelper.getAsString(this.attributes, "string2", "default"));
    }


     public void testGetAsStringAttributesString() throws Exception {
        assertEquals("s", XMLAttributeHelper.getAsString(this.attributes, "string"));
        
        try {
            XMLAttributeHelper.getAsString(this.attributes, "string2");
            fail("A SAXException should be thrown");
        } catch (SAXException expected) { /* expected */ }
    }
     
    /** Test the getAsBoolean() methods */
    public void testGetAsBoolean() throws SAXException {
        assertTrue (XMLAttributeHelper.getAsBoolean(this.attributes, "bool" , false));
        assertFalse(XMLAttributeHelper.getAsBoolean(this.attributes, "bool2", false));

        assertTrue(XMLAttributeHelper.getAsBoolean(this.attributes, "bool"));
        try {
            assertTrue(XMLAttributeHelper.getAsBoolean(this.attributes, "bool2"));
            fail("A SAXException should be thrown");
        } catch (SAXException expected) { /* expected */ }
        
        assertTrue (XMLAttributeHelper.getAsBoolean(this.attributes, "bool3"));
    }

    public void testGetAsIntegerAttributesStringInt() {
        assertEquals(10, XMLAttributeHelper.getAsInteger(this.attributes, "int", 0));
        assertEquals(0, XMLAttributeHelper.getAsInteger(this.attributes, "int2", 0));
    }

    public void testGetAsIntegerAttributesString() throws Exception {
        assertEquals(10, XMLAttributeHelper.getAsInteger(this.attributes, "int"));
        
        try {
            assertEquals(0, XMLAttributeHelper.getAsInteger(this.attributes, "int2"));
            fail("A SAXException should be thrown");
        } catch (SAXException expected) { /* expected */ }
    }

    public static class BrokenSingleton {
		static final Object INSTANCE = new BrokenSingleton();

		private BrokenSingleton() {
			// just for instance
		}
    }
    
    public static class BrokenSingleton2 {
        public static BrokenSingleton2 getInstance() {
            throw new IllegalArgumentException("Dont try to call me !");
        }
    }

    /* ---------------------------------------------------------------------- *
     * Suite
     * ---------------------------------------------------------------------- */

    /** Return the suite of tests to execute.
     */
    public static Test suite() {
        return new TestSuite(TestXMLAttributeHelper.class);
        // return new BasicTestSetup(new TestXMLAttributeHelper("testSingleton"));
    }

    /** main function for direct testing.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
