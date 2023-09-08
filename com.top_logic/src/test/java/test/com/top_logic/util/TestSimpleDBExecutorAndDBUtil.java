/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.SimpleDBExecutor;
import com.top_logic.util.db.DBUtil;

/**
 * Testcase for the SimpleDBExecutor and DBUtil classes.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class TestSimpleDBExecutorAndDBUtil extends BasicTestCase {

    public static Test suite() {
        return KBSetup.getKBTest(TestSimpleDBExecutorAndDBUtil.class);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dropTestTable();
        createTestTable();
    }

    @Override
    protected void tearDown() throws Exception {
        dropTestTable();
        super.tearDown();
    }

	private static final DBType[] TYPES = {
		DBType.STRING,
		DBType.INT,
		DBType.LONG,
		DBType.STRING,
		DBType.LONG
	};

    private void createTestTable() throws Exception {
		DBHelper sqlDialect = sqlDialect();
		int col = 0;
		StringBuilder sb = new StringBuilder();

		sb.append("CREATE TABLE ");
		sb.append(sqlDialect.tableRef("TEST_DBUTIL"));
		sb.append(" ( ");
		sb.append(sqlDialect.columnRef("TEST_ID"));
		sb.append(" ");
		sb.append(getDBType(TYPES[col++], 32, 0, true, false));
		sb.append(", ");
		sb.append(sqlDialect.columnRef("TEST_INT"));
		sb.append(" ");
		sb.append(getDBType(TYPES[col++], 0, 0, false, false));
		sb.append(", ");
		sb.append(sqlDialect.columnRef("TEST_LONG"));
		sb.append(" ");
		sb.append(getDBType(TYPES[col++], 20, 0, false, false));
		sb.append(", ");
		sb.append(sqlDialect.columnRef("TEST_STRING"));
		sb.append(" ");
		sb.append(getDBType(TYPES[col++], 256, 0, false, false));
		sb.append(", ");
		sb.append(sqlDialect.columnRef("TEST_DATE"));
		sb.append(" ");
		sb.append(getDBType(TYPES[col++], 20, 0, false, false));
		sb.append(", ");
		sb.append("PRIMARY KEY (" + sqlDialect.columnRef("TEST_ID") + ") )");

		DBUtil.executeDirectUpdate(sb.toString());
    }

	private String getDBType(DBType dbType, int size, int precision, boolean mandatory, boolean binary)
			throws SQLException {
		StringBuilder result = new StringBuilder();
		sqlDialect().appendDBType(result, dbType, "foo", size, precision, mandatory, binary);
		return result.toString();
	}

	private DBHelper sqlDialect() throws SQLException {
		return ConnectionPoolRegistry.getDefaultConnectionPool().getSQLDialect();
	}

	private void dropTestTable() throws Exception {
        try {
			DBUtil.executeDirectUpdate("DROP TABLE " + sqlDialect().tableRef("TEST_DBUTIL"));
        }
        catch (SQLException e) {
            // table doesn't exist, good
            // ... or perhaps database connection is broken - ignore
        }
    }

    private <T> T[] params(T... elements) {
        return elements;
    }

    private static final String TEXT = "This is an example text.";
    private static final String TEXT1 = "THIS IS A VERY VERY LONG STRING.";
    private static final String TEXT2 = "This string is even longer than the yery yery long string.";
    private static final String TEXT3 = " ";
    private static final String TEXT4 = null;



    public void testDBUtil() throws Exception {
		DBHelper sqlDialect = sqlDialect();
		String testDBUtiltableRef = sqlDialect.tableRef("TEST_DBUTIL");
		String testIDColumnRef = sqlDialect.columnRef("TEST_ID");
		String testIntColumnRef = sqlDialect.columnRef("TEST_INT");
		String testLongColumnRef = sqlDialect.columnRef("TEST_LONG");
		String testStringColumnRef = sqlDialect.columnRef("TEST_STRING");
		String testDateColumnRef = sqlDialect.columnRef("TEST_DATE");

        Long now = Long.valueOf(System.currentTimeMillis());
		String nowString = now.toString();
		assertFalse(DBUtil
			.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " WHERE " + testLongColumnRef + " > 42"));
		assertEquals(1, DBUtil.executeDirectUpdate("INSERT INTO " + testDBUtiltableRef + " VALUES (?, ?, ?, ?, ?)",
			params("TEST01", 5, 5, null, null)));
		assertFalse(
			DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " WHERE " + testLongColumnRef + " > ?",
				params(42)));
		assertEquals(1, DBUtil.executeDirectUpdate("INSERT INTO " + testDBUtiltableRef + " VALUES (?, ?, ?, ?, ?)",
			params("TEST02", Integer.valueOf(5), Long.valueOf(50L), null, now)));
		assertTrue(
			DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " WHERE " + testLongColumnRef + " > ?",
			params(Long.valueOf(42))));
		assertEquals(1, DBUtil.executeDirectUpdate("INSERT INTO " + testDBUtiltableRef + " VALUES (?, ?, ?, ?, ?)",
			params("TEST03", Integer.valueOf(42), null, TEXT, now)));
		assertTrue(
			DBUtil.executeQueryAsBoolean("SELECT * FROM " + testDBUtiltableRef + " WHERE " + testLongColumnRef + " > ?",
			params(Integer.valueOf(42))));
		assertEquals(1,
			DBUtil.executeQueryAsInteger(
				"SELECT COUNT(*) FROM " + testDBUtiltableRef + " WHERE " + testLongColumnRef + " > 42"));
		assertEquals(3, DBUtil.executeQueryAsInteger("SELECT COUNT(*) FROM " + testDBUtiltableRef + ""));
		assertEquals(5,
			DBUtil.executeQueryAsInteger(
				"SELECT " + testIntColumnRef + " FROM " + testDBUtiltableRef + " WHERE " + testIDColumnRef
					+ " = 'TEST02'"));
		assertEquals(50L,
			DBUtil.executeQueryAsLong(
				"SELECT " + testLongColumnRef + " FROM " + testDBUtiltableRef + " WHERE " + testIDColumnRef
					+ " = 'TEST02'"));
		assertEquals(0L, DBUtil.executeQueryAsLong(
			"SELECT " + testLongColumnRef + " FROM " + testDBUtiltableRef + " WHERE " + testIDColumnRef + " = ?",
			params("TEST03")));
		assertEquals(TEXT,
			DBUtil.executeQueryAsString(
				"SELECT " + testStringColumnRef + " FROM " + testDBUtiltableRef + " WHERE " + testIDColumnRef
					+ " = 'TEST03'"));
		assertEquals(new Date(now.longValue()),
			DBUtil.executeQueryAsJavaDate(
				"SELECT " + testDateColumnRef + " FROM " + testDBUtiltableRef + " WHERE " + testIDColumnRef
					+ " = 'TEST03'"));
		assertEquals(now.longValue(),
			DBUtil.executeQueryAsLong(
				"SELECT " + testDateColumnRef + " FROM " + testDBUtiltableRef + " WHERE " + testIDColumnRef
					+ " = 'TEST03'"));

		List<String> resultList =
			DBUtil.executeQueryAsStringList(
				"SELECT " + testIDColumnRef + " FROM " + testDBUtiltableRef + " ORDER BY " + testIDColumnRef + "");
        assertEquals(3, resultList.size());
        assertTrue(CollectionUtil.containsSame(resultList, CollectionUtil.createSet("TEST01", "TEST02", "TEST03")));

		List<Object[]> resultMatrix =
			DBUtil.executeQueryAsMatrix("SELECT * FROM " + testDBUtiltableRef + " ORDER BY " + testIDColumnRef + "",
				TYPES);
        assertEquals(3, resultMatrix.size());
		assertTrue(ArrayUtil.equals(resultMatrix.get(0), ArrayUtil.createArray("TEST01", 5, 5L, null, null)));
		assertTrue(ArrayUtil.equals(resultMatrix.get(1), ArrayUtil.createArray("TEST02", 5, 50L, null, now)));
		assertTrue(ArrayUtil.equals(resultMatrix.get(2), ArrayUtil.createArray("TEST03", 42, null, TEXT, now)));

		String[][] resultTable =
			DBUtil.executeQueryAsTable("SELECT * FROM " + testDBUtiltableRef + " ORDER BY " + testIDColumnRef + "");
        assertEquals(4, resultTable.length);
		assertTrue(ArrayUtil.equals(resultTable[0],
			ArrayUtil.createArray("TEST_ID", "TEST_INT", "TEST_LONG",
				"TEST_STRING",
				"TEST_DATE")));
        assertTrue(ArrayUtil.equals(resultTable[1], ArrayUtil.createArray("TEST01", "5", "5", null, null)));
		assertTrue(ArrayUtil.equals(resultTable[2], ArrayUtil.createArray("TEST02", "5", "50", null, nowString)));
		assertTrue(ArrayUtil.equals(resultTable[3], ArrayUtil.createArray("TEST03", "42", null, TEXT, nowString)));

		assertFalse(
			DBUtil.executeQueryAsBoolean(
				"SELECT 1 FROM " + testDBUtiltableRef + " WHERE " + testIDColumnRef + " = 'NOT_EXISTING_ID'"));
		assertEquals(0, DBUtil.executeQueryAsInteger(
			"SELECT " + testIntColumnRef + " FROM " + testDBUtiltableRef + " WHERE " + testIDColumnRef
				+ " = 'NOT_EXISTING_ID'"));
		assertEquals(0L, DBUtil
			.executeQueryAsLong(
				"SELECT " + testLongColumnRef + " FROM " + testDBUtiltableRef + " WHERE " + testIDColumnRef
					+ " = 'NOT_EXISTING_ID'"));
		assertEquals(null, DBUtil.executeQueryAsString(
			"SELECT " + testStringColumnRef + " FROM " + testDBUtiltableRef + " WHERE " + testIDColumnRef
				+ " = 'NOT_EXISTING_ID'"));
		assertEquals(null, DBUtil.executeQueryAsJavaDate(
			"SELECT " + testDateColumnRef + " FROM " + testDBUtiltableRef + " WHERE " + testIDColumnRef
				+ " = 'NOT_EXISTING_ID'"));

		resultList = DBUtil.executeQueryAsStringList(
			"SELECT " + testIDColumnRef + " FROM " + testDBUtiltableRef + " WHERE " + testIDColumnRef
				+ " = 'NOT_EXISTING_ID'");
        assertNotNull(resultList);
        assertEquals(0, resultList.size());

		resultMatrix =
			DBUtil.executeQueryAsMatrix(
				"SELECT * FROM " + testDBUtiltableRef + " WHERE " + testIDColumnRef + " = 'NOT_EXISTING_ID'",
				TYPES);
        assertNotNull(resultMatrix);
        assertEquals(0, resultMatrix.size());

		resultTable =
			DBUtil.executeQueryAsTable(
				"SELECT * FROM " + testDBUtiltableRef + " WHERE " + testIDColumnRef + " = 'NOT_EXISTING_ID'");
        assertNotNull(resultTable);
        assertEquals(1, resultTable.length);
		assertTrue(ArrayUtil.equals(resultTable[0],
			ArrayUtil.createArray("TEST_ID", "TEST_INT", "TEST_LONG", "TEST_STRING", "TEST_DATE")));

        try {
            DBUtil.executeDirectUpdate("ILLEGAL SQL STATEMENT");
            fail("Expected SQL-Exception at illegal statement.");
        }
        catch (SQLException e) {
            // expected
        }

		DBUtil.executeDirectUpdate("DELETE FROM " + testDBUtiltableRef + "");
		assertFalse(DBUtil.executeQueryAsBoolean("SELECT * FROM " + testDBUtiltableRef + ""));
		assertEquals(0, DBUtil.executeQueryAsInteger("SELECT COUNT(*) FROM " + testDBUtiltableRef + ""));
    }



    public void testMaxValues() throws Exception {
		DBHelper sqlDialect = sqlDialect();
		String testDBUtiltableRef = sqlDialect.tableRef("TEST_DBUTIL");
		assertFalse(DBUtil.executeQueryAsBoolean("SELECT * FROM " + testDBUtiltableRef + ""));

		assertEquals(1,
			DBUtil.executeDirectUpdate("INSERT INTO " + testDBUtiltableRef + " VALUES (?, ?, ?, ?, ?)",
				params(TEXT1, Integer.valueOf(Integer.MAX_VALUE), Long.valueOf(Long.MAX_VALUE), TEXT2,
					Long.valueOf(Long.MAX_VALUE))));
		Object[] result1 =
			CollectionUtil.getFirst(DBUtil.executeQueryAsMatrix("SELECT * FROM " + testDBUtiltableRef + "", TYPES));
		assertEquals(TEXT1, result1[0]);
		assertEquals(Integer.MAX_VALUE, result1[1]);
		assertEquals(Long.MAX_VALUE, result1[2]);
		assertEquals(TEXT2, result1[3]);
		assertEquals(new Date(Long.MAX_VALUE), new Date((Long) result1[4]));

		DBUtil.executeDirectUpdate("DELETE FROM " + testDBUtiltableRef + "");
		assertFalse(DBUtil.executeQueryAsBoolean("SELECT * FROM " + testDBUtiltableRef + ""));

		assertEquals(1,
			DBUtil.executeDirectUpdate("INSERT INTO " + testDBUtiltableRef + " VALUES (?, ?, ?, ?, ?)",
				params(TEXT3, Integer.valueOf(Integer.MIN_VALUE), Long.valueOf(Long.MIN_VALUE), TEXT4,
					Long.valueOf(Long.MIN_VALUE))));
		Object[] result =
			CollectionUtil.getFirst(DBUtil.executeQueryAsMatrix("SELECT * FROM " + testDBUtiltableRef + "", TYPES));
        assertEquals(TEXT3, result[0]);
		assertEquals(Integer.MIN_VALUE, result[1]);
		assertEquals(Long.MIN_VALUE, result[2]);
        assertEquals(TEXT4, result[3]);
		assertEquals(new Date(Long.MIN_VALUE), new Date((Long) result[4]));

        // test currentTimeMillis()
        long diff = 1000 * 60 * 60 * 24 * 2; // 2 days
        long now = System.currentTimeMillis();
        long dbnow = DBUtil.currentTimeMillis();
        boolean inInterval = dbnow > now - diff && dbnow < now + diff;
        assertTrue("Current time of system differs from current time of the data base by more than 2 days. System-Time: " + new Date(now) + ", DB time: " + new Date(dbnow), inInterval);
    }



    public void testSimpleDBExecutorOwn() throws Exception {
		DBHelper sqlDialect = sqlDialect();
		String testDBUtiltableRef = sqlDialect.tableRef("TEST_DBUTIL");

		assertFalse(DBUtil.executeQueryAsBoolean("SELECT * FROM " + testDBUtiltableRef + ""));
        KnowledgeBase kb = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
        SimpleDBExecutor db = new SimpleDBExecutor();

        try {
            try {
				db.executeUpdate("INSERT INTO " + testDBUtiltableRef + " VALUES (?, ?, ?, ?, ?)",
					params("TEST19", null, null, null, null));
                fail("Expected IllegalArgumentException.");
            }
            catch (IllegalArgumentException e) {
                // expected;
            }

            assertNull(db.getTransactionConnection());
            db.beginTransaction();
            assertNotNull(db.getTransactionConnection());

            try {
                db.beginTransaction();
                fail("Expected IllegalArgumentException.");
            }
            catch (IllegalArgumentException e) {
                // expected;
            }
            try {
                db.beginTransaction((CommitHandler)kb);
                fail("Expected IllegalArgumentException.");
            }
            catch (IllegalArgumentException e) {
                // expected;
            }

			assertEquals(1, db.executeUpdate("INSERT INTO " + testDBUtiltableRef + " VALUES (?, ?, ?, ?, ?)",
				params("TEST12", null, null, null, null)));

			String testIDColumnRef = sqlDialect.columnRef("TEST_ID");

			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST11'"));
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'"));
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST13'"));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM "
				+ testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef
				+ " = 'TEST11'", null));
			assertTrue(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM " + testDBUtiltableRef
				+ " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'", null));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM "
				+ testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef
				+ " = 'TEST13'", null));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(),
				"SELECT 1 FROM " + testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE "
					+ testIDColumnRef + " = ?",
				params("TEST11")));
			assertTrue(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(),
				"SELECT 1 FROM " + testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE "
					+ testIDColumnRef + " = ?",
				params("TEST12")));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(),
				"SELECT 1 FROM " + testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE "
					+ testIDColumnRef + " = ?",
				params("TEST13")));

            kb.commit();
            assertNotNull(db.getTransactionConnection());
            // assert nothing has changed as this is a own connection
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST11'"));
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'"));
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST13'"));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM "
				+ testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef
				+ " = 'TEST11'", null));
			assertTrue(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM " + testDBUtiltableRef
				+ " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'", null));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM "
				+ testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef
				+ " = 'TEST13'", null));

            kb.rollback();
            assertNotNull(db.getTransactionConnection());
            // assert nothing has changed as this is a own connection
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST11'"));
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'"));
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST13'"));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM "
				+ testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef
				+ " = 'TEST11'", null));
			assertTrue(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM " + testDBUtiltableRef
				+ " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'", null));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM "
				+ testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef
				+ " = 'TEST13'", null));

            db.commitTransaction();
            assertNotNull(db.getTransactionConnection());

			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST11'"));
			assertTrue(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'"));
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST13'"));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM "
				+ testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef
				+ " = 'TEST11'", null));
			assertTrue(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM " + testDBUtiltableRef
				+ " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'", null));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM "
				+ testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef
				+ " = 'TEST13'", null));

			assertEquals(1, db.executeUpdate(
				"INSERT INTO " + testDBUtiltableRef + " VALUES (?, NULL, NULL, NULL, NULL)", params("TEST13")));

			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST11'"));
			assertTrue(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'"));
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST13'"));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM "
				+ testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef
				+ " = 'TEST11'", null));
			assertTrue(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM " + testDBUtiltableRef
				+ " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'", null));
			assertTrue(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM " + testDBUtiltableRef
				+ " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST13'", null));

            db.rollbackTransaction();
            assertNotNull(db.getTransactionConnection());

			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST11'"));
			assertTrue(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'"));
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST13'"));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM "
				+ testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef
				+ " = 'TEST11'", null));
			assertTrue(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM " + testDBUtiltableRef
				+ " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'", null));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM "
				+ testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef
				+ " = 'TEST13'", null));

            db.closeTransaction();
            assertNull(db.getTransactionConnection());
            try {
				db.executeUpdate("INSERT INTO " + testDBUtiltableRef + " VALUES (?, ?, ?, ?, ?)",
					params("TEST19", null, null, null, null));
                fail("Expected IllegalArgumentException.");
            }
            catch (IllegalArgumentException e) {
                // expected;
            }
        }
        finally {
            db.closeTransaction();
            db.releaseCommitContext();
        }

    }



    public void testSimpleDBExecutorKB() throws Exception {
		DBHelper sqlDialect = sqlDialect();
		String testDBUtiltableRef = sqlDialect.tableRef("TEST_DBUTIL");
		assertFalse(DBUtil.executeQueryAsBoolean("SELECT * FROM " + testDBUtiltableRef));
        KnowledgeBase kb = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
        SimpleDBExecutor db = new SimpleDBExecutor();

        try {
            try {
				db.executeUpdate("INSERT INTO " + testDBUtiltableRef + " VALUES (?, ?, ?, ?, ?)",
					params("TEST19", null, null, null, null));
                fail("Expected IllegalArgumentException.");
            }
            catch (IllegalArgumentException e) {
                // expected;
            }

            assertNull(db.getTransactionConnection());
            db.beginTransaction((CommitHandler)kb);
            assertNotNull(db.getTransactionConnection());

            try {
                db.beginTransaction();
                fail("Expected IllegalArgumentException.");
            }
            catch (IllegalArgumentException e) {
                // expected;
            }

			assertEquals(1, db.executeUpdate("INSERT INTO " + testDBUtiltableRef + " VALUES (?, ?, ?, ?, ?)",
				params("TEST12", null, null, null, null)));

			String testIDColumnRef = sqlDialect.columnRef("TEST_ID");

			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST11'"));
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'"));
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST13'"));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM "
				+ testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef
				+ " = 'TEST11'", null));
			assertTrue(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM " + testDBUtiltableRef
				+ " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'", null));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM "
				+ testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef
				+ " = 'TEST13'", null));

            try {
                db.commitTransaction();
                fail("Expected IllegalArgumentException.");
            }
            catch (IllegalArgumentException e) {
                // expected;
            }

            assertNotNull(db.getTransactionConnection());
            // assert nothing has changed as this is a own connection
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST11'"));
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'"));
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST13'"));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM "
				+ testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef
				+ " = 'TEST11'", null));
			assertTrue(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM " + testDBUtiltableRef
				+ " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'", null));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM "
				+ testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef
				+ " = 'TEST13'", null));

            kb.commit();
            assertNull(db.getTransactionConnection());

			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST11'"));
			assertTrue(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'"));
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST13'"));
            db.beginTransaction((CommitHandler)kb);
            assertNotNull(db.getTransactionConnection());

			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST11'"));
			assertTrue(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'"));
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST13'"));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM "
				+ testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef
				+ " = 'TEST11'", null));
			assertTrue(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM " + testDBUtiltableRef
				+ " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'", null));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM "
				+ testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef
				+ " = 'TEST13'", null));

			assertEquals(1, db.executeUpdate(
				"INSERT INTO " + testDBUtiltableRef + " VALUES (?, NULL, NULL, NULL, NULL)", params("TEST13")));

			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST11'"));
			assertTrue(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'"));
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST13'"));
			assertFalse(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM "
				+ testDBUtiltableRef + " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef
				+ " = 'TEST11'", null));
			assertTrue(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM " + testDBUtiltableRef
				+ " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'", null));
			assertTrue(DBUtil.executeQueryAsBoolean(db.getTransactionConnection(), "SELECT 1 FROM " + testDBUtiltableRef
				+ " " + sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST13'", null));

            kb.rollback();
            assertNull(db.getTransactionConnection());

			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST11'"));
			assertTrue(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST12'"));
			assertFalse(DBUtil.executeQueryAsBoolean("SELECT 1 FROM " + testDBUtiltableRef + " "
				+ sqlDialect.selectNoBlockHint() + " WHERE " + testIDColumnRef + " = 'TEST13'"));

            try {
				db.executeUpdate("INSERT INTO " + testDBUtiltableRef + " VALUES (?, ?, ?, ?, ?)",
					params("TEST19", null, null, null, null));
                fail("Expected IllegalArgumentException.");
            }
            catch (IllegalArgumentException e) {
                // expected;
            }
        }
        finally {
            db.closeTransaction();
            db.releaseCommitContext();
        }

    }

}
