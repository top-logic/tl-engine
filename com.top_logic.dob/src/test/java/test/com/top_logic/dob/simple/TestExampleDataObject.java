/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.simple;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.TLID;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.simple.ExampleDataObject;
import com.top_logic.dob.util.MetaObjectUtils;

/**
 * Test cases for the {@link com.top_logic.dob.simple.ExampleDataObject}.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestExampleDataObject extends TestCase {

    /**
     * Create a Test for the given (function-) name.
     *
     * @param name the (funxtion)-name of the test to perform.
     */
    public TestExampleDataObject (String name) {
        super (name);
    }
    
    public void testEquality() {
        ExampleDataObject e1 = new ExampleDataObject(10);
        e1.setAttributeValue("a", 1);
        e1.setAttributeValue("b", 2);
        e1.setAttributeValue("c", 3);
        
        ExampleDataObject e2 = new ExampleDataObject(10);
        
		final TLID id1 = e2.getIdentifier();
        
        e2.setAttributeValue("a", 1);
        e2.setAttributeValue("b", 2);
        e2.setAttributeValue("c", 3);
        
		final TLID id2 = e2.getIdentifier();
        
        assertEquals("Objects not equal.", e1, e2);
        assertEquals("Hashcodes differ.", e1.hashCode(), e2.hashCode());
        assertEquals("Identifier not stable.", id1, id2);
    }

    /** test the Variants of CTors, */
    public void testCTors() throws Exception  {
    
        Hashtable h1 = new Hashtable();
        h1.put("aaa", "aaa");
		h1.put("bbb", Integer.valueOf(17));
        h1.put("zzz", new Date(9999999));
    
        HashMap h2 = new HashMap();
        h2.put("aaa", "aaa");
		h2.put("bbb", Integer.valueOf(17));
        h2.put("zzz", new Date(9999999));
        
        String names [] =  { "aaa", "bbb"           , "zzz" };
		Object values[] = { "aaa", Integer.valueOf(17), new Date(9999999) };

        ExampleDataObject e1 = new ExampleDataObject(h1);
        ExampleDataObject e2 = new ExampleDataObject(h2);
        ExampleDataObject e3 = new ExampleDataObject(names, values);
        
        assertTrue(e1.equals(e1));

        assertTrue(e1.equals(e2));
        assertTrue(e1.equals(e3));
        assertTrue(e2.equals(e3));
    }
    
    /** test Meta functions of the ExampleDataObject, */
    public void testMeta() throws Exception  {
        
        HashMap ex = new HashMap();
        ex.put("aaa", "aaa");
		ex.put("bbb", Integer.valueOf(17));
        ex.put("nnn", null);
        ex.put("zzz", new Date(9999999));
        
        ExampleDataObject edo = new ExampleDataObject(ex);
        
        MetaObject meta   = edo.tTable();
        String     name[] = MetaObjectUtils.getAttributeNames(meta);
        List       lNames = Arrays.asList(name);
        assertTrue(lNames.contains("aaa"));
        assertTrue(lNames.contains("bbb"));
        assertTrue(lNames.contains("nnn"));
        assertTrue(lNames.contains("zzz"));

        name   = edo.getAttributeNames();
        lNames = Arrays.asList(name);
        assertTrue(lNames.contains("aaa"));
        assertTrue(lNames.contains("bbb"));
        assertTrue(lNames.contains("nnn"));
        assertTrue(lNames.contains("zzz"));
    }
    
    /** Tets null values in intial Map. */
    public void testNullEntries() throws Exception  {
    
        HashMap h = new HashMap();
        h.put("aaa", null);
        h.put(null , "And now ?");

        ExampleDataObject edo = new ExampleDataObject(h);
        
        MetaObject meta   = edo.tTable();
        String     name[] = MetaObjectUtils.getAttributeNames(meta);
        List       lNames = Arrays.asList(name);
        assertTrue(lNames.contains("aaa"));
        assertTrue(lNames.contains(null));

        name   = edo.getAttributeNames();
        lNames = Arrays.asList(name);
        assertTrue(lNames.contains("aaa"));
        assertTrue(lNames.contains(null));
        
        Object someStrangeObject = this;
        
        edo.setAttributeValue("aaa", "bbb");
        edo.setAttributeValue("aaa", someStrangeObject);

        edo.setAttributeValue(null, "bbb");
        edo.setAttributeValue(null, someStrangeObject);
    }



    /**
     * Test if the ExampleDataObject can handle setAttributeValue
     * with null values properly. (Due to a lasy init problem.)
     *
     * #author Michael Eriksson
     */
    public void testSetWithNull () throws Exception {
        final String theName = "test";

        Map theAttributeMap = new HashMap (1);
        theAttributeMap.put (theName, "value is gone later");

        DataObject theDO = new ExampleDataObject (theAttributeMap);

        // Quirks: Trigger creation of the meta object. 
        theDO.tTable();

        theDO.setAttributeValue (theName, null);

        assertTrue ("The attribute must be supported.",
                      MetaObjectUtils.hasAttribute(theDO.tTable (), theName));
    }

   /**
     * The suite of Test to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestExampleDataObject.class);
        // TestSuite suite = new TestSuite ();
        // suite.addTest(new TestExampleDataObject("testCTors"));
        return suite;
    }

    /**
     * Main fucntion for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
