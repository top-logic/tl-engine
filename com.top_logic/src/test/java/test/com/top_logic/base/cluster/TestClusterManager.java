/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.cluster;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.TestFactory;

import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.base.cluster.ClusterManager.ClusterManagerDBExecutor;
import com.top_logic.base.cluster.ClusterManager.NodeState;
import com.top_logic.base.cluster.ClusterManager.PropertyType;
import com.top_logic.base.cluster.ClusterManagerException;
import com.top_logic.base.cluster.PendingChangeException;
import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.util.db.DBUtil;

/**
 * Test the {@link ClusterManager}.
 * 
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class TestClusterManager extends BasicTestCase {

	@Override
    protected void setUp() throws Exception {
		super.setUp();
		clearCMTables();
    }

    @Override
    protected void tearDown() throws Exception {
		clearCMTables();
        super.tearDown();
    }

	private void clearCMTables() {
        try {
			ClusterManager cm = new TestableClusterManager(true);
			DBHelper sqlDialect = cm.getConnectionPool().getSQLDialect();
			DBUtil.executeDirectUpdate("CLEAR TABLE " + sqlDialect.tableRef(ClusterManagerDBExecutor.TABLE_VALUE));
        }
        catch (SQLException e) {
            // table doesn't exist, good
            // ... or perhaps database connection is broken - ignore
        }
    }



    private static final String PROP_BOOL = "prop_bool";
    private static final String PROP_INT = "prop_int";
    private static final String PROP_LONG = "prop_long";
    private static final String PROP_FLOAT = "prop_float";
    private static final String PROP_DOUBLE = "prop_double";
    private static final String PROP_CHAR = "prop_char";
    private static final String PROP_STRING = "prop_string";

    private static final String PROP_1 = "prop_1";
    private static final String PROP_2 = "prop_2";
    private static final String PROP_3 = "prop_3";
    private static final String PROP_4 = "prop_4";

    private static final String VALUE_A = "A";
    private static final String VALUE_B = "B";
    private static final String VALUE_C = "C";



    public void testNormalClusterSystem() throws Exception {
        ClusterManager cm1 = new TestableClusterManager(true);
        ClusterManager cm2 = new TestableClusterManager(true);
        ClusterManager cm3 = new TestableClusterManager(true);

        try {
            cm1.setValue(PROP_STRING, VALUE_C);
            fail("Expected IllegalStateException, as cluster manager is not initialized yet.");
        }
        catch(IllegalStateException e) {
            // expected
        }

        // startup
        assertEquals(true, cm1.isClusterMode());
        assertEquals(false, cm1.isClusterModeActive());
		assertNull(cm1.getNodeState());
		assertNull(cm2.getNodeState());
		assertNull(cm3.getNodeState());
        cm1.initNode(); cm2.initNode(); cm3.initNode();
        assertEquals(cm1.getNodeState(), NodeState.WAIT_FOR_STARTUP);
        assertEquals(cm2.getNodeState(), NodeState.WAIT_FOR_STARTUP);
        assertEquals(cm3.getNodeState(), NodeState.WAIT_FOR_STARTUP);
        assertEquals(true, cm1.isClusterModeActive());

        startNode(cm1); startNode(cm2); startNode(cm3);

		assertNull(cm1.getValue(PROP_STRING));
		assertNull(cm2.getValue(PROP_STRING));
		assertNull(cm3.getValue(PROP_STRING));

        // test simple value setting
        cm1.setValue(PROP_STRING, VALUE_A);
        assertEquals(VALUE_A, cm1.getValue(PROP_STRING));
		assertNull(cm2.getValue(PROP_STRING));
		assertNull(cm3.getValue(PROP_STRING));
        assertEquals(false, cm1.isConfirmed(PROP_STRING));

        try {
            cm1.getConfirmedValue(PROP_STRING);
            fail("Expected PendingChangeException.");
        }
        catch (PendingChangeException e) {
            assertEquals(PROP_STRING, e.getProperty());
            assertEquals(VALUE_A, e.getNewValue());
			assertNull(e.getOldValue());
        }
        assertEquals(VALUE_A, cm1.getLatestUnconfirmedValue(PROP_STRING));
		assertNull(cm1.getLatestConfirmedValue(PROP_STRING));

        cm2.refetch();
        assertEquals(VALUE_A, cm1.getValue(PROP_STRING));
        assertEquals(VALUE_A, cm2.getValue(PROP_STRING));
		assertNull(cm3.getValue(PROP_STRING));
        assertEquals(false, cm1.isConfirmed(PROP_STRING));
        assertEquals(false, cm2.isConfirmed(PROP_STRING));

        assertEquals(VALUE_A, cm2.getLatestUnconfirmedValue(PROP_STRING));
		assertNull(cm2.getLatestConfirmedValue(PROP_STRING));
        try {
            cm2.getConfirmedValue(PROP_STRING);
            fail("Expected PendingChangeException.");
        }
        catch (PendingChangeException e) {
            assertEquals(PROP_STRING, e.getProperty());
            assertEquals(VALUE_A, e.getNewValue());
			assertNull(e.getOldValue());
        }


        cm3.refetch();
        assertEquals(VALUE_A, cm1.getValue(PROP_STRING));
        assertEquals(VALUE_A, cm2.getValue(PROP_STRING));
        assertEquals(VALUE_A, cm3.getValue(PROP_STRING));
        assertEquals(true, cm1.isConfirmed(PROP_STRING));
        assertEquals(true, cm2.isConfirmed(PROP_STRING));
        assertEquals(true, cm3.isConfirmed(PROP_STRING));

        assertEquals(VALUE_A, cm1.getConfirmedValue(PROP_STRING));
        assertEquals(VALUE_A, cm1.getLatestUnconfirmedValue(PROP_STRING));
        assertEquals(VALUE_A, cm1.getLatestConfirmedValue(PROP_STRING));

        // test auto refetch at isConfirmed
        cm2.setValue(PROP_STRING, VALUE_B);
        assertEquals(VALUE_A, cm1.getValue(PROP_STRING));
        assertEquals(VALUE_B, cm2.getValue(PROP_STRING));
        assertEquals(VALUE_A, cm3.getValue(PROP_STRING));
        assertEquals(false, cm2.isConfirmed(PROP_STRING));
        assertEquals(false, cm1.isConfirmed(PROP_STRING));
        assertEquals(VALUE_B, cm1.getValue(PROP_STRING));
        assertEquals(true, cm3.isConfirmed(PROP_STRING));
        assertEquals(VALUE_B, cm3.getValue(PROP_STRING));
        assertEquals(VALUE_B, cm2.getLatestUnconfirmedValue(PROP_STRING));
        assertEquals(VALUE_B, cm2.getLatestConfirmedValue(PROP_STRING));
        assertEquals(VALUE_B, cm2.getConfirmedValue(PROP_STRING));

        // test auto refetch at getConfirmedValue
        cm3.setValue(PROP_STRING, VALUE_C);
        try {
            cm2.getConfirmedValue(PROP_STRING);
            fail("Expected PendingChangeException.");
        }
        catch (PendingChangeException e) {
            assertEquals(PROP_STRING, e.getProperty());
            assertEquals(VALUE_C, e.getNewValue());
            assertEquals(VALUE_B, e.getOldValue());
        }
        assertEquals(VALUE_C, cm1.getConfirmedValue(PROP_STRING));

        // test null value
        cm1.setValue(PROP_STRING, null);
        assertEquals(VALUE_C, cm2.getValue(PROP_STRING));
        cm2.refetch();
		assertNull(cm2.getValue(PROP_STRING));
        assertEquals(false, cm1.isConfirmed(PROP_STRING));
        assertEquals(true, cm3.isConfirmed(PROP_STRING));
		assertNull(cm3.getConfirmedValue(PROP_STRING));

        // test value overwriting
        cm1.setValue(PROP_STRING, VALUE_A);
        cm1.setValue(PROP_DOUBLE, Double.valueOf(1.23456789));
        cm2.setValue(PROP_INT, Integer.valueOf(42));
        assertEquals(false, cm1.isConfirmed(PROP_STRING));
        assertEquals(false, cm1.isConfirmed(PROP_DOUBLE));
        cm3.setValue(PROP_STRING, VALUE_B);
        assertEquals(false, cm3.isConfirmed(PROP_STRING));
        assertEquals(true, cm3.isConfirmed(PROP_DOUBLE));
        try {
            cm1.getConfirmedValue(PROP_STRING);
            fail("Expected PendingChangeException.");
        }
        catch (PendingChangeException e) {
            assertEquals(PROP_STRING, e.getProperty());
            assertEquals(VALUE_B, e.getNewValue());
			assertNull(e.getOldValue());
        }
        assertEquals(VALUE_B, cm2.getConfirmedValue(PROP_STRING));
        assertEquals(true, cm2.isConfirmed(PROP_DOUBLE));

        // test setValueIfUnchanged
        cm1.setValueIfUnchanged(PROP_STRING, VALUE_C);
        assertEquals(VALUE_C, cm1.getValue(PROP_STRING));
        assertEquals(false, cm1.isConfirmed(PROP_STRING));
        try {
            cm2.setValueIfUnchanged(PROP_STRING, VALUE_A);
            fail("Expected PendingChangeException.");
        }
        catch (PendingChangeException e) {
            assertEquals(PROP_STRING, e.getProperty());
            assertEquals(VALUE_C, e.getNewValue());
            assertEquals(VALUE_B, e.getOldValue());
        }
        cm2.setValue(PROP_STRING, VALUE_A);
        try {
            cm2.setValueIfUnchanged(PROP_STRING, VALUE_B);
            fail("Expected PendingChangeException.");
        }
        catch (PendingChangeException e) {
            assertEquals(PROP_STRING, e.getProperty());
            assertEquals(VALUE_A, e.getNewValue());
            assertEquals(VALUE_B, e.getOldValue());
        }

        // test undeclare
        assertEquals(false, cm1.isDeclared(PROP_4));
        cm1.declareValue(PROP_4, PropertyType.STRING);
        assertEquals(true, cm1.isDeclared(PROP_4));
		assertNull(cm1.getValue(PROP_4));
        cm1.setValue(PROP_4, VALUE_A);
        assertEquals(VALUE_A, cm1.getValue(PROP_4));
        cm1.undeclareValue(PROP_4);
        assertEquals(false, cm1.isDeclared(PROP_4));
        try {
            cm1.getValue(PROP_4);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
        cm1.declareValue(PROP_4, PropertyType.STRING);
        assertEquals(true, cm1.isDeclared(PROP_4));
        assertEquals(VALUE_A, cm1.getValue(PROP_4));

        // test illegal calls
        try {
            cm1.getValue("NOT_DECLARED_PROPERTY");
            fail("Expected IllegalArgumentException.");
        }
        catch(IllegalArgumentException e) {
            // expected
        }
        try {
            cm1.setValue("NOT_DECLARED_PROPERTY", VALUE_A);
            fail("Expected IllegalArgumentException.");
        }
        catch(IllegalArgumentException e) {
            // expected
        }
        try {
            cm1.setValue(PROP_INT, "InvalidValue");
            fail("Expected Exception.");
        }
        catch(Exception e) {
            // expected
        }

        try {
            cm1.setValue(PROP_STRING, Integer.valueOf(5));
            fail("Expected IllegalArgumentException.");
        }
        catch(IllegalArgumentException e) {
            // expected
        }
        cm1.setValue(PROP_STRING, Integer.toString(5));
        cm1.setValue(PROP_LONG, Long.valueOf(1));
        cm1.setValue(PROP_DOUBLE, Double.valueOf(1.2345));
        cm1.setValue(PROP_BOOL, Boolean.TRUE);
        cm1.setValue(PROP_CHAR, 'c');
        cm2.refetch(); cm3.refetch();
        assertEquals("5", cm1.getValue(PROP_STRING));
        assertEquals("5", cm2.getValue(PROP_STRING));
        assertEquals("5", cm3.getValue(PROP_STRING));
        assertEquals(1L, cm1.<Long>getValue(PROP_LONG).longValue());
        assertEquals(1L, cm2.<Long>getValue(PROP_LONG).longValue());
        assertEquals(1L, cm3.<Long>getValue(PROP_LONG).longValue());
        assertEquals(Double.valueOf(1.2345), cm1.getValue(PROP_DOUBLE));
        assertEquals(Double.valueOf(1.2345), cm2.getValue(PROP_DOUBLE));
        assertEquals(Double.valueOf(1.2345), cm3.getValue(PROP_DOUBLE));
		assertEquals('c', (char) cm1.getValue(PROP_CHAR));
		assertEquals('c', (char) cm2.getValue(PROP_CHAR));
		assertEquals('c', (char) cm3.getValue(PROP_CHAR));

        // tear down
        cm1.setNodeState(NodeState.SHUTDOWN);
        cm2.setNodeState(NodeState.SHUTDOWN);
        cm3.setNodeState(NodeState.SHUTDOWN);
        cm1.removeNode();
        cm2.removeNode();
        cm3.removeNode();

        try {
            cm1.setValue(PROP_STRING, VALUE_C);
            fail("Expected IllegalStateException, as cluster manager is already shut down.");
        }
        catch(IllegalStateException e) {
            // expected
        }
    }

	private void startNode(ClusterManager cm) throws Exception {
        cm.setNodeState(NodeState.STARTUP);
        assertEquals(cm.getNodeState(), NodeState.STARTUP);

        assertEquals(false, cm.isDeclared(PROP_BOOL));
        cm.declareValue(PROP_BOOL, PropertyType.BOOLEAN);
        cm.declareValue(PROP_INT, PropertyType.INT);
        cm.declareValue(PROP_LONG, PropertyType.LONG);
        cm.declareValue(PROP_FLOAT, PropertyType.FLOAT);
        cm.declareValue(PROP_DOUBLE, PropertyType.DOUBLE);
        cm.declareValue(PROP_CHAR, PropertyType.CHAR);
        cm.declareValue(PROP_STRING, PropertyType.STRING);
        try {
            cm.declareValue(PROP_STRING, PropertyType.STRING);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(true, cm.isDeclared(PROP_BOOL));

        cm.setNodeState(NodeState.RUNNING);
        assertEquals(cm.getNodeState(), NodeState.RUNNING);
    }



    public void testNoClusterMode() throws Exception {
        ClusterManager cm = new TestableClusterManager(false);
        try {
            cm.setValue(PROP_STRING, VALUE_C);
            fail("Expected IllegalStateException, as cluster manager is not initialized yet.");
        }
        catch(IllegalStateException e) {
            // expected
        }

        // startup
        assertEquals(false, cm.isClusterMode());
        assertEquals(false, cm.isClusterModeActive());
		assertNull(cm.getNodeState());
        cm.initNode();
        assertEquals(cm.getNodeState(), NodeState.WAIT_FOR_STARTUP);
        assertEquals(false, cm.isClusterModeActive());

        startNode(cm);

		assertNull(cm.getValue(PROP_STRING));

        // test simple value setting
        cm.setValue(PROP_STRING, VALUE_A);
        assertEquals(VALUE_A, cm.getValue(PROP_STRING));
        assertEquals(true, cm.isConfirmed(PROP_STRING));
        assertEquals(VALUE_A, cm.getConfirmedValue(PROP_STRING));

        // test null value
        cm.setValue(PROP_STRING, null);
		assertNull(cm.getValue(PROP_STRING));
        assertEquals(true, cm.isConfirmed(PROP_STRING));
		assertNull(cm.getConfirmedValue(PROP_STRING));

        // test value overwriting
        cm.setValue(PROP_STRING, VALUE_A);
        cm.setValue(PROP_DOUBLE, Double.valueOf(1.23456789));
        cm.setValue(PROP_INT, Integer.valueOf(42));
        assertEquals(true, cm.isConfirmed(PROP_STRING));
        assertEquals(true, cm.isConfirmed(PROP_DOUBLE));
        cm.setValue(PROP_STRING, VALUE_B);
        assertEquals(true, cm.isConfirmed(PROP_STRING));
        assertEquals(true, cm.isConfirmed(PROP_DOUBLE));
        assertEquals(VALUE_B, cm.getConfirmedValue(PROP_STRING));

        // test undeclare
        assertEquals(false, cm.isDeclared(PROP_4));
        cm.declareValue(PROP_4, PropertyType.STRING);
        assertEquals(true, cm.isDeclared(PROP_4));
		assertNull(cm.getValue(PROP_4));
        cm.setValue(PROP_4, VALUE_A);
        assertEquals(VALUE_A, cm.getValue(PROP_4));
        cm.undeclareValue(PROP_4);
        assertEquals(false, cm.isDeclared(PROP_4));
        try {
            cm.getValue(PROP_4);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
        cm.declareValue(PROP_4, PropertyType.STRING);
        assertEquals(true, cm.isDeclared(PROP_4));
        assertEquals(VALUE_A, cm.getValue(PROP_4));

        // test illegal calls
        try {
            cm.getValue("NOT_DECLARED_PROPERTY");
            fail("Expected IllegalArgumentException.");
        }
        catch(IllegalArgumentException e) {
            // expected
        }
        try {
            cm.setValue("NOT_DECLARED_PROPERTY", VALUE_A);
            fail("Expected IllegalArgumentException.");
        }
        catch(IllegalArgumentException e) {
            // expected
        }
        try {
            cm.setValue(PROP_INT, "InvalidValue");
            fail("Expected Exception.");
        }
        catch(Exception e) {
            // expected
        }
        try {
            cm.setValue(PROP_STRING, Integer.valueOf(5));
            fail("Expected IllegalArgumentException.");
        }
        catch(IllegalArgumentException e) {
            // expected
        }

        cm.setValue(PROP_STRING, Integer.toString(5));
        cm.setValue(PROP_LONG, Long.valueOf(1));
        cm.setValue(PROP_DOUBLE, Double.valueOf(1.2345));
        cm.setValue(PROP_BOOL, Boolean.TRUE);
        cm.setValue(PROP_CHAR, 'c');
        assertEquals("5", cm.getValue(PROP_STRING));
        assertEquals(1L, cm.<Long>getValue(PROP_LONG).longValue());
        assertEquals(Double.valueOf(1.2345), cm.getValue(PROP_DOUBLE));
		assertEquals('c', (char) cm.getValue(PROP_CHAR));
        assertEquals("5", cm.getConfirmedValue(PROP_STRING));
        assertEquals(1L, cm.<Long>getConfirmedValue(PROP_LONG).longValue());
        assertEquals(Double.valueOf(1.2345), cm.getConfirmedValue(PROP_DOUBLE));
		assertEquals('c', (char) cm.getConfirmedValue(PROP_CHAR));

        // tear down
        cm.setNodeState(NodeState.SHUTDOWN);
        cm.removeNode();

        try {
            cm.setValue(PROP_STRING, VALUE_C);
            fail("Expected IllegalStateException, as cluster manager is already shut down.");
        }
        catch(IllegalStateException e) {
            // expected
        }
    }



    /**
     * This test simulates a cluster startup with 3 nodes.
     */
    public void testInterleavedClusterMode() throws Exception {

        ClusterManager cm1 = new TestableClusterManager(true);
        ClusterManager cm2 = new TestableClusterManager(true);
        ClusterManager cm3 = new TestableClusterManager(true);

        insertOldEntries(cm1);

        // activating node 1
        cm1.initNode();

		DBHelper sqlDialect = cm1.getConnectionPool().getSQLDialect();

        // test cleanup old entries
        assertNodes(cm1);
		assertFalse(
			DBUtil.executeQueryAsBoolean("SELECT * FROM " + sqlDialect.tableRef(ClusterManagerDBExecutor.TABLE_VALUE)));



        // startup node 1
        cm1.setNodeState(NodeState.STARTUP);

        cm1.declareValue(PROP_1, PropertyType.STRING);
		assertNull(cm1.getConfirmedValue(PROP_1));
        cm1.setValue(PROP_1, VALUE_A);
        assertEquals(VALUE_A, cm1.getConfirmedValue(PROP_1));

        // activating other nodes
        cm2.initNode();
        cm3.initNode();
        assertNodes(cm1, cm2, cm3);
        assertEquals(VALUE_A, cm1.getConfirmedValue(PROP_1));

        // startup node 1 (continue)
        cm1.declareValue(PROP_2, PropertyType.STRING);
		assertNull(cm1.getConfirmedValue(PROP_2));
        cm1.setValue(PROP_2, VALUE_B);
        assertEquals(VALUE_B, cm1.getConfirmedValue(PROP_2));
        cm1.declareValue(PROP_3, PropertyType.STRING);

        cm1.setNodeState(NodeState.RUNNING);



        // startup node 2
        cm2.setNodeState(NodeState.STARTUP);

        cm2.declareValue(PROP_1, PropertyType.STRING);
        assertEquals(VALUE_A, cm2.getConfirmedValue(PROP_1));
        assertEquals(VALUE_A, cm1.getConfirmedValue(PROP_1));

        cm1.refetch(); // parallel cluster access

        cm2.declareValue(PROP_2, PropertyType.STRING);
        assertEquals(VALUE_B, cm2.getConfirmedValue(PROP_2));
        cm2.setValue(PROP_2, VALUE_C);

        cm2.declareValue(PROP_3, PropertyType.STRING);
		assertNull(cm2.getConfirmedValue(PROP_3));

        cm2.setNodeState(NodeState.RUNNING);



        // startup node 3
        cm3.setNodeState(NodeState.STARTUP);
        cm3.declareValue(PROP_1, PropertyType.STRING);
        assertEquals(VALUE_A, cm3.getConfirmedValue(PROP_1));

        cm3.declareValue(PROP_2, PropertyType.STRING);
        try {
            cm3.getConfirmedValue(PROP_2);
            fail("Expected PendingChangeException.");
        }
        catch (PendingChangeException e) {
            assertEquals(PROP_2, e.getProperty());
            assertEquals(VALUE_C, e.getNewValue());
            assertEquals(VALUE_B, e.getOldValue());
        }
        assertEquals(VALUE_C, cm3.getLatestUnconfirmedValue(PROP_2));
        cm1.refetch();
        assertEquals(VALUE_C, cm3.getConfirmedValue(PROP_2));
        cm3.declareValue(PROP_3, PropertyType.STRING);
		assertNull(cm3.getConfirmedValue(PROP_3));

        cm3.setNodeState(NodeState.RUNNING);
        cm1.refetch(); cm2.refetch(); cm3.refetch();

        cm1.setValue(PROP_1, VALUE_A);
        cm1.setValue(PROP_2, VALUE_A);
        cm1.setValue(PROP_3, VALUE_A);
        cm1.refetch(); cm2.refetch(); cm3.refetch();
        assertNodes(cm1, cm2, cm3);



        // all nodes are running, testing not responding nodes
        cm1.setValue(PROP_1, VALUE_B);
        timeout(cm3);
        cm2.refetch();
        assertNodes(cm1, cm2);
        assertEquals(VALUE_B, cm1.getConfirmedValue(PROP_1));
        cm2.setValue(PROP_2, VALUE_B);
        assertEquals(VALUE_B, cm1.getConfirmedValue(PROP_2));
        assertNodes(cm1, cm2);

        cm3.refetch();
        assertNodes(cm1, cm2, cm3);
        assertEquals(VALUE_B, cm3.getValue(PROP_1));
        assertEquals(VALUE_B, cm3.getValue(PROP_2));
        assertEquals(VALUE_B, cm1.getConfirmedValue(PROP_1));
        assertEquals(VALUE_B, cm1.getConfirmedValue(PROP_2));
        assertEquals(VALUE_B, cm3.getConfirmedValue(PROP_1));
        assertEquals(VALUE_B, cm3.getConfirmedValue(PROP_2));

        cm1.refetch(); cm2.refetch(); cm3.refetch();
        assertNodes(cm1, cm2, cm3);



        // shutdown
        cm1.setNodeState(NodeState.SHUTDOWN);
        cm2.setValue(PROP_3, VALUE_C);
        cm3.refetch();
        assertEquals(VALUE_C, cm3.getConfirmedValue(PROP_3));
        cm1.refetch();
        assertEquals(VALUE_C, cm1.getValue(PROP_3));
        cm1.setValue(PROP_1, VALUE_C);
        cm2.refetch();
        assertEquals(VALUE_C, cm2.getValue(PROP_1));
        assertEquals(VALUE_C, cm3.getConfirmedValue(PROP_1));

        assertNodes(cm1, cm2, cm3);
        cm1.removeNode();
        assertNodes(cm2, cm3);

        cm2.setNodeState(NodeState.SHUTDOWN);
        cm3.setNodeState(NodeState.SHUTDOWN);

        cm2.setValue(PROP_2, VALUE_C);
        assertEquals(VALUE_C, cm2.getConfirmedValue(PROP_2));
        assertEquals(VALUE_B, cm3.getValue(PROP_2));
        assertEquals(VALUE_C, cm3.getConfirmedValue(PROP_2));

        cm2.removeNode();
        assertNodes(cm3);

        cm3.removeNode();
        assertNodes();

        // assert clean tables
		assertFalse(
			DBUtil.executeQueryAsBoolean("SELECT * FROM " + sqlDialect.tableRef(ClusterManagerDBExecutor.TABLE_NODE)));
		assertFalse(
			DBUtil.executeQueryAsBoolean("SELECT * FROM " + sqlDialect.tableRef(ClusterManagerDBExecutor.TABLE_VALUE)));
    }



    private void assertNodes(ClusterManager... cms) throws SQLException {
		if (cms.length > 0) {
			ClusterManager clusterManager = cms[0];
			DBHelper sqlDialect = clusterManager.getConnectionPool().getSQLDialect();
			String statement =
				"SELECT " + sqlDialect.columnRef(ClusterManagerDBExecutor.COLUMN_ID) + " FROM "
					+ sqlDialect.tableRef(ClusterManagerDBExecutor.TABLE_NODE);
			List<String> entries = DBUtil.executeQueryAsStringList(statement);
			assertEquals(cms.length, entries.size());
			List<String> expected = new ArrayList<>(cms.length);
			for (ClusterManager cm : cms) {
				expected.add(((TestableClusterManager) cm).getNodeID().toString());
			}
			assertTrue(CollectionUtil.containsSame(expected, entries));
		}
    }

    private void insertOldEntries(ClusterManager cm) throws SQLException {
        Object[] params;
        long dbnow = DBUtil.currentTimeMillis();
		DBHelper sqlDialect = cm.getConnectionPool().getSQLDialect();
		params =
			ArrayUtil.<Object> createArray(Long.valueOf(-8), "RUNNING",
				Long.valueOf(dbnow - 2 * ((TestableClusterManager) cm).getTimeoutRunningNode()), Long.valueOf(15));
		String tableNodeRef = sqlDialect.tableRef(ClusterManagerDBExecutor.TABLE_NODE);
		String tableValueRef = sqlDialect.tableRef(ClusterManagerDBExecutor.TABLE_VALUE);
		DBUtil.executeDirectUpdate("INSERT INTO " + tableNodeRef + " VALUES (?, ?, ?, ?)", params);
		params =
			ArrayUtil.<Object> createArray(Long.valueOf(-9), "STARTUP",
				Long.valueOf(dbnow - 2 * ((TestableClusterManager) cm).getTimeoutOtherNode()), Long.valueOf(28));
		DBUtil.executeDirectUpdate("INSERT INTO " + tableNodeRef + " VALUES (?, ?, ?, ?)", params);
		params = ArrayUtil.<Object> createArray(PROP_1, "VERY_OLD_VALUE", "EVEN_OLDER_VALUE", 78);
		DBUtil.executeDirectUpdate("INSERT INTO " + tableValueRef + " VALUES (?, ?, ?, ?)", params);

		String statement =
			"SELECT " + sqlDialect.columnRef(ClusterManagerDBExecutor.COLUMN_ID) + " FROM " + tableNodeRef;
		List<String> entries = DBUtil.executeQueryAsStringList(statement);
        assertTrue(CollectionUtil.containsSame(entries, CollectionUtil.createSet("-8", "-9")));
		statement = "SELECT " + sqlDialect.columnRef(ClusterManagerDBExecutor.COLUMN_NAME) + " FROM " + tableValueRef;
        entries = DBUtil.executeQueryAsStringList(statement);
        assertTrue(CollectionUtil.containsSame(entries, CollectionUtil.createSet(PROP_1)));
    }

    private void timeout(ClusterManager cm) throws SQLException {
        long dbnow = DBUtil.currentTimeMillis();
        Object[] params = ArrayUtil.createArray(Long.valueOf(dbnow - 2 * ((TestableClusterManager)cm).getTimeoutRunningNode()), ((TestableClusterManager)cm).getNodeID());
		DBHelper sqlDialect = cm.getConnectionPool().getSQLDialect();
		String statement = "UPDATE " + sqlDialect.tableRef(ClusterManagerDBExecutor.TABLE_NODE) + " SET "
			+ sqlDialect.columnRef(ClusterManagerDBExecutor.COLUMN_LIFE_SIGN) + " = ? WHERE "
			+ sqlDialect.columnRef(ClusterManagerDBExecutor.COLUMN_ID) + " = ?";
        DBUtil.executeDirectUpdate(statement, params);
    }



	/**
	 * Required to get access to protected methods and fields to build up test scenario.
	 * 
	 * OTOH this does not use the ModuleSystem.
	 */
    private static class TestableClusterManager extends ClusterManager {

		public TestableClusterManager(boolean clusterMode) throws ClusterManagerException {
			super(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, createConfig(clusterMode), false);
        }

		private static Config createConfig(boolean clusterMode) {
			Map<String, String> values = new HashMap<>();
			values.put(ClusterManager.Config.CLUSTER_PROPERTY, Boolean.toString(clusterMode));
			try {
				return TypedConfiguration.newConfigItem(ClusterManager.Config.class, values.entrySet());
			} catch (ConfigurationException ex) {
				throw new ConfigurationError("Invalid test values: " + values, ex);
			}
		}

        protected Long getNodeID() {
            return nodeID;
        }

        protected long getTimeoutRunningNode() {
            return timeoutRunningNode;
        }

        protected long getTimeoutOtherNode() {
            return timeoutOtherNode;
        }

    }

	public static Test suite() {
		Test test = DatabaseTestSetup.getDBTest(TestClusterManager.class, new TestFactory() {

			@Override
			public Test createSuite(Class<? extends Test> testCase, String suiteName) {
				TestSuite suite = new TestSuite(testCase);
				suite.setName(suiteName);
				Test innerTest = suite;
				innerTest = ClusterManagerSetup.setupClusterTables(innerTest);
				return innerTest;
			}
		});
		test = TLTestSetup.createTLTestSetup(test);
		return test;
	}

}
