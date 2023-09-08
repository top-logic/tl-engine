/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.MapComparator;

/**
 * Test The {@link MapComparator}
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestMapComparator extends TestCase {

    /** 
     * Create a new TestMapComparator.
     */
    public TestMapComparator(String aName) {
        super(aName);
    }

    /**
     * Main and single testcase for now.
     */
    public void testMain() {
        
        Map m1 = new HashMap();
		m1.put("Kai", Integer.valueOf(1));
		m1.put("Anna", Integer.valueOf(2));
        
        Map m2 = new HashMap();

		m2.put("Kai", Integer.valueOf(1));
		m2.put("Anna", Integer.valueOf(0));
        
        MapComparator mc1 = new MapComparator("Kai");
        
        assertEquals(0, mc1.compare(m1, m2));
        assertEquals(0, mc1.compare(m2, m1));

        MapComparator mc2 = new MapComparator("Anna");

        assertTrue( mc2.compare(m1, m2) > 0);
        assertTrue( mc2.compare(m2, m1) < 0);
        
        MapComparator mc3 = new MapComparator("Anna", /* invert */ true);

        assertTrue( mc3.compare(m1, m2) < 0);
        assertTrue( mc3.compare(m2, m1) > 0);
    }

   /** Return the suite of tests to execute.
    */
    public static Test suite () {
        return new TestSuite(TestMapComparator.class);
    }

}

