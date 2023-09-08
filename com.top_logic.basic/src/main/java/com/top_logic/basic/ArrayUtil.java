/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Random;
import java.util.RandomAccess;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.top_logic.basic.col.ArrayIterator;
import com.top_logic.basic.col.ArrayListIterator;

/**
 * Class implementing extended array operations.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class ArrayUtil {

	/** Empty byte array. */
	public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	/** Empty short array. */
	public static final short[] EMPTY_SHORT_ARRAY = new short[0];

	/** Empty int array. */
	public static final int[] EMPTY_INT_ARRAY = new int[0];

	/** Empty long array. */
	public static final long[] EMPTY_LONG_ARRAY = new long[0];

	/** Empty char array. */
	public static final char[] EMPTY_CHAR_ARRAY = new char[0];

	/** Empty boolean array. */
	public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];

	/** Empty float array. */
	public static final float[] EMPTY_FLOAT_ARRAY = new float[0];

	/** Empty double array. */
	public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

	/** Empty Object array. */
	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	/** Empty {@link File} array. */
	public static final File[] EMPTY_FILE_ARRAY = new File[0];

	/** Instance of an empty object array */
	public static final Object[] EMPTY_ARRAY = EMPTY_OBJECT_ARRAY;

    /** Instance of an empty string array */
	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	/** Empty {@link Annotation} array. */
	public static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

    /**
     * This class provides only static functions.
     * Therefore the default constructor is private.
     */
    private ArrayUtil() {
        // static only
    }


	/**
	 * Creates an {@link Iterator} that traverses over the given array.
	 * 
	 * <p>
	 * The {@link Iterator} does not supports {@link Iterator#remove()}
	 * </p>
	 * 
	 * @param elements
	 *        The base elements for the iterator
	 */
	public static <E> Iterator<E> iterator(E... elements) {
		return new ArrayIterator<>(elements, 0);
    }

	/**
	 * Creates an {@link ListIterator} that traverses over the given array.
	 * 
	 * <p>
	 * The {@link ListIterator} does not supports {@link ListIterator#remove()},
	 * {@link ListIterator#add(Object)}, or {@link ListIterator#set(Object)}
	 * </p>
	 * 
	 * @param elements
	 *        The base elements for the iterator
	 * @param index
	 *        Index of the element in the given array returned by first call of
	 *        {@link Iterator#next()}.
	 */
	public static <E> ListIterator<E> listIterator(int index, E... elements) {
		return new ArrayListIterator<>(elements, index);
	}

    /**
     * Checks, whether the given array is <code>null</code> or empty.
     *
     * @param aArray
     *            the array to check
     * @return <code>true</code>, if the array is <code>null</code> or empty
     */
    public static boolean isEmpty(Object[] aArray) {
        return (aArray == null) || aArray.length == 0;
    }

    /**
     * Checks, whether the given array is <code>null</code> or empty.
     *
     * @param aArray
     *            the array to check
     * @return <code>true</code>, if the array is <code>null</code> or empty
     */
    public static boolean isEmpty(int[] aArray) {
        return (aArray == null) || aArray.length == 0;
    }

    /**
     * Checks, whether the given array is <code>null</code> or empty.
     *
     * @param aArray
     *            the array to check
     * @return <code>true</code>, if the array is <code>null</code> or empty
     */
    public static boolean isEmpty(double[] aArray) {
        return (aArray == null) || aArray.length == 0;
    }


    /**
     * Checks, whether the given array contains only empty (<code>null</code>) elements.
     *
     * @param aArray
     *        the array to check
     * @return <code>true</code>, if the array contains only <code>null</code> elements
     */
    public static boolean isContentEmpty(Object[] aArray) {
        if (aArray == null) return true;
        for (int i = 0; i < aArray.length; i++) {
            if (aArray[i] != null) return false;
        }
        return true;
    }

    /**
     * Checks, whether the given array contains only empty strings.
     *
     * @param aArray
     *        the array to check
     * @return <code>true</code>, if the array contains only empty strings
     */
    public static boolean isContentEmpty(String[] aArray) {
        if (aArray == null) return true;
        for (int i = 0; i < aArray.length; i++) {
            if (aArray[i] != null && aArray[i].length() > 0) return false;
        }
        return true;
    }

    /**
     * Checks, whether the given array contains only empty (0) values.
     *
     * @param aArray
     *        the array to check
     * @return <code>true</code>, if the array contains only 0 values
     */
    public static boolean isContentEmpty(int[] aArray) {
        if (aArray == null) return true;
        for (int i = 0; i < aArray.length; i++) {
            if (aArray[i] != 0) return false;
        }
        return true;
    }

    /**
     * Checks, whether the given array is <code>null</code> and creates a new empty
     * Object[] in this case.
     *
     * @param aArray
     *            the array to check
     * @return the original array or an empty Object[], if the array is <code>null</code>
     */
    public static Object[] checkOnNull(Object[] aArray) {
        return aArray == null ? EMPTY_ARRAY : aArray;
    }

    /**
     * Checks, whether the given array is <code>null</code> and creates a new empty
     * Object[] in this case.
     *
     * @param aArray
     *            the array to check
     * @return the original array or an empty Object[], if the array is <code>null</code>
     */
    public static String[] checkOnNull(String[] aArray) {
        return aArray == null ? EMPTY_STRING_ARRAY : aArray;
    }

    /**
     * Checks, whether the given array is <code>null</code> and creates a new empty
     * int[] in this case.
     *
     * @param aArray
     *            the array to check
     * @return the original array or an empty int[], if the array is <code>null</code>
     */
    public static int[] checkOnNull(int[] aArray) {
        return aArray == null ? EMPTY_INT_ARRAY : aArray;
    }

    /**
     * Checks, whether the given array is <code>null</code> and creates a new empty
     * double[] in this case.
     *
     * @param aArray
     *            the array to check
     * @return the original array or an empty double[], if the array is <code>null</code>
     */
    public static double[] checkOnNull(double[] aArray) {
        return aArray == null ? EMPTY_DOUBLE_ARRAY : aArray;
    }



    /**
     * Clears the given array by setting all elements to <code>null</code>.
     *
     * @param aArray
     *        the array to clear
     */
    public static void clear(Object[] aArray) {
        fill(aArray, null);
    }

    /**
     * Clears the given array by setting all values to 0.
     *
     * @param aArray
     *        the array to clear
     */
    public static void clear(int[] aArray) {
        fill(aArray, 0);
    }

    /**
     * Clears the given array by setting all values to 0.0.
     *
     * @param aArray
     *        the array to clear
     */
    public static void clear(double[] aArray) {
        fill(aArray, 0.0);
    }


    /**
     * Clears the given array by setting all elements to the empty String.
     *
     * @param aArray
     *        the array to clear
     */
    public static void clearStringArray(String[] aArray) {
        fill(aArray, StringServices.EMPTY_STRING);
    }



    /**
     * Fills the given array (with any dimension) with the given object.
     *
     * @param aArray
     *        the array to fill
     * @param aValue
     *        the value to fill the array with
     */
    public static void fill(Object[] aArray, Object aValue) {
        if (aArray != null)
            for (int i = 0; i < aArray.length; i++)
                if (aArray[i] instanceof Object[]) fill((Object[])aArray[i], aValue);
                else aArray[i] = aValue;
    }

    /**
     * Fills the given array with the given value.
     *
     * @param aArray
     *        the array to fill
     * @param aValue
     *        the value to fill the array with
     */
    public static void fill(int[] aArray, int aValue) {
        if (aArray != null)
            for (int i = 0; i < aArray.length; i++)
                aArray[i] = aValue;
    }

    /**
     * Fills the given array with the given value.
     *
     * @param aArray
     *        the array to fill
     * @param aValue
     *        the value to fill the array with
     */
    public static void fill(double[] aArray, double aValue) {
        if (aArray != null)
            for (int i = 0; i < aArray.length; i++)
                aArray[i] = aValue;
    }

    /**
     * Fill the given array (with any dimension) with default instances of the given class.
     *
     * @param aArray
     *        The array to be filled, may be <code>null</code>.
     * @param aClass
     *        The class to be used for getting the default instance from, may be
     *        <code>null</code>.
     * @return the given array to allow chaining calls.
     */
    public static Object[] fillArray(Object[] aArray, Class aClass) {
        try {
            if (aArray != null)
                for (int i = 0; i < aArray.length; i++)
                    if (aArray[i] instanceof Object[]) ArrayUtil.fillArray((Object[])aArray[i], aClass);
                    else aArray[i] = aClass == null ? null : aClass.newInstance();
        }
        catch (Exception e) {
            Logger.error("Failed to create new elements for class '" + aClass + "'.", e, ArrayUtil.class);
        }
        return aArray;
    }


    /**
     * Fills up empty values in the given array (with any dimension) with the given value.
     *
     * @param aArray
     *        the array to fill
     * @param aValue
     *        the value to fill the array with
     */
    public static void fillEmpty(Object[] aArray, Object aValue) {
        if (aArray != null)
            for (int i = 0; i < aArray.length; i++)
                if (aArray[i] instanceof Object[]) fillEmpty((Object[])aArray[i], aValue);
                else if (aArray[i] == null) aArray[i] = aValue;
    }

    /**
     * Fills up empty values in the given array with the given value.
     *
     * @param aArray
     *        the array to fill
     * @param aValue
     *        the value to fill the array with
     */
    public static void fillEmptyStrings(String[] aArray, String aValue) {
        if (aArray != null)
            for (int i = 0; i < aArray.length; i++)
                if (StringServices.isEmpty(aArray[i])) aArray[i] = aValue;
    }

    /**
     * Fills up empty values in the given array with the given value.
     *
     * @param aArray
     *        the array to fill
     * @param aValue
     *        the value to fill the array with
     */
    public static void fillEmpty(int[] aArray, int aValue) {
        if (aArray != null)
            for (int i = 0; i < aArray.length; i++)
                if (aArray[i] == 0) aArray[i] = aValue;
    }

    /**
     * Fills up empty values in the given array (with any dimension) with default instances
     * of the given class.
     *
     * @param aArray
     *        The array to be filled, may be <code>null</code>.
     * @param aClass
     *        The class to be used for getting the default instance from, may be
     *        <code>null</code>.
     * @return the given array to allow chaining calls.
     */
    public static Object[] fillArrayEmpty(Object[] aArray, Class aClass) {
        try {
            if (aArray != null)
                for (int i = 0; i < aArray.length; i++)
                    if (aArray[i] instanceof Object[]) ArrayUtil.fillArrayEmpty((Object[])aArray[i], aClass);
                    else if (aArray[i] == null) aArray[i] = aClass == null ? null : aClass.newInstance();
        }
        catch (Exception e) {
            Logger.error("Failed to create new elements for class '" + aClass + "'.", e, ArrayUtil.class);
        }
        return aArray;
    }



    /**
     * Searches for an object within an array, using the equals method.
     *
     * @param aObject
     *            the object to search for
     * @param aArray
     *            the array to search
     * @return the index of the first object in the array that equals the given object or
     *         <code>-1</code>, if the object was not found
     */
    public static int indexOf(Object aObject, Object[] aArray) {
		if (aArray == null) {
            return -1;
        }
		return indexOf(aArray, aObject, 0, aArray.length - 1);
    }


	/**
	 * Searches ascending for an object within an array, using the equals method.
	 * 
	 * @param arr
	 *        The array to search. Must not be <code>null</code>.
	 * @param o
	 *        The object to search for.
	 * @param start
	 *        The start slot to check for equality. Must be > 0 and &lt; size of the array.
	 * @param stop
	 *        The stop slot to check for equality. Must be > start and &lt; size of the array.
	 * @return The first index greater than <code>start</code> and lower or equal to
	 *         <code>stop</code> of an object in the array that equals the given object or
	 *         <code>-1</code>, if the object was not found.
	 */
	public static int indexOf(Object[] arr, Object o, int start, int stop) {
		if (o == null) {
			for (int i = start; i <= stop; i++) {
                if (arr[i] == null) return i;
            }
		} else {
			for (int i = start; i <= stop; i++) {
                if (o.equals(arr[i])) return i;
            }
        }
        return -1;
	}

    /**
     * Searches for an object within an array, using the equals method, beginning from the
     * end.
     *
     * @param aObject
     *            the object to search for
     * @param aArray
     *            the array to search
     * @return the index of the last object in the array that equals the given object or
     *         <code>-1</code>, if the object was not found
     */
    public static int lastIndexOf(Object aObject, Object[] aArray) {
        if (aArray == null) {
            return -1;
        }
		return lastIndexOf(aArray, aObject, aArray.length - 1, 0);
    }


	/**
	 * Searches descending for an object within an array, using the equals method.
	 * 
	 * @param arr
	 *        The array to search. Must not be <code>null</code>.
	 * @param o
	 *        The object to search for.
	 * @param start
	 *        The start slot to check for equality. Must be greater than or equal to <code>0</code>
	 *        and less than the size of the array.
	 * @param stop
	 *        The stop slot to check for equality. Must be less than <code>start</code> and greater
	 *        than or equal to <code>0</code>.
	 * @return The greatest index less than <code>start</code> and greater than <code>stop</code> of
	 *         an object in the array that equals the given object or <code>-1</code>, if the object
	 *         was not found.
	 */
	public static int lastIndexOf(Object[] arr, Object o, int start, int stop) {
		if (o == null) {
			for (int i = start; i >= stop; i--) {
                if (arr[i] == null) return i;
            }
		} else {
			for (int i = start; i >= stop; i--) {
                if (o.equals(arr[i])) return i;
            }
        }
        return -1;
	}

    /**
     * Searches for a string within a string array, ignoring case.
     *
     * @param aString
     *            the string to search for
     * @param aArray
     *            the array to search
     * @return the index of the first string in the array that equals the given string or
     *         <code>-1</code>, if the string was not found
     */
    public static int indexOfIgnoreCase(String aString, String[] aArray) {
        if (aArray == null) {
            return -1;
        }
        if (aString == null) {
            return indexOf(null, aArray);
        }
        for (int i = 0; i < aArray.length; i++) {
            if (aString.equalsIgnoreCase(aArray[i])) return i;
        }
        return -1;
    }

    /**
     * Searches for a string within a string array, ignoring case, beginning from the end.
     *
     * @param aString
     *            the string to search for
     * @param aArray
     *            the array to search
     * @return the index of the last string in the array that equals the given string or
     *         <code>-1</code>, if the string was not found
     */
    public static int lastIndexOfIgnoreCase(String aString, String[] aArray) {
        if (aArray == null) {
            return -1;
        }
        if (aString == null) {
            return lastIndexOf(null, aArray);
        }
        for (int i = aArray.length - 1; i >= 0; i--) {
            if (aString.equalsIgnoreCase(aArray[i])) return i;
        }
        return -1;
    }

    /**
     * Searches for a value within an array.
     *
     * @param aValue
     *            the value to search for
     * @param aArray
     *            the array to search
     * @return the index of the first value in the array that equals the given value or
     *         <code>-1</code>, if the value was not found
     */
    public static int indexOf(int aValue, int[] aArray) {
        if (aArray != null) {
            for (int i = 0; i < aArray.length; i++) {
                if (aArray[i] == aValue) return i;
            }
        }
        return -1;
    }

    /**
     * Searches for a value within an array, beginning from the end.
     *
     * @param aValue
     *            the value to search for
     * @param aArray
     *            the array to search
     * @return the index of the last value in the array that equals the given value or
     *         <code>-1</code>, if the value was not found
     */
    public static int lastIndexOf(int aValue, int[] aArray) {
        if (aArray != null) {
            for (int i = aArray.length - 1; i >= 0; i--) {
                if (aArray[i] == aValue) return i;
            }
        }
        return -1;
    }

    /**
     * Searches for a value within an array.
     *
     * @param aValue
     *            the value to search for
     * @param aArray
     *            the array to search
     * @return the index of the first value in the array that equals the given value or
     *         <code>-1</code>, if the value was not found
     */
    public static int indexOf(byte aValue, byte[] aArray) {
    	if (aArray != null) {
    		for (int i = 0; i < aArray.length; i++) {
    			if (aArray[i] == aValue) return i;
    		}
    	}
    	return -1;
    }
    
    /**
     * Searches for an object within an array, using the equals method.
     *
     * @param aArray
     *            the array to search
     * @param aObject
     *            the object to search for
     * @return <code>true</code>, if the given array contains the given object,
     *         <code>false</code> otherwise
     */
    public static boolean contains(Object[] aArray, Object aObject) {
        return indexOf(aObject, aArray) > -1;
    }

    /**
     * Searches for a string within a string array, ignoring case.
     *
     * @param aArray
     *            the array to search
     * @param aString
     *            the object to search for
     * @return <code>true</code>, if the given array contains the given string,
     *         <code>false</code> otherwise
     */
    public static boolean containsIgnoreCase(String[] aArray, String aString) {
        return indexOfIgnoreCase(aString, aArray) > -1;
    }

    /**
     * Searches for a value within an array.
     *
     * @param aArray
     *            the array to search
     * @param aValue
     *            the value to search for
     * @return <code>true</code>, if the given array contains the given value,
     *         <code>false</code> otherwise
     */
    public static boolean contains(int[] aArray, int aValue) {
        return indexOf(aValue, aArray) > -1;
    }

    /**
     * Checks, whether there is a value which appears in both arrays.
     *
     * @param a1
     *        the first array
     * @param a2
     *        the second array
     * @return <code>true</code> if there is a value which appears in both arrays
     */
    public static boolean containsAny(int[] a1, int[] a2) {
        if (isEmpty(a1) || isEmpty(a2)) return false;
        for (int i = 0; i < a1.length; i++) {
            if (contains(a2, a1[i])) return true;
        }
        return false;
    }

    /**
     * Checks, whether there is an object which appears in both arrays.
     *
     * @param a1
     *        the first array
     * @param a2
     *        the second array
     * @return <code>true</code> if there is an object which appears in both arrays
     */
    public static boolean containsAny(Object[] a1, Object[] a2) {
        if (isEmpty(a1) || isEmpty(a2)) return false;
        for (int i = 0; i < a1.length; i++) {
            if (contains(a2, a1[i])) return true;
        }
        return false;
    }

    /**
     * Checks, whether there is a string which appears in both arrays, ignoring case.
     *
     * @param a1
     *        the first array
     * @param a2
     *        the second array
     * @return <code>true</code> if there is a string which appears in both arrays,
     *         ignoring case
     */
    public static boolean containsAnyIgnoreCase(String[] a1, String[] a2) {
        if (isEmpty(a1) || isEmpty(a2)) return false;
        for (int i = 0; i < a1.length; i++) {
            if (containsIgnoreCase(a2, a1[i])) return true;
        }
        return false;
    }


    /**
     * Checks, whether both arrays contains the same elements.
     *
     * @param a1
     *        the first array
     * @param a2
     *        the second array
     * @return <code>true</code> if both arrays contains the same elements
     */
    public static boolean containsSame(int[] a1, int[] a2) {
        if (a1 == null && a2 == null) return true;
        if (a1 == null || a2 == null) return false;
        for (int i = 0; i < a1.length; i++) {
            if (!contains(a2, a1[i])) return false;
        }
        for (int i = 0; i < a2.length; i++) {
            if (!contains(a1, a2[i])) return false;
        }
        return true;
    }

    /**
     * Checks, whether both arrays contains the same elements.
     *
     * @param a1
     *        the first array
     * @param a2
     *        the second array
     * @return <code>true</code> if both arrays contains the same elements
     */
    public static boolean containsSame(Object[] a1, Object[] a2) {
        if (a1 == null && a2 == null) return true;
        if (a1 == null || a2 == null) return false;
        for (int i = 0; i < a1.length; i++) {
            if (!contains(a2, a1[i])) return false;
        }
        for (int i = 0; i < a2.length; i++) {
            if (!contains(a1, a2[i])) return false;
        }
        return true;
    }

    /**
     * Checks, whether both arrays contains the same elements, ignoring case.
     *
     * @param a1
     *        the first array
     * @param a2
     *        the second array
     * @return <code>true</code> if both arrays contains the same elements, ignoring case
     */
    public static boolean containsSameIgnoreCase(String[] a1, String[] a2) {
        if (a1 == null && a2 == null) return true;
        if (a1 == null || a2 == null) return false;
        for (int i = 0; i < a1.length; i++) {
            if (!containsIgnoreCase(a2, a1[i])) return false;
        }
        for (int i = 0; i < a2.length; i++) {
            if (!containsIgnoreCase(a1, a2[i])) return false;
        }
        return true;
    }



    /**
     * Checks, whether the given arrays and all their elements are equal.
     *
     * @param a1
     *            the first array
     * @param a2
     *            the second array
     * @return <code>true</code>, if the given arrays are the same and contains the same
     *         elements, <code>false</code> otherwise
     */
    public static boolean equals(Object[] a1, Object[] a2) {
        return Arrays.equals(a1, a2);
    }

    /**
     * Checks, whether the given arrays and all their elements are equal.
     *
     * @param a1
     *            the first array
     * @param a2
     *            the second array
     * @return <code>true</code>, if the given arrays are the same and contains the same
     *         values, <code>false</code> otherwise
     */
    public static boolean equals(int[] a1, int[] a2) {
        return Arrays.equals(a1, a2);
    }

    /**
     * Checks, whether the given arrays and all their elements are equal.
     *
     * @param a1
     *            the first array
     * @param a2
     *            the second array
     * @return <code>true</code>, if the given arrays are the same and contains the same
     *         values, <code>false</code> otherwise
     */
    public static boolean equals(double[] a1, double[] a2) {
        return Arrays.equals(a1, a2);
    }



    /**
     * Checks whether two objects are equal.
     *
     * @param o1
     *        the first object
     * @param o2
     *        the second object
     * @return <code>true</code>, if the given objects are equal, <code>false</code>
     *         otherwise
     */
    public static boolean equalsObjects(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 == null || o2 == null) return false;
        return o1.equals(o2);
    }


    /**
     * Compares two objects.
     *
     * @param c1
     *        the first object
     * @param c2
     *        the second object
     * @return -x, 0, or +x as c1 is less than, equal to, or greater than c2
     * @throws ClassCastException
     *         if the given elements are incompatible to each other.
     */
	public static <T extends Comparable<T>> int compareObjects(T c1, T c2) {
        // null is smaller than any key
        if (c1 == null && c2 == null) return 0;
        if (c1 == null) return -1;
        if (c2 == null) return 1;
        return c1.compareTo(c2);
    }



    /**
     * Compares each element in the first array with the corresponding element in the second
     * array. The next elements are compared only if the last elements were equal. If all
     * elements are equal, the array sizes are compared at last. The elements in the arrays
     * must all be instanceof Comparable.
     *
     * @param a1
     *        the first array to compare
     * @param a2
     *        the second array to compare
     * @return -x, 0, or +x as the first array is less than, equal to, or greater than the
     *         second array
     * @throws ClassCastException
     *         if elements in the array are not comparable or elements in the first array
     *         are incompatible with the elements in the second array.
     */
    public static int compare(Object[] a1, Object[] a2) {
        int result = 0;
        for (int i = 0, length = Math.min(a1.length, a2.length); result == 0 && i < length; i++) {
            result = compareObjects((Comparable)a1[i], (Comparable)a2[i]);
        }
        return result == 0 ? a1.length - a2.length : result;
    }



    /**
     * Creates an array containing the elements given in the argument list.
     *
     * @param <T>
     *        the type of the array.
     * @param elements
     *        the elements of the new array
     * @return an array containing the elements given in the argument list
     */
    public static <T> T[] createArray(T... elements) {
        return elements;
    }

    /**
     * Creates an int array containing the elements given in the argument list.
     *
     * @param elements
     *        the elements of the new array
     * @return an int array containing the elements given in the argument list
     */
    public static int[] createIntArray(int... elements) {
        return elements;
    }

    /**
     * Creates a double array containing the elements given in the argument list.
     *
     * @param elements
     *        the elements of the new array
     * @return a double array containing the elements given in the argument list
     */
    public static double[] createDoubleArray(double... elements) {
        return elements;
    }



    /**
     * Creates a new array containing all elements from the given collection. The runtime
     * type of the returned array is the type of the first element in the given collection.
     * Therefore, all elements in the collection must be of the same type or the first
     * element must be of a common super type of all elements. If the given collection is
     * empty, the runtime type of the array is Object. Use
     * {@link #toArray(Collection, Class)} to specify a component type instead, if you have
     * elements of different types in your collection or the collection might be empty.
     *
     * @see #toObjectArray(Collection)
     * @see #toStringArray(Collection)
     *
     * @param aCollection
     *        the collection containing the elements to put into an array
     * @return a new array containing all elements from the given collection
     */
    public static Object[] toArray(Collection aCollection) {
        if (aCollection == null || aCollection.isEmpty()) return EMPTY_ARRAY;
        Iterator it = aCollection.iterator();
        Object first = it.next();
        Object[] a = (Object[])Array.newInstance(first == null ? Object.class : first.getClass(), aCollection.size());
        a[0] = first;
        for (int i = 1; it.hasNext(); i++) {
            a[i] = it.next();
        }
        return a;
    }

    /**
	 * Creates a new array containing all elements from the given collection. The runtime type of
	 * the returned array is of the given target type. All elements in the collection must be
	 * instanceof the given target type or <code>null</code>.
	 * 
	 * @see #toObjectArray(Collection)
	 * @see #toStringArray(Collection)
	 * 
	 * @param aCollection
	 *        the collection containing the elements to put into an array
	 * @param aComponentType
	 *        the component type of the resulting array
	 * @return a new array with the given component type containing all elements from the given
	 *         collection
	 * 
	 * @deprecated Use {@link #toArrayTyped(Collection, Class)}. Ensure that neither given
	 *             collection nor component type is null.
	 */
	@Deprecated
    public static Object[] toArray(Collection aCollection, Class aComponentType) {
        Object[] a = (Object[])Array.newInstance(aComponentType == null ? Object.class : aComponentType, aCollection == null ? 0 : aCollection.size());
        if (aCollection == null) return a;
        Iterator it = aCollection.iterator();
        for (int i = 0; it.hasNext(); i++) {
            a[i] = it.next();
        }
        return a;
    }

	/**
	 * Creates a new array containing all elements from the given collection. The runtime type of
	 * the returned array is of the given target type. All elements in the collection must be
	 * instance of the given target type or <code>null</code>.
	 * 
	 * @param source
	 *        The collection to create array from.
	 * @param componentType
	 *        The component type of the new Array.
	 */
	public static <T> T[] toArrayTyped(Collection<? extends T> source, Class<T> componentType) {
		int numberElements = source.size();
		@SuppressWarnings("unchecked")
		T[] a = (T[]) Array.newInstance(componentType, numberElements);
		if (source instanceof RandomAccess) {
			List<? extends T> listSource = (List<? extends T>) source;
			for (int i = 0; i < numberElements; i++) {
				a[i] = listSource.get(i);
			}
		} else {
			int i = 0;
			for (T element : source) {
				a[i++] = element;
			}
		}
		return a;
	}

    /**
     * Creates a new array containing all elements from the given collection. The runtime
     * type of the returned array is Object.
     *
     * @see #toArray(Collection)
     * @see #toStringArray(Collection)
     *
     * @param aCollection
     *        the collection containing the elements to put into an array
     * @return a new array containing all elements from the given collection
     */
    public static Object[] toObjectArray(Collection aCollection) {
        if (aCollection == null) return EMPTY_ARRAY;
        Object[] a = new Object[aCollection.size()];
        Iterator it = aCollection.iterator();
        for (int i = 0; it.hasNext(); i++) {
            a[i] = it.next();
        }
        return a;
    }

    /**
     * Creates a new string array containing the string representations of all elements from
     * the given collection. The runtime type of the returned array is String.
     *
     * @see #toArray(Collection)
     *
     * @param aCollection
     *        the collection containing the elements to put into an array
     * @return a new array containing the string representations of all elements from the
     *         given collection
     */
    public static String[] toStringArray(Collection aCollection) {
        if (aCollection == null) return EMPTY_STRING_ARRAY;
        String[] a = new String[aCollection.size()];
        Iterator it = aCollection.iterator();
        for (int i = 0; it.hasNext(); i++) {
            Object o = it.next();
            a[i] = (o == null ? "" : o.toString());
        }
        return a;
    }

    /**
     * Creates a new int array containing all values from the given collection. All elements
     * within the collection must be instanceof Number.
     *
     * @param aCollection
     *        the collection containing the values to put into an array
     * @return a new array containing the values of all elements from the given collection
     */
    public static int[] toIntArray(Collection aCollection) {
        if (aCollection == null) return EMPTY_INT_ARRAY;
        int[] a = new int[aCollection.size()];
        Iterator it = aCollection.iterator();
        for (int i = 0; it.hasNext(); i++) {
            a[i] = ((Number)it.next()).intValue();
        }
        return a;
    }

    /**
     * Creates a new double array containing all values from the given collection. All
     * elements within the collection must be instanceof Number.
     *
     * @param aCollection
     *        the collection containing the values to put into an array
     * @return a new array containing the values of all elements from the given collection
     */
    public static double[] toDoubleArray(Collection aCollection) {
        if (aCollection == null) return EMPTY_DOUBLE_ARRAY;
        double[] a = new double[aCollection.size()];
        Iterator it = aCollection.iterator();
        for (int i = 0; it.hasNext(); i++) {
            a[i] = ((Number)it.next()).doubleValue();
        }
        return a;
    }



    /**
     * Creates a new array with only one element, the given object, in it. The component
     * type of the resulting array is the runtime type of the given object. The resulting
     * array will always have a length of 1. If the given object is <code>null</code>, a
     * Object[] with a <code>null</code> element is returned.
     *
     * @param aObject
     *            the object to put into an array
     * @return a new array of length 1 with the given object in it
     */
    public static Object[] intoArray(Object aObject) {
        Object[] a = (aObject == null ? new Object[1] : (Object[])Array.newInstance(aObject.getClass(), 1));
        a[0] = aObject;
        return a;
    }

    /**
     * Creates a new string array with only one element, the given string, in it. The resulting
     * array will always have a length of 1. If the given string is <code>null</code>, a
     * String[] with a <code>null</code> element is returned.
     *
     * @param aString
     *            the object to put into an array
     * @return a new array of length 1 with the given object in it
     */
    public static String[] intoStringArray(String aString) {
        return new String[] {aString};
    }

    /**
     * Returns the first element of an array.
     *
     * @param aArray
     *            the array to get the first element from
     * @return the first element of the given array or <code>null</code>, if the array is
     *         empty.
     */
    public static Object getFirst(Object[] aArray) {
        return isEmpty(aArray) ? null : aArray[0];
    }

    /**
     * Returns the last element of an array.
     *
     * @param aArray
     *            the array to get the last element from
     * @return the last element of the given array or <code>null</code>, if the array is
     *         empty.
     */
    public static Object getLast(Object[] aArray) {
        return isEmpty(aArray) ? null : aArray[aArray.length - 1];
    }



    /**
     * This method creates a flat copy of an array. The runtime type of the returned array
     * is that of the given one.
     * <p>
     * Because of that you can use this method as follows:<br/>
     * <br/>
     * <code>
     * Double[] a1 = new Double[] {new Double(1.0), new Double(2.0)};<br/>
     * Double[] a2 = (Double[])clone(a1);<br/>
     * </code>
     * </p>
     *
     * @param aArray
     *            the array to copy
     * @return a flat copy of the given array
     */
    public static Object[] clone(Object[] aArray) {
        if (aArray == null) return null;
        return aArray.clone();
    }



    /**
     * Joins two arrays to one array together. If the two arrays have the same runtime type,
     * the runtime type of the returned array is that of the specified arrays. If one of the
     * two array's component type {@link Class#isAssignableFrom(Class)}} of the other
     * array's component type, the returned array will have this component type. Otherwise
     * the returned array's component type is Object.
     * <p>
     * Because of that you can use this method to join arrays for any types as follows:<br/>
     * <br/>
     * <code>
     * Double[]  a1 = new Double[] {new Double(1.0), new Double(2.0)};<br/>
     * Double[]  a2 = new Double[] {new Double(3.0), new Double(4.0)};<br/>
     * Double[]  a3 = (Double[])ArrayUtil.join(a1, a2);<br/>
     * Integer[] a4 = new Integer[] {Integer.valueOf(5), Integer.valueOf(6)};<br/>
     * Object[]  a5 = ArrayUtil.join(a3, a4);<br/>
     * </code>
     * </p>
     *
     * @param a1
     *            the first array
     * @param a2
     *            the second array
     * @return a new array containing the elements from a1 and a2. If one of the arrays is
     *         <code>null</code>, the other array is returned, checked on
     *         <code>null</code>. If both arrays are <code>null</code>, an empty
     *         Object[] is returned.
     */
    public static Object[] join(Object[] a1, Object[] a2) {
        if (a1 == null && a2 == null) return EMPTY_ARRAY;
        if (a1 == null) return checkOnNull(a2);
        if (a2 == null) return checkOnNull(a1);

        Object[] a;
        if (a1.getClass().getComponentType().isAssignableFrom(a2.getClass().getComponentType())) {
            a = (Object[])Array.newInstance(a1.getClass().getComponentType(), a1.length + a2.length);
        }
        else if (a2.getClass().getComponentType().isAssignableFrom(a1.getClass().getComponentType())) {
            a = (Object[])Array.newInstance(a2.getClass().getComponentType(), a1.length + a2.length);
        }
        else {
            a = new Object[a1.length + a2.length];
        }
        System.arraycopy(a1, 0, a, 0, a1.length);
        System.arraycopy(a2, 0, a, a1.length, a2.length);
        return a;
    }

    /**
     * Joins two arrays to one array together. The result of the join is saved into the
     * given target array. If the target array is to small, a new array with the same
     * runtime type will be created. So this method can be used as a <code>void</code>
     * method, if it is ensured that the target array is large enough to store all elements
     * of both source arrays.
     *
     * @param a1
     *            the first array
     * @param a2
     *            the second array
     * @param aTarget
     *            the target array to put the result of the join into
     * @return the given target array with the join result or a new array with the same
     *         runtime type, if the given array is not big enough.
     * @exception ArrayStoreException
     *                if an element in the source arrays could not be stored into the
     *                <code>target</code> array because of a type mismatch.
     */
    public static Object[] join(Object[] a1, Object[] a2, Object[] aTarget) {
        Object[] array1 = (a1 != null) ? a1 : EMPTY_ARRAY;
        Object[] array2 = (a2 != null) ? a2 : EMPTY_ARRAY;
        Object[] theTarget = (aTarget != null) ? aTarget : EMPTY_ARRAY;
        int size = array1.length + array2.length;

        if (theTarget.length < size) {
            theTarget = (Object[])Array.newInstance(theTarget.getClass().getComponentType(), size);
        }
        System.arraycopy(array1, 0, theTarget, 0, array1.length);
        System.arraycopy(array2, 0, theTarget, array1.length, array2.length);

        for (int i = size; i < theTarget.length; i++) {
            theTarget[i] = null;
        }
        return theTarget;
    }

	/**
	 * Joins two arrays to one array together. The result of the join is saved
	 * into a new array of the given target class.
	 * 
	 * <p>
	 * Because of that you can use this method to join arrays for any types as
	 * follows:<br/>
	 * <br/>
	 * <code>
	 * Double[]     a1 = new Double[] {new Double(1.0), new Double(2.0)};<br/>
	 * Integer[]    a2 = new Integer[] {Integer.valueOf(3), Integer.valueOf(4)};<br/>
	 * Number[]     a3 = (Number[])ArrayUtil.join(a1, a2, Number.class);<br/>
	 * String[]     a4 = new String[] {new String("5"), new String("6")};<br/>
	 * Comparable[] a5 = (Comparable[])ArrayUtil.join(a3, a4, Comparable.class);<br/>
	 * </code>
	 * </p>
	 * 
	 * @param <T>
	 *        type of the elements which can be stored in the result array
	 * @param a1
	 *        the first array
	 * @param a2
	 *        the second array
	 * @param aComponentType
	 *        the component type of the resulting array
	 * @return the given target array with the join result or a new array with
	 *         the same runtime type, if the given array is not big enough.
	 * @exception ArrayStoreException
	 *            if an element in the source arrays could not be stored into
	 *            the <code>target</code> array because of a type mismatch.
	 */
    public static <T> T[] join(Object[] a1, Object[] a2, Class<T> aComponentType) {
        Object[] array1 = (a1 != null) ? a1 : EMPTY_ARRAY;
        Object[] array2 = (a2 != null) ? a2 : EMPTY_ARRAY;

        int size = array1.length + array2.length;
        
		Object theTarget = Array.newInstance(aComponentType, size);

        System.arraycopy(array1, 0, theTarget, 0, array1.length);
        System.arraycopy(array2, 0, theTarget, array1.length, array2.length);
        
        @SuppressWarnings("unchecked")
		T[] result = (T[]) theTarget;
        
		return result;
    }

    /**
	 * Appends an object at the end of a array. If the objects runtime type is the same as the
	 * arrays component type, the runtime type of the returned array is that of the specified array,
	 * else the returned arrays component type is Object.
	 * <p>
	 * Because of that you can use this method to join arrays for any types as follows:<br/>
	 * <br/>
	 * <code>
	 * Double[] array = new Double[] {new Double(1.0), new Double(2.0)};<br/>
	 * Double object = new Double(3.0);<br/>
	 * Double[] result = (Double[])ArrayUtil.join(array, object);<br/>
	 * </code>
	 * </p>
	 *
	 * @param aArray
	 *        the array to append into
	 * @param aObject
	 *        the object to append
	 * @return a new array containing the given object and the elements from the given array. If the
	 *         object is <code>null</code>, the array is returned, checked on <code>null</code>. If
	 *         the array is <code>null</code>, a new array with only the object as element is
	 *         returned. If both parameters are <code>null</code>, an empty Object[] is returned.
	 * 
	 * @deprecated Use {@link #addElement(Object[], Object)}.
	 */
	@Deprecated
    public static Object[] join(Object[] aArray, Object aObject) {
        if (aObject == null) return checkOnNull(aArray);
        Object[] a = (Object[])Array.newInstance(aObject.getClass(), 1);
        a[0] = aObject;
        return aArray == null ? a : join(aArray, a);
    }

    /**
	 * Inserts an object at the beginning of a array. If the objects runtime type is the same as the
	 * arrays component type, the runtime type of the returned array is that of the specified array,
	 * else the returned arrays component type is Object.
	 * <p>
	 * Because of that you can use this method to join arrays for any types as follows:<br/>
	 * <br/>
	 * <code>
	 * Double object = new Double(1.0);<br/>
	 * Double[] array = new Double[] {new Double(2.0), new Double(3.0)};<br/>
	 * Double[] result = (Double[])ArrayUtil.join(object, array);<br/>
	 * </code>
	 * </p>
	 *
	 * @param aObject
	 *        the object to insert
	 * @param aArray
	 *        the array to insert into
	 * @return a new array containing the given object and the elements from the given array. If the
	 *         object is <code>null</code>, the array is returned, checked on <code>null</code>. If
	 *         the array is <code>null</code>, a new array with only the object as element is
	 *         returned. If both parameters are <code>null</code>, an empty Object[] is returned.
	 * 
	 * @deprecated Use {@link #addElement(Object, Object[])}.
	 */
	@Deprecated
    public static Object[] join(Object aObject, Object[] aArray) {
        if (aObject == null) return checkOnNull(aArray);
        Object[] a = (Object[])Array.newInstance(aObject.getClass(), 1);
        a[0] = aObject;
        return aArray == null ? a : join(a, aArray);
    }


	/**
	 * Appends an object at the end of a array. If the objects runtime type is the same as the
	 * arrays component type, the runtime type of the returned array is that of the specified array,
	 * else the returned arrays component type is Object.
	 * <p>
	 * Because of that you can use this method to join arrays for any types as follows:<br/>
	 * <br/>
	 * <code>
	 * Double[] array = new Double[] {new Double(1.0), new Double(2.0)};<br/>
	 * Double object = new Double(3.0);<br/>
	 * Double[] result = (Double[])ArrayUtil.join(array, object);<br/>
	 * </code>
	 * </p>
	 *
	 * @param src
	 *        the array to append into
	 * @param elem
	 *        the object to append
	 * @return a new array containing the given object and the elements from the given array. If the
	 *         object is <code>null</code>, the array is returned, checked on <code>null</code>. If
	 *         the array is <code>null</code>, a new array with only the object as element is
	 *         returned. If both parameters are <code>null</code>, an empty Object[] is returned.
	 */
	public static <T> T[] addElement(T[] src, T elem) {
		@SuppressWarnings("unchecked")
		T[] result = (T[]) Array.newInstance(src.getClass().getComponentType(), src.length + 1);
		System.arraycopy(src, 0, result, 0, src.length);
		result[src.length] = elem;
		return result;
	}

	/**
	 * Inserts an object at the beginning of a array. If the objects runtime type is the same as the
	 * arrays component type, the runtime type of the returned array is that of the specified array,
	 * else the returned arrays component type is Object.
	 * <p>
	 * Because of that you can use this method to join arrays for any types as follows:<br/>
	 * <br/>
	 * <code>
	 * Double object = new Double(1.0);<br/>
	 * Double[] array = new Double[] {new Double(2.0), new Double(3.0)};<br/>
	 * Double[] result = (Double[])ArrayUtil.join(object, array);<br/>
	 * </code>
	 * </p>
	 *
	 * @param elem
	 *        the object to insert
	 * @param src
	 *        the array to insert into
	 * @return a new array containing the given object and the elements from the given array. If the
	 *         object is <code>null</code>, the array is returned, checked on <code>null</code>. If
	 *         the array is <code>null</code>, a new array with only the object as element is
	 *         returned. If both parameters are <code>null</code>, an empty Object[] is returned.
	 */
	public static <T> T[] addElement(T elem, T[] src) {
		@SuppressWarnings("unchecked")
		T[] result = (T[]) Array.newInstance(src.getClass().getComponentType(), src.length + 1);
		System.arraycopy(src, 0, result, 1, src.length);
		result[0] = elem;
		return result;
	}

    // Arithmetic operations:
    // ----------------------

    /**
	 * This method computes the AND function of all values in the given array.
	 * 
	 * @param values
	 *        The array with the values. Must not be <code>null</code>.
	 * 
	 * @return The AND function result on all values in the given array.
	 */
	public static boolean and(boolean... values) {
		return and(values, 0, values.length);
    }

    /**
	 * This method computes the OR function of all values in the given array.
	 * 
	 * @param values
	 *        The array with the values. Must not be <code>null</code>.
	 * @return the OR function result on all values in the given array
	 */
	public static boolean or(boolean... values) {
		return or(values, 0, values.length);
    }



    /**
	 * This method computes the AND function of all values in the given array within the given
	 * range.
	 * 
	 * @param values
	 *        The array with the values. Must not be <code>null</code>
	 * @param start
	 *        The start index (inclusive).
	 * @param end
	 *        The end index (exclusive).
	 * 
	 * @return The AND function result on all values in the given array within the given range.
	 * 
	 * @throws IndexOutOfBoundsException
	 *         if <code>start &lt; 0</code> or <code>end >= values.length</code>.
	 */
    public static boolean and(boolean[] values, int start, int end) {
		for (int i = start; i < end; i++) {
            if (!values[i]) return false;
        }
        return true;
    }

    /**
	 * This method computes the OR function of all values in the given array within the given range.
	 * 
	 * @param values
	 *        The array with the values. Must not be <code>null</code>.
	 * @param start
	 *        The start index (inclusive).
	 * @param end
	 *        The end index (exclusive).
	 * 
	 * @return The OR function result on all values in the given array within the given range
	 * 
	 * @throws IndexOutOfBoundsException
	 *         if <code>start &lt; 0</code> or <code>end >= values.length</code>.
	 */
    public static boolean or(boolean[] values, int start, int end) {
		for (int i = start; i < end; i++) {
            if (values[i]) return true;
        }
        return false;
    }



    /**
     * This method computes the sum of all elements in the given array.
     *
     * @param summands
     *            the array with the summands
     * @return the sum of all elements in the given array
     */
    public static int sum(int[] summands) {
        return summands == null ? 0 : sum(summands, 0, summands.length - 1);
    }

    /**
     * This method computes the sum of all elements in the given array.
     *
     * @param summands
     *            the array with the summands
     * @return the sum of all elements in the given array
     */
    public static double sum(double[] summands) {
        return summands == null ? 0.0 : sum(summands, 0, summands.length - 1);
    }



    /**
     * This method computes the sum of all elements in the given array within the given
     * range.
     *
     * @param summands
     *        the array with the summands
     * @param start
     *        the start index(inclusive)
     * @param end
     *        the end index (inclusive)
     * @return the sum of all elements in the given array within the given range
     */
    public static int sum(int[] summands, int start, int end) {
        int sum = 0;
        if (summands == null || start > end) return sum;
        for (int i = start; i <= end; i++) {
            sum += summands[i];
        }
        return sum;
    }

    /**
     * This method computes the sum of all elements in the given array within the given
     * range.
     *
     * @param summands
     *        the array with the summands
     * @param start
     *        the start index(inclusive)
     * @param end
     *        the end index (inclusive)
     * @return the sum of all elements in the given array within the given range
     */
    public static double sum(double[] summands, int start, int end) {
        double sum = 0.0;
        if (summands == null || start > end) return sum;
        for (int i = start; i <= end; i++) {
            sum += summands[i];
        }
        return sum;
    }



    /**
     * This method adds to each value of the result array the corresponding value from the
     * summand array. If the arrays haven't the same length, only the first elements are
     * processed.
     *
     * @param aResultArray
     *            the array where the new values shall be added
     * @param aSummandArray
     *            the array with the values to add to the result array
     */
    public static void add(int[] aResultArray, int[] aSummandArray) {
        if (aResultArray == null || aSummandArray == null) {
            return;
        }
        int maxLength = Math.min(aResultArray.length, aSummandArray.length);
        for (int i = 0; i < maxLength; i++) {
            aResultArray[i] += aSummandArray[i];
        }
    }

    /**
     * This method adds to each value of the result array the corresponding value from the
     * summand array. If the arrays haven't the same length, only the first elements are
     * processed.
     *
     * @param aResultArray
     *            the array where the new values shall be added
     * @param aSummandArray
     *            the array with the values to add to the result array
     */
    public static void add(double[] aResultArray, double[] aSummandArray) {
        if (aResultArray == null || aSummandArray == null) {
            return;
        }
        int maxLength = Math.min(aResultArray.length, aSummandArray.length);
        for (int i = 0; i < maxLength; i++) {
            aResultArray[i] += aSummandArray[i];
        }
    }

	/**
	 * Creates a new array of the same length as the source array which contains
	 * all entries of the source array in the same order.
	 * 
	 * @param source
	 *        the source array must not be null
	 * 
	 * @return a copy of the source array
	 */
	public static Object[] copy(Object... source) {
		final int sourceLength = source.length;
		if (sourceLength == 0) {
			return new Object[0];
		}
		Object[] resultObjects = new Object[sourceLength];
		System.arraycopy(source, 0, resultObjects, 0, sourceLength);
		return resultObjects;
	}

	/**
	 * Joins that given byte arrays.
	 * 
	 * @param first
	 *        The first array.
	 * @param second
	 *        The second array.
	 * @return Th concatenation of the first and the second array.
	 */
	public static byte[] join(byte[] first, byte[] second) {
		int firstLength = first.length;
		int secondLength = second.length;
		byte[] result = new byte[firstLength + secondLength];
		System.arraycopy(first, 0, result, 0, firstLength);
		System.arraycopy(second, 0, result, firstLength, secondLength);
		return result;
	}

	/**
	 * Copies a region from a given byte array.
	 * 
	 * @param src
	 *        The array to copy from.
	 * @param start
	 *        The first index to to copy.
	 * @param length
	 *        The number of entries to copy.
	 * @return A new array with the copied contents.
	 */
	public static byte[] copy(byte[] src, int start, int length) {
		byte[] result = new byte[length];
		System.arraycopy(src, start, result, 0, length);
		return result;
	}

	/**
	 * <code>null</code> is converted to byte[0].
	 * <p>
	 * Otherwise the original array is returned unchanged.
	 * </p>
	 * 
	 * @param nullable
	 *        Is allowed to be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	public static byte[] nonNull(byte[] nullable) {
		return nullable == null ? EMPTY_BYTE_ARRAY : nullable;
	}

	/**
	 * <code>null</code> is converted to short[0].
	 * <p>
	 * Otherwise the original array is returned unchanged.
	 * </p>
	 * 
	 * @param nullable
	 *        Is allowed to be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	public static short[] nonNull(short[] nullable) {
		return nullable == null ? EMPTY_SHORT_ARRAY : nullable;
	}

	/**
	 * <code>null</code> is converted to int[0].
	 * <p>
	 * Otherwise the original array is returned unchanged.
	 * </p>
	 * 
	 * @param nullable
	 *        Is allowed to be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	public static int[] nonNull(int[] nullable) {
		return nullable == null ? EMPTY_INT_ARRAY : nullable;
	}

	/**
	 * <code>null</code> is converted to long[0].
	 * <p>
	 * Otherwise the original array is returned unchanged.
	 * </p>
	 * 
	 * @param nullable
	 *        Is allowed to be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	public static long[] nonNull(long[] nullable) {
		return nullable == null ? EMPTY_LONG_ARRAY : nullable;
	}

	/**
	 * <code>null</code> is converted to char[0].
	 * <p>
	 * Otherwise the original array is returned unchanged.
	 * </p>
	 * 
	 * @param nullable
	 *        Is allowed to be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	public static char[] nonNull(char[] nullable) {
		return nullable == null ? EMPTY_CHAR_ARRAY : nullable;
	}

	/**
	 * <code>null</code> is converted to boolean[0].
	 * <p>
	 * Otherwise the original array is returned unchanged.
	 * </p>
	 * 
	 * @param nullable
	 *        Is allowed to be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	public static boolean[] nonNull(boolean[] nullable) {
		return nullable == null ? EMPTY_BOOLEAN_ARRAY : nullable;
	}

	/**
	 * <code>null</code> is converted to float[0].
	 * <p>
	 * Otherwise the original array is returned unchanged.
	 * </p>
	 * 
	 * @param nullable
	 *        Is allowed to be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	public static float[] nonNull(float[] nullable) {
		return nullable == null ? EMPTY_FLOAT_ARRAY : nullable;
	}

	/**
	 * <code>null</code> is converted to double[0].
	 * <p>
	 * Otherwise the original array is returned unchanged.
	 * </p>
	 * 
	 * @param nullable
	 *        Is allowed to be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	public static double[] nonNull(double[] nullable) {
		return nullable == null ? EMPTY_DOUBLE_ARRAY : nullable;
	}

	/**
	 * <code>null</code> is converted to Object[0].
	 * <p>
	 * Otherwise the original array is returned unchanged.
	 * </p>
	 * 
	 * @param nullable
	 *        Is allowed to be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	public static Object[] nonNull(Object[] nullable) {
		return nullable == null ? EMPTY_OBJECT_ARRAY : nullable;
	}

	/**
	 * Reverses the order of the elements in the specified array.
	 * <p>
	 * This method runs in linear time.
	 * </p>
	 *
	 * @param arr
	 *        the array whose elements are to be reversed.
	 * @return The given array.
	 */
	public static <T> T[] reverse(T[] arr) {
		int length = arr.length;
		switch (length) {
			case 0:
			case 1:
				return arr;
			case 2:
				swap(arr, 0, 1);
				return arr;
			case 3:
				swap(arr, 0, 2);
				return arr;
			default:
				return reverse(arr, 0, length);
		}
	}

	/**
	 * Reverses the order of the elements in the specified array.
	 * <p>
	 * This method runs in linear time.
	 * </p>
	 * 
	 * @param arr
	 *        the array whose elements are to be reversed.
	 * @param first
	 *        The index in the array from which reversing occurs. 0 based. Must be a valid index in
	 *        the array.
	 * @param length
	 *        The number of elements to reverse. Must be > 0. <code>first+length</code> must be less
	 *        than the length of the array.
	 *
	 * @return The given array.
	 */
	public static <T> T[] reverse(T[] arr, int first, int length) {
		if (length < 1) {
			throw new IllegalArgumentException(
				"Number of elements to reverse must be strict positive: '" + length + "'.");
		}
		int last = first + (length - 1);
		if (last < first) {
			// Overflow
			throw new IllegalArgumentException("Number overflow: first=" + first + ",length=" + length);
		}
		while (last > first) {
			swap(arr, first, last);
			last--;
			first++;
		}
		return arr;
	}

	/**
	 * Swaps the two specified elements in the specified array.
	 */
	private static <T> void swap(T[] arr, int i, int j) {
		T tmp = arr[i];
		arr[i] = arr[j];
		arr[j] = tmp;
	}

	/**
	 * Randomly permutes the specified array using a default source of randomness. All permutations
	 * occur with approximately equal likelihood.
	 *
	 * <p>
	 * The hedge "approximately" is used in the foregoing description because default source of
	 * randomness is only approximately an unbiased source of independently chosen bits. If it were
	 * a perfect source of randomly chosen bits, then the algorithm would choose permutations with
	 * perfect uniformity.
	 * </p>
	 *
	 * <p>
	 * This implementation traverses the array backwards, from the last element up to the second,
	 * repeatedly swapping a randomly selected element into the "current position". Elements are
	 * randomly selected from the portion of the list that runs from the first element to the
	 * current position, inclusive.
	 * </p>
	 *
	 * <p>
	 * This method runs in linear time.
	 * </p>
	 *
	 * @param arr
	 *        the array to be shuffled.
	 * 
	 * @see Collections#shuffle(List, Random)
	 */
	public static <T> void shuffle(T[] arr, Random rnd) {
		for (int i = arr.length; i > 1; i--) {
			swap(arr, i - 1, rnd.nextInt(i));
		}
	}

	/**
	 * Shuffles the given array with a newly created random.
	 * 
	 * @see #shuffle(Object[], Random)
	 */
	public static <T> void shuffle(T[] arr) {
		shuffle(arr, new Random());
	}

	/**
	 * Performs the given action for each element of the given array until all elements have been
	 * processed or the action throws an exception. Exceptions thrown by the action are relayed to
	 * the caller.
	 *
	 * @param action
	 *        The action to be performed for each element.
	 * @throws NullPointerException
	 *         if the specified action or array is <code>null</code>.
	 * 
	 * @see Iterable#forEach(Consumer)
	 */
	public static <T> void forEach(T[] array, Consumer<? super T> action) {
		forEach(array, 0, array.length, action);
	}

	/**
	 * Performs the given action for each element of the given array until all elements have been
	 * processed or the action throws an exception. Exceptions thrown by the action are relayed to
	 * the caller.
	 *
	 * @param action
	 *        The action to be performed for each element.
	 * @param start
	 *        index of the first element to process.
	 * @param stop
	 *        first index of the element greater than start not to process.
	 * @throws NullPointerException
	 *         if the specified action or array is <code>null</code>.
	 * @throws IndexOutOfBoundsException
	 *         if start &lt; 0, or stop > length of the array, or start > stop.
	 * 
	 * @see Iterable#forEach(Consumer)
	 */
	public static <T> void forEach(T[] array, int start, int stop, Consumer<? super T> action) {
		Objects.requireNonNull(action);
		if (start > stop) {
			throw new IllegalArgumentException(start + ">" + stop);
		}
		for (int i = start; i < stop; i++) {
			action.accept(array[i]);
		}
	}

	/**
	 * Filters the elements in the given array, with the given filter.
	 * 
	 * <p>
	 * The entries of the array the match the given filter are moved to the front of the array (in
	 * the original order). The slot immediately following the last matching entry (if there is one)
	 * is set to <code>null</code>.
	 * </p>
	 * 
	 * @param array
	 *        The array to filter.
	 * @param filter
	 *        The filter to apply.
	 * @return Number of elements in the array that match the given filter.
	 */
	public static <T> int filterInline(T[] array, Predicate<? super T> filter) {
		int length = array.length;
		if (length == 0) {
			return 0;
		}
		int writeIndex = 0;
		for (int readIndex = 0; readIndex < length; readIndex++) {
			T obj = array[readIndex];
			if (filter.test(obj)) {
				if (writeIndex != readIndex) {
					array[writeIndex] = obj;
				}
				writeIndex++;
			}
		}
		if (writeIndex < length) {
			array[writeIndex] = null;
		}
		return writeIndex;
	}
}
