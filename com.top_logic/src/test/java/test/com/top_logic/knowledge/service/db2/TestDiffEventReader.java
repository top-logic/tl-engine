/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.io.binary.TestingBinaryData;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.AObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.CObj;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemCreation;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.diff.DiffEventReader;

/**
 * The class {@link TestDiffEventReader} tests functionality of
 * {@link DiffEventReader}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestDiffEventReader extends AbstractDBKnowledgeBaseClusterTest {

	/**
	 * Tests a special case: In case touched types are determined using XRef-Table it may be that
	 * only type {@link KnowledgeBaseTestScenarioConstants#B_NAME} is touched. Fetching changes in
	 * flex table may report a result for a differnt type (
	 * {@link KnowledgeBaseTestScenarioConstants#E_NAME}) because it has large attribute (BLOB or
	 * CLOB). Such attributes are always reported because they can not be compared by the database.
	 */
	public void testLargeFlexAttributeInPreviousRevisionOnUntouchedType() throws DataObjectException, SQLException {
		KnowledgeObject e1;
		KnowledgeObject b1;
		Revision r0;
		{
			Transaction tx = begin();
			e1 = newE("e1");
			e1.setAttributeValue("largeFlexAttribute", new TestingBinaryData(4711, 10240));
			tx.commit();
		}
		{
			Transaction tx = begin();
			b1 = newB("b1");
			tx.commit();
			r0 = tx.getCommitRevision();
		}
		Revision r1;
		{
			Transaction tx = begin();
			b1.setAttributeValue(A1_NAME, "b1_new");
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		DiffEventReader reader = newDiffReader(r0, r1);
		try {
			ItemEvent event = reader.readEvent();
			assertNull(reader.readEvent());
			assertInstanceof(event, ItemUpdate.class);
			assertNotEquals("Type " + E_NAME + " was not touched.", ObjectBranchId.toObjectBranchId(e1.tId()),
				((ItemUpdate) event).getObjectId());
			assertEquals(ObjectBranchId.toObjectBranchId(b1.tId()), ((ItemUpdate) event).getObjectId());
			assertEquals(Collections.singletonMap(A1_NAME, "b1_new"), ((ItemUpdate) event).getValues());
			assertEquals(Collections.singletonMap(A1_NAME, "b1"), ((ItemUpdate) event).getOldValues());
		} finally {
			reader.close();
		}

	}

	public void testMultipleFlexAttributeSameType() throws DataObjectException, SQLException {
		KnowledgeObject e1;
		Revision r0;
		{
			Transaction tx = begin();
			e1 = newE("e1");
			e1.setAttributeValue("flex3", "felx1");
			e1.setAttributeValue("flex4", "felx1");
			tx.commit();
			r0 = tx.getCommitRevision();
		}
		Revision r1;
		{
			Transaction tx = begin();
			// Update
			e1.setAttributeValue("flex1", "felx1");
			e1.setAttributeValue("flex2", 15);
			// Deletion
			e1.setAttributeValue("flex3", null);
			e1.setAttributeValue("flex4", null);
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		DiffEventReader reader = newDiffReader(r0, r1);
		Map<String, Object> r1Values =
			new MapBuilder<String, Object>().put("flex1", "felx1").put("flex2", 15).put("flex3", null)
				.put("flex4", null).toMap();
		Map<String, Object> r0Values = new MapBuilder<String, Object>().put("flex1", null).put("flex2", null).put("flex3", "felx1")
			.put("flex4", "felx1").toMap();
		try {
			ItemEvent event = reader.readEvent();
			assertInstanceof(event, ItemUpdate.class);
			assertEquals(r1Values, ((ItemUpdate) event).getValues());
			assertEquals(r0Values, ((ItemUpdate) event).getOldValues());
			assertNull(reader.readEvent());
		} finally {
			reader.close();
		}

		DiffEventReader reverseReader = newDiffReader(r1, r0);
		try {
			ItemEvent event = reverseReader.readEvent();
			assertInstanceof(event, ItemUpdate.class);
			assertEquals(r1Values, ((ItemUpdate) event).getOldValues());
			assertEquals(r0Values, ((ItemUpdate) event).getValues());
			assertNull(reverseReader.readEvent());
		} finally {
			reverseReader.close();
		}

	}

	public void testMultipleFlexAttributeDifferentType() throws DataObjectException, SQLException {
		KnowledgeObject e1;
		KnowledgeObject d1;
		Revision r0;
		{
			Transaction tx = begin();
			e1 = newE("e1");
			d1 = newD("d1");
			d1.setAttributeValue("flex3D1", "felx1");
			e1.setAttributeValue("flex2E1", 15);
			e1.setAttributeValue("flex3E1", Boolean.TRUE);
			tx.commit();
			r0 = tx.getCommitRevision();
		}
		Revision r2;
		{
			Transaction tx = begin();
			// Update
			d1.setAttributeValue("flex1D1", "felx1");
			d1.setAttributeValue("flex2D1", Boolean.TRUE);
			e1.setAttributeValue("flex1E1", 15);
			// Deletion
			d1.setAttributeValue("flex3D1", null);
			e1.setAttributeValue("flex2E1", null);
			e1.setAttributeValue("flex3E1", null);
			tx.commit();
			r2 = tx.getCommitRevision();
		}
		DiffEventReader reader = newDiffReader(r0, r2);
		try {
			ItemUpdate e1Update =
				new ItemUpdate(DiffEventReader.NO_REVISION, ObjectBranchId.toObjectBranchId(e1.tId()), true);
			e1Update.getOldValues().put("flex1E1", null);
			e1Update.getOldValues().put("flex2E1", 15);
			e1Update.getOldValues().put("flex3E1", Boolean.TRUE);
			e1Update.getValues().put("flex1E1", 15);
			e1Update.getValues().put("flex2E1", null);
			e1Update.getValues().put("flex3E1", null);
			ItemUpdate d1Update =
				new ItemUpdate(DiffEventReader.NO_REVISION, ObjectBranchId.toObjectBranchId(d1.tId()), true);
			d1Update.getOldValues().put("flex1D1", null);
			d1Update.getOldValues().put("flex2D1", null);
			d1Update.getOldValues().put("flex3D1", "felx1");
			d1Update.getValues().put("flex1D1", "felx1");
			d1Update.getValues().put("flex2D1", Boolean.TRUE);
			d1Update.getValues().put("flex3D1", null);

			ItemEvent event1 = reader.readEvent();
			ItemEvent event2 = reader.readEvent();
			assertEquals(set(e1Update, d1Update), set(event1, event2));
			assertNull(reader.readEvent());
		} finally {
			reader.close();
		}

	}

	public void testCyclicReferenceValue() throws DataObjectException, SQLException {
		Revision r0 = HistoryUtils.getLastRevision(kb());
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
		DiffEventReader reader = newDiffReader(r0, r1);

		ItemEvent evt;
		boolean source1Found = false;
		boolean source2Found = false;
		while ((evt = reader.readEvent()) != null) {
			final ObjectBranchId objectId = evt.getObjectId();
			assertInstanceof(evt, ItemCreation.class);
			Map<String, Object> values = ((ItemCreation) evt).getValues();
			assertEquals(3, values.size());
			if (objectId.equals(getObjectID(source1))) {
				source1Found = true;
				assertEquals("source1", values.get(A1_NAME));
				assertEquals(source2.tId(), values.get(REFERENCE_POLY_CUR_LOCAL_NAME));
				assertEquals(source2.getCreateCommitNumber(), values.get(BasicTypes.REV_CREATE_ATTRIBUTE_NAME));
			} else if (objectId.equals(getObjectID(source2))) {
				source2Found = true;
				assertEquals("source2", values.get(A1_NAME));
				assertEquals(source1.tId(), values.get(REFERENCE_POLY_CUR_LOCAL_NAME));
				assertEquals(source1.getCreateCommitNumber(), values.get(BasicTypes.REV_CREATE_ATTRIBUTE_NAME));
			} else {
				fail("Unexpected event: " + evt);
			}
		}
		assertTrue("No event found for source2", source2Found);
		assertTrue("No event found for source1", source1Found);
	}

	public void testReferenceToSelf() throws DataObjectException, SQLException {
		Revision r0 = HistoryUtils.getLastRevision(kb());
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
		DiffEventReader reader = newDiffReader(r0, r1);

		try {
			final ItemEvent readEvent = reader.readEvent();
			assertInstanceof(readEvent, ItemCreation.class);
			assertEquals(getObjectID(source1), readEvent.getObjectId());
			final Map<String, Object> values = ((ItemCreation) readEvent).getValues();
			assertEquals(3, values.size());
			assertEquals("source1", values.get(A1_NAME));
			assertEquals(source1.tId(), values.get(REFERENCE_POLY_CUR_LOCAL_NAME));
			assertEquals(source1.getCreateCommitNumber(), values.get(BasicTypes.REV_CREATE_ATTRIBUTE_NAME));
			assertNull(reader.readEvent());
		} finally {
			reader.close();
		}

		final KnowledgeObject source2;
		final Revision r2 = HistoryUtils.getLastRevision(kb());
		{
			// create source and add reference in different revision
			final Transaction createObject = begin();
			source2 = newE("source2");
			createObject.commit();

			final Transaction addReference = begin();
			setReference(source2, source2, !MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
			addReference.commit();
		}
		final Revision r3 = HistoryUtils.getLastRevision(kb());

		DiffEventReader reader2 = newDiffReader(r2, r3);

		try {
			final ItemEvent readEvent = reader2.readEvent();
			assertInstanceof(readEvent, ItemCreation.class);
			assertEquals(getObjectID(source2), readEvent.getObjectId());
			final Map<String, Object> values = ((ItemCreation) readEvent).getValues();
			assertEquals(3, values.size());
			assertEquals("source2", values.get(A1_NAME));
			assertEquals(source2.tId(), values.get(REFERENCE_POLY_CUR_LOCAL_NAME));
			assertEquals(source2.getCreateCommitNumber(), values.get(BasicTypes.REV_CREATE_ATTRIBUTE_NAME));
			assertNull(reader2.readEvent());
		} finally {
			reader2.close();
		}
	}

	public void testReferenceCreation() throws SQLException {
		final KnowledgeItem referenceObject;
		{
			Transaction tx = begin();
			referenceObject = newD("referenceD");
			commit(tx);
		}

		final KnowledgeObject source;
		final ObjectBranchId sourceID;
		final Revision r0 = HistoryUtils.getLastRevision(kb());
		{
			// source creation and reference setting in different revisions
			Transaction createSource = begin();
			source = newE("source");
			sourceID = getObjectID(source);
			commit(createSource);

			Transaction setReference = begin();
			setReference(source, referenceObject, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
			commit(setReference);
		}

		final Revision r1 = HistoryUtils.getLastRevision(kb());

		DiffEventReader reader = newDiffReader(r0, r1);
		try {
			final ItemEvent readEvent = reader.readEvent();
			assertInstanceof(readEvent, ItemCreation.class);
			assertEquals(sourceID, readEvent.getObjectId());
			final Map<String, Object> values = ((ItemCreation) readEvent).getValues();
			assertEquals(3, values.size());
			assertEquals("source", values.get(A1_NAME));
			assertEquals(referenceObject.tId(), values.get(REFERENCE_MONO_CUR_LOCAL_NAME));
			assertEquals(source.getCreateCommitNumber(), values.get(BasicTypes.REV_CREATE_ATTRIBUTE_NAME));
			assertNull(reader.readEvent());
		} finally {
			reader.close();
		}

		final KnowledgeObject source2;
		final ObjectBranchId sourceID2;
		final Revision r2 = HistoryUtils.getLastRevision(kb());
		{
			// source creation and reference setting in same revisions
			Transaction createSourceAndSetReference = begin();
			source2 = newE("source2");
			sourceID2 = getObjectID(source2);
			setReference(source2, referenceObject, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
			commit(createSourceAndSetReference);
		}

		final Revision r3 = HistoryUtils.getLastRevision(kb());

		DiffEventReader reader2 = newDiffReader(r2, r3);
		try {
			final ItemEvent readEvent = reader2.readEvent();
			assertInstanceof(readEvent, ItemCreation.class);
			assertEquals(sourceID2, readEvent.getObjectId());
			final Map<String, Object> values = ((ItemCreation) readEvent).getValues();
			assertEquals(3, values.size());
			assertEquals("source2", values.get(A1_NAME));
			assertEquals(referenceObject.tId(), values.get(REFERENCE_MONO_CUR_LOCAL_NAME));
			assertEquals(source2.getCreateCommitNumber(), values.get(BasicTypes.REV_CREATE_ATTRIBUTE_NAME));
			assertNull(reader2.readEvent());
		} finally {
			reader2.close();
		}
	}

	public void testSimpleReferenceCreation() throws SQLException {
		final KnowledgeItem referenceObject;
		{
			Transaction tx = begin();
			KnowledgeObject tmp = newE("referenceE");
			commit(tx);
			
			referenceObject = HistoryUtils.getKnowledgeItem(HistoryUtils.getLastRevision(), tmp);
		}
		
		final KnowledgeObject bobj;
		final ObjectBranchId bobjObjectID;
		final Revision r0 = HistoryUtils.getLastRevision(kb());
		{
			Transaction tx = begin();
			bobj = newE("e2");
			bobjObjectID = getObjectID(bobj);

			commit(tx);
		}

		{
			Transaction tx = begin();
			bobj.setAttributeValue(REFERENCE_POLY_HIST_GLOBAL_NAME, referenceObject);
			commit(tx);
		}
		final Revision r1 = HistoryUtils.getLastRevision(kb());

		DiffEventReader reader = newDiffReader(r0, r1);
		try {
			final ItemEvent readEvent = reader.readEvent();
			assertInstanceof(readEvent, ItemCreation.class);
			assertEquals(bobjObjectID, readEvent.getObjectId());
			final Map<String, Object> values = ((ItemCreation) readEvent).getValues();
			assertEquals(3, values.size());
			assertEquals(bobj.getAttributeValue(A1_NAME), values.get(A1_NAME));
			assertEquals(referenceObject.tId(), values.get(REFERENCE_POLY_HIST_GLOBAL_NAME));
			assertEquals(bobj.getCreateCommitNumber(), values.get(BasicTypes.REV_CREATE_ATTRIBUTE_NAME));
			assertNull(reader.readEvent());
		} finally {
			reader.close();
		}

	}
	
	/**
	 * Test deleting an object having a flex attribute.
	 */
	public void testDeleteObjWithFlex() throws SQLException {
		final BObj bobj;
		final ObjectBranchId bobjObjectID;
		final Revision r0;
		{
			Transaction tx = begin();
			bobj = BObj.newBObj("b2");
			bobjObjectID = getObjectID(bobj);
			
			bobj.setF1("f1_value");
			commit(tx);
			r0 = tx.getCommitRevision();
		}
		
		{
			Transaction tx = begin();
			bobj.setF1(null);
			commit(tx);
		}
		
		final Revision r1;
		{
			Transaction tx = begin();
			bobj.tDelete();
			commit(tx);
			r1 = tx.getCommitRevision();
		}
		
		DiffEventReader reader = newDiffReader(r0, r1);
		try {
			final ItemEvent readEvent = reader.readEvent();
			assertInstanceof(readEvent, ItemDeletion.class);
			ItemDeletion deleteEvent = (ItemDeletion) readEvent;
			assertEquals(bobjObjectID, deleteEvent.getObjectId());
			Map<String, Object> expectedOldValues = new MapBuilder<String, Object>()
				.put(A1_NAME, "b2")
				.put(BObj.F1_NAME, "f1_value")
				.put(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, r0.getCommitNumber())
				.toMap();
			assertEquals(expectedOldValues, deleteEvent.getValues());
			assertNull(reader.readEvent());
		} finally {
			reader.close();
		}
	}
	
	/**
	 * Deletes a flex attribute on some object
	 */
	public void testFlexDeletion() throws SQLException {
		final BObj flexCreation;
		final Revision r0;
		{
			Transaction tx = begin();
			flexCreation = BObj.newBObj("b2");
			flexCreation.setF1("f1_value");
			commit(tx);
			r0 = tx.getCommitRevision();
		}
		
		final Revision r1;
		{
			Transaction tx = begin();
			flexCreation.setF1(null);
			commit(tx);
			r1 = tx.getCommitRevision();
		}
		
		DiffEventReader reader = newDiffReader(r0, r1);
		try {
			ItemEvent event;
			boolean flexCreationReported = false;
			while ((event = reader.readEvent()) != null) {
				if (event.getObjectId().equals(getObjectID(flexCreation))) {
					flexCreationReported = true;
					assertInstanceof(event, ItemUpdate.class);
					ItemUpdate update = (ItemUpdate) event;
					final Map<String, Object> values = update.getValues();
					assertEquals(null, values.get(BObj.F1_NAME));
					final Map<String, Object> oldValues = update.getOldValues();
					assertEquals("f1_value", oldValues.get(BObj.F1_NAME));
				} else {
					fail("Unexpected event for object '" + event.getObjectId() + "'");
				}
			}
			assertTrue("Change of flex attribute not reported", flexCreationReported);
		} finally {
			reader.close();
		}
	}
	
	/**
	 * Creates a flex attribute which currently don't has any flex attributes
	 */
	public void testSimpleFlexCreation() throws SQLException {
		final BObj flexCreation;
		final Revision r0;
		{
			Transaction tx = begin();
			flexCreation = BObj.newBObj("b2");
			commit(tx);
			r0 = tx.getCommitRevision();
		}

		final Revision r1;
		{
			Transaction tx = begin();
			flexCreation.setF1("f1_value");
			commit(tx);
			r1 = tx.getCommitRevision();
		}
	
		DiffEventReader reader = newDiffReader(r0, r1);
		try {
			ItemEvent event;
			boolean flexCreationReported = false;
			while ((event = reader.readEvent()) != null) {
				if (event.getObjectId().equals(getObjectID(flexCreation))) {
					flexCreationReported = true;
					assertInstanceof(event, ItemUpdate.class);
					ItemUpdate update = (ItemUpdate) event;
					final Map<String, Object> values = update.getValues();
					assertEquals("f1_value", values.get(BObj.F1_NAME));
					final Map<String, Object> oldValues = update.getOldValues();
					assertEquals(null, oldValues.get(BObj.F1_NAME));
				} else {
					fail("Unexpected event for object '" + event.getObjectId() + "'");
				}
			}
			assertTrue("Ticket #2591: Creation of flex attribute not reported", flexCreationReported);
		} finally {
			reader.close();
		}
	}
	
	/**
	 * Creates a flex attribute on some object which already has another flex
	 * attribute
	 */
	public void testFlexCreationAnotherFlexExists() throws SQLException {
		final BObj flexCreation;
		final Revision r0;
		{
			Transaction tx = begin();
			flexCreation = BObj.newBObj("b2");
			flexCreation.setF1("f1_value");
			commit(tx);
			r0 = tx.getCommitRevision();
		}

		final Revision r1;
		{
			Transaction tx = begin();
			flexCreation.setF2("f2_value");
			commit(tx);
			r1 = tx.getCommitRevision();
		}

		DiffEventReader reader = newDiffReader(r0, r1);
		try {
			ItemEvent event;
			boolean flexCreationReported = false;
			while ((event = reader.readEvent()) != null) {
				if (event.getObjectId().equals(getObjectID(flexCreation))) {
					flexCreationReported = true;
					assertInstanceof(event, ItemUpdate.class);
					ItemUpdate update = (ItemUpdate) event;
					final Map<String, Object> values = update.getValues();
					assertEquals("f2_value", values.get(BObj.F2_NAME));
					final Map<String, Object> oldValues = update.getOldValues();
					assertEquals(null, oldValues.get(BObj.F2_NAME));
				} else {
					fail("Unexpected event for object '" + event.getObjectId() + "'");
				}
			}
			assertTrue("Ticket #2591: Creation of flex attribute not reported", flexCreationReported);
		} finally {
			reader.close();
		}
	}

	/**
	 * Changes a flex attribute on some object
	 */
	public void testFlexValueChanged() throws SQLException {
		final BObj flexCreation;
		final Revision r0;
		{
			Transaction tx = begin();
			flexCreation = BObj.newBObj("b2");
			flexCreation.setF1("f1_value");
			commit(tx);
			r0 = tx.getCommitRevision();
		}
		
		final Revision r1;
		{
			Transaction tx = begin();
			flexCreation.setF1("f1_new_value");
			commit(tx);
			r1 = tx.getCommitRevision();
		}
		
		DiffEventReader reader = newDiffReader(r0, r1);
		try {
			ItemEvent event;
			boolean flexCreationReported = false;
			while ((event = reader.readEvent()) != null) {
				if (event.getObjectId().equals(getObjectID(flexCreation))) {
					flexCreationReported = true;
					assertInstanceof(event, ItemUpdate.class);
					ItemUpdate update = (ItemUpdate) event;
					final Map<String, Object> values = update.getValues();
					assertEquals("f1_new_value", values.get(BObj.F1_NAME));
					final Map<String, Object> oldValues = update.getOldValues();
					assertEquals("f1_value", oldValues.get(BObj.F1_NAME));
					assertEquals(null, oldValues.get(BObj.F2_NAME));
				} else {
					fail("Unexpected event for object '" + event.getObjectId() + "'");
				}
			}
			assertTrue("Change of flex attribute not reported", flexCreationReported);
		} finally {
			reader.close();
		}
	}
	
	public void testSimpleAttributeChange() throws SQLException {
		final BObj changedRow;
		final BObj changedFlex;
		final BinaryData binaryData;
		final BObj unchanged;
		final Revision r0;
		{
			Transaction tx = begin();
			changedRow = BObj.newBObj("b1");
			changedRow.setA2("a2");
			
			changedFlex = BObj.newBObj("b2");
			changedFlex.setF1("f1");
			changedFlex.setF2("f2");
			binaryData = BinaryDataFactory.createBinaryData(new byte[4]);
			changedFlex.setF4(binaryData);
			
			unchanged = BObj.newBObj("unchanged");
			unchanged.setA2("a2");
			unchanged.setF1("f1");
			unchanged.setF4(binaryData);
			commit(tx);
			
			r0 = tx.getCommitRevision();
		}

		final BinaryData newBinaryData;
		{
			Transaction tx = begin();
			changedRow.setA2("b1.a2_tmp");
			changedFlex.setF1("f1_tmp");
			newBinaryData = BinaryDataFactory.createBinaryData(new byte[5]);
			changedFlex.setF4(newBinaryData);
			commit(tx);
		}

		{
			Transaction tx = begin();
			changedFlex.setF2("f2_tmp");
			unchanged.setA2("a2_tmp");
			unchanged.setF1("f1_tmp");
			commit(tx);
		}

		final Revision r1;
		{
			Transaction tx = begin();
			changedRow.setA2("a2_new");
			changedFlex.setF2(null);
			changedFlex.setF1("f1_new");
			unchanged.setA2("a2");
			unchanged.setF1("f1");
			commit(tx);
			r1 = tx.getCommitRevision();
		}

		DiffEventReader reader = newDiffReader(r0, r1);
		try {
			ItemEvent event;
			boolean rowChangeFound = false;
			boolean flexChangeFound = false;
			while ((event = reader.readEvent()) != null) {
				if (event.getObjectId().equals(getObjectID(changedRow))) {
					rowChangeFound = true;
					assertInstanceof(event, ItemChange.class);
					final Map<String, Object> values = ((ItemChange) event).getValues();
					assertEquals("a2_new", values.get(A2_NAME));
				} else if (event.getObjectId().equals(getObjectID(changedFlex))) {
					flexChangeFound = true;
					assertInstanceof(event, ItemUpdate.class);
					final Map<String, Object> values = ((ItemUpdate) event).getValues();
					final Map<String, Object> oldValues = ((ItemUpdate) event).getOldValues();
					assertEquals("f1_new", values.get(AObj.F1_NAME));
					assertEquals("f1", oldValues.get(AObj.F1_NAME));
					assertEquals(newBinaryData, values.get(AObj.F4_NAME));
					assertEquals(binaryData, oldValues.get(AObj.F4_NAME));
					assertEquals(null, values.get(AObj.F2_NAME));
					assertEquals("f2", oldValues.get(AObj.F2_NAME));
				} else {
					fail("unexpected event" + event);
				}
			}
			assertTrue("No event for changing row attributes", rowChangeFound);
			assertTrue("No event for changing flex attributes", flexChangeFound);
		} finally {
			reader.close();
		}

		DiffEventReader invertedReader = newDiffReader(r1, r0);
		try {
			ItemEvent event;
			boolean rowChangeFound = false;
			boolean flexChangeFound = false;
			while ((event = invertedReader.readEvent()) != null) {
				if (event.getObjectId().equals(getObjectID(changedRow))) {
					rowChangeFound = true;
					assertInstanceof(event, ItemChange.class);
					final Map<String, Object> values = ((ItemChange) event).getValues();
					assertEquals("a2", values.get(A2_NAME));
				} else if (event.getObjectId().equals(getObjectID(changedFlex))) {
					flexChangeFound = true;
					assertInstanceof(event, ItemUpdate.class);
					final Map<String, Object> values = ((ItemUpdate) event).getValues();
					final Map<String, Object> oldValues = ((ItemUpdate) event).getOldValues();
					assertEquals("f1", values.get(AObj.F1_NAME));
					assertEquals("f1_new", oldValues.get(AObj.F1_NAME));
					assertEquals(binaryData, values.get(AObj.F4_NAME));
					assertEquals(newBinaryData, oldValues.get(AObj.F4_NAME));
					assertEquals(null, oldValues.get(AObj.F2_NAME));
					assertEquals("f2", values.get(AObj.F2_NAME));
				} else {
					fail("unexpected event" + event);
				}
			}
			assertTrue("No event for changing row attributes", rowChangeFound);
			assertTrue("No event for changing flex attributes", flexChangeFound);
		} finally {
			invertedReader.close();
		}
	}

	public void testObjectDeletion() throws SQLException {
		final BObj deletedObj;
		final ObjectBranchId deletedID;
		final BObj sourceObj;
		final ObjectBranchId sourceID;
		final CObj destObj;
		final KnowledgeAssociation directlyDeletedAsso;
		final ObjectBranchId directlyDeletedAssoID;
		final KnowledgeAssociation indirectlyDeletedAsso;
		final ObjectBranchId indirectlyDeletedAssoID;
		final Revision r0;
		{
			Transaction tx = begin();
			deletedObj = BObj.newBObj("deletedItem");
			deletedID = getObjectID(deletedObj);
			
			sourceObj = BObj.newBObj("source");
			sourceID = getObjectID(sourceObj);
			
			destObj = CObj.newCObj("dest");
			directlyDeletedAsso = newBC(sourceObj.tHandle(), destObj.tHandle());
			directlyDeletedAssoID = getObjectID(directlyDeletedAsso);
			indirectlyDeletedAsso = newBC(sourceObj.tHandle(), destObj.tHandle());
			indirectlyDeletedAsso.getAttributeValue(BC1_NAME);
			indirectlyDeletedAssoID = getObjectID(indirectlyDeletedAsso);
			commit(tx);
			r0 = tx.getCommitRevision();
		}

		{
			Transaction tx = begin();
			directlyDeletedAsso.delete();
			commit(tx);
		}
		
		final Revision r1;
		{
			Transaction tx = begin();
			deletedObj.setA2("a3"); // object is deleted. value is ignored
			deletedObj.tDelete();
			commit(tx);
			r1 = tx.getCommitRevision();
		}

		final Revision r2;
		{
			Transaction tx = begin();
			sourceObj.tDelete(); // also deletes indirectlyDeletedAsso
			commit(tx);
			r2 = tx.getCommitRevision();
		}

		DiffEventReader reader = newDiffReader(r0, r1);
		try {
			ItemEvent event;
			boolean objDeletionFound = false;
			boolean assoDelFound = false;
			while ((event = reader.readEvent()) != null) {
				if (event.getObjectId().equals(deletedID)) {
					objDeletionFound = true;
					assertInstanceof(event, ItemDeletion.class);
					MapBuilder<String, Object> expectedOldValues = new MapBuilder<>();
					expectedOldValues.put(A1_NAME, "deletedItem");
					expectedOldValues.put(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, r0.getCommitNumber());
					assertEquals(expectedOldValues.toMap(), ((ItemDeletion) event).getValues());
				} else if (event.getObjectId().equals(directlyDeletedAssoID)) {
					assoDelFound = true;
					assertInstanceof(event, ItemDeletion.class);
					KnowledgeItem lastStableDirectlyDeletedAsso =
						kb().resolveObjectKey(directlyDeletedAssoID.toObjectKey(r0));
					MapBuilder<String, Object> expectedOldValues = new MapBuilder<>();
					expectedOldValues.put(BC1_NAME, lastStableDirectlyDeletedAsso.getAttributeValue(BC1_NAME));
					expectedOldValues.put(DBKnowledgeAssociation.REFERENCE_DEST_NAME,
						((KnowledgeItem) lastStableDirectlyDeletedAsso
							.getAttributeValue(DBKnowledgeAssociation.REFERENCE_DEST_NAME)).tId());
					expectedOldValues.put(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME,
						((KnowledgeItem) lastStableDirectlyDeletedAsso
							.getAttributeValue(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME)).tId());
					expectedOldValues.put(BasicTypes.REV_CREATE_ATTRIBUTE_NAME,
						lastStableDirectlyDeletedAsso.getCreateCommitNumber());
					assertEquals(expectedOldValues.toMap(), ((ItemDeletion) event).getValues());
				} else {
					fail("unexpected event" + event);
				}
			}
			assertTrue("No event for deleting item", objDeletionFound);
			assertTrue("No event for deleting association", assoDelFound);
		} finally {
			reader.close();
		}

		DiffEventReader indirectlyDeletingReader = newDiffReader(r1, r2);
		try {
			ItemEvent event;
			boolean objDeletionFound = false;
			boolean assoDelFound = false;
			while ((event = indirectlyDeletingReader.readEvent()) != null) {
				if (event.getObjectId().equals(sourceID)) {
					objDeletionFound = true;
					assertInstanceof(event, ItemDeletion.class);
					ItemDeletion deleteEvent = (ItemDeletion) event;
					MapBuilder<String, Object> expectedOldValues = new MapBuilder<>();
					expectedOldValues.put(A1_NAME, "source");
					expectedOldValues.put(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, r0.getCommitNumber());
					assertEquals(expectedOldValues.toMap(), deleteEvent.getValues());
				} else if (event.getObjectId().equals(indirectlyDeletedAssoID)) {
					assoDelFound = true;
					assertInstanceof(event, ItemDeletion.class);
					KnowledgeItem lastStableIndirectlyDeletedAsso =
						kb().resolveObjectKey(indirectlyDeletedAssoID.toObjectKey(r1));
					MapBuilder<String, Object> expectedOldValues = new MapBuilder<>();
					expectedOldValues.put(BC1_NAME, lastStableIndirectlyDeletedAsso.getAttributeValue(BC1_NAME));
					expectedOldValues.put(DBKnowledgeAssociation.REFERENCE_DEST_NAME,
						((KnowledgeItem) lastStableIndirectlyDeletedAsso
							.getAttributeValue(DBKnowledgeAssociation.REFERENCE_DEST_NAME)).tId());
					expectedOldValues.put(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME,
						((KnowledgeItem) lastStableIndirectlyDeletedAsso
							.getAttributeValue(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME)).tId());
					expectedOldValues.put(BasicTypes.REV_CREATE_ATTRIBUTE_NAME,
						lastStableIndirectlyDeletedAsso.getCreateCommitNumber());
					assertEquals(expectedOldValues.toMap(), ((ItemDeletion) event).getValues());
				} else {
					fail("unexpected event" + event);
				}
			}
			assertTrue("No event for deleting item", objDeletionFound);
			assertTrue("No event for deleting association", assoDelFound);
		} finally {
			indirectlyDeletingReader.close();
		}
	}

	public void testObjectCreation() throws SQLException {
		final Revision r0 = HistoryUtils.getLastRevision(kb());

		final BObj sourceObj;
		final CObj destObj;
		{
			Transaction tx = begin();
			sourceObj = BObj.newBObj("source");
			destObj = CObj.newCObj("dest");
			commit(tx);
		}

		final Revision r1;
		{
			Transaction tx = begin();
			sourceObj.setA2("source.a2");
			commit(tx);
			r1 = tx.getCommitRevision();
		}

		final KnowledgeAssociation asso;
		{
			Transaction tx = begin();
			asso = newBC(sourceObj.tHandle(), destObj.tHandle());
			commit(tx);
		}
		
		final Revision r2;
		{
			Transaction tx = begin();
			setBC1(asso, "asso.bc1");
			commit(tx);
			r2 = tx.getCommitRevision();
		}

		DiffEventReader itemCreationReader = newDiffReader(r0, r1);
		try {
			ItemEvent event;
			boolean sourceObjCreationFound = false;
			boolean destObjCreationFound = false;
			while ((event = itemCreationReader.readEvent()) != null) {
				if (event.getObjectId().equals(getObjectID(sourceObj))) {
					sourceObjCreationFound = true;
					assertInstanceof(event, ObjectCreation.class);
					final Map<String, Object> values = ((ObjectCreation) event).getValues();
					assertEquals("source.a2", values.get(A2_NAME));
				} else if (event.getObjectId().equals(getObjectID(destObj))) {
					destObjCreationFound = true;
					assertInstanceof(event, ObjectCreation.class);
				} else {
					fail("unexpected event" + event);
				}
			}
			assertTrue("No event for creating item", sourceObjCreationFound);
			assertTrue("No event for creating item", destObjCreationFound);
		} finally {
			itemCreationReader.close();
		}

		DiffEventReader assoCreationReader = newDiffReader(r1, r2);
		try {
			ItemEvent event;
			boolean assoCreationFound = false;
			while ((event = assoCreationReader.readEvent()) != null) {
				if (event.getObjectId().equals(getObjectID(asso))) {
					assoCreationFound = true;
					assertInstanceof(event, ObjectCreation.class);
					final ObjectCreation creationEvent = (ObjectCreation) event;
					final Map<String, Object> values = creationEvent.getValues();
					assertEquals("asso.bc1", values.get(BC1_NAME));
					assertEquals(KBUtils.getWrappedObjectKey(destObj),
						creationEvent.getValues().get(DBKnowledgeAssociation.REFERENCE_DEST_NAME));
					assertEquals(KBUtils.getWrappedObjectKey(sourceObj),
						creationEvent.getValues().get(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME));
				} else {
					fail("unexpected event" + event);
				}
			}
			assertTrue("No event for creating association", assoCreationFound);
		} finally {
			assoCreationReader.close();
		}
		
	}

	public void testChangeOnAssociation() throws SQLException {
		final BObj sourceObj;
		final CObj destObj;
		final KnowledgeAssociation changedAsso;
		final KnowledgeAssociation unchangedAsso;
		final Revision r0;
		{
			Transaction tx = begin();
			sourceObj = BObj.newBObj("source");
			destObj = CObj.newCObj("dest");
			
			changedAsso = newBC(sourceObj.tHandle(), destObj.tHandle());
			unchangedAsso = newBC(sourceObj.tHandle(), destObj.tHandle());
			setBC1(changedAsso, "changed.bc1");
			setBC2(changedAsso, "changed.bc2");
			setBC1(unchangedAsso, "unchanged.bc1");
			setBC2(unchangedAsso, "changed.bc2");
			commit(tx);
			r0 = tx.getCommitRevision();
		}

		{
			Transaction tx = begin();
			setBC1(changedAsso, "changed.bc1_tmp");
			setBC1(unchangedAsso, "unchanged.bc1_tmp");
			commit(tx);
		}

		{
			Transaction tx = begin();
			setBC1(changedAsso, "changed.bc1_new");
			commit(tx);
		}

		final Revision r1;
		{
			Transaction tx = begin();
			setBC1(unchangedAsso, "unchanged.bc1");
			commit(tx);
			r1 = tx.getCommitRevision();
		}

		DiffEventReader reader = newDiffReader(r0, r1);
		try {
			ItemEvent event;
			boolean changeFound = false;
			while ((event = reader.readEvent()) != null) {
				if (event.getObjectId().equals(getObjectID(changedAsso))) {
					changeFound = true;
					assertInstanceof(event, ItemChange.class);
					final Map<String, Object> values = ((ItemChange) event).getValues();
					assertEquals("changed.bc1_new", values.get(BC1_NAME));
				} else {
					fail("unexpected event" + event);
				}
			}
			assertTrue("No event for changing row attributes on association", changeFound);
		} finally {
			reader.close();
		}
	}

	/**
	 * Tests a bug in {@link DiffEventReader} (see Ticket #2255).
	 * {@link ItemChange}s were not reported when they don't have values. That
	 * is just correct for {@link ItemUpdate}, but not for {@link ItemCreation}.
	 */
	public void testAssociationCreationWithoutValues() throws SQLException {
		final BObj sourceObj;
		final CObj destObj;
		final Revision r0;
		{
			Transaction tx = begin();
			sourceObj = BObj.newBObj("source");
			destObj = CObj.newCObj("dest");
			commit(tx);
			r0 = tx.getCommitRevision();
		}
		
		final KnowledgeAssociation changedAsso;
		final Revision r1;
		{
			Transaction tx = begin();
			changedAsso = newBC(sourceObj.tHandle(), destObj.tHandle());
			setBC1(changedAsso, null);
			commit(tx);
			r1 = tx.getCommitRevision();
		}
		
		DiffEventReader reader = newDiffReader(r0, r1);
		try {
			final ItemEvent associationCreation = reader.readEvent();
			assertNotNull(associationCreation);
			
			final ItemEvent noEvent = reader.readEvent();
			assertNull(noEvent);
		} finally {
			reader.close();
		}
	}

	private DiffEventReader newDiffReader(Revision sourceRev, Revision destRev) throws SQLException {
		return newDiffreader(sourceRev, kb().getTrunk(), destRev, kb().getTrunk());
	}

	private DiffEventReader newDiffreader(Revision sourceRev, Branch sourceBranch, Revision destRev, Branch destBranch)
			throws SQLException {
		DBKnowledgeBase kb = kb();
		long sourceRevNr = sourceRev.getCommitNumber();
		long sourceBranchId = sourceBranch.getBranchId();
		long destRevNr = destRev.getCommitNumber();
		long destBranchId = destBranch.getBranchId();
		return new DiffEventReader(kb, sourceRevNr, sourceBranchId, destRevNr, destBranchId);
	}
	
	@SuppressWarnings("unused")
	public static Test suite() {
		if (!true) {
			return runOneTest(TestDiffEventReader.class, "testLargeFlexAttributeInPreviousRevisionAndUntouchedType");
		}
		return suite(TestDiffEventReader.class);
	}
}
