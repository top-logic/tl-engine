/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.common.remote;

import java.io.IOException;
import java.util.HashSet;

import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.common.remote.factory.ReflectionFactory;
import com.top_logic.common.remote.shared.CustomDataMapping;
import com.top_logic.common.remote.shared.CustomObjectData;
import com.top_logic.common.remote.shared.DefaultSharedObject;
import com.top_logic.common.remote.shared.ObjectData;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.common.remote.shared.SharedObject;

/**
 * Test case for {@link ObjectScope}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestObjectScope extends AbstractObjectScopeTest {

	/**
	 * Default {@link SharedObject}.
	 */
	public static class A extends DefaultSharedObject {
		public A(ObjectScope scope) {
			super(scope);
		}

		public void setOther(Object value) {
			set("other", value);
		}

		@Override
		protected void onDelete() {
			// Nothing to do.
		}

	}

	/**
	 * Generalized {@link SharedObject} not inheriting from any base class.
	 */
	public static class B {

		private ObjectData _data;

		public B(ObjectScope scope) {
			super();
			_data = scope.data(this);
		}

		public int getX() {
			return _data.getDataInt("x");
		}

		public void setX(int value) {
			_data.setDataInt("x", value);
		}

		public void setOther(Object value) {
			_data.setData("other", value);
		}

	}

	public void testInitialUpdate() throws IOException {
		ObjectScope src = scope();
		ObjectScope dest = scope();

		A a1 = new A(src);

		transport(src, dest);

		assertExists(dest, src, a1);

		B b1 = new B(src);

		b1.setOther(a1);
		a1.setOther(b1);

		transport(src, dest);

		assertExists(dest, src, b1);

		assertRefEquals(dest, a1, "other", src, b1);
		assertRefEquals(dest, b1, "other", src, a1);
	}

	public void testCyclicInitialUpdate() throws IOException {
		ObjectScope src = scope();
		ObjectScope dest = scope();

		A a1 = new A(src);
		B b1 = new B(src);
		b1.setOther(a1);
		a1.setOther(b1);

		try {
			transport(src, dest);

			fail("Expected failure.");
		} catch (IllegalArgumentException exception) {
			assertCyclicErrorMessage(exception.getMessage());
		}
	}

	private void assertCyclicErrorMessage(String message) {
		HashSet<String> dependencyIds = new HashSet<>();

		dependencyIds.add("1");
		dependencyIds.add("2");

		assertEquals(CollectionUtilShared.CYCLIC_DEPENDENCIES_MESSAGE + dependencyIds, message);
	}

	public void testSync() throws IOException {
		ObjectScope src = scope();
		ObjectScope dest = scope();

		A a1 = new A(src);
		A a2 = new A(src);
		B b1 = new B(src);
		B b2 = new B(src);
		b1.setOther(b2);

		// Note: Order is important. This tests that an inner custom object (b2) is transported, if
		// it is assigned as a whole (b1->b2) to another shared object (a1).
		a1.setOther(b1);

		transport(src, dest);

		assertExists(dest, src, a1);
		assertExists(dest, src, a2);
		assertExists(dest, src, b1);
		assertExists(dest, src, b2);

		assertRefEquals(dest, a1, "other", src, b1);
		assertRefEquals(dest, b1, "other", src, b2);

		A a3 = new A(src);
		B b3 = new B(src);
		a1.setOther(a3);
		a2.setOther(b3);
		b2.setOther(b3);

		transport(src, dest);

		assertExists(dest, src, a3);
		assertExists(dest, src, b3);

		assertRefEquals(dest, a1, "other", src, a3);
		assertRefEquals(dest, a2, "other", src, b3);
		assertRefEquals(dest, b2, "other", src, b3);

		assertEquals(b2.getX(), 0);
		assertEquals(in(dest, src, b2).getX(), 0);

		b2.setX(42);

		transport(src, dest);

		assertEquals(b2.getX(), 42);
		assertEquals(in(dest, src, b2).getX(), 42);
	}

	private ObjectScope scope() {
		ObjectScope scope = new ObjectScope(ReflectionFactory.INSTANCE);
		scope.setDataMapping(new TestingDataMapping());
		return scope;
	}

	static final class TestingDataMapping extends CustomDataMapping {
		@Override
		protected ObjectData createData(ObjectScope scope, Object obj) {
			if (obj instanceof B) {
				return new CustomObjectData(scope, obj);
			}
			return null;
		}
	}

}
