/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import static com.top_logic.basic.ArrayUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.top_logic.basic.ArrayUtil;

/**
 * Testcase for {@link ArrayUtil}.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
@SuppressWarnings("javadoc")
public class TestArrayUtil extends BasicTestCase {

	public void testFilter() {
		Integer[] array = new Integer[] { 8, 1, 4, 6, 3, 7 };
		
		int numberElements = ArrayUtil.filterInline(array, number -> number > 3);
		assertEquals(4, numberElements);
		assertEquals(8, array[0].intValue());
		assertEquals(4, array[1].intValue());
		assertEquals(6, array[2].intValue());
		assertEquals(7, array[3].intValue());
		assertNull(array[4]);

		array = new Integer[0];
		assertEquals(0, ArrayUtil.filterInline(array, number -> number > 3));
	}

	public void testReverse() {
		// Low number of elements
		assertEquals(new Integer[] {}, ArrayUtil.reverse(new Integer[] {}));
		assertEquals(new Integer[] { 1 }, ArrayUtil.reverse(new Integer[] { 1 }));
		assertEquals(new Integer[] { 1, 2 }, ArrayUtil.reverse(new Integer[] { 2, 1 }));
		assertEquals(new Integer[] { 1, 2, 3 }, ArrayUtil.reverse(new Integer[] { 3, 2, 1 }));

		// test odd number of entries
		assertEquals(new Integer[] { 1, 2, 3, 4, 5 }, ArrayUtil.reverse(new Integer[] { 5, 4, 3, 2, 1 }));
		// test even number of entries
		assertEquals(new Integer[] { 1, 2, 3, 4 }, ArrayUtil.reverse(new Integer[] { 4, 3, 2, 1 }));

		// test partial reverse
		assertEquals(new Integer[] { 1, 2, 3, 4, 5, 6, 7 },
			ArrayUtil.reverse(new Integer[] { 1, 2, 5, 4, 3, 6, 7 }, 2, 3));
		assertEquals(new Integer[] { 1, 2, 3, 4, 5, 6, 7 },
			ArrayUtil.reverse(new Integer[] { 1, 2, 6, 5, 4, 3, 7 }, 2, 4));

		// Error case
		try {
			ArrayUtil.reverse(new Integer[] { 1, 2, 6, 5, 4, 3, 7 }, -1, 4);
			fail("first index is negative");
		} catch (Exception ex) {
			// expected
		}
		try {
			ArrayUtil.reverse(new Integer[] { 1, 2, 6, 5, 4, 3, 7 }, 3, 5);
			fail("Too many entries to reverse");
		} catch (Exception ex) {
			// expected
		}
		try {
			ArrayUtil.reverse(new Integer[] { 1, 2, 6, 5, 4, 3, 7 }, 3, -1);
			fail("Negative number of entries to reverse");
		} catch (Exception ex) {
			// expected
		}
		try {
			ArrayUtil.reverse(new Integer[] { 1, 2, 6, 5, 4, 3, 7 }, 3, Integer.MAX_VALUE);
			fail("Too many number of entries to reverse");
		} catch (Exception ex) {
			// expected
		}
	}
	public void testJoinBytes() {
		doTestJoinBytes(0, 0);
		doTestJoinBytes(1, 1);
		doTestJoinBytes(1, 10);
		doTestJoinBytes(0, 10);
		doTestJoinBytes(5, 10);
	}

	private void doTestJoinBytes(int firstLength, int secondLength) {
		Random rnd = new Random();
		byte[] first = new byte[firstLength];
		byte[] second = new byte[secondLength];
		rnd.nextBytes(first);
		rnd.nextBytes(second);
		byte[] concatenation = ArrayUtil.join(first, second);
		byte[] firstCopy = ArrayUtil.copy(concatenation, 0, firstLength);
		byte[] secondCopy = ArrayUtil.copy(concatenation, firstLength, secondLength);
		assertTrue(Arrays.equals(first, firstCopy));
		assertTrue(Arrays.equals(second, secondCopy));
	}
	
    public void testIsEmpty() {
        Integer[] a = null;
        assertTrue(ArrayUtil.isEmpty(a));
        a = new Integer[0];
        assertTrue(ArrayUtil.isEmpty(a));
        a = new Integer[] {Integer.valueOf(5)};
        assertFalse(ArrayUtil.isEmpty(a));
        a[0] = null;
        assertFalse(ArrayUtil.isEmpty(a));
        a = new Integer[] {Integer.valueOf(5), Integer.valueOf(4)};
        assertFalse(ArrayUtil.isEmpty(a));

        int[] b = null;
        assertTrue(ArrayUtil.isEmpty(b));
        b = new int[0];
        assertTrue(ArrayUtil.isEmpty(b));
        b = new int[] {5};
        assertFalse(ArrayUtil.isEmpty(b));
        b[0] = 0;
        assertFalse(ArrayUtil.isEmpty(b));
        b = new int[] {5, 4};
        assertFalse(ArrayUtil.isEmpty(b));

        double[] c = null;
        assertTrue(ArrayUtil.isEmpty(c));
        c = new double[0];
        assertTrue(ArrayUtil.isEmpty(c));
        c = new double[] {5.0};
        assertFalse(ArrayUtil.isEmpty(c));
        c[0] = 0.0;
        assertFalse(ArrayUtil.isEmpty(c));
        c = new double[] {5.0, 4.0};
        assertFalse(ArrayUtil.isEmpty(c));
    }

    public void testIsContentEmpty() {
        Integer[] a = null;
        assertTrue(ArrayUtil.isContentEmpty(a));
        a = new Integer[0];
        assertTrue(ArrayUtil.isContentEmpty(a));
        a = new Integer[] {Integer.valueOf(5)};
        assertFalse(ArrayUtil.isContentEmpty(a));
        a[0] = null;
        assertTrue(ArrayUtil.isContentEmpty(a));
        a = new Integer[] {Integer.valueOf(5), Integer.valueOf(4)};
        assertFalse(ArrayUtil.isContentEmpty(a));
        a[0] = null;
        assertFalse(ArrayUtil.isContentEmpty(a));
        a[1] = null;
        assertTrue(ArrayUtil.isContentEmpty(a));

        int[] b = null;
        assertTrue(ArrayUtil.isContentEmpty(b));
        b = new int[0];
        assertTrue(ArrayUtil.isContentEmpty(b));
        b = new int[] {5};
        assertFalse(ArrayUtil.isContentEmpty(b));
        b[0] = 0;
        assertTrue(ArrayUtil.isContentEmpty(b));
        b = new int[] {5, 4};
        assertFalse(ArrayUtil.isContentEmpty(b));
        b[0] = 0;
        assertFalse(ArrayUtil.isContentEmpty(b));
        b[1] = 0;
        assertTrue(ArrayUtil.isContentEmpty(b));

        String[] d = null;
        assertTrue(ArrayUtil.isContentEmpty(d));
        d = new String[0];
        assertTrue(ArrayUtil.isContentEmpty(d));
        d = new String[] {"Hello"};
        assertFalse(ArrayUtil.isContentEmpty(d));
        d[0] = "";
        assertTrue(ArrayUtil.isContentEmpty(d));
        d[0] = null;
        assertTrue(ArrayUtil.isContentEmpty(d));
        d = new String[] {"Hello", "World"};
        assertFalse(ArrayUtil.isContentEmpty(d));
        d[0] = "";
        assertFalse(ArrayUtil.isContentEmpty(d));
        d[0] = null;
        assertFalse(ArrayUtil.isContentEmpty(d));
        d[1] = "";
        assertTrue(ArrayUtil.isContentEmpty(d));
        d[1] = null;
        assertTrue(ArrayUtil.isContentEmpty(d));
        d[0] = "";
        assertTrue(ArrayUtil.isContentEmpty(d));
        d[1] = "";
        assertTrue(ArrayUtil.isContentEmpty(d));
        d[1] = "!";
        assertFalse(ArrayUtil.isContentEmpty(d));
        d[1] = " ";
        assertFalse(ArrayUtil.isContentEmpty(d));
    }

    public void testCheckOnNull() {
        Integer[] a = null;
        assertNotNull(ArrayUtil.checkOnNull(a));
        a = new Integer[0];
        assertNotNull(ArrayUtil.checkOnNull(a));
        a = new Integer[] {Integer.valueOf(5)};
        assertNotNull(ArrayUtil.checkOnNull(a));
        a[0] = null;
        assertNotNull(ArrayUtil.checkOnNull(a));

        String[] b = null;
        assertNotNull(ArrayUtil.checkOnNull(b));
        b = new String[0];
        assertNotNull(ArrayUtil.checkOnNull(b));
        b = new String[] {"A"};
        assertNotNull(ArrayUtil.checkOnNull(b));
        b[0] = null;
        assertNotNull(ArrayUtil.checkOnNull(b));

        int[] c = null;
        assertNotNull(ArrayUtil.checkOnNull(c));
        c = new int[0];
        assertNotNull(ArrayUtil.checkOnNull(c));
        c = new int[] {5};
        assertNotNull(ArrayUtil.checkOnNull(c));
        c[0] = 0;
        assertNotNull(ArrayUtil.checkOnNull(c));

        double[] d = null;
        assertNotNull(ArrayUtil.checkOnNull(d));
        d = new double[0];
        assertNotNull(ArrayUtil.checkOnNull(d));
        d = new double[] {5.0};
        assertNotNull(ArrayUtil.checkOnNull(d));
        d[0] = 0.0;
        assertNotNull(ArrayUtil.checkOnNull(d));
    }

    public void testClear() {
        String[] array1 = {"A", "B", "C", "D", "E"};
        ArrayUtil.clear(array1);
        assertEquals(array1.length, 5);
        assertNull(array1[0]);
        assertNull(array1[1]);
        assertNull(array1[2]);
        assertNull(array1[3]);
        assertNull(array1[4]);
        ArrayUtil.clear(array1);
        assertEquals(array1.length, 5);
        assertNull(array1[0]);
        assertNull(array1[1]);
        assertNull(array1[2]);
        assertNull(array1[3]);
        assertNull(array1[4]);

        ArrayUtil.clearStringArray(array1);
        assertEquals(array1.length, 5);
        assertEquals(array1[0], "");
        assertEquals(array1[1], "");
        assertEquals(array1[2], "");
        assertEquals(array1[3], "");
        assertEquals(array1[4], "");
        ArrayUtil.clearStringArray(array1);
        assertEquals(array1.length, 5);
        assertEquals(array1[0], "");
        assertEquals(array1[1], "");
        assertEquals(array1[2], "");
        assertEquals(array1[3], "");
        assertEquals(array1[4], "");

        array1 = new String[] {"A", "B", "C"};
        ArrayUtil.clearStringArray(array1);
        assertEquals(array1.length, 3);
        assertEquals(array1[0], "");
        assertEquals(array1[1], "");
        assertEquals(array1[2], "");

        int[] array2 = {1, 2, 3, 4, 5};
        ArrayUtil.clear(array2);
        assertEquals(array2.length, 5);
        assertEquals(array2[0], 0);
        assertEquals(array2[1], 0);
        assertEquals(array2[2], 0);
        assertEquals(array2[3], 0);
        assertEquals(array2[4], 0);
        ArrayUtil.clear(array2);
        assertEquals(array2.length, 5);
        assertEquals(array2[0], 0);
        assertEquals(array2[1], 0);
        assertEquals(array2[2], 0);
        assertEquals(array2[3], 0);
        assertEquals(array2[4], 0);

        double[] array3 = {1.0, 2.0, 3.0, 4.0, 5.0};
        ArrayUtil.clear(array3);
        assertEquals(array3.length, 5);
        assertEquals(array3[0], 0.0, 0.0);
        assertEquals(array3[1], 0.0, 0.0);
        assertEquals(array3[2], 0.0, 0.0);
        assertEquals(array3[3], 0.0, 0.0);
        assertEquals(array3[4], 0.0, 0.0);
        ArrayUtil.clear(array3);
        assertEquals(array3.length, 5);
        assertEquals(array3[0], 0.0, 0.0);
        assertEquals(array3[1], 0.0, 0.0);
        assertEquals(array3[2], 0.0, 0.0);
        assertEquals(array3[3], 0.0, 0.0);
        assertEquals(array3[4], 0.0, 0.0);
    }

    public void testFill() {
        String[] array1 = {"A", "B", "C", "D", "E"};
        ArrayUtil.fill(array1, "X");
        assertEquals(array1.length, 5);
        assertEquals("X", array1[0]);
        assertEquals("X", array1[1]);
        assertEquals("X", array1[2]);
        assertEquals("X", array1[3]);
        assertEquals("X", array1[4]);
        ArrayUtil.fill(array1, "X");
        assertEquals(array1.length, 5);
        assertEquals("X", array1[0]);
        assertEquals("X", array1[1]);
        assertEquals("X", array1[2]);
        assertEquals("X", array1[3]);
        assertEquals("X", array1[4]);

        array1 = new String[] {"A", null, "C", "D", null};
        ArrayUtil.fillEmpty(array1, "Y");
        assertEquals(array1.length, 5);
        assertEquals("A", array1[0]);
        assertEquals("Y", array1[1]);
        assertEquals("C", array1[2]);
        assertEquals("D", array1[3]);
        assertEquals("Y", array1[4]);
        ArrayUtil.fillEmpty(array1, "Z");
        assertEquals(array1.length, 5);
        assertEquals("A", array1[0]);
        assertEquals("Y", array1[1]);
        assertEquals("C", array1[2]);
        assertEquals("D", array1[3]);
        assertEquals("Y", array1[4]);


        array1 = new String[] {"A", "B", "C"};
        ArrayUtil.fill(array1, "A");
        assertEquals(array1.length, 3);
        assertEquals(array1[0], "A");
        assertEquals(array1[1], "A");
        assertEquals(array1[2], "A");
        array1 = new String[] {"A", null, ""};
        ArrayUtil.fillEmptyStrings(array1, "B");
        assertEquals(array1.length, 3);
        assertEquals(array1[0], "A");
        assertEquals(array1[1], "B");
        assertEquals(array1[2], "B");


        int[] array2 = {1, 2, 3, 4, 5};
        ArrayUtil.fill(array2, 5);
        assertEquals(array2.length, 5);
        assertEquals(array2[0], 5);
        assertEquals(array2[1], 5);
        assertEquals(array2[2], 5);
        assertEquals(array2[3], 5);
        assertEquals(array2[4], 5);
        array2 = new int[] {1, 0, 3, 0, 5};
        ArrayUtil.fillEmpty(array2, 5);
        assertEquals(array2.length, 5);
        assertEquals(array2[0], 1);
        assertEquals(array2[1], 5);
        assertEquals(array2[2], 3);
        assertEquals(array2[3], 5);
        assertEquals(array2[4], 5);


        double[] array3 = {1.0, 2.0, 3.0, 4.0, 5.0};
        ArrayUtil.fill(array3, 5.0);
        assertEquals(array3.length, 5);
        assertEquals(array3[0], 5.0, 0.0);
        assertEquals(array3[1], 5.0, 0.0);
        assertEquals(array3[2], 5.0, 0.0);
        assertEquals(array3[3], 5.0, 0.0);
        assertEquals(array3[4], 5.0, 0.0);

        Object[][] array4 = new Object[3][4];
        for (int i = 0; i < array4.length; i++) {
            for (int j = 0; j < array4[i].length; j++) {
                assertNull(array4[i][j]);
            }
        }
        ArrayUtil.fill(array4, "I");
        for (int i = 0; i < array4.length; i++) {
            for (int j = 0; j < array4[i].length; j++) {
                assertEquals("I", array4[i][j]);
            }
        }
        for (int i = 0; i < array4.length; i++) {
            for (int j = 0; j < array4[i].length; j++) {
                if (i == j) {
                    array4[i][j] = null;
                    assertNull(array4[i][j]);
                }
                else {
                    assertEquals("I", array4[i][j]);
                }
            }
        }
        ArrayUtil.fillEmpty(array4, "J");
        for (int i = 0; i < array4.length; i++) {
            for (int j = 0; j < array4[i].length; j++) {
                assertEquals((i == j ? "J" : "I"), array4[i][j]);
            }
        }

        array4 = new String[0][0];
        ArrayUtil.fillEmpty(array4, "K");
        assertEquals(0, array4.length);
    }

    public void testFillArray() {
        assertNull(ArrayUtil.fillArray(null, null));
        assertNull(ArrayUtil.fillArray(null, this.getClass()));

        Object[] array1 = ArrayUtil.fillArray(new Object[3], null);
        assertNotNull(array1);
        assertEquals(3, array1.length);
        for (int i = 0; i < array1.length; i++) {
            assertNull(array1[i]);
        }

        ArrayUtil.fillArray(array1, String.class);
        assertNotNull(array1);
        assertEquals(3, array1.length);
        for (int i = 0; i < array1.length; i++) {
            assertEquals(String.class, array1[i].getClass());
            assertEquals("", array1[i]);
        }
        assertTrue(array1[0] != array1[1]);
        assertTrue(array1[0] != array1[2]);
        assertTrue(array1[1] != array1[2]);

        ArrayUtil.fillArray(array1, Object.class);
        for (int i = 0; i < array1.length; i++) {
            assertEquals(Object.class, array1[i].getClass());
        }
        assertTrue(array1[0] != array1[1]);
        assertTrue(array1[0] != array1[2]);
        assertTrue(array1[1] != array1[2]);

        array1 = new Object[] {Integer.valueOf(5), null, Double.valueOf(3.0)};
        ArrayUtil.fillArrayEmpty(array1, String.class);
        assertEquals(Integer.class, array1[0].getClass());
        assertEquals(String.class, array1[1].getClass());
        assertEquals(Double.class, array1[2].getClass());
        assertEquals("", array1[1]);


        Object[][] array2 = (String[][]) ArrayUtil.fillArray(new String[3][5], null);
        assertNotNull(array2);
        assertEquals(3, array2.length);
        assertEquals(5, array2[0].length);
        for (int i = 0; i < array2.length; i++) {
            assertTrue(array2[i] instanceof String[]);
            for (int j = 0; j < array2[i].length; j++) {
                assertNull(array2[i][j]);
            }
        }

        ArrayUtil.fillArray(array2, String.class);
        assertNotNull(array2);
        for (int i = 0; i < array2.length; i++) {
            for (int j = 0; j < array2[i].length; j++) {
                assertEquals(String.class, array2[i][j].getClass());
                assertEquals("", array2[i][j]);
            }
        }

        ArrayUtil.fill(array2, "X");
        for (int i = 0; i < array2.length; i++) {
            for (int j = 0; j < array2[i].length; j++) {
                assertEquals("X", array2[i][j]);
                if (i == j) {
                    array2[i][j] = null;
                    assertNull(array2[i][j]);
                }
            }
        }

        ArrayUtil.fillArrayEmpty(array2, String.class);
        for (int i = 0; i < array2.length; i++) {
            for (int j = 0; j < array2[i].length; j++) {
                assertEquals((i == j ? "" : "X"), array2[i][j]);
            }
        }

    }

    public void testIndexOf() {
        String[] array = {"A", "B", "C", "D", "E"};
        assertEquals(3, ArrayUtil.indexOf("D", array));
        assertEquals(-1, ArrayUtil.indexOf("F", array));
        assertEquals(-1, ArrayUtil.indexOf(null, array));
        assertEquals(-1, ArrayUtil.indexOf("A", null));
        assertEquals(-1, ArrayUtil.indexOf("a", null));
        array[1] = null;
        assertEquals(-1, ArrayUtil.indexOf("B", array));
        assertEquals(1, ArrayUtil.indexOf(null, array));
		array[1] = "D";
		assertEquals(1, ArrayUtil.indexOf("D", array));
        assertEquals(-1, ArrayUtil.indexOf("d", array));

		assertEquals(1, ArrayUtil.indexOf(array, "D", 1, 2));
		assertEquals(1, ArrayUtil.indexOf(array, "D", 1, array.length - 1));
		assertEquals(3, ArrayUtil.indexOf(array, "D", 2, array.length - 1));

        int[] i = {1, 2, 3, 4, 5};
        assertEquals(3, ArrayUtil.indexOf(4, i));
        assertEquals(-1, ArrayUtil.indexOf(6, i));
        assertEquals(-1, ArrayUtil.indexOf(0, i));
        assertEquals(-1, ArrayUtil.indexOf(1, (int[])null));
        i[1] = 0;
        assertEquals(-1, ArrayUtil.indexOf(2, i));
        assertEquals(1, ArrayUtil.indexOf(0, i));
        i[2] = 4;
        assertEquals(2, ArrayUtil.indexOf(4, i));
    }

    public void testLastIndexOf() {
        String[] array = {"A", "B", "C", "D", "E"};
        assertEquals(3, ArrayUtil.lastIndexOf("D", array));
        assertEquals(-1, ArrayUtil.lastIndexOf("F", array));
        assertEquals(-1, ArrayUtil.lastIndexOf(null, array));
        assertEquals(-1, ArrayUtil.lastIndexOf("A", null));
        assertEquals(-1, ArrayUtil.indexOf("a", null));
        array[1] = null;
        assertEquals(-1, ArrayUtil.lastIndexOf("B", array));
        assertEquals(1, ArrayUtil.lastIndexOf(null, array));
		array[1] = "D";
        assertEquals(3, ArrayUtil.lastIndexOf("D", array));
        assertEquals(-1, ArrayUtil.lastIndexOf("d", array));

		assertEquals(3, ArrayUtil.lastIndexOf(array, "D", 3, 2));
		assertEquals(3, ArrayUtil.lastIndexOf(array, "D", 4, 0));
		assertEquals(1, ArrayUtil.lastIndexOf(array, "D", 2, 0));

        int[] i = {1, 2, 3, 4, 5};
        assertEquals(3, ArrayUtil.lastIndexOf(4, i));
        assertEquals(-1, ArrayUtil.lastIndexOf(6, i));
        assertEquals(-1, ArrayUtil.lastIndexOf(0, i));
        assertEquals(-1, ArrayUtil.lastIndexOf(1, (int[])null));
        i[1] = 0;
        assertEquals(-1, ArrayUtil.lastIndexOf(2, i));
        assertEquals(1, ArrayUtil.lastIndexOf(0, i));
        i[2] = 4;
        assertEquals(3, ArrayUtil.lastIndexOf(4, i));
    }

    public void testIndexOfIgnoreCase() {
        String[] array = {"A", "B", "C", "D", "E"};
        assertEquals(3, ArrayUtil.indexOfIgnoreCase("D", array));
        assertEquals(-1, ArrayUtil.indexOfIgnoreCase("F", array));
        assertEquals(-1, ArrayUtil.indexOfIgnoreCase(null, array));
        assertEquals(-1, ArrayUtil.indexOfIgnoreCase("A", null));
        assertEquals(0, ArrayUtil.indexOfIgnoreCase("a", array));
        array[1] = null;
        assertEquals(-1, ArrayUtil.indexOfIgnoreCase("B", array));
        assertEquals(1, ArrayUtil.indexOfIgnoreCase(null, array));
        array[2] = "D";
        assertEquals(2, ArrayUtil.indexOfIgnoreCase("D", array));
        assertEquals(2, ArrayUtil.indexOfIgnoreCase("d", array));
    }

    public void testLastIndexOfIgnoreCase() {
        String[] array = {"A", "B", "C", "D", "E"};
        assertEquals(3, ArrayUtil.lastIndexOfIgnoreCase("D", array));
        assertEquals(-1, ArrayUtil.lastIndexOfIgnoreCase("F", array));
        assertEquals(-1, ArrayUtil.lastIndexOfIgnoreCase(null, array));
        assertEquals(-1, ArrayUtil.lastIndexOfIgnoreCase("A", null));
        assertEquals(0, ArrayUtil.lastIndexOfIgnoreCase("a", array));
        array[1] = null;
        assertEquals(-1, ArrayUtil.lastIndexOfIgnoreCase("B", array));
        assertEquals(1, ArrayUtil.lastIndexOfIgnoreCase(null, array));
        array[2] = "D";
        assertEquals(3, ArrayUtil.lastIndexOfIgnoreCase("D", array));
        assertEquals(3, ArrayUtil.lastIndexOfIgnoreCase("d", array));
    }

    public void testContains() {
        String[] array = {"A", "B", "C", "D", "E"};
        assertEquals(true, ArrayUtil.contains(array, "D"));
        assertEquals(false, ArrayUtil.contains(array, "F"));
        assertEquals(false, ArrayUtil.contains(array, null));
        assertEquals(false, ArrayUtil.contains(null, "A"));
        assertEquals(false, ArrayUtil.contains(null, "a"));
        array[1] = null;
        assertEquals(false, ArrayUtil.contains(array, "B"));
        assertEquals(true, ArrayUtil.contains(array, null));
        array[2] = "D";
        assertEquals(true, ArrayUtil.contains(array, "D"));
        assertEquals(false, ArrayUtil.contains(array, "d"));

        int[] i = {1, 2, 3, 4, 5};
        assertEquals(true, ArrayUtil.contains(i, 4));
        assertEquals(false, ArrayUtil.contains(i, 6));
        assertEquals(false, ArrayUtil.contains(i, 0));
        assertEquals(false, ArrayUtil.contains((int[])null, 1));
        i[1] = 0;
        assertEquals(false, ArrayUtil.contains(i, 2));
        assertEquals(true, ArrayUtil.contains(i, 0));
        i[2] = 4;
        assertEquals(true, ArrayUtil.contains(i, 4));
    }

    public void testContainsIgnoreCase() {
        String[] array = {"A", "B", "C", "D", "E"};
        assertEquals(true, ArrayUtil.containsIgnoreCase(array, "D"));
        assertEquals(false, ArrayUtil.containsIgnoreCase(array, "F"));
        assertEquals(false, ArrayUtil.containsIgnoreCase(array, null));
        assertEquals(false, ArrayUtil.containsIgnoreCase(null, "A"));
        assertEquals(true, ArrayUtil.containsIgnoreCase(array, "a"));
        array[1] = null;
        assertEquals(false, ArrayUtil.containsIgnoreCase(array, "B"));
        assertEquals(true, ArrayUtil.containsIgnoreCase(array, null));
        array[2] = "D";
        assertEquals(true, ArrayUtil.containsIgnoreCase(array, "D"));
        assertEquals(true, ArrayUtil.containsIgnoreCase(array, "d"));
    }

    public void testContainsAny() {
        int[] ai1 = {1, 2, 3};
        int[] ai2 = {4, 5, 6};
        int[] ai3 = {2, 4, 5};
        int[] ai4 = {-1, -2, -3};
        assertEquals(false, ArrayUtil.containsAny((int[])null, (int[])null));
        assertEquals(false, ArrayUtil.containsAny(ai1, null));
        assertEquals(false, ArrayUtil.containsAny(null, ai2));
        assertEquals(true, ArrayUtil.containsAny(ai1, ai1));
        assertEquals(false, ArrayUtil.containsAny(ai1, ai2));
        assertEquals(false, ArrayUtil.containsAny(ai2, ai1));
        assertEquals(true, ArrayUtil.containsAny(ai1, ai3));
        assertEquals(true, ArrayUtil.containsAny(ai3, ai1));
        assertEquals(true, ArrayUtil.containsAny(ai2, ai3));
        assertEquals(true, ArrayUtil.containsAny(ai3, ai2));
        assertEquals(false, ArrayUtil.containsAny(ai1, ai4));
        
        String[] as1 = {"A", "B", "C"};
        String[] as2 = {"D", "E", "F"};
        String[] as3 = {"B", "D", "E"};
        String[] as4 = {"a", "b", "c"};
        assertEquals(false, ArrayUtil.containsAny((String[])null, (String[])null));
        assertEquals(false, ArrayUtil.containsAny(as1, null));
        assertEquals(false, ArrayUtil.containsAny(null, as2));
        assertEquals(true, ArrayUtil.containsAny(as1, as1));
        assertEquals(false, ArrayUtil.containsAny(as1, as2));
        assertEquals(false, ArrayUtil.containsAny(as2, as1));
        assertEquals(true, ArrayUtil.containsAny(as1, as3));
        assertEquals(true, ArrayUtil.containsAny(as3, as1));
        assertEquals(true, ArrayUtil.containsAny(as2, as3));
        assertEquals(true, ArrayUtil.containsAny(as3, as2));
        assertEquals(false, ArrayUtil.containsAny(as1, as4));
        
        as1 = new String[] {"A", "B", "C"};
        as2 = new String[] {"D", "E", "F"};
        as3 = new String[] {"b", "d", "e"};
        as4 = new String[] {"a", "b", "c"};
        assertEquals(false, ArrayUtil.containsAnyIgnoreCase(null, null));
        assertEquals(false, ArrayUtil.containsAnyIgnoreCase(as1, null));
        assertEquals(false, ArrayUtil.containsAnyIgnoreCase(null, as2));
        assertEquals(true, ArrayUtil.containsAnyIgnoreCase(as1, as1));
        assertEquals(false, ArrayUtil.containsAnyIgnoreCase(as1, as2));
        assertEquals(false, ArrayUtil.containsAnyIgnoreCase(as2, as1));
        assertEquals(true, ArrayUtil.containsAnyIgnoreCase(as1, as3));
        assertEquals(true, ArrayUtil.containsAnyIgnoreCase(as3, as1));
        assertEquals(true, ArrayUtil.containsAnyIgnoreCase(as2, as3));
        assertEquals(true, ArrayUtil.containsAnyIgnoreCase(as3, as2));
        assertEquals(true, ArrayUtil.containsAnyIgnoreCase(as1, as4));
    }

    public void testContainsSame() {
        int[] ai1 = {1, 2, 3};
        int[] ai2 = {4, 5, 6};
        int[] ai3 = {2, 4, 5};
        int[] ai4 = {3, 2, 1};
        assertEquals(true, ArrayUtil.containsSame((int[])null, (int[])null));
        assertEquals(true, ArrayUtil.containsSame(new int[0],  new int[0]));
        assertEquals(false, ArrayUtil.containsSame(ai1, null));
        assertEquals(false, ArrayUtil.containsSame(null, ai2));
        assertEquals(true, ArrayUtil.containsSame(ai1, ai1));
        assertEquals(false, ArrayUtil.containsSame(ai1, ai2));
        assertEquals(false, ArrayUtil.containsSame(ai2, ai1));
        assertEquals(false, ArrayUtil.containsSame(ai1, ai3));
        assertEquals(false, ArrayUtil.containsSame(ai3, ai1));
        assertEquals(false, ArrayUtil.containsSame(ai2, ai3));
        assertEquals(false, ArrayUtil.containsSame(ai3, ai2));
        assertEquals(true, ArrayUtil.containsSame(ai1, ai4));
        assertEquals(true, ArrayUtil.containsSame(ai1, ai4));

        String[] as1 = {"A", "B", "C"};
        String[] as2 = {"D", "E", "F"};
        String[] as3 = {"B", "D", "E"};
        String[] as4 = {"C", "B", "A"};
        String[] as5 = {"c", "b", "a"};
        assertEquals(true, ArrayUtil.containsSame((String[])null, (String[])null));
        assertEquals(true, ArrayUtil.containsSame(new String[0],  new String[0]));
        assertEquals(false, ArrayUtil.containsSame(as1, null));
        assertEquals(false, ArrayUtil.containsSame(null, as2));
        assertEquals(true, ArrayUtil.containsSame(as1, as1));
        assertEquals(false, ArrayUtil.containsSame(as1, as2));
        assertEquals(false, ArrayUtil.containsSame(as2, as1));
        assertEquals(false, ArrayUtil.containsSame(as1, as3));
        assertEquals(false, ArrayUtil.containsSame(as3, as1));
        assertEquals(false, ArrayUtil.containsSame(as2, as3));
        assertEquals(false, ArrayUtil.containsSame(as3, as2));
        assertEquals(true, ArrayUtil.containsSame(as1, as4));
        assertEquals(true, ArrayUtil.containsSame(as1, as4));
        assertEquals(false, ArrayUtil.containsSame(as1, as5));
        assertEquals(false, ArrayUtil.containsSame(as5, as1));
        
        as1 = new String[] {"A", "B", "C"};
        as2 = new String[] {"D", "E", "F"};
        as3 = new String[] {"b", "d", "e"};
        as4 = new String[] {"C", "B", "A"};
        as5 = new String[] {"c", "b", "a"};
        assertEquals(true, ArrayUtil.containsSameIgnoreCase((String[])null, (String[])null));
        assertEquals(true, ArrayUtil.containsSameIgnoreCase(new String[0],  new String[0]));
        assertEquals(false, ArrayUtil.containsSameIgnoreCase(as1, null));
        assertEquals(false, ArrayUtil.containsSameIgnoreCase(null, as2));
        assertEquals(true, ArrayUtil.containsSameIgnoreCase(as1, as1));
        assertEquals(false, ArrayUtil.containsSameIgnoreCase(as1, as2));
        assertEquals(false, ArrayUtil.containsSameIgnoreCase(as2, as1));
        assertEquals(false, ArrayUtil.containsSameIgnoreCase(as1, as3));
        assertEquals(false, ArrayUtil.containsSameIgnoreCase(as3, as1));
        assertEquals(false, ArrayUtil.containsSameIgnoreCase(as2, as3));
        assertEquals(false, ArrayUtil.containsSameIgnoreCase(as3, as2));
        assertEquals(true, ArrayUtil.containsSameIgnoreCase(as1, as4));
        assertEquals(true, ArrayUtil.containsSameIgnoreCase(as1, as4));
        assertEquals(true, ArrayUtil.containsSameIgnoreCase(as1, as5));
        assertEquals(true, ArrayUtil.containsSameIgnoreCase(as5, as1));
    }

    public void testEquals() {
        Integer[] a = new Integer[] {Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)};
        Integer[] b = new Integer[] {Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)};
        Integer[] c = a;
        assertTrue(ArrayUtil.equals(a, new Integer[] {Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)}));
        assertFalse(ArrayUtil.equals(a, new Integer[] {Integer.valueOf(1), Integer.valueOf(2)}));
        assertFalse(ArrayUtil.equals(a, new Integer[] {Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(1)}));
        assertFalse(ArrayUtil.equals(a, new Integer[] {Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4)}));
        assertFalse(ArrayUtil.equals(a, null));
        assertTrue(ArrayUtil.equals(a, a));
        assertTrue(ArrayUtil.equals(a, b));
        assertTrue(ArrayUtil.equals(a, c));
        assertTrue(ArrayUtil.equals((Integer[])null, (Integer[])null));
        assertFalse(ArrayUtil.equals(new Integer[0], null));
        assertFalse(ArrayUtil.equals(null, new Integer[0]));
        assertFalse(ArrayUtil.equals(a, null));
        assertFalse(ArrayUtil.equals(null, a));

        int[] a2 = new int[] {1, 2, 3};
        int[] b2 = new int[] {1, 2, 3};
        int[] c2 = a2;
        assertTrue(ArrayUtil.equals(a2, new int[] {1, 2, 3}));
        assertFalse(ArrayUtil.equals(a2, new int[] {1, 2}));
        assertFalse(ArrayUtil.equals(a2, new int[] {1, 1, 1}));
        assertFalse(ArrayUtil.equals(a2, new int[] {1, 2, 3, 4}));
        assertFalse(ArrayUtil.equals(a2, null));
        assertTrue(ArrayUtil.equals(a2, a2));
        assertTrue(ArrayUtil.equals(a2, b2));
        assertTrue(ArrayUtil.equals(a2, c2));
        assertTrue(ArrayUtil.equals((int[])null, (int[])null));
        assertFalse(ArrayUtil.equals(new int[0], null));
        assertFalse(ArrayUtil.equals(null, new int[0]));
        assertFalse(ArrayUtil.equals(a2, null));
        assertFalse(ArrayUtil.equals(null, a2));

        double[] a3 = new double[] {1.0, 2.0, 3.0};
        double[] b3 = new double[] {1.0, 2.0, 3.0};
        double[] c3 = a3;
        assertTrue(ArrayUtil.equals(a3, new double[] {1.0, 2.0, 3.0}));
        assertFalse(ArrayUtil.equals(a3, new double[] {1.0, 2.0}));
        assertFalse(ArrayUtil.equals(a3, new double[] {1.0, 1.0, 1.0}));
        assertFalse(ArrayUtil.equals(a3, new double[] {1.0, 2.0, 3.0, 4.0}));
        assertFalse(ArrayUtil.equals(a3, null));
        assertTrue(ArrayUtil.equals(a3, a3));
        assertTrue(ArrayUtil.equals(a3, b3));
        assertTrue(ArrayUtil.equals(a3, c3));
        assertTrue(ArrayUtil.equals((double[])null, (double[])null));
        assertFalse(ArrayUtil.equals(new double[0], null));
        assertFalse(ArrayUtil.equals(null, new double[0]));
        assertFalse(ArrayUtil.equals(a3, null));
        assertFalse(ArrayUtil.equals(null, a3));
    }

    public void testToArray() {
        ArrayList l1 = new ArrayList();
        ArrayList l2 = new ArrayList();
		ArrayList<Number> l3 = new ArrayList<>();
        ArrayList l4 = new ArrayList();
        l1.add(Integer.valueOf(1));
        l1.add(Integer.valueOf(2));
        l1.add(Integer.valueOf(3));
        l2.add(Double.valueOf(4.0));
        l2.add(Double.valueOf(5.0));
        l2.add(Double.valueOf(6.0));
        l3.addAll(l1);
        l3.addAll(l2);
        l4.addAll(l3);
        l4.add("Hello");
        l4.add("Test");

        Integer[] a1 = (Integer[])ArrayUtil.toArray(l1);
        Double[] a2 = (Double[])ArrayUtil.toArray(l2);
		Number[] a3 = ArrayUtil.toArrayTyped(l3, Number.class);
        Object[] a4 = ArrayUtil.toObjectArray(l3);
        String[] a5 = ArrayUtil.toStringArray(l4);

        assertTrue(ArrayUtil.equals(a1, new Integer[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)}));
        assertTrue(ArrayUtil.equals(a2, new Double[]{Double.valueOf(4.0), Double.valueOf(5.0), Double.valueOf(6.0)}));
        assertTrue(ArrayUtil.equals(a3, new Number[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Double.valueOf(4.0), Double.valueOf(5.0), Double.valueOf(6.0)}));
        assertTrue(ArrayUtil.equals(a4, new Number[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Double.valueOf(4.0), Double.valueOf(5.0), Double.valueOf(6.0)}));
        assertTrue(ArrayUtil.equals(a5, new String[]{Integer.toString(1), Integer.toString(2), Integer.toString(3), Double.toString(4.0), Double.toString(5.0), Double.toString(6.0), new String("Hello"), "Test"}));

        int[] a6 = ArrayUtil.toIntArray(l3);
        double[] a7 = ArrayUtil.toDoubleArray(l3);
        assertEquals(1, a6[0]);assertEquals(2, a6[1]);assertEquals(3, a6[2]);
        assertEquals(4, a6[3]);assertEquals(5, a6[4]);assertEquals(6, a6[5]);
        assertEquals(1.0, a7[0], 0);assertEquals(2.0, a7[1], 0);assertEquals(3.0, a7[2], 0);
        assertEquals(4.0, a7[3], 0);assertEquals(5.0, a7[4], 0);assertEquals(6.0, a7[5], 0);
    }

    public void testIntoArray() {
        Object[] a = ArrayUtil.intoArray(null);
        String[] b = (String[])ArrayUtil.intoArray("Hello");
        Double[] c = (Double[])ArrayUtil.intoArray(Double.valueOf(1.0));
        String[] d = ArrayUtil.intoStringArray("String");
        String[] e = ArrayUtil.intoStringArray(null);
        assertEquals(1, a.length);
        assertEquals(1, b.length);
        assertEquals(1, c.length);
        assertEquals(1, d.length);
        assertEquals(1, e.length);
        assertNull(a[0]);
        assertEquals("Hello", b[0]);
        assertEquals(1.0, c[0].doubleValue(), 0.0);
        assertEquals("String", d[0]);
        assertNull(e[0]);
    }

    public void testGetFirst() {
        assertEquals(null, ArrayUtil.getFirst(null));
        assertEquals(null, ArrayUtil.getFirst(new String[0]));
        assertEquals(null, ArrayUtil.getFirst(new String[]{null}));
        assertEquals(null, ArrayUtil.getFirst(new String[]{null, "A"}));
        assertEquals("A", ArrayUtil.getFirst(new String[]{"A", null}));
        assertEquals("A", ArrayUtil.getFirst(new String[]{"A"}));
        assertEquals("A", ArrayUtil.getFirst(new String[]{"A", "B"}));
    }

    public void testGetLast() {
        assertEquals(null, ArrayUtil.getLast(null));
        assertEquals(null, ArrayUtil.getLast(new String[0]));
        assertEquals(null, ArrayUtil.getLast(new String[]{null}));
        assertEquals("A", ArrayUtil.getLast(new String[]{null, "A"}));
        assertEquals(null, ArrayUtil.getLast(new String[]{"A", null}));
        assertEquals("A", ArrayUtil.getLast(new String[]{"A"}));
        assertEquals("B", ArrayUtil.getLast(new String[]{"A", "B"}));
    }

    public void testClone() {
        Double[] a1 = new Double[] {Double.valueOf(1.0), Double.valueOf(2.0), Double.valueOf(3.0)};
        Double[] a2 = (Double[])ArrayUtil.clone(a1);
        assertEquals(a1.length, a2.length);
        assertEquals(a1[0], a2[0]);
        assertEquals(a1[1], a2[1]);
        assertEquals(a1[2], a2[2]);

        a1[0] = Double.valueOf(5.0);
        a2[1] = Double.valueOf(6.0);
        assertEquals(a1[0], Double.valueOf(5.0));
        assertEquals(a2[0], Double.valueOf(1.0));
        assertEquals(a1[1], Double.valueOf(2.0));
        assertEquals(a2[1], Double.valueOf(6.0));

        Double[] a3 = null;
        assertEquals(ArrayUtil.clone(a3), null);
        Double[] a4 = new Double[0];
        assertEquals(ArrayUtil.clone(a4).length, 0);
    }

	@SuppressWarnings("deprecation")
    public void testJoin() {
        Double[] a1 = new Double[] { Double.valueOf(1.0), Double.valueOf(2.0) };
        Double[] a2 = new Double[] { Double.valueOf(3.0), Double.valueOf(4.0) };
        Double[] a3 = (Double[]) ArrayUtil.join(a1, a2);
        Integer[] a4 = new Integer[] { Integer.valueOf(5), Integer.valueOf(6) };
        Object[] a5 = ArrayUtil.join(a3, a4);
        Number[] a6a = new Number[0];
        a6a = (Number[])ArrayUtil.join(a3, a4, a6a);
        Number[] a6b = new Number[a3.length + a4.length];
        ArrayUtil.join(a3, a4, a6b);
        Number[] a6c = new Number[a3.length + a4.length + 2];
        ArrayUtil.join(a3, a4, a6c);
        assertEquals(a3.length, a1.length + a2.length);
        assertEquals(a5.length, a3.length + a4.length);
        assertTrue(ArrayUtil.equals(a3, new Double[]{Double.valueOf(1.0), Double.valueOf(2.0), Double.valueOf(3.0), Double.valueOf(4.0)}));
        assertTrue(ArrayUtil.equals(a5, new Number[]{Double.valueOf(1.0), Double.valueOf(2.0), Double.valueOf(3.0), Double.valueOf(4.0), Integer.valueOf(5), Integer.valueOf(6)}));
        assertTrue(ArrayUtil.equals(a6a, new Object[]{Double.valueOf(1.0), Double.valueOf(2.0), Double.valueOf(3.0), Double.valueOf(4.0), Integer.valueOf(5), Integer.valueOf(6)}));
        assertTrue(ArrayUtil.equals(a6b, new Object[]{Double.valueOf(1.0), Double.valueOf(2.0), Double.valueOf(3.0), Double.valueOf(4.0), Integer.valueOf(5), Integer.valueOf(6)}));
        assertTrue(ArrayUtil.equals(a6c, new Object[]{Double.valueOf(1.0), Double.valueOf(2.0), Double.valueOf(3.0), Double.valueOf(4.0), Integer.valueOf(5), Integer.valueOf(6), null, null}));
		Double[] a7 = (Double[]) ArrayUtil.join(a1, Double.valueOf(7.0));
        Object[] a8 = ArrayUtil.join(a1, Integer.valueOf(7));
        assertTrue(ArrayUtil.equals(a7, new Double[]{Double.valueOf(1.0), Double.valueOf(2.0), Double.valueOf(7.0)}));
        assertTrue(ArrayUtil.equals(a8, new Object[]{Double.valueOf(1.0), Double.valueOf(2.0), Integer.valueOf(7)}));
        Double[] a9 = (Double[]) ArrayUtil.join(Double.valueOf(8.0), a1);
        Object[] a10 = ArrayUtil.join(Integer.valueOf(8), a1);
        assertTrue(ArrayUtil.equals(a9, new Double[]{Double.valueOf(8.0), Double.valueOf(1.0), Double.valueOf(2.0)}));
        assertTrue(ArrayUtil.equals(a10, new Object[]{Integer.valueOf(8), Double.valueOf(1.0), Double.valueOf(2.0)}));
    }

	public void testJoinClass() {
		Double[]     b1 = new Double[] {Double.valueOf(1.0), Double.valueOf(2.0)};
        Integer[]    b2 = new Integer[] {Integer.valueOf(3), Integer.valueOf(4)};
        Number[]     b3 = ArrayUtil.join(b1, b2, Number.class);
        String[]     b4 = new String[] {new String("5"), new String("6")};
        Comparable[] b5 = ArrayUtil.join(b3, b4, Comparable.class);
        assertTrue(ArrayUtil.equals(b3, new Number[]{Double.valueOf(1.0), Double.valueOf(2.0), Integer.valueOf(3), Integer.valueOf(4)}));
        assertTrue(ArrayUtil.equals(b5, new Object[]{Double.valueOf(1.0), Double.valueOf(2.0), Integer.valueOf(3), Integer.valueOf(4), new String("5"), new String("6")}));
	}

	public void testAddElement() {
		Double[] a1 = new Double[] { 1.0d, 2.0d };
		assertEquals(new Double[] { 1.0d, 2.0d, 3.0d }, ArrayUtil.addElement(a1, 3.0d));
		assertEquals(new Double[] { 3.0d, 1.0d, 2.0d }, ArrayUtil.addElement(3.0d, a1));
	}

    public void testSum() {
        int[] a = new int[0];
        int[] b = new int[] {1};
        int[] c = new int[] {1, 2, 3, 4, 5};
        int[] d = null;
        assertEquals(0, ArrayUtil.sum(a));
        assertEquals(1, ArrayUtil.sum(b));
        assertEquals(15, ArrayUtil.sum(c));
        assertEquals(0, ArrayUtil.sum(d));

        assertEquals(15, ArrayUtil.sum(c, 0, c.length - 1));
        assertEquals(2, ArrayUtil.sum(c, 1, 1));
        assertEquals(6, ArrayUtil.sum(c, 0, 2));
        assertEquals(9, ArrayUtil.sum(c, 3, 4));
        assertEquals(0, ArrayUtil.sum(c, 3, 1));

        double[] aD = new double[0];
        double[] bD = new double[] {1.0};
        double[] cD = new double[] {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] dD = null;
        assertEquals(0.0, ArrayUtil.sum(aD), 0);
        assertEquals(1.0, ArrayUtil.sum(bD), 0);
        assertEquals(15.0, ArrayUtil.sum(cD), 0);
        assertEquals(0.0, ArrayUtil.sum(dD), 0);

        assertEquals(15.0, ArrayUtil.sum(cD, 0, c.length - 1), 0);
        assertEquals(2.0, ArrayUtil.sum(cD, 1, 1), 0);
        assertEquals(6.0, ArrayUtil.sum(cD, 0, 2), 0);
        assertEquals(9.0, ArrayUtil.sum(cD, 3, 4), 0);
        assertEquals(0.0, ArrayUtil.sum(cD, 3, 1), 0);
    }

	public void testAndOr() {
		boolean[] a = new boolean[] {};
		boolean[] b = new boolean[] { true };
		boolean[] d = new boolean[] { true, true, true };
		boolean[] e = new boolean[] { false, false, false };
		boolean[] f = new boolean[] { true, true, false };

		assertFalse(ArrayUtil.or(a));
		assertTrue(ArrayUtil.and(a));

		assertTrue(ArrayUtil.or(b));
		assertTrue(ArrayUtil.and(b));

		assertFalse(ArrayUtil.or(false));
		assertFalse(ArrayUtil.and(false));

		assertTrue(ArrayUtil.or(d));
		assertTrue(ArrayUtil.and(d));

		assertFalse(ArrayUtil.or(e));
		assertFalse(ArrayUtil.and(e));

		assertTrue(ArrayUtil.or(f));
		assertFalse(ArrayUtil.or(f, 2, 3));
		assertTrue(ArrayUtil.and(f, 0, 2));
		assertFalse(ArrayUtil.and(f));

		assertTrue(ArrayUtil.and());
		assertFalse(ArrayUtil.or());

	}

    public void testAdd() {
        int[] a = new int[0];
        int[] b = new int[] {1};
        int[] c = new int[] {1, 2, 3, 4, 5};
        int[] d = new int[] {0, 0, 0, 0, 0};
        ArrayUtil.add(d, c);
        assertTrue(ArrayUtil.equals(c, new int[] {1, 2, 3, 4, 5}));
        assertTrue(ArrayUtil.equals(d, new int[] {1, 2, 3, 4, 5}));
        ArrayUtil.add(d, c);
        assertTrue(ArrayUtil.equals(c, new int[] {1, 2, 3, 4, 5}));
        assertTrue(ArrayUtil.equals(d, new int[] {2, 4, 6, 8, 10}));
        ArrayUtil.add(d, b);
        assertTrue(ArrayUtil.equals(b, new int[] {1}));
        assertTrue(ArrayUtil.equals(d, new int[] {3, 4, 6, 8, 10}));
        ArrayUtil.add(d, a);
        ArrayUtil.add(d, null);
        assertTrue(ArrayUtil.equals(d, new int[] {3, 4, 6, 8, 10}));

        double[] a2 = new double[0];
        double[] b2 = new double[] {1.0};
        double[] c2 = new double[] {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] d2 = new double[] {0.0, 0.0, 0.0, 0.0, 0.0};
        ArrayUtil.add(d2, c2);
        assertTrue(ArrayUtil.equals(c2, new double[] {1.0, 2.0, 3.0, 4.0, 5.0}));
        assertTrue(ArrayUtil.equals(d2, new double[] {1.0, 2.0, 3.0, 4.0, 5.0}));
        ArrayUtil.add(d2, c2);
        assertTrue(ArrayUtil.equals(c2, new double[] {1.0, 2.0, 3.0, 4.0, 5.0}));
        assertTrue(ArrayUtil.equals(d2, new double[] {2.0, 4.0, 6.0, 8.0, 10.0}));
        ArrayUtil.add(d2, b2);
        assertTrue(ArrayUtil.equals(b2, new double[] {1.0}));
        assertTrue(ArrayUtil.equals(d2, new double[] {3.0, 4.0, 6.0, 8.0, 10.0}));
        ArrayUtil.add(d2, a2);
        ArrayUtil.add(d2, null);
        assertTrue(ArrayUtil.equals(d2, new double[] {3.0, 4.0, 6.0, 8.0, 10.0}));
    }
    
    public void testCopy() {
    	final Object[] nullCopy = ArrayUtil.copy();
		assertNotNull(nullCopy);
		assertEquals(0, nullCopy.length);
		
		final Object[] emptyArray = ArrayUtil.copy(new Object[0]);
		assertNotNull(emptyArray);
		assertEquals(0, emptyArray.length);
		
		final Object[] array1 = ArrayUtil.copy(1,2,3);
		assertNotNull(array1);
		assertEquals(3, array1.length);
		assertEquals(1, array1[0]);
		assertEquals(2, array1[1]);
		assertEquals(3, array1[2]);
		
		Object[] source = new Integer[] { 1, 2, 3 };
		final Object[] array2 = ArrayUtil.copy(source);
		assertNotNull(array2);
		assertEquals(1, array2[0]);
		assertEquals(2, array2[1]);
		assertEquals(3, array2[2]);
		
		final Object[] array3 = ArrayUtil.copy(new Object[] {1,2,3});
		assertNotNull(array3);
		assertEquals(3, array2.length);
		assertEquals(1, array3[0]);
		assertEquals(2, array3[1]);
		assertEquals(3, array3[2]);
    }

	/** Tests for the "nonNull" methods. */
	public void testNonNull() {
		assertNotNull(nonNull((boolean[]) null));
		assertNotNull(nonNull((char[]) null));
		assertNotNull(nonNull((byte[]) null));
		assertNotNull(nonNull((short[]) null));
		assertNotNull(nonNull((int[]) null));
		assertNotNull(nonNull((long[]) null));
		assertNotNull(nonNull((float[]) null));
		assertNotNull(nonNull((double[]) null));
		assertNotNull(nonNull((Object[]) null));
	}

	public void testForEach() {
		String[] orig = new String[] { "a", "b", "c", "d", "e" };
		List<String> copy = new ArrayList<>();

		forEach(orig, s -> copy.add(s));
		assertEquals(Arrays.asList(orig), copy);
		copy.clear();

		forEach(orig, 0, orig.length, s -> copy.add(s));
		assertEquals(Arrays.asList(orig), copy);
		copy.clear();

		forEach(orig, 1, 5, s -> copy.add(s));
		assertEquals(Arrays.asList(orig).subList(1, 5), copy);
		copy.clear();
	}

    /**
     * Return the suite of Tests to perform.
     *
     * @return the test for this class
     */
    public static Test suite() {
        return BasicTestSetup.createBasicTestSetup(new TestSuite(TestArrayUtil.class));
    }

    /**
     * Main function for direct execution.
     *
     * @param args
     *            command line parameters are ignored.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
