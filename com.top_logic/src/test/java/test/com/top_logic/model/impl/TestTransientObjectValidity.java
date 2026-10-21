/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.impl;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.impl.TransientTLObjectImpl;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for the {@link TLObject#tContainer() container} and the {@link TLObject#tValid() validity}
 * of a {@link TransientTLObjectImpl}: a transient object is part of the container it was created
 * in or that holds it in a composite reference, and dies with that container.
 */
@SuppressWarnings("javadoc")
public class TestTransientObjectValidity extends AbstractDBKnowledgeBaseTest {

	private TLClass _type;

	private TLReference _children;

	private TLReference _refs;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		TLModule module = TLModelUtil.addModule(model, "test");
		_type = TLModelUtil.addClass(module, "Element");
		_children = addListReference(module, "children", true);
		_refs = addListReference(module, "refs", false);
	}

	private TLReference addListReference(TLModule module, String name, boolean composite) {
		TLAssociationEnd end =
			TLModelUtil.addEnd(TLModelUtil.addAssociation(module, "Element$" + name), name, _type);
		end.setComposite(composite);
		TLReference ref = TLModelUtil.addReference(_type, name, end);
		ref.setMultiple(true);
		ref.setOrdered(true);
		return ref;
	}

	@Override
	protected void tearDown() throws Exception {
		_type = null;
		_children = null;
		_refs = null;

		super.tearDown();
	}

	/**
	 * An object created without a container exists on its own and stays valid.
	 */
	public void testValidWithoutContainer() {
		assertTrue(newTransient(null).tValid());
	}

	/**
	 * An object created in a persistent container is valid while that container lives.
	 */
	public void testDiesWithContainer() throws DataObjectException {
		TLObject container = newPersistentObject();
		TLObject element = newTransient(container);

		assertTrue("The container is alive.", element.tValid());

		delete(container);

		assertFalse("The container is deleted.", element.tValid());
	}

	/**
	 * An object created in a transient container follows that container's own container.
	 */
	public void testDiesWithGrandContainer() throws DataObjectException {
		TLObject container = newPersistentObject();
		TLObject inner = newTransient(container);
		TLObject element = newTransient(inner);

		assertTrue("The container of the container is alive.", element.tValid());

		delete(container);

		assertFalse("The container of the container is deleted.", element.tValid());
	}

	/**
	 * An object added to a composite reference has the referer as container.
	 */
	public void testContainerByCompositeAdd() {
		TLObject container = newTransient(null);
		TLObject element = newTransient(null);

		container.tAdd(_children, element);

		assertSame(container, element.tContainer());
		assertSame(_children, element.tContainerReference());
	}

	/**
	 * An object set as value of a composite reference has the referer as container.
	 */
	public void testContainerByCompositeUpdate() {
		TLObject container = newTransient(null);
		TLObject element = newTransient(null);

		container.tUpdate(_children, List.of(element));

		assertSame(container, element.tContainer());
		assertSame(_children, element.tContainerReference());
	}

	/**
	 * A non-composite reference does not make a container.
	 */
	public void testNoContainerByNonCompositeReference() {
		TLObject referer = newTransient(null);
		TLObject element = newTransient(null);

		referer.tAdd(_refs, element);
		assertNull(element.tContainer());
		assertNull(element.tContainerReference());

		referer.tUpdate(_refs, List.of(element));
		assertNull(element.tContainer());
		assertNull(element.tContainerReference());
	}

	/**
	 * Removing an object from a composite reference makes it container-less again.
	 */
	public void testNoContainerAfterRemove() {
		TLObject container = newTransient(null);
		TLObject element = newTransient(null);
		TLObject other = newTransient(null);

		container.tAdd(_children, element);
		container.tRemove(_children, element);
		assertNull(element.tContainer());
		assertNull(element.tContainerReference());

		container.tUpdate(_children, List.of(element, other));
		assertSame(container, element.tContainer());
		container.tUpdate(_children, List.of(other));
		assertNull(element.tContainer());
		assertNull(element.tContainerReference());
		assertSame(container, other.tContainer());
	}

	/**
	 * The container an object was created in wins over a composite reference holding it.
	 */
	public void testCreationContextWins() {
		TLObject a = newTransient(null);
		TLObject b = newTransient(null);
		TLObject element = newTransient(a);

		b.tAdd(_children, element);

		assertSame(a, element.tContainer());
		assertNull(element.tContainerReference());
	}

	/**
	 * An object held in a composite reference dies with the container of its holder.
	 */
	public void testDiesWithCompositeContainer() throws DataObjectException {
		TLObject persistent = newPersistentObject();
		TLObject holder = newTransient(persistent);
		TLObject element = newTransient(null);

		holder.tAdd(_children, element);

		assertTrue("The container of the holder is alive.", element.tValid());

		delete(persistent);

		assertFalse("The container of the holder is deleted.", element.tValid());
		assertSame(holder, element.tContainer());
	}

	private TLObject newTransient(TLObject container) {
		return TransientObjectFactory.INSTANCE.createObject(_type, container);
	}

	private TLObject newPersistentObject() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject item = newB("b1");
		tx.commit();
		return item.getWrapper();
	}

	private void delete(TLObject object) {
		Transaction tx = begin();
		object.tDelete();
		tx.commit();
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		return suite(TestTransientObjectValidity.class);
	}

}
