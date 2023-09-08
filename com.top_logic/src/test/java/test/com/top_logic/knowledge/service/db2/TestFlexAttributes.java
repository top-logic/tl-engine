/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.io.binary.TestingBinaryData;
import test.com.top_logic.knowledge.wrap.TestFlexWrapper;
import test.com.top_logic.knowledge.wrap.TestFlexWrapperCluster;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.basic.col.ObjectFlag;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataProxy;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.objects.ChangeInspectable;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.LifecycleAttributes;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DeletedObjectAccess;

/**
 * {@link TestFlexAttributes} tests the dynamic attributes in the {@link KnowledgeObject}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestFlexAttributes extends AbstractDBKnowledgeBaseClusterTest {

	private static final String FLEX_ATTRIBUTE = "flex";

	private static final String FLEX_ATTRIBUTE_2 = "flex_2";
	
	/**
	 * Test flex attributes in new objects.
	 */
	public void testCreateObjectWithFlexAttributes() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject item = newE("e1");
		assertNull(item.getAttributeValue(FLEX_ATTRIBUTE));
		item.setAttributeValue(FLEX_ATTRIBUTE, "value1");
		assertEquals("value1", item.getAttributeValue(FLEX_ATTRIBUTE));
		item.setAttributeValue(FLEX_ATTRIBUTE, "value2");
		assertEquals("value2", item.getAttributeValue(FLEX_ATTRIBUTE));
		commit(tx);

		assertEquals("Commit not sucessful.", "value2", item.getAttributeValue(FLEX_ATTRIBUTE));

		Transaction tx1 = begin();
		item.setAttributeValue(A2_NAME, "a2");
		commit(tx1);

		assertEquals("Row attribute change changes flex value.", "value2", item.getAttributeValue(FLEX_ATTRIBUTE));
	}

	/**
	 * Tests that fetching dynamic attributes does not fetch values in a future revision (i.e. in a
	 * revision later than that of the {@link KnowledgeBase}).
	 */
	public void testFetchingFutureDynamicAttributes() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(FLEX_ATTRIBUTE, "flex");
		KnowledgeObject e2 = newE("e2");
		e2.setAttributeValue(FLEX_ATTRIBUTE, "flex");
		createTx.commit();
		
		refetchNode2();
		// fetch item to simulate only flexAttribute loading
		KnowledgeItem e1_node2 = node2Item(e1);

		Transaction changeTx = begin();
		e1.setAttributeValue(FLEX_ATTRIBUTE, "flex_new");
		e2.setAttributeValue(FLEX_ATTRIBUTE, "flex_new");
		changeTx.commit();

		assertEquals("Flex attribute is set in a future revision.", "flex", e1_node2.getAttributeValue(FLEX_ATTRIBUTE));
		// check that loading object does not automatically loads wrong flex attributes
		assertEquals("Flex attribute is set in a future revision.", "flex",
			node2Item(e2).getAttributeValue(FLEX_ATTRIBUTE));

	}

	/**
	 * Tests {@link KnowledgeBase#createKnowledgeItem(Branch, TLID, String, Iterable, Class)} with
	 * flex attributes as initial values.
	 */
	public void testInitialFlexValue() throws DataObjectException {
		{
			Transaction tx = kb().beginTransaction();
			Iterable<Entry<String, Object>> initialValues =
				new NameValueBuffer().put(A1_NAME, "e1").put(FLEX_ATTRIBUTE, null);
			KnowledgeItem newE = kb().createKnowledgeItem(trunk(), E_NAME, initialValues, KnowledgeItem.class);
			assertEquals("FlexAttribute not set", null, newE.getAttributeValue(FLEX_ATTRIBUTE));
			assertEquals("Row attribute not set", "e1", newE.getAttributeValue(A1_NAME));
			tx.commit();
		}
		{
			Transaction tx = kb().beginTransaction();
			Iterable<Entry<String, Object>> initialValues =
				new NameValueBuffer().put(A1_NAME, "e1").put(FLEX_ATTRIBUTE, 15);
			KnowledgeItem newE = kb().createKnowledgeItem(trunk(), null, E_NAME, initialValues, KnowledgeItem.class);
			assertEquals("FlexAttribute not set", 15, newE.getAttributeValue(FLEX_ATTRIBUTE));
			assertEquals("Row attribute not set", "e1", newE.getAttributeValue(A1_NAME));
			tx.commit();
		}
	}

	public void testSettingIllegalFlexValue() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject e1 = newE("e1");
		createTx.commit();

		Transaction change1Tx = begin();
		e1.setAttributeValue(FLEX_ATTRIBUTE, e1);
		assertFalse("This is useless when '" + FLEX_ATTRIBUTE + "' is an knowledge attribute.",
			MetaObjectUtils.hasAttribute(e1.tTable(), FLEX_ATTRIBUTE));
		commit(change1Tx, "Setting KnowledgeItem as flexAttribute must fail.", true);

		Transaction change2Tx = begin();
		e1.setAttributeValue(FLEX_ATTRIBUTE, new Object());
		commit(change2Tx, "Setting random java object as flexAttribute must fail.", true);
	}

	/**
	 * Tests access of the {@link LifecycleAttributes}.
	 */
	public void testLifecycleAttributes() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject e1 = newE("e1");
		createTx.commit();
		long created = createTx.getCommitRevision().getDate();
		String creator = createTx.getCommitRevision().getAuthor();

		Transaction changeTx = begin();
		e1.setAttributeValue(FLEX_ATTRIBUTE, "value");
		changeTx.commit();
		long modified = changeTx.getCommitRevision().getDate();
		String modifier = changeTx.getCommitRevision().getAuthor();

		Set<String> allLifecycleAttr = new HashSet<>(LifecycleAttributes.LSANAMES);
		checkLifecycleAttribute(e1, LifecycleAttributes.CREATED, allLifecycleAttr, created, created + 1);
		checkLifecycleAttribute(e1, LifecycleAttributes.CREATOR, allLifecycleAttr, creator, "Any other");
		checkLifecycleAttribute(e1, LifecycleAttributes.MODIFIED, allLifecycleAttr, modified, modified + 1);
		checkLifecycleAttribute(e1, LifecycleAttributes.MODIFIER, allLifecycleAttr, modifier, "No one");
		assertEmpty("Not all lifecycle attributes checked.", true, allLifecycleAttr);
	}

	private void checkLifecycleAttribute(KnowledgeObject e1, String lifecycleAttribute,
			Collection<String> uncheckedAttributes, Object expectedValue, Object newValue) {
		try {
			assertEquals("LifecycleAttribute '" + lifecycleAttribute + "' has wrong value.", expectedValue,
				e1.getAttributeValue(lifecycleAttribute));
		} catch (DataObjectException ex) {
			fail("Access to lifecycle attribute '" + lifecycleAttribute + "' failed.", ex);
		}
		try {
			e1.setAttributeValue(lifecycleAttribute, newValue);
			fail("Setting lifecycle attribute '" + lifecycleAttribute + "' must fail.");
		} catch (Exception ex) {
			// expected
		}
		uncheckedAttributes.remove(lifecycleAttribute);
	}

	/**
	 * Test whether synchronized access or {@link java.util.concurrent.atomic.AtomicReference} to
	 * the global dynamic attributes is faster.
	 */
	public void testAccessGlobalValuePerformance() throws DataObjectException, InterruptedException {
		Transaction createTx = begin();
		final KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(FLEX_ATTRIBUTE, "flexValue");
		createTx.commit();
		
		// Trigger initialisation
		assertEquals("flexValue", e1.getAttributeValue(FLEX_ATTRIBUTE));

		int numberThreads = 20;
		final int numberAccessInThread = 20;
		int numberRuns = 20;
		class AccessGlobalFlexAttribute implements Execution {

			private int _numberAccess;

			public AccessGlobalFlexAttribute(int numberAccess) {
				_numberAccess = numberAccess;
			}

			@Override
			public void run() throws Exception {
				while (_numberAccess-- > 0) {
					assertEquals("flexValue", e1.getAttributeValue(FLEX_ATTRIBUTE));
				}
			}

		}
		StopWatch stopWatch = StopWatch.createStartedWatch();
		while (numberRuns-- > 0){
			parallelTest(numberThreads, new ExecutionFactory() {

				@Override
				public Execution createExecution(int id) {
					return new AccessGlobalFlexAttribute(numberAccessInThread);
				}
			});
		}
		stopWatch.stop();
		/* There is no difference between AtomicReference and synchronized access, but with
		 * synchronized access there is no need to create an additional object (the reference) for
		 * each item. */
		System.out.println("Needed " + StopWatch.toStringNanos(stopWatch.getElapsedNanos()));
	}

	/**
	 * Tests {@link KnowledgeItem#getAllAttributeNames()}
	 */
	public void testAllAttributeNames() throws DataObjectException {
		Transaction createTx = begin();
		final KnowledgeObject e1 = newE("e1");
		createTx.commit();

		final Set<String> globalE1Atts =
			Mappings.mapIntoSet(MetaObjectUtils.NAME_MAPPING, type(E_NAME).getAttributes());
		assertEquals(globalE1Atts, e1.getAllAttributeNames());

		Set<String> localE1Attrs = new HashSet<>(globalE1Atts);
		{
			Transaction rollbackTx = begin();
			assertEquals(localE1Attrs, e1.getAllAttributeNames());
			e1.setAttributeValue(FLEX_ATTRIBUTE, "flex");
			e1.setAttributeValue(FLEX_ATTRIBUTE_2, "flex2");
			localE1Attrs.add(FLEX_ATTRIBUTE);
			localE1Attrs.add(FLEX_ATTRIBUTE_2);
			assertEquals("Added FlexAttribute not contained", localE1Attrs, e1.getAllAttributeNames());
			inThread(new Execution() {

				@Override
				public void run() throws Exception {
					assertEquals("Dynamic value must not be visible in different thread.", globalE1Atts,
						e1.getAllAttributeNames());
				}
			});
			e1.setAttributeValue(FLEX_ATTRIBUTE, null);
			localE1Attrs.remove(FLEX_ATTRIBUTE);
			assertEquals("Removed FlexAttribute not contained", localE1Attrs, e1.getAllAttributeNames());
			rollbackTx.rollback();
		}

		assertEquals("FlexAttributes must be removed after rollback.", globalE1Atts, e1.getAllAttributeNames());

		// rollback -> only static attributes
		localE1Attrs = new HashSet<>(globalE1Atts);
		{
			Transaction commitTx = begin();
			e1.setAttributeValue(FLEX_ATTRIBUTE, "flex");
			e1.setAttributeValue(FLEX_ATTRIBUTE_2, "flex2");
			localE1Attrs.add(FLEX_ATTRIBUTE);
			localE1Attrs.add(FLEX_ATTRIBUTE_2);
			commitTx.commit();
		}

		assertEquals("FlexAttributes must contained after commit.", localE1Attrs, e1.getAllAttributeNames());
	}

	/**
	 * Tests {@link ChangeInspectable} on an deleted object.
	 * 
	 * @see TestDBKnowledgeBase#testJournalDelete()
	 */
	public void testJournalDelete() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(FLEX_ATTRIBUTE, "flex");
		ChangeInspectable ci = (ChangeInspectable) e1;
		tx1.commit();

		Transaction tx2 = begin();
		e1.setAttributeValue(FLEX_ATTRIBUTE, "flex_new");
		e1.setAttributeValue(FLEX_ATTRIBUTE_2, "flex2");
		e1.delete();
		ItemChange change = ci.getChange();
		Map<String, Object> values = change.getValues();
		assertEquals("e1", values.get(A1_NAME));
		assertEquals("flex", values.get(FLEX_ATTRIBUTE));
		assertFalse(values.containsKey(FLEX_ATTRIBUTE_2));
		tx2.commit();
	}

	/**
	 * Tests {@link ChangeInspectable} on a new object.
	 * 
	 * @see TestDBKnowledgeBase#testJournalCreate()
	 */
	public void testJournalCreate() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(FLEX_ATTRIBUTE, "flex");
		ChangeInspectable ci = (ChangeInspectable) e1;
		ItemChange change = ci.getChange();
		assertNotNull(change);
		Map<String, Object> values = change.getValues();
		assertEquals("e1", values.get(A1_NAME));
		assertEquals("flex", values.get(FLEX_ATTRIBUTE));
		tx1.commit();
	}

	/**
	 * Tests {@link ChangeInspectable} on a object with modified knowledge attributes and dynamic
	 * attributes.
	 */
	public void testJournalModifyFlexKO() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(FLEX_ATTRIBUTE, "flex");
		ChangeInspectable ci = (ChangeInspectable) e1;
		tx1.commit();

		Transaction tx2 = begin();
		e1.setAttributeValue(A1_NAME, "e1_new");
		e1.setAttributeValue(FLEX_ATTRIBUTE, "newFlex");
		e1.setAttributeValue(FLEX_ATTRIBUTE_2, "newFlex_2");
		ItemChange change = ci.getChange();
		assertNotNull(ci);
		assertInstanceof(change, ItemUpdate.class);
		ItemUpdate update = (ItemUpdate) change;
		Map<String, Object> values = update.getValues();
		assertEquals("e1_new", values.get(A1_NAME));
		assertEquals("newFlex", values.get(FLEX_ATTRIBUTE));
		assertEquals("newFlex_2", values.get(FLEX_ATTRIBUTE_2));
		Map<String, Object> oldValues = update.getOldValues();
		assertNotNull(oldValues);
		assertEquals("e1", oldValues.get(A1_NAME));
		assertEquals("flex", oldValues.get(FLEX_ATTRIBUTE));
		assertEquals(null, oldValues.get(FLEX_ATTRIBUTE_2));
		tx2.commit();
	}

	/**
	 * Tests {@link ChangeInspectable} on a object with modified dynamic attributes.
	 */
	public void testJournalModifyFlex() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(FLEX_ATTRIBUTE, "flex");
		ChangeInspectable ci = (ChangeInspectable) e1;
		tx1.commit();

		Transaction tx2 = begin();
		e1.setAttributeValue(FLEX_ATTRIBUTE, "newFlex");
		e1.setAttributeValue(FLEX_ATTRIBUTE_2, "newFlex_2");
		ItemChange change = ci.getChange();
		assertNotNull(ci);
		assertInstanceof(change, ItemUpdate.class);
		ItemUpdate update = (ItemUpdate) change;
		Map<String, Object> values = update.getValues();
		assertEquals("newFlex", values.get(FLEX_ATTRIBUTE));
		assertEquals("newFlex_2", values.get(FLEX_ATTRIBUTE_2));
		Map<String, Object> oldValues = update.getOldValues();
		assertNotNull(oldValues);
		assertEquals("flex", oldValues.get(FLEX_ATTRIBUTE));
		assertEquals(null, oldValues.get(FLEX_ATTRIBUTE_2));
		tx2.commit();
	}

	/**
	 * Tests {@link ChangeInspectable} on a object with modified dynamic attributes in differerent
	 * context.
	 */
	public void testJournalModifyFlexInDifferentContext() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(FLEX_ATTRIBUTE, "flex");
		final ChangeInspectable ci = (ChangeInspectable) e1;
		tx1.commit();

		Transaction tx2 = begin();
		e1.setAttributeValue(FLEX_ATTRIBUTE, "newFlex");
		inThread(new Execution() {

			@Override
			public void run() throws Exception {
				assertNull("Local flex modification must not be seen outside modification context.", ci.getChange());
			}
		});
		tx2.rollback();
	}

	/**
	 * Tests {@link ChangeInspectable} on a object with deleted flex attributes.
	 */
	public void testJournalDeleteFlex() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(FLEX_ATTRIBUTE, "flex");
		ChangeInspectable ci = (ChangeInspectable) e1;
		tx1.commit();

		Transaction tx2 = begin();
		e1.setAttributeValue(FLEX_ATTRIBUTE, null);
		ItemChange change = ci.getChange();
		assertNotNull(ci);
		assertInstanceof(change, ItemUpdate.class);
		ItemUpdate update = (ItemUpdate) change;
		Map<String, Object> values = update.getValues();
		assertEquals(null, values.get(FLEX_ATTRIBUTE));
		Map<String, Object> oldValues = update.getOldValues();
		assertNotNull(oldValues);
		assertEquals("flex", oldValues.get(FLEX_ATTRIBUTE));
		tx2.commit();
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

	public void testOnlyDynamicAttributeInNewObject() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		b1.setAttributeValue(FLEX_ATTRIBUTE, "flexValue");
		assertEquals("flexValue", b1.getAttributeValue(FLEX_ATTRIBUTE));
		rollback(tx);
	}

	public void testOnlyDynamicAttributeChanged() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		tx.commit();

		assertNull(b1.getAttributeValue(FLEX_ATTRIBUTE));

		Transaction changeTx = begin();
		b1.setAttributeValue(FLEX_ATTRIBUTE, "flexValue");
		assertEquals("flexValue", b1.getAttributeValue(FLEX_ATTRIBUTE));
		changeTx.commit();

		assertEquals("Flex value not persistent", "flexValue", b1.getAttributeValue(FLEX_ATTRIBUTE));
		// refetch to be able to resolve object
		refetchNode2();
		KnowledgeItem b1Node2 = node2Item(b1);
		assertEquals("Flex value not persistent", "flexValue", b1Node2.getAttributeValue(FLEX_ATTRIBUTE));
	}

	public void testDynamicAndStaticAttributes() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		tx.commit();

		assertNull(b1.getAttributeValue(FLEX_ATTRIBUTE));
		assertNull(b1.getAttributeValue(A2_NAME));

		Transaction changeTx = begin();
		b1.setAttributeValue(FLEX_ATTRIBUTE, "flexValue");
		b1.setAttributeValue(A2_NAME, "a2");
		assertEquals("flexValue", b1.getAttributeValue(FLEX_ATTRIBUTE));
		changeTx.commit();

		assertEquals("Flex value not persistent", "flexValue", b1.getAttributeValue(FLEX_ATTRIBUTE));
		// refetch to be able to resolve object
		refetchNode2();
		KnowledgeItem b1Node2 = node2Item(b1);
		assertEquals("Flex value not persistent", "flexValue", b1Node2.getAttributeValue(FLEX_ATTRIBUTE));
	}

	/**
	 * @see TestFlexWrapperCluster#testCheckChangeHistory()
	 */
	public void testChangeHistoric() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		tx.commit();

		Transaction changeTx = begin();
		b1.setAttributeValue(FLEX_ATTRIBUTE, "flexValue");
		changeTx.commit();

		KnowledgeItem historicB1 = HistoryUtils.getKnowledgeItem(changeTx.getCommitRevision(), b1);

		Transaction illegalCommit = begin();
		try {
			historicB1.setAttributeValue(FLEX_ATTRIBUTE, "newValue");
			fail("Must not allow modification of stable object.");
		} catch (IllegalStateException ex) {
			// expected
		}
		assertEquals("flexValue", b1.getAttributeValue(FLEX_ATTRIBUTE));
		illegalCommit.commit();
		assertEquals("flexValue", b1.getAttributeValue(FLEX_ATTRIBUTE));
	}

	/**
	 * @see TestFlexWrapperCluster#testSearch()
	 */
	public void testSearch() throws DataObjectException {
		SetExpression expr =
			filter(allOf(B_NAME), eqBinary(flex(MOPrimitive.STRING, FLEX_ATTRIBUTE), attribute(B_NAME, B1_NAME)));

		Transaction tx = begin();
		// attribute b1 and f1 are both null
		KnowledgeObject b1 = newB("b1");

		KnowledgeObject b2 = newB("b2");
		b2.setAttributeValue(B1_NAME, "v1");
		b2.setAttributeValue(FLEX_ATTRIBUTE, "v1");

		KnowledgeObject b3 = newB("b3");
		b3.setAttributeValue(B1_NAME, "v1");

		KnowledgeObject b4 = newB("b4");
		b4.setAttributeValue(FLEX_ATTRIBUTE, "v1");
		commit(tx);

		List<KnowledgeObject> koResult = kb().search(queryUnresolved(expr));

		assertEquals(set(b1, b2), toSet(koResult));
	}

	public void testRollbackChanged() throws DataObjectException {
		Transaction createTX = begin();
		KnowledgeObject b1 = newB("b1");
		createTX.commit();

		Transaction rollbackTx = begin();
		b1.setAttributeValue(FLEX_ATTRIBUTE, "flexValue");
		b1.delete();
		rollbackTx.rollback();

		assertTrue("Deletion was rolled back.", b1.isAlive());
		assertNull("Setting flex value was rolled back.", b1.getAttributeValue(FLEX_ATTRIBUTE));
	}

	public void testSetIntoDeletedObject() throws DataObjectException {
		Transaction createTX = begin();
		KnowledgeObject b1 = newB("b1");
		createTX.commit();

		Transaction rollbackTx = begin();
		b1.delete();

		try {
			b1.setAttributeValue(FLEX_ATTRIBUTE, "flexValue");
			fail("object is already deleted");
		} catch (IllegalStateException ex) {
			// expected
		}
		rollbackTx.commit();
	}

	/**
	 * @see TestFlexWrapperCluster#testDeleteWithLocalModificationsWithoutPersistentData()
	 */
	public void testDeleteWithLocalModificationsWithoutPersistentData() throws DataObjectException {
		Transaction createTX = begin();
		KnowledgeObject b1 = newB("b1");
		createTX.commit();

		Transaction deleteTx = begin();
		b1.setAttributeValue(FLEX_ATTRIBUTE, "flexValue");
		b1.delete();
		deleteTx.commit();
	}

	/**
	 * @see TestFlexWrapperCluster#testDeleteWithLocalModificationsWithPersistentData()
	 */
	public void testDeleteWithLocalModificationsWithPersistentData() throws DataObjectException {
		Transaction createTX = begin();
		KnowledgeObject b1 = newB("b1");
		createTX.commit();

		Transaction changeTx = begin();
		b1.setAttributeValue(FLEX_ATTRIBUTE_2, "flexValue");
		changeTx.commit();

		Transaction deleteTx = begin();
		b1.setAttributeValue(FLEX_ATTRIBUTE, "flexValue");
		b1.delete();
		deleteTx.commit();
	}

	public void testModificationThreadLocal() throws DataObjectException {
		Transaction createTX = begin();
		final KnowledgeObject b1 = newB("b1");
		b1.setAttributeValue(FLEX_ATTRIBUTE, "globalFlexValue");
		createTX.commit();

		Transaction slowerTx = begin();
		b1.setAttributeValue(FLEX_ATTRIBUTE, "flexValue");
		inThread(new Execution() {

			@Override
			public void run() throws Exception {
				Object globalFlexValue = b1.getAttributeValue(FLEX_ATTRIBUTE);
				assertEquals("Local modification must not be visible in different thread.", "globalFlexValue",
					globalFlexValue);
			}
		});
		inThread(new Execution() {

			@Override
			public void run() throws Exception {
				Transaction fasterTx = begin();
				b1.setAttributeValue(FLEX_ATTRIBUTE, "other flexValue");
				commit(fasterTx);
			}
		});
		commit(slowerTx, "Concurrent modification in differernt thread.", true);
		assertEquals("other flexValue", b1.getAttributeValue(FLEX_ATTRIBUTE));
	}

	/**
	 * @see TestFlexWrapperCluster#testChangeCheckFlex()
	 */
	public void testNoChangeNoCommit() throws DataObjectException {
		Transaction initialTX = begin();
		KnowledgeObject b1 = newB("b1");
		b1.setAttributeValue(FLEX_ATTRIBUTE, "f1");
		commit(initialTX);

		Revision r2 = initialTX.getCommitRevision();

		Transaction noopTx = begin();
		b1.setAttributeValue(FLEX_ATTRIBUTE, "f1");
		commit(noopTx);

		assertNull("Ticket #884: Revision was created without actual changes.", noopTx.getCommitRevision());
		Revision r2again = HistoryUtils.getLastRevision();

		assertEquals("Ticket #884: Revision was created without actual changes.", r2, r2again);
	}

	/**
	 * @see TestFlexWrapperCluster#testReadRetry()
	 */
	public void testReadRetry() throws DataObjectException, SQLException {
		Transaction tx = begin();
		final KnowledgeObject b1 = newB("b1");
		b1.setAttributeValue(FLEX_ATTRIBUTE, "f1");
		commit(tx);

		// refetch to be able to resolve object
		refetchNode2();

		DBKnowledgeBaseClusterTestSetup.simulateConnectionBreakdownNode2();

		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				KnowledgeItem b1Node2 = node2Item(b1);
				assertEquals("f1", b1Node2.getAttributeValue(FLEX_ATTRIBUTE));
			}
		});
	}

	/**
	 * @see TestFlexWrapperCluster#testRollback()
	 */
	public void testRollbackByCommitFailure() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject b1 = newB("b1");
		b1.setAttributeValue(FLEX_ATTRIBUTE, "f1");
		commit(createTx);

		Transaction failingTx = begin();
		b1.setAttributeValue(FLEX_ATTRIBUTE, null);
		assertEquals(null, b1.getAttributeValue(FLEX_ATTRIBUTE));
		kb().addCommittable(COMMIT_FAILURE);
		commit(failingTx, true);

		assertEquals("Value was not rolled back.", "f1", b1.getAttributeValue(FLEX_ATTRIBUTE));
	}

	private class SimpleSetup {

		KnowledgeObject _b1;

		KnowledgeItem _b1Node2;

		public SimpleSetup() throws DataObjectException {
			Transaction createTX = begin();
			_b1 = newB("b1");
			_b1.setAttributeValue(FLEX_ATTRIBUTE, "flex");
			createTX.commit();
			// refetch to be able to resolve object
			refetchNode2();
			_b1Node2 = node2Item(_b1);

			assertEquals("FlexAttribute was set on other node.", "flex", _b1Node2.getAttributeValue(FLEX_ATTRIBUTE));
		}
	}

	/**
	 * @see TestFlexWrapperCluster#testFlexCreationRefetch()
	 */
	public void testFlexCreationRefetch() throws DataObjectException {
		SimpleSetup setup = new SimpleSetup();

		Transaction changeTX = begin();
		setup._b1.setAttributeValue(FLEX_ATTRIBUTE_2, "flex");
		changeTX.commit();

		refetchNode2();

		assertEquals("FlexAttributes not refetched", "flex", setup._b1Node2.getAttributeValue(FLEX_ATTRIBUTE_2));
	}

	/**
	 * @see TestFlexWrapperCluster#testFlexModificationRefetch()
	 */
	public void testFlexModificationRefetch() throws DataObjectException {
		SimpleSetup setup = new SimpleSetup();

		Transaction changeTX = begin();
		setup._b1.setAttributeValue(FLEX_ATTRIBUTE, "flex_new");
		changeTX.commit();

		refetchNode2();

		assertEquals("FlexAttributes not refetched", "flex_new", setup._b1Node2.getAttributeValue(FLEX_ATTRIBUTE));
	}

	/**
	 * @see TestFlexWrapperCluster#testFlexDeletionRefetch()
	 */
	public void testFlexDeletionRefetch() throws DataObjectException {
		SimpleSetup setup = new SimpleSetup();

		Transaction changeTX = begin();
		setup._b1.setAttributeValue(FLEX_ATTRIBUTE, null);
		changeTX.commit();

		refetchNode2();

		assertEquals("FlexAttributes not refetched", null, setup._b1Node2.getAttributeValue(FLEX_ATTRIBUTE));
	}

	/**
	 * @see TestFlexWrapperCluster#testDetectLostUpdateAddConcurrent()
	 */
	public void testDetectLostUpdateAddConcurrent() throws DataObjectException {
		SimpleSetup setup = new SimpleSetup();

		{
			Transaction tx1 = begin();
			Transaction tx2 = beginNode2();

			// Pseudo concurrently modify.
			setup._b1.setAttributeValue(FLEX_ATTRIBUTE_2, "f2node1");
			setup._b1Node2.setAttributeValue(FLEX_ATTRIBUTE_2, "f2node2");

			// Pseudo concurrently commit.
			commit(tx1);
			commitNode2Fail(tx2);
		}

		assertEquals("f2node1", setup._b1Node2.getAttributeValue(FLEX_ATTRIBUTE_2));
	}

	/**
	 * @see TestFlexWrapperCluster#testDetectLostUpdateDeleteModify()
	 */
	public void testDetectLostUpdateDeleteModify() throws DataObjectException {
		SimpleSetup setup = new SimpleSetup();

		{
			Transaction tx1 = begin();
			Transaction tx2 = beginNode2();

			// Pseudo concurrently delete and modify.
			setup._b1.delete();
			setup._b1Node2.setAttributeValue(FLEX_ATTRIBUTE_2, "f2node2");

			// Pseudo concurrently commit.
			commit(tx1);
			commitNode2Fail(tx2);
		}

		assertFalse(setup._b1.isAlive());
		assertFalse(setup._b1Node2.isAlive());
	}

	/**
	 * @see TestFlexWrapperCluster#testDetectLostUpdateModifyDelete()
	 */
	public void testDetectLostUpdateModifyDelete() throws DataObjectException {
		SimpleSetup setup = new SimpleSetup();

		{
			Transaction tx1 = begin();
			Transaction tx2 = beginNode2();

			// Pseudo concurrently delete and modify.
			setup._b1.setAttributeValue(FLEX_ATTRIBUTE_2, "f2node2");
			setup._b1Node2.delete();

			// Pseudo concurrently commit.
			commit(tx1);
			commitNode2Fail(tx2);
		}

		assertTrue(setup._b1.isAlive());
		assertTrue(setup._b1Node2.isAlive());
	}

	/**
	 * @see TestFlexWrapperCluster#testDetectLostUpdateFlexKO()
	 */
	public void testDetectLostUpdateFlexKO() throws DataObjectException {
		SimpleSetup setup = new SimpleSetup();

		{
			Transaction tx = begin();
			setup._b1.setAttributeValue(FLEX_ATTRIBUTE_2, "f2node1");
			commit(tx);
		}

		{
			Transaction tx = beginNode2();
			setup._b1Node2.setAttributeValue(A2_NAME, "a2node2");
			commitNode2Fail(tx);
		}

		assertEquals(null, setup._b1Node2.getAttributeValue(A2_NAME));
	}

	/**
	 * @see TestFlexWrapperCluster#testDetectLostUpdateKOFlex()
	 */
	public void testDetectLostUpdateKOFlex() throws DataObjectException {
		SimpleSetup setup = new SimpleSetup();

		{
			Transaction tx = begin();
			setup._b1.setAttributeValue(A2_NAME, "a2node1");
			commit(tx);
		}
		{
			Transaction tx = beginNode2();
			setup._b1Node2.setAttributeValue(FLEX_ATTRIBUTE_2, "f2node2");
			commitNode2Fail(tx);
		}

		assertEquals(null, setup._b1Node2.getAttributeValue(FLEX_ATTRIBUTE_2));
	}

	/**
	 * @see TestFlexWrapperCluster#testDetectLostUpdateFlexAdd()
	 */
	public void testDetectLostUpdateFlexAdd() throws DataObjectException {
		SimpleSetup setup = new SimpleSetup();

		{
			Transaction tx = begin();
			setup._b1.setAttributeValue(FLEX_ATTRIBUTE_2, "f2node1");
			commit(tx);
		}
		{
			Transaction tx = beginNode2();
			setup._b1Node2.setAttributeValue(FLEX_ATTRIBUTE_2, "f2node2");
			commitNode2Fail(tx);
		}

		assertEquals("f2node1", setup._b1Node2.getAttributeValue(FLEX_ATTRIBUTE_2));
	}

	/**
	 * @see TestFlexWrapperCluster#testDetectLostUpdateFlexModify()
	 */
	public void testDetectLostUpdateFlexModify() throws DataObjectException {
		SimpleSetup setup = new SimpleSetup();

		{
			Transaction tx = begin();
			setup._b1.setAttributeValue(FLEX_ATTRIBUTE, "f1node1");
			commit(tx);
		}
		{
			Transaction tx = beginNode2();
			setup._b1Node2.setAttributeValue(FLEX_ATTRIBUTE, "f1node2");
			commitNode2Fail(tx);
		}

		assertEquals("f1node1", setup._b1Node2.getAttributeValue(FLEX_ATTRIBUTE));
	}

	/**
	 * @see TestFlexWrapperCluster#testDetectLostUpdateFlexModifyDelete()
	 */
	public void testDetectLostUpdateFlexModifyDelete() throws DataObjectException {
		SimpleSetup setup = new SimpleSetup();

		{
			Transaction tx = begin();
			setup._b1.setAttributeValue(FLEX_ATTRIBUTE, "f1node1");
			commit(tx);
		}
		{
			Transaction tx = beginNode2();
			setup._b1Node2.setAttributeValue(FLEX_ATTRIBUTE, null);
			commitNode2Fail(tx);
		}

		assertEquals("f1node1", setup._b1Node2.getAttributeValue(FLEX_ATTRIBUTE));
	}

	/**
	 * @see TestFlexWrapperCluster#testDetectLostUpdateFlexDeleteModify()
	 */
	public void testDetectLostUpdateFlexDeleteModify() throws DataObjectException {
		SimpleSetup setup = new SimpleSetup();

		{
			Transaction tx = begin();
			setup._b1.setAttributeValue(FLEX_ATTRIBUTE, null);
			commit(tx);
		}
		{
			Transaction tx = beginNode2();
			setup._b1Node2.setAttributeValue(FLEX_ATTRIBUTE, "f1node1");
			commitNode2Fail(tx);
		}

		assertEquals(null, setup._b1Node2.getAttributeValue(FLEX_ATTRIBUTE));
	}

	/**
	 * @see TestFlexWrapperCluster#testBranching()
	 */
	public void testBranching() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		b1.setAttributeValue(FLEX_ATTRIBUTE, "flex");
		commit(tx);

		Branch branch =
			HistoryUtils.createBranch(HistoryUtils.getContextBranch(), HistoryUtils.getLastRevision(), types(B_NAME));

		KnowledgeItem b2 = HistoryUtils.getKnowledgeItem(branch, b1);
		assertEquals("b1", b2.getAttributeValue(A1_NAME));
		assertEquals("flex", b2.getAttributeValue(FLEX_ATTRIBUTE));
	}

	public void testRealivedObjectEmptyFlexAttributes() throws DataObjectException {
		TLID id = kb().createID();

		Transaction createTx = begin();
		KnowledgeObject b1 = kb().createKnowledgeObject(id, B_NAME);
		// A1 is mandatory
		setA1(b1, "b1");
		b1.setAttributeValue(FLEX_ATTRIBUTE, "flex");
		ObjectKey b1Key = b1.tId();
		commit(createTx);

		Transaction deleteTx = begin();
		b1.delete();
		deleteTx.commit();

		Transaction createTx2 = begin();
		KnowledgeObject b1_new = kb().createKnowledgeObject(id, B_NAME);
		// A1 is mandatory
		setA1(b1_new, "b1");
		assertEquals(b1Key, b1_new.tId());
		createTx2.commit();

		assertNull("Object was deleted, therefore also flex attributes must be removed.",
			b1_new.getAttributeValue(FLEX_ATTRIBUTE));
	}

	/**
	 * @see TestFlexWrapper#testBinaryData()
	 */
	public void testBinaryData() throws DataObjectException, IOException {
		BinaryData smallData = new TestingBinaryData(42, 15L);
		BinaryData longData = new TestingBinaryData(42, BinaryDataFactory.MAX_MEMORY_SIZE);

		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		b1.setAttributeValue(FLEX_ATTRIBUTE, smallData);
		b1.setAttributeValue(FLEX_ATTRIBUTE_2, longData);
		commit(tx);

		assertEquals(smallData, b1.getAttributeValue(FLEX_ATTRIBUTE));
		assertEquals(longData, b1.getAttributeValue(FLEX_ATTRIBUTE_2));

		byte[] smallText = randomString(BinaryDataFactory.MAX_MEMORY_SIZE / 2, true, true, false, false)
			.getBytes(StringServices.CHARSET_UTF_8);
		BinaryData smallTextData =
			BinaryDataFactory.createBinaryData(new ByteArrayInputStream(smallText), smallText.length, "plain/text");
		byte[] largeText = randomString(BinaryDataFactory.MAX_MEMORY_SIZE * 2, true, true, false, false).getBytes();
		BinaryData largeTextData =
			BinaryDataFactory.createBinaryData(new ByteArrayInputStream(largeText), largeText.length, "plain/text");
		Transaction tx2 = begin();
		b1.setAttributeValue(FLEX_ATTRIBUTE, smallTextData);
		b1.setAttributeValue(FLEX_ATTRIBUTE_2, largeTextData);
		commit(tx2);

		assertEquals(smallTextData, b1.getAttributeValue(FLEX_ATTRIBUTE));
		assertEquals(smallTextData.getContentType(),
			((BinaryData) b1.getAttributeValue(FLEX_ATTRIBUTE)).getContentType());

		assertEquals(largeTextData, b1.getAttributeValue(FLEX_ATTRIBUTE_2));
		assertEquals(largeTextData.getContentType(),
			((BinaryData) b1.getAttributeValue(FLEX_ATTRIBUTE_2)).getContentType());

	}

	/**
	 * Tests that the binary data is available after commit, also if original binary data is not
	 * accessible.
	 */
	public void testLostBinaryData() throws DataObjectException {
		final ObjectFlag<Boolean> dataBroken = new ObjectFlag<>(Boolean.FALSE);
		final BinaryData longData = new TestingBinaryData(42, BinaryDataFactory.MAX_MEMORY_SIZE);
		BinaryData brokenProxy = new BinaryDataProxy(longData) {

			@Override
			protected BinaryData createBinaryData() throws IOException {
				return longData;
			}

			@Override
			public InputStream getStream() throws IOException {
				if (dataBroken.get()) {
					throw new IOException("No Data available");
				}
				return super.getStream();
			}

			@Override
			public String getName() {
				return longData.getName();
			}
		};

		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		b1.setAttributeValue(FLEX_ATTRIBUTE, brokenProxy);
		assertEquals(longData, b1.getAttributeValue(FLEX_ATTRIBUTE));
		commit(tx);

		dataBroken.set(Boolean.TRUE);
		assertEquals(longData, b1.getAttributeValue(FLEX_ATTRIBUTE));
	}

	/**
	 * Tests that dynamic attributes can not be accessed when the object is deleted.
	 */
	public void testNoFlexAttributeAccessOnDeletedObjects() throws DataObjectException {
		Transaction tx = begin();
		final KnowledgeObject b1 = newB("b1");
		b1.setAttributeValue(FLEX_ATTRIBUTE, "flex");
		commit(tx);

		Transaction deleteTX = begin();
		b1.delete();

		try {
			b1.getAttributeValue(FLEX_ATTRIBUTE);
			fail("Must not be able to access flex value of deleted object.");
		} catch (DeletedObjectAccess ex) {
			// expected
		}
		try {
			b1.setAttributeValue(FLEX_ATTRIBUTE, "flex2");
			fail("Must not be able to change flex value of deleted object.");
		} catch (DeletedObjectAccess ex) {
			// expected
		}
		inThread(new Execution() {

			@Override
			public void run() throws Exception {
				assertEquals("Deletion in other thread must not affect fetching flex value.", "flex",
					b1.getAttributeValue(FLEX_ATTRIBUTE));
			}
		});
		commit(deleteTX);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestFlexAttributes}.
	 */
	@SuppressWarnings("unused")
	public static Test suite() {
		if (!true) {
			return runOneTest(TestFlexAttributes.class, "testCreateObjectWithFlexAttributes");
		}
		return suite(TestFlexAttributes.class);
	}

}

