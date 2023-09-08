/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.InlineSet;

/**
 * Test the {@link TestInlineSet}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestInlineSet extends TestCase {

    /**
	 * Test Main usage of InlineSet.
	 */
    public void testMain() {
		Object il = InlineSet.newInlineSet();
        
        String v1 = "v1";
		String v2 = "v2";
		String v3 = "v3";

		assertFalse(InlineSet.contains(il, null));
		assertTrue(InlineSet.toSet(String.class, il).isEmpty());
		assertEquals(0, InlineSet.toArray(il).length);
		assertFalse(InlineSet.contains(il, null));
		assertFalse(InlineSet.contains(il, null));
		assertEquals(InlineSet.newInlineSet(), il = InlineSet.remove(il, v1));

		il = InlineSet.add(String.class, il, v1);

		assertFalse(InlineSet.contains(il, null));
		assertTrue(InlineSet.contains(il, v1));
		assertFalse(InlineSet.toSet(String.class, il).isEmpty());
		assertEquals(1, InlineSet.toArray(il).length);
		assertFalse(InlineSet.contains(il, null));
		assertNotNull(il = InlineSet.remove(il, null));
		assertEquals(InlineSet.newInlineSet(), il = InlineSet.remove(il, v1));

		il = InlineSet.add(String.class, il, v1);
		il = InlineSet.add(String.class, il, v1);

		assertFalse(InlineSet.contains(il, null));
		assertTrue(InlineSet.contains(il, v1));
		assertEquals("Set does not contain duplicates", 1, InlineSet.toSet(String.class, il).size());
		assertEquals("Set does not contain duplicates", 1, 2, InlineSet.toArray(il).length);
		assertFalse(InlineSet.contains(il, null));
		assertNotNull(il = InlineSet.remove(il, null));
		assertNotNull(il = InlineSet.remove(il, v1));
		assertEquals(InlineSet.newInlineSet(), il = InlineSet.remove(il, v1));

		il = InlineSet.add(String.class, il, v1);
		il = InlineSet.add(String.class, il, v2);
		il = InlineSet.add(String.class, il, v3);
        
		assertFalse(InlineSet.contains(il, null));
		assertTrue(InlineSet.contains(il, v1));
		assertEquals(v1, InlineSet.toSet(String.class, il).iterator().next());
		assertEquals(3, InlineSet.toSet(String.class, il).size());
		assertEquals(3, InlineSet.toArray(il).length);
		assertFalse(InlineSet.contains(il, il));
		assertNotNull(il = InlineSet.remove(il, null));
		assertNotNull(il = InlineSet.remove(il, v1));
		assertNotNull(il = InlineSet.remove(il, v2));
		assertEquals(InlineSet.newInlineSet(), il = InlineSet.remove(il, v3));
    }

	/**
	 * Test {@link InlineSet#toArray(Object, Object[])}.
	 */
	public void testToArray() {
		Object il = InlineSet.newInlineSet();
		String[] output = new String[1];
		output[0] = "toClear";
		String[] array0 = InlineSet.toArray(il, output);
		assertSame("Contents fit into the array: Contract in toArray(Object[]).", output, array0);
		assertNull("Element after size should be null: Contract in toArray(Object[])", array0[0]);

		String v1 = "v1";

		il = InlineSet.add(Object.class, il, v1);

		String[] array1 = InlineSet.toArray(il, output);
		assertSame("Contents fit into the array: Contract in toArray(Object[]).", output, array1);
		assertEquals(v1, array1[0]);

		String[] output2 = new String[21];
		output2[0] = "v2";
		output2[1] = "v3";
		String[] array2 = InlineSet.toArray(il, output2);
		assertSame("Contents fit into the array: Contract in toArray(Object[]).", output2, array2);
		assertEquals(v1, array2[0]);
		assertNull("Element after size should be null: Contract in toArray(Object[])", array2[1]);

		String[] output3 = new String[0];
		String[] array3 = InlineSet.toArray(il, output3);
		assertNotSame("Contents does not fit into the array.", output3, array3);
		assertEquals(v1, array3[0]);

		String v2 = "v2";
		il = InlineSet.add(Object.class, il, v2);
		assertTrue(Arrays.equals(new String[] { v1, v2 }, InlineSet.toArray(il, new String[0])));
	}

	/**
	 * Test {@link InlineSet#clear(Object)}.
	 */
	public void testClear() {
		Object il = InlineSet.newInlineSet();
		assertTrue(InlineSet.isEmpty(il));
		String v1 = "v1";
		String v2 = "v2";

		il = InlineSet.add(Object.class, il, v1);
		assertFalse(InlineSet.isEmpty(il));
		il = InlineSet.add(Object.class, il, v2);
		assertFalse(InlineSet.isEmpty(il));
		il = InlineSet.clear(il);
		assertTrue(InlineSet.isEmpty(il));
	}

	/**
	 * Test {@link InlineSet#size(Object)}.
	 */
	public void testSize() {
		Object il = InlineSet.newInlineSet();
		assertEquals(0, InlineSet.size(il));

		String v1 = "v1";
		il = InlineSet.add(Object.class, il, v1);
		assertEquals(1, InlineSet.size(il));

		String v2 = "v2";
		il = InlineSet.add(Object.class, il, v2);
		assertEquals(2, InlineSet.size(il));

		il = InlineSet.clear(il);
		assertEquals(0, InlineSet.size(il));
	}

	/**
	 * Test {@link InlineSet#addAll(Class, Object, java.util.Collection)}
	 */
	public void testAddAll() {
		Object il = InlineSet.newInlineSet();

		il = InlineSet.addAll(Object.class, il, new ArrayList<>());
		assertTrue(InlineSet.isEmpty(il));

		il = InlineSet.addAll(Object.class, il, list(1, 2, 3));
		assertEquals(set(1, 2, 3), InlineSet.toSet(Object.class, il));
		il = InlineSet.addAll(Object.class, il, list(1, 2, 3));
		assertEquals(set(1, 2, 3), InlineSet.toSet(Object.class, il));

		il = InlineSet.addAll(Object.class, il, list(4, 5));
		assertEquals(set(1, 2, 3, 4, 5), InlineSet.toSet(Object.class, il));

		il = InlineSet.add(Object.class, InlineSet.newInlineSet(), 15);
		il = InlineSet.addAll(Object.class, il, list(4, 5));
		assertEquals(set(15, 4, 5), InlineSet.toSet(Object.class, il));

		il = InlineSet.add(Object.class, InlineSet.newInlineSet(), 1);
		il = InlineSet.addAll(Object.class, il, list(4));
		assertEquals(set(1, 4), InlineSet.toSet(Object.class, il));
		il = InlineSet.addAll(Object.class, il, list(4, 4));
		assertEquals(set(1, 4), InlineSet.toSet(Object.class, il));

	}

	/**
	 * Test {@link InlineSet#forEach(Class, Object, Consumer)}
	 */
	public void testForEach() {
		Object is = InlineSet.newInlineSet();
		Set<Object> calledElements = new HashSet<>();
		Consumer<Object> consumer = obj -> calledElements.add(obj);

		InlineSet.forEach(String.class, is, consumer);
		assertEquals(set(), calledElements);
		calledElements.clear();

		is = InlineSet.add(String.class, is, "a");
		InlineSet.forEach(String.class, is, consumer);
		assertEquals(set("a"), calledElements);
		calledElements.clear();

		is = InlineSet.add(String.class, is, "b");
		InlineSet.forEach(String.class, is, consumer);
		assertEquals(set("a", "b"), calledElements);
		calledElements.clear();

		is = InlineSet.remove(is, "a");
		InlineSet.forEach(String.class, is, consumer);
		assertEquals(set("b"), calledElements);
		calledElements.clear();

		is = InlineSet.remove(is, "b");
		InlineSet.forEach(String.class, is, consumer);
		assertEquals(set(), calledElements);
		calledElements.clear();

	}

    /** Return the suite of tests to execute.
    */
    public static Test suite () {
        return new TestSuite(TestInlineSet.class);
    }

}

