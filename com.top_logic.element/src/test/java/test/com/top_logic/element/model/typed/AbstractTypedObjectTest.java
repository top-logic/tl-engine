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
import com.top_logic.dsa.util.DataAccessProxyBinaryContent;
import com.top_logic.element.model.DefaultModelFactory;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.impl.TypedObjectClasses;
import com.top_logic.util.model.ModelService;

/**
 * Base class for tests of objects that implement the generated Java interfaces of their type
 * through {@link TypedObjectClasses}.
 *
 * <p>
 * Sets up a model with <code>tl.element:StructuredElement</code> and the test module
 * {@link #MODULE} of <code>TestTypedObjectClasses.model.xml</code>, whose types are bound to the
 * Java interfaces of this package. All changes of a test are reverted afterwards.
 * </p>
 */
public abstract class AbstractTypedObjectTest extends TLModelTest {

	/**
	 * Name of the test module.
	 */
	protected static final String MODULE = "testTypedObjectClasses";

	/**
	 * The model defining <code>tl.element:StructuredElement</code>.
	 */
	private static final String TL_ELEMENT_MODEL = "webinf://model/tl.element.model.xml";

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
			extendModel(new DataAccessProxyBinaryContent(TL_ELEMENT_MODEL));
			extendModel(ClassRelativeBinaryContent.withSuffix(TestTypedObjectClasses.class, "model.xml"));
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		KBUtils.revert(kb(), _preSetUpRevision);
		super.tearDown();
	}

	/**
	 * The {@link KnowledgeBase} of the test.
	 */
	protected static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * Creates a transient object of the type with the given name in the test module.
	 */
	protected TLObject newTransient(String typeName) {
		return TransientObjectFactory.INSTANCE.createObject(type(MODULE + ":" + typeName), null, null);
	}

	/**
	 * Creates a persistent object of the type with the given name in the test module.
	 *
	 * <p>
	 * Must be called in a transaction.
	 * </p>
	 */
	protected TLObject newPersistent(String typeName) {
		return getFactory().createObject(type(MODULE + ":" + typeName), null, null);
	}

	/**
	 * The test suite for the given test class with a knowledge base and the model service.
	 */
	protected static Test suite(Class<? extends AbstractTypedObjectTest> testClass) {
		Test modelTest = ServiceTestSetup.createSetup(testClass, ModelService.Module.INSTANCE);
		Test kbTest = KBSetup.getSingleKBTest(modelTest);
		return TestUtils.doNotMerge(kbTest);
	}

}
