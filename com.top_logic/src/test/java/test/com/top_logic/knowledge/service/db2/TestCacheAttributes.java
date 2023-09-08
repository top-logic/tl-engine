/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;

import com.top_logic.basic.Log;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.attr.ComputedMOAttribute;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.attr.storage.CacheValueFactory;
import com.top_logic.dob.attr.storage.CachedComputedAttributeStorage;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;

/**
 * Tests attribute with {@link CachedComputedAttributeStorage}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestCacheAttributes extends AbstractDBKnowledgeBaseClusterTest {

	protected static final String TEST_TYPE = "type";

	protected static final String TEST_ATTRIBUTE = "attribute";

	static class TestingValueFactory implements CacheValueFactory {

		/**
		 * Singleton {@link TestCacheAttributes.TestingValueFactory} instance.
		 */
		public static final TestingValueFactory INSTANCE = new TestingValueFactory();

		private TestingValueFactory() {
			// Singleton constructor.
		}

		@Override
		public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
			MOAttribute a1Attr = ((MOStructure) item.tTable()).getAttributeOrNull(A1_NAME);
			return value(a1Attr.getStorage().getCacheValue(a1Attr, item, storage));
		}

		static Object getValue(DataObject item) throws UnreachableAssertion {
			Object a1;
			try {
				a1 = item.getAttributeValue(A1_NAME);
			} catch (NoSuchAttributeException ex) {
				throw new UnreachableAssertion(
					"Attribute is registered at type " + B_NAME + " which has attribute " + A1_NAME, ex);
			}
			return value(a1);
		}

		private static String value(Object a1) {
			return A1_NAME + " value: " + a1;
		}

		@Override
		public boolean preserveCacheValue(MOAttribute cacheAttribute, DataObject changedObject,
				Object[] storage, MOAttribute changedAttribute) {
			// Don't care about change of other attributes.
			return !A1_NAME.equals(changedAttribute.getName());
		}

	}

	@Override
	protected LocalTestSetup createSetup(Test self) {
		DBKnowledgeBaseClusterTestSetup setup = (DBKnowledgeBaseClusterTestSetup) super.createSetup(self);
		setup.addAdditionalTypes(new TypeProvider() {

			@Override
			public void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository) {
				MOKnowledgeItemImpl testType = new MOKnowledgeItemImpl(TEST_TYPE);
				testType.setSuperclass(new DeferredMetaObject(B_NAME));
				ComputedMOAttribute computedMOAttribute = new ComputedMOAttribute(TEST_ATTRIBUTE, MOPrimitive.STRING);
				computedMOAttribute.setStorage(new CachedComputedAttributeStorage(new TestingValueFactory()));
				try {
					testType.addAttribute(computedMOAttribute);
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
		return setup;
	}

	public void testSimple() throws DataObjectException, RefetchTimeout {
		Transaction tx = begin();
		KnowledgeObject ko = kb().createKnowledgeObject(TEST_TYPE);
		assertCorrectCacheValue("Value for newly created object correct.", ko);
		setA1(ko, "a1");
		assertCorrectCacheValue("Value in transaction correct.", ko);
		tx.commit();
		assertCorrectCacheValue("Value after commit still correct.", ko);

		kbNode2().refetch();
		KnowledgeItem node2Ko = node2Item(ko);
		assertCorrectCacheValue("Value correct in cluster node.", node2Ko);

		Transaction chTx = begin();
		setA1(ko, "a1_new");
		assertCorrectCacheValue("Value after change correct.", ko);
		chTx.commit();
		assertCorrectCacheValue("Value after commit still correct.", ko);

		KnowledgeItem historicKI = HistoryUtils.getKnowledgeItem(tx.getCommitRevision(), ko);
		assertCorrectCacheValue("Value in historic object correct.", historicKI);

		kbNode2().refetch();
		assertCorrectCacheValue("Value correct in cluster node after refetch.", node2Ko);
	}

	private void assertCorrectCacheValue(String message, DataObject ko)
			throws UnreachableAssertion, NoSuchAttributeException {
		Object modifiedA1Value = TestingValueFactory.getValue(ko);
		assertEquals(message, modifiedA1Value, ko.getAttributeValue(TEST_ATTRIBUTE));
	}

	public void testDeleteObjectWithCachedAttribute() throws DataObjectException, RefetchTimeout {
		Transaction tx = begin();
		KnowledgeObject ko = kb().createKnowledgeObject(TEST_TYPE);
		setA1(ko, "a1");
		tx.commit();

		kbNode2().refetch();
		KnowledgeItem node2Ko = node2Item(ko);
		Transaction delTX = beginNode2();
		node2Ko.delete();
		delTX.commit();
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestCacheAttributes}.
	 */
	public static Test suite() {
		return suite(TestCacheAttributes.class);
	}

}
