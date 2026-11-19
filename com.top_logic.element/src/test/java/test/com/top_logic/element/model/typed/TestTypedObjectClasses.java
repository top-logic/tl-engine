/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.typed;

import junit.framework.Test;

import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.model.util.TLModelTest;
import test.com.top_logic.element.model.util.TLModelTestUtil;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.element.model.DefaultModelFactory;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.impl.TransientTLObjectImpl;
import com.top_logic.model.impl.TypedObjectClasses;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link TypedObjectClasses} with transient objects created by the
 * {@link TransientObjectFactory}.
 *
 * <p>
 * The test module binds its types to the Java interfaces of this package: the interfaces
 * <code>Named</code> and <code>Coded</code> have the Java interfaces {@link Named} and
 * {@link Coded}, the classes <code>NamedItem</code> and <code>Item</code> inherit from them
 * without a Java interface of their own, <code>Plain</code> has no Java interface at all, and
 * <code>Node</code> has the Java interface {@link Node} extending a framework interface.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestTypedObjectClasses extends TLModelTest {

	private static final String MODULE = "testTypedObjectClasses";

	private Revision _preSetUpRevision;

	@Override
	protected TLModel setUpModel() {
		return TLModelTestUtil.createTLModelInTransaction(kb());
	}

	@Override
	protected TLFactory setUpFactory() {
		return new DefaultModelFactory();
	}

	@Override
	protected void tearDownModel() {
		// Model changes are reverted in tearDown().
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_preSetUpRevision = HistoryUtils.getSessionRevision();
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			extendModel(ClassRelativeBinaryContent.withSuffix(TestTypedObjectClasses.class, "model.xml"));
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		KBUtils.revert(kb(), _preSetUpRevision);
		super.tearDown();
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private TLObject newTransient(String typeName) {
		return TransientObjectFactory.INSTANCE.createObject(type(MODULE + ":" + typeName), null, null);
	}

	/**
	 * A transient object of a type with a Java interface implements it, and its getter and setter
	 * access the value of the object.
	 */
	public void testInterfaceOfGeneralization() {
		TLObject object = newTransient("NamedItem");

		assertTrue(object instanceof TransientTLObjectImpl);
		assertTrue(object instanceof Named);
		Named named = (Named) object;

		named.setName("n1");
		assertEquals("n1", object.tValueByName(Named.NAME_ATTR));

		object.tUpdateByName(Named.NAME_ATTR, "n2");
		assertEquals("n2", named.getName());
	}

	/**
	 * A type with several generalizations implements the Java interfaces of all of them.
	 */
	public void testInterfacesOfAllGeneralizations() {
		TLObject object = newTransient("Item");

		assertTrue(object instanceof Named);
		assertTrue(object instanceof Coded);

		((Coded) object).setCode("c1");
		((Named) object).setName("n1");
		assertEquals("c1", object.tValueByName(Coded.CODE_ATTR));
		assertEquals("n1", object.tValueByName(Named.NAME_ATTR));
	}

	/**
	 * A type without any Java interface uses the base class itself.
	 */
	public void testNoInterface() {
		TLClass plain = type(MODULE + ":Plain");

		assertSame(TransientTLObjectImpl.class, TypedObjectClasses.classFor(TransientTLObjectImpl.class, plain));
		assertSame(TransientTLObjectImpl.class, newTransient("Plain").getClass());
	}

	/**
	 * The generated class is created once for the same type.
	 */
	public void testClassCached() {
		TLClass item = type(MODULE + ":Item");

		Class<?> first = TypedObjectClasses.classFor(TransientTLObjectImpl.class, item);
		assertNotSame(TransientTLObjectImpl.class, first);
		assertSame(first, TypedObjectClasses.classFor(TransientTLObjectImpl.class, item));
		assertSame(first, newTransient("Item").getClass());
	}

	/**
	 * Two types with the same Java interfaces share the generated class.
	 */
	public void testClassSharedByInterfaces() {
		assertSame(
			TypedObjectClasses.classFor(TransientTLObjectImpl.class, type(MODULE + ":NamedItem")),
			TypedObjectClasses.classFor(TransientTLObjectImpl.class, type(MODULE + ":Named")));
	}

	/**
	 * An abstract method of a framework interface that the base class does not implement fails
	 * with a message naming the type.
	 */
	public void testUnsupportedFrameworkMethod() {
		TLObject object = newTransient("Node");
		assertTrue(object instanceof Node);
		Node node = (Node) object;

		object.tUpdateByName(Node.LABEL_ATTR, "l1");
		assertEquals("l1", node.getLabel());

		try {
			node.getTypes();
			fail("Abstract framework method must not be implemented.");
		} catch (UnsupportedOperationException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains(MODULE + ":Node"));
			assertTrue(ex.getMessage(), ex.getMessage().contains("getTypes"));
		}
	}

	/**
	 * Test suite.
	 */
	public static Test suite() {
		Test modelTest = ServiceTestSetup.createSetup(TestTypedObjectClasses.class, ModelService.Module.INSTANCE);
		Test kbTest = KBSetup.getSingleKBTest(modelTest);
		return TestUtils.doNotMerge(kbTest);
	}

}
