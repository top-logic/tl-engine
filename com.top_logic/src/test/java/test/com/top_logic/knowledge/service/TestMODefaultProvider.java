/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.DBKnowledgeBaseTestSetup;

import com.top_logic.basic.Log;
import com.top_logic.basic.col.KeyValueBuffer;
import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MODefaultProvider;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;

/**
 * Test for {@link MODefaultProvider}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestMODefaultProvider extends AbstractDBKnowledgeBaseTest {

	static final class TestingStringDefaultProvider implements MODefaultProvider {

		/** Singleton {@link TestingStringDefaultProvider} instance. */
		public static final TestingStringDefaultProvider INSTANCE = new TestingStringDefaultProvider();

		String _defaultValue = "default";

		@Override
		public Object createDefault(MOAttribute attribute) {
			return _defaultValue;
		}

	}

	static final String TEST_TYPE = "TestType";

	static final String TEST_ATTRIBUTE = "testAttribute";

	@Override
	protected LocalTestSetup createSetup(Test self) {
		DBKnowledgeBaseTestSetup localSetup = (DBKnowledgeBaseTestSetup) super.createSetup(self);
		localSetup.addAdditionalTypes(new TypeProvider() {

			@Override
			public void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository) {
				MOKnowledgeItemImpl testType = new MOKnowledgeItemImpl(TEST_TYPE);
				testType.setSuperclass(new DeferredMetaObject(B_NAME));
				MOAttributeImpl attribute = new MOAttributeImpl(TEST_ATTRIBUTE, MOPrimitive.STRING);
				attribute.setDefaultProvider(TestingStringDefaultProvider.INSTANCE);
				try {
					testType.addAttribute(attribute);
				} catch (DuplicateAttributeException ex) {
					log.error("Duplicate attribute " + TEST_ATTRIBUTE, ex);
				}
				try {
					typeRepository.addMetaObject(testType);
				} catch (DuplicateTypeException ex) {
					log.error("Duplicate type " + TEST_TYPE, ex);
				}

			}
		});
		return localSetup;
	}

	public void testDefaultValue() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject ko = kb().createKnowledgeObject(TEST_TYPE);
		setA1(ko, "a1");
		assertEquals(TestingStringDefaultProvider.INSTANCE._defaultValue, ko.getAttributeValue(TEST_ATTRIBUTE));
		tx.commit();
	}

	public void testAttributeValueCanBeSet() throws DataObjectException {
		String notDefaultValue = "not default value";
		testCanBeSet(notDefaultValue);
	}

	public void testAttributeValueCanBeSetToNull() throws DataObjectException {
		String notDefaultValue = null;
		testCanBeSet(notDefaultValue);
	}

	private void testCanBeSet(String notDefaultValue)
			throws DataObjectException, NoSuchAttributeException, KnowledgeBaseException {
		Transaction tx = begin();
		KnowledgeObject ko = kb().createKnowledgeObject(TEST_TYPE);
		setA1(ko, "a1");
		ko.setAttributeValue(TEST_ATTRIBUTE, notDefaultValue);
		assertNotEquals(TestingStringDefaultProvider.INSTANCE._defaultValue, ko.getAttributeValue(TEST_ATTRIBUTE));
		assertEquals(notDefaultValue, ko.getAttributeValue(TEST_ATTRIBUTE));
		tx.commit();
	}

	public void testInitialValueIsNotOverridden() throws DataObjectException {
		String notDefault = "not default value";
		testInitialValueNotOverridden(notDefault);
	}

	public void testInitialNullValueIsNotOverridden() throws DataObjectException {
		String notDefault = null;
		testInitialValueNotOverridden(notDefault);
	}

	private void testInitialValueNotOverridden(String notDefaultValue)
			throws DataObjectException, NoSuchAttributeException, KnowledgeBaseException {
		Transaction tx = begin();
		KeyValueBuffer<String, Object> initialValues = new NameValueBuffer().put(TEST_ATTRIBUTE, notDefaultValue);
		KnowledgeObject ko = kb().createKnowledgeObject(TEST_TYPE, initialValues);
		setA1(ko, "a1");
		assertNotEquals(TestingStringDefaultProvider.INSTANCE._defaultValue, ko.getAttributeValue(TEST_ATTRIBUTE));
		assertEquals(notDefaultValue, ko.getAttributeValue(TEST_ATTRIBUTE));
		tx.commit();
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestMODefaultProvider}.
	 */
	public static Test suite() {
		return suite(TestMODefaultProvider.class);
	}

}
