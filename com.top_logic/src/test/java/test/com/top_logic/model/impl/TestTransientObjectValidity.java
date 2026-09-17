/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.impl;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.impl.TransientTLObjectImpl;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for the {@link TLObject#tValid() validity} of a {@link TransientTLObjectImpl}: a transient
 * object dies with the container it was created in.
 */
@SuppressWarnings("javadoc")
public class TestTransientObjectValidity extends AbstractDBKnowledgeBaseTest {

	private TLClass _type;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		_type = TLModelUtil.addClass(TLModelUtil.addModule(model, "test"), "Element");
	}

	@Override
	protected void tearDown() throws Exception {
		_type = null;

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
