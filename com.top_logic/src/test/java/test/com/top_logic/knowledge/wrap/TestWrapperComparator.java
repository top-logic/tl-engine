/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.util.Comparator;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.WrapperComparator;
import com.top_logic.model.TLObject;

/**
 * Testcase for the {@link WrapperComparator}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TestWrapperComparator extends BasicTestCase {

    /** 
     * Constructor for a special test.
     *
     * @param name function name of the test to execute.
     */
    public TestWrapperComparator(String name) {
        super(name);
    }

	/**
	 * Test Ticket #20390.
	 */
	public void testTransitivity() {
		Comparator name = WrapperComparator.ID_COMPARATOR;
		List<? extends TLObject> objects = TestWrapperComparatorTransitivity.createTestObjects();
		TestWrapperComparatorTransitivity.assertTransitivity(name, objects);
	}

	/**
     * Return the suite of tests to perform. 
     */
    public static Test suite() {
		return KBSetup.getKBTest(TestWrapperComparator.class);
    }

    /** Main function for direct testing.
     */
    public static void main(String[] args) {
        Logger.configureStdout();
        KBSetup.setCreateTables(false);
        
        junit.textui.TestRunner.run(suite());
    }
    

}
