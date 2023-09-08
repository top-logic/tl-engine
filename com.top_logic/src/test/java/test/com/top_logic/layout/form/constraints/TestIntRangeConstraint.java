/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.constraints;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.constraints.IntRangeConstraint;

/**
 * Tests the class {@link com.top_logic.layout.form.constraints.IntRangeConstraint}
 * 
 * @author     <a href="mailto:tma@top-logic.com">tma</a>
 */
public class TestIntRangeConstraint extends BasicTestCase {
	
	/**
	 * Tests if the {@link com.top_logic.layout.form.Constraint#check(Object)}check method works as expected.
	 *  
	 * @throws CheckException indicates a failure, so we will let JUnit handle this
	 */
	public void testCheckLowerAndUpperBound() throws CheckException{
		IntRangeConstraint constraint = new IntRangeConstraint(10,100);
		
		// positive tests - should all be accepted
		
		assertTrue(constraint.check(Integer.valueOf(10)));
		assertTrue(constraint.check(Long.valueOf(11)));
		assertTrue(constraint.check(Integer.valueOf(99)));
		assertTrue(constraint.check(Double.valueOf(99.999999999999)));
		
		
		// negative tests - using values out of the boundaries
		try {
			constraint.check(Integer.valueOf(1));
			fail("Expected CheckException");
		} catch (CheckException e) {
			//expected
		}

		try {
			constraint.check(Integer.valueOf(101));
			fail("Expected CheckException");
		} catch (CheckException e) {
			//expected
		}
	}
	
	public void testLowerBound() throws Exception {
		IntRangeConstraint constraint = new IntRangeConstraint(10, null);

		try {
			constraint.check(Integer.valueOf(11));
		} catch (CheckException e) {
			fail("Constraint has to accept values greater than lower bound!");
		}
		try {
			constraint.check(Integer.valueOf(10));
		} catch (CheckException e) {
			fail("Constraint has to accept values equal to lower bound!");
		}
		try {
			constraint.check(Integer.valueOf(9));
			fail("Constraint must fail for values smaller than lower bound!");
		} catch (CheckException e) {
			// expected
		}
	}

	public void testUpperBound() throws Exception {
		IntRangeConstraint constraint = new IntRangeConstraint(null, 10);
		try {
			constraint.check(Integer.valueOf(9));
		} catch (CheckException e) {
			fail("Constraint has to accept values smaller than upper bound!");
		}
		try {
			constraint.check(Integer.valueOf(10));
		} catch (CheckException e) {
			fail("Constraint has to accept values equal to upper bound!");
		}
		try {
			constraint.check(Integer.valueOf(11));
			fail("Constraint must fail for values greater than upper bound!");
		} catch (CheckException e) {
			// expected
		}
	}

    /**
     * Tests if the {@link com.top_logic.layout.form.Constraint#check(Object)}check method works as expected.
     *  
     * @throws CheckException indicates a failure, so we will let JUnit handle this
     */
    public void testUnexpected() throws CheckException{
        IntRangeConstraint constraint = new IntRangeConstraint(10,100);
        
        // This should result in a class cast expection
        try {
            constraint.check("aaa");
            fail("Expected ClassCastException");
        } catch (ClassCastException e) {
            //expected
        }

        try {
            constraint.check(this);
            fail("Expected ClassCastException");
        } catch (ClassCastException e) {
            //expected
        }
    }

    /**
	 * Returns the test suite
	 * @return the test suite
	 */
    public static Test suite () {
        TestSuite theSuite  = new TestSuite(TestIntRangeConstraint.class);
        return TLTestSetup.createTLTestSetup(theSuite);
    }
	
    /**
     * The main method
     */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
	
}
