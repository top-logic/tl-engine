/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.DataObjectMapAdaptor;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.data.DefaultDataObject;
import com.top_logic.dob.meta.MOStructureImpl;
import com.top_logic.dob.simple.FileDataObject;

/**
 * Test the {@link DataObjectMapAdaptor}
 *  
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestDataObjectMapAdaptor extends TestCase {

    /** 
     * Creates a {@link TestDataObjectMapAdaptor}.
     */
    public TestDataObjectMapAdaptor(String name) {
        super(name);
    }

    /**
     * Test DataObjectMapAdaptor with an empty Meta/DataObejct,
     */
    public void testEmptyMapping() {
        
        MetaObject        emptyMo = new MOStructureImpl("empty");
        DefaultDataObject emptyDo = new DefaultDataObject(emptyMo);
        
        DataObjectMapAdaptor doma = (DataObjectMapAdaptor) DataObjectMapAdaptor.MAPPING.map(emptyDo);
        
        assertNull(doma.get(null));
        assertNull(doma.get(this));
        assertNull(doma.get("any"));

        assertFalse(doma.containsKey(null));
        assertFalse(doma.containsKey(this));
        assertFalse(doma.containsKey("any"));
        
        assertFalse(doma.containsValue(null));
        assertFalse(doma.containsValue(this));
        assertFalse(doma.containsValue("any"));

        assertTrue  (   doma.isEmpty());
        assertEquals(0, doma.size());

        try {
            doma.put(null, "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) { /* expected */ }
        try {
            doma.put(this, doma);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) { /* expected */ }
        try {
            doma.put("any", "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) { /* expected */ }
        
        // No way to really put anything here, ok

        doma.putAll(Collections.EMPTY_MAP);
       
        try {
            doma.putAll(Collections.singletonMap("a", "b"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) { /* expected */ }
        
        assertTrue(doma.keySet()  .isEmpty());
        assertTrue(doma.entrySet().isEmpty());
        assertTrue(doma.values()  .isEmpty());
        
    }

    /**
     * Test the unsupported operations only.
     */
    public void testUnsupported() {
        DataObjectMapAdaptor doma = (DataObjectMapAdaptor) DataObjectMapAdaptor.MAPPING.map(null);
        try {
            doma.clear();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) { /* expected */ }
        try {
            doma.remove(this);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) { /* expected */ }

    }
    
    /**
     * Test DataObjectMapAdaptor with a a FileDataObject
     */
    public void testFileDataObjectMapping() {
        
		File file =
			new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/dob/TestDataObjectMapAdaptor.java");
        FileDataObject       fdo  = new FileDataObject(file);
        DataObjectMapAdaptor doma = (DataObjectMapAdaptor) DataObjectMapAdaptor.MAPPING.map(fdo);
        
 
        assertEquals(file.getName()                 , doma.get((Object) "name"));
        assertEquals(Boolean.valueOf(file.canRead()), doma.get((Object) "isReadable"));
		assertEquals(Long.valueOf(file.length()), doma.get((Object) "length"));
		assertEquals(Long.valueOf(file.lastModified()), doma.get((Object) "lastModified"));

        assertTrue(doma.containsKey("name"));
        assertTrue(doma.containsKey("isReadable"));
        assertTrue(doma.containsKey("length"));
        assertTrue(doma.containsKey("lastModified"));
        
        assertTrue(doma.containsValue(file.getName()));
        assertTrue(doma.containsValue(Boolean.TRUE));
		assertTrue(doma.containsValue(Long.valueOf(file.length())));
		assertTrue(doma.containsValue(Long.valueOf(file.lastModified())));

        assertFalse (  doma.isEmpty());
        assertEquals(9,doma.size());

        try {
            doma.put("name", "value");
            fail("Expected IllegalArgumentException");
		} catch (UnsupportedOperationException expected) {
			/* expected */
		}
        
        // No way to really put anything here, ok

        try {
            doma.putAll(Collections.singletonMap("name", "wont work"));
            fail("Expected IllegalArgumentException");
		} catch (UnsupportedOperationException expected) {
			/* expected */
		}
        
        assertTrue(doma.keySet().contains("name"));
        assertTrue(doma.values().contains(file.getName()));

        assertEquals(9, doma.entrySet().size());
    }

    /**
     * Test DataObjectMapAdaptor with a simple DataObject
     */
    public void testSimpleDataObject() throws DataObjectException {
        
        MOStructureImpl     theMO = new MOStructureImpl("simple");
        theMO.addAttribute( new MOAttributeImpl ("name", MOPrimitive.STRING , false));
        theMO.addAttribute( new MOAttributeImpl ("len" , MOPrimitive.INTEGER, false));
        theMO.freeze();
        
        DefaultDataObject  theDo = new DefaultDataObject(theMO);
        
        final String  name    = "testSimpleDataObject";
		final Integer len = Integer.valueOf(42);
        final String  newName = "aber Hallo";
		final Integer newLen = Integer.valueOf(31415927);
        
        theDo.setAttributeValue("name", name);
        theDo.setAttributeValue("len" , len);
        
        DataObjectMapAdaptor doma = (DataObjectMapAdaptor) DataObjectMapAdaptor.MAPPING.map(theDo);
 
        assertEquals(name, doma.get("name"));
        assertEquals(len , doma.get("len"));
        
        assertTrue(doma.containsKey("name"));
        assertTrue(doma.containsKey("len"));
        
        assertTrue(doma.containsValue(name));
        assertTrue(doma.containsValue(len));

        assertFalse (  doma.isEmpty());
        assertEquals(2,doma.size());

        doma.put("name", newName);
        assertEquals(newName, theDo.getAttributeValue("name"));

        assertTrue(doma.keySet().contains("name"));
        assertTrue(doma.values().contains(newName));

        Iterator entries = doma.entrySet().iterator();
        Map.Entry entry = (Map.Entry) entries.next();
        
        assertEquals("name"  , entry.getKey());
        assertEquals(newName , entry.getValue());
        
        entry = (Map.Entry) entries.next();
        
        assertEquals("len", entry.getKey());
        assertEquals(len   ,entry.getValue());
        
        entry.setValue(newLen);
        assertEquals(newLen, theDo.getAttributeValue("len"));
    }

    /**
     * the suite of tests to perform.
     */
    public static Test suite () {
        return new TestSuite (TestDataObjectMapAdaptor.class);
        // return new TestDataObjectMapAdaptor("testClasses");
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        TestRunner.run (suite ());
    }
}

