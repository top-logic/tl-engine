/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.AObj;

import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.KIReference;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;

/**
 * The class {@link TestBenchmark} contains benchmark test.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
@DeactivatedTest("Benchmark test that creates complex scenario which must not be executed during nightly build.")
public class TestBenchmark extends AbstractDBKnowledgeBaseClusterTest {

	static final String TYPE_NAME = "TestBenchmark";

	static final String REF_CUR_LOCAL_POLY_DELETE = "parent";

	@Override
	protected LocalTestSetup createSetup(Test self) {
		DBKnowledgeBaseTestSetup setup = new DBKnowledgeBaseClusterTestSetup(self);
		setup.addAdditionalTypes(new TypeProvider() {
			
			@Override
			public void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository) {
				MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(TYPE_NAME);
				type.setSuperclass(new DeferredMetaObject(KnowledgeBaseTestScenarioConstants.A_NAME));
				MOReference attribute = KIReference.referenceById(REF_CUR_LOCAL_POLY_DELETE, type);
				attribute.setDeletionPolicy(DeletionPolicy.DELETE_REFERER);
				attribute.setBranchGlobal(false);
				attribute.setHistoryType(HistoryType.CURRENT);
				attribute.setMonomorphic(false);
				try {
					type.addAttribute(attribute);
				} catch (DuplicateAttributeException ex) {
					throw new UnreachableAssertion(ex);
				}
				KnowledgeBaseTestScenarioImpl.setApplicationType(type, AObj.class);
				try {
					typeRepository.addMetaObject(type);
				} catch (DuplicateTypeException ex) {
					throw new KnowledgeBaseRuntimeException(ex);
				}
			}
		});
		return setup;
	}

	public void testRefetch() throws DataObjectException {
		setup(B_NAME, 1000, 10000);
		refetchNode2();

		Transaction tx = kb().beginTransaction();
		newB("newB").setAttributeValue(A2_NAME, "refetch value");
		tx.commit();
		
		fetchLastChange(tx.getCommitRevision());

	}

	private void fetchLastChange(Revision lastRevision) {
		StopWatch sw = StopWatch.createStartedWatch();
		Revision startRevision = kb().getHistoryManager().getRevision(lastRevision.getCommitNumber() - 1);
		try (ChangeSetReader reader = kb().getDiffReader(startRevision, trunk(), lastRevision, trunk(), true)) {
			ChangeSet cs;
			while ((cs = reader.read()) != null) {
				// just read stream
			}
		}
		sw.stop();
		Logger
			.info("Refetch needed " + StopWatch.toStringNanos(sw.getElapsedNanos()) + ".", TestBenchmark.class);
	}

	private List<KnowledgeItem> setup(String typeName, int numberItems, int numberModifications)
			throws DataObjectException, KnowledgeBaseException {
		List<KnowledgeItem> items = new ArrayList<>(numberItems);
		Transaction tx = kb().beginTransaction();
		for (int i = 0; i < numberItems; i++) {
			KnowledgeObject result = kb().createKnowledgeObject(typeName);
			setA1(result, "item_" + i);
			KnowledgeObject item = result;
			items.add(item);
		}
		tx.commit();
		Random r = new Random(1000L);
		for (int i = 0; i < numberModifications; i++) {
			int maxNumberItemsPerCommit = r.nextInt(50) + 1;
			Transaction modTx = kb().beginTransaction();
			while (maxNumberItemsPerCommit-- > 0) {
				items.get(r.nextInt(numberItems)).setAttributeValue(A1_NAME, "val_" + i);
			}
			modTx.commit();
		}
		return items;
	}

	public void testReferenceIndexes() throws DataObjectException {
		int numberItems = 500;
		int iterations = 100;
		for (int i = 0; i < iterations; i++) {
			setup(E_NAME, numberItems, 20);
		}
		StopWatch sw = StopWatch.createStartedWatch();
		int numberElements = 1000;
		for (int i = 0; i < numberElements; i++) {
			KnowledgeObject item;
			Transaction createTx = begin();
			try {
				item = newD("a" + i);
				createTx.commit();
			} finally {
				createTx.rollback();
			}

			Transaction deleteTx = begin();
			try {
				item.delete();
				deleteTx.commit();
			} finally {
				deleteTx.rollback();
			}
		}
		Logger.info("Creation, deletion of " + numberElements + " elements with " + numberItems * iterations
			+ " living objects needed " + StopWatch.toStringNanos(sw.getElapsedNanos()), TestBenchmark.class);
		/* Using MOReferenceIndex: Creation, deletion of 1000 elements with 50000 living objects
		 * needed 5 min 16 s 661.631357 ms */
		/* Not using MOReferenceIndex: Creation, deletion of 1000 elements with 50000 living objects
		 * needed 23 min 32 s 90.436174 ms */
	}

	public void testBenchmarkSetPrimitiveValueToNewObject() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject b = newB("b1");
		int numberSet = 1000000;
		// Run test without measuring to initialise JIT
		internalTestBenchmarkSetPrimitiveValue(b, numberSet);
		StopWatch sw = StopWatch.createStartedWatch();
		internalTestBenchmarkSetPrimitiveValue(b, numberSet);
		sw.stop();
		commit(createTx);

		Logger.info("Needed " + StopWatch.toStringNanos(sw.getElapsedNanos()) + " to call setValue " + numberSet
			+ " times to new object with simple value.", TestBenchmark.class);
		/* Before: Needed 339.466096 ms to call setValue 1000000 times to new object with simple
		 * value. */
		/* After: Needed 285.909343 ms to call setValue 1000000 times to new object with simple
		 * value. */
		/* Optimization ~1,187 */
	}

	public void testBenchmarkSetPrimitiveValueToPersistentObject() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject b = newB("b1");
		commit(createTx);

		Transaction changeTx = begin();
		int numberSet = 1000000;
		// Run test without measuring to initialise JIT
		internalTestBenchmarkSetPrimitiveValue(b, numberSet);
		StopWatch sw = StopWatch.createStartedWatch();
		internalTestBenchmarkSetPrimitiveValue(b, numberSet);
		sw.stop();
		commit(changeTx);

		Logger.info("Needed " + StopWatch.toStringNanos(sw.getElapsedNanos()) + " to call setValue " + numberSet
			+ " times to persistent object with simple value.", TestBenchmark.class);
		/* Before: Needed Needed 353.990310 ms to call setValue 1000000 times to persistent object
		 * with simple value. */
		/* After: Needed 303.474829 ms to call setValue 1000000 times to persistent object with
		 * simple value. */
		/* Optimization ~1,166 */
	}

	private void internalTestBenchmarkSetPrimitiveValue(KnowledgeObject b, int numberSet) throws DataObjectException {
		for (int i = 0; i < numberSet; i++) {
			setA1(b, String.valueOf(i));
		}
	}

	public void testBenchmarkSetReferenceValueToNewObject() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject d = newD("b1");
		KnowledgeObject ref1 = newReference("ref1");
		KnowledgeObject ref2 = newReference("ref2");
		int numberSet = 1000000;
		// Run test without measuring to initialise JIT
		internalTestBenchmarkSetReferenceValue(d, ref1, ref2, numberSet);
		StopWatch sw = StopWatch.createStartedWatch();
		internalTestBenchmarkSetReferenceValue(d, ref1, ref2, numberSet);
		sw.stop();
		commit(createTx);

		Logger.info("Needed " + StopWatch.toStringNanos(sw.getElapsedNanos()) + " to call setValue " + numberSet
			+ " times to new object with reference value.", TestBenchmark.class);
		/* Before: Needed 1 s 761.218107 ms to call setValue 1000000 times to new object with
		 * reference value. */
		/* After: Needed 703.336053 ms to call setValue 1000000 times to new object with reference
		 * value. */
		/* Optimization ~1,504 */
	}

	public void testBenchmarkSetReferenceValueToPersistentObject() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject d = newD("b1");
		commit(createTx);

		Transaction changeTx = begin();
		KnowledgeObject ref1 = newReference("ref1");
		KnowledgeObject ref2 = newReference("ref2");
		int numberSet = 1000000;
		// Run test without measuring to initialise JIT
		internalTestBenchmarkSetReferenceValue(d, ref1, ref2, numberSet);
		StopWatch sw = StopWatch.createStartedWatch();
		internalTestBenchmarkSetReferenceValue(d, ref1, ref2, numberSet);
		sw.stop();
		commit(changeTx);

		Logger.info("Needed " + StopWatch.toStringNanos(sw.getElapsedNanos()) + " to call setValue " + numberSet
			+ " times to persistent object with reference value.", TestBenchmark.class);
		/* Before: Needed 2 s 384.897516 ms to call setValue 1000000 times to persistent object with
		 * reference value. */
		/* After: Needed 859.613567 ms to call setValue 1000000 times to persistent object with
		 * reference value. */
		/* Optimization ~2,774 */
	}

	private void internalTestBenchmarkSetReferenceValue(KnowledgeObject d, KnowledgeObject ref1, KnowledgeObject ref2, int numberSet)
			throws DataObjectException {
		String attr = getReferenceAttr(!MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);

		for (int i = 0; i < numberSet; i++) {
			KnowledgeObject ref;
			if (i % 2 == 0) {
				ref = ref1;
			} else {
				ref = ref2;
			}
			d.setAttributeValue(attr, ref);
		}
	}

	/**
	 * Equal to {@link #testCascadingDeleteTree()} but with a type and reference attribute such that
	 * only this attribute must be checked during deletion.
	 */
	public void testCascadingDeleteTree2() throws DataObjectException {
		// initialize JIT and cached queries
		createDelete(TYPE_NAME, REF_CUR_LOCAL_POLY_DELETE, 1, 2);
		int level = 2;
		int itemsPerLevel = 100;
		createDelete(TYPE_NAME, REF_CUR_LOCAL_POLY_DELETE, level, itemsPerLevel);
		/* Measured with MySQL. */
		/* Before: */
		/* Needed 25 s 767.833251 ms to collect object to delete for tree with 2 levels and 100
		 * items per level (i.e. 10101 items). */
		/* Needed 3 s 227.972306 ms to commit deleted objects of tree with 2 levels and 100 items
		 * per level (i.e. 10101 items). */
		/* Needed 28 s 995.805557 ms to delete tree with 2 levels and 100 items per level (i.e.
		 * 10101 items). */
		/* After: */
		/* Needed 628.231238 ms to collect object to delete for tree with 2 levels and 100 items per
		 * level (i.e. 10101 items). */
		/* Needed 3 s 571.329341 ms to commit deleted objects of tree with 2 levels and 100 items
		 * per level (i.e. 10101 items). */
		/* Needed 4 s 199.560579 ms to delete tree with 2 levels and 100 items per level (i.e. 10101
		 * items). */

		/* Measured with ORACLE. */
		/* Before: */
		/* Needed 3 min 37 s 785.915208 ms to collect object to delete for tree with 2 levels and
		 * 100 items per level (i.e. 10101 items). */
		/* Needed 40 s 575.994435 ms to commit deleted objects of tree with 2 levels and 100 items
		 * per level (i.e. 10101 items). */
		/* Needed 4 min 18 s 361.909643 ms to delete tree with 2 levels and 100 items per level
		 * (i.e. 10101 items). */
		/* After: */
		/* Needed 31 s 302.482210 ms to collect object to delete for tree with 2 levels and 100
		 * items per level (i.e. 10101 items). */
		/* Needed 42 s 428.784290 ms to commit deleted objects of tree with 2 levels and 100 items
		 * per level (i.e. 10101 items). */
		/* Needed 1 min 13 s 731.266500 ms to delete tree with 2 levels and 100 items per level
		 * (i.e. 10101 items). */
	}

	public void testCascadingDeleteTree() throws DataObjectException {
		// initialize JIT and cached queries
		createEDelete(1, 2);
		int level = 2;
		int itemsPerLevel = 100;
		createEDelete(level, itemsPerLevel);
		/* Measured with MySQL. */
		/* Before: Needed 6 min 0 s 884.153711 ms to delete tree with 2 levels and 100 items per
		 * level (i.e. 10000.0 items). */
		/* After: Needed 8 s 716.071720 ms to delete tree with 2 levels and 100 items per level
		 * (i.e. 10000.0 items). */

		/* Measured with ORACLE. */
		/* Before: Needed 25 min 59 s 127.859346 ms to delete tree with 2 levels and 100 items per
		 * level (i.e. 10000.0 items). */
		/* After: Needed 4 min 43 s 296.082460 ms to delete tree with 2 levels and 100 items per
		 * level (i.e. 10000.0 items). */
	}

	private void createEDelete(int level, int itemsPerLevel) throws DataObjectException {
		createDelete(E_NAME, REFERENCE_DELETE_POLICY_NAME, level, itemsPerLevel);
	}

	private void createDelete(String typeName, String parentAttr, int level, int itemsPerLevel)
			throws DataObjectException {
		Transaction createTX = begin();
		KnowledgeObject parent = newA(typeName, "root");
		int allItems = 0;
		for (int i = 0; i <= level; i++) {
			allItems += (int) Math.pow(itemsPerLevel, i);
		}
		Logger.info("Create " + allItems + " items.", TestBenchmark.class);
		createTree(parent, parentAttr, level, itemsPerLevel);
		commit(createTX);
		StopWatch sw = StopWatch.createStartedWatch();
		Transaction deleteTX = begin();
		Logger.info("Delete " + allItems + " items by just deleting parent.", TestBenchmark.class);
		parent.delete();
		long collectNanos = sw.getElapsedNanos();
		Logger.info("Needed " + StopWatch.toStringNanos(collectNanos) + " to collect object to delete for tree with "
			+ level + " levels and " + itemsPerLevel + " items per level (i.e. " + allItems + " items).",
			TestBenchmark.class);
		sw.reset();
		sw.start();
		commit(deleteTX);
		long commitNanos = sw.getElapsedNanos();
		Logger.info("Needed " + StopWatch.toStringNanos(commitNanos) + " to commit deleted objects of  tree with "
			+ level + " levels and " + itemsPerLevel + " items per level (i.e. " + allItems + " items).",
			TestBenchmark.class);
		Logger.info("Needed " + StopWatch.toStringNanos(collectNanos + commitNanos) + " to delete tree with " + level
			+ " levels and " + itemsPerLevel + " items per level (i.e. " + allItems + " items).", TestBenchmark.class);

	}

	private void createTree(KnowledgeObject parent, String parentAttr, int level, int itemsPerLevel)
			throws DataObjectException {
		if (level == 0) {
			return;
		}
		for (int i = 0; i < itemsPerLevel; i++) {
			KnowledgeObject child = newA(parent.tTable().getName(), "child");
			child.setAttributeValue(parentAttr, parent);
			createTree(child, parentAttr, level - 1, itemsPerLevel);
		}
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestBenchmark}.
	 */
	public static Test suite() {
		return runOneTest(TestBenchmark.class, "testCascadingDeleteTree2", DBType.MYSQL_DB);
	}
}
