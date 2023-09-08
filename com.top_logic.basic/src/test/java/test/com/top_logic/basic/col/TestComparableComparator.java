/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestNamedComparator.NamedImpl;

import com.top_logic.basic.Named;
import com.top_logic.basic.col.ComparableComparator;

/**
 * This Class tests the ComparableComparator.
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class TestComparableComparator extends TestCase{
    
    static Comparator cC  = ComparableComparator.INSTANCE;
    static Comparator dCC = ComparableComparator.INSTANCE_DESCENDING;
    
    public void testDescendingComparator() {
        assertEquals(1, dCC.compare(value(10), value(20)));
    }
    
    public void testBothNull() {
        check(0, null, null);
    }
    
    public void testOneNullOtherInteger() {
        check(-1, null, value(100));
    }
    
    public void testBothIntegerNotNull() {
		Integer o1 = Integer.valueOf(Integer.MIN_VALUE);
		Integer o2 = Integer.valueOf(Integer.MAX_VALUE);
        check(-1, o1, o2);
    }
    
    public void testOneNullOtherEmptyList() {
        List   o2 = new ArrayList();
        check(0, null, o2);
    }
    
    public void testBothEmptyList() {
        Object o1 = new ArrayList();
        List   o2 = new ArrayList();
        check(0, o1, o2);
    }
    
    public void testIntegerWithListofOneInteger() {
        check(-1, value(1), list(10));
    }
    
    public void testBothListsNotEmptySameLength() {
        check(1, list(20, 10), list(20, 5));
    }
    
    public void testBothListsNotEmptyDifferentLength() {
        check(-1, list(5), list(5,10));
    }
    
    public void testTwoListsDifferendLength() {
        check(-1,listWithLength(5),listWithLength(10));
    }

    /**
     * Returns an {@link Integer} with the given value.
     */
    private Integer value(int val) {
		Integer o1 = Integer.valueOf(val);
        return o1;
    }
    
    /**
     * Returns a list containing the given value as {@link Integer}.
     * 
     * @return a list with the given value
     */
    private List list(int value) {
        List    o2 = new ArrayList();
		o2.add(Integer.valueOf(value));
        return o2;
    }

    /**
     * Returns a list containing the given values as {@link Integer}.
     * 
     * @return a list with the given values
     */
    private List list(int v1, int v2) {
        List o1 = new ArrayList();
		o1.add(Integer.valueOf(v1));
		o1.add(Integer.valueOf(v2));
        return o1;
    }
    
    /**
     * This method creates a list with aLength. 
     * Every element of the list will be an {@link Integer}.
     */
    private List listWithLength(int aLength) {
        List o1 = new ArrayList();
        for(int i = 0; i<aLength;i++) {
			o1.add(Integer.valueOf(i));
        }
        return o1;
    }
    
    /**
     * This method is used to call the compare method. If the expected value != zero it checks both 
     * possibilities.
     * 
     * @param anExpectedValue 1-, 0 or 1
     * @param anObj1 to compare
     * @param anObj2 to compare
     */
    public void check(int anExpectedValue, Object anObj1, Object anObj2) {
        if (anExpectedValue == 0) {
            assertEquals(anExpectedValue, cC.compare(anObj1, anObj2));
        } else if (anExpectedValue!=0) {
            int theExpectedValue = getSign(anExpectedValue);
            assertEquals( theExpectedValue, getSign(cC.compare(anObj1, anObj2)));
            assertEquals(-theExpectedValue, getSign(cC.compare(anObj2, anObj1)));
        }
    }
    
    /**
     * This method returns -1 if anInt was less than zero, 1 if anInt was greater than zero.
     * It returns zero if anInt is zero.
     * 
     * @param anInt to get the sign from.
     * @return 0,1 or -1 if the given int zero, greater or less than zero.
     */
    public int getSign(int anInt) {
        // Compare to Math.sign form JDK 1.5
        if (anInt < 0) {
            return -1;
        } else if(anInt > 0) {
            return 1;
        }
        return 0;
    }
    
	/**
	 * Tests comparision of {@link String} and {@link Named}.
	 */
	public void testStringCompare() throws Exception {
		BasicTestCase.executeInDefaultLocale(Locale.GERMAN, () -> {
			assertTrue(cC.compare("alpha", "Zulu") < 0);
			assertTrue(cC.compare("alpha", "Alpha") < 0);
			assertTrue(cC.compare("a", "ä") < 0);

			assertTrue(cC.compare(new NamedImpl("alpha"), new NamedImpl("Zulu")) < 0);
			assertTrue(cC.compare(new NamedImpl("alpha"), new NamedImpl("Alpha")) < 0);
			return null;
		});
	}

	/**
	 * Returns a suite of tests.
	 */
    public static Test suite() {
        return new TestSuite(TestComparableComparator.class);
    }

    /**
     * This main function is for direct testing.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
}
