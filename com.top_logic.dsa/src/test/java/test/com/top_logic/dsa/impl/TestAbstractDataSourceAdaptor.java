/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dsa.impl;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.dsa.DSATestSetup;

import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.dsa.impl.AbstractDataSourceAdaptor;

/**
 * Testcase for {@link com.top_logic.dsa.impl.AbstractDataSourceAdaptor}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class TestAbstractDataSourceAdaptor extends TestCase {

	/**
     * Test the methods implemented by AbstractDataSourceAdaptor
     */
    public void testImplemented() throws DatabaseAccessException {
        AbstractDataSourceAdaptor adsa = new AbstractDataSourceAdaptor() {
		};
        
        adsa.close();
        assertEquals(null, adsa.getMimeType("blurb.txt"));

        assertFalse(adsa.isPrivate());
        assertFalse(adsa.isStructured());
        assertFalse(adsa.isRepository());

        assertFalse(adsa.isContainer(null));
        assertFalse(adsa.isEntry    (null));
        
        assertNull(adsa.getProtocol());
        adsa.setProtocol("banana");
        assertEquals("banana", adsa.getProtocol());
        
        assertNull(adsa.getVersions(null));
    }

    /**
     * the suite of tests to execute.
     */
    public static Test suite () {
        
        TestSuite suite = new TestSuite (TestAbstractDataSourceAdaptor.class);
        
        return DSATestSetup.createDSATestSetup(suite);
    }
}
