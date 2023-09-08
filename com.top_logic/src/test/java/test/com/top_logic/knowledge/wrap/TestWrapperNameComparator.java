/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.util.Comparator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.dummy.DummyWrapper;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.WrapperNameComparator;
import com.top_logic.model.TLObject;

/**
 * Testcase for the {@link WrapperNameComparator}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TestWrapperNameComparator extends BasicTestCase {

    /** 
     * Constructor for a special test.
     *
     * @param name function name of the test to execute.
     */
    public TestWrapperNameComparator(String name) {
        super(name);
    }

	/**
	 * Test Ticket #20390.
	 */
	public void testTransitivity() {
		Comparator<TLObject> name = WrapperNameComparator.getInstanceNullsafe();
		List<? extends TLObject> objects = TestWrapperComparatorTransitivity.createTestObjects();
		TestWrapperComparatorTransitivity.assertTransitivity(name, objects);
	}

	/**
	 * Test ticket #21022 *
	 */
	public void testEquality() {
		String wrapperName = "A";
		TLObject wrapper1 = createDummyWrapper(wrapperName);
		TLObject wrapper2 = createDummyWrapper(wrapperName);
		assertEquals(0, WrapperNameComparator.getInstance().compare(wrapper1, wrapper2));
	}

	private DummyWrapper createDummyWrapper(String wrapperName) {
		DummyWrapper wrapper = DummyWrapper.obj();
		wrapper.setName(wrapperName);
		return wrapper;
	}

    /**
     * Return the suite of tests to perform. 
     */
    public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(new TestSuite(TestWrapperNameComparator.class));
    }

    /** Main function for direct testing.
     */
    public static void main(String[] args) {
        Logger.configureStdout();
        KBSetup.setCreateTables(false);
        
        junit.textui.TestRunner.run(suite());
    }
    

}
