/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.renderer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.tree.renderer.DefaultTableDeclaration;

/**
 * Test the {@link DefaultTableDeclaration}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestDefaultTableDeclaration extends TestCase {

    /** 
     * Creates a TestDefaultTableDeclaration for given test name.
     */
    public TestDefaultTableDeclaration(String name) {
        super(name);
    }

    /**
     * Test method for {@link DefaultTableDeclaration#DefaultTableDeclaration()}.
     */
    public void testEmptyDefaultTableDeclaration() {
        DefaultTableDeclaration dtd = new DefaultTableDeclaration();
        assertNull(dtd.getAccessor());

        assertTrue    (    dtd.getColumnNames().isEmpty());
		assertEquals("", dtd.getResourcePrefix().toPrefix());
        assertNotNull (    dtd.getResourceProvider());
        assertNull    (     dtd.getColumnDeclaration("testEmptyDefaultTableDeclaration"));
    }

    /**
	 * Test method for {@link DefaultTableDeclaration#DefaultTableDeclaration(ResPrefix, String[])}.
	 */
    public void testSimpleDefaultTableDeclaration() {
        DefaultTableDeclaration dtd = 
			new DefaultTableDeclaration(ResPrefix.forTest("testSimpleDefaultTableDeclaration"),
                    new String[] { "Eins", "Zwei", "Drei"});
        assertNotNull(dtd.getAccessor());

        assertEquals(4, dtd.getColumnNames().size()); // 3 + default
		assertEquals("testSimpleDefaultTableDeclaration.", dtd.getResourcePrefix().toPrefix());
        assertNotNull(dtd.getResourceProvider());
        assertNotNull(dtd.getColumnDeclaration("default"));
        assertNotNull(dtd.getColumnDeclaration("Drei"));

    }
    
    /**
     * the suite of Tests to execute 
     */
    static public Test suite () {       
        return new TestSuite(TestDefaultTableDeclaration.class);
    }

}

