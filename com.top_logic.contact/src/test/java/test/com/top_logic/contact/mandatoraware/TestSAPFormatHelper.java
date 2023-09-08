/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.contact.mandatoraware;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.contact.mandatoraware.SAPFormatHelper;

/**
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestSAPFormatHelper extends TestCase {

    /** 
     * Create a new TestSAPFormatHelper.
     */
    public TestSAPFormatHelper(String name) {
        super(name);
    }


    /**
     * Test method for {@link com.top_logic.contact.mandatoraware.SAPFormatHelper#fillSAPNo(java.lang.String)}.
     */
    public void testFillSAPNo() {
        assertNull  (SAPFormatHelper.fillSAPNo(null));
        assertEquals("0000000000"   , SAPFormatHelper.fillSAPNo(""));
        assertEquals("0000000012"   , SAPFormatHelper.fillSAPNo(    "00000012"));
        assertNull  (SAPFormatHelper.fillSAPNo("990000000000"));
    }

    /**
     * Test method for {@link com.top_logic.contact.mandatoraware.SAPFormatHelper#stripSAPNo(java.lang.String)}.
     */
    public void testStripSAPNo() {
        assertNull  (       SAPFormatHelper.stripSAPNo(null));
        assertEquals(""   , SAPFormatHelper.stripSAPNo(""));
        assertEquals("123", SAPFormatHelper.stripSAPNo("0123"));
        assertEquals(""   , SAPFormatHelper.stripSAPNo("000000000000"));
    }

    /*
    public void testGetDate() {
        fail("Not yet implemented");
    }

    public void testGetDouble() {
        fail("Not yet implemented");
    }

    public void testFormatPrice() {
        fail("Not yet implemented");
    }

    public void testFormatExchRate() {
        fail("Not yet implemented");
    }

    public void testFormatQuantity() {
        fail("Not yet implemented");
    }
    */

    /**
     * The method constructing a test suite for this class.
     *
     * @return    The test to be executed.
     */
    public static Test suite () {
        return new TestSuite(TestSAPFormatHelper.class);
    }

}

