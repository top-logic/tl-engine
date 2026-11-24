/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.form.overlay;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.element.model.typed.AbstractTypedObjectTest;
import test.com.top_logic.element.model.typed.Named;
import test.com.top_logic.element.model.typed.Node;

import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.overlay.ObjectConstructor;
import com.top_logic.element.meta.form.overlay.ObjectCreation;
import com.top_logic.element.meta.form.overlay.ObjectEditing;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.ResPrefix;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLType;
import com.top_logic.model.impl.TypedObjectClasses;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for the form buffers {@link ObjectEditing} and {@link ObjectCreation} of an
 * {@link AttributeUpdateContainer} implementing the generated Java interfaces of their type, see
 * {@link TypedObjectClasses}.
 */
@SuppressWarnings("javadoc")
public class TestTypedFormObjectOverlay extends AbstractTypedObjectTest {

	private static final String LOCAL_TYPE = "LocalType";

	private AttributeFormContext _form;

	/**
	 * Creates persistent objects with the factory of the test.
	 */
	private final ObjectConstructor _constructor = new ObjectConstructor() {
		@Override
		public TLObject newInstance(TLFormObject overlay) {
			return getFactory().createObject((TLClass) overlay.tType(), overlay.tContainer());
		}

		@Override
		public boolean isTransient(TLFormObject overlay) {
			return false;
		}
	};

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_form = new AttributeFormContext(ResPrefix.forTest(MODULE));
	}

	@Override
	protected void tearDown() throws Exception {
		_form = null;
		super.tearDown();
	}

	private TLObject persistent(String typeName, String name) {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject result = newPersistent(typeName);
			result.tUpdateByName(TLNamed.NAME_ATTRIBUTE, name);
			tx.commit();
			return result;
		}
	}

	private TLFormObject create(String typeName) {
		return _form.createObject(type(MODULE + ":" + typeName), null, null, _constructor);
	}

	private TLObject allocate(TLFormObject creation) {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			((ObjectCreation) creation).create();
			tx.commit();
		}
		TLObject created = creation.getEditedObject();
		assertNotNull(created);
		return created;
	}

	/**
	 * The buffer of a persistent object implements the Java interface of its type. Its typed
	 * accessors read and write the buffered value, the persistent object is not changed.
	 */
	public void testEditing() {
		TLObject object = persistent("NamedItem", "p1");
		TLFormObject overlay = _form.editObject(object);

		assertTrue(overlay instanceof ObjectEditing);
		assertTrue(overlay instanceof Named);
		Named named = (Named) overlay;
		assertEquals("p1", named.getName());

		overlay.tUpdateByName(Named.NAME_ATTR, "p2");
		assertEquals("p2", named.getName());
		assertEquals("p1", object.tValueByName(Named.NAME_ATTR));

		named.setName("p3");
		assertEquals("p3", overlay.tValueByName(Named.NAME_ATTR));
		assertEquals("p1", object.tValueByName(Named.NAME_ATTR));

		assertSame(overlay, _form.editObject(object));
		assertEquals(object.tId(), overlay.tId());
		assertSame(object.tHandle(), overlay.tHandle());
	}

	/**
	 * The buffer of a new object implements the Java interface of its type. It has no handle
	 * before the object is allocated, and the handle of the allocated object afterwards.
	 */
	public void testCreation() {
		TLFormObject overlay = create("NamedItem");

		assertTrue(overlay instanceof ObjectCreation);
		assertTrue(overlay instanceof Named);
		Named named = (Named) overlay;

		named.setName("c1");
		assertEquals("c1", overlay.tValueByName(Named.NAME_ATTR));
		assertEquals("c1", named.getName());

		assertTrue(overlay.tTransient());
		try {
			overlay.tHandle();
			fail("A new object has no handle before it is allocated.");
		} catch (UnsupportedOperationException ex) {
			// Expected.
		}

		TLObject created = allocate(overlay);
		assertSame(created.tHandle(), overlay.tHandle());
		assertEquals(created.tId(), overlay.tId());
	}

	/**
	 * The buffer of a persistent object of a type below <code>tl.element:StructuredElement</code>
	 * implements {@link StructuredElement}: The name and {@link Wrapper#getValue(String)},
	 * {@link Wrapper#setValue(String, Object)} access the buffer, the handle and the types are
	 * those of the persistent object.
	 */
	public void testStructuredElementEditing() {
		TLObject object = persistent("Node", "n1");
		TLClass localType = localType(object);
		TLFormObject overlay = _form.editObject(object);

		assertTrue(overlay instanceof ObjectEditing);
		assertTrue(overlay instanceof Node);
		assertTrue(overlay instanceof StructuredElement);
		Node node = (Node) overlay;

		assertEquals("n1", node.getName());
		node.setValue(TLNamed.NAME_ATTRIBUTE, "n2");
		assertEquals("n2", node.getName());
		assertEquals("n2", node.getValue(TLNamed.NAME_ATTRIBUTE));
		assertEquals("n2", overlay.tValueByName(TLNamed.NAME_ATTRIBUTE));
		assertEquals("n1", object.tValueByName(TLNamed.NAME_ATTRIBUTE));

		node.setLabel("l1");
		assertEquals("l1", overlay.tValueByName(Node.LABEL_ATTR));
		assertNull(object.tValueByName(Node.LABEL_ATTR));

		assertSame(object.tHandle(), node.tHandle());
		assertSame(localType, node.getType(LOCAL_TYPE));
		assertEquals(list(((TLScope) object).getTypes()), list(node.getTypes()));
		assertTrue(node.getTypes().contains(localType));
	}

	/**
	 * The buffer of a new object of a type below <code>tl.element:StructuredElement</code> has
	 * neither handle nor types before it is allocated; afterwards both are those of the allocated
	 * object.
	 */
	public void testStructuredElementCreation() {
		TLFormObject overlay = create("Node");

		assertTrue(overlay instanceof ObjectCreation);
		assertTrue(overlay instanceof Node);
		Node node = (Node) overlay;

		node.setValue(TLNamed.NAME_ATTRIBUTE, "c1");
		assertEquals("c1", node.getName());
		assertEquals("c1", overlay.tValueByName(TLNamed.NAME_ATTRIBUTE));

		try {
			node.tHandle();
			fail("A new object has no handle before it is allocated.");
		} catch (UnsupportedOperationException ex) {
			// Expected.
		}
		assertTrue(node.getTypes().isEmpty());
		assertNull(node.getType(LOCAL_TYPE));

		TLObject created = allocate(overlay);
		TLClass localType = localType(created);

		assertSame(created.tHandle(), node.tHandle());
		assertSame(localType, node.getType(LOCAL_TYPE));
		assertEquals(list(((TLScope) created).getTypes()), list(node.getTypes()));
	}

	/**
	 * Creates a type in the scope of the given persistent object.
	 */
	private TLClass localType(TLObject scope) {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			TLClass result = TLModelUtil.addClass(module(MODULE), (TLScope) scope, LOCAL_TYPE);
			tx.commit();
			return result;
		}
	}

	private static List<TLType> list(Iterable<TLType> types) {
		List<TLType> result = new ArrayList<>();
		types.forEach(result::add);
		return result;
	}

	/**
	 * Test suite.
	 */
	public static Test suite() {
		return suite(TestTypedFormObjectOverlay.class);
	}

}
