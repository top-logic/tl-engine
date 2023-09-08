/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.top_logic.basic.StringID;

/**
 * Tests for {@linkplain StringID}.
 * 
 * @author    <a href=mailto:cdo@top-logic.com>cdo</a>
 */
public class TestStringID extends BasicTestCase {

    /* ---------------------------------------------------------------------- *
     * Tests
     * ---------------------------------------------------------------------- */
    public void testCreateId() {
        int theTestCount = 10000;
        HashSet theSet = new HashSet(theTestCount);
        
        for (int i = 0; i < theTestCount; i++) {
            theSet.add(StringID.createRandomID());
        }
        
        /* If the test fails, there have been equal entries in the set. */
        assertEquals(theTestCount, theSet.size());
        
    }
    
    /* ---------------------------------------------------------------------- *
     * Suite
     * ---------------------------------------------------------------------- */

    /**
     * The method constructing a test suite for this class.
     *
     * @return    The test to be executed.
     */
    public static Test suite() {
        return BasicTestSetup.createBasicTestSetup(new TestSuite(TestStringID.class));
    }

    /**
     * The main program for executing this test also from console.
     *
     * @param    args    Will be ignored.
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}
