/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.services.simpleajax;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;

import junit.framework.Test;
import junit.textui.TestRunner;

import org.xml.sax.InputSource;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.StringWriterNonNull;

import com.top_logic.base.services.simpleajax.XMLValueDecoder;
import com.top_logic.base.services.simpleajax.XMLValueEncoder;
import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.sax.SAXUtil;

/**
 * Test the {@link XMLValueEncoder} and {@link XMLValueDecoder}.
 * 
 * TODO KHA/BHU assert valid XML (including xsd)
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestXMLValueEncoding extends BasicTestCase {

    public TestXMLValueEncoding(String aName) {
        super(aName);
    }

    /**
     * Test encoding the hard way, every Bean counts.
     */
    public void testBeanEncoding() throws Exception {
        TagWriter       out  = new TagWriter();
        XMLValueEncoder xenc = new XMLValueEncoder(out);
        xenc.encode(this);
        
        String xml = out.toString();
		/* assertEquals("<object xmlns=\"http://top-logic.com/ns/xml-value\">" +
		 * "<property><name>name</name>" + "<string>testBeanEncoding</string>" + "</property>" +
		 * "</object>", out.toString()); */
         XMLValueDecoder xdec = new XMLValueDecoder();
         parseXML(xml, xdec);
         Map result = (Map) xdec.getObject();
         
         assertEquals("testBeanEncoding", result.get("name"));
    }

    /**
     * Test encoding allowed types.
     */
    public void testSimpleEncodings() throws Exception {
        StringWriter    sout = new StringWriterNonNull();
        TagWriter       out  = new TagWriter(sout);
        XMLValueEncoder xenc = new XMLValueEncoder(out);
        
        Object os[] = new Object[] {
                Character.valueOf('\uFEFE'),
                null,
                "TestXMLValueEncoder.testSimpleEncodingsÄöüß §3",
                Boolean.TRUE,
                Byte.valueOf((byte) 0xFF),
                Short.valueOf((short) -17),
                Integer.valueOf(Integer.MAX_VALUE),
                Long.valueOf(Integer.MIN_VALUE), // TODO KHA/BHU known Bug
                Double.valueOf(Double.NEGATIVE_INFINITY),
                Float.valueOf((float)Math.PI),
                /* Those wont work,  as BeanProperties wont help here
                new BigInteger("123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ", 36),
                new BigDecimal(Math.E)
                */
        };        
        
        XMLValueDecoder  xdec       = new XMLValueDecoder();
		SAXParser theParser = SAXUtil.newSAXParserNamespaceAware();
        
        Object results[] = new Object[] { // Some Object "loose type" while encoding
                "\uFEFE",
                null,
                "TestXMLValueEncoder.testSimpleEncodingsÄöüß §3",
                Boolean.TRUE,
				Integer.valueOf((byte) 0xFF),
				Integer.valueOf(-17),
				Integer.valueOf(Integer.MAX_VALUE),
				Integer.valueOf(Integer.MIN_VALUE),
				Double.valueOf(Double.NEGATIVE_INFINITY),
				Double.valueOf(3.1415927), // Pi is transfered as float only
        };        

        for (int i = 0; i < os.length; i++) {
            Object o = os[i];
            xenc.encode(o);
            theParser.parse(new InputSource(new StringReader(sout.toString())), xdec);
            sout.getBuffer().setLength(0);
            Object result = xdec.getObject();
            assertEquals("#" + i, results[i], result);

        }
		/* String found = out.toString(); String expected =
		 * "<null xmlns=\"http://top-logic.com/ns/xml-value\" />" +
		 * "<string xmlns=\"http://top-logic.com/ns/xml-value\">TestXMLValueEncoder.testSimpleEncodingsÄöüß §3</string>"
		 * + "<boolean xmlns=\"http://top-logic.com/ns/xml-value\">true</boolean>" +
		 * "<string xmlns=\"http://top-logic.com/ns/xml-value\">\uFEFE</string>" +
		 * "<int xmlns=\"http://top-logic.com/ns/xml-value\">-1</int>" +
		 * "<int xmlns=\"http://top-logic.com/ns/xml-value\">-17</int>" +
		 * "<int xmlns=\"http://top-logic.com/ns/xml-value\">2147483647</int>" +
		 * "<int xmlns=\"http://top-logic.com/ns/xml-value\">-9223372036854775808</int>" +
		 * "<float xmlns=\"http://top-logic.com/ns/xml-value\">-Infinity</float>" +
		 * "<float xmlns=\"http://top-logic.com/ns/xml-value\">3.1415927</float>";
		 * assertEquals(expected, found); */
    }

    /**
     * Test Enconding Arrays and Maps forming cyles.
     */
    public void testCycles() throws Exception {
        
        TagWriter       out  = new TagWriter();
        XMLValueEncoder xenc = new XMLValueEncoder(out);

        List   list    = new ArrayList();
        Map    map     = new LinkedHashMap() { 
            // default hashCode of Map will result in endless recursion ;-)
            @Override
			public int hashCode() { return 17; }
        };
        Object array[] = new Object[3];
        
		list.add(Integer.valueOf(77));
        list.add(map);
        list.add(null);
        
        map .put("testCycles-Key1", Boolean.FALSE);
        map .put("testCycles-Key2", list);
        map .put("testCycles-Key3", null);
        
        array[0] = array;
        array[1] = list;
        array[2] = map;
        
        xenc.encode(list);
        xenc.encode(map);
        xenc.encode(array);
        
        String xml    = out.toString();
        
        String expected = 
				"<array xmlns=\"http://top-logic.com/ns/xml-value\">"
					+ "<int>77</int>"
					+ "<object>"
					+ "<property><name>testCycles-Key1</name>"
					+ "<boolean>false</boolean>"
					+ "</property>"
					+ "<property><name>testCycles-Key2</name>"
					+ "<ref id=\"0\"/>"
					+ "</property>"
					+ "<property><name>testCycles-Key3</name>"
					+ "<null/>"
					+ "</property>"
					+ "</object>"
					+ "<null/>"
					+ "</array>"
					+ "<ref xmlns=\"http://top-logic.com/ns/xml-value\" id=\"1\"/>"
					+ "<array xmlns=\"http://top-logic.com/ns/xml-value\">"
					+ "<ref id=\"2\"/>"
					+ "<ref id=\"0\"/>"
					+ "<ref id=\"1\"/>"
					+ "</array>";
        
        // System.out.println(xml);

        assertEquals(expected, xml);
 
    }

    /**
     * Test Enconding Maps whith non-Strings as keys.
     */
	public void testMaps() {
        
        TagWriter       out  = new TagWriter();
        XMLValueEncoder xenc = new XMLValueEncoder(out);

        try {
            xenc.encode( Collections.singletonMap(Boolean.TRUE, null));
            fail("Expected IOException");
        }
        catch (IOException expected) { /* expected */ }

        try {
            xenc.encode( Collections.singletonMap(null, null));
            fail("Expected IOException");
        }
        catch (IOException expected) { /* expected */ }
 
    }

    /**
     * Test method for {@link com.top_logic.base.services.simpleajax.XMLValueEncoder#reset()}.
     * 
     * In addition the reference decoding in the {@link XMLValueDecoder} is tete.
     */
    public void testReset() throws Exception {
        StringWriter    sout = new StringWriterNonNull();
        TagWriter       out  = new TagWriter(sout);
        XMLValueEncoder xenc = new XMLValueEncoder(out);
        XMLValueDecoder xdec = new XMLValueDecoder();

        List   list    = new ArrayList();
        Map    map     = new HashMap() { 
            // default hashCode of Map will result in endless recursion ;-)
            @Override
			public int hashCode() { return 17; }
        };
        Object array[] = new Object[3];
        
		list.add(Integer.valueOf(77));
        list.add(map);
        list.add(null);
        
        map .put("testCycles-Key1", Boolean.FALSE);
        map .put("testCycles-Key2", list);
        map .put("testCycles-Key3", null);
        
        array[0] = array;
        array[1] = list;
        array[2] = map;
        
        xenc.encode(list);
        parseXML(sout.toString(), xdec);
        List lresult = (List) xdec.getObject();

        assertEquals(3               , lresult.size());
		assertEquals(Integer.valueOf(77), lresult.get(0));
        Map mresult = (Map)            lresult.get(1);
        assertNull  (                  lresult.get(2));

        assertEquals(Boolean.FALSE , mresult.get("testCycles-Key1"));
        assertSame  (lresult       , mresult.get("testCycles-Key2"));
        assertNull  (                mresult.get("testCycles-Key3"));
         
        xenc.reset();   sout.getBuffer().setLength(0);
        xenc.encode(map);
        parseXML(sout.toString(), xdec);
        mresult = (Map) xdec.getObject();
        
        assertEquals(3, mresult.size());

        assertEquals(Boolean.FALSE , mresult.get("testCycles-Key1"));
                    lresult = (List) mresult.get("testCycles-Key2");
        assertNull  (                mresult.get("testCycles-Key3"));

		assertEquals(Integer.valueOf(77), lresult.get(0));
        // Will die recurive death in toString() ;-)
        // assertSame  (mresult         , lresult.get(1));
        assertNull  (                  lresult.get(2));
        
        xenc.reset();   sout.getBuffer().setLength(0);
        
        xenc.encode(array);
        parseXML(sout.toString(), xdec);
        lresult = (List) xdec.getObject();
        
        assertEquals(3, lresult.size());
    }

    /** 
     * Return the suite of Tests to execute.
     */
    public static Test suite() {
        return TLTestSetup.createTLTestSetup(TestXMLValueEncoding.class);
        // return new TestXMLValueEncoding("testReset");
    }

    /** 
     * Main function for direct execution.
     */
    public static void main (String[] args) throws IOException {
        Logger.configureStdout();
        TestRunner.run(suite());
    }

}

