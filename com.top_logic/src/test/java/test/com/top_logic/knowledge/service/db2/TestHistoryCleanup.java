/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario;

import com.top_logic.basic.Log;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.HistoryCleanup;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;

/**
 * Test of {@link HistoryCleanup}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestHistoryCleanup extends AbstractDBKnowledgeBaseClusterTest {

	static final String CLEANUP_TYPE = "cleanupType";

	static final String STRING_FLEX_ATTR = "flexAttr";

	private HistoryCleanup _historyCleanup;

	private HistoryCleanup _historyCleanup2;

	@Override
	protected LocalTestSetup createSetup(Test self) {
		DBKnowledgeBaseClusterTestSetup setup = new DBKnowledgeBaseClusterTestSetup(self) {

			@Override
			protected Map<String, String> createKBConfig(String name, String connectionPool) {
				Map<String, String> kbConfig = super.createKBConfig(name, connectionPool);
				// disable automatic cleanup as cleanup is tested here.
				kbConfig.put(KnowledgeBaseConfiguration.CLEANUP_UNVERSIONED_TYPES_INTERVAL_PROPERTY, Long.toString(0L));
				return kbConfig;
			}
		};
		setup.addAdditionalTypes(new TypeProvider() {

			@Override
			public void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository) {
				MOClass cleanupType = new MOKnowledgeItemImpl(CLEANUP_TYPE);
				try {
					MOClass superType = (MOClass) typeRepository.getType(KnowledgeBaseTestScenarioConstants.B_NAME);
					cleanupType.setSuperclass(superType);
				} catch (UnknownTypeException ex) {
					log.error("failure setting super type to cleanup type.", ex);
				}
				KnowledgeBaseTestScenarioImpl.setApplicationType(cleanupType,
					SimpleWrapperFactoryTestScenario.BObj.class);
				cleanupType.setVersioned(false);
				try {
					typeRepository.addMetaObject(cleanupType);
				} catch (DuplicateTypeException ex) {
					log.error("failure adding additional cleanup type.", ex);
				}
			}
		});
		return setup;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_historyCleanup =
			new HistoryCleanup(new AssertProtocol(getClass().getName()), kb(),
				Collections.singletonList((DBTableMetaObject) type(CLEANUP_TYPE)));
		initCleanupTable(_historyCleanup);
		_historyCleanup2 =
			new HistoryCleanup(new AssertProtocol(getClass().getName()), kbNode2(),
				Collections.singletonList((DBTableMetaObject) type2(CLEANUP_TYPE)));
		initCleanupTable(_historyCleanup2);
	}

	private void initCleanupTable(HistoryCleanup historyCleanup) {
		ReflectionUtils.executeMethod(historyCleanup, "initTable", new Class[0], new Object[0]);
	}

	@Override
	protected void tearDown() throws Exception {
		cleanupCleanupTable(_historyCleanup2);
		cleanupCleanupTable(_historyCleanup);
		super.tearDown();
	}

	private void cleanupCleanupTable(HistoryCleanup historyCleanup) {
		ReflectionUtils.executeMethod(historyCleanup, "cleanupTable", new Class[0], new Object[0]);
	}

	public void testCleanupCluster() throws DataObjectException, SQLException, RefetchTimeout {
		Transaction tx = begin();
		KnowledgeObject ko = kb().createKnowledgeObject(CLEANUP_TYPE);
		setA1(ko, "a1");
		ko.setAttributeValue(STRING_FLEX_ATTR, "flexVal1");
		commit(tx);
		Revision r1 = tx.getCommitRevision();

		kbNode2().refetch();
		updateSessionRevisionNode2();

		Transaction changeTX = begin();
		setA1(ko, "a2");
		ko.setAttributeValue(STRING_FLEX_ATTR, "flexVal2");
		changeTX.commit();


		ObjectKey key = KBUtils.createHistoricObjectKey(ko.tId(), r1.getCommitNumber());

		checkDataExists(kb(), key);
		cleanupHistory();
		checkDataExists(kbNode2(), key);

		/* Data are still needed by second KB. */
		KnowledgeItem node2KO = kbNode2().resolveObjectKey(node2Key(ko));
		assertEquals("flexVal1", node2KO.getAttributeValue(STRING_FLEX_ATTR));
		assertEquals("a1", node2KO.getAttributeValue(A1_NAME));
		kbNode2().refetch();
		updateSessionRevisionNode2();
		assertEquals("flexVal2", node2KO.getAttributeValue(STRING_FLEX_ATTR));
		assertEquals("a2", node2KO.getAttributeValue(A1_NAME));

		checkDataExists(kbNode2(), key);
		cleanupHistory();
		checkDataCleanedUp(kbNode2(), key);

	}

	private void cleanupHistory() {
		// Run both cleanups, because the call of this method updates the revisions in the cleanup
		// table.
		_historyCleanup.run();
		_historyCleanup2.run();
	}

	public void testSimpleCleanup() throws DataObjectException, SQLException, RefetchTimeout {
		Transaction tx = begin();
		KnowledgeObject ko = kb().createKnowledgeObject(CLEANUP_TYPE);
		setA1(ko, "a1");
		ko.setAttributeValue(STRING_FLEX_ATTR, "flexVal1");
		commit(tx);
		Revision r1 = tx.getCommitRevision();

		Transaction changeTX = begin();
		setA1(ko, "a2");
		ko.setAttributeValue(STRING_FLEX_ATTR, "flexVal2");
		changeTX.commit();

		kbNode2().refetch();

		ObjectKey key = KBUtils.createHistoricObjectKey(ko.tId(), r1.getCommitNumber());

		checkDataExists(kb(), key);
		cleanupHistory();
		checkDataCleanedUp(kb(), key);

	}

	private void checkDataCleanedUp(DBKnowledgeBase kb, ObjectKey key) throws SQLException {
		ConnectionPool connectionPool = kb.getConnectionPool();
		PooledConnection connection = connectionPool.borrowReadConnection();
		try {
			ResultSet rowDataAfterCleanup = getRowData(connection, key);
			try {
				assertFalse("Old data not removed.", rowDataAfterCleanup.next());
			} finally {
				rowDataAfterCleanup.close();
			}
			ResultSet flexDataAfterCleanup =
				getFlexData(connection, kb.lookupType(AbstractFlexDataManager.FLEX_DATA), key);
			try {
				assertFalse(flexDataAfterCleanup.next());
			} finally {
				flexDataAfterCleanup.close();
			}
		} finally {
			connectionPool.releaseReadConnection(connection);
		}
	}

	private void checkDataExists(DBKnowledgeBase kb, ObjectKey key) throws SQLException {
		ConnectionPool connectionPool = kb.getConnectionPool();
		PooledConnection connection = connectionPool.borrowReadConnection();
		try {
			ResultSet rowData = getRowData(connection, key);
			try {
				assertTrue(rowData.next());
				assertFalse("There is only one row for a revision.", rowData.next());
			} finally {
				rowData.close();
			}
			ResultSet flexData = getFlexData(connection, kb.lookupType(AbstractFlexDataManager.FLEX_DATA), key);
			try {
				assertTrue(flexData.next());
				assertFalse("There is only one flex attribute.", flexData.next());
			} finally {
				flexData.close();
			}
		} finally {
			connectionPool.releaseReadConnection(connection);
		}
	}

	public static Test suite() {
		return suite(TestHistoryCleanup.class);
	}

}
