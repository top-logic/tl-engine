/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import test.com.top_logic.KBTestUtils;
import test.com.top_logic.basic.ExpectedError;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.AObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.SB;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.BooleanFlag;
import com.top_logic.basic.col.KeyValueBuffer;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.H2Helper;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.objects.ChangeInspectable;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.LifecycleAttributes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.Committable;
import com.top_logic.knowledge.service.CommittableAdapter;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.DeletedObjectAccess;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * Test case for {@link DBKnowledgeBase}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
@SuppressWarnings("javadoc")
public class TestDBKnowledgeBase extends AbstractDBKnowledgeBaseTest {
	
	public void testGlobalAttributeValue() throws DataObjectException {
		testGlobalAttributeValueAccess(A2_NAME);
	}

	public void testGlobalDynamicAttributeValue() throws DataObjectException {
		testGlobalAttributeValueAccess("flexAttribute");
	}

	private void testGlobalAttributeValueAccess(String attributeName) throws DataObjectException,
			NoSuchAttributeException {
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		assertNull(b1.getAttributeValue(attributeName));
		b1.setAttributeValue(attributeName, "value");
		assertEquals("Local value not visible", "value", b1.getAttributeValue(attributeName));
		try {
			b1.getGlobalAttributeValue(attributeName);
			fail("No global values, because object is not committed yet.");
		} catch (DeletedObjectAccess ex) {
			// Expected
		}
		commit(tx);

		assertEquals("value", b1.getAttributeValue(attributeName));
		assertEquals("value", b1.getGlobalAttributeValue(attributeName));

		Transaction chTX = begin();
		b1.setAttributeValue(attributeName, "value2");
		assertEquals("value2", b1.getAttributeValue(attributeName));
		assertEquals("Must not see local value.", "value", b1.getGlobalAttributeValue(attributeName));
		rollback(chTX);

		assertEquals("value", b1.getAttributeValue(attributeName));
		assertEquals("value", b1.getGlobalAttributeValue(attributeName));
		
		Transaction delTX = begin();
		b1.delete();
		try {
			b1.getAttributeValue(attributeName);
			fail("Item locally deleted.");
		} catch (DeletedObjectAccess ex) {
			// expected.
		}
		assertEquals("value", b1.getGlobalAttributeValue(attributeName));
		rollback(delTX);

		assertEquals("value", b1.getAttributeValue(attributeName));
		assertEquals("value", b1.getGlobalAttributeValue(attributeName));
	}

	public void testAnyReferer() throws DataObjectException {
		KnowledgeObject match1 = newD("match");
		KnowledgeObject match2 = newD("match");
		KnowledgeObject noMatch = newD("noMatch");
		Transaction tx = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, match1);

