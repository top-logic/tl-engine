/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.basic.AssertNoErrorLogListener;

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
import com.top_logic.dob.attr.storage.ComputedAttributeStorage;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
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

	/**
	 * Computed attribute whose value can not be computed for the {@link #A1_NAME} value
	 * {@link FailingValueFactory#_failingA1}.
	 */
	protected static final String FAILING_ATTRIBUTE = "failingAttribute";

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

	/**
	 * {@link CacheValueFactory} that fails for a configurable value of {@link #A1_NAME}, like a
	 * derived value whose computation rule no longer accepts the stored source value.
	 */
	static class FailingValueFactory implements CacheValueFactory {

		/**
		 * Value of {@link #A1_NAME} for which the computation fails, <code>null</code> for never.
		 */
		static volatile String _failingA1;

		@Override
		public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
			MOAttribute a1Attr = ((MOStructure) item.tTable()).getAttributeOrNull(A1_NAME);
			Object a1 = a1Attr.getStorage().getCacheValue(a1Attr, item, storage);
			if (a1 != null && a1.equals(_failingA1)) {
				throw new IllegalStateException("Value can not be computed for " + A1_NAME + " value: " + a1);
			}
			return "computed: " + a1;
		}

		@Override
		public boolean preserveCacheValue(MOAttribute cacheAttribute, DataObject changedObject,
				Object[] storage, MOAttribute changedAttribute) {
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
				ComputedMOAttribute failingAttribute = new ComputedMOAttribute(FAILING_ATTRIBUTE, MOPrimitive.STRING);
				failingAttribute.setStorage(new ComputedAttributeStorage(new FailingValueFactory()));
				try {
					testType.addAttribute(failingAttribute);
				} catch (DuplicateAttributeException ex) {
					log.error("Duplicate attribute " + FAILING_ATTRIBUTE, ex);
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
	 * Computed attributes are not part of the change event of a commit, neither on the committing
	 * node nor on a node that receives the change through refetch.
	 */
	public void testComputedAttributeNotInChangeEvent() throws DataObjectException, RefetchTimeout {
		Transaction tx = begin();
		KnowledgeObject ko = kb().createKnowledgeObject(TEST_TYPE);
		setA1(ko, "a1");
		tx.commit();
		kbNode2().refetch();

		List<ItemUpdate> localUpdates = new ArrayList<>();
		UpdateListener localListener = updateCollector(localUpdates);
		List<ItemUpdate> remoteUpdates = new ArrayList<>();
		UpdateListener remoteListener = updateCollector(remoteUpdates);
		kb().addUpdateListener(localListener);
		kbNode2().addUpdateListener(remoteListener);
		try {
			Transaction chTx = begin();
			setA1(ko, "a1_new");
			chTx.commit();

			kbNode2().refetch();
		} finally {
			kb().removeUpdateListener(localListener);
			kbNode2().removeUpdateListener(remoteListener);
		}

		assertChangeOfA1Only("Local", localUpdates);
		assertChangeOfA1Only("Remote", remoteUpdates);
	}

	private void assertChangeOfA1Only(String message, List<ItemUpdate> updates) {
		assertEquals(message + " updates: " + updates, 1, updates.size());
		ItemUpdate update = updates.get(0);
		Map<String, Object> newValues = update.getValues();
		assertEquals(message, "a1_new", newValues.get(A1_NAME));
		assertFalse(message + " update contains computed attribute: " + update, newValues.containsKey(TEST_ATTRIBUTE));
		assertFalse(message + " update contains computed attribute: " + update,
			newValues.containsKey(FAILING_ATTRIBUTE));
	}

	private static UpdateListener updateCollector(List<ItemUpdate> updates) {
		return new UpdateListener() {
			@Override
			public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
				updates.addAll(event.getChanges().getUpdates());
			}
		};
	}

	/**
	 * A commit does not compute the value of a computed attribute for the state before the
	 * commit, which may no longer be computable.
	 */
	public void testNoErrorForUncomputableOldValue() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject ko = kb().createKnowledgeObject(TEST_TYPE);
		setA1(ko, "outdated");
		tx.commit();

		FailingValueFactory._failingA1 = "outdated";
		AssertNoErrorLogListener logListener = new AssertNoErrorLogListener(false);
		logListener.activate();
		try {
			Transaction chTx = begin();
			setA1(ko, "current");
			chTx.commit();
			logListener.assertNoErrorLogged("Commit of a change away from an uncomputable value: ");
		} finally {
			logListener.deactivate();
			FailingValueFactory._failingA1 = null;
		}
		assertEquals("computed: current", ko.getAttributeValue(FAILING_ATTRIBUTE));
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestCacheAttributes}.
	 */
	public static Test suite() {
		return suite(TestCacheAttributes.class);
	}

}
