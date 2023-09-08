/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The NumberUtil contains useful static methods for numbers.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class NumberUtil {

    private NumberUtil() {
        // Use the static methods
    }

    /** 
     * Convenience method to get double values from wrapper number
     * attributes (e.g. NumberUtil.getDoubleValue(wrapper.getValue("budget"))).
     */
    public static double getDoubleValue(Object aNumber) {
        return getDoubleValue((Number) aNumber);
    }
    
    /** 
     * Convenience method to get int values from wrapper number
     * attributes (e.g. NumberUtil.getIntValue(wrapper.getValue("budget"))).
     */
    public static int getIntValue(Object aNumber) {
        return getIntValue((Number) aNumber);
    }
    
    /**
     * Returns a int value in any case. Null permitted! If the given number
     * is <code>null</code> 0 is returned.
     * 
     * @param aNumber
     *        A number. Maybe <code>null</code>.
     */
    public static int getIntValue(Number aNumber) {
        if (aNumber == null) { return 0; }
        
        return aNumber.intValue();
    }
    
    /**
     * Returns a double value in any case. Null permitted! If the given number
     * is <code>null</code> 0.0 is returned.
     * 
     * @param aNumber
     *        A number. Maybe <code>null</code>.
     */
    public static double getDoubleValue(Number aNumber) {
        // Problem with representing values, which are not instance of Double.
        // When getting a float, the normal Number.doubleValue() method will 
        // return a wrong value.
        if (aNumber instanceof Double) {
            return aNumber.doubleValue();
        }
        else if (aNumber == null) {
            return 0.0d;
        }
        else {
            return Double.parseDouble(aNumber.toString());
        }
    }

    /** 
     * Returns in any case a Double. If the given number is 
     * <code>null</code> Double(0) is returned but 
     * never <code>null</code>.
     * 
     * @param aNumber A {@link Number}. Maybe <code>null</code>.
     */
    public static Double getDouble(Number aNumber) {
        if (aNumber instanceof Double) {
            return (Double) aNumber;
        }
        else if (aNumber == null) {
            return Double.valueOf(0.0d);
        }
        else {
            return Double.valueOf(aNumber.toString());
        }
    }

    /**
     * Checks whether the given numbers have the same double values.
     */
    public static boolean numbersEqual(Number a, Number b){
        return a.doubleValue() - b.doubleValue() == 0;
    }

    /**
     * This method stores values in the given map. If for the key exists already
     * a value, the sum (of the given value and the map value) is stored.
     * 
     * @param aMap
     *        A {@link Map} with sums. Must not be <code>null</code>.
     * @param aKey
     *        The key. Must not be <code>null</code>.
     * @param aValue
     *        The value to store.
     */
    public static void storeSum (Map aMap, Object aKey, double aValue) {
        Number number = (Number) aMap.get(aKey);
        
        if (number == null) {
            aMap.put(aKey, Double.valueOf(aValue));
            return;
        } else {
            double sum = number.doubleValue() + aValue;
            aMap.put(aKey, Double.valueOf(sum));
        }
    }

    /**
	 * This method rounds the given double to a double with maximum two digits after the comma, e.g.
	 * round(3.128456, 2) == 3.13
	 * 
	 * @param aValue
	 *        A value to round.
	 * @param aDigitNumber
	 *        A digit number (greater or equal to 0).
	 */
    public static double round(double aValue, int aDigitNumber) {
		double factor;
		switch (aDigitNumber) {
			case 0:
				return Math.rint(aValue);
			case 1:
				factor = 10.0;
				break;
			case 2:
				factor = 100.0;
				break;
			case 3:
				factor = 1000.0;
				break;
			case 4:
				factor = 10000.0;
				break;
			case 5:
				factor = 100000.0;
				break;
			default:
				factor = Math.pow(10, aDigitNumber);
		}
		return Math.rint(aValue * factor) / factor;
	}

    /** 
     * This method returns a value array with the first value as base value and
     * the following values as difference to the base value.
     * 
     * values      return
     * [0] =   0   [0] =    0
     * [0] = 100   [0] =  100
     * [0] = 200   [0] =  100
     * [0] = 150   [0] =  -50
     * 
     * @param someValues The values. Must NOT be <code>null</code>.
     */
    public static double[] transformInBaseValueAndDiffs(double[] someValues) {
        double[] result = new double[someValues.length];
        result[0] = someValues[0];
        
        double refValue = someValues[0];
        for (int i = 1; i < someValues.length; i++) {
            double value = someValues[i];
            
            if (refValue == 0 && value == 0) {
                result[i] = 0;
            } else if (refValue != 0 && value == 0) {
                result[i] = 0;
            } else if (refValue == 0 && value != 0) {
                result[i] = value;
                refValue  = value;
            } else {
                result[i] = value - refValue;
                refValue  = value;
            }
        }
        
        return result;
    }
    
	/**
	 * Which bits are set in the given byte?
	 * <p>
	 * Indices are 0-based. <br/>
	 * Using the method {@link #getBitsSet(long)} is not possible, as the conversion "byte -> long"
	 * changes which bits are set, if the number is negative.
	 * </p>
	 */
	public static List<Integer> getBitsSet(byte bitset) {
		List<Integer> bitsSet = new ArrayList<>();
		for (int i = 0; i < Byte.SIZE; i++) {
			if (isBitSet(bitset, i)) {
				bitsSet.add(i);
			}
		}
		return bitsSet;
	}

	/**
	 * Is the bit at the given index set in the given byte?
	 * <p>
	 * The index is 0-based. <br/>
	 * Using the method {@link #isBitSet(long, int)} is not possible, as the conversion
	 * "byte -> long" changes which bits are set, if the number is negative.
	 * </p>
	 */
	public static boolean isBitSet(byte bitset, int index) {
		return (bitset & (1 << index)) != 0;
	}

	/**
	 * Which bits are set in the given short?
	 * <p>
	 * Indices are 0-based. <br/>
	 * Using the method {@link #getBitsSet(long)} is not possible, as the conversion "short -> long"
	 * changes which bits are set, if the number is negative.
	 * </p>
	 */
	public static List<Integer> getBitsSet(short bitset) {
		List<Integer> bitsSet = new ArrayList<>();
		for (int i = 0; i < Short.SIZE; i++) {
			if (isBitSet(bitset, i)) {
				bitsSet.add(i);
			}
		}
		return bitsSet;
	}

	/**
	 * Is the bit at the given index set in the given short?
	 * <p>
	 * The index is 0-based. <br/>
	 * Using the method {@link #isBitSet(long, int)} is not possible, as the conversion
	 * "short -> long" changes which bits are set, if the number is negative.
	 * </p>
	 */
	public static boolean isBitSet(short bitset, int index) {
		return (bitset & (1 << index)) != 0;
	}

	/**
	 * Which bits are set in the given int?
	 * <p>
	 * Indices are 0-based. <br/>
	 * Using the method {@link #getBitsSet(long)} is not possible, as the conversion "int -> long"
	 * changes which bits are set, if the number is negative.
	 * </p>
	 */
	public static List<Integer> getBitsSet(int bitset) {
		List<Integer> bitsSet = new ArrayList<>(Integer.bitCount(bitset));
		for (int i = 0; i < Integer.SIZE; i++) {
			if (isBitSet(bitset, i)) {
				bitsSet.add(i);
			}
		}
		return bitsSet;
	}

	/**
	 * Is the bit at the given index set in the given int?
	 * <p>
	 * The index is 0-based. <br/>
	 * Using the method {@link #isBitSet(long, int)} is not possible, as the conversion
	 * "int -> long" changes which bits are set, if the number is negative.
	 * </p>
	 */
	public static boolean isBitSet(int bitset, int index) {
		return (bitset & (1 << index)) != 0;
	}

	/**
	 * Which bits are set in the given long?
	 * <p>
	 * Indices are 0-based.
	 * </p>
	 */
	public static List<Integer> getBitsSet(long bitset) {
		List<Integer> bitsSet = new ArrayList<>(Long.bitCount(bitset));
		for (int i = 0; i < Long.SIZE; i++) {
			if (isBitSet(bitset, i)) {
				bitsSet.add(i);
			}
		}
		return bitsSet;
	}

	/**
	 * Is the bit at the given index set in the given long?
	 * <p>
	 * The index is 0-based.
	 * </p>
	 */
	public static boolean isBitSet(long bitset, int index) {
		return (bitset & (1L << index)) != 0;
	}

}

