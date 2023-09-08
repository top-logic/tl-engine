/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.kafka.services;

import static com.top_logic.basic.ArrayUtil.*;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;

import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.basic.SingleTestFactory;
import test.com.top_logic.basic.TestFactory;

import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.db.DBUtil;

/**
 * Test case for TLSync
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestTLSync extends AbstractTLSyncTest {

	static String SOURCE_MODULE_NAME = TestTLSync.class.getName() + ".source";

	static String TARGET_MODULE_NAME = TestTLSync.class.getName() + ".target";

	static String SOURCE_TYPE_NAME = "Node";

	static String SOURCE_TYPE2_NAME = "Node2";

	static String SOURCE_NOT_EXPORTED_TYPE_NAME = "NodeNotExported";

	static String TARGET_TYPE_NAME = "TargetNode";

	static String TARGET_TYPE2_NAME = "TargetNode2";

	static String NOT_EXPORTED_TYPE_NOT_EXPORTED_ATTRIBUTE = "otherNodeTypeNotExported";

	static String EXPORTED_NOT_IMPORTED_ATTRIBUTE = "exportedNotImportedNode";

	static String OTHER_NODE_ATTRIBUTE = "otherNode";

	/**
	 * Test creation an association and delete it before sending to Kafka. In such case the create
	 * event must search the association in the the revision of the event.
	 */
	public void testCreateDeletion() throws Exception {
		runWithKafka(new Execution() {

			@Override
			public void run() {
				Transaction tx = begin();
				TLObject item = newObject(SOURCE_MODULE_NAME, SOURCE_TYPE_NAME);
				TLObject target = newObject(SOURCE_MODULE_NAME, SOURCE_NOT_EXPORTED_TYPE_NAME);
				item.tUpdateByName(NOT_EXPORTED_TYPE_NOT_EXPORTED_ATTRIBUTE, target);
				commit(tx);

				tx = begin();
				item.tUpdateByName(NOT_EXPORTED_TYPE_NOT_EXPORTED_ATTRIBUTE, null);
				commit(tx);
			}
		});

	}

	/**
	 * Checks that the deletion of a not exported association creation is also not exported.
	 */
	public void testCorrectDeletionEventRemoval() throws Exception {
		final AtomicReference<TLObject> sourceItem = new AtomicReference<>();
		runWithKafka(new Execution() {

			@Override
			public void run() {
				Transaction tx = begin();
				sourceItem.set(newObject(SOURCE_MODULE_NAME, SOURCE_TYPE_NAME));
				TLObject target = newObject(SOURCE_MODULE_NAME, SOURCE_NOT_EXPORTED_TYPE_NAME);
				sourceItem.get().tUpdateByName(NOT_EXPORTED_TYPE_NOT_EXPORTED_ATTRIBUTE, target);
				commit(tx);
			}
		});
		
		TLObject targetItem = findReceivedObjectFor(type(TARGET_MODULE_NAME, TARGET_TYPE_NAME), sourceItem.get());
		assertNotNull(targetItem);

		runWithKafka(new Execution() {

			@Override
			public void run() {
				Transaction tx = begin();
				sourceItem.get().tUpdateByName(NOT_EXPORTED_TYPE_NOT_EXPORTED_ATTRIBUTE, null);
				commit(tx);
			}
		});

	}

	/**
	 * Tests that a changeset which is irrelevant for the receiving system does not stop the
	 * receiver.
	 * <p>
	 * That could happen, if the irrelevant changeset would be filtered out too soon, before it is
	 * stored that this changeset was received and processed. If it was not stored that it was
	 * processed, the receiver would think that it is missing, when the next changeset arrives.
	 * Because the next changeset would state that the irrelevant changeset was the last sent
	 * changeset.
	 * </p>
	 */
	public void testRelevantIrrelevantRelevantChanges() {
		/* Setup and relevant change for the receiver: */
		TLObject sourceObject = sync(() -> newObject(SOURCE_MODULE_NAME, SOURCE_TYPE_NAME));
		TLObject targetObject = findReceivedObjectFor(type(TARGET_MODULE_NAME, TARGET_TYPE_NAME), sourceObject);

		/* Setup and relevant change for the receiver: */
		TLObject sourceValue1 = sync(() -> newObject(SOURCE_MODULE_NAME, SOURCE_TYPE_NAME));

		/* Setup and relevant change for the receiver: */
		TLObject sourceValue2 = sync(() -> newObject(SOURCE_MODULE_NAME, SOURCE_TYPE2_NAME));
		TLObject targetValue2 = findReceivedObjectFor(type(TARGET_MODULE_NAME, TARGET_TYPE2_NAME), sourceValue2);

		assertNull(targetObject.tValueByName(OTHER_NODE_ATTRIBUTE));
		assertNull(targetObject.tValueByName(EXPORTED_NOT_IMPORTED_ATTRIBUTE));

		/* Irrelevant change for the receiver: */
		sync(() -> sourceObject.tUpdateByName(EXPORTED_NOT_IMPORTED_ATTRIBUTE, sourceValue1));
		assertNull(targetObject.tValueByName(OTHER_NODE_ATTRIBUTE));
		assertNull(targetObject.tValueByName(EXPORTED_NOT_IMPORTED_ATTRIBUTE));

		/* Relevant change for the receiver: */
		sync(() -> sourceObject.tUpdateByName(OTHER_NODE_ATTRIBUTE, sourceValue2));
		assertEquals(targetValue2, targetObject.tValueByName(OTHER_NODE_ATTRIBUTE));
		assertNull(targetObject.tValueByName(EXPORTED_NOT_IMPORTED_ATTRIBUTE));
	}

	public void testNotImportedAssociationAttribute() throws Exception {
		final AtomicReference<TLObject> sourceItem = new AtomicReference<>();
		runWithKafka(new Execution() {

			@Override
			public void run() {
				Transaction tx = begin();
				sourceItem.set(newObject(SOURCE_MODULE_NAME, SOURCE_TYPE_NAME));
				TLObject target = newObject(SOURCE_MODULE_NAME, SOURCE_TYPE_NAME);
				sourceItem.get().tUpdateByName(EXPORTED_NOT_IMPORTED_ATTRIBUTE, target);
				commit(tx);

			}
		});
		TLObject targetItem = findReceivedObjectFor(type(TARGET_MODULE_NAME, TARGET_TYPE_NAME), sourceItem.get());
		assertNotNull(targetItem);
		TLStructuredTypePart part = targetItem.tType().getPart(EXPORTED_NOT_IMPORTED_ATTRIBUTE);
		assertNotNull(part);
		assertNull("Ticket #22722: Attribute must not be imported, because it is configured to be not imported.",
			targetItem.tValue(part));
	}

	public void testCorrectAssociationTarget() throws Exception {
		final AtomicReference<TLObject> refTargetItem = new AtomicReference<>();
		final AtomicReference<TLObject> sourceItem = new AtomicReference<>();
		runWithKafka(new Execution() {

			@Override
			public void run() {
				Transaction tx = begin();
				sourceItem.set(newObject(SOURCE_MODULE_NAME, SOURCE_TYPE_NAME));
				refTargetItem.set(newObject(SOURCE_MODULE_NAME, SOURCE_TYPE2_NAME));
				sourceItem.get().tUpdateByName(OTHER_NODE_ATTRIBUTE, refTargetItem.get());
				commit(tx);

			}
		});
		TLObject targetItem = findReceivedObjectFor(type(TARGET_MODULE_NAME, TARGET_TYPE_NAME), sourceItem.get());
		assertNotNull(targetItem);
		assertEquals(findReceivedObjectFor(type(TARGET_MODULE_NAME, TARGET_TYPE2_NAME), refTargetItem.get()),
			targetItem.tValueByName(OTHER_NODE_ATTRIBUTE));
	}

	/** Tests that the revCreate value is not being sent and used in the receiving system. */
	public void testRevCreate() {
		TLObject source = sync(() -> createObject());
		/* It is necessary to have a TLModel attribute with the name of the createRev attribute.
		 * Without it, the test will succeed, as there is not TLModel attribute with the name
		 * createRev. And without a TLModel attribute, the createRev value is ignored. But the
		 * createRev has to be ignored even if there is a TLModel attribute. Therefore, this
		 * attribute exists and is necessary. As the test would otherwise not test every necessary
		 * situation. */
		assertNotNull(source.tType().getPart(BasicTypes.REV_CREATE_ATTRIBUTE_NAME));
		TLObject target = findTargetObject(source);
		long sourceCreateRevision = WrapperHistoryUtils.getCreateRevision(source).getCommitNumber();
		long targetCreateRevision = WrapperHistoryUtils.getCreateRevision(target).getCommitNumber();
		assertTrue(targetCreateRevision > sourceCreateRevision);
		/* Circumvent all KnowledgeBase layers as they might fix the createRev in some cases with
		 * the revMin, but forget that and fail in other cases. Checking directly the raw database
		 * value prevents that. */
		long dbCreateRevision = queryCreateRevision(target);
		assertTrue(dbCreateRevision > sourceCreateRevision);
	}

	private long queryCreateRevision(TLObject target) {
		ConnectionPool connectionPool = ((DBKnowledgeBase)kb()).getConnectionPool();
		try {
			DBHelper sqlDialect = connectionPool.getSQLDialect();
			String query = "select " + sqlDialect.columnRef(BasicTypes.REV_CREATE_DB_NAME) + " from "
				+ sqlDialect.tableRef(getTableName(target)) + " where " + BasicTypes.IDENTIFIER_DB_NAME + " = "
				+ getIdString(target);
			return DBUtil.executeQueryAsLong(connectionPool, query, EMPTY_OBJECT_ARRAY);
		} catch (SQLException exception) {
			throw new RuntimeException(exception);
		}
	}

	private String getTableName(TLObject target) {
		MOClass table = (MOClass) target.tHandle().tTable();
		return table.getDBMapping().getDBName();
	}

	private String getIdString(TLObject target) {
		return target.tIdLocal().toExternalForm();
	}

	private TLObject findTargetObject(TLObject source) {
		return findReceivedObjectFor(type(TARGET_MODULE_NAME, TARGET_TYPE_NAME), source);
	}

	private TLObject createObject() {
		return newObject(SOURCE_MODULE_NAME, SOURCE_TYPE_NAME);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestTLSync}.
	 */
	@SuppressWarnings("unused")
	public static Test suite() {
		TestFactory factory = DefaultTestFactory.INSTANCE;
		if (false) {
			factory = new SingleTestFactory("testCorrectAssociationTarget");
		}
		return suite(TestTLSync.class, factory);
	}

}
