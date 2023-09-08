/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.InlineList;

/**
 * Test the {@link TestInlineList}.
 *
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
@SuppressWarnings("javadoc")
public class TestInlineList extends TestCase {

    /**
     * Create a new TesstInlineList.
     */
    public TestInlineList(String name) {
        super(name);
    }

    /**
     * Test Main usage of InlineList.
     */
    public void testMain() {
		Object il = InlineList.newInlineList();
        
        String v1 = "v1";

        assertFalse  (   InlineList.contains(il, null));
        assertFalse  (   InlineList.iterator (String.class, il).hasNext());
        assertTrue   (   InlineList.toList   (String.class, il).isEmpty());
        assertEquals (0, InlineList.toArray  (il).length);
        assertFalse  (   InlineList.contains (il, null));
		assertFalse(InlineList.contains(il, null));
		assertEquals(InlineList.newInlineList(), il = InlineList.remove(il, v1));

        il = InlineList.add(String.class, il,v1);

        assertFalse  (     InlineList.contains(il, null));
        assertTrue   (     InlineList.contains(il, v1));
        assertEquals (v1,InlineList.iterator (String.class, il).next());
        assertFalse  (     InlineList.toList   (String.class, il).isEmpty());
        assertEquals (1,   InlineList.toArray  (il).length);
        assertFalse  (     InlineList.contains (il, null));
        assertNotNull(il = InlineList.remove   (il, null));
		assertEquals(InlineList.newInlineList(), il = InlineList.remove(il, v1));

        il = InlineList.add(String.class, il,v1);
        il = InlineList.add(String.class, il,v1);

        assertFalse  (     InlineList.contains(il, null));
        assertTrue   (     InlineList.contains(il, v1));
        assertEquals (v1,InlineList.iterator (String.class, il).next());
        assertEquals (2,   InlineList.toList   (String.class, il).size());
        assertEquals (2,   InlineList.toArray  (il).length);
        assertFalse  (     InlineList.contains (il, null));
        assertNotNull(il = InlineList.remove   (il, null));
        assertNotNull(il = InlineList.remove   (il, v1));
		assertEquals(InlineList.newInlineList(), il = InlineList.remove(il, v1));

        il = InlineList.add(String.class, il,v1);
        il = InlineList.add(String.class, il,v1);
        il = InlineList.add(String.class, il,v1);
        
        assertFalse  (     InlineList.contains(il, null));
        assertTrue   (     InlineList.contains(il, v1));
        assertEquals (v1,InlineList.iterator (String.class, il).next());
        assertEquals (3,   InlineList.toList   (String.class, il).size());
        assertEquals (3,   InlineList.toArray  (il).length);
        assertFalse  (     InlineList.contains (il, il));
        assertNotNull(il = InlineList.remove   (il, null));
        assertNotNull(il = InlineList.remove   (il, v1));
        assertNotNull(il = InlineList.remove   (il, v1));
		assertEquals(InlineList.newInlineList(), il = InlineList.remove(il, v1));
    }

	public void testAddNull() {
		Object il = InlineList.newInlineList();
		assertTrue(InlineList.isEmpty(il));
		il = InlineList.add(String.class, il, null);
		assertFalse(InlineList.isEmpty(il));
		assertEquals(Collections.singletonList(null), InlineList.toList(String.class, il));
	}

	/**
	 * Test Illegal usage of InlineList.
	 */
    public void testIllegal() {
		Object il = InlineList.newInlineList();
        String v1 = "v1";

        il = InlineList.add(Object.class, il,v1);
        il = InlineList.add(Object.class, il,v1);
        try {
            il = InlineList.add(Object.class, il,il);
            fail("Expectd IllegalArgumentException");
        } catch (IllegalArgumentException expected ) { /* expected */ }

    }

	/**
	 * Test {@link InlineList#toArray(Object, Object[])}.
	 */
	public void testToArray() {
		Object il = InlineList.newInlineList();
		String[] output = new String[1];
		output[0] = "toClear";
		String[] array0 = InlineList.toArray(il, output);
		assertSame("Contents fit into the array: Contract in toArray(Object[]).", output, array0);
		assertNull("Element after size should be null: Contract in toArray(Object[])", array0[0]);

		String v1 = "v1";

		il = InlineList.add(Object.class, il, v1);

		String[] array1 = InlineList.toArray(il, output);
		assertSame("Contents fit into the array: Contract in toArray(Object[]).", output, array1);
		assertEquals(v1, array1[0]);

		String[] output2 = new String[21];
		output2[0] = "v2";
		output2[1] = "v3";
		String[] array2 = InlineList.toArray(il, output2);
		assertSame("Contents fit into the array: Contract in toArray(Object[]).", output2, array2);
		assertEquals(v1, array2[0]);
		assertNull("Element after size should be null: Contract in toArray(Object[])", array2[1]);

		String[] output3 = new String[0];
		String[] array3 = InlineList.toArray(il, output3);
		assertNotSame("Contents does not fit into the array.", output3, array3);
		assertEquals(v1, array3[0]);

		String v2 = "v2";
		il = InlineList.add(Object.class, il, v2);
		assertTrue(Arrays.equals(new String[] { v1, v2 }, InlineList.toArray(il, new String[0])));
	}

	/**
	 * Test {@link InlineList#clear(Object)}.
	 */
	public void testClear() {
		Object il = InlineList.newInlineList();
		assertTrue(InlineList.isEmpty(il));
		String v1 = "v1";
		String v2 = "v2";

		il = InlineList.add(Object.class, il, v1);
		assertFalse(InlineList.isEmpty(il));
		il = InlineList.add(Object.class, il, v2);
		assertFalse(InlineList.isEmpty(il));
		il = InlineList.clear(il);
		assertTrue(InlineList.isEmpty(il));
	}

	/**
	 * Test {@link InlineList#size(Object)}.
	 */
	public void testSize() {
		Object il = InlineList.newInlineList();
		assertEquals(0, InlineList.size(il));

		String v1 = "v1";
		il = InlineList.add(Object.class, il, v1);
		assertEquals(1, InlineList.size(il));

		String v2 = "v2";
		il = InlineList.add(Object.class, il, v2);
		assertEquals(2, InlineList.size(il));

		il = InlineList.clear(il);
		assertEquals(0, InlineList.size(il));
	}

	/**
	 * Test {@link InlineList#addAll(Class, Object, java.util.Collection)}
	 */
	public void testAddAll() {
		Object il = InlineList.newInlineList();

		il = InlineList.addAll(Object.class, il, new ArrayList<>());
		assertTrue(InlineList.isEmpty(il));

		il = InlineList.addAll(Object.class, il, list(1, 2, 3));
		assertEquals(list(1, 2, 3), InlineList.toList(Object.class, il));

		il = InlineList.addAll(Object.class, il, list(4, 5));
		assertEquals(list(1, 2, 3, 4, 5), InlineList.toList(Object.class, il));

		il = InlineList.add(Object.class, InlineList.newInlineList(), 15);
		il = InlineList.addAll(Object.class, il, list(4, 5));
		assertEquals(list(15, 4, 5), InlineList.toList(Object.class, il));

		il = InlineList.add(Object.class, InlineList.newInlineList(), 1);
		il = InlineList.addAll(Object.class, il, list(4));
		assertEquals(list(1, 4), InlineList.toList(Object.class, il));
		il = InlineList.addAll(Object.class, il, list(4, 4));
		assertEquals(list(1, 4, 4, 4), InlineList.toList(Object.class, il));

	}

	/**
	 * Test {@link InlineList#forEach(Class, Object, Consumer)}
	 */
	public void testForEach() {
		Object il = InlineList.newInlineList();
		List<Object> calledElements = new ArrayList<>();
		Consumer<Object> consumer = obj -> calledElements.add(obj);

		InlineList.forEach(String.class, il, consumer);
		assertEquals(list(), calledElements);
		calledElements.clear();

		il = InlineList.add(String.class, il, "a");
		InlineList.forEach(String.class, il, consumer);
		assertEquals(list("a"), calledElements);
		calledElements.clear();

		il = InlineList.add(String.class, il, "b");
		InlineList.forEach(String.class, il, consumer);
		assertEquals(list("a", "b"), calledElements);
		calledElements.clear();

		// Check with actual super type
		InlineList.forEach(Object.class, il, consumer);
		assertEquals(list("a", "b"), calledElements);
		calledElements.clear();

		il = InlineList.remove(il, "a");
		InlineList.forEach(String.class, il, consumer);
		assertEquals(list("b"), calledElements);
		calledElements.clear();

		il = InlineList.remove(il, "b");
		InlineList.forEach(String.class, il, consumer);
		assertEquals(list(), calledElements);
		calledElements.clear();

	}

	public void testCopyOnWrite() {
		Object list = InlineList.newInlineList();
		list = InlineList.addAll(String.class, list, Arrays.asList("a", "b", "c"));

		for (String x : InlineList.toList(String.class, list)) {
			list = InlineList.remove(list, x);
		}

		assertEquals(0, InlineList.size(list));
	}

    /** Return the suite of tests to execute.
    */
    public static Test suite () {
        return new TestSuite(TestInlineList.class);
    }

}

