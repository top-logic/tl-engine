/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.xml.MapBasedXMLAttributes;
import com.top_logic.basic.xml.XMLAttributeHelper;

/**
 * Test the MapBasedXMLAttributes.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestMapBasedXMLAttributes extends TestCase {

    public TestMapBasedXMLAttributes(String name) {
        super(name);
    }

    /**
     * Test MapBasedXMLAttributes for an empty map.
     */
    public void testEmpty() {
        MapBasedXMLAttributes mbxa = new MapBasedXMLAttributes(Collections.EMPTY_MAP);
        assertEquals(-1, mbxa.getIndex("any"));
        assertEquals(-1, mbxa.getIndex(null, "any"));
        assertEquals(0 , mbxa.getLength());
        assertNull  (mbxa.getLocalName(0));
        assertNull  (mbxa.getLocalName(99));
        assertNull  (mbxa.getLocalName(-10));
        assertNull  (mbxa.getQName(0));
        assertNull  (mbxa.getQName(99));
        assertNull  (mbxa.getQName(-10));
        // Not much to test here
        mbxa.getType(0);          
        mbxa.getType(null);        
        mbxa.getType(null, null);  
        mbxa.getType(null, null);  
        
        assertNull  (mbxa.getURI(0));
        assertNull  (mbxa.getURI(99));
        assertNull  (mbxa.getURI(-10));

        assertNull  (mbxa.getValue(0));
        assertNull  (mbxa.getValue(99));
        assertNull  (mbxa.getValue(-10));

        assertNull  (XMLAttributeHelper.getAsStringOptional(mbxa, null));
        assertNull  (XMLAttributeHelper.getAsStringOptional(mbxa, ""));
        assertNull  (XMLAttributeHelper.getAsStringOptional(mbxa, "any"));

        assertNull  (mbxa.getValue(null, null));
        assertNull  (mbxa.getValue(null, ""));
        assertNull  (mbxa.getValue(null, "any"));
    }
    
    /**
     * Test MapBasedXMLAttributes for some simpel Attributes.
     */
    public void testNormal() {
        Map attrs = new LinkedHashMap();
        attrs.put("name", "value");
        attrs.put("date", "now");
        attrs.put("empty", "");
        
        MapBasedXMLAttributes mbxa = new MapBasedXMLAttributes(attrs);
        assertEquals( 1, mbxa.getIndex("date"));
        assertEquals(-1, mbxa.getIndex(null, "any"));
        assertEquals(3 , mbxa.getLength());
        assertEquals("name",mbxa.getLocalName(0));
        assertNull  (mbxa.getLocalName(99));
        assertNull  (mbxa.getLocalName(-10));
        assertEquals("",mbxa.getQName(2));
        assertNull  (mbxa.getQName(99));
        assertNull  (mbxa.getQName(-10));
        // Not much to test here
        mbxa.getType(2);          
        mbxa.getType(null);        
        mbxa.getType(null, null);  
        mbxa.getType(null, null);  
        
        assertEquals  ("", mbxa.getURI(0));
        assertNull  (mbxa.getURI(99));
        assertNull  (mbxa.getURI(-10));

        assertEquals("value" , mbxa.getValue(0));
        assertNull  (mbxa.getValue(99));
        assertNull  (mbxa.getValue(-10));

        assertEquals("now" , XMLAttributeHelper.getAsStringOptional(mbxa, "date"));
        assertNull  (XMLAttributeHelper.getAsStringOptional(mbxa, ""));
        assertNull  (XMLAttributeHelper.getAsStringOptional(mbxa, "any"));

        assertNull  (mbxa.getValue(null, null));
        assertNull  (mbxa.getValue(null, ""));
        assertNull  (mbxa.getValue(null, "any"));
    }

    /**
     * the suite of tests to execute.
     */
    public static Test suite () {
        return new TestSuite (TestMapBasedXMLAttributes.class);
    }


}

