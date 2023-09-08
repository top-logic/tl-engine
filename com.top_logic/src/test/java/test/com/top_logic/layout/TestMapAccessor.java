/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.layout.Accessor;
import com.top_logic.layout.MapAccessor;
import com.top_logic.layout.ReadOnlyMapAccessor;

/**
 * Test the {@link MapAccessor}.
 * 
 * @author    <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class TestMapAccessor extends TestCase {

    /** 
     * Create a new TestMapAccessor with aName,
     */
    public TestMapAccessor(String aName) {
        super(aName);
    }

    /**
     * Test method for {@link com.top_logic.layout.MapAccessor#MapAccessor(com.top_logic.layout.Accessor)}.
     */
    public void testMapAccessorAccessor() {
        MapAccessor complex = new MapAccessor(MapAccessor.INSTANCE);
        assertNull (complex.getValue(null, null));
        assertNull (complex.getValue(null, "anything"));
        assertNull (complex.getValue("anything", null));

        Map values = new HashMap();
        values.put("eins", "1");
        values.put("zwei", "2");
        values.put("drei", "3");
        
        Map inner = new HashMap();
        inner.put("hü",   "->");
        inner.put("hott", "<-");
        values.put(MapAccessor.THIS_ATTR, inner);

        assertNull (complex.getValue(values, null));
        assertNull (complex.getValue(values, "notThere"));
        assertEquals("1", complex.getValue(values, "eins"));
        assertEquals("2", complex.getValue(values, "zwei"));
        assertEquals("3", complex.getValue(values, "drei"));
        assertEquals("->", complex.getValue(values, "hü"));
        assertEquals("<-", complex.getValue(values, "hott"));
        
        complex.setValue(values, "eins", "I");
        complex.setValue(values, "zwei", "II");
        assertEquals("I", complex.getValue(values, "eins"));
        assertEquals("II", complex.getValue(values, "zwei"));
        
    }

    /**
     * Test method for {@link com.top_logic.layout.MapAccessor#MapAccessor()}.
     */
    public void testMapAccessor() {
        MapAccessor simple = MapAccessor.INSTANCE;
        assertNull (simple.getValue(null, null));
        assertNull (simple.getValue(null, "anything"));
        assertNull (simple.getValue("anything", null));
        
        Map values = new HashMap();
        values.put("eins", "1");
        values.put("zwei", "2");
        values.put("drei", "3");
        assertNull (simple.getValue(values, null));
        assertNull (simple.getValue(values, "notThere"));
        assertEquals("1", simple.getValue(values, "eins"));
        assertEquals("2", simple.getValue(values, "zwei"));
        assertEquals("3", simple.getValue(values, "drei"));

        simple.setValue(values, "eins", "I");
        simple.setValue(values, "zwei", "II");
        assertEquals("I", simple.getValue(values, "eins"));
        assertEquals("II", simple.getValue(values, "zwei"));
    }
    
    public void testReadOnlyMapAccessor() {
		Accessor simple = ReadOnlyMapAccessor.INSTANCE;
		assertNull(simple.getValue(null, null));
		assertNull(simple.getValue(null, "anything"));
		assertNull(simple.getValue("anything", null));

		Map values = new HashMap();
		values.put("eins", "1");
		values.put("zwei", "2");
		values.put("drei", "3");
		assertNull(simple.getValue(values, null));
		assertNull(simple.getValue(values, "notThere"));
		assertEquals("1", simple.getValue(values, "eins"));
		assertEquals("2", simple.getValue(values, "zwei"));
		assertEquals("3", simple.getValue(values, "drei"));
		try {
			simple.setValue(values, "eins", "I");
			simple.setValue(values, "zwei", "II");
		} catch (RuntimeException e) {
			// can be happen since it is a read only Accessor
		}
		assertEquals("1", simple.getValue(values, "eins"));
		assertEquals("2", simple.getValue(values, "zwei"));
	}

    /**
     * the suite of Tests to execute 
     */
    static public Test suite () {       
        return new TestSuite(TestMapAccessor.class);
    }

    /**
     * main function for direct testing.
     */
    static public void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}

