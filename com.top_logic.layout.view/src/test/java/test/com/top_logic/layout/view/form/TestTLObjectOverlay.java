/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TransientObject;

/**
 * Tests for {@link TLObjectOverlay}.
 */
public class TestTLObjectOverlay extends TestCase {

	/**
	 * Tests that unchanged attributes delegate to the base object.
	 */
	public void testReadDelegatesToBase() {
		TLStructuredTypePart namePart = mockPart("name");
		MockTLObject base = new MockTLObject();
		base.set(namePart, "Alice");

		TLObjectOverlay overlay = new TLObjectOverlay(base);
		assertEquals("Alice", overlay.tValue(namePart));
	}

	/**
	 * Tests that writes are intercepted and stored locally.
	 */
	public void testWriteIntercepted() {
		TLStructuredTypePart namePart = mockPart("name");
		MockTLObject base = new MockTLObject();
		base.set(namePart, "Alice");

		TLObjectOverlay overlay = new TLObjectOverlay(base);
		overlay.tUpdate(namePart, "Bob");

		assertEquals("Bob", overlay.tValue(namePart));
		assertEquals("Alice", base.tValue(namePart));
	}

	/**
	 * Tests that the overlay starts clean (not dirty).
	 */
	public void testInitiallyNotDirty() {
		TLObjectOverlay overlay = new TLObjectOverlay(new MockTLObject());
		assertFalse(overlay.isDirty());
	}

	/**
	 * Tests that the overlay becomes dirty after a write.
	 */
	public void testDirtyAfterUpdate() {
		TLStructuredTypePart namePart = mockPart("name");
		TLObjectOverlay overlay = new TLObjectOverlay(new MockTLObject());

		overlay.tUpdate(namePart, "value");
		assertTrue(overlay.isDirty());
	}

	/**
	 * Tests that {@link TLObjectOverlay#isChanged(TLStructuredTypePart)} works correctly.
	 */
	public void testIsChanged() {
		TLStructuredTypePart namePart = mockPart("name");
		TLStructuredTypePart agePart = mockPart("age");
		TLObjectOverlay overlay = new TLObjectOverlay(new MockTLObject());

		overlay.tUpdate(namePart, "value");
		assertTrue(overlay.isChanged(namePart));
		assertFalse(overlay.isChanged(agePart));
	}

	/**
	 * Tests that {@link TLObjectOverlay#reset()} clears all changes.
	 */
	public void testReset() {
		TLStructuredTypePart namePart = mockPart("name");
		MockTLObject base = new MockTLObject();
		base.set(namePart, "Alice");

		TLObjectOverlay overlay = new TLObjectOverlay(base);
		overlay.tUpdate(namePart, "Bob");
		assertTrue(overlay.isDirty());

		overlay.reset();
		assertFalse(overlay.isDirty());
		assertEquals("Alice", overlay.tValue(namePart));
	}

	/**
	 * Tests that {@link TLObjectOverlay#applyTo(TLObject)} transfers changes to a target.
	 */
	public void testApplyTo() {
		TLStructuredTypePart namePart = mockPart("name");
		TLStructuredTypePart agePart = mockPart("age");
		MockTLObject base = new MockTLObject();
		base.set(namePart, "Alice");
		base.set(agePart, Integer.valueOf(30));

		TLObjectOverlay overlay = new TLObjectOverlay(base);
		overlay.tUpdate(namePart, "Bob");
		overlay.tUpdate(agePart, Integer.valueOf(25));

		MockTLObject target = new MockTLObject();
		overlay.applyTo(target);

		assertEquals("Bob", target.tValue(namePart));
		assertEquals(Integer.valueOf(25), target.tValue(agePart));
	}

	/**
	 * Tests that {@link TLObjectOverlay#getBase()} returns the original object.
	 */
	public void testGetBase() {
		MockTLObject base = new MockTLObject();
		TLObjectOverlay overlay = new TLObjectOverlay(base);
		assertSame(base, overlay.getBase());
	}

	/**
	 * Tests that {@link TLObjectOverlay#tType()} delegates to the base object.
	 */
	public void testTTypeDelegates() {
		MockTLObject base = new MockTLObject();
		TLObjectOverlay overlay = new TLObjectOverlay(base);
		assertSame(base.tType(), overlay.tType());
	}

	/**
	 * Tests that null values can be stored as changes.
	 */
	public void testNullValueChange() {
		TLStructuredTypePart namePart = mockPart("name");
		MockTLObject base = new MockTLObject();
		base.set(namePart, "Alice");

		TLObjectOverlay overlay = new TLObjectOverlay(base);
		overlay.tUpdate(namePart, null);

		assertTrue(overlay.isChanged(namePart));
		assertNull(overlay.tValue(namePart));
	}

	/**
	 * Tests that multiple attributes can be changed independently.
	 */
	public void testMultipleAttributes() {
		TLStructuredTypePart namePart = mockPart("name");
		TLStructuredTypePart agePart = mockPart("age");
		TLStructuredTypePart emailPart = mockPart("email");

		MockTLObject base = new MockTLObject();
		base.set(namePart, "Alice");
		base.set(agePart, Integer.valueOf(30));
		base.set(emailPart, "alice@example.com");

		TLObjectOverlay overlay = new TLObjectOverlay(base);
		overlay.tUpdate(namePart, "Bob");

		assertEquals("Bob", overlay.tValue(namePart));
		assertEquals(Integer.valueOf(30), overlay.tValue(agePart));
		assertEquals("alice@example.com", overlay.tValue(emailPart));
	}

	/**
	 * Creates a mock {@link TLStructuredTypePart} using a dynamic proxy.
	 *
	 * <p>
	 * The proxy uses identity equality and supports {@code getName()} and {@code toString()}.
	 * </p>
	 */
	private static TLStructuredTypePart mockPart(String name) {
		return (TLStructuredTypePart) Proxy.newProxyInstance(
			TLStructuredTypePart.class.getClassLoader(),
			new Class<?>[] { TLStructuredTypePart.class },
			new PartHandler(name));
	}

	/**
	 * Minimal {@link InvocationHandler} for mock {@link TLStructuredTypePart} instances.
	 */
	private static class PartHandler implements InvocationHandler {

		private final String _name;

		PartHandler(String name) {
			_name = name;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			switch (method.getName()) {
				case "getName":
					return _name;
				case "toString":
					return "MockPart(" + _name + ")";
				case "hashCode":
					return System.identityHashCode(proxy);
				case "equals":
					return proxy == args[0];
				default:
					return null;
			}
		}
	}

	/**
	 * Minimal mock {@link TLObject} that stores attribute values in a map.
	 *
	 * <p>
	 * Uses identity-based keys (the {@link TLStructuredTypePart} proxy instances). This avoids
	 * needing any TypeIndex or service infrastructure.
	 * </p>
	 */
	private static class MockTLObject extends TransientObject {

		private final Map<TLStructuredTypePart, Object> _values = new LinkedHashMap<>();

		@Override
		public Object tValue(TLStructuredTypePart part) {
			return _values.get(part);
		}

		@Override
		public void tUpdate(TLStructuredTypePart part, Object value) {
			_values.put(part, value);
		}

		/**
		 * Convenience setter for test setup.
		 */
		void set(TLStructuredTypePart part, Object value) {
			_values.put(part, value);
		}
	}
}
