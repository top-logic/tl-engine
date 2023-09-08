/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import static com.top_logic.basic.util.NumberUtil.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.util.NumberUtil;


/**
 * Test case for {@link NumberUtil}.
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class TestNumberUtil extends BasicTestCase{
    
    /**
     * test method getDoubleValue
     */
    public void testgetDoubleValue() {
        
        NumberUtil.getDouble(null);
        assertEquals(0.0d, NumberUtil.getDoubleValue(null), 0.0d);
        
		Number theNum = Double.valueOf(123);
        assertEquals(123d, NumberUtil.getDoubleValue(theNum), 0.0d);

		Number theFloat = Float.valueOf((float) (0.25d * 0.3d));
        assertEquals(0.075d, NumberUtil.getDoubleValue(theFloat), 0.0d);
    }

    /**
     * test method getDouble
     */
    public void testGetDouble() {
        NumberUtil.getDouble(null);
        assertEquals(0d, NumberUtil.getDouble(null).doubleValue(), 0d);
        
		Number theNum = Double.valueOf(456);
        assertEquals(456d, NumberUtil.getDouble(theNum).doubleValue(), 0d);
        
		Number theNum1 = Integer.valueOf(123);
        assertEquals(123d,NumberUtil.getDouble(theNum1).doubleValue(), 0d);

		Number theFloat = Float.valueOf((float) (0.25d * 0.3d));
        assertEquals(0.075d, NumberUtil.getDouble(theFloat).doubleValue(), 0.0d);
    }
    
	/**
	 * @see NumberUtil#round(double, int)
	 */
	public void testRound() {
		assertEquals(1D, NumberUtil.round(1.423D, 0), 0.0);
		assertEquals(1.4D, NumberUtil.round(1.423D, 1), 0.0);
		assertEquals(1.42D, NumberUtil.round(1.423D, 2), 0.0);
		assertEquals(1500.0D, NumberUtil.round(1453.5D, -2), 0.0);
	}

	/**
	 * @see NumberUtil#round(double, int)
	 */
	public void testRoundLarge() {
		assertEquals(Long.MAX_VALUE + 1.4D, NumberUtil.round(Long.MAX_VALUE + 1.42D, 1), 0.0);
	}

	/**
	 * @see NumberUtil#round(double, int)
	 */
	public void testRoundSmall() {
		assertEquals(Long.MIN_VALUE - 2D + 0.4D, NumberUtil.round(Long.MIN_VALUE - 2D + 0.42D, 1), 0.0);
	}

	/**
	 * @see NumberUtil#round(double, int)
	 */
	public void testRoundHighPrecision() {
		assertEquals(1.1234567890123456789010D, NumberUtil.round(1.1234567890123456789012D, 22), 0.0);
	}

    /**
     * test method storeSum
     */
    public void testStoreSum() {
        Map theMap = new HashMap(4);
        String theKey = "abc";
        double theVal = 100d;
        
        /* test one, initial state - put 100 into the Map */
        NumberUtil.storeSum(theMap, theKey, theVal);
        Number theNum = (Number)theMap.get(theKey);
        assertEquals(100d,theNum.doubleValue(),0d);
        
        /* test two, added 100 */
        NumberUtil.storeSum(theMap, theKey, theVal);
        Number theNum2 = (Number)theMap.get(theKey);
        assertEquals(200d,theNum2.doubleValue(),0d);
    }

    public void testTransformInBaseValueAndDiffs() {
        double[] values = new double[]{0, 100, 200, 150};
        double[] result = new double[]{0, 100, 100, -50};
        
        assertTrue(equals(NumberUtil.transformInBaseValueAndDiffs(values), result));
        
        values = new double[]{0, 0, 200, 0, 0, -300};
        result = new double[]{0, 0, 200, 0, 0, -500};
        
        assertTrue(equals(NumberUtil.transformInBaseValueAndDiffs(values), result));
        
        values = new double[]{100, 200, 200, 200, 0, 0};
        result = new double[]{100, 100,   0,   0, 0, 0};
        
        assertTrue(equals(NumberUtil.transformInBaseValueAndDiffs(values), result));
    }
    
    private boolean equals(double[] arr1, double[] arr2) {
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i]) return false;
        }
        
        return true;
    }

	// The following tests contain parameters that are aligned in columns for easier detection of missing numbers.

	public void testGetBitsSetByte() {
		assertBitsSetByte((byte) 0x00, Arrays.<Integer>asList());
		assertBitsSetByte((byte) 0xFF, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7));
		assertBitsSetByte((byte) 0x7F, Arrays.asList(0, 1, 2, 3, 4, 5, 6));
		assertBitsSetByte((byte) 0x0F, Arrays.asList(0, 1, 2, 3));
		assertBitsSetByte((byte) 0xF0, Arrays.asList(4, 5, 6, 7));
		assertBitsSetByte((byte) 0x81, Arrays.asList(0, 7));
	}

	public void testGetBitsSetShort() {
		assertBitsSetShort((short) 0x0000, Arrays.<Integer>asList());
		assertBitsSetShort((short) 0xFFFF, Arrays.asList(0, 1,  2,  3,  4,  5,  6,  7, 8, 9, 10, 11, 12, 13, 14, 15));
		assertBitsSetShort((short) 0x7FFF, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14));
		assertBitsSetShort((short) 0x00FF, Arrays.asList(0, 1,  2,  3,  4,  5,  6,  7));
		assertBitsSetShort((short) 0xFF00, Arrays.asList(8, 9, 10, 11, 12, 13, 14, 15));
		assertBitsSetShort((short) 0x0F0F, Arrays.asList(0, 1,  2,  3,  8,  9, 10, 11));
		assertBitsSetShort((short) 0x8001, Arrays.asList(0, 15));
	}

	public void testGetBitsSetInt() {
		assertBitsSetInt(0x00000000, Arrays.<Integer>asList());
		assertBitsSetInt(0xFFFFFFFF, Arrays.asList( 0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15, 16,
			17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31));
		assertBitsSetInt(0x7FFFFFFF, Arrays.asList( 0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15, 16,
			17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30));
		assertBitsSetInt(0x0000FFFF, Arrays.asList( 0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15));
		assertBitsSetInt(0xFFFF0000, Arrays.asList(16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31));
		assertBitsSetInt(0x0F0F0F0F, Arrays.asList( 0,  1,  2,  3,  8,  9, 10, 11, 16, 17, 18, 19, 24, 25, 26, 27));
		assertBitsSetInt(0x80000001, Arrays.asList(0, 31));
	}

	public void testGetBitsSetLong() {
		assertBitsSetLong(0x0000000000000000L, Arrays.<Integer>asList());
		assertBitsSetLong(0xFFFFFFFFFFFFFFFFL, Arrays.asList( 0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15,
			16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
			32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
			48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63));
		assertBitsSetLong(0x7FFFFFFFFFFFFFFFL, Arrays.asList( 0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15,
			16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
			32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
			48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62));
		assertBitsSetLong(0x00000000FFFFFFFFL, Arrays.asList( 0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15,
			16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31));
		assertBitsSetLong(0xFFFFFFFF00000000L, Arrays.asList(32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
			48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63));
		assertBitsSetLong(0x0F0F0F0F0F0F0F0FL, Arrays.asList( 0,  1,  2,  3,  8,  9, 10, 11, 16, 17, 18, 19, 24, 25, 26, 27,
			32, 33, 34, 35, 40, 41, 42, 43, 48, 49, 50, 51, 56, 57, 58, 59));
		assertBitsSetLong(0x8000000000000001L, Arrays.asList(0, 63));
	}
	
	private void assertBitsSetByte(byte bitsActual, List<Integer> expected) {
		assertEquals(expected, getBitsSet(bitsActual));
	}

	private void assertBitsSetShort(short bitsActual, List<Integer> expected) {
		assertEquals(expected, getBitsSet(bitsActual));
	}

	private void assertBitsSetInt(int bitsActual, List<Integer> expected) {
		assertEquals(expected, getBitsSet(bitsActual));
	}

	private void assertBitsSetLong(long bitsActual, List<Integer> expected) {
		assertEquals(expected, getBitsSet(bitsActual));
	}

    /** 
     * This method returns the test in a suite.
     * 
     * @return Returns the test in a suite.
     */
    public static Test suite () {
        return BasicTestSetup.createBasicTestSetup(new TestSuite(TestNumberUtil.class));
    }
    
    /**
     * This method can be used for direct testing.
     * 
     * @param args A string array of argument.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }

}
