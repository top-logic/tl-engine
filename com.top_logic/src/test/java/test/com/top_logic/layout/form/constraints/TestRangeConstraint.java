/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.constraints;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.constraints.RangeConstraint;

/**
 * Testcase for the new RangeConstraint.
 * 
 * @author     <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestRangeConstraint extends BasicTestCase {

    /**
     * Test throwing of IllegalArgumentExceptions on CTor.
     */
    public void testIllegal() {
        
        Integer lower = null;
        Integer upper = null;
        
        try {
            new RangeConstraint(lower, upper);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expect) { /* expected */ }
        
		lower = Integer.valueOf(100);
		upper = Integer.valueOf(-100);
        try {
            new RangeConstraint(lower, upper);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expect) { /* expected */ }

    }

    /** 
     * Test using the RangeConstraint as expected 
     */
    public void testCheck() throws CheckException {
        RangeConstraint rc = new RangeConstraint(
			Integer.valueOf(0), Integer.valueOf(100));
        
		assertTrue(rc.check(Integer.valueOf(50)));
        try {
			rc.check(Integer.valueOf(-10));
            fail("Expected CheckException");
        } catch (CheckException expected) { /* expected */ }
        try {
			rc.check(Integer.valueOf(110));
            fail("Expected CheckException");
        } catch (CheckException expected) { /* expected */ }
        assertTrue(rc.check(null));
    }

    /** 
     * Test using lower bound only.
     */
    public void testLower() throws CheckException {
        RangeConstraint rc = new RangeConstraint(
			Integer.valueOf(0), null);
        
		assertTrue(rc.check(Integer.valueOf(50)));
		assertTrue(rc.check(Integer.valueOf(Integer.MAX_VALUE)));
        try {
			rc.check(Integer.valueOf(-10));
            fail("Expected CheckException");
        } catch (CheckException expected) { /* expected */ }
    }

    /** 
     * Test using upper bound only.
     */
    public void testUpper() throws CheckException {
        RangeConstraint rc = new RangeConstraint(null , "ZZZ");
        
        assertTrue(rc.check("AAA"));
        assertTrue(rc.check("Kabelsalat"));
        try {
            rc.check("zu Groﬂ");
            fail("Expected CheckException");
        } catch (CheckException expected) { /* expected */ }
    }

	/**
	 * Test mit exlusiven Grenzen.
	 */
	public void testExclusiveBounds() throws CheckException {
		RangeConstraint rc = new RangeConstraint(0.0, 10.0);
		rc.setUpperInclusive(false);
		rc.setLowerInclusive(false);
		rc.setConversion(new Mapping<Number, Double>() {
			@Override
			public Double map(Number input) {
				if (input == null) {
					return null;
				}
				return input.doubleValue();
			}
		});

		assertTrue(rc.check(0.01));
		assertTrue(rc.check(1L));
		assertTrue(rc.check(9.99));
		try {
			rc.check(0.0);
			fail("Expected failure.");
		} catch (CheckException expected) {
			// Expected.
			assertResourceDefined(expected);
		}
		try {
			rc.check(0);
			fail("Expected failure.");
		} catch (CheckException expected) {
			// Expected.
			assertResourceDefined(expected);
		}
		try {
			rc.check(10.0);
			fail("Expected failure.");
		} catch (CheckException expected) {
			// Expected.
			assertResourceDefined(expected);
		}
		try {
			rc.check(10);
			fail("Expected failure.");
		} catch (CheckException expected) {
			// Expected.
			assertResourceDefined(expected);
		}
	}

	private void assertResourceDefined(CheckException expected) {
		assertFalse(expected.getMessage().contains("["));
	}

    /** 
     * Test mixing comparables (which will result in a ClassCastException).
     */
    public void testCast() throws CheckException {
        RangeConstraint rc = new RangeConstraint(
			Integer.valueOf(0), Integer.valueOf(100));
        
        try {
			rc.check(Long.valueOf(50));
            fail("Expected ClassCastException");
        } catch (ClassCastException expect) { /* expected */ }
        try {
			rc.check(Long.valueOf(-10));
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) { /* expected */ }
        try {
			rc.check(Long.valueOf(110));
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) { /* expected */ }
    }

    
    /**
     * Returns the test suite
     * @return the test suite
     */
    public static Test suite () {
        return TLTestSetup.createTLTestSetup(new TestSuite(TestRangeConstraint.class));    
   }
    
    /**
     * The main method
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
