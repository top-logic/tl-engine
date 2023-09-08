/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.service.db2.DBKnowledgeAssociation.*;
import static test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.AObj.*;
import static test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.AObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.TLID;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemCreation;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.KnowledgeEvent;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.Committable;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.FlexData;
import com.top_logic.knowledge.service.db2.ItemEventReader;
import com.top_logic.knowledge.service.db2.OrderedItemEventReader;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;

/**
 * Test class for {@link ItemEventReader}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestItemEventReader extends AbstractDBKnowledgeBaseClusterTest {

	public void testMultipleUpdate() throws SQLException {
		final BObj b1;
		final ObjectBranchId b1ID;
		Revision r1;
		{
			/* Create an Object which is modified later */
			Transaction tx = begin();
			b1 = newBObj("b1");
			tx.commit();
			b1ID = getObjectID(b1);
			r1 = tx.getCommitRevision();
		}
		{
			Transaction modify1 = begin();
			b1.setA1("b1_new");
			modify1.commit();
		}
		{
			Transaction modify2 = begin();
			b1.setA2("b1_a2");
			modify2.commit();
		}
		{
			Transaction modify3 = begin();
			b1.setF2("flex");
			modify3.commit();
		}
		{
			Transaction delete = begin();
			b1.tDelete();
			delete.commit();
		}

		ItemEventReader r = detailedEventReader(r1, lastRevision());
		try {
			ObjectCreation expectedCreate = new ObjectCreation(r1.getCommitNumber(), b1ID);
			expectedCreate.setValue(A1_NAME, null, "b1");
			expectedCreate.setValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, null, r1.getCommitNumber());
			assertEquals(expectedCreate, r.readEvent());

			ItemUpdate expectedUpdate1 = new ItemUpdate(r1.getCommitNumber() + 1, b1ID, true);
			expectedUpdate1.setValue(A1_NAME, "b1", "b1_new");
			assertEquals(expectedUpdate1, r.readEvent());

			ItemUpdate expectedUpdate2 = new ItemUpdate(r1.getCommitNumber() + 2, b1ID, true);
			expectedUpdate2.setValue(A2_NAME, null, "b1_a2");
			assertEquals(expectedUpdate2, r.readEvent());

			ItemUpdate expectedUpdate3 = new ItemUpdate(r1.getCommitNumber() + 3, b1ID, true);
			expectedUpdate3.setValue(F2_NAME, null, "flex");
			assertEquals(expectedUpdate3, r.readEvent());

			ItemDeletion delete = new ItemDeletion(r1.getCommitNumber() + 4, b1ID);
			delete.setValue(A1_NAME, "b1_new");
			delete.setValue(A2_NAME, "b1_a2");
			delete.setValue(F2_NAME, "flex");
			delete.setValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, r1.getCommitNumber());
			assertEquals(delete, r.readEvent());

			assertNull(r.readEvent());
		} finally {
			r.close();
		}
	}

	/**
	 * Tests that an object which was killed and later rose from the dead has different values as
	 * before.
	 */
	public void testTicket5292() throws SQLException {
		final TLID id = kb().createID();
		
		final BObj b1;
		final ObjectBranchId b1ID;
		Revision r1;
		{
			/* Create an Object which is killed later */
			final Transaction tx = begin();
			b1 = newBObj("b1", id);
			tx.commit();
			b1ID = getObjectID(b1);
			r1 = tx.getCommitRevision();
		}
		
		{
			/* kill the created Object */
			final Transaction tx = begin();
			b1.tDelete();
			tx.commit();
		}

		final ObjectBranchId b1RealivedID;
		final long b1RealivedRev;
		final BObj b1Realived;
		{
			/* Create an Object with the same id */
			final Transaction tx = begin();
			b1Realived = newBObj("b2", id);
			tx.commit();
			b1RealivedID = getObjectID(b1Realived);
			b1RealivedRev = tx.getCommitRevision().getCommitNumber();
		}
		assertEquals("Realived object with different ID.", b1ID, b1RealivedID);
		{
			/* Create an Object with the same id */
			final Transaction changeTx = begin();
			b1Realived.setA2("b2_a2");
			b1Realived.setA1("b2_new");
			changeTx.commit();

			final Transaction deleteTx = begin();
			b1Realived.tDelete();
			deleteTx.commit();
		}

		ItemEventReader r = detailedEventReader(r1, lastRevision());

		try {
			{
				final ItemEvent createEvt = r.readEvent();
				assertInstanceof(createEvt, ObjectCreation.class);
				assertEquals(b1ID, createEvt.getObjectId());
				assertEquals("b1", ((ObjectCreation) createEvt).getValues().get(A1_NAME));
			}
			{
				final ItemEvent deleteEvt = r.readEvent();
				assertInstanceof(deleteEvt, ItemDeletion.class);
				ItemDeletion deletion = (ItemDeletion) deleteEvt;
				assertEquals(b1ID, deletion.getObjectId());
				assertEquals("b1", deletion.getValues().get(A1_NAME));
			}
			{
				final ItemEvent realiveEvt;
				try {
					realiveEvt = r.readEvent();
				} catch (AssertionError ae) {
					throw fail("Ticket #5292: Old values must be null", ae);
				}
				assertInstanceof(realiveEvt, ObjectCreation.class);
				assertEquals(b1RealivedID, realiveEvt.getObjectId());
				assertEquals("b2", ((ObjectCreation) realiveEvt).getValues().get(A1_NAME));
			}
			{
				ItemEvent evt = r.readEvent();
				assertInstanceof(evt, ItemUpdate.class);
				ItemUpdate updateEvt = (ItemUpdate) evt;
				Map<String, Object> expectedValues =
					new MapBuilder<String, Object>().put(A1_NAME, "b2_new").put(A2_NAME, "b2_a2").toMap();
				assertEquals(expectedValues, updateEvt.getValues());
			}
			{
				ItemEvent evt = r.readEvent();
				assertInstanceof(evt, ItemDeletion.class);
				ItemDeletion deleteEvt = (ItemDeletion) evt;
				assertEquals(b1RealivedID, deleteEvt.getObjectId());
				Map<String, Object> expectedValues =
					new MapBuilder<String, Object>().put(A1_NAME, "b2_new").put(A2_NAME, "b2_a2")
						.put(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, b1RealivedRev).toMap();
				assertEquals(expectedValues, deleteEvt.getValues());
			}
			assertNull(r.readEvent());
		} finally {
			r.close();
		}
	}

	/**
	 * This method tests that the map which is used to detect changes of values in different
	 * revisions for the same object is reset correctly.
	 * 
	 * Currently there is a bug that the map is not reset, if there is a new object and the first
	 * event for that object is an update or creation (which is an update from <code>null</code> to
	 * a different value) of an flex attribute. If the previous reported object has the same flex
	 * attribute with the same value, then the flex attribute value of the new object is
	 * <code>null</code>.
	 * 
	 * See also Ticket 5154
	 */
	public void testTicket5154() throws SQLException, DataObjectException {
		BObj b1;
		BObj b2;
		{
			/* Set up the base values. Suppose the row 'x' in the object table and the row 'y' in
			 * the flex table are written */
			final Transaction tx = begin();
			b1 = newBObj("b1");
			b1.setF1("flexValue1");
			b2 = newBObj("b2");
			b2.setF1("flexValue2");
			tx.commit();
		}
		{
			/* Ping the object table to ensure that the reader do not find the row 'x' in the object
			 * table */
			final Transaction tx = begin();
			b1.setA2("b1");
			b2.setA2("b2");
			tx.commit();
		}

		// Reader found event for the actions done from here on

		Revision r1;
		{
			/* Change flex attributes to ensure that the reader reads the row 'y' in the flex table
			 * to determine change of value */
			final Transaction tx = begin();
			b1.setF1("flexValue1_new");
			b2.setF1("flexValue2_new");
			tx.commit();
			r1 = tx.getCommitRevision();
		}

		/* Flex attribute must have the <b>same</b> value, to find bug */
		String SAME_FLEX_VALUE = "same flex value";
		{
			final Transaction tx = begin();
			b1.setF2(SAME_FLEX_VALUE);
			b2.setF2(SAME_FLEX_VALUE);
			tx.commit();
		}

		final List<ItemEvent> allEvents =
			new ArrayList<>(TestKnowledgeEvent.getAllEvents(detailedEventReader(r1, lastRevision())));
		Collections.sort(allEvents, KnowledgeEvent.RevisionOrder.ASCENDING_INSTANCE);
		assertEquals(4, allEvents.size());

		final ItemEvent firstIrrelevantFlexUpdate = allEvents.get(0);
		assertInstanceof(firstIrrelevantFlexUpdate, ItemUpdate.class);
		final Object irrelevantValue = ((ItemUpdate) firstIrrelevantFlexUpdate).getValues().get(F1_NAME);
		final ItemEvent secondIrrelevantFlexUpdate = allEvents.get(1);
		assertInstanceof(secondIrrelevantFlexUpdate, ItemUpdate.class);
		final Object secondIrrelevantValue = ((ItemUpdate) secondIrrelevantFlexUpdate).getValues().get(F1_NAME);
		assertEquals(set("flexValue1_new", "flexValue2_new"), set(irrelevantValue, secondIrrelevantValue));

		final ItemEvent firstRelevantFlexUpdate = allEvents.get(2);
		assertNotNull("Ticket #5154: Flex updates are suppressed", firstRelevantFlexUpdate);
		assertInstanceof(firstRelevantFlexUpdate, ItemUpdate.class);
		assertEquals(SAME_FLEX_VALUE, ((ItemUpdate) firstRelevantFlexUpdate).getValues().get(F2_NAME));
		final ItemEvent secondRelevantFlexUpdate = allEvents.get(3);
		assertNotNull("Ticket #5154: Flex updates are suppressed", secondRelevantFlexUpdate);
		assertInstanceof(secondRelevantFlexUpdate, ItemUpdate.class);
		assertEquals(SAME_FLEX_VALUE, ((ItemUpdate) secondRelevantFlexUpdate).getValues().get(F2_NAME));
	}

	public void testFlexUpdateWithoutObjectTouch() throws DataObjectException, SQLException {
		final KnowledgeObject ko;
		{
			final Transaction tx = begin();
			ko = newC("c1");
			tx.commit();
		}
		{
			// produce traffic
			final Transaction tx = begin();
			KnowledgeObject ko2 = newD("d1");
			tx.commit();
			for (int i = 0; i < 35; i++) {
				final Transaction begin = begin();
				ko2.setAttributeValue(A2_NAME, "a2_" + i);
				begin.commit();
			}
		}
		{
			final MyCommitable myCommitable = new MyCommitable(ko);
			final DBKnowledgeBase kb = kb();
			kb.addCommittable(myCommitable);
			try {
				final Transaction tx = begin();
				myCommitable.getModifications().setAttributeValue("flex", "flexValue");
				tx.commit();
			} finally {
				kb.removeCommittable(myCommitable);
			}
		}

		OrderedItemEventReader reader =
			new OrderedItemEventReader(kb(), true, 1, kb().getHistoryManager().getLastRevision() + 1, 10,
				Collections.singleton(C_NAME), null, KnowledgeEvent.RevisionOrder.ASCENDING_INSTANCE);

		try {
			final ItemEvent createEvt = reader.readEvent();
			assertInstanceof(createEvt, ItemCreation.class);
			assertEquals(getObjectID(ko), createEvt.getObjectId());

			final ItemEvent flexUpdateEvt = reader.readEvent();
			assertNotNull("FlexUpdate not reported", flexUpdateEvt);
			assertInstanceof(flexUpdateEvt, ItemUpdate.class);
			assertEquals("flexValue", ((ItemUpdate) flexUpdateEvt).getValues().get("flex"));
		} finally {
			reader.close();
		}
	}

	private static class MyCommitable implements Committable {

		private final KnowledgeObject _ko;

		private FlexData _modifications;

		MyCommitable(KnowledgeObject ko) {
			_ko = ko;
			_modifications = _ko.getFlexDataManager().load(_ko.getKnowledgeBase(), _ko.tId(), true);
		}

		FlexData getModifications() {
			return _modifications;
		}

		@Override
		public boolean prepare(CommitContext aContext) {
			return _ko.getFlexDataManager().store(_ko.tId(), _modifications, aContext);
		}

		@Override
		public boolean prepareDelete(CommitContext aContext) {
			return _ko.getFlexDataManager().delete(_ko.tId(), aContext);
		}

		@Override
		public boolean commit(CommitContext aContext) {
			return true;
		}

		@Override
		public boolean rollback(CommitContext aContext) {
			return true;
		}

		@Override
		public void complete(CommitContext aContext) {
			// nothing to do here
		}

	}

	public void testCyclicReferenceSameRevision() throws DataObjectException, SQLException {
		final KnowledgeObject source1;
		final KnowledgeObject source2;
		final Revision r1;
		{
			// create source and add reference in same revision
			final Transaction tx = begin();
			source1 = newE("source1");
			source2 = newE("source2");
			setReference(source1, source2, !MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
			setReference(source2, source1, !MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
			tx.commit();
			r1 = tx.getCommitRevision();
		}

		final ItemEventReader eventReader1 = detailedEventReader(r1, r1);
		final HashSet<ItemEvent> readEvents1 = TestKnowledgeEvent.getAllEvents(eventReader1);
		final ObjectCreation createSource1Evt = new ObjectCreation(r1.getCommitNumber(), getObjectID(source1));
		createSource1Evt.setValue(A1_NAME, null, "source1");
		createSource1Evt.setValue(REFERENCE_POLY_CUR_LOCAL_NAME, null, source2.tId());
		createSource1Evt.setValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, null, source1.getCreateCommitNumber());
		final ObjectCreation createSource2Evt = new ObjectCreation(r1.getCommitNumber(), getObjectID(source2));
		createSource2Evt.setValue(A1_NAME, null, "source2");
		createSource2Evt.setValue(REFERENCE_POLY_CUR_LOCAL_NAME, null, source1.tId());
		createSource2Evt.setValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, null, source2.getCreateCommitNumber());
		assertEquals(set(createSource1Evt, createSource2Evt), readEvents1);
	}

	public void testReferenceToSelf() throws DataObjectException, SQLException {
		final KnowledgeObject source1;
		final Revision r1;
		{
			// create source and add reference in same revision
			final Transaction tx = begin();
			source1 = newE("source1");
			setReference(source1, source1, !MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
			tx.commit();
			r1 = tx.getCommitRevision();
		}

		final ItemEventReader eventReader1 = detailedEventReader(r1, r1);
		final HashSet<ItemEvent> readEvents1 = TestKnowledgeEvent.getAllEvents(eventReader1);
		final ObjectCreation createSource1Evt = new ObjectCreation(r1.getCommitNumber(), getObjectID(source1));
		createSource1Evt.setValue(A1_NAME, null, "source1");
		createSource1Evt.setValue(REFERENCE_POLY_CUR_LOCAL_NAME, null, source1.tId());
		createSource1Evt.setValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, null, source1.getCreateCommitNumber());
		assertEquals(set(createSource1Evt), readEvents1);

		final KnowledgeObject source2;
		final Revision r2;
		final Revision r3;
		{
			// create source and add reference in different revision
			final Transaction createObject = begin();
			source2 = newE("source2");
			createObject.commit();
			r2 = createObject.getCommitRevision();

			final Transaction addReference = begin();
			setReference(source2, source2, !MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
			addReference.commit();
			r3 = addReference.getCommitRevision();
		}

		final ItemEventReader eventReader2 = detailedEventReader(r2, r3);
		final HashSet<ItemEvent> readEvents2 = TestKnowledgeEvent.getAllEvents(eventReader2);
		final ObjectCreation createSource2Evt = new ObjectCreation(r2.getCommitNumber(), getObjectID(source2));
		createSource2Evt.setValue(A1_NAME, null, "source2");
		createSource2Evt.setValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, null, source2.getCreateCommitNumber());
		final ItemChange addReferenceEvt = new ItemUpdate(r3.getCommitNumber(), getObjectID(source2), true);
		addReferenceEvt.setValue(REFERENCE_POLY_CUR_LOCAL_NAME, null, source2.tId());
		assertEquals(set(createSource2Evt, addReferenceEvt), readEvents2);

	}

	public void testReferenceObject() throws DataObjectException, SQLException {
		final KnowledgeItem reference;
		{
			final Transaction tx = begin();
			reference = newD("reference");
			tx.commit();
		}

		final KnowledgeObject source1;
		final Revision r1;
		{
			// create source and add reference in same revision
			final Transaction tx = begin();
			source1 = newE("source1");
			setReference(source1, reference, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		final ItemEventReader eventReader1 = detailedEventReader(r1, r1);
		final HashSet<ItemEvent> readEvents1 = TestKnowledgeEvent.getAllEvents(eventReader1);
		final ObjectCreation createSource1Evt = new ObjectCreation(r1.getCommitNumber(), getObjectID(source1));
		createSource1Evt.setValue(A1_NAME, null, "source1");
		createSource1Evt.setValue(REFERENCE_MONO_CUR_LOCAL_NAME, null, reference.tId());
		createSource1Evt.setValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, null, source1.getCreateCommitNumber());
		assertEquals(set(createSource1Evt), readEvents1);

		final KnowledgeObject source2;
		final Revision r2;
		final Revision r3;
		{
			// create source and add reference in different revision
			final Transaction txCreateSource = begin();
			source2 = newE("source2");
			txCreateSource.commit();
			r2 = txCreateSource.getCommitRevision();

			final Transaction txAddReference = begin();
			setReference(source2, reference, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
			txAddReference.commit();
			r3 = txAddReference.getCommitRevision();
		}
		final ItemEventReader eventReader2 = detailedEventReader(r2, r3);
		final HashSet<ItemEvent> readEvents2 = TestKnowledgeEvent.getAllEvents(eventReader2);
		final ObjectCreation createSource2Evt = new ObjectCreation(r2.getCommitNumber(), getObjectID(source2));
		createSource2Evt.setValue(A1_NAME, null, "source2");
		createSource2Evt.setValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, null, source2.getCreateCommitNumber());
		final ItemChange addReferenceEvt = new ItemUpdate(r3.getCommitNumber(), getObjectID(source2), true);
		addReferenceEvt.setValue(REFERENCE_MONO_CUR_LOCAL_NAME, null, reference.tId());
		assertEquals(set(createSource2Evt, addReferenceEvt), readEvents2);

	}

	public void testSimpleReferenceObject() throws DataObjectException, SQLException {
		final KnowledgeItem reference;
		{
			final Transaction tx = begin();
			final KnowledgeObject tmp = newE("reference");
			tx.commit();
			reference = HistoryUtils.getKnowledgeItem(HistoryUtils.getLastRevision(), tmp);
		}

		final KnowledgeObject source1;
		final Revision r1;
		{
			// create source and add reference in same revision
			final Transaction tx = begin();
			source1 = newE("source1");
			source1.setAttributeValue(REFERENCE_POLY_HIST_GLOBAL_NAME, reference);
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		final ItemEventReader eventReader1 = detailedEventReader(r1, r1);
		final HashSet<ItemEvent> readEvents1 = TestKnowledgeEvent.getAllEvents(eventReader1);
		final ObjectCreation createSource1Evt = new ObjectCreation(r1.getCommitNumber(), getObjectID(source1));
		createSource1Evt.setValue(A1_NAME, null, "source1");
		createSource1Evt.setValue(REFERENCE_POLY_HIST_GLOBAL_NAME, null, reference.tId());
		createSource1Evt.setValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, null, source1.getCreateCommitNumber());
		assertEquals(set(createSource1Evt), readEvents1);

		final KnowledgeObject source2;
		final Revision r2;
		final Revision r3;
		{
			// create source and add reference in different revision
			final Transaction txCreateSource = begin();
			source2 = newE("source2");
			txCreateSource.commit();
			r2 = txCreateSource.getCommitRevision();

			final Transaction txAddReference = begin();
			source2.setAttributeValue(REFERENCE_POLY_HIST_GLOBAL_NAME, reference);
			txAddReference.commit();
			r3 = txAddReference.getCommitRevision();
		}
		final ItemEventReader eventReader2 = detailedEventReader(r2, r3);
		final HashSet<ItemEvent> readEvents2 = TestKnowledgeEvent.getAllEvents(eventReader2);
		final ObjectCreation createSource2Evt = new ObjectCreation(r2.getCommitNumber(), getObjectID(source2));
		createSource2Evt.setValue(A1_NAME, null, "source2");
		createSource2Evt.setValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, null, source2.getCreateCommitNumber());
		final ItemChange addReferenceEvt = new ItemUpdate(r3.getCommitNumber(), getObjectID(source2), true);
		addReferenceEvt.setValue(REFERENCE_POLY_HIST_GLOBAL_NAME, null, reference.tId());
		assertEquals(set(createSource2Evt, addReferenceEvt), readEvents2);
	}

	public void testOnBranch() throws SQLException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final BObj b1;
		final BObj b2;
		final long r0;
		{
			Transaction tx = begin();
			b1 = newBObj("b1");
			b2 = newBObj("b2");
			commit(tx);
			r0 = tx.getCommitRevision().getCommitNumber();
		}

		final Branch branch = kb().createBranch(kb().getTrunk(), kb().getRevision(r0), null);
		final Wrapper b1_branch = WrapperHistoryUtils.getWrapper(branch, b1);
		ObjectBranchId b1_branchID = getObjectID(b1_branch);
		final Wrapper b2_branch = WrapperHistoryUtils.getWrapper(branch, b2);
		ObjectBranchId b2_branchID = getObjectID(b2_branch);
		final long startRev = kb().getLastRevision();

		{
			Transaction tx = begin();
			b1.setB1("b1.b1");
			b2.setF2("b2.f2");
			commit(tx);
		}

		final long r1;
		{
			Transaction tx = begin();
			b1_branch.setValue(B1_NAME, "b1_branch.b1_new");
			b2.tDelete();
			commit(tx);
			r1 = tx.getCommitRevision().getCommitNumber();
		}

		final long stopRev;
		{
			Transaction tx = begin();
			b2_branch.setValue(B2_NAME, "b2_branch.b2");
			commit(tx);
			stopRev = tx.getCommitRevision().getCommitNumber();
		}

		final ItemEventReader eventReader =
			new ItemEventReader(kb(), true, startRev, stopRev + 1, null, Collections.singleton(Long.valueOf(branch
				.getBranchId())));
		final HashSet<ItemEvent> readEvents = TestKnowledgeEvent.getAllEvents(eventReader);

		final ItemUpdate b1Update = new ItemUpdate(r1, b1_branchID, true);
		b1Update.setValue(B1_NAME, null, "b1_branch.b1_new");

		final ItemUpdate b2Update = new ItemUpdate(stopRev, b2_branchID, true);
		b2Update.setValue(B2_NAME, null, "b2_branch.b2");
		assertEquals(set(b1Update, b2Update), readEvents);

	}

	public void testInvertObjectEvents() throws SQLException {
		final BObj b1;
		final BObj b2;
		final BObj b3;
		final BObj b4;
		final ObjectBranchId b1ID;
		final ObjectBranchId b2ID;
		final ObjectBranchId b3ID;
		final ObjectBranchId b4ID;
		final long r1;
		{
			Transaction tx = begin();
			b1 = newBObj("b1");
			b1ID = getObjectID(b1);
			b2 = newBObj("b2");
			b2ID = getObjectID(b2);
			b3 = newBObj("b3");
			b3ID = getObjectID(b3);
			b4 = newBObj("b4");
			b4ID = getObjectID(b4);
			
			b1.setB2("b1.b2");
			b2.setB1("b2.b1");
			b3.setF1("b3.f1");
			b4.setF1("b4.f1");
			commit(tx);
			r1 = tx.getCommitRevision().getCommitNumber();
		}

		final ObjectBranchId b5ID;
		final long r2;
		{
			Transaction tx = begin();
			b5ID = getObjectID(newBObj("b5"));
			b1.setB2("b1.b2_new");
			b2.setB1(null);
			b3.setF2("b3.f2");
			b4.setF1(null);
			commit(tx);
			r2 = tx.getCommitRevision().getCommitNumber();
		}

		{
			Transaction tx = begin();
			b1.tDelete();
			commit(tx);
		}

		// deleting b1 must trigger creation of b1
		final ObjectCreation b1Creation = new ObjectCreation(r2, b1ID);
		b1Creation.setValue(A1_NAME, null, "b1");
		b1Creation.setValue(B2_NAME, null, "b1.b2_new");
		b1Creation.setValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, null, r1);

		// Updates on values of b1-b4. Creation of b5 must trigger its deletion
		final ItemUpdate b4f1Creation = new ItemUpdate(r1, b4ID, true);
		b4f1Creation.setValue(AObj.F1_NAME, null, "b4.f1");
		final ItemUpdate b3f2Deletion = new ItemUpdate(r1, b3ID, true);
		b3f2Deletion.setValue(AObj.F2_NAME, "b3.f2", null);
		final ItemUpdate b2b1Creation = new ItemUpdate(r1, b2ID, true);
		b2b1Creation.setValue(B1_NAME, null, "b2.b1");
		final ItemUpdate b1B2Update = new ItemUpdate(r1, b1ID, true);
		b1B2Update.setValue(B2_NAME, "b1.b2_new", "b1.b2");
		final ItemDeletion b5Deletion = new ItemDeletion(r1, b5ID);
		b5Deletion.setValue(A1_NAME, "b5");
		b5Deletion.setValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, r2);

		final ItemEventReader reader = new ItemEventReader(kb(), true, Revision.CURRENT_REV, r1);
		final Set<?> allEvents = TestKnowledgeEvent.getAllEvents(reader);
		final Set<?> expected = set(b1Creation, b4f1Creation, b3f2Deletion, b2b1Creation, b1B2Update, b5Deletion);
		
		assertEquals(expected, allEvents);
	}

	public void testInvertAssociationEvents() throws DataObjectException, SQLException {
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

		ObjectKey b1Key = b1.tId();
		ObjectKey b2Key = b2.tId();
		ObjectKey b3Key = b3.tId();

		final ObjectBranchId b1ID = getObjectID(b1);
		final ObjectBranchId b1b2ID = getObjectID(b1b2);
		final long r1 = kb().getLastRevision();

		Revision rev2;
		final KnowledgeAssociation b2b3;
		final Object b2b3AB1;
		final KnowledgeAssociation b1b3;
		final Object b1b3AB1;
		{
			Transaction tx = begin();
			b2b3 = newAB(b2, b3);
			b2b3AB1 = b2b3.getAttributeValue(AB1_NAME);
			b1b3 = newAB(b1, b3);
			b1b3AB1 = b1b3.getAttributeValue(AB1_NAME);
			commit(tx);
			rev2 = tx.getCommitRevision();
		}

		final ObjectBranchId b2b3ID = getObjectID(b2b3);
		final ObjectBranchId b1b3ID = getObjectID(b1b3);

		final long r3;
		{
			Transaction tx = begin();
			setAB2(b1b3, "b1b3.ab2");
			b1b2.delete();
			commit(tx);
			r3 = tx.getCommitRevision().getCommitNumber();
		}

		final long r4;
		{
			Transaction tx = begin();
			b1.delete();
			commit(tx);
			r4 = tx.getCommitRevision().getCommitNumber();
		}

		// b1.delete must trigger b1 creation and b1b3 creation
		Set<KnowledgeEvent> expected = new HashSet<>();
		final ObjectCreation b1Creation = new ObjectCreation(r3, b1ID);
		b1Creation.setValue(A1_NAME, null, "b1");
		b1Creation.setValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, null, r1);
		expected.add(b1Creation);
		final ObjectCreation b1b3Creation = new ObjectCreation(r3, b1b3ID);
		b1b3Creation.setValue(AB1_NAME, null, "b1-b3");
		b1b3Creation.setValue(AB2_NAME, null, "b1b3.ab2");
		b1b3Creation.setValue(REFERENCE_DEST_NAME, null, b3.tId());
		b1b3Creation.setValue(REFERENCE_SOURCE_NAME, null, b1.tId());
		b1b3Creation.setValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, null, rev2.getCommitNumber());
		expected.add(b1b3Creation);

		// b1b2.delete must trigger b1b2 creation
		// changing value of attribute bc1
		final ObjectCreation b1b2Creation = new ObjectCreation(rev2.getCommitNumber(), b1b2ID);
		b1b2Creation.setValue(AB1_NAME, null, "b1-b2");
		b1b2Creation.setValue(REFERENCE_DEST_NAME, null, b2.tId());
		b1b2Creation.setValue(REFERENCE_SOURCE_NAME, null, b1.tId());
		b1b2Creation.setValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, null, r1);
		expected.add(b1b2Creation);
		final ItemUpdate b1b3Update = new ItemUpdate(rev2.getCommitNumber(), b1b3ID, true);
		b1b3Update.setValue(AB2_NAME, "b1b3.ab2", null);
		b1b3Update.setValue(REFERENCE_DEST_NAME, b3.tId(), b3.tId(), false);
		b1b3Update.setValue(REFERENCE_SOURCE_NAME, b1.tId(), b1.tId(), false);
		expected.add(b1b3Update);

		// creation b1b3 and b2b3 must trigger their deletion
		ItemDeletion b1b3Deletion = new ItemDeletion(r1, b1b3ID);
		b1b3Deletion.setValue(AB1_NAME, b1b3AB1);
		b1b3Deletion.setValue(REFERENCE_DEST_NAME, b3Key);
		b1b3Deletion.setValue(REFERENCE_SOURCE_NAME, b1Key);
		b1b3Deletion.setValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, rev2.getCommitNumber());
		expected.add(b1b3Deletion);
		ItemDeletion b2b3Deletion = new ItemDeletion(r1, b2b3ID);
		b2b3Deletion.setValue(AB1_NAME, b2b3AB1);
		b2b3Deletion.setValue(REFERENCE_DEST_NAME, b3Key);
		b2b3Deletion.setValue(REFERENCE_SOURCE_NAME, b2Key);
		b2b3Deletion.setValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, rev2.getCommitNumber());
		expected.add(b2b3Deletion);

		final ItemEventReader reader = new ItemEventReader(kb(), true, r4, r1);
		final HashSet<ItemEvent> allEvents = TestKnowledgeEvent.getAllEvents(reader);
		
		assertEquals(expected, allEvents);
	}

	public void testUndo() throws DataObjectException {
		final KnowledgeObject b1;
		final TLID b1Name;
		final KnowledgeObject c1;
		final TLID c1Name;
		final KnowledgeAssociation b1c1;
		final TLID b1c1Name;
		final Revision r1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			setB2(b1, "b1.b2");
			b1Name = KBUtils.getObjectName(b1);
			c1 = newC("c1");
			c1Name = KBUtils.getObjectName(c1);
			b1c1 = newBC(b1, c1);
			b1c1Name = KBUtils.getObjectName(b1c1);
			commit(tx);
			r1 = tx.getCommitRevision();
		}

		{
			Transaction tx = begin();
			b1.delete();
			commit(tx);
		}

		final KnowledgeObject b1Before = kb().getKnowledgeObject(B_NAME, b1Name);
		assertNull("b1 was not deleted", b1Before);
		final KnowledgeObject c1Before = kb().getKnowledgeObject(C_NAME, c1Name);
		assertEquals(c1, c1Before);
		final KnowledgeObject bc1Before = kb().getKnowledgeObject(BC_NAME, b1c1Name);
		assertNull("b1c1 was not deleted", bc1Before);

		KBUtils.revert(kb(), r1, trunk());

		final KnowledgeObject b1After = kb().getKnowledgeObject(B_NAME, b1Name);
		assertNotNull(b1After);
		assertEquals("b1.b2", b1After.getAttributeValue(B2_NAME));
		final KnowledgeObject c1After = kb().getKnowledgeObject(C_NAME, c1Name);
		assertEquals(c1, c1After);
		final KnowledgeAssociation bc1After = kb().getKnowledgeAssociation(BC_NAME, b1c1Name);
		assertNotNull(bc1After);
	}
	
	private ItemEventReader detailedEventReader(Revision startRev, Revision stopRev) throws SQLException {
		return new ItemEventReader(kb(), true, startRev.getCommitNumber(), stopRev.getCommitNumber() + 1);
	}

	public static Test suite() {

		// Switch to true to activate single test.
		if (false) {
			return runOneTest(TestItemEventReader.class, "testUndo");
		}

		return suite(TestItemEventReader.class);
	}

}
