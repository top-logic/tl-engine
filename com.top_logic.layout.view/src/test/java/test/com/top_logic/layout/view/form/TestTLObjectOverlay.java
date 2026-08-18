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

import com.top_logic.basic.StringID;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TransientObject;

/**
 * Tests for {@link TLObjectOverlay}.
 */
public class TestTLObjectOverlay extends TestCase {

	/** Branch context of the trunk branch, the only branch mock objects live on. */
	private static final long TRUNK = 1L;

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
	 * Tests that {@link TLObjectOverlay#apply()} transfers changes to the base object.
	 */
	public void testApply() {
		TLStructuredTypePart namePart = mockPart("name");
		TLStructuredTypePart agePart = mockPart("age");
		MockTLObject base = new MockTLObject();
		base.set(namePart, "Alice");
		base.set(agePart, Integer.valueOf(30));

		TLObjectOverlay overlay = new TLObjectOverlay(base);
		overlay.tUpdate(namePart, "Bob");
		overlay.tUpdate(agePart, Integer.valueOf(25));

		overlay.apply();

		assertEquals("Bob", base.tValue(namePart));
		assertEquals(Integer.valueOf(25), base.tValue(agePart));
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
	 * Tests that the overlay is a {@link TLFormObjectBase} reporting the object it buffers the edits
	 * for.
	 */
	public void testIsFormObject() {
		MockTLObject base = new MockTLObject();
		TLObjectOverlay overlay = new TLObjectOverlay(base);

		assertTrue("An editing buffer must be a form object.", overlay instanceof TLFormObjectBase);
		assertFalse(overlay.isCreate());
		assertSame(base, overlay.getEditedObject());
		assertNull(overlay.getDomain());
	}

	/**
	 * Tests that the overlay stands for the object being edited, so that an identity-comparing
	 * check (e.g. a uniqueness constraint) does not treat it as a different object.
	 */
	public void testStandsForEditedObject() {
		MockTLObject base = new MockTLObject(objectKey("o1"));
		TLObjectOverlay overlay = new TLObjectOverlay(base);

		assertEquals(base.tId(), overlay.tId());
		assertEquals("The overlay must be the object it stands for.", base, overlay);
		assertEquals("Identity must be symmetric.", overlay, base);
		assertEquals(base.hashCode(), overlay.hashCode());
	}

	/**
	 * Tests that an overlay of a different object is not identity-equal.
	 */
	public void testDifferentObjectsNotEqual() {
		TLObjectOverlay overlay1 = new TLObjectOverlay(new MockTLObject(objectKey("o1")));
		TLObjectOverlay overlay2 = new TLObjectOverlay(new MockTLObject(objectKey("o2")));

		assertFalse(overlay1.equals(overlay2));
	}

	/**
	 * Tests that the form value of an attribute is the edited one, while the base value is the one
	 * the editing started from.
	 */
	public void testFieldValueVsBaseValue() {
		TLStructuredTypePart namePart = mockPart("name");
		MockTLObject base = new MockTLObject();
		base.set(namePart, "Alice");

		TLObjectOverlay overlay = new TLObjectOverlay(base);
		overlay.tUpdate(namePart, "Bob");

		assertEquals("Bob", overlay.getFieldValue(namePart));
		assertEquals("Alice", overlay.getBaseValue(namePart));
		assertEquals("Alice", overlay.defaultValue(namePart));
	}

	/**
	 * Creates an {@link ObjectKey} identifying a mock object.
	 */
	private static ObjectKey objectKey(String id) {
		return new DefaultObjectKey(TRUNK, Revision.CURRENT_REV, new MOClassImpl("MockType"),
			StringID.valueOf(id));
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

		private final ObjectKey _id;

		private final Map<TLStructuredTypePart, Object> _values = new LinkedHashMap<>();

		/**
		 * Creates a {@link MockTLObject} without identity.
		 */
		MockTLObject() {
			this(null);
		}

		/**
		 * Creates a {@link MockTLObject} identified by the given key.
		 */
		MockTLObject(ObjectKey id) {
			_id = id;
		}

		@Override
		public ObjectKey tId() {
			return _id;
		}

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