		KnowledgeObject e2 = newE("e2");
		e2.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, noMatch);

		KnowledgeObject e3 = newE("e3");
		e3.setAttributeValue(REFERENCE_STABILISE_POLICY_NAME, match2);

		KnowledgeObject f1 = newF("t1");
		f1.setAttributeValue(REFERENCE_STABILISE_POLICY_NAME, match1);
		tx.commit();

		assertNotEquals("Tests needs that reference has not stabilize policy.", DeletionPolicy.STABILISE_REFERENCE,
			MetaObjectUtils.getReference(type(E_NAME), REFERENCE_MONO_CUR_LOCAL_NAME).getDeletionPolicy());
		assertEquals("Tests needs that reference has stabilize policy.", DeletionPolicy.STABILISE_REFERENCE,
			MetaObjectUtils.getReference(type(E_NAME), REFERENCE_STABILISE_POLICY_NAME).getDeletionPolicy());

		List<KnowledgeItem> referer =
			kb().getAnyReferer(Revision.CURRENT, type(D_NAME), list(match1, match2), null, KnowledgeItem.class);
		assertEquals(set(e1, e3, f1), toSet(referer));

		List<KnowledgeItem> referer2 =
			kb().getAnyReferer(Revision.CURRENT, type(D_NAME), list(match1, match2),
				DeletionPolicy.STABILISE_REFERENCE, KnowledgeItem.class);
		assertEquals(set(e3, f1), toSet(referer2));

		Transaction chTX = begin();
		f1.setAttributeValue(REFERENCE_STABILISE_POLICY_NAME, noMatch);

		List<KnowledgeItem> refererInTX =
			kb().getAnyReferer(Revision.CURRENT, type(D_NAME), list(match1, match2), null, KnowledgeItem.class);
		assertEquals(set(e1, e3), toSet(refererInTX));

		List<KnowledgeItem> referer2InTX =
			kb().getAnyReferer(Revision.CURRENT, type(D_NAME), list(match1, match2),
				DeletionPolicy.STABILISE_REFERENCE, KnowledgeItem.class);
		assertEquals(set(e3), toSet(referer2InTX));

		chTX.rollback();

		List<KnowledgeItem> refererAfterRollback =
			kb().getAnyReferer(Revision.CURRENT, type(D_NAME), list(match1, match2), null, KnowledgeItem.class);
		assertEquals(set(e1, e3, f1), toSet(refererAfterRollback));

		List<KnowledgeItem> referer2AfterRollback =
			kb().getAnyReferer(Revision.CURRENT, type(D_NAME), list(match1, match2),
				DeletionPolicy.STABILISE_REFERENCE, KnowledgeItem.class);
		assertEquals(set(e3, f1), toSet(referer2AfterRollback));

	}

	public void testDuplicateConcurrentCommit() throws DataObjectException, InterruptedException {
		Transaction tx1 = begin();
		KnowledgeObject b1 = newB("b1");
		final KnowledgeObject b2 = newB("b2");
		commit(tx1);
		final Barrier barrier1 = createBarrier(1);
		final Barrier barrier2 = createBarrier(1);
		final long timeout = 1000;
		inParallel(new Execution() {
			
			@Override
			public void run() throws Exception {
				Transaction tx2 = begin();
				KnowledgeObject b3 = newB("b3");
				newAB(b2, b3);
				barrier1.enter(timeout);
				// wait until source of association is deleted.
				barrier2.await(timeout);
				commit(tx2, "Association source b2 was deleted concurrently", true);
			}
		});
		// wait until concurrent thread has made changes
		barrier1.await(timeout);
		Transaction tx3 = begin();
		b1.delete();
		commit(tx3);
		Transaction tx4 = begin();
		b2.delete();
		commit(tx4);
		barrier2.enter(timeout);
	}

	public void testMassiveItemOperations() throws DataObjectException {
		testMassiveItemOperations(false);
		testMassiveItemOperations(true);
	}

	private void testMassiveItemOperations(boolean bulkDelete) {
		int numberElements = 10000;
		KnowledgeObject[] items = new KnowledgeObject[numberElements];
		try (Transaction createTx = begin()) {
			for (int i = 0; i < items.length; i++) {
				items[i] = newB("a" + i);
			}
			createTx.commit();
		}

		try (Transaction updateTx = begin()) {
			for (int i = 0; i < items.length; i++) {
				items[i].setAttributeValue(A2_NAME, "a2_" + i);
			}
			updateTx.commit();
		}

		try (Transaction deleteTx = begin()) {
			if (bulkDelete) {
				kb().deleteAll(Arrays.asList(items));
			} else {
				ArrayUtil.forEach(items, KnowledgeItem::delete);
			}
			deleteTx.commit();
		}
	}

	public void testEmptyMandatoryAttribute() throws DataObjectException {
		KnowledgeObject b;
		Transaction tx = begin();
		try {
			b = newB("a1");
			tx.commit();
		} finally {
			tx.rollback();
		}

		Transaction emptyAttributeTx = begin();
		try {
			setA1(b, "");
			try {
				emptyAttributeTx.commit();
				fail("Attribute '" + A1_NAME + "' is mandatory. Must not be set to empty string.");
			} catch (KnowledgeBaseException ex) {
				// expected
			}
		} finally {
			emptyAttributeTx.rollback();
		}

	}

	public void testMandatoryAttributeInNewObject() throws DataObjectException {
		Transaction tx = begin();
		try {
			KnowledgeObject b = kb().createKnowledgeObject(B_NAME);
			assertTrue("Mandatory tests is only useful for mandatory attributes.",
				MetaObjectUtils.getAttribute(b.tTable(), A1_NAME).isMandatory());
			b.setAttributeValue(A1_NAME, null);
			try {
				tx.commit();
				fail("Attribute '" + A1_NAME + "' is mandatory");
			} catch (KnowledgeBaseException ex) {
				// expected
			}
		} finally {
			tx.rollback();
		}
	}

	public void testMandatoryAttribute() throws DataObjectException {
		KnowledgeObject b;
		Transaction tx = begin();
		try {
			b = kb().createKnowledgeObject(B_NAME);
			assertTrue("Mandatory tests is only useful for mandatory attributes.",
				MetaObjectUtils.getAttribute(b.tTable(), A1_NAME).isMandatory());
			setA1(b, "a1");
			tx.commit();
		} finally {
			tx.rollback();
		}

		Transaction changeTX = begin();
		try {
			// set value to null is temporarily possible
			b.setAttributeValue(A1_NAME, null);
			b.setAttributeValue(A1_NAME, "newA1");
			changeTX.commit();
		} finally {
			changeTX.rollback();
		}

		Transaction removeA1TX = begin();
		try {
			b.setAttributeValue(A1_NAME, null);
			try {
				removeA1TX.commit();
				fail("Attribute '" + A1_NAME + "' is mandatory");
			} catch (KnowledgeBaseException ex) {
				// expected
			}
		} finally {
			removeA1TX.rollback();
		}

		Transaction removeTX = begin();
		try {
			b.delete();
		} catch (Exception e) {
			fail("Mandatory attribute must permit deleting of object.", e);
		} finally {
			removeTX.rollback();
		}
	}

	public void testCreatePrimitiveItemFails() {
		Transaction tx = begin();
		try {
			kb().createKnowledgeItem(MOPrimitive.STRING.getName(), KnowledgeObject.class);
			fail("Creating items for primitive types must not be possible.");
		} catch (DataObjectException ex) {
			// expected
		} finally {
			tx.rollback();
		}
	}

	public void testCreateAlternativeItemFails() {
		Transaction tx = begin();
		try {
			kb().createKnowledgeItem(BD_NAME, KnowledgeObject.class);
			fail("Creating items for MOAlternative-types must not be possible.");
		} catch (DataObjectException ex) {
			// expected
		} finally {
			tx.rollback();
		}
	}

	public void testCreateAbstractItemFails() {
		Transaction tx = begin();
		try {
			assertTrue("Test that creating abstract types is not possible is just for abstract types useful.",
				type(A_NAME).isAbstract());
			kb().createKnowledgeItem(A_NAME, KnowledgeObject.class);
			fail("Creating items for abstract types must not be possible.");
		} catch (DataObjectException ex) {
			// expected
		} finally {
			tx.rollback();
		}
	}

	public void testCommitTransactionOnRollbackOfCommittedNestedTransaction() throws InterruptedException,
			DataObjectException {
		Transaction outer = kb().beginTransaction();
		KnowledgeObject b1 = newB("b1");

		Transaction inner = kb().beginTransaction();
		KnowledgeObject b2 = newB("b2");
		inner.commit();
		inner.rollback();

		outer.commit();

		assertTrue(b1.isAlive());
		assertTrue(b2.isAlive());
	}

	public void testFailTransactionOnRollbackEmptyUnommittedNestedTransaction() throws InterruptedException,
			DataObjectException {
		Transaction outer = kb().beginTransaction();
		KnowledgeObject b1 = newB("b1");

		Transaction inner = kb().beginTransaction();
		inner.rollback();

		try {
			outer.commit();
			fail("Outer transaction must roll back on rollback of nested transaction.");
		} catch (KnowledgeBaseException ex) {
			// Expected.
		}

		assertFalse(b1.isAlive());
	}

	public void testFailEmptyTransactionOnRollbackEmptyUnommittedNestedTransaction() throws InterruptedException,
			DataObjectException {
		Transaction outer = kb().beginTransaction();

		Transaction inner = kb().beginTransaction();
		inner.rollback();

		try {
			outer.commit();
			fail("Outer transaction must roll back on rollback of nested transaction.");
		} catch (KnowledgeBaseException ex) {
			// Expected.
		}
	}

	public void testAutoRollback() throws InterruptedException, DataObjectException {
		final java.util.concurrent.Semaphore start = new java.util.concurrent.Semaphore(0);
		final java.util.concurrent.Semaphore finish = new java.util.concurrent.Semaphore(0);
		final BooleanFlag success = new BooleanFlag(false);

		Thread thread = new Thread() {
			@Override
			public void run() {
				ThreadContext.inSystemContext(getClass(), new InContext() {
					@Override
					public void inContext() {
						Transaction tx = kb().beginTransaction();
						assertNotNull(tx);

						CommitContext context = kb().createCommitContext();

						// Start DB transaction.
						long commitNumber = context.getCommitNumber();
						assertTrue(commitNumber > 0);

						// Database is locked. Let main thread continue.
						start.release();

						// No commit or rollback.
					}
				});

				try {
					finish.acquire();
					success.set(true);
				} catch (InterruptedException ex) {
					// Ignore.
				}
			}
		};

		thread.start();

		start.acquire();
		try {
			// Thread should have done an auto-rollback. Try to commit a transaction.
			Transaction tx = kb().beginTransaction();
			newB("b1");
			tx.commit();
		} finally {
			finish.release();
		}

		thread.join();

		assertTrue(success.get());
	}

	/**
	 * Tests {@link KnowledgeItem#getAllAttributeNames()}
	 */
	public void testAllAttributeNames() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject e1 = newE("e1");
		tx.commit();

		Set<?> expected = Mappings.mapIntoSet(MetaObjectUtils.NAME_MAPPING, type(E_NAME).getAttributes());
		assertEquals(expected, e1.getAllAttributeNames());
	}

	/**
	 * Tests {@link ChangeInspectable} on an deleted object that was modified before.
	 */
	public void testJournalModifyDelete() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		ChangeInspectable ci = (ChangeInspectable) e1;
		tx1.commit();

		Transaction tx2 = begin();
		setA1(e1, "new_e1");
		e1.delete();
		ItemChange change = ci.getChange();
		Map<String, Object> values = change.getValues();
		assertEquals("Object is deleted, so change must not be reported.", "e1", values.get(A1_NAME));
		tx2.commit();
	}

	/**
	 * Tests {@link ChangeInspectable} on an deleted object.
	 */
	public void testJournalDelete() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		ChangeInspectable ci = (ChangeInspectable) e1;
		tx1.commit();

		Transaction tx2 = begin();
		e1.delete();
		ItemChange change = ci.getChange();
		Map<String, Object> values = change.getValues();
		assertEquals("e1", values.get(A1_NAME));
		tx2.commit();
	}

	/**
	 * Tests {@link ChangeInspectable} on a new object.
	 */
	public void testJournalCreate() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		ChangeInspectable ci = (ChangeInspectable) e1;
		ItemChange change = ci.getChange();
		assertNotNull(change);
		Map<String, Object> values = change.getValues();
		assertEquals("e1", values.get(A1_NAME));
		tx1.commit();
	}

	/**
	 * Tests {@link ChangeInspectable} on a modified object.
	 */
	public void testJournalModify() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		ChangeInspectable ci = (ChangeInspectable) e1;
		tx1.commit();

		Transaction tx2 = begin();
		e1.setAttributeValue(A1_NAME, "e1_new");
		e1.setAttributeValue(A2_NAME, "e1_a2");
		ItemChange change = ci.getChange();
		assertNotNull(ci);
		assertInstanceof(change, ItemUpdate.class);
		ItemUpdate update = (ItemUpdate) change;
		Map<String, Object> values = update.getValues();
		assertEquals("e1_new", values.get(A1_NAME));
		assertEquals("e1_a2", values.get(A2_NAME));
		Map<String, Object> oldValues = update.getOldValues();
		assertNotNull(oldValues);
		assertEquals("e1", oldValues.get(A1_NAME));
		assertEquals(null, oldValues.get(A2_NAME));
		tx2.commit();
	}

	/**
	 * Tests {@link ChangeInspectable} on an object modified in a different context.
	 */
	public void testJournalModifyInDifferentContext() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		final ChangeInspectable ci = (ChangeInspectable) e1;
		tx1.commit();

		Transaction tx2 = begin();
		e1.setAttributeValue(A1_NAME, "e1_new");
		inThread(new Execution() {

			@Override
			public void run() throws Exception {
				assertNull("Changes must not be seen outside modification context.", ci.getChange());
			}
		});
		tx2.rollback();
	}

	/**
	 * Tests {@link ChangeInspectable} on a unmodified object.
	 */
	public void testJournalUnmodified() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		ChangeInspectable ci = (ChangeInspectable) e1;
		tx1.commit();

		Transaction tx2 = begin();
		assertNull("No attribute changed.", ci.getChange());
		tx2.commit();
	}

	/**
	 * Tests {@link ChangeInspectable} on a persistent object which was changed but the changes were
	 * reverted, such that the item is actually unchanged.
	 */
	public void testJournalModificationReverted() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		ChangeInspectable ci = (ChangeInspectable) e1;
		tx1.commit();

		Transaction tx2 = begin();
		String origValue = (String) e1.getAttributeValue(A1_NAME);

		String newValue = origValue + origValue;
		e1.setAttributeValue(A1_NAME, newValue);
		ItemChange change = ci.getChange();
		assertInstanceof(change, ItemUpdate.class);
		assertEquals(origValue, (((ItemUpdate) change).getOldValues().get(A1_NAME)));
		assertEquals(newValue, (((ItemUpdate) change).getValues().get(A1_NAME)));

		e1.setAttributeValue(A1_NAME, origValue);
		ItemChange revertedChange = ci.getChange();
		assertNull("Change has been reverted. There is actually no update.", revertedChange);

		tx2.commit();
	}

	public void testCreateObject() throws DataObjectException {
		Transaction tx = begin();
		// Type-safe API
		checkBObj(kb().createObject(B_NAME, BObj.class));
		checkBObj(kb().createObject(B_NAME, values(), BObj.class));

		checkBObj(kb().createObject(kb().getTrunk(), B_NAME, BObj.class));
		checkBObj(kb().createObject(kb().getTrunk(), B_NAME, values(), BObj.class));

		// Generic API
		checkBObj(kb().createObject(kb().getTrunk(), B_NAME, values()));
		checkBObj(kb().createObject(kb().getTrunk(), null, B_NAME, values()));
		tx.commit();
	}

	private KeyValueBuffer<String, Object> values() {
		return new KeyValueBuffer<String, Object>(1).put(A1_NAME, "foo");
	}

	public void testCreateItem() throws DataObjectException {
		Transaction tx = begin();
		// Type-safe API
		checkBObjKO(kb().createKnowledgeItem(B_NAME, KnowledgeObject.class));
		checkBObjKO(kb().createKnowledgeItem(B_NAME, values(), KnowledgeObject.class));

		checkBObjKO(kb().createKnowledgeItem(kb().getTrunk(), B_NAME, KnowledgeObject.class));
		checkBObjKO(kb().createKnowledgeItem(kb().getTrunk(), B_NAME, values(), KnowledgeObject.class));
		checkBObjKO(kb().createKnowledgeItem(kb().getTrunk(), null, B_NAME, values(), KnowledgeObject.class));

		// Generic API
		checkBObjKO(kb().createKnowledgeItem(kb().getTrunk(), B_NAME));
		checkBObjKO(kb().createKnowledgeItem(kb().getTrunk(), B_NAME, values()));
		checkBObjKO(kb().createKnowledgeItem(kb().getTrunk(), null, B_NAME, values()));
		tx.commit();
	}

	public void testCreateKO() throws DataObjectException {
		Transaction tx = begin();
		// Legacy API
		checkBObjKO(kb().createKnowledgeObject(B_NAME));
		checkBObjKO(kb().createKnowledgeObject(B_NAME, values()));
		checkBObjKO(kb().createKnowledgeObject(kb().getTrunk(), B_NAME));
		checkBObjKO(kb().createKnowledgeObject(kb().getTrunk(), B_NAME, values()));
		checkBObjKO(kb().createKnowledgeObject(kb().getTrunk(), null, B_NAME, values()));

		// Deprecated API
		checkBObjKO(kb().createKnowledgeObject(kb().getTrunk(), null, B_NAME));
		checkBObjKO(kb().createKnowledgeObject((TLID) null, B_NAME, values()));
		checkBObjKO(kb().createKnowledgeObject((TLID) null, B_NAME));
		tx.commit();
	}

	private void checkBObjKO(Object obj) {
		assertNotNull(obj);
		assertInstanceof(obj, KnowledgeObject.class);

		// Make sure that commit does not fail.
		((AObj) ((KnowledgeObject) obj).getWrapper()).setA1("passed");
	}

	private void checkBObj(Object obj) {
		assertNotNull(obj);
		assertInstanceof(obj, BObj.class);

		// Make sure that commit does not fail.
		((BObj) obj).setA1("passed");
	}

	public void testCreateAssociationWithoutEnds() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		tx1.commit();

		Transaction tx2 = begin();
		Iterable<Entry<String, Object>> initialValues = Collections.emptyList();
		kb().createKnowledgeItem(trunk(), AB_NAME, initialValues, KnowledgeAssociation.class);
		try {
			tx2.commit();
			fail("Associations need source and destination.");
		} catch (DataObjectException ex) {
			// expected
		} finally {
			tx2.rollback();
		}
		Transaction tx3 = begin();
		KeyValueBuffer<String, Object> sourceInitValue =
			new NameValueBuffer().put(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, e1);
		kb().createKnowledgeItem(trunk(), null, AB_NAME, sourceInitValue, KnowledgeAssociation.class);
		try {
			tx3.commit();
			fail("Associations need source and destination.");
		} catch (DataObjectException ex) {
			// expected
		} finally {
			tx3.rollback();
		}
		Transaction tx4 = begin();
		KeyValueBuffer<String, Object> destInitValue =
			new NameValueBuffer().put(DBKnowledgeAssociation.REFERENCE_DEST_NAME, e1);
		kb().createKnowledgeItem(trunk(), null, AB_NAME, destInitValue, KnowledgeAssociation.class);
		try {
			tx4.commit();
			fail("Associations need source and destination.");
		} catch (DataObjectException ex) {
			// expected
		} finally {
			tx4.rollback();
		}
		Transaction tx5 = begin();
		KeyValueBuffer<String, Object> initValue =
			new NameValueBuffer().put(DBKnowledgeAssociation.REFERENCE_DEST_NAME, e1).put(
				DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, e1);
		KnowledgeAssociation result =
			kb().createKnowledgeItem(trunk(), null, AB_NAME, initValue, KnowledgeAssociation.class);
		tx5.commit();
		assertNotNull(result);
	}

	public void testInitialValues() throws DataObjectException {
		MOClass sType = type(S_NAME);
		assertTrue(S1_NAME +" needs to be initial for this test to be useful.", sType.getAttribute(S1_NAME).isInitial());
		assertTrue(S_TYPE_NAME + " needs to be initial for this test to be useful.", sType.getAttribute(S_TYPE_NAME).isInitial());

		Iterable<Entry<String, Object>> emptyInitialValues = Collections.emptyList();
		try {
			kb().createKnowledgeObject(S_NAME, emptyInitialValues);
			fail("Initial attribute '" + S1_NAME + "' not set.");
		} catch (DataObjectException ex) {
			// expected
		}
		Transaction createTx = begin();
		SB newSBObj = SB.newSBObj("s1");
		try {
			newSBObj.setString(S1_NAME, "newS1");
			fail("Initial attribute " + S1_NAME + " must not be set twice.");
		} catch (Exception ex) {
			// expected
		}
		assertEquals("s1", newSBObj.getString(S1_NAME));
		createTx.commit();
		assertEquals("s1", newSBObj.getString(S1_NAME));
		
		Transaction changeTx = begin();
		newSBObj.setString(S1_NAME, "newS1");
		assertEquals("newS1", newSBObj.getString(S1_NAME));
		changeTx.commit();
		assertEquals("newS1", newSBObj.getString(S1_NAME));
	}

	/**
	 * Test that history context of association end keys is consistent with the history context of
	 * the referenced objects.
	 */
	public void testHistoricReferenceKeyLookup() throws DataObjectException {
		KnowledgeObject b1;
		KnowledgeObject b2;
		KnowledgeAssociation link;
		Revision linkDataRevision;
		{
			// Create linked objects.
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			link = newAB(b1, b2);

			commit(tx);

			linkDataRevision = tx.getCommitRevision();
		}

		// Change linked objects.
		Revision lookupRevision;
		{
			Transaction tx = begin();
			setA1(b1, "change1");
			commit(tx);

			lookupRevision = tx.getCommitRevision();
		}
		{
			Transaction tx = begin();
			setA1(b2, "change2");
			commit(tx);
		}

		assertNotEquals(lookupRevision, linkDataRevision);

		KnowledgeAssociation historicLink = (KnowledgeAssociation) HistoryUtils.getKnowledgeItem(lookupRevision, link);
		assertEquals(historicLink.getSourceObject().getHistoryContext(), lookupRevision.getCommitNumber());
		assertEquals(historicLink.getSourceIdentity().getHistoryContext(), lookupRevision.getCommitNumber());
		assertEquals(historicLink.getDestinationObject().getHistoryContext(), lookupRevision.getCommitNumber());
		assertEquals(historicLink.getDestinationIdentity().getHistoryContext(), lookupRevision.getCommitNumber());
	}

	/**
	 * Tests that the {@link CommitContext} of the {@link KnowledgeBase} can be accessed during
	 * rollback.
	 */
	public void testAccessDBContextDuringRollback() throws DataObjectException {
		final AtomicReference<AssertionFailedError> failureHolder = new AtomicReference<>();
		final Committable committable = new CommittableAdapter() {

			@Override
			public boolean rollback(CommitContext context) {
				if (context != kb().getCurrentContext()) {
					// DBContext caches error so it must be thrown outside
					failureHolder.set(new AssertionFailedError(
						"Wrong commitContext: Expected current context of the KnowledgBase is the given one."));
				}
				return super.rollback(context);
			}
		};

		kb().addCommittable(committable);
		Transaction tx = begin();
		kb().createKnowledgeObject(E_NAME);
		tx.rollback();

		AssertionFailedError failure = failureHolder.get();
		if (failure != null) {
			throw failure;
		}
	}

	/**
	 * Tests that searching by attribute(s) also works with reference attributes.
	 */
	public void testSearchObjectsbyReferenceAttribute() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject e2 = newE("e2");
		e1.setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, e1);

		DataObject objectByAttributeInTx = kb().getObjectByAttribute(E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME, e1);
		assertSame(e1, objectByAttributeInTx);
		commit(tx1);
		DataObject objectByAttribute = kb().getObjectByAttribute(E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME, e1);
		assertSame(e1, objectByAttribute);

		Transaction tx2 = begin();
		KnowledgeObject e3 = newE("e3");
		e3.setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, e2);
		e2.setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, e1);

		Set<KnowledgeItem> objectsByAttributeInTx =
			toSet(kb().getObjectsByAttribute(E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME, e1));
		assertEquals(set(e1, e2), objectsByAttributeInTx);
		commit(tx2);
		Set<KnowledgeItem> objectsByAttribute =
			toSet(kb().getObjectsByAttribute(E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME, e1));
		assertEquals(set(e1, e2), objectsByAttribute);

		String value = "value";
		Transaction tx3 = begin();
		e3.setAttributeValue(A2_NAME, value);
		e2.setAttributeValue(A2_NAME, value);

		Set objectsByAttributesInTx = toSet(kb().getObjectsByAttributes(trunk(), Revision.CURRENT, E_NAME,
			new String[] { REFERENCE_POLY_CUR_LOCAL_NAME, A2_NAME }, new Object[] { e1, value }));
		assertEquals(Collections.singleton(e2), objectsByAttributesInTx);
		commit(tx3);
		Set objectsByAttributes = toSet(kb().getObjectsByAttributes(trunk(), Revision.CURRENT, E_NAME,
			new String[] { REFERENCE_POLY_CUR_LOCAL_NAME, A2_NAME }, new Object[] { e1, value }));
		assertEquals(Collections.singleton(e2), objectsByAttributes);
	}

	/**
	 * Tests that current object is found after an failed deletion of an historic object.
	 */
	public void testSearchCurrentAfterFailedHistoricObjectDeletion() throws DataObjectException {
		Transaction createTx = kb().beginTransaction();
		KnowledgeObject e1 = newE("e1");
		commit(createTx);
		KnowledgeItem historicE1 = HistoryUtils.getKnowledgeItem(createTx.getCommitRevision(), e1);
		try {
			Transaction deleteTx = kb().beginTransaction();
			historicE1.delete();
			deleteTx.commit();
		} catch (IllegalStateException ex) {
			// expected
		}

		Collection<KnowledgeObject> allKnownEs = kb().getAllKnowledgeObjects(E_NAME);
		assertTrue("Not deleted e1 not found.", allKnownEs.contains(e1));
	}

	/**
	 * Tests that object can be modified after an failed deletion of an historic object.
	 */
	public void testChangeCurrentAfterFailedHistoricObjectDeletion() throws DataObjectException {
		Transaction createTx = kb().beginTransaction();
		KnowledgeObject e1 = newE("e1");
		commit(createTx);
		KnowledgeItem historicE1 = HistoryUtils.getKnowledgeItem(createTx.getCommitRevision(), e1);
		try {
			Transaction deleteTx = kb().beginTransaction();
			historicE1.delete();
			deleteTx.commit();
		} catch (IllegalStateException ex) {
			// expected
		}
		Transaction changeTx = kb().beginTransaction();
		e1.setAttributeValue(A1_NAME, "new A1 value");
		commit(changeTx);
		assertEquals("new A1 value", e1.getAttributeValue(A1_NAME));
	}

	/**
	 * Tests that historic objects which were changed after its revision can not be deleted.
	 */
	public void testDeleteHistoricObject1() throws DataObjectException {
		Transaction createTx = kb().beginTransaction();
		KnowledgeObject e1 = newE("e1");
		commit(createTx);
		Transaction changeTx = kb().beginTransaction();
		e1.setAttributeValue(A1_NAME, "new a1 value");
		commit(changeTx);

		try {
			KnowledgeItem historicE1 = HistoryUtils.getKnowledgeItem(createTx.getCommitRevision(), e1);
			Transaction deleteTx = kb().beginTransaction();
			historicE1.delete();
			deleteTx.commit();
			fail("Historic objects '" + historicE1 + "' can not be deleted");
		} catch (IllegalStateException ex) {
			// expected
		}
		assertTrue("Historic object deletions must not kill current objects.", e1.isAlive());
	}

	/**
	 * Tests that historic objects can not be deleted.
	 */
	public void testDeleteHistoricObject() throws DataObjectException {
		Transaction createTx = kb().beginTransaction();
		KnowledgeObject e1 = newE("e1");
		commit(createTx);
		KnowledgeItem historicE1 = HistoryUtils.getKnowledgeItem(createTx.getCommitRevision(), e1);
		try {
			Transaction deleteTx = kb().beginTransaction();
			historicE1.delete();
			deleteTx.commit();
			fail("Historic objects '" + historicE1 + "' can not be deleted");
		} catch (IllegalStateException ex) {
			// expected
		}
		assertTrue("Historic object deletions must not kill current objects.", e1.isAlive());
		assertTrue("Historic object are always alive.", historicE1.isAlive());
	}
	
	/**
	 * Tests that historic associations can not be deleted.
	 */
	public void testDeleteHistoricAssociation() throws DataObjectException {
		Transaction createTx = kb().beginTransaction();
		KnowledgeObject e1 = newE("e1");
		KnowledgeAssociation association = newAB(e1, e1);
		commit(createTx);
		KnowledgeItem historicAssociation = HistoryUtils.getKnowledgeItem(createTx.getCommitRevision(), association);
		try {
			Transaction deleteTx = kb().beginTransaction();
			historicAssociation.delete();
			deleteTx.commit();
			fail("Historic objects '" + historicAssociation + "' can not be deleted");
		} catch (IllegalStateException ex) {
			// expected
		}
		assertTrue("Historic object deletions must not kill current objects.", association.isAlive());
		assertTrue("Historic object are always alive.", historicAssociation.isAlive());
	}

	public void testGetIncomingAssociationsHistoric() throws DataObjectException {
		Transaction tx = kb().beginTransaction();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject e2 = newE("e2");
		KnowledgeObject b1 = newB("b1");
		KnowledgeAssociation ab1 = newAB(e1, e2);
		KnowledgeAssociation ab2 = newAB(e2, e1);
		KnowledgeAssociation bc1 = newBC(b1, e1);
		commit(tx);
		KnowledgeObject oldE1 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(lastRevision(), e1);
		KnowledgeObject oldE2 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(lastRevision(), e2);
		KnowledgeObject oldB1 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(lastRevision(), b1);
		KnowledgeItem oldAB1 = HistoryUtils.getKnowledgeItem(lastRevision(), ab1);
		KnowledgeItem oldAB2 = HistoryUtils.getKnowledgeItem(lastRevision(), ab2);
		KnowledgeItem oldBC1 = HistoryUtils.getKnowledgeItem(lastRevision(), bc1);

		assertEquals(set(oldAB2, oldBC1), toSet(oldE1.getIncomingAssociations()));
		assertEquals(set(oldAB2), toSet(oldE1.getIncomingAssociations(AB_NAME)));
		assertEquals(set(oldBC1), toSet(oldE1.getIncomingAssociations(BC_NAME)));
		assertEquals(set(), toSet(oldB1.getIncomingAssociations()));
		assertEquals(set(oldAB1), toSet(oldE2.getIncomingAssociations()));
	}

	public void testGetOutgoingAssociations() throws DataObjectException {
		Transaction tx = kb().beginTransaction();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject e2 = newE("e2");
		KnowledgeObject b1 = newB("b1");
		KnowledgeAssociation ab1 = newAB(e1, e2);
		KnowledgeAssociation ab2 = newAB(e2, e1);
		KnowledgeAssociation bc1 = newBC(b1, e1);
		commit(tx);

		assertEquals(set(ab1), toSet(e1.getOutgoingAssociations()));
		assertEquals(set(ab1), toSet(e1.getOutgoingAssociations(AB_NAME)));
		assertEquals(set(), toSet(e1.getOutgoingAssociations(BC_NAME)));
		assertEquals(set(bc1), toSet(b1.getOutgoingAssociations()));
		assertEquals(set(ab2), toSet(e2.getOutgoingAssociations()));

		Transaction deleteTx = kb().beginTransaction();
		ab1.delete();
		assertEquals(set(), toSet(e1.getOutgoingAssociations()));
		assertEquals(set(), toSet(e1.getOutgoingAssociations(AB_NAME)));
		assertEquals(set(), toSet(e1.getOutgoingAssociations(BC_NAME)));
		assertEquals(set(bc1), toSet(b1.getOutgoingAssociations()));
		assertEquals(set(ab2), toSet(e2.getOutgoingAssociations()));

		rollback(deleteTx);
	}

	public void testGetIncomingAssociations() throws DataObjectException {
		Transaction tx = kb().beginTransaction();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject e2 = newE("e2");
		KnowledgeObject b1 = newB("b1");
		KnowledgeAssociation ab1 = newAB(e1, e2);
		KnowledgeAssociation ab2 = newAB(e2, e1);
		KnowledgeAssociation bc1 = newBC(b1, e1);
		commit(tx);

		assertEquals(set(ab2, bc1), toSet(e1.getIncomingAssociations()));
		assertEquals(set(ab2), toSet(e1.getIncomingAssociations(AB_NAME)));
		assertEquals(set(bc1), toSet(e1.getIncomingAssociations(BC_NAME)));
		assertEquals(set(), toSet(b1.getIncomingAssociations()));
		assertEquals(set(ab1), toSet(e2.getIncomingAssociations()));

		Transaction deleteTx = kb().beginTransaction();
		ab2.delete();
		assertEquals(set(bc1), toSet(e1.getIncomingAssociations()));
		assertEquals(set(), toSet(e1.getIncomingAssociations(AB_NAME)));
		assertEquals(set(bc1), toSet(e1.getIncomingAssociations(BC_NAME)));
		assertEquals(set(), toSet(b1.getIncomingAssociations()));
		assertEquals(set(ab1), toSet(e2.getIncomingAssociations()));

		rollback(deleteTx);
	}

	public void testGetAllKnowledgeObjectsOnBranch() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Transaction tx = kb().beginTransaction();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject e2 = newE("e1");
		KnowledgeObject b1 = newB("b1");
		commit(tx);

		Branch branch = kb().createBranch(trunk(), lastRevision(), set(type(E_NAME), type(D_NAME)));
		KnowledgeItem b1OnBranch = HistoryUtils.getKnowledgeItem(branch, b1);
		KnowledgeItem e1OnBranch = HistoryUtils.getKnowledgeItem(branch, e1);
		KnowledgeItem e2OnBranch = HistoryUtils.getKnowledgeItem(branch, e2);

		Collection<KnowledgeObject> allKnowledgeObjectsOnTrunk = kb().getAllKnowledgeObjects();
		assertEquals(set(e1, e2, b1), toSet(allKnowledgeObjectsOnTrunk));

		Branch oldCtxBranch = HistoryUtils.setContextBranch(branch);
		try {
			Collection<KnowledgeObject> allKnowledgeObjectsOnBranch = kb().getAllKnowledgeObjects();
			assertEquals(set(e1OnBranch, e2OnBranch, b1OnBranch), toSet(allKnowledgeObjectsOnBranch));
		} finally {
			HistoryUtils.setContextBranch(oldCtxBranch);
		}

	}

	public void testGetAllAssociations() throws DataObjectException {
		testGetAllAssociations(false);
	}

	public void testGetAllAssociationsInTransaction() throws DataObjectException {
		testGetAllAssociations(true);
	}

	private void testGetAllAssociations(boolean inTx) throws DataObjectException {
		Transaction tx = kb().beginTransaction();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject e2 = newE("e2");
		KnowledgeObject b1 = newB("b1");
		KnowledgeObject c1 = newC("c1");
		KnowledgeAssociation ab1 = newAB(e1, e2);
		KnowledgeAssociation ab2 = newAB(b1, e1);
		KnowledgeAssociation bc1 = newBC(b1, c1);
		if (!inTx) {
			commit(tx);
		}

		{
			Collection<KnowledgeAssociation> allAssociations = kb().getAllKnowledgeAssociations();
			assertEquals(set(ab1, ab2, bc1), toSet(allAssociations));

			Collection<KnowledgeAssociation> allAB = kb().getAllKnowledgeAssociations(AB_NAME);
			assertEquals(set(ab1, ab2), toSet(allAB));

			Collection<KnowledgeAssociation> allBC = kb().getAllKnowledgeAssociations(BC_NAME);
			assertEquals(set(bc1), toSet(allBC));
		}
		{

			Transaction deleteTx = null;
			if (!inTx) {
				deleteTx = kb().beginTransaction();
			}
			e2.delete();
			bc1.delete();
			if (!inTx) {
				commit(deleteTx);
			}

			Collection<KnowledgeAssociation> allAssociations = kb().getAllKnowledgeAssociations();
			assertEquals(set(ab2), toSet(allAssociations));

			Collection<KnowledgeAssociation> allAB = kb().getAllKnowledgeAssociations(AB_NAME);
			assertEquals(set(ab2), toSet(allAB));

			Collection<KnowledgeAssociation> allBC = kb().getAllKnowledgeAssociations(BC_NAME);
			assertEquals(set(), toSet(allBC));
		}
		if (inTx) {
			rollback(tx);
		}
	}

	public void testGetAllKnowledgObjects() throws DataObjectException {
		testGetAllKnowledgeObjects(false);
	}

	public void testGetAllKnowledgObjectsInTransaction() throws DataObjectException {
		testGetAllKnowledgeObjects(true);
	}

	private void testGetAllKnowledgeObjects(boolean inTx) throws DataObjectException {
		Transaction tx = kb().beginTransaction();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject e2 = newE("e1");
		KnowledgeObject b1 = newB("b1");
		KnowledgeObject u1 = newU("u1");
		if (!inTx) {
			commit(tx);
		}

		{
			Collection<KnowledgeObject> allKnowledgeObjects = kb().getAllKnowledgeObjects();
			assertEquals(set(e1, e2, u1, b1), toSet(allKnowledgeObjects));

			Collection<KnowledgeObject> allE = kb().getAllKnowledgeObjects(E_NAME);
			assertEquals(set(e1, e2), toSet(allE));

			Collection<KnowledgeObject> allU = kb().getAllKnowledgeObjects(U_NAME);
			assertEquals(set(u1), toSet(allU));
		}
		{
			Transaction deleteTx = kb().beginTransaction();
			e2.delete();
			b1.delete();
			commit(deleteTx);

			Collection<KnowledgeObject> allKnowledgeObjects = kb().getAllKnowledgeObjects();
			assertEquals(set(e1, u1), toSet(allKnowledgeObjects));

			Collection<KnowledgeObject> allE = kb().getAllKnowledgeObjects(E_NAME);
			assertEquals(set(e1), toSet(allE));

			Collection<KnowledgeObject> allB = kb().getAllKnowledgeObjects(B_NAME);
			assertEquals(set(), toSet(allB));
		}
		if (inTx) {
			rollback(tx);
		}
	}

	/**
	 * Tests that creating an association failed if an end was deleted concurrently.
	 */
	public void testConcurrentEndDeletion() throws DataObjectException, InterruptedException {
		testCreatingDanglingPointer(true);
	}

	/**
	 * Tests that deleting an object fails if an association with such end was created concurrently.
	 */
	public void testConcurrentAssociationCreation() throws DataObjectException, InterruptedException {
		testCreatingDanglingPointer(false);
	}

	/**
	 * Tests
	 * <ol>
	 * <li>
	 * that creating an association failed if an end was deleted concurrently or</li>
	 * <li>
	 * that deleting an object fails if an association with such end was created concurrently.</li>
	 * </ol>
	 * 
	 * @param concurrentDeletion
	 *        if <code>true</code> then case 1. is tested
	 */
	private void testCreatingDanglingPointer(final boolean concurrentDeletion) throws DataObjectException,
			InterruptedException {
		final KnowledgeObject b1;
		final KnowledgeObject b2;
		{
			// Create objects on the primary node.
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			commit(tx);
		}
		final boolean[] lock = new boolean[] { concurrentDeletion };
		final TestDBKnowledgeBase outer = this;

		// used to ensure that both transaction started
		final Barrier barrier = createBarrier(2);
		final AtomicReference<AssertionFailedError> problem = new AtomicReference<>();

		class Creation implements Execution {

			@Override
			public void run() throws Exception {
				Transaction tx = outer.begin();
				outer.newAB(b1, b2);
				barrier.enter(0);
				synchronized (lock) {
					while (lock[0]) {
						lock.wait();
					}
					lock[0] = !lock[0];
					lock.notifyAll();
					if (!concurrentDeletion) {
						outer.commit(tx);
					} else {
						String error =
							"Ticket #7220: Expected commit failed as an end of association was deleted concurrently.";
						commit(tx, error, true);
					}
				}
			}
		}

		class Deletion implements Execution {

			@Override
			public void run() throws Exception {
				Transaction tx = outer.begin();
				b1.delete();
				barrier.enter(0);
				synchronized (lock) {
					while (!lock[0]) {
						lock.wait();
					}
					lock[0] = !lock[0];
					lock.notifyAll();
					if (concurrentDeletion) {
						outer.commit(tx);
					} else {
						String error =
							"Ticket #7220: Expected commit failed as an association whith that end was created concurrently.";
						commit(tx, error, true);
					}
				}
			}
		}

		parallelTest(2, new ExecutionFactory() {

			@Override
			public Execution createExecution(int id) {
				switch (id) {
					case 0:
						return new Deletion();
					case 1:
						return new Creation();
					default:
						throw new IllegalArgumentException();
				}
			}
		});

		if (problem.get() != null) {
			throw problem.get();
		}
	}

	public void testChangeHistoricValue() throws DataObjectException {
		Revision createRev;
		KnowledgeObject b1;
		{
			Transaction createTx = begin();
			b1 = newB("b1");
			createTx.commit();

			createRev = createTx.getCommitRevision();
		}

		Revision trafficRev;
		{
			Transaction trafficTx = begin();
			newB("b12");
			trafficTx.commit();

			trafficRev = trafficTx.getCommitRevision();
		}

		Revision changeRev;
		{
			Transaction changeTx = begin();
			setA1(b1, "b2");
			changeTx.commit();

			changeRev = changeTx.getCommitRevision();
		}

		{
			/* check change of historic object that was changed in its revision can not be
			 * persistent */
			KnowledgeItem historicB = HistoryUtils.getKnowledgeItem(createRev, b1);

			assertEquals("b1", historicB.getAttributeValue(A1_NAME));
			try {
				Transaction historicChangeTX = begin();
				setA1(historicB, "b3");
				historicChangeTX.commit();
				assertEquals("Historic value changed", "b1", historicB.getAttributeValue(A1_NAME));
				fail("Setting values to historic objects must fail.");
			} catch (Exception ex) {
				// expected
			}
		}
		{
			/* check change of historic object that was not changed in its revision can not be
			 * persistent */
			KnowledgeItem historicB = HistoryUtils.getKnowledgeItem(trafficRev, b1);

			assertEquals("b1", historicB.getAttributeValue(A1_NAME));
			try {
				Transaction historicChangeTX = begin();
				setA1(historicB, "b3");
				historicChangeTX.commit();
				assertEquals("Historic value changed", "b1", historicB.getAttributeValue(A1_NAME));
				fail("Setting values to historic objects must fail.");
			} catch (Exception ex) {
				// expected
			}
		}
		{
			/* Check change of transient changes in historic objects */
			KnowledgeItem historicB = HistoryUtils.getKnowledgeItem(trafficRev, b1);
			assertEquals("b1", historicB.getAttributeValue(A1_NAME));
			try {
				setA1(historicB, "b3");
				assertEquals("Historic value changed", "b1", historicB.getAttributeValue(A1_NAME));
				fail("Setting values to historic objects must fail.");
			} catch (Exception ex) {
				// expected
			}
		}
	}

	/**
	 * Test case for Ticket #5784: toString() von <code>KnowledgeItemView</code> zeigt den falschen
	 * History-Context an.
	 */
	public void testToString() throws DataObjectException {
		Revision r1;
		KnowledgeObject b1;
		Transaction tx = begin();
		{
			b1 = newB("b1");
			tx.commit();

			r1 = tx.getCommitRevision();
		}

		Revision r2;
		Transaction tx2 = begin();
		{
			newB("other");
			tx2.commit();
			r2 = tx2.getCommitRevision();
		}

		Transaction tx3 = begin();
		{
			setA1(b1, "new value");
			tx3.commit();
		}

		KnowledgeItem b1r1 = HistoryUtils.getKnowledgeItem(r1, b1);
		KnowledgeItem b1r2 = HistoryUtils.getKnowledgeItem(r2, b1);

		assertNotEquals(b1.toString(), b1r1.toString());
		assertNotEquals(b1r1.toString(), b1r2.toString());
	}

	/**
	 * Tests that resolving already deleted or not yet created objects results in <code>null</code>.
	 */
	public void testAccessNotExististingObjects() throws DataObjectException {
		final HistoryManager historyManager = kb().getHistoryManager();

		Transaction irrelevantTx = begin();
		newB("irrelevant");
		irrelevantTx.commit();
		final Revision b1DoesNotExists = irrelevantTx.getCommitRevision();

		Transaction createTx = begin();
		final KnowledgeObject b1 = newB("b1_name");
		createTx.commit();

		// tests access to revision before b1 creation
		final KnowledgeItem notYetExistingObject =
			historyManager.getKnowledgeItem(b1DoesNotExists, b1);
		assertNull(notYetExistingObject);

		Transaction delTx = begin();
		b1.delete();
		delTx.commit();

		Transaction irrelevantTx2 = begin();
		newB("irrelevant");
		irrelevantTx2.commit();

		final Revision lastRevision = HistoryUtils.getLastRevision(historyManager);
		// tests access to revision after b1 deletion
		final KnowledgeItem deletedObject =
			historyManager.getKnowledgeItem(lastRevision, b1);
		assertNull(deletedObject);

	}

	public void testObjectKeySyntax() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Transaction tx = begin();
		final KnowledgeObject b1 = newB("b1");
		tx.commit();

		Branch branch = kb().createBranch(kb().getTrunk(), tx.getCommitRevision(), null);

		ObjectKey key = b1.tId();
		ObjectKey parsedKey = toStringFromStringKey(key);

		assertEquals(b1, kb().resolveObjectKey(parsedKey));

		ObjectKey branchKey =
			new DefaultObjectKey(branch.getBranchId(), kb().getLastRevision(), key.getObjectType(),
				key.getObjectName());

		KnowledgeItem b1OnBranch = kb().resolveObjectKey(branchKey);
		assertNotNull(b1OnBranch);

		ObjectKey parsedBranchKey = toStringFromStringKey(branchKey);

		// Test of old format.
		ObjectKey oldBranchKey = ObjectKey.fromStringObjectKey(kb().getMORepository(),
			branchKey.getObjectType().getName() +
				":" + "ID(" + IdentifierUtil.toExternalForm(branchKey.getObjectName()) + ")" +
				"#" + branchKey.getBranchContext() +
				"-" + branchKey.getHistoryContext());
		assertEquals(parsedBranchKey, oldBranchKey);

		assertEquals(b1OnBranch, kb().resolveObjectKey(parsedBranchKey));
	}

	private ObjectKey toStringFromStringKey(ObjectKey key) throws UnknownTypeException {
		String keyText = key.asString();
		ObjectKey parsedKey = ObjectKey.fromStringObjectKey(kb().getMORepository(), keyText);
		return parsedKey;
	}

	/**
	 * Tests that resolving an Object for an {@link ObjectKey} with future history context, fails.
	 */
	public void testAccessToFutureObjects() throws DataObjectException {
		Transaction tx = begin();
		final KnowledgeObject b1 = newB("b1_name");
		tx.commit();

		final Revision commitRevision;
		{
			Transaction irrelevantTx = begin();
			newB("irrelevant");
			irrelevantTx.commit();
			commitRevision = irrelevantTx.getCommitRevision();
		}

		final ObjectKey currentObjectKey = b1.tId();
		final long futureRevision = commitRevision.getCommitNumber() + 1;
		DefaultObjectKey futureKey =
			new DefaultObjectKey(currentObjectKey.getBranchContext(), futureRevision, currentObjectKey.getObjectType(),
				currentObjectKey.getObjectName());
		try {

			kb().resolveObjectKey(futureKey);
			fail("Ticket #4808: Must not be able to resolve future objects");
		} catch (RuntimeException ex) {
			// expected
		}
	}

	public void testHistoricValue() throws DataObjectException {
		Transaction tx = begin();
		final KnowledgeObject b1 = newB("b1_name");
		tx.commit();

		final Transaction changeTx = begin();
		setA2(b1, "a2");
		changeTx.commit();

		final KnowledgeItem b1OnChangeDate = HistoryUtils.getKnowledgeItem(changeTx.getCommitRevision(), b1);
		assertEquals("Object have the value set in that revision", "a2", b1OnChangeDate.getAttributeValue(A2_NAME));

	}
	public void testSearchNull() throws DataObjectException {
		Transaction tx = begin();
		final KnowledgeObject b1 = newB("b1_name");
		setB1(b1, "b1");
		setB2(b1, "b2");
		setA2(b1, null);
		final KnowledgeObject b2 = newB("b2_name");
		setB1(b2, null);
		setB2(b2, "null");
		setA2(b2, null);
		tx.commit();

		final Iterator<KnowledgeItem> b1IsNull = kb().getObjectsByAttribute(B_NAME, B1_NAME, null);
		assertTrue(b1IsNull.hasNext());
		assertEquals(b2, b1IsNull.next());
		assertFalse(b1IsNull.hasNext());

		final Iterator<KnowledgeItem> a2IsNull = kb().getObjectsByAttribute(B_NAME, A2_NAME, null);
		assertEquals(set(b1, b2), toSet(a2IsNull));

		final Iterator<KnowledgeItem> b2IsNull = kb().getObjectsByAttribute(B_NAME, B2_NAME, null);
		assertFalse(b2IsNull.hasNext());
	}

	/**
	 * Test that {@link KnowledgeBase} performs a DB commit, if the commit
	 * connection was handed out to the application.
	 *
	 * <p>
	 * Note: Test of deprecated API
	 * </p>
	 */
	@SuppressWarnings("deprecation")
	public void testEmptyCommit1() {
		long r1 = kb().getLastRevision();
		
		kb().createCommitContext().getConnection();
		kb().commit();
		
		long r2 = kb().getLastRevision();
		
		assertNotEquals("DB commit must be done, because connection was published.", r1, r2);
	}

	/**
	 * Test that {@link KnowledgeBase} performs a DB commit, if the commit
	 * revision number was requested.
	 *
	 * <p>
	 * Note: Test of deprecated API
	 * </p>
	 */
	@SuppressWarnings("deprecation")
	public void testEmptyCommit2() {
		long r1 = kb().getLastRevision();
		
		long r2Number = kb().createCommitContext().getCommitNumber();
		kb().commit();
		
		long r2 = kb().getLastRevision();
		
		assertNotEquals("DB commit must be done, because connection was published.", r1, r2);
		assertEquals(r2Number, r2);
	}
	
	/**
	 * Test that {@link KnowledgeBase} performs a DB commit, if the commit
	 * connection was handed out to the application.
	 */
	public void testEmptyCommit3() {
		long r1 = kb().getLastRevision();
		
		{
			Transaction tx = begin();
			kb().createCommitContext().getConnection();
			commit(tx);
		}
		
		long r2 = kb().getLastRevision();
		
		assertNotEquals("DB commit must be done, because connection was published.", r1, r2);
	}
	
	/**
	 * Test that {@link KnowledgeBase} performs a DB commit, if the commit
	 * revision number was requested.
	 */
	public void testEmptyCommit4() {
		long r1 = kb().getLastRevision();
		
		long r2Number;
		{
			Transaction tx = begin();
			r2Number = kb().createCommitContext().getCommitNumber();
			commit(tx);
		}
		
		long r2 = kb().getLastRevision();
		
		assertNotEquals("DB commit must be done, because connection was published.", r1, r2);
		assertEquals(r2Number, r2);
	}

	/**
	 * Test that no revision is created, if no changes are performed.
	 */
	public void testNoEmptyCommit() {
		long r1 = kb().getLastRevision();
		
		{
			Transaction tx = begin();
			commit(tx);
		}
		
		long r2 = kb().getLastRevision();
		
		assertEquals("No DB commit must be done, if its sure that no changes are performed.", r1, r2);
	}
	
	/**
	 * <p>
	 * Note: Test of deprecated API
	 * </p>
	 */
	@SuppressWarnings("deprecation")
	public void testEmptyAnonymousCommit() {
		kb().commit();
	}

	/**
	 * <p>
	 * Note: Test of deprecated API
	 * </p>
	 */
	@SuppressWarnings("deprecation")
	public void testEmptyAnonymousRollback() {
		kb().rollback();
	}

	/**
	 * <p>
	 * Note: Test of deprecated API
	 * </p>
	 */
	@SuppressWarnings("deprecation")
	public void testAnonymousRollback2() throws DataObjectException {
		kb().begin();
		Object ctx = KBTestUtils.switchThreadContext(kb(), null);
		KBTestUtils.switchThreadContext(kb(), ctx);
		newB("b1");
		kb().rollback();
		KBTestUtils.switchThreadContext(kb(), ctx);
		kb().rollback();
	}
	
	public void testExplicitRollback2() throws DataObjectException {
		Transaction tx = begin();
		newB("b1");
		tx.rollback();
		tx.rollback();
	}
	
	/**
	 * Test allocating an object with an existing identifier that was created in
	 * the same transaction.
	 */
	public void testPreventDuplicateCreate1() throws DataObjectException {
		{
			Transaction tx = begin();
			try {
				KnowledgeObject b1 = kb().createKnowledgeObject(B_NAME);
				setA1(b1, "b1");
				
				try {
					kb().createKnowledgeObject(b1.getObjectName(), B_NAME);
					fail("Ticket #2764 : Create with existing identifier must be prevented.");
				} catch (IllegalArgumentException ex) {
					// Expected.
				}
			} finally {
				rollback(tx);
			}
		}
	}

	/**
	 * Test allocating an object with an existing identifier that was created in
	 * a previous transaction.
	 */
	public void testPreventDuplicateCreate2() throws DataObjectException {
		KnowledgeObject b1;
		{
			Transaction tx = begin();
			
			b1 = kb().createKnowledgeObject(B_NAME);
			setA1(b1, "b1");
			
			commit(tx);
		}
		
		{
			Transaction tx = begin();
		
			try {
				kb().createKnowledgeObject(b1.getObjectName(), B_NAME);
				fail("Ticket #2764 : Create with existing identifier must be prevented.");
			} catch (IllegalArgumentException ex) {
				// Expected.
			} finally {
				rollback(tx);
			}
		}
	}
	
	/**
	 * Test nesting of explicit transactions.
	 */
	public void testTransactionNesting1() throws DataObjectException {
		long r1 = kb().getLastRevision();
		
		final KnowledgeObject b1;
		{
			Transaction tx1 = begin();
			b1 = newB("b1");

			// Ensure, that all inner commits are captured by outer
			// transaction.
			{
				Transaction tx2 = begin();
				newB("b2");
				
				commit(tx2);
				
				assertTrue(tx2.isNested());
				assertNull(tx2.getCommitRevision());
			}

			{
				Transaction tx3 = begin();
				newB("b3");
				
				{
					Transaction tx4 = begin();
					newB("b4");
					
					commit(tx4);
					assertTrue(tx4.isNested());
					assertNull(tx4.getCommitRevision());
				}
				
				commit(tx3);
				assertTrue(tx3.isNested());
				assertNull(tx3.getCommitRevision());
			}
			
			commit(tx1);
			assertFalse(tx1.isNested());
			assertEquals("Ticket #2798: Explicit transaction did not capture anonymous commits.", r1 + 1, tx1
				.getCommitRevision().getCommitNumber());
		}
		
		// Make sure, object was committed.
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				assertTrue(b1.isAlive());
			}
		});
	}
	
	/**
	 * Test nesting of anonymous legacy transactions within explicit transactions.
	 * 
	 * <p>
	 * Note: Test of deprecated API
	 * </p>
	 */
	@SuppressWarnings("deprecation")
	public void testTransactionNesting2() throws DataObjectException {
		long r1 = kb().getLastRevision();
		
		final KnowledgeObject b1;
		{
			Transaction tx1 = begin();
			b1 = newB("b1");

			// Ensure, that all anonymous inner commits are captured by explicit
			// transaction. 
			kb().commit();
			
			kb().begin();
			kb().commit();
			
			kb().commit();
			
			commit(tx1);
			
			assertEquals("Ticket #2798: Explicit transaction did not capture anonymous commits.", r1 + 1, tx1
				.getCommitRevision().getCommitNumber());
		}

		// Make sure, object was committed.
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				assertTrue(b1.isAlive());
			}
		});
	}
	
	/**
	 * Test nesting explicit transactions.
	 */
	public void testRollbackInner1() {
		long r1 = kb().getLastRevision();
		{
			Transaction tx1 = begin();

			{
				Transaction tx2 = begin();
				rollback(tx2);
			}

			commit(tx1, true);
		}
		assertEquals(r1, kb().getLastRevision());
	}
	
	/**
	 * Test nesting explicit transaction into legacy anonymous transactions.
	 * 
	 * <p>
	 * Note: Test of deprecated API
	 * </p>
	 */
	@SuppressWarnings("deprecation")
	public void testRollbackInner2() {
		long r1 = kb().getLastRevision();
		{
			kb().begin();
			
			{
				Transaction tx2 = begin();
				rollback(tx2);
			}
			
			assertFalse(kb().commit());
		}
		assertEquals(r1, kb().getLastRevision());
	}
	
	/**
	 * Test that update events are sent for {@link KnowledgeObject#touch() touched} objects.
	 */
	public void testEventForLockedObject() throws DataObjectException {
		final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			commit(tx);
		}
		
		final BooleanFlag reached = new BooleanFlag(false);
		kb().addUpdateListener(new UpdateListener() {
			@Override
			public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
				assertTrue(event.getUpdatedObjectKeys().contains(b1.tId()));
				reached.set(true);
			}
		});
		
		{
			Transaction tx = begin();
			b1.touch();
			commit(tx);
		}
		
		assertTrue(reached.get());
	}
	
	
	/**
	 * Test an internal branch query in an open transaction.
	 */
	public void testBranchSearchWithUncommittedChanges() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		long r1 = kb().getLastRevision();
		Branch branch1 = kb().createBranch(kb().getTrunk(), HistoryUtils.getRevision(r1), null);
		Branch branch2 = kb().createBranch(kb().getTrunk(), HistoryUtils.getRevision(r1), null);
		Branch branch3 = kb().createBranch(kb().getTrunk(), HistoryUtils.getRevision(r1), null);
		
		{
			Transaction tx = begin();
			newB("b1");
			
			List<Branch> branches = kb().getTrunk().getChildBranches();
			assertEquals("Ticket #1379: Search for branches must work in open transaction.",
				set(branch1, branch2, branch3), toSet(branches));
			
			rollback(tx);
		}
	}
	
	public void testUnversionedLinks() throws DataObjectException {
		final KnowledgeObject b1;
		final KnowledgeObject b2;
		final KnowledgeObject u1;
		final KnowledgeObject u2;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			u1 = newU("u1");
			u2 = newU("u2");
			tx.commit();
		}

		{
			Transaction tx = begin();

			newAB(b1, b2);

			newUA(b1, b2);
			newUA(u1, b2);
			newUA(b1, u2);
			newUA(u1, u2);

			try {
				newAB(u1, b2);
				fail("Must not allow versioned association between versioned and unversioned object.");
			} catch (IncompatibleTypeException ex) {
				// expected.
			}
			try {
				newAB(b1, u2);
				fail("Must not allow versioned association between versioned and unversioned object.");
			} catch (IncompatibleTypeException ex) {
				// expected.
			}
			try {
				newAB(u1, u2);
				fail("Must not allow versioned association between versioned and unversioned object.");
			} catch (IncompatibleTypeException ex) {
				// expected.
			}

			tx.commit();
		}
	}

	/**
	 * Test access to unversioned objects in stable revisions.
	 */
	public void testUnversionedHistoricView() throws DataObjectException {
		final KnowledgeObject u1;
		final KnowledgeObject u2;
		final KnowledgeAssociation u1u2;
		final long r1;
		{
			Transaction tx = begin();
			u1 = newU("u1");
			u2 = newU("u2");
			u1u2 = newUA(u1, u2);
			commit(tx);
			r1 = tx.getCommitRevision().getCommitNumber();
		}
		
		assertNotNull(u1);
		assertNotNull(u2);
		assertNotNull(u1u2);
		
		// Unversioned objects do not exist in any stable revision.
		assertNull("Ticket #1713: Unversioned objects must not exist in any stable revision.",
			HistoryUtils.getKnowledgeItem(HistoryUtils.getRevision(r1), u1));
		
		// Associations between unversioned objects are not versioned and do
		// therefore not exist in any stable revision.
		assertNull("Ticket #3976: Ticket #1713: Associations between unversioned objects must not be versioned.",
			HistoryUtils.getKnowledgeItem(HistoryUtils.getRevision(r1), u1u2));
		
		final long r2;
		{
			Transaction tx = begin();
			setA2(u1, "update 1");
			commit(tx);
			r2 = tx.getCommitRevision().getCommitNumber();
		}
		
		// Unversioned objects do not exist in any stable revision.
		assertNull(HistoryUtils.getKnowledgeItem(HistoryUtils.getRevision(r2), u1));
	}
	
	/**
	 * Test (not) branching unversioned objects.
	 */
	public void testBranchedUnversionedObjects() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final KnowledgeObject u1;
		final KnowledgeObject u2;
		final KnowledgeAssociation u1u2;
		final long r1;
		{
			Transaction tx = begin();
			u1 = newU("u1");
			u2 = newU("u2");
			u1u2 = newUA(u1, u2);
			commit(tx);
			r1 = tx.getCommitRevision().getCommitNumber();
		}
		
		Branch branch2 = kb().createBranch(kb().getTrunk(), kb().getRevision(r1), null);
		
		// Unversioned objects do not exist in any stable revision and therefore
		// are not included in a branch that is made from a stable revision.
		assertNull("Ticket #1713: Unversioned objects must not exist in any stable revision.",
			HistoryUtils.getKnowledgeItem(branch2, u1));
		assertNull("Ticket #3976: Ticket #1713: Associations between unversioned objects must not be versioned.",
			HistoryUtils.getKnowledgeItem(branch2, u1u2));
	}
	
	/**
	 * Test internal lifecylcle attributes for unversioned types.
	 */
	public void testUnversionedLivecycle() throws DataObjectException {
		final KnowledgeObject u0;
		{
			Transaction tx = begin();
			u0 = newU("u0");
			commit(tx);
		}
		
		final KnowledgeObject u1;
		final long r1;
		{
			Transaction tx = begin();
			u1 = newU("u1");
			commit(tx);
			r1 = tx.getCommitRevision().getCommitNumber();
		}
		
		{
			Transaction tx = begin();
			setA2(u0, "don't case 1");
			commit(tx);
			// Long r2 = tx.getCommitRevision().getCommitNumber();
		}
		
		{
			Transaction tx = begin();
			setA2(u1, "update 1");
			commit(tx);
			// Long r3 = tx.getCommitRevision().getCommitNumber();
		}
		
		{
			Transaction tx = begin();
			setA2(u0, "don't case 2");
			commit(tx);
			// Long r4 = tx.getCommitRevision().getCommitNumber();
		}
		
		final long r5;
		{
			Transaction tx = begin();
			setA2(u1, "update 2");
			commit(tx);
			r5 = tx.getCommitRevision().getCommitNumber();
		}
		
		{
			Transaction tx = begin();
			setA2(u0, "don't case 3");
			commit(tx);
			// Long r6 = tx.getCommitRevision().getCommitNumber();
		}
		
		// Check that u1 is an unversioned object.
		assertNull(HistoryUtils.getKnowledgeItem(HistoryUtils.getRevision(r1), u1));
		
		assertEquals("Ticket #1713: Lifecyle attributes for unversioned objects must work as for versioned objects.",
			r1, HistoryUtils.getCreateRevision(u1).getCommitNumber());
		assertEquals("Ticket #1713: Lifecyle attributes for unversioned objects must work as for versioned objects.",
			r5, HistoryUtils.getLastUpdate(u1).getCommitNumber());
	}
	
	/**
	 * Test intrinsic life cycle attributes base on revisions.
	 */
	public void testVersionedLifecycle() throws DataObjectException {
		final KnowledgeObject b0;
		{
			Transaction tx = begin();
			b0 = newB("b0");
			commit(tx);
		}
		
		final KnowledgeObject b1;
		final long r1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			commit(tx);
			r1 = tx.getCommitRevision().getCommitNumber();
		}
		
		final long r2;
		{
			Transaction tx = begin();
			setA2(b1, "update 1");
			commit(tx);
			r2 = tx.getCommitRevision().getCommitNumber();
		}
		
		final long r3;
		{
			Transaction tx = begin();
			setA2(b0, "don't care");
			commit(tx);
			r3 = tx.getCommitRevision().getCommitNumber();
		}
		
		final long r4;
		{
			Transaction tx = begin();
			setA2(b1, "update 2");
			commit(tx);
			r4 = tx.getCommitRevision().getCommitNumber();
		}
		
		{
			Transaction tx = begin();
			newB("b2");
			commit(tx);
		}
		
		assertEquals(r1, HistoryUtils.getCreateRevision(b1).getCommitNumber());
		assertEquals(r4, HistoryUtils.getLastUpdate(b1).getCommitNumber());
		
		KnowledgeItem b1r2 = HistoryUtils.getKnowledgeItem(HistoryUtils.getRevision(r3), b1);
		assertEquals(r1, HistoryUtils.getCreateRevision(b1r2).getCommitNumber());
		assertEquals(r2, HistoryUtils.getLastUpdate(b1r2).getCommitNumber());
	}
	
	public void testCleanupAfterTruncationFailure() throws DataObjectException {
		int illegalSize = KnowledgeBaseTestScenarioImpl.A2.getSQLSize() + 1;
		StringBuffer buffer = new StringBuffer(illegalSize);
		for (int n = 0; n < illegalSize; n++) {
			buffer.append('Z');
		}
		
		{
			Transaction tx = begin();
			KnowledgeObject b1 = newB("b1");
			setA2(b1, buffer.toString());
			commit(tx, true);
		}
		
		{
			Transaction tx = begin();
			KnowledgeObject b2 = newB("b1");
			setA2(b2, buffer.substring(0, buffer.length() - 1));
			commit(tx);
		}
	}
	
	public void testCleanupAfterOutOfRangeFailure() throws DataObjectException, SQLException {
		DBHelper sqlDialect = kb().getConnectionPool().getSQLDialect();
		{
			Transaction tx = begin();
			newZ("z1", Double.MAX_VALUE);
			
			try {
				// This fails, because the value is out of range.
				commit(tx, "Double.MAX_VALUE is out of range. Database type of z2 is FLOAT.", true);
			} catch (AssertionFailedError ex) {
				if (sqlDialect instanceof H2Helper) {
					/* Exptected due to the known bug in ticket #17728: H2 stores values out of
					 * range. */
					return;
				}
				throw ex;
			}
			if (sqlDialect instanceof H2Helper) {
				fail("Test should fail due to the known bug in ticket #17728: H2 stores values out of range.");
			}
		}
			
		{
			Transaction tx = begin();
			// Fails with Oracle, if executed against the same connection as the
			// first commit. This is a test for connections being dropped upon
			// failed commits.
			newZ("z1", 42.13d);
			commit(tx);
		}
	}
	
	public void testFailureInListener() throws DataObjectException {
		UpdateListener listener = new UpdateListener() {

			@Override
			public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
				throw new KnowledgeBaseRuntimeException();
			}

		};
		
		kb().addUpdateListener(listener);
		try {
			Transaction tx = begin();
			KnowledgeObject b1 = newB("b1");
			KnowledgeObject b2 = newB("b2");
			newAB(b1, b2);
			
			try {
	            Logger.configureStdout("FATAL"); // suppress the (correct) ERROR
				commit(tx);
				fail("Expected failure.");
			} catch (KnowledgeBaseRuntimeException ex) {
				// Expected.
			} finally {
			    Logger.configureStdout();
			}
		} finally {
			kb().removeUpdateListener(listener);
		}
	}
	
	public void testErrorInListener() throws DataObjectException {
		UpdateListener listener = new UpdateListener() {
			@Override
			public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
				throw new ExpectedError();
			}
			
		};
		
		kb().addUpdateListener(listener);
		try {
			Transaction tx = begin();
			KnowledgeObject b1 = newB("b1");
			KnowledgeObject b2 = newB("b2");
			newAB(b1, b2);
			
			try {
			    Logger.configureStdout("FATAL"); // suppress the (correct) ERROR
				commit(tx);
				fail("Expected error.");
			} catch (ExpectedError ex) {
				// Expected.
			} finally {
			    Logger.configureStdout(); 
			}
		} finally {
			kb().removeUpdateListener(listener);
		}
	}
	
    public void testChangeCheck() throws DataObjectException {
    	final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			setA2(b1, "a2");
			commit(tx);
		}
		final Revision r2 = HistoryUtils.getLastRevision();
		
		{
			Transaction tx = begin();
			setA2(b1, "a2");
			commit(tx);
			
			assertNull("Ticket #884: Revision was created without actual changes.", tx.getCommitRevision());
		}
		// Note: tx.getCommitRevision does return null for a noop commit.
		// Therefore r2 and r2again cannot be checked for equality.
		final Revision r2again = HistoryUtils.getLastRevision();
	
		assertEquals("Ticket #884: Revision was created without actual changes.", r2, r2again);
	}
	
	public void testIsAlive() throws Throwable {
		final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			
			assertTrue(b1.isAlive());
			
			inThread(new Execution() {
				@Override
				public void run() throws Exception {
					assertFalse(b1.isAlive());
				}
			});
			
			commit(tx);
		}
		
		assertTrue(b1.isAlive());
		
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				assertTrue(b1.isAlive());
			}
		});

		{
			Transaction tx = begin();
			b1.delete();
			
			assertFalse(b1.isAlive());
			
			inThread(new Execution() {
				@Override
				public void run() throws Exception {
					assertTrue(b1.isAlive());
				}
			});
			
			commit(tx);
		}
		
		assertFalse(b1.isAlive());
		
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				assertFalse(b1.isAlive());
			}
		});
	}
	
	public void testItemLookup() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Branch branch2 = HistoryUtils.createBranch(HistoryUtils.getTrunk(), HistoryUtils.getLastRevision(), null);
		HistoryUtils.setContextBranch(branch2);
		
		KnowledgeObject b1;
		KnowledgeObject b2;
		KnowledgeObject b3;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			b3 = newB("b3");
			newAB(b1, b2);
			newAB(b1, b3);
			commit(tx);
		}
		
		TLID b1Name = b1.getObjectName();
		
		{
			Transaction tx = begin();
			setA1(b1, "b1a1");
			commit(tx);
		}
		
		final Revision r2;
		{
			Transaction tx = begin();
			setA1(b2, "b2a1");
			commit(tx);
			r2 = tx.getCommitRevision();
		}
		
		{
			Transaction tx = begin();
			setA1(b3, "b3a1");
			commit(tx);
		}
		
		final Revision r3;
		{
			Transaction tx = begin();
			b1.delete();
			b2.delete();
			b3.delete();
			commit(tx);
			r3 = tx.getCommitRevision();
		}
		
		HistoryUtils.setContextBranch(HistoryUtils.getTrunk());
		
		// Check, whether object does no longer exist an cannot be retrieved on wrong branch.
		assertNull(HistoryUtils.getKnowledgeItem(HistoryUtils.getTrunk(), Revision.CURRENT, type(B_NAME), b1Name));
		assertNull(HistoryUtils.getKnowledgeItem(HistoryUtils.getTrunk(), r2, type(B_NAME), b1Name));
		assertNull(HistoryUtils.getKnowledgeItem(HistoryUtils.getTrunk(), r3, type(B_NAME), b1Name));
		assertNull(HistoryUtils.getKnowledgeItem(branch2, Revision.CURRENT, type(B_NAME), b1Name));
		assertNull(HistoryUtils.getKnowledgeItem(branch2, r3, type(B_NAME), b1Name));
		
		KnowledgeObject b1r2 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch2, r2, type(B_NAME), b1Name);
		assertNotNull(b1r2);
		
		assertTargetNames("", new String[] { "b2a1", "b3" }, b1r2.getOutgoingAssociations(AB_NAME));
	}
	
	public void testRevisionSearch() throws DataObjectException, InterruptedException {
		int cnt = 25;
		long[] commitTimes = new long[cnt];
		Revision[] revisions = new Revision[cnt];
		
		for (int n = 0; n < cnt; n++) {
			{
				Transaction tx = begin();
				newB("b" + n);
				commit(tx);
				revisions[n] = tx.getCommitRevision();
				commitTimes[n] = System.currentTimeMillis();
			}
			
			// Make sure not creating more than one revision per millisecond.
			Thread.sleep(1);
		}

		for (int n = 0; n < cnt; n++) {
			assertEquals(revisions[n], HistoryUtils.getRevisionAt(commitTimes[n]));
		}
	}
	
	public void testSearchInContext() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Branch trunk = HistoryUtils.getTrunk();
		
		HistoryUtils.setContextBranch(trunk);
		Revision r2 = HistoryUtils.getLastRevision();
		
		// Create branch that does not branch the created objects.
		Branch branch2 = HistoryUtils.createBranch(trunk, r2, types(C_NAME));
		Branch branch3 = HistoryUtils.createBranch(trunk, r2, types(C_NAME));
		
		// Switch to new branch
		HistoryUtils.setContextBranch(branch2);

		KnowledgeObject b1;
		KnowledgeObject b2;
		{
			// Create new objects (in base branch of partial branch).
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			
			assertEquals(trunk.getBranchId(), b1.getBranchContext());
			
			try {
				newAB(b1, b2);
				fail("Type '" + AB_NAME + "' is branched, type '" + B_NAME
					+ "' is not. No branch local reference possible");
			} catch (DataObjectException ex) {
				// expected
			}
			Branch former = HistoryUtils.setContextBranch(trunk);
			KnowledgeItem ab = newAB(b1, b2);
			HistoryUtils.setContextBranch(former);
			assertEquals(trunk.getBranchId(), ab.getBranchContext());
			
			// Search test 1 in creation branch.
			{
				KnowledgeObject b1_lookup = (KnowledgeObject) kb().getObjectByAttribute(B_NAME, A1_NAME, "b1");
				assertNotNull(b1_lookup);
				assertEquals(b1.getBranchContext(), b1_lookup.getBranchContext());
				assertSame(b1, b1_lookup);
			}
			
			// Search test 2 in separate branch.
			{
				HistoryUtils.setContextBranch(branch3);
				
				KnowledgeObject b1_lookup = (KnowledgeObject) kb().getObjectByAttribute(B_NAME, A1_NAME, "b1");
				assertNotNull(b1_lookup);
				assertEquals(b1.getBranchContext(), b1_lookup.getBranchContext());
				assertSame(b1, b1_lookup);
			}
			
			commit(tx);
		}
		
		// Check that objects also exist in trunk.
		HistoryUtils.setContextBranch(trunk);
		KnowledgeObject b1_trunk = (KnowledgeObject) kb().getObjectByAttribute(B_NAME, A1_NAME, "b1");
		assertNotNull(b1_trunk);
		assertSame(b1, b1_trunk);
		
		KnowledgeObject b2_trunk = (KnowledgeObject) kb().getObjectByAttribute(B_NAME, A1_NAME, "b2");
		assertNotNull(b2_trunk);
		assertSame(b2, b2_trunk);
		
		KBTestUtils.assertTargets("", new KnowledgeObject[] { b2_trunk }, b1_trunk.getOutgoingAssociations(AB_NAME));
	}
	
	public void testLookupInContext() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Branch trunk = HistoryUtils.getTrunk();
		
		HistoryUtils.setContextBranch(trunk);
		Revision r2 = HistoryUtils.getLastRevision();
		
		// Create branch that does not branch the created objects.
		Branch branch2 = HistoryUtils.createBranch(trunk, r2, types(C_NAME));
		Branch branch3 = HistoryUtils.createBranch(trunk, r2, types(C_NAME));
		
		// Switch to new branch
		HistoryUtils.setContextBranch(branch2);
		
		KnowledgeObject b1;
		KnowledgeObject b2;
		{
			// Create object views.
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			
			assertEquals(trunk.getBranchId(), b1.getBranchContext());
			
			try {
				newAB(b1, b2);
				fail("Type '" + AB_NAME + "' is branched, type '" + B_NAME
					+ "' is not. No branch local reference possible");
			} catch (DataObjectException ex) {
				// expected
			}
			Branch former = HistoryUtils.setContextBranch(trunk);
			KnowledgeItem ab = newAB(b1, b2);
			HistoryUtils.setContextBranch(former);
			assertEquals(trunk.getBranchId(), ab.getBranchContext());
			
			// Lookup test 1 in creation branch.
			{
				KnowledgeObject b1_lookup = kb().getKnowledgeObject(B_NAME, b1.getObjectName());
				assertNotNull(b1_lookup);
				assertEquals(b1.getBranchContext(), b1_lookup.getBranchContext());
				assertSame(b1, b1_lookup);
			}
			
			// Lookup test 2 in separate branch.
			{
				HistoryUtils.setContextBranch(branch3);
				
				KnowledgeObject b1_lookup = kb().getKnowledgeObject(B_NAME, b1.getObjectName());
				assertNotNull(b1_lookup);
				assertEquals(b1.getBranchContext(), b1_lookup.getBranchContext());
				assertSame(b1, b1_lookup);
			}
			
			commit(tx);
		}
		
		// Check that objects also exist in trunk.
		HistoryUtils.setContextBranch(trunk);
		KnowledgeObject b1_trunk = (KnowledgeObject) kb().getObjectByAttribute(B_NAME, A1_NAME, "b1");
		assertNotNull(b1_trunk);
		assertSame(b1, b1_trunk);
		
		KnowledgeObject b2_trunk = (KnowledgeObject) kb().getObjectByAttribute(B_NAME, A1_NAME, "b2");
		assertNotNull(b2_trunk);
		assertSame(b2, b2_trunk);
		
		KBTestUtils.assertTargets("", new KnowledgeObject[] { b2_trunk }, b1_trunk.getOutgoingAssociations(AB_NAME));
	}
	
	public void testLifeCycleAttributes() throws Throwable {
		final KnowledgeObject[] b = new KnowledgeObject[2];
		
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				TLContextManager.getSubSession().setContextId("creator-b1");
				{
					Transaction tx = begin();
					b[0] = newB("b1");
					commit(tx);
				}
			}
		});
		
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				TLContextManager.getSubSession().setContextId("creator-b2");
				{
					Transaction tx = begin();
					b[1] = newB("b2");
					commit(tx);
				}
			}
		});
		
		Revision r1 = HistoryUtils.getLastRevision();
		Thread.sleep(100);
		
		updateSessionRevision();

		assertEquals("creator-b1", b[0].getAttributeValue(LifecycleAttributes.CREATOR));
		assertEquals("creator-b2", b[1].getAttributeValue(LifecycleAttributes.CREATOR));
		
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				TLContextManager.getSubSession().setContextId("modifier-b1");
				{
					Transaction tx = begin();
					b[0].setAttributeValue(A2_NAME, "b1.a2");
					commit(tx);
				}
			}
		});
		
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				TLContextManager.getSubSession().setContextId("modifier-b2");
				{
					Transaction tx = begin();
					b[1].setAttributeValue(A2_NAME, "b2.a2");
					commit(tx);
				}
			}
		});

		Revision r2 = HistoryUtils.getLastRevision();
		Thread.sleep(100);
		
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				TLContextManager.getSubSession().setContextId("second-modifier-b1");
				{
					Transaction tx = begin();
					b[0].setAttributeValue(A2_NAME, "updated-b1.a2");
					commit(tx);
				}
			}
		});

		Revision r3 = HistoryUtils.getLastRevision();

		updateSessionRevision();
		
		assertEquals("creator-b1", b[0].getAttributeValue(LifecycleAttributes.CREATOR));
		assertEquals("creator-b2", b[1].getAttributeValue(LifecycleAttributes.CREATOR));
		
		assertEquals("second-modifier-b1", b[0].getAttributeValue(LifecycleAttributes.MODIFIER));
		assertEquals("modifier-b2", b[1].getAttributeValue(LifecycleAttributes.MODIFIER));
		
		KnowledgeItem b1_r1 = HistoryUtils.getKnowledgeItem(r1, b[0]);
		assertNotNull(b1_r1);
		assertEquals("creator-b1", b1_r1.getAttributeValue(LifecycleAttributes.CREATOR));
		assertEquals("creator-b1", b1_r1.getAttributeValue(LifecycleAttributes.MODIFIER));
		
		KnowledgeItem b2_r1 = HistoryUtils.getKnowledgeItem(r1, b[1]);
		assertEquals("creator-b2", b2_r1.getAttributeValue(LifecycleAttributes.CREATOR));
		assertEquals("creator-b2", b2_r1.getAttributeValue(LifecycleAttributes.MODIFIER));
		
		KnowledgeItem b1_r2 = HistoryUtils.getKnowledgeItem(r2, b[0]);
		assertEquals("creator-b1", b1_r2.getAttributeValue(LifecycleAttributes.CREATOR));
		assertEquals("modifier-b1", b1_r2.getAttributeValue(LifecycleAttributes.MODIFIER));

		KnowledgeItem b2_r2 = HistoryUtils.getKnowledgeItem(r2, b[1]);
		assertEquals("creator-b2", b2_r2.getAttributeValue(LifecycleAttributes.CREATOR));
		assertEquals("modifier-b2", b2_r2.getAttributeValue(LifecycleAttributes.MODIFIER));

		KnowledgeItem b1_r3 = HistoryUtils.getKnowledgeItem(r3, b[0]);
		KnowledgeItem b2_r3 = HistoryUtils.getKnowledgeItem(r3, b[1]);
		
		assertEquals("creator-b1", b1_r3.getAttributeValue(LifecycleAttributes.CREATOR));
		assertEquals("second-modifier-b1", b1_r3.getAttributeValue(LifecycleAttributes.MODIFIER));
		
		assertEquals(b[0].getAttributeValue(LifecycleAttributes.CREATED), b1_r2.getAttributeValue(LifecycleAttributes.CREATED));
		assertEquals(b2_r2.getAttributeValue(LifecycleAttributes.MODIFIER), b2_r3.getAttributeValue(LifecycleAttributes.MODIFIER));
		assertTrue(((Long) b[0].getAttributeValue(LifecycleAttributes.CREATED)).longValue() < ((Long) b[0].getAttributeValue(LifecycleAttributes.MODIFIED)).longValue());
	}
	
	public void testReadRetrySearch() throws Throwable {
		final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			commit(tx);
		}
		assertNotNull(b1);
		
		DBKnowledgeBaseTestSetup.simulateConnectionBreakdown();
		
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				DataObject b1Lookup = kb().getObjectByAttribute(B_NAME, A1_NAME, "b1");
				assertNotNull(b1Lookup);
				assertEquals(b1, b1Lookup);
			}
		});
	}
	
	public void testReadRetryRefetch() throws Throwable {
		final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			commit(tx);
		}
		assertNotNull(b1);
		
		DBKnowledgeBaseTestSetup.simulateConnectionBreakdown();
		
		try {
			kb().refetch();
		} catch (KnowledgeBaseRuntimeException ex) {
			fail("Refetch is broken. Connection is not recreated?", ex);
		}
	}
	
	public void testGetTrunk() {
		Branch trunk = HistoryUtils.getTrunk();
		
		assertNotNull(trunk);
		assertNull(trunk.getBaseBranch());
	}
	
	public void testDetectSharedNewObject() throws Throwable {
		Transaction tx = begin();
		final KnowledgeObject b1 = newB("b1");
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				try {
					setA2(b1, "illegal access");
					fail("Access to new object from other thread must fail.");
				} catch (IllegalStateException ex) {
					// Expected.
				}
			}
		});

		assertNull(b1.getAttributeValue(A2_NAME));
		setA2(b1, "legal access");
		commit(tx);

	}

	public void testConcurrentModificationFailed() throws DataObjectException {
		Transaction tx = begin();
		final KnowledgeObject b1 = newB("b1");
		commit(tx);

		Transaction changeTx = begin();
		setA2(b1, "update of persistent object");
		
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				Transaction concurrentTx = begin();
				setA2(b1, "faster commit");
				commit(concurrentTx);
			}
		});
		
		commit(changeTx, "Concurrent change of same object.", true);
		assertEquals("faster commit", b1.getAttributeValue(A2_NAME));
	}
	
	public void testCommittedCaseInsensitiveSearch() throws DataObjectException, SQLException {
		doTestCollatedSearch(true, true);
	}

	public void testUncommittedCaseInsensitiveSearch() throws DataObjectException, SQLException {
		doTestCollatedSearch(false, true);
	}

	public void testCommittedBinarySearch() throws DataObjectException, SQLException {
		doTestCollatedSearch(true, false);
	}

	public void testUncommittedBinarySearch() throws DataObjectException, SQLException {
		doTestCollatedSearch(false, false);
	}
	
	private void doTestCollatedSearch(boolean commit, boolean caseInsensitiveSearch) throws DataObjectException,
			UnknownTypeException, SQLException {
		{
			Transaction tx = begin();
			final KnowledgeObject b1 = newB("b1");
			final KnowledgeObject b2 = newB("b2");
			final KnowledgeObject b3 = newB("b3");
			final KnowledgeObject b4 = newB("b4");
			final KnowledgeObject b5 = newB("b5");
			
			if (caseInsensitiveSearch) {
				setA1(b1, "Test");
				setA1(b2, "test");
				setA1(b3, "test   ");
				setA1(b4, "test   it!");
				setA1(b5, "   test");
			} else {
				setA2(b1, "Test");
				setA2(b2, "test");
				setA2(b3, "test   ");
				setA2(b4, "test   it!");
				setA2(b5, "   test");
			}
			
			if (commit) {
				commit(tx);
			}
			
			DBKnowledgeBase kb = kb();

			if (caseInsensitiveSearch) {
				DBHelper sqlDialect = kb.getConnectionPool().getSQLDialect();
				if (commit && !(sqlDialect instanceof H2Helper)) {
					try {
						assertEquals(set(b1, b2),
							CollectionUtil.toSet(kb.getObjectsByAttribute(B_NAME, A1_NAME, "Test")));
						fail("Test should fail due to the known bug in ticket #18743: Case insensitive search failed.");
					} catch (AssertionFailedError exception) {
						/* Expected due to the known bug in ticket #18743: Case insensitive search
						 * failed. */
					}
				} else {
					assertEquals(set(b1, b2),
						CollectionUtil.toSet(kb.getObjectsByAttribute(B_NAME, A1_NAME, "Test")));
				}
			} else {
				assertEquals("Ticket #382: Binary search failed.", set(b1),
					CollectionUtil.toSet(kb.getObjectsByAttribute(B_NAME, A2_NAME, "Test")));
			}
			
			if (! commit) {
				rollback(tx);
			}
		}
	}

	public void testBranchIdentity() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final KnowledgeObject b1;
		final Revision r1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			
			commit(tx);
			r1 = tx.getCommitRevision();
		}
		
		assertNotNull(b1);
		
		// Lookup the trunk branch.
		Branch trunkInTrunk = HistoryUtils.getTrunk();
		
		// Create branch2.
		Branch branch2 = HistoryUtils.createBranch(HistoryUtils.getContextBranch(), r1, null);
		
		// Switch to branch2
		HistoryUtils.setContextBranch(branch2);
		
		// Lookup the representation of branch2 and trunk in the context of branch2.
		Branch branch2InBranch2 = HistoryUtils.getBranch(branch2.getBranchId());
		Branch trunkInBranch2 = HistoryUtils.getTrunk();

		// Switch back to trunk.
		HistoryUtils.setContextBranch(trunkInTrunk);
		
		// Lookup the representation of branch2 in the context of trunk.
		Branch branch2InTrunk = HistoryUtils.getBranch(branch2.getBranchId());
		
		// Check that all representatives of corresponding branches are equal.
		assertTrue(trunkInTrunk.equals(trunkInBranch2));
		assertTrue(trunkInBranch2.equals(trunkInTrunk));
		
		assertTrue(branch2InBranch2.equals(branch2InTrunk));
		assertTrue(branch2InTrunk.equals(branch2InBranch2));
		assertTrue(branch2InBranch2.equals(branch2));
		assertTrue(branch2InTrunk.equals(branch2));
		assertTrue(branch2.equals(branch2InBranch2));
		assertTrue(branch2.equals(branch2InTrunk));
	}
	
	public void testBasicBranching() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final KnowledgeObject b1;
		final KnowledgeObject b2;
		final KnowledgeObject b3;
		final KnowledgeObject c1;
		final KnowledgeObject c2;
		final KnowledgeObject c3;
		final KnowledgeAssociation b2c3;
		final Revision r1;
		{
			// Create set of objects.
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			b3 = newB("b3");
			
			c1 = newC("c1");
			c2 = newC("c2");
			c3 = newC("c3");
			
			// Link b1 to c1-c2 and b3 using AB
			newAB(b1, c1);
			newAB(b1, c2);
			newAB(b1, b3);
			
			// Link b2 to c1-c3 and b3 using BC
			newBC(b2, c1);
			newBC(b2, c2);
			b2c3 = newBC(b2, c3);
			newBC(b2, b3);
			
			commit(tx);
			r1 = tx.getCommitRevision();
		}
		
		// Create branch2 that only branches types B and AB.
		Branch branch2 = HistoryUtils.createBranch(HistoryUtils.getContextBranch(), r1, types(B_NAME, C_NAME));

		// Fetch corresponding objects on branch2.
		KnowledgeObject b1_branch2 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch2, b1);
		KnowledgeObject b2_branch2 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch2, b2);
		KnowledgeObject b3_branch2 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch2, b3);
		
		KnowledgeObject c1_branch2 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch2, c1);
		KnowledgeObject c2_branch2 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch2, c2);
		KnowledgeObject c3_branch2 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch2, c3);
		
		assertNotNull(c1_branch2);
		assertNotNull(c2_branch2);
		assertNotNull(c3_branch2);
		
		KnowledgeAssociation b1c2_branch2 = b1_branch2.getOutgoingAssociations(AB_NAME, c2_branch2).next();
		assertNotNull(b1c2_branch2);
		
		
		// Check that the branch context of b1 in branch 2 is branch2.
		assertEquals(branch2, HistoryUtils.getBranch(b1_branch2));
		
		// Even if the object data of an object of type C is not branched,
		// assert that its transient representations knows about its branch
		// context.
		assertEquals(branch2, HistoryUtils.getBranch(c1_branch2));
		
		// Check, whether all associations are still intact on branch 2.
		KBTestUtils.assertTargets("", new KnowledgeObject[] { c1_branch2, c2_branch2, b3_branch2 },
			b1_branch2.getOutgoingAssociations(AB_NAME));
		KBTestUtils.assertTargets("", new KnowledgeObject[] { c1_branch2, c2_branch2, c3_branch2, b3_branch2 },
			b2_branch2.getOutgoingAssociations(BC_NAME));
		
		{
			// Delete originals.
			Transaction tx = begin();
			b3.delete();
			c2.delete();
			
			// Delete association.
			b2c3.delete();
	
			// Test result before commit.
			KBTestUtils.assertTargets("", new KnowledgeObject[] { c1 }, b1.getOutgoingAssociations(AB_NAME));
			KBTestUtils.assertTargets("", new KnowledgeObject[] { c1 }, b2.getOutgoingAssociations(BC_NAME));
	
			KBTestUtils.assertTargets("", new KnowledgeObject[] { c1_branch2, c2_branch2, b3_branch2 },
				b1_branch2.getOutgoingAssociations(AB_NAME));
			KBTestUtils.assertTargets("", new KnowledgeObject[] { c1_branch2, c2_branch2, c3_branch2, b3_branch2 },
				b2_branch2.getOutgoingAssociations(BC_NAME));
			
			commit(tx);
		}
		
		assertFalse(b3.isAlive());
		assertFalse(c2.isAlive());
		assertTrue(c2_branch2.isAlive());
		assertFalse(b2c3.isAlive());
		
		// Test result after commit.
		KBTestUtils.assertTargets("", new KnowledgeObject[] { c1 }, b1.getOutgoingAssociations(AB_NAME));
		KBTestUtils.assertTargets("", new KnowledgeObject[] { c1 }, b2.getOutgoingAssociations(BC_NAME));
		
		KBTestUtils.assertTargets("", new KnowledgeObject[] { c1_branch2, c2_branch2, b3_branch2 },
			b1_branch2.getOutgoingAssociations(AB_NAME));
		KBTestUtils.assertTargets("", new KnowledgeObject[] { c1_branch2, c2_branch2, c3_branch2, b3_branch2 },
			b2_branch2.getOutgoingAssociations(BC_NAME));
	}
	
	public void testAssociationMultipleUnique() throws DataObjectException {
		{
			Transaction tx = begin();
			final KnowledgeObject b1 = newB("b1");
			
			final KnowledgeObject c1 = newC("c1");
			final KnowledgeObject c2 = newC("c2");
			final KnowledgeObject c3 = newC("c3");
			
			assertNotNull(b1);
			assertNotNull(c1);
			assertNotNull(c2);
			assertNotNull(c3);
	
	// TODO: Test new style associations. 
	//
			// kb.createAssociation(b1, c1, OTHERS_TARGET_NAME);
			// kb.createAssociation(b1, c2, OTHERS_TARGET_NAME);
			// kb.createAssociation(b1, c3, OTHERS_TARGET_NAME);
			
			rollback(tx);
		}
	}
	
	
	/**
	 * Regression Test: Navigate over KAs via EventListener inside commit().
	 */
    public void testKAsInCommit() throws DataObjectException {
    	final KnowledgeObject b1;
    	final KnowledgeObject c1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			c1 = newC("c1");
	        commit(tx);
		}
		
        TestKAEventListener listener = new TestKAEventListener();
		kb().addUpdateListener(listener);

		try {
			{
				Transaction tx = begin();
				newAB(b1, c1);
				listener.expectedCount = 1;
				commit(tx);
			}

			{
				Transaction tx = begin();
				newAB(b1, c1);
				listener.expectedCount = 2;
				commit(tx);
			}

			{
				Transaction tx = begin();
				newAB(b1, c1);
				listener.expectedCount = 3;
				commit(tx);
			}
		} finally {
			kb().removeUpdateListener(listener);
		}
    }

    /**
	 * Check that KA events are fied and check navigation _inside_ commit.
	 */
	class TestKAEventListener implements UpdateListener {
	    
	    int expectedCount;
	    
	    @Override
		public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
			assertEquals("Expected exactly one createEvents.", 1, event.getCreatedObjectKeys().size());
	        try {
				ObjectKey createdObjectKey = event.getCreatedObjectKeys().iterator().next();
				KnowledgeItem item = event.getKnowledgeBase().resolveObjectKey(createdObjectKey);
				String kaType = item.tTable().getName();
				assertSame(item, kb().getKnowledgeAssociation(kaType, item.getObjectName()));

				KnowledgeAssociation link = (KnowledgeAssociation) item;
				assertCountIterator(expectedCount, link.getSourceObject().getOutgoingAssociations());
				assertCountIterator(expectedCount, link.getSourceObject().getOutgoingAssociations(kaType));
				assertCountIterator(expectedCount,
					link.getSourceObject().getOutgoingAssociations(kaType, link.getDestinationObject()));

				assertCountIterator(expectedCount, link.getDestinationObject().getIncomingAssociations());
				assertCountIterator(expectedCount, link.getDestinationObject().getIncomingAssociations(kaType));
                
				assertInIterator(link, link.getSourceObject().getOutgoingAssociations());
				assertInIterator(link, link.getSourceObject().getOutgoingAssociations(kaType));
				assertInIterator(link,
					link.getSourceObject().getOutgoingAssociations(kaType, link.getDestinationObject()));

				assertInIterator(link, link.getDestinationObject().getIncomingAssociations());
				assertInIterator(link, link.getDestinationObject().getIncomingAssociations(kaType));
                
            }
            catch (InvalidLinkException ex) {
                fail("Unexpected InvalidLinkException");
            }
            
	    }
	    
	}
	
	
	public void testInitialRevision() {
		Branch initialTrunk = HistoryUtils.getTrunk();
		Revision initialRevision = initialTrunk.getCreateRevision();
		assertNotEquals(initialRevision.getCommitNumber(), Revision.CURRENT.getCommitNumber());
	}
	
	public void testCheckContextId() throws DataObjectException {
		// Create context without context ID.
		SubSessionContext session = ThreadContextManager.getSubSession();
		String contextId = session.getContextId();
		session.setContextId(null);
		try {
			{
				try {
					Transaction tx = begin();
					try {
						newB("b");
						fail("Expected exception.");
					} finally {
						rollback(tx);
					}
				} catch (KnowledgeBaseRuntimeException ex) {
					// expected.
				}
			}
		} finally {
			session.setContextId(contextId);
		}
	}

	public void testObjectLivecycle() throws Throwable {
		final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b.a1");
			
			b1.setAttributeValue(A2_NAME, "b.a2");
			
			b1.setAttributeValue(B1_NAME, "b.b1");
			b1.setAttributeValue(B2_NAME, "b.b2");
			
			DataObject b2 = kb().getObjectByAttribute(B_NAME, B1_NAME, "b.b1");
			assertEquals("New object found in context", b1, b2);
	
			inThread(new Execution() {
				@Override
				public void run() {
					DataObject b2a = kb().getObjectByAttribute(B_NAME, B1_NAME, "b.b1");
					assertNull("New object not yet seen by other thread", b2a);
				}
			});
			
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			b1.setAttributeValue(B2_NAME, "b1.b2");
			
			DataObject b3 = kb().getObjectByAttribute(B_NAME, B2_NAME, "b.b2");
			assertNull("Lookup with old value does not return result in local context", b3);
	
			DataObject b4 = kb().getObjectByAttribute(B_NAME, B2_NAME, "b1.b2");
			assertEquals("Lookup with new value finds the object in local context", b1, b4);
			
			inThread(new Execution() {
				@Override
				public void run() {
					DataObject b4a = kb().getObjectByAttribute(B_NAME, B2_NAME, "b.b2");
					assertEquals("Lookup with old value does return the object in other thread", b1, b4a);
				}
			});
			
			commit(tx);
		}
		
		DataObject b5 = kb().getObjectByAttribute(B_NAME, B2_NAME, "b1.b2");
		assertEquals("Lookup with new value still finds the object in local context after commit", b1, b5);
		
		inThread(new Execution() {
			@Override
			public void run() {
				DataObject b5a = kb().getObjectByAttribute(B_NAME, B2_NAME, "b1.b2");
				assertEquals("Lookup with new value now finds the object in other thread after commit", b1, b5a);
			}
		});
		
		{
			Transaction tx = begin();
			kb().delete(b1);
			
			DataObject b6 = kb().getObjectByAttribute(B_NAME, B2_NAME, "b1.b2");
			assertNull("Deleted object is no longer returned in local context", b6);
			
			inThread(new Execution() {
				@Override
				public void run() {
					DataObject b6a = kb().getObjectByAttribute(B_NAME, B2_NAME, "b1.b2");
					assertEquals("Deleted object is still found in other thread", b1, b6a);
				}
			});
			
			commit(tx);
		}
		
		inThread(new Execution() {
			@Override
			public void run() {
				DataObject b7 = kb().getObjectByAttribute(B_NAME, B2_NAME, "b1.b2");
				assertNull("Object now deleted for all threads", b7);
			}
		});
	}
	
	public void testDuplicateIdentifier() throws DataObjectException {
		final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			commit(tx);
		}
		
		TLID b1Name = b1.tId().getObjectName();
		
		{
			Transaction tx = begin();
			try {
				kb().createKnowledgeObject(b1Name, B_NAME);
				fail("Create with name clash.");
			} catch (IllegalArgumentException ex) {
				// Expected.
			}
			rollback(tx);
		}
		
		{
			Transaction tx = begin();
			// Delete b1
			b1.delete();
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			// Try to revive b1 with original identifier.
			KnowledgeObject b1Revived = kb().createKnowledgeObject(b1Name, B_NAME);
			assertNotNull(b1Revived);
			b1Revived.setAttributeValue(A1_NAME, "b1revived");
			
			commit(tx);
		}
	}
	
	public void testAssociation() throws Throwable {
		final KnowledgeObject b1;
		final KnowledgeObject c1;
		final KnowledgeObject c2;
		final KnowledgeObject d1;
		final KnowledgeObject d2;
		{
			// Create objects.
			Transaction tx = begin();
			b1 = newB("b1");
			
			c1 = newC("c1");
			c2 = newC("c2");
			
			d1 = newD("d1");
			d2 = newD("c2");
			
			commit(tx);
		}
		
		final Execution checkCreated;
		final KnowledgeAssociation bc1;
		final KnowledgeAssociation bc2;
		final KnowledgeAssociation ab1;
		final KnowledgeAssociation ab2;
		{
			// Create associations.
			Transaction tx = begin();
			bc1 = newBC(b1, c1);
			bc1.setAttributeValue(BC1_NAME, "bc1.bc1");
			bc2 = newBC(b1, c2);
			bc2.setAttributeValue(BC1_NAME, "bc1.bc2");
			
			ab1 = newAB(b1, d1);
			ab1.setAttributeValue(AB1_NAME, "ab1.ab1");
			ab2 = newAB(b1, d2);
			ab2.setAttributeValue(AB1_NAME, "ab1.ab2");
			
			checkCreated = new Execution() {
				@Override
				public void run() throws Exception {
					KBTestUtils.assertTargets("All association links by type match", 
						new KnowledgeObject[] {c1, c2}, 
						b1.getOutgoingAssociations(BC_NAME));
	
					KBTestUtils.assertTargets("Association links by type and target match.", 
						new KnowledgeObject[] {d2}, 
						b1.getOutgoingAssociations(AB_NAME, d2));
					
					KBTestUtils.assertTargets("All association links from given source match.", 
						new KnowledgeObject[] {c1, c2, d1, d2}, 
						b1.getOutgoingAssociations());
				}
			};
	
			checkCreated.run();
			
			inThread(new Execution() {
				@Override
				public void run() throws Exception {
					KBTestUtils.assertTargets("Other thread sees no modifications before commit.", 
						new KnowledgeObject[] {}, 
						b1.getOutgoingAssociations(BC_NAME));
				}
			});
			
			commit(tx);
		}
		
		checkCreated.run();
		inThread(checkCreated);
		
		final Execution checkRemoved1;
		{
			// Remove associations.
			Transaction tx = begin();
			bc1.delete();
			ab1.delete();
			
			checkRemoved1 = new Execution() {
				@Override
				public void run() throws Exception {
					KBTestUtils.assertTargets("Association have been removed", 
						new KnowledgeObject[] {c2}, 
						b1.getOutgoingAssociations(BC_NAME));
	
					KBTestUtils.assertTargets("Association have been removed", 
						new KnowledgeObject[] {d2}, 
						b1.getOutgoingAssociations(AB_NAME, d2));
					
					KBTestUtils.assertTargets("Association have been removed", 
						new KnowledgeObject[] {c2, d2}, 
						b1.getOutgoingAssociations());
				}
			};
	
			checkRemoved1.run();
			
			commit(tx);
		}
		
		checkRemoved1.run();
		inThread(checkRemoved1);
		
		final Execution checkRemoved2;
		{
			// Remove target object.
			Transaction tx = begin();
			b1.delete();
	
			checkRemoved2 = new Execution() {
				@Override
				public void run() throws Exception {
					KBTestUtils.assertTargets("Association have been removed", 
						new KnowledgeObject[] {}, 
						c2.getOutgoingAssociations());
	
					KBTestUtils.assertTargets("Association have been removed", 
						new KnowledgeObject[] {}, 
						d2.getOutgoingAssociations());
				}
			};
			
			checkRemoved2.run();
			inThread(checkRemoved1);
			
			commit(tx);
		}
		
		checkRemoved2.run();
		inThread(checkRemoved2);
	}
	
	public void testLastUpdate() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final KnowledgeObject b1;
		final KnowledgeObject c1;
		final Revision r1;
		{
			// Initial setup.
			Transaction tx = begin();
			b1 = newB("b1");
			c1 = newC("c1");
	
			commit(tx);
			r1 = tx.getCommitRevision();
		}
		
		assertEquals(r1, lastUpdate(b1));
		assertEquals(r1, lastUpdate(c1));

		final Revision r2;
		{
			// Modify b1
			Transaction tx = begin();
			b1.setAttributeValue(B2_NAME, "update 1");
			
			commit(tx);
			r2 = tx.getCommitRevision();
		}
		
		assertEquals(r2, lastUpdate(b1));
		assertEquals(r1, lastUpdate(c1));
		
		final Revision r3;
		{
			// Again modify b1
			Transaction tx = begin();
			b1.setAttributeValue(B2_NAME, "update 2");
			
			commit(tx);
			r3 = tx.getCommitRevision();
		}
		
		assertEquals(r3, lastUpdate(b1));
		assertEquals(r1, lastUpdate(c1));
		
		final Revision r4;
		{
			// Modify c1.
			Transaction tx = begin();
			c1.setAttributeValue(B2_NAME, "update 1");
			
			commit(tx);
			r4 = tx.getCommitRevision();
		}
		
		assertEquals(r3, lastUpdate(b1));
		assertEquals(r4, lastUpdate(c1));
		
		// Create partial branch.
		Branch branch1 = HistoryUtils.createBranch(HistoryUtils.getContextBranch(), r3, types(C_NAME));
		Revision r5 = HistoryUtils.getLastRevision();
		
		KnowledgeItem b1_branch1 = HistoryUtils.getKnowledgeItem(branch1, b1);
		KnowledgeItem c1_branch1 = HistoryUtils.getKnowledgeItem(branch1, c1);
		
		assertEquals("B was not branched, therefore, b1 is a view.", r3, HistoryUtils.getLastUpdate(b1_branch1));
		assertEquals("C was branched, therefore, c1 was copied.", r5, HistoryUtils.getLastUpdate(c1_branch1));
	}

	public void testCreateRevision() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");

		assertEquals(Revision.CURRENT, HistoryUtils.getCreateRevision(b1));
		/* Concurrent creation must not touch create revision. */
		inThread(new Execution() {

			@Override
			public void run() throws Exception {
				Transaction innerTx = begin();
				KnowledgeObject innerB1 = newB("b1");
				assertEquals(Revision.CURRENT, HistoryUtils.getCreateRevision(innerB1));
				innerTx.commit();
				assertEquals(innerTx.getCommitRevision(), HistoryUtils.getCreateRevision(innerB1));
			}
		});
		assertEquals(Revision.CURRENT, HistoryUtils.getCreateRevision(b1));
		tx.commit();
		assertEquals(tx.getCommitRevision(), HistoryUtils.getCreateRevision(b1));
	}

	public void testHistory() throws Throwable {
		final KnowledgeObject b1;
		final KnowledgeObject b2;
		final KnowledgeObject c1;
		final KnowledgeObject c2;
		final KnowledgeObject d1;
		final KnowledgeObject d2;
		{
			// Create objects.
			Transaction tx = begin();
			b1 = newB("b1");
			b1.setAttributeValue(B2_NAME, "initial");
	
			b2 = newB("b2");
			c1 = newC("c1");
			c2 = newC("c2");
			d1 = newC("d1");
			d2 = newC("d2");
			
			commit(tx);
		}
		
		// With the first revision, the trunk branch is created!
		long commit1 = 2L;
		assertEquals("Objects created in revision " + commit1, commit1, lastUpdate(b1).getCommitNumber());
		
		final KnowledgeAssociation b1c1;
		{
			// Create associations.
			Transaction tx = begin();
			b1c1 = newBC(b1, c1);
			newBC(b1, c2);
			newBC(b2, c2);
			newAB(b1, d1);
			newAB(b1, d2);
			newAB(b2, d1);
			newAB(b2, d2);
			
			commit(tx);
		}
		
		long commit2 = 3L;
		Revision revision2 = HistoryUtils.getLastUpdate(b1c1);
		assertEquals("Associations created in revision " + commit2, commit2, revision2.getCommitNumber());

		final KnowledgeObject c3;
		{
			// Modify. 
			Transaction tx = begin();
			c2.delete();
			
			c3 = newC("c3");
			newBC(b1, c3);
			
			b1.setAttributeValue(B2_NAME, "update1");
			
			commit(tx);
		}
		
		long commit3 = 4L;
		assertEquals("Created in revision " + commit3, commit3, lastUpdate(c3).getCommitNumber());
		assertEquals("Modified in revision " + commit3, commit3, lastUpdate(b1).getCommitNumber());
		
		// TODO: toString() of objectName is wrong.
		KnowledgeObject b1_2 =
			(KnowledgeObject) HistoryUtils.getKnowledgeItem(revision2, type(B_NAME),
				HistoryUtils.getItemIdentity(b1, b1).getObjectName());
		assertNotNull(b1_2);
		assertEquals("Initial modification time found.", commit1, lastUpdate(b1_2).getCommitNumber());
		assertEquals("Correct point in time found.", commit2, revison(b1_2).getCommitNumber());
		assertEquals("Initial attribute value found.", "initial", b1_2.getAttributeValue(B2_NAME));
		TestDBKnowledgeBase.assertTargetNames("Original associations found.", new String[]{"c1", "c2", "d1", "d2"}, b1_2.getOutgoingAssociations());
	}
	
	public void testHistoryContext() throws DataObjectException {
		final KnowledgeObject b1;
		final KnowledgeObject b2;
		final Revision r2;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			commit(tx);
			r2 = tx.getCommitRevision();
		}
		
		final Revision r3;
		{
			Transaction tx = begin();
			setA2(b1, "update 1");
			commit(tx);
			r3 = tx.getCommitRevision();
		}
		
		final Revision r4;
		{
			Transaction tx = begin();
			setA2(b1, "update 2");
			commit(tx);
			r4 = tx.getCommitRevision();
		}
		
		final Revision r5;
		{
			Transaction tx = begin();
			setA2(b2, "update 1");
			commit(tx);
			r5 = tx.getCommitRevision();
		}
		
		assertEquals(r4, lastUpdate(b1));
		assertEquals(r5, lastUpdate(b2));
		
		assertEquals(Revision.CURRENT, HistoryUtils.getRevision(b1));
		assertEquals(Revision.CURRENT, HistoryUtils.getRevision(b2));
		
		{
			KnowledgeItem b1_r4 = HistoryUtils.getKnowledgeItem(r4, b1);
			KnowledgeItem b2_r4 = HistoryUtils.getKnowledgeItem(r4, b2);
			KnowledgeItem b1_r3 = HistoryUtils.getKnowledgeItem(r3, b1);
			KnowledgeItem b2_r3 = HistoryUtils.getKnowledgeItem(r3, b2);
			KnowledgeItem b1_r2 = HistoryUtils.getKnowledgeItem(r2, b1);
			KnowledgeItem b2_r2 = HistoryUtils.getKnowledgeItem(r2, b2);
			
			assertHistoryContext(r2, r3, r4, b1_r4, b2_r4, b1_r3, b2_r3, b1_r2, b2_r2);
		}
		
		{
			KnowledgeItem b1_r4 = (KnowledgeItem) HistoryUtils.getObjectsByAttribute(r4, B_NAME, A1_NAME, "b1").get(0);
			KnowledgeItem b2_r4 = (KnowledgeItem) HistoryUtils.getObjectsByAttribute(r4, B_NAME, A1_NAME, "b2").get(0);
			KnowledgeItem b1_r3 = (KnowledgeItem) HistoryUtils.getObjectsByAttribute(r3, B_NAME, A1_NAME, "b1").get(0);
			KnowledgeItem b2_r3 = (KnowledgeItem) HistoryUtils.getObjectsByAttribute(r3, B_NAME, A1_NAME, "b2").get(0);
			KnowledgeItem b1_r2 = (KnowledgeItem) HistoryUtils.getObjectsByAttribute(r2, B_NAME, A1_NAME, "b1").get(0);
			KnowledgeItem b2_r2 = (KnowledgeItem) HistoryUtils.getObjectsByAttribute(r2, B_NAME, A1_NAME, "b2").get(0);
			
			assertHistoryContext(r2, r3, r4, b1_r4, b2_r4, b1_r3, b2_r3, b1_r2, b2_r2);
		}
		
	}

	private void assertHistoryContext(Revision r2, Revision r3, Revision r4,
			KnowledgeItem b1_r4, KnowledgeItem b2_r4, KnowledgeItem b1_r3,
			KnowledgeItem b2_r3, KnowledgeItem b1_r2, KnowledgeItem b2_r2) {
		assertEquals(r4, lastUpdate(b1_r4));
		assertEquals(r2, lastUpdate(b2_r4));
		
		assertEquals(r3, lastUpdate(b1_r3));
		assertEquals(r2, lastUpdate(b2_r3));
		
		assertEquals(r2, lastUpdate(b1_r2));
		assertEquals(r2, lastUpdate(b2_r2));
		
		// Test history contexts of objects.
		assertEquals(r4, HistoryUtils.getRevision(b1_r4));
		assertEquals(r4, HistoryUtils.getRevision(b2_r4));
		
		assertEquals(r3, HistoryUtils.getRevision(b1_r3));
		assertEquals(r3, HistoryUtils.getRevision(b2_r3));
		
		assertEquals(r2, HistoryUtils.getRevision(b1_r2));
		assertEquals(r2, HistoryUtils.getRevision(b2_r2));
	}

	public void testChecks() throws Throwable {
	    class CheckFailingRemoval implements Execution {
	    	private final KnowledgeObject obj;
			private final KnowledgeAssociation link;

			private CheckFailingRemoval(KnowledgeObject obj, KnowledgeAssociation link) {
				this.link = link;
				this.obj = obj;
			}

			@Override
			public void run() throws Exception {
				// Remove removed object (knowledge base utility).
				try {
					kb().delete(obj);
					fail("Removal of removed object must fail.");
				} catch (IllegalStateException ex) {
					// Expected.
				}
				
				// Remove removed object (object method).
				try {
					obj.delete();
					fail("Removal of removed object must fail.");
				} catch (IllegalStateException ex) {
					// Expected.
				}

				// Remove removed link (knowledge base utility).
				try {
					kb().delete(link);
					fail("Removal of removed link must fail.");
				} catch (IllegalStateException ex) {
					// Expected.
				}
				
				// Remove removed link (link method).
				try {
					link.delete();
					fail("Removal of removed link must fail.");
				} catch (IllegalStateException ex) {
					// Expected.
				}
			}
		}

		final Execution checkFailingRemoveNew;
		final KnowledgeObject b1;
		final KnowledgeObject b2;
		final KnowledgeObject b3;
		final KnowledgeObject c1;
		final KnowledgeObject c2;
		final KnowledgeObject c3;
		final KnowledgeAssociation b2c1;
		final KnowledgeAssociation b2c2;
		{
			// Create objects.
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			b3 = newB("b3");
			
			c1 = newC("c1");
			c2 = newC("c2");
			c3 = newC("c3");
	
			// Create associations.
			KnowledgeAssociation b1c1 = newBC(b1, c1);
			KnowledgeAssociation b1c2 = newBC(b1, c2);
			KnowledgeAssociation b1c3 = newBC(b1, c3);
			b2c1 = newBC(b2, c1);
			b2c2 = newBC(b2, c2);
			KnowledgeAssociation b2c3 = newBC(b2, c3);
			KnowledgeAssociation b3c1 = newBC(b3, c1);
			KnowledgeAssociation b3c2 = newBC(b3, c2);
			KnowledgeAssociation b3c3 = newBC(b3, c3);
			
			assertNotNull(b1c1);
			assertNotNull(b1c2);
			assertNotNull(b1c3);
			assertNotNull(b2c1);
			assertNotNull(b2c2);
			assertNotNull(b2c3);
			assertNotNull(b3c1);
			assertNotNull(b3c2);
			assertNotNull(b3c3);
			
			// Remove new object
			b1.delete();
			
			// Remove new link.
			b2c1.delete();
			
			checkFailingRemoveNew = new CheckFailingRemoval(b1, b2c1);
			
			checkFailingRemoveNew.run();
	
			commit(tx);
		}
		
		checkFailingRemoveNew.run();
		inThread(checkFailingRemoveNew);

		final Execution checkFailingRemovePersistent;
		{
			// Remove persistent object.
			Transaction tx = begin();
			b3.delete();
			
			// Remove persistent link.
			b2c2.delete();
			
			checkFailingRemovePersistent = new CheckFailingRemoval(b3, b2c2);
			
			checkFailingRemovePersistent.run();
			
			commit(tx);
		}
		
		checkFailingRemovePersistent.run();
		inThread(checkFailingRemovePersistent);
	}
	
    public void testRemoveNewItems() throws Exception {
    	final KnowledgeObject b1;
    	final KnowledgeObject c1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			c1 = newC("c1");
	        
	        commit(tx); 
		}
		
		{
			Transaction tx = begin();
	        KnowledgeAssociation c1b1 = newAB(c1, b1);
	        
	        // Remove uncommitted new association.
	        c1b1.delete();
	        
	        commit(tx);    
		}
		
		{
			Transaction tx = begin();
	        KnowledgeObject b2 = newB("b2");
	        
	        // Remove uncommited new object.
	        b2.delete();
	        
	        commit(tx);
		}
		
		final KnowledgeObject b3;
		{
			Transaction tx = begin();
			b3 = newB("b3");
	        KnowledgeAssociation newc1b1 = newBC(c1, b1);
	        newAB(c1, b3);
	        
	        assertNotNull(newc1b1);
			assertTrue(newc1b1.isAlive());
	        
	        // Implicitly delete new uncommitted newc1b1.
	        b1.delete();
	        
	        commit(tx);
		}
		
		KBTestUtils.assertTargets("Association to new object survived.", new KnowledgeObject[] { b3 },
			c1.getOutgoingAssociations(AB_NAME));
    }
    
	/**
	 * Tests that it is possible to remove different ends of one association in different threads.
	 */
	public void testAssociationEndDeletion() throws Throwable {
    	final KnowledgeObject b1;
    	final KnowledgeObject c1;
    	final KnowledgeAssociation b1c1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			c1 = newC("c1");
	
			b1c1 = newBC(b1, c1);
	
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			kb().delete(b1); // implies delete of b1c1
			
			inThread(new Execution() {
				@Override
				public void run() throws Exception {
					Transaction tx = begin();
					kb().delete(c1);
					commit(tx);
				}
			});
			
			commit(tx, "Deletion of other end should work, also if associationed association already deleted.");
		}
		
		assertFalse(b1c1.isAlive());
	}

    public void testAtomicChecks() throws DataObjectException {
    	final int cnt = 25;
		final HashSet<String> cnNames = new HashSet<>();
    	final KnowledgeObject[] cn = new KnowledgeObject[cnt];
    	final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			
			// Allocate c0,..., c24 that are linked to b1.
			for (int n = 0; n < cnt; n++) {
				String cnName = "c" + n;
				cn[n] = newC(cnName);
				cnNames.add(cnName);
				
				newBC(b1, cn[n]);
			}
	
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			cn[15].delete();
			assertTrue(cnNames.remove("c15"));
			commit(tx);
		}
		
		assertTrue(b1.isAlive());
		assertTargetNames("Only c15 is no longer referenced.", cnNames.toArray(new String[cnNames.size()]),
			b1.getOutgoingAssociations(BC_NAME));
    }

    /** 
     * How do KOs and KAs behave on rollback()?
     */
    public void testRollbackDelete() throws Exception {
    	final KnowledgeObject b1;
    	final KnowledgeObject c1;
    	final KnowledgeAssociation c1b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			c1 = newC("c1");
	        
			c1b1 = newAB(c1, b1);
	        
	        commit(tx);    
		}
		
		{
			Transaction tx = begin();
	        c1.delete();
	        
	        assertSame(kb(), b1.getKnowledgeBase());
	        assertSame(kb(), c1.getKnowledgeBase());
	        assertSame(kb(), c1b1.getKnowledgeBase());
	
	        rollback(tx);    
		}
		
		{
			Transaction tx = begin();
	        assertSame(kb(),   b1.getKnowledgeBase());
	        assertSame(kb(),   c1.getKnowledgeBase());
	        assertSame(kb(), c1b1.getKnowledgeBase());
	
	        c1.delete();
	        commit(tx); 
		}
		
        assertSame(kb(),   b1.getKnowledgeBase());
		assertFalse(c1.isAlive());
		assertFalse(c1b1.isAlive());
    }

    public void testRollbackModify() throws Exception {
    	final KnowledgeObject b1;
    	final KnowledgeObject b2;
    	final KnowledgeObject b3;
    	final KnowledgeAssociation b1b2;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			b3 = newB("b3");
	
			b1b2 = newAB(b1, b2);
	        
	        commit(tx);
		}
		
		{
			Transaction tx = begin();
	        setA1(b1, "b1new");
	        kb().addCommittable(COMMIT_FAILURE);
	        
	        commit(tx, true);
		}
		
		assertEquals("b1", b1.getAttributeValue(A1_NAME));
        
        final KnowledgeAssociation b1b2new;
		{
			Transaction tx = begin();
	        b1b2.delete();
			b1b2new = newAB(b1, b3);
	        assertNotNull(b1b2new);
	
			assertTargetNames("", new String[] { "b3" }, b1.getOutgoingAssociations(AB_NAME));
	        
	        kb().addCommittable(COMMIT_FAILURE);
	        commit(tx, true);
		}
		
        assertFalse(b1b2new.isAlive());
        
		assertTargetNames("", new String[] { "b2" }, b1.getOutgoingAssociations(AB_NAME));
    }

    public void testRevisionLookup() throws Throwable {
    	final KnowledgeObject b1;
    	final KnowledgeObject c1;
    	final KnowledgeAssociation c1b1;
		final long commit1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			c1 = newC("c1");
	        
			c1b1 = newAB(c1, b1);
	        assertNotNull(c1b1);
	        
	        commit(tx);    
			commit1 = tx.getCommitRevision().getCommitNumber();
		}
		
        final String[] committer = new String[] {
        	TLContext.getContext().getContextId(),
        	null
        };
        
        inThread(new Execution() {
			@Override
			public void run() throws Exception {
				committer[1] = TLContext.getContext().getContextId();
				
				{
					Transaction tx = begin();
					final KnowledgeObject c2 = newC("c2");
			        
					final KnowledgeAssociation c2b1 = newAB(c2, b1);
			        assertNotNull(c2b1);
			        
			        commit(tx);
				}
			}
        });
        
        Revision revision1 = HistoryUtils.getRevision(commit1);
        Revision revision2 = HistoryUtils.getLastRevision();
        
        assertEquals("Committer matches author of revision " + revision1 + ".", committer[0], revision1.getAuthor());
        assertEquals("Committer matches author of revision " + revision2 + ".", committer[1], revision2.getAuthor());
    }


    public void testUnversionedIdentity() throws DataObjectException {
    	final KnowledgeObject b1;
    	final KnowledgeObject c1;
    	final Revision revision1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			c1 = newC("c1");
	    	
	        commit(tx);    
			revision1 = tx.getCommitRevision();
		}
		
		final Revision revision2;
		{
			Transaction tx = begin();
	        setA1(b1, "b1@2");
	        setA1(c1, "c1@2");
	
	        commit(tx);    
			revision2 = tx.getCommitRevision();
		}
		
		final Revision revision3;
		{
			Transaction tx = begin();
	        setA1(b1, "b1@3");
	
	        commit(tx);    
			revision3 = tx.getCommitRevision();
		}
		
		final Revision revision4;
		{
			Transaction tx = begin();
	        setA1(c1, "c1@4");
	        
	        commit(tx);    
			revision4 = tx.getCommitRevision();
		}
		
		{
			Transaction tx = begin();
	        setA1(c1, "c1@presence");
	
	        
	        KnowledgeItem b1_1 = HistoryUtils.getKnowledgeItem(revision1, b1);
	        KnowledgeItem c1_1 = HistoryUtils.getKnowledgeItem(revision1, c1);
	        
	        KnowledgeItem b1_2 = HistoryUtils.getKnowledgeItem(revision2, b1);
	        KnowledgeItem c1_2 = HistoryUtils.getKnowledgeItem(revision2, c1);
	        
	        KnowledgeItem b1_3 = HistoryUtils.getKnowledgeItem(revision3, b1);
	        KnowledgeItem c1_3 = HistoryUtils.getKnowledgeItem(revision3, c1);
	        
	        KnowledgeItem b1_4 = HistoryUtils.getKnowledgeItem(revision4, b1);
	        KnowledgeItem c1_4 = HistoryUtils.getKnowledgeItem(revision4, c1);
	
	        // History does not equal presence.
	        assertNotEquals(b1, b1_1);
	        assertNotEquals(c1, c1_1);
	
	        // Last stable objects do not equal their mutable current counterparts,
			// no matter whether the current object differs from its last committed
			// version.
	        assertNotEquals(b1, b1_4);
	        assertNotEquals(c1, c1_4);
	        
	        // Stable versions are not equal, even if the actual contents did not
			// change in the corresponding revisions. Reason: the history context of
			// the object differs.
	        assertNotEquals(b1_2, b1_3);
	        assertNotEquals(c1_2, c1_3);
	        
	
	        // Corresponding unversioned identities match.
	        assertEquals(itemIdentity(b1), itemIdentity(b1_1));
	        assertEquals(itemIdentity(c1), itemIdentity(c1_1));
	        assertEquals(itemIdentity(b1), itemIdentity(b1_4));
	        assertEquals(itemIdentity(c1), itemIdentity(c1_4));
	        assertEquals(itemIdentity(b1_2), itemIdentity(b1_3));
	        assertEquals(itemIdentity(c1_2), itemIdentity(c1_3));
	        
	        rollback(tx);
		}
    }
    
    public void testVersionedSearch() throws DataObjectException {
    	// Create objects with unique values in each column.
    	int objCnt = 20;
    	KnowledgeObject[] objs = new KnowledgeObject[objCnt];
		for (int n = 0; n < objCnt; n++) {
			{
				Transaction tx = begin();
				objs[n] = newX(true, (byte) n, ((char) ('a' + n)), "1970-1-1 00:" + n + "0", n, n, n, n, (short) n, Integer.toString(n));
				commit(tx);
			}
    	}

		// Update all objects some times.
    	int updateCnt = 10;
    	Revision[] revisionForUpdate = new Revision[updateCnt];
		for (int k = 0; k < updateCnt; k++) {
			// Check, whether each object can be identified by its unique value in column X7.
	    	for (int n = 0; n < objCnt; n++) {
				KnowledgeObject object =
					(KnowledgeObject) kb().getObjectByAttribute(X_NAME, X7_NAME, Integer.valueOf((n + k)));
				assertEquals(objs[n], object);
	    	}

			{
				Transaction tx = begin();
		    	// Simultaneously update all objects by shifting their values in
				// each column by one. This preserves the uniqueness property for 
				// each column.
		    	for (int n = 0; n < objCnt; n++) {
					int m = n + k + 1;
		    		updateX(objs[n], true, (byte) m, ((char) ('a' + m)), "1970-1-1 00:" + m + "0", m, m, m, m, (short) m, Integer.toString(m));
		    	}
		    	
				// Check, whether each object can be identified by its unique value in column X7.
		    	for (int n = 0; n < objCnt; n++) {
					KnowledgeObject object =
						(KnowledgeObject) kb().getObjectByAttribute(X_NAME, X7_NAME, Integer.valueOf((n + k + 1)));
					assertEquals(objs[n], object);
		    	}
		    	
		    	commit(tx);
		    	revisionForUpdate[k] = tx.getCommitRevision();
			}
    	}
		
		// Check that old revisions also can be retrieved by looking up objects by their historic attribute values.
		for (int k = 0; k < updateCnt; k++) {
			// Check, whether each object can be identified by its unique value in column X7.
	    	for (int n = 0; n < objCnt; n++) {
				List objects =
					HistoryUtils.getObjectsByAttribute(revisionForUpdate[k], X_NAME, X7_NAME,
						Integer.valueOf((n + k + 1)));
				assertEquals(1, objects.size());
				
				assertEquals(itemIdentity(objs[n]), itemIdentity((KnowledgeItem) objects.get(0)));
	    	}
		}
    }
    
    public void testUniqueOk() throws DataObjectException {
    	final KnowledgeObject o1;
    	final KnowledgeObject o2;
    	final KnowledgeObject o3;
		{
			Transaction tx = begin();
			o1 = newY(1, 100, "a");
			o2 = newY(2, 100, "b");
			o3 = newY(3, 101, "a");
	    	
	    	commit(tx);
		}
		
		{
			// Swap first attribute value for o1 and o2
			Transaction tx = begin();
	    	updateY(o1, 2, 100, "a");
	    	updateY(o2, 1, 100, "b");
	    	
	    	commit(tx);
		}
		
		{
			// Bring first attribute of o2 and o3 in order.
			Transaction tx = begin();
	    	updateY(o2, 3, 100, "b");
	    	updateY(o3, 4, 101, "a");
	    	
	    	commit(tx);
		}
    }
    
    public void testUniqueClashOnCreate() throws DataObjectException {
    	final KnowledgeObject y1;
    	final KnowledgeObject y2;
		{
			Transaction tx = begin();
			// Violates unique index on column y1.
			y1 = newY(1, 100, "a");
			y2 = newY(1, 101, "b");
	    	
	    	commit(tx, true);
		}
		
		{
			Transaction tx = begin();
		    	try {
		    		updateY(y1, 2, 200, "c");
		    		fail("Object invalid.");
		    	} catch (IllegalStateException ex) {
		    		// Expected.
		    	}
	    	
	    	// Violates constraint 3.
	    	newY(1, 100, "a");
	    	newY(2, 100, "a");
	    	
	    	commit(tx, true);
		}
    }
    
    public void testDeleteNewObject() throws DataObjectException {
    	final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
	
	    	// New objects have not to be locked, because they are still private to
			// the creating thread. This is an implementation detail and must not be
			// tested.
	    	//
	    	// assertTrue(b1.isLocked());
	
			assertSame(b1, kb().getObjectByAttribute(B_NAME, A1_NAME, "b1"));
			assertSame(b1, kb().resolveObjectKey(copyKey(b1.tId())));
	    	
	    	b1.delete();
	
			assertNull(kb().getObjectByAttribute(B_NAME, A1_NAME, "b1"));
			assertNull(kb().resolveObjectKey(copyKey(b1.tId())));
	    	
	    	commit(tx);
		}
		
    }

	public void testDeletePersistentObject() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		commit(tx);

		Transaction deleteTx = begin();
		assertSame(b1, kb().getObjectByAttribute(B_NAME, A1_NAME, "b1"));
		assertSame(b1, kb().resolveObjectKey(copyKey(b1.tId())));

		b1.delete();

		assertNull(kb().getObjectByAttribute(B_NAME, A1_NAME, "b1"));
		assertNull(kb().resolveObjectKey(copyKey(b1.tId())));

		commit(deleteTx);
	}

    public void testRollbackNewObjectDeletion() throws DataObjectException {
    	final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
	    	
	    	// New objects have not to be locked, because they are still private to the creating thread.
	    	//
	    	// assertTrue(b1.isLocked());
	    	
			assertSame(b1, kb().getObjectByAttribute(B_NAME, A1_NAME, "b1"));
	    	
	    	b1.delete();
	    	
			assertNull(kb().getObjectByAttribute(B_NAME, A1_NAME, "b1"));
	    	
	    	rollback(tx);
		}
		
		assertNull(kb().getObjectByAttribute(B_NAME, A1_NAME, "b1"));
    }
    
    public void testDeleteNewLink() throws DataObjectException {
    	final KnowledgeObject b1;
    	final KnowledgeObject b2;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
	
	    	commit(tx);
		}
		
		final DBKnowledgeAssociation b1b2;
		{
			Transaction tx = begin();
			b1b2 = (DBKnowledgeAssociation) newAB(b1, b2);
	    	
			KBTestUtils.assertTargets("Transient association found.", list(b2), b1.getOutgoingAssociations(AB_NAME));
	    	
	    	b1b2.delete();
	
			KBTestUtils.assertTargets("Association deleted.", NO_OBJECTS, b1.getOutgoingAssociations(AB_NAME));
	    	
	    	commit(tx);
		}
		
		KBTestUtils.assertTargets("Association deleted.", NO_OBJECTS, b1.getOutgoingAssociations(AB_NAME));
    }
    
    public void testRollbackNewLinkDeletion() throws DataObjectException {
    	final KnowledgeObject b1;
    	final KnowledgeObject b2;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
	    	
	    	commit(tx);
		}
		
		final DBKnowledgeAssociation b1b2;
		{
			Transaction tx = begin();
			b1b2 = (DBKnowledgeAssociation) newAB(b1, b2);
	    	
			KBTestUtils.assertTargets("Transient association found.", list(b2), b1.getOutgoingAssociations(AB_NAME));
	    	
	    	b1b2.delete();
	    	
			KBTestUtils.assertTargets("Association deleted.", NO_OBJECTS, b1.getOutgoingAssociations(AB_NAME));
	    	
	    	rollback(tx);
		}
		
		KBTestUtils.assertTargets("Association not committed.", NO_OBJECTS, b1.getOutgoingAssociations(AB_NAME));
    }

    public void testDeleteLink() throws DataObjectException {
    	final KnowledgeObject b1;
    	final KnowledgeObject b2;
    	final DBKnowledgeAssociation b1b2;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
	    	
			b1b2 = (DBKnowledgeAssociation) newAB(b1, b2);
	    	
			KBTestUtils.assertTargets("Transient association found.", list(b2), b1.getOutgoingAssociations(AB_NAME));
	    	
	    	commit(tx);
		}

		{
			Transaction tx = begin();
	    	b1b2.delete();
	
			KBTestUtils.assertTargets("Association deleted.", NO_OBJECTS, b1.getOutgoingAssociations(AB_NAME));
	    	
	    	commit(tx);
		}
    	
		KBTestUtils.assertTargets("Association deleted.", NO_OBJECTS, b1.getOutgoingAssociations(AB_NAME));
    }
    
    public void testRollbackLinkDeletion() throws DataObjectException {
    	final KnowledgeObject b1;
    	final KnowledgeObject b2;
    	final DBKnowledgeAssociation b1b2;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
	    	
			b1b2 = (DBKnowledgeAssociation) newAB(b1, b2);
	    	
			KBTestUtils.assertTargets("Transient association found.", list(b2), b1.getOutgoingAssociations(AB_NAME));
	    	
	    	commit(tx);
		}

		KBTestUtils.assertTargets("Persistent association found.", list(b2), b1.getOutgoingAssociations(AB_NAME));
    	
		{
			Transaction tx = begin();
	    	b1b2.delete();
	    	
			KBTestUtils.assertTargets("Association deleted.", NO_OBJECTS, b1.getOutgoingAssociations(AB_NAME));
	    	
	    	rollback(tx);
		}
    	
		KBTestUtils.assertTargets("Persistent association restored.", list(b2), b1.getOutgoingAssociations(AB_NAME));
    }

    public void testUniqueClashOnSimultaneousUpdate() throws DataObjectException {
    	final KnowledgeObject o1;
    	final KnowledgeObject o2;
		{
			Transaction tx = begin();
	    	// Violates constraint 1.
			o1 = newY(1, 100, "a");
			o2 = newY(2, 101, "b");
	    	
	    	commit(tx);
		}
		
		{
			// Clash on constraint 1.
			Transaction tx = begin();
	    	updateY(o1, 1, 100, "a");
	    	updateY(o2, 1, 101, "b");
	    	
	    	commit(tx, true);
		}
		
		{
			// Update after failed commit:
			Transaction tx = begin();
	    	updateY(o1, 2, 100, "a");
	    	updateY(o2, 3, 101, "b");
	    	
	    	commit(tx);
		}
		
		{
			// Clash on constraint 3.
			Transaction tx = begin();
	    	updateY(o1, 2, 101, "a");
	    	updateY(o2, 3, 101, "a");
	    	
	    	commit(tx, true);
		}
    }
    
    public void testUniqueClashWithExistingValue() throws DataObjectException {
    	final KnowledgeObject o1;
    	final KnowledgeObject o2;
		{
			// Violates constraint 1.
			Transaction tx = begin();
			o1 = newY(1, 100, "a");
			o2 = newY(2, 101, "b");
	    	
	    	commit(tx);
		}
		
		{
			// Clash on constraint 1.
			Transaction tx = begin();
	    	updateY(o2, 1, 101, "b");
	    	
	    	commit(tx, true);
		}
		
		{
			// Clash on constraint 3.
			Transaction tx = begin();
	    	updateY(o1, 1, 101, "b");
	    	
	    	commit(tx, true);
		}
    }

    public void testUniqueInBranch() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
    	final KnowledgeObject o1;
    	final KnowledgeObject o2;
		{
			Transaction tx = begin();
			o1 = newY(1, 100, "a");
			o2 = newY(2, 101, "b");
	    	
	    	commit(tx);
		}
		
    	// Branch table with unique constraint. This tests that unique
		// constraints are branch local.
    	Branch branch1 = HistoryUtils.createBranch(HistoryUtils.getTrunk(), HistoryUtils.getLastRevision(), null);
    	
		{
			// Change o2 and o1 on branch 1 to the same value. 
			Transaction tx = begin();
	    	updateY(o2, 3, 101, "c");
	    	
	    	HistoryUtils.setContextBranch(branch1);
	    	KnowledgeObject o1_branch1 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch1, o1);
	    	
	    	updateY(o1_branch1, 3, 101, "c");
	
	    	// Test that values on different branches do not interfere.
	    	commit(tx);
		}
    }

    public void testMultiBranchUpdates() throws Throwable {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
    	Branch trunk = HistoryUtils.getTrunk();
    	HistoryUtils.setContextBranch(trunk);

    	final KnowledgeObject b1;
    	final Revision r1;
		{
			Transaction tx = begin();
			b1 = newB("b1.a1");
	    	
	    	commit(tx);
			r1 = tx.getCommitRevision();
		}
		
    	Branch branch1 = HistoryUtils.createBranch(trunk, r1, null);
    	HistoryUtils.setContextBranch(branch1);

    	final KnowledgeObject b1_branch1 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch1, b1);
    	
    	final KnowledgeObject b2_branch1;
    	final Revision r2;
		{
			Transaction tx = begin();
			b2_branch1 = newB("b2.a1.branch1");
	    	newAB(b1_branch1, b2_branch1);
	    	newBC(b2_branch1, b1_branch1);
	
	    	commit(tx);
			r2 = tx.getCommitRevision();
		}
		
    	Branch branch2 = HistoryUtils.createBranch(branch1, r2, null);
    	HistoryUtils.setContextBranch(branch2);
    	
    	final KnowledgeObject b1_branch2;
    	final KnowledgeObject b2_branch2;
    	final KnowledgeObject b3_branch2;
		{
			Transaction tx = begin();
			b1_branch2 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch2, b1);
			b2_branch2 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch2, b2_branch1);
			b3_branch2 = newB("b2.a1.branch2");
	    	newAB(b1_branch2, b3_branch2);
	    	newAB(b2_branch2, b3_branch2);
	    	newBC(b3_branch2, b1_branch2);
	    	newBC(b3_branch2, b2_branch2);
	    	
	    	commit(tx);
		}
		
		Execution testValues;
		{
			// Perform multi-branch update.
			Transaction tx = begin();
	    	setA1(b1, "b1.a1.update1");
	    	setA1(b1_branch1, "b1.a1.branch1.update1");
	    	setA1(b1_branch2, "b1.a1.branch2.update1");
	    	
	    	setA1(b2_branch1, "b2.a1.branch1.update1");
	    	setA1(b2_branch2, "b2.a1.branch2.update1");
	    	
	    	setA1(b3_branch2, "b3.a1.branch2.update1");
	
			testValues = new Execution() {
				@Override
				public void run() throws Exception {
					KBTestUtils.assertTargets("Branch1 associations.", 
							new KnowledgeObject[] {b1_branch1}, 
							b2_branch1.getOutgoingAssociations(BC_NAME));
					
					KBTestUtils.assertTargets("Branch2 associations.", 
							new KnowledgeObject[] {b1_branch2}, 
							b2_branch2.getOutgoingAssociations(BC_NAME));
					
					KBTestUtils.assertTargets("Branch2 associations.", 
							new KnowledgeObject[] {b1_branch2, b2_branch2}, 
							b3_branch2.getOutgoingAssociations(BC_NAME));
					
					assertEquals("b1.a1.update1", b1.getAttributeValue(A1_NAME));
					assertEquals("b2.a1.branch1.update1", b2_branch1.getAttributeValue(A1_NAME));
					assertEquals("b3.a1.branch2.update1", b3_branch2.getAttributeValue(A1_NAME));
				}
	    	};
	    	
	    	testValues.run();
	    	
	    	commit(tx);
		}
		
    	testValues.run();
    	inThread(testValues);
    }
	
	public void testBranchTree() {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Branch trunk = HistoryUtils.getTrunk();
		{
			Branch b1 = HistoryUtils.createBranch(trunk, HistoryUtils.getLastRevision(), null);
			Branch b2 = HistoryUtils.createBranch(trunk, HistoryUtils.getLastRevision(), null);
			{
				Branch b21 = HistoryUtils.createBranch(b2, HistoryUtils.getLastRevision(), null);
				Branch b22 = HistoryUtils.createBranch(b2, HistoryUtils.getLastRevision(), null);
				
				assertEquals(set(b21, b22), toSet(b2.getChildBranches()));
			}
			Branch b3 = HistoryUtils.createBranch(trunk, HistoryUtils.getLastRevision(), null);
			{
				Branch b31 = HistoryUtils.createBranch(b3, HistoryUtils.getLastRevision(), null);
				Branch b32 = HistoryUtils.createBranch(b3, HistoryUtils.getLastRevision(), null);
				Branch b33 = HistoryUtils.createBranch(b3, HistoryUtils.getLastRevision(), null);
				
				assertEquals(set(b31, b32, b33), toSet(b3.getChildBranches()));
			}
			
			assertEquals(set(b1, b2, b3), toSet(trunk.getChildBranches()));
		}
	}
	
	/**
	 * Tests that it is not possible to create an object on some branch which is not the base branch
	 * of the corresponding type.
	 */
	public void testCreateNonBranchedTypeOnBranch() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		long r1 = kb().getLastRevision();
		Branch branch1 = kb().createBranch(kb().getTrunk(), HistoryUtils.getRevision(r1), types(B_NAME));
		MetaObject nonBranchedType = type(D_NAME);
		long baseBranchId = branch1.getBaseBranchId(type(D_NAME));
		assertFalse(D_NAME + " was not branched but has same base branch is", branch1.getBranchId() == baseBranchId);

		final KnowledgeObject newKnowledgeObject;
		try {
			newKnowledgeObject = kb().createKnowledgeObject(branch1, nonBranchedType.getName());
		} catch (Exception ex) {
			// creating on a branch which is not the base branch fails. Thats ok
			return;
		}
		assertEquals("KnowledgeObject does not live on base branch of its type", baseBranchId,
			newKnowledgeObject.getBranchContext());
	}

	public void testAccessNewObjectFromOuterThread() throws InterruptedException, NoSuchAttributeException {
		final AtomicReference<KnowledgeItem> currentlyCreatedItem = new AtomicReference<>();
		final Barrier createBarrier = createBarrier(2);
		final Barrier finishBarrier = createBarrier(2);
		
		inParallel(new Execution() {
			
			@Override
			public void run() throws Exception {
				Transaction createTX = begin();
				currentlyCreatedItem.set(newE("e1"));
				createBarrier.enter(1000);
				finishBarrier.enter(1000);
				rollback(createTX);
			}
		});
		createBarrier.enter(1000);

		try {
			currentlyCreatedItem.get().getAttributeValue(A1_NAME);
			fail("Access from outside creation thread not allowed.");
		} catch (RuntimeException ex) {
			// expected
		}

		Transaction otherTX = begin();
		try {
			currentlyCreatedItem.get().getAttributeValue(A1_NAME);
			fail("Access from outside creation thread not allowed.");
		} catch (RuntimeException ex) {
			// expected
		}
		rollback(otherTX);

		finishBarrier.enter(1000);
	}

	public void testSearchForFlexAttribute() throws DataObjectException {
		String stringFlexAttribute = "stringFlex";
		String longFlexAttribute = "longFlex";
		long longValue = 15L;
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		b1.setAttributeValue(longFlexAttribute, longValue);
		KnowledgeObject b2 = newB("b2");
		b2.setAttributeValue(stringFlexAttribute, Long.toString(longValue));
		commit(tx);

		assertEquals(list(b1), toList(kb().getObjectsByAttribute(B_NAME, longFlexAttribute, longValue)));
		assertEquals(list(b2),
			toList(kb().getObjectsByAttribute(B_NAME, stringFlexAttribute, Long.toString(longValue))));
	}

	public void testUpdateListenerRegistration() throws DataObjectException {
		class SelfUnregistering implements UpdateListener {
			private SelfUnregistering _next;

			private int _cnt;

			@Override
			public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
				_cnt++;
				_next = new SelfUnregistering();

				sender.removeUpdateListener(this);
				sender.addUpdateListener(_next);
			}

			public int getCnt() {
				return _cnt;
			}

			public SelfUnregistering getNext() {
				return _next;
			}

		}

		SelfUnregistering l1;
		SelfUnregistering l2;

		kb().addUpdateListener(l1 = new SelfUnregistering());
		kb().addUpdateListener(l2 = new SelfUnregistering());

		Transaction tx1 = begin();
		newB("b1");
		commit(tx1);

		assertEquals(1, l1.getCnt());
		assertEquals(1, l2.getCnt());
		assertEquals(0, l1.getNext().getCnt());
		assertEquals(0, l2.getNext().getCnt());

		Transaction tx2 = begin();
		newB("b2");
		commit(tx2);

		assertEquals(1, l1.getCnt());
		assertEquals(1, l2.getCnt());
		assertEquals(1, l1.getNext().getCnt());
		assertEquals(1, l2.getNext().getCnt());
	}

	@SuppressWarnings("unused")
	public static Test suite() {
    	// Switch to true to activate single test.
		if (false) {
			return runOneTest(TestDBKnowledgeBase.class, "testAnyReferer");
    	}
    	
		return suite(TestDBKnowledgeBase.class);
    }

}
