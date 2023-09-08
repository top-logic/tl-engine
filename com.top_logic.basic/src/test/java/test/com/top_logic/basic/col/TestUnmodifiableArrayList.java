/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.UnmodifiableArrayList;

/**
 * Tests for the {@link UnmodifiableArrayList}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestUnmodifiableArrayList extends TestCase {

	public void testEmptyList() {
		Object[] storage = new String[0];
		List<Object> unmodifiableList = UnmodifiableArrayList.newUnmodifiableList(storage);
		assertTrue(unmodifiableList.isEmpty());
	}

	public void testSerializable() throws IOException, ClassNotFoundException {
		String[] storage = new String[] { "a", "b" };
		List<String> unmodifiableList = UnmodifiableArrayList.newUnmodifiableList(storage);
		assertInstanceof("Tests expects implementation of " + UnmodifiableArrayList.class, unmodifiableList,
			UnmodifiableArrayList.class);
		assertInstanceof("Tests expects implementation of " + Serializable.class, unmodifiableList, Serializable.class);

		byte[] byteArray = serialize(unmodifiableList);
		Object readObject = deserialize(byteArray);

		assertSame(unmodifiableList.getClass(), readObject.getClass());
		assertEquals(unmodifiableList, readObject);
	}

	private Object deserialize(byte[] input) throws IOException, ClassNotFoundException {
		ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(input));
		try {
			return stream.readObject();
		} finally {
			stream.close();
		}
	}

	private byte[] serialize(Object serializable) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		try {
			objectOutputStream.writeObject(serializable);
		} finally {
			objectOutputStream.close();
		}
		return outputStream.toByteArray();
	}

	public void testUnmodifiable() {
		Object[] storage = new String[] { "a", "b" };
		List<Object> unmodifiableList = UnmodifiableArrayList.newUnmodifiableList(storage);
		try {
			unmodifiableList.add("x");
			fail("List is unmodifiable.");
		} catch (Exception ex) {
			// expected
		}

		try {
			unmodifiableList.remove("a");
			fail("List is unmodifiable.");
		} catch (Exception ex) {
			// expected
		}
	}

	public void testSize() {
		Object[] storage = new String[] { "a", "b" };
		List<Object> unmodifiableList = UnmodifiableArrayList.newUnmodifiableList(storage);
		assertEquals(2, unmodifiableList.size());
		assertFalse(unmodifiableList.isEmpty());
	}

	public void testToArray() {
		Object[] storage = new String[] { "a", "b" };
		List<Object> unmodifiableList = UnmodifiableArrayList.newUnmodifiableList(storage);

		Object[] array = unmodifiableList.toArray();
		BasicTestCase.assertEquals(new Object[] { "a", "b" }, array);
		array[0] = 15;
		assertEquals(15, array[0]);
		assertEquals("toArray does not clone array.", "a", unmodifiableList.get(0));

		BasicTestCase.assertEquals(unmodifiableList.toArray(), unmodifiableList.toArray(new Object[0]));

		Object[] toArray = new Object[4];
		Arrays.fill(toArray, "XXX");
		Object[] result = unmodifiableList.toArray(toArray);
		assertSame(toArray, result);
		assertEquals("a", toArray[0]);
		assertEquals("b", toArray[1]);
		assertEquals(null, toArray[2]);

	}

	public void testContains() {
		Object[] storage = new String[] { "a", "b", "a" };
		List<Object> unmodifiableList = UnmodifiableArrayList.newUnmodifiableList(storage);

		assertFalse(unmodifiableList.contains(new Object()));
		assertTrue(unmodifiableList.contains("a"));
		assertSame(0, unmodifiableList.indexOf("a"));
		assertSame(2, unmodifiableList.lastIndexOf("a"));

	}

}


