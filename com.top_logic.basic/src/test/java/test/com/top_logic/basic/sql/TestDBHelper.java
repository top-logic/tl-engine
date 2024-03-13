/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import static test.com.top_logic.basic.sql.TestDBHelper.Spec.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.LongID;
import com.top_logic.basic.Settings;
import com.top_logic.basic.TLID;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.EmptyBinaryData;
import com.top_logic.basic.io.binary.MemoryBinaryData;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.MSSQLHelper;
import com.top_logic.basic.sql.MySQL55Helper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.ComputationEx2;

/**
 * Testcase for {@link com.top_logic.basic.sql.DBHelper} and ist subclasses.
 * 
 * As of now Access. Mysql and Oracle are tested.
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
@SuppressWarnings("javadoc")
public class TestDBHelper extends AbstractConnectionTest {
	
	private class TestingBinaryDataFromArray extends MemoryBinaryData {

		private TestingBinaryDataFromArray(byte[] byteData) {
			super(byteData, CONTENT_TYPE_OCTET_STREAM, "DemoData");
		}

	}

	static class Spec {

		private final DBType _type;

		private final long _size;

		private final int _precision;

		private final boolean _binary;

		private final boolean _mandatory;

		private final boolean _canSearch;

		private Spec(DBType type, long size, int precision, boolean mandatory, boolean binary,
				boolean canSearch) {
			_type = type;
			_size = size;
			_precision = precision;
			_mandatory = mandatory;
			_binary = binary;
			_canSearch = canSearch;
		}

		public static Spec spec(DBType dbType) {
			boolean mandatory = false;
			boolean binary = true;
			boolean canSearch = true;
			return spec(dbType, 20, 0, mandatory, binary, canSearch);
		}

		public static Spec spec(DBType dbType, long size, int precision, boolean mandatory, boolean binary,
				boolean canSearch) {
			return new Spec(dbType, size, precision, mandatory, binary, canSearch);
		}

		public DBType type() {
			return _type;
		}

		public long size() {
			return _size;
		}

		public int precision() {
			return _precision;
		}

		public boolean binary() {
			return _binary;
		}

		public boolean mandatory() {
			return _mandatory;
		}

		public boolean canSearch() {
			return _canSearch;
		}

	}

	private static final String TABLE_NAME = ConnectionSetup.TABLE_NAME;

	/**
	 * Mon Mar 20 14:05:40 CET 2006
	 */
	private static final java.util.Date MON_MAR_20_14_05_40_CET_2006 = new java.util.Date(1142859940000L);
    
	public void testBoolean() throws SQLException, ParseException {
		doTest(col(spec(DBType.BOOLEAN), true, false, null));
	}
	
	public void testByte() throws SQLException, ParseException {
		doTest(col(spec(DBType.BYTE), Byte.MIN_VALUE, (byte) 0, (byte) 3, Byte.MAX_VALUE, null));
	}
	
	public void testShort() throws SQLException, ParseException {
		doTest(col(spec(DBType.SHORT), Short.MIN_VALUE, (short) 7, Short.MAX_VALUE, null));
	}
	
	public void testInt() throws SQLException, ParseException {
		doTest(col(spec(DBType.INT), Integer.MIN_VALUE, 42, Integer.MAX_VALUE, null));
	}
	
	public void testLong() throws SQLException, ParseException {
		doTest(col(spec(DBType.LONG), Long.MIN_VALUE, ((long) Integer.MAX_VALUE) + 1, Long.MAX_VALUE, null));
	}
	
	public void testID() throws SQLException, ParseException {
		assertTrue("If no short ids the test must be adapted.", IdentifierUtil.SHORT_IDS);
		doTest(col(spec(DBType.ID), LongID.valueOf(((long) Integer.MAX_VALUE) + 1), LongID.valueOf(Long.MAX_VALUE),
			null));
	}

	public void testFloat() throws SQLException, ParseException {
		doTest(col(spec(DBType.FLOAT), -42.125f, 0f, 42.125f, null));
	}
	
	public void testDouble() throws SQLException, ParseException {
		doTest(col(spec(DBType.DOUBLE), -42.1234567890123d, 0d, 42.1234567890123d, null));
	}
	
	public void testDecimal() throws SQLException, ParseException {
		doTest(col(spec(DBType.DECIMAL, 10, 0, false, true, true), -12345d, -42d, 12345d, null));
		doTest(col(spec(DBType.DECIMAL, 10, 5, false, true, true), -12345.12345d, 12345.12345d, null));
	}
	
	public void testDate() throws SQLException, ParseException {
		testDateComputation().run();
		executeInTimeZone(getTimeZoneAuckland(), testDateComputation());
		executeInTimeZone(getTimeZoneLosAngeles(), testDateComputation());
	}

	private ComputationEx2<Void, SQLException, ParseException> testDateComputation() {
		return new ComputationEx2<>() {

			@Override
			public Void run() throws SQLException, ParseException {
				SimpleDateFormat format = CalendarUtil.newSimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				doTest(col(spec(DBType.DATE), DateUtil.createDate(2016, Calendar.JANUARY, 21),
					format.parse("2013-02-20 00:00:00"), null));
				doTest(col(spec(DBType.TIME), DateUtil.createDate(1970, Calendar.JANUARY, 1, 23, 55, 59),
					format.parse("1970-01-01 09:40:37"), null));
				doTest(col(spec(DBType.DATETIME), DateUtil.createDate(2016, Calendar.JANUARY, 21, 23, 55, 59),
					format.parse("2013-02-20 09:40:37"), null));
				try {
					doTest(col(spec(DBType.DATETIME),
						CalendarUtil.newSimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2013-02-20 09:40:37.999")));
				} catch (AssertionFailedError ex) {
					if (getSQLDialect() instanceof MSSQLHelper) {
						// Known bug in MSSQL 2005 and 2019.
					} else if (getSQLDialect() instanceof MySQL55Helper) {
						// Known bug in MySQL 5.5 which can not be solved.
					} else {
						throw ex;
					}
				}
				return null;
			}
		};
	}
	
	public void testSpecialDateFunctions() throws SQLException {
		String tableName = "TEST_DATE_FUNCTION";
		doCreateTable(col(spec(DBType.DATETIME)), tableName);
		DBHelper sqlDialect = getSQLDialect();
		StringBuffer insert = new StringBuffer("INSERT INTO " + sqlDialect.tableRef(tableName) + " VALUES ");
		insert.append("(");
		insert.append(sqlDialect.fnNow());
		insert.append(")");
		execute(insert.toString());
		doCreateTable(col(spec(DBType.TIME)), tableName);
		insert = new StringBuffer("INSERT INTO " + sqlDialect.tableRef(tableName) + " VALUES ");
		insert.append("(");
		insert.append(sqlDialect.fnCurrTime());
		insert.append(")");
		execute(insert.toString());
		doCreateTable(col(spec(DBType.DATE)), tableName);
		insert = new StringBuffer("INSERT INTO " + sqlDialect.tableRef(tableName) + " VALUES ");
		insert.append("(");
		insert.append(sqlDialect.fnCurrDate());
		insert.append(")");
		execute(insert.toString());
	}

	public void testString() throws SQLException, ParseException {
		doTest(col(spec(DBType.STRING, 100, 0, false, false, true), "Hello World öäüß€!", null));
		doTest(col(spec(DBType.STRING, 100, 0, false, true, true), "Hello world!", null));
		doTest(col(spec(DBType.STRING, 10000, 0, false, false, false),
			BasicTestCase.randomString(999, true, true, true, false), null));
	}
	
	public void testStringEscape() throws SQLException {
		doTest(col(spec(DBType.STRING, 100, 0, false, false, true), "Test String contain tab character \t.", null));
		doTest(col(spec(DBType.STRING, 100, 0, false, false, true), "Test String contain line feed character \n.", null));
		doTest(col(spec(DBType.STRING, 100, 0, false, false, true),
			"Test String contain carriage return character \r.", null));
		doTest(col(spec(DBType.STRING, 100, 0, false, false, true), "Test String contain single quote character '.",
			null));
	}

	public void testClob() throws SQLException {
		doTest(col(spec(DBType.CLOB, 10000, 0, false, false, false),
			BasicTestCase.randomString(999, true, true, true, false), null));
		doTest(col(spec(DBType.CLOB, 10000, 0, false, false, false),
			BasicTestCase.randomString(4000, true, true, false, false), null));
		doTest(col(spec(DBType.CLOB, 1024 * 1024 + 1, 0, false, false, false),
			BasicTestCase.randomString(1024 * 1024 + 1, true, true, false, false), null), false);
	}
	
	public void testBlob() throws SQLException {
		final byte[] byteData =
			BasicTestCase.randomString(999, true, true, false, false).getBytes(Charset.forName("ISO-8859-1"));

		BinaryData blobData = new TestingBinaryDataFromArray(byteData);
		BinaryData emptyData = EmptyBinaryData.INSTANCE;

		doTest(col(spec(DBType.BLOB, 10000, 0, false, true, false), blobData, emptyData, null));
	}

	public void testBlobUnknownSize() throws SQLException {
		final byte[] byteData =
			BasicTestCase.randomString(999, true, true, false, false).getBytes(Charset.forName("ISO-8859-1"));
		
		Object[] storeData = { new TestingBinaryDataFromArray(byteData) {
			@Override
			public long getSize() {
				return -1;
			}
		} };
		
		Object[] checkData = { new TestingBinaryDataFromArray(byteData) };

		doTest(col(spec(DBType.BLOB, 10000, 0, false, true, false), storeData, checkData));
	}

	void doTest(Col col) throws SQLException {
		doTest(col, false);
	}

	void doTest(Col col, boolean testLiteral) throws SQLException {
		String tableName = "TEST_LITERALS";
		doCreateTable(col, tableName);

		DBHelper sqlDialect = getSQLDialect();

		if (testLiteral) {
			for (Object value : col.storeValues()) {
				insertAsLiteral(tableName, sqlDialect, col.spec(), value);
			}
			checkValues(sqlDialect, tableName, col);

			clear(sqlDialect, tableName);
		}

		for (Object value : col.storeValues()) {
			insertAsParameterAndCommit(tableName, sqlDialect, col.spec(), value);
		}
		checkValues(sqlDialect, tableName, col);

		if (testLiteral) {
			String dump = dump(sqlDialect, tableName);
			clear(sqlDialect, tableName);
			executeScript(dump);

			checkValues(sqlDialect, tableName, col);
		}
	}

	private DBHelper doCreateTable(Col col, String tableName) throws SQLException {
		drop(tableName);
		DBHelper sqlDialect = getSQLDialect();
		StringBuilder create = new StringBuilder("CREATE TABLE " + sqlDialect.tableRef(tableName));
		create.append("(");
		{
			String columnName = "V";
			create.append(columnName);
			create.append(" ");
			Spec spec = col.spec();
			sqlDialect.appendDBType(create, spec.type(), columnName, spec.size(), spec.precision(),
				spec.mandatory(), spec.binary());
		}
		create.append(")");
		sqlDialect.appendTableOptions(create, false, 0);
		execute(create.toString());
		return sqlDialect;
	}

	private void insertAsLiteral(String tableName, DBHelper sqlDialect, Spec spec, Object value) throws SQLException {
		StringBuffer insert = new StringBuffer("INSERT INTO " + sqlDialect.tableRef(tableName) + " VALUES ");
		insert.append("(");
		{
			sqlDialect.literal(insert, spec.type(), value);
		}
		insert.append(")");
		execute(insert.toString());
	}

	private void insertAsParameterAndCommit(String tableName, DBHelper sqlDialect, Spec spec, Object value)
			throws SQLException {
		insertAsParameter(tableName, sqlDialect, spec, value);

		getConnection().commit();
	}

	private void insertAsParameter(String tableName, DBHelper sqlDialect, Spec spec, Object value) throws SQLException {
		StringBuffer insert = new StringBuffer("INSERT INTO " + sqlDialect.tableRef(tableName) + " VALUES (?)");
		PooledConnection connection = getConnection();
		PreparedStatement statement = connection.prepareStatement(insert.toString());
		try {
			sqlDialect.setFromJava(statement, value, 1, spec.type());
			int rows = statement.executeUpdate();
			assertEquals(1, rows);
		} finally {
			statement.close();
		}
	}

	private void clear(DBHelper sqlDialect, String tableName) throws SQLException {
		execute("DELETE FROM " + sqlDialect.tableRef(tableName));
	}

	private String dump(DBHelper sqlDialect, String tableName) throws SQLException {
		StringWriter insertBuffer = new StringWriter();
		PooledConnection connection = getConnection();
		Statement statement = connection.createStatement();
		sqlDialect.dumpAsInsert(new PrintWriter(insertBuffer), statement, tableName);
		String dump = insertBuffer.toString();
		return dump;
	}

	private static class Col {

		private final Spec _spec;

		private final Object[] _storeValues;

		private final Object[] _checkValues;

		public Col(Spec spec, Object[] values, Object[] checkValues) {
			_spec = spec;
			_storeValues = values;
			_checkValues = checkValues;
			if (checkValues.length != storeValues().length) {
				throw new IllegalArgumentException("Items to store and to check must have same length.");
			}
		}

		public Spec spec() {
			return _spec;
		}

		public Object[] storeValues() {
			return _storeValues;
		}

		public Object[] checkValues() {
			return _checkValues;
		}

	}

	Col col(Spec spec, Object... values) {
		return col(spec, values, values);
	}

	Col col(Spec spec, Object[] storeValues, Object[] checkValues) {
		return new Col(spec, storeValues, checkValues);
	}

	private void executeScript(String script) throws SQLException {
		PooledConnection connection = getConnection();
		Statement statement = connection.createStatement();
		try {
			for (String command : script.split("\\s*;\\s*\\n")) {
				executeOnly(statement, command);
			}
			connection.commit();
		} finally {
			statement.close();
		}
	}

	private void execute(String command) throws SQLException {
		PooledConnection connection = getConnection();
		Statement statement = connection.createStatement();
		try {
			executeOnly(statement, command);
			connection.commit();
		} finally {
			statement.close();
		}
	}

	private void executeOnly(Statement statement, String command) throws SQLException {
		DBHelper sqlDialect = getSQLDialect();
		PooledConnection connection = getConnection();
		Savepoint savePoint = sqlDialect.setSavepoint(connection);
		try {
			statement.execute(command);
		} catch (SQLException ex) {
			sqlDialect.rollback(connection, savePoint);
			throw enhance(ex, command);
		} finally {
			sqlDialect.releaseSavepoint(connection, savePoint);
		}
	}

	private void checkValues(DBHelper sqlDialect, String tableName, Col col) throws SQLException {
		Spec spec = col.spec();
		Object[] values = col.checkValues();

		if (spec.canSearch()) {
			for (Object value : values) {
				checkSingleValue(sqlDialect, tableName, spec, value);
			}
		}

		List<Object> expectedValues = spec.canSearch() ? nonNull(values) : list(values);
		{
			StringBuffer sql =
				new StringBuffer("SELECT (" + "V" + ") FROM " + sqlDialect.tableRef(tableName));
			if (spec.canSearch()) {
				sql.append(" WHERE ");
				sql.append("V");
				sql.append(" IN ");
				sqlDialect.literalSet(sql, spec.type(), expectedValues);
			}
			String select = sql.toString();
			assertEquals("In set statement for type '" + spec.type() + "' failed.", expectedValues.size(),
				count(select));

			try (Statement statement = getConnection().createStatement()) {
				try (ResultSet resultSet = statement.executeQuery(select)) {
					while (resultSet.next()) {
						{
							Object columnValue = sqlDialect.mapToJava(resultSet, 1, spec.type());
							assertTrue("Missing result value '" + columnValue + "'.",
								set(values).contains(columnValue));
						}
					}
				}
			}
		}
	}

	private void checkSingleValue(DBHelper sqlDialect, String tableName, Spec spec, Object value) throws SQLException {
		StringBuffer sql =
			new StringBuffer("SELECT (" + "V" + ") FROM " + sqlDialect.tableRef(tableName) + " WHERE ");
		{
			sql.append("V");
			if (value == null) {
				sql.append(" IS NULL");
			} else {
				sql.append(" = ");
				sqlDialect.literal(sql, spec.type(), value);
			}
		}
		String select = sql.toString();
		assertEquals("Select statement for type '" + spec.type() + "' and value '" + value + "' failed.", 1,
			count(select));

		Statement statement = getConnection().createStatement();
		try {
			ResultSet resultSet = statement.executeQuery(select);
			try {
				if (resultSet.next()) {
					{
						Object columnValue = sqlDialect.mapToJava(resultSet, 1, spec.type());
						assertEquals("Unexpected value.", value, columnValue);
					}
				}
			} finally {
				resultSet.close();
			}
		} finally {
			statement.close();
		}
	}

	private List<Object> nonNull(Object[] values) {
		ArrayList<Object> result = new ArrayList<>();
		for (Object x : values) {
			if (x == null) {
				continue;
			}
			result.add(x);
		}
		return result;
	}

	private Object count(String sql) throws SQLException {
		Statement statement = getConnection().createStatement();
		try {
			ResultSet resultSet = statement.executeQuery(sql);
			try {
				int count = 0;
				while (resultSet.next()) {
					count++;
				}
				return count;
			} finally {
				resultSet.close();
			}
		} catch (SQLException ex) {
			throw enhance(ex, sql);
		} finally {
			statement.close();
		}
	}

	public void testLimit() throws SQLException {
    	String tableName = "TestLimit";
    	
		// Cleanup table from last execution.
    	DBHelper sqlDialect = getSQLDialect();
		drop(tableName);
    	
    	// Try to create table with synthesized name.
    	StringBuilder createSql = new StringBuilder();
    	createSql.append(
    		"CREATE TABLE " + sqlDialect.tableRef(tableName) + " ("
    				+ sqlDialect.columnRef("l1") + " INT"
    				+ ")"
    			);
		sqlDialect.appendTableOptions(createSql, false, 0);
    	
    	PreparedStatement createStatement = getConnection().prepareStatement(createSql.toString());
    	try {
    		createStatement.execute();
    	} finally {
    		createStatement.close();
    	}
		getConnection().commit();

		String insert = SQLH.createInsert(sqlDialect, tableName, 1);
		PreparedStatement pstm = getConnection().prepareStatement(insert);
		for (int n = 0; n < 100; n++) {
			pstm.setInt(1, n);
			pstm.addBatch();
		}
		pstm.executeBatch();
		getConnection().commit();
    	
		boolean supportsLimitStop = sqlDialect.supportsLimitStop();
		if (supportsLimitStop) {
			assertEquals(list(0, 1, 2, 3, 4), fetchLimitedInts(tableName, 0, 5));
		}

		boolean supportsLimitStart = sqlDialect.supportsLimitStart();
		if (supportsLimitStart) {
			assertEquals(list(97, 98, 99), fetchLimitedInts(tableName, 97, -1));
		}

		if (supportsLimitStart && supportsLimitStop) {
			assertEquals(list(5, 6, 7), fetchLimitedInts(tableName, 5, 8));
		}
	}

	private void drop(String tableName) {
		String dropSql = "DROP TABLE " + getSQLDialect().tableRef(tableName);
		try {
			execute(dropSql);
		} catch (SQLException ex) {
			// Ignore: Table may not exist.
		}
	}

	private SQLException enhance(SQLException ex, String sql) {
		return new SQLException("Statement failed: " + sql, ex.getSQLState(), ex.getErrorCode(), ex);
	}

	private List<Integer> fetchLimitedInts(String tableName, int startRow, int stopRow) throws SQLException {
		return fetchInts(createSelectLimit(tableName, startRow, stopRow));
	}

	private List<Integer> fetchInts(String sql) throws SQLException {
		ArrayList<Integer> result = new ArrayList<>();
		PreparedStatement statement = getConnection().prepareStatement(sql);
		try {
			ResultSet resultSet = statement.executeQuery();
			try {
				while (resultSet.next()) {
					result.add(resultSet.getInt(1));
				}
			} finally {
				resultSet.close();
			}
		} finally {
			statement.close();
		}
		return result;
	}

	private String createSelectLimit(String tableName, int startRow, int stopRow) {
		DBHelper sqlDialect = getSQLDialect();

		StringBuilder buffer = new StringBuilder();
		sqlDialect.limitStart(buffer, startRow, stopRow);
		buffer.append("SELECT ");
		sqlDialect.limitColumns(buffer, startRow, stopRow);
		buffer.append("*");
		buffer.append(" FROM ");
		buffer.append(sqlDialect.tableRef(tableName));
		buffer.append(" ORDER BY ");
		buffer.append(sqlDialect.columnRef("l1"));
		sqlDialect.limitLast(buffer, startRow, stopRow);
		String sql = buffer.toString();
		return sql;
	}

	public void testTableNameLength() throws SQLException {
    	DBHelper sqlDialect = getSQLDialect();
    	
    	StringBuilder tableName = new StringBuilder();
    	for (int n = 0; n < sqlDialect.getMaxNameLength(); n++) {
    		tableName.append('A');
    	}
    	
    	// Cleanup table from last execution.
		drop(tableName.toString());
    	
		// Try to create table with synthesized name.
    	StringBuilder createSql = new StringBuilder();
		createSql.append(
			"CREATE TABLE " + sqlDialect.tableRef(tableName.toString()) + " ("
    		+ sqlDialect.columnRef("c1") + " VARCHAR(32)"
    		+ ")"
    	);
    	sqlDialect.appendTableOptions(createSql, false, 0);
    	
    	PreparedStatement createStatement = getConnection().prepareStatement(createSql.toString());
    	try {
			createStatement.execute();
		} finally {
			createStatement.close();
		}
    }
    
    /**
     * Anything not tested elsewhere is breeding here.
     */
	public void testMain() throws SQLException {
        DBHelper dbh = getSQLDialect();
        
        assertSame(dbh.getClass(), 
			DBHelper.getDBHelper(getConnection()).getClass());
        
        assertNotNull(dbh.fnNow());
        assertNotNull(dbh.fnCurrDate());
        assertNotNull(dbh.fnCurrTime());
        assertNotNull(dbh.notNullSpec());
        assertNotNull(dbh.nullSpec());
        dbh.supportNullInSetObject();   // Just for the coverage
    }
    
    /**
	 * tests using MetaData
	 */
	public void testMetaData() throws SQLException, IOException {
        DBHelper dbh = getSQLDialect();

        Connection con  = getConnection();
        String     prod = con.getMetaData().getDatabaseProductName();
		int illegalCharIndex = prod.indexOf('/');
		if (illegalCharIndex > -1) {
			// DB 2 product name is DB2/N64 which results in an illegal file name
			prod = prod.substring(0, illegalCharIndex);
		}

        Statement  stm  = getConnection().createStatement();
        try {
			ResultSet res = stm.executeQuery("SELECT * FROM " + table(TABLE_NAME));
        	try {
        		ResultSetMetaData meta  = res.getMetaData();
        		int               count = meta.getColumnCount();
        		for (int i=1; i <= count; i++) {
        			assertNotNull(dbh.getColumnTypeName(meta, i));
        		}
        		
        		while (res.next()) {
        			for (int i=1; i <= count; i++) {
        				/* Object mapped = */ 
						dbh.mapToJava(res, i, DBType.fromSqlType(dbh, meta.getColumnType(i), meta.getScale(i)));
        			}
        		}
			} finally {
				res.close();
			}
        	
        	FileWriter  dump  = new FileWriter(BasicTestCase.createNamedTestFile("addValue-" + prod + ".sql"));
        	try {
				PrintWriter out = new PrintWriter(dump);
        		try {
					dbh.dumpAsInsert(out, stm, TABLE_NAME);
				} finally {
					out.close();
				}
			} finally {
				dump.close();
			}
		} finally {
			stm.close();
		}
                
    }

    /** tests using types are found here */
	public void testTypes() {
        DBHelper dbh = getSQLDialect();

		for (DBType sqlType : DBType.values()) {
            dbh.noSize(sqlType);
			getDBType(sqlType, 10, 0, false);
        }
    }

	private String getDBType(DBType sqlType, int size, int precision, boolean binary) {
		StringBuilder result = new StringBuilder();
		getSQLDialect().appendDBType(result, sqlType, "foo", size, precision, false, binary);
		return result.toString();
	}

	/** Test the isSystemtable() method */
	public void testIsSytemtable() {
        DBHelper dbh = getSQLDialect();

		assertFalse(dbh.isSystemTable(TABLE_NAME));
        if (dbh instanceof MSSQLHelper) {
            assertTrue(dbh.isSystemTable("dtproperties"));
        }
    }

    /** Test Mapping of arbitray Java type to db-specific ones */
    private void setValues()throws SQLException {
        DBHelper dbh = getSQLDialect();
        
        Connection        con    = getConnection();
		String insert = SQLH.createInsert(dbh, TABLE_NAME, 13);
		try (PreparedStatement pstm = con.prepareStatement(insert)) {
			java.util.Date now = MON_MAR_20_14_05_40_CET_2006;
			dbh.setFromJava(pstm, Integer.valueOf(22), 1, DBType.INT);
			dbh.setFromJava(pstm, Integer.valueOf(-4), 2, DBType.INT);
			dbh.setFromJava(pstm, true, 3, DBType.BOOLEAN);
			dbh.setFromJava(pstm, true, 4, DBType.BOOLEAN);
			dbh.setFromJava(pstm, 'z', 5, DBType.CHAR);
			dbh.setFromJava(pstm, "VarraCarra", 6, DBType.STRING);
			dbh.setFromJava(pstm, Float.valueOf(39.8f), 7, DBType.FLOAT);
			dbh.setFromJava(pstm, Double.valueOf(4.77), 8, DBType.FLOAT);
			dbh.setFromJava(pstm, now, 9, DBType.DATE);
			dbh.setFromJava(pstm, now, 10, DBType.TIME);
			dbh.setFromJava(pstm, now, 11, DBType.DATETIME);
			dbh.setFromJava(pstm, Long.valueOf(123), 12, DBType.LONG);
			dbh.setFromJava(pstm, Long.valueOf(456), 13, DBType.LONG);
			assertEquals(1, pstm.executeUpdate());
			con.commit();
		}
        
    }

    /** Test Mapping of arbitray Java type to db-specific ones, with variations */
    private void setValues2()throws SQLException {
        DBHelper dbh = getSQLDialect();
        
        Connection         con    = getConnection();
		String insert = SQLH.createInsert(dbh, TABLE_NAME, 13);
		try (PreparedStatement pstm = con.prepareStatement(insert)) {
			java.sql.Date now1 = new java.sql.Date(MON_MAR_20_14_05_40_CET_2006.getTime());
			java.sql.Time now2 = new java.sql.Time(MON_MAR_20_14_05_40_CET_2006.getTime());
			java.sql.Timestamp now3 = new java.sql.Timestamp(MON_MAR_20_14_05_40_CET_2006.getTime());

			dbh.setFromJava(pstm, Integer.valueOf(23), 1, DBType.INT);
			dbh.setFromJava(pstm, Integer.valueOf(-4), 2, DBType.INT);
			dbh.setFromJava(pstm, true, 3, DBType.BOOLEAN);
			dbh.setFromJava(pstm, true, 4, DBType.BOOLEAN);
			dbh.setFromJava(pstm, 'z', 5, DBType.CHAR);
			dbh.setFromJava(pstm, "VarraCarra", 6, DBType.STRING);
			dbh.setFromJava(pstm, Float.valueOf(39.8f), 7, DBType.FLOAT);
			dbh.setFromJava(pstm, Double.valueOf(4.77), 8, DBType.FLOAT);
			dbh.setFromJava(pstm, now1, 9, DBType.DATE);
			dbh.setFromJava(pstm, now2, 10, DBType.TIME);
			dbh.setFromJava(pstm, now3, 11, DBType.DATETIME);
			dbh.setFromJava(pstm, Long.valueOf(123), 12, DBType.LONG);
			dbh.setFromJava(pstm, Long.valueOf(456), 13, DBType.LONG);
			assertEquals(1, pstm.executeUpdate());

			con.commit();
		}
        
    }
    
    /** Test Mapping of arbitray Java type to db-specific ones, invalid usage */
	public void testSetFromJavaBroken() throws SQLException {
        DBHelper dbh = getSQLDialect();
        
        Connection         con    = getConnection();
		String insert = SQLH.createInsert(dbh, TABLE_NAME, 13);
		try (PreparedStatement pstm = con.prepareStatement(insert)) {
			java.util.Date now = new java.util.Date();
			Long now1 = now.getTime();
			Long now2 = now.getTime();
			Long now3 = now.getTime();

			dbh.setFromJava(pstm, Integer.valueOf(24), 1, DBType.INT);
			dbh.setFromJava(pstm, Integer.valueOf(-4), 2, DBType.INT);
			dbh.setFromJava(pstm, true, 3, DBType.BOOLEAN);
			dbh.setFromJava(pstm, true, 4, DBType.BOOLEAN);
			dbh.setFromJava(pstm, 'z', 5, DBType.CHAR);
			dbh.setFromJava(pstm, "VarraCarra", 6, DBType.STRING);
			dbh.setFromJava(pstm, Float.valueOf(39.8f), 7, DBType.FLOAT);
			dbh.setFromJava(pstm, Double.valueOf(4.77), 8, DBType.FLOAT);
			try {
				dbh.setFromJava(pstm, now1, 9, DBType.DATE);
				fail("Expected SQLException");
			} catch (SQLException expected) { /* expected */
			}
			// mut not be null ...
			dbh.setFromJava(pstm, now, 9, DBType.DATE);
			try {
				dbh.setFromJava(pstm, now2, 10, DBType.TIME);
				fail("Expected SQLException");
			} catch (SQLException expected) { /* expected */
			}
			// mut not be null ...
			dbh.setFromJava(pstm, now, 10, DBType.TIME);
			try {
				dbh.setFromJava(pstm, now3, 11, DBType.DATETIME);
				fail("Expected SQLException");
			} catch (SQLException expected) { /* expected */
			}
			dbh.setFromJava(pstm, now, 11, DBType.DATETIME);
			dbh.setFromJava(pstm, Long.valueOf(123), 12, DBType.LONG);
			dbh.setFromJava(pstm, Long.valueOf(456), 13, DBType.LONG);
			assertEquals(1, pstm.executeUpdate());

			con.commit();
		}
        
        
    }

	public void testCommitAfterSQLException() throws SQLException {
		PooledConnection connection = getConnection();
		Statement statement = connection.createStatement();

		Spec spec = spec(DBType.INT, 0, 0, true, true, false);
		Col col = col(spec);
		String tableName = "TEST_COMMIT_AFTER_SQL";

		doCreateTable(col, tableName);
		connection.commit();

		insertAsParameter(tableName, getSQLDialect(), spec, 1);

		try {
			insertAsParameter(tableName, getSQLDialect(), spec, null);

			fail("Null value can not be inserted in a not null column");
		} catch (SQLException ex) {
			// Expected.

			ex.printStackTrace();
		}

		connection.commit();
		ResultSet result = statement.executeQuery("SELECT * FROM " + table("TEST_COMMIT_AFTER_SQL"));
		boolean next = result.next();

		assertTrue(next);
		int value = result.getInt(1);
		assertEquals(1, value);
	}

    public void testDoSelect()throws SQLException {
    	setValues();
    	setValues2();
        
        Connection        con  = getConnection();
        {
			PreparedStatement pstm =
				con.prepareStatement("SELECT * FROM " + table(TABLE_NAME) + " WHERE " + columnRef("t1") + "=?");
        	try {
				java.sql.Time time = new java.sql.Time(MON_MAR_20_14_05_40_CET_2006.getTime());
				getSQLDialect().setTime(pstm, 1, time);
        		ResultSet res = pstm.executeQuery();
        		try {
        			assertTrue(res.next());
        			assertTrue(res.next());
        			assertFalse(res.next());
        		} finally {
        			res.close();
        		}
        	} finally {
        		pstm.close();
        	}
        }
        
		{
			PreparedStatement pstm =
				con.prepareStatement("SELECT * FROM " + table(TABLE_NAME) + " WHERE " + columnRef("dt") + "=?");
        	try {
				Timestamp dt = new Timestamp(MON_MAR_20_14_05_40_CET_2006.getTime());
				getSQLDialect().setTimestamp(pstm, 1, dt);
        		ResultSet res = pstm.executeQuery();
        		try {
        			assertTrue (res.next());
        			assertTrue (res.next());
        			assertFalse(res.next());
				} finally {
					res.close();
				}
        	} finally {
        		pstm.close();
        	}
        	
        }
		{
			PreparedStatement pstm =
				con.prepareStatement("SELECT * FROM " + table(TABLE_NAME) + " WHERE " + columnRef("dt") + "<=?");
			try {
				Timestamp dt = new Timestamp(MON_MAR_20_14_05_40_CET_2006.getTime());
				getSQLDialect().setTimestamp(pstm, 1, dt);
				ResultSet res = pstm.executeQuery();
				try {
					assertTrue (res.next());
					// assertFalse(res.next());
				} finally {
					res.close();
				}
			} finally {
				pstm.close();
			}
		}
		{
			PreparedStatement pstm =
				con.prepareStatement("SELECT * FROM " + table(TABLE_NAME) + " WHERE " + columnRef("dt") + ">=?");
			try {
				Timestamp dt = new Timestamp(MON_MAR_20_14_05_40_CET_2006.getTime());
				getSQLDialect().setTimestamp(pstm, 1, dt);
				ResultSet res = pstm.executeQuery();
				try {
					assertTrue (res.next());
					// assertFalse(res.next());
				} finally {
					res.close();
				}
			} finally {
				pstm.close();
			}
		}
		{        
			PreparedStatement pstm =
				con.prepareStatement("SELECT * FROM " + table(TABLE_NAME) + " WHERE " + columnRef("d1") + "=?");
	        try {
				java.sql.Date date = new java.sql.Date(1142820000000L); // 2006-03-20
				getSQLDialect().setDate(pstm, 1, date);
	            ResultSet res = pstm.executeQuery();
	            try {
	            	assertTrue (res.next());
	            	// assertFalse(res.next());
				} finally {
					res.close();
				}
	        } finally {
	            pstm.close();
	        }
		}
    }
    
    /** Test Analyze/optimze for data inserted by testSetFromJava() */
	public void testDoAnalyzeOptimize() throws SQLException {
        DBHelper          dbh = getSQLDialect();
        Connection        con = getConnection();
		Statement stm = con.createStatement();
        try {
			dbh.analyzeTable(stm, TABLE_NAME);
			dbh.optimizeTable(stm, TABLE_NAME);
        } finally {
			stm.close();
        }
    }


    /** Test Mapping of db-types back to Java types */
	public void testMapToJava() throws SQLException {

        DBHelper          dbh = getSQLDialect();
        Connection        con = getConnection();
		Statement stm = con.createStatement();
        try {
			ResultSet res = stm.executeQuery("SELECT * FROM " + table(TABLE_NAME) + " ORDER BY " + columnRef("i1"));
			try {
				assertTrue(res.next());

				int i = 1;
				assertEquals(Integer.valueOf(-1), dbh.mapToJava(res, i++, DBType.INT));
				assertEquals(Integer.valueOf(65565), dbh.mapToJava(res, i++, DBType.INT));
				assertEquals(true, dbh.mapToJava(res, i++, DBType.BOOLEAN));
				assertEquals(true, dbh.mapToJava(res, i++, DBType.BOOLEAN));

				assertInstanceof(dbh.mapToJava(res, i++, DBType.CHAR), Character.class);
				assertInstanceof(dbh.mapToJava(res, i++, DBType.STRING), String.class);

				assertInstanceof(dbh.mapToJava(res, i++, DBType.FLOAT), Float.class);
				assertInstanceof(dbh.mapToJava(res, i++, DBType.DOUBLE), Double.class);

				assertTrue(res.next()); // NO Dates in first row

				i = 5;

				assertNull(dbh.mapToJava(res, i++, DBType.CHAR));
				assertInstanceof(dbh.mapToJava(res, i++, DBType.STRING), String.class);

				assertInstanceof(dbh.mapToJava(res, i++, DBType.FLOAT), Float.class);
				assertInstanceof(dbh.mapToJava(res, i++, DBType.DOUBLE), Double.class);

				// returning the sql.Date objects reuslts in much confusion
				// and result in trouble when mixing them (The are not comparable ...)
				// after much debate whe now use only java.util.Date
				assertTrue(dbh.mapToJava(res, i++, DBType.DATE).getClass() == java.util.Date.class);
				assertTrue(dbh.mapToJava(res, i++, DBType.TIME).getClass() == java.util.Date.class);
				assertTrue(dbh.mapToJava(res, i++, DBType.DATETIME).getClass() == java.util.Date.class);
			} finally {
				res.close();
			}
        } finally {
			stm.close();
		}
    }

	public void testMapToJavaByte() throws SQLException {
		assertInstanceof(loadAs("15.3", DBType.BYTE), Byte.class);
	}

	public void testMapToJavaShort() throws SQLException {
		assertInstanceof(loadAs("15.3", DBType.SHORT), Short.class);
	}

	public void testMapToJavaInteger() throws SQLException {
		assertInstanceof(loadAs("15.3", DBType.INT), Integer.class);
	}

	public void testMapToJavaLong() throws SQLException {
		assertInstanceof(loadAs("15.3", DBType.LONG), Long.class);
	}

	public void testMapToJavaID() throws SQLException {
		assertInstanceof(loadAs("15", DBType.ID), TLID.class);
	}

	public void testMapToJavaFloat() throws SQLException {
		assertInstanceof(loadAs("15", DBType.FLOAT), Float.class);
	}

	public void testMapToJavaDouble() throws SQLException {
		assertInstanceof(loadAs("15", DBType.DOUBLE), Double.class);
	}

	public void testMapToJavaString() throws SQLException {
		assertInstanceof(loadAs("15.3", DBType.STRING), String.class);
	}

	public void testMapToJavaBoolean() throws SQLException {
		assertEquals(loadAs("1", DBType.BOOLEAN), true);
		assertEquals(loadAs("0", DBType.BOOLEAN), false);
		assertEquals(loadAs("1", DBType.BOOLEAN), true);
		assertEquals(loadAs("0", DBType.BOOLEAN), false);
	}

	private Object loadAs(String sqlLiteral, DBType type) throws SQLException {
		DBHelper dbh = getSQLDialect();
		Connection con = getConnection();
		Statement stm = con.createStatement();
		Object value;
		try {
			ResultSet res = stm.executeQuery("SELECT " + sqlLiteral + getSQLDialect().fromNoTable());
			try {
				assertTrue(res.next());
				value = dbh.mapToJava(res, 1, type);
			} finally {
				res.close();
			}
		} finally {
			stm.close();
		}
		return value;
	}

    /** Test Mapping of non standard db-types back to Java types */
	public void testMapToJava2() throws SQLException {

        DBHelper          dbh = getSQLDialect();
        Connection        con = getConnection();
		Statement stm = con.createStatement();
        try {
			ResultSet res = stm.executeQuery("SELECT * FROM " + table(TABLE_NAME) + " ORDER BY " + columnRef("i1"));
			try {
				assertTrue(res.next());

				int i = 1;
				assertEquals(Integer.valueOf(-1), dbh.mapToJava(res, i++, DBType.INT));
				assertEquals(Integer.valueOf(65565), dbh.mapToJava(res, i++, DBType.INT));
				// The ideas how to map Bit to boolean when actually using number vary ...
				assertInstanceof(dbh.mapToJava(res, i++, DBType.BOOLEAN), Boolean.class);
				assertInstanceof(dbh.mapToJava(res, i++, DBType.BOOLEAN), Boolean.class);

				assertInstanceof(dbh.mapToJava(res, i++, DBType.CHAR), Character.class);
				assertInstanceof(dbh.mapToJava(res, i++, DBType.STRING), String.class);

				assertInstanceof(dbh.mapToJava(res, i++, DBType.FLOAT), Float.class);
				assertInstanceof(dbh.mapToJava(res, i++, DBType.DOUBLE), Double.class);

				assertTrue(res.next()); // NO Dates in first row

				i = 3;

				assertEquals(Boolean.FALSE, dbh.mapToJava(res, i++, DBType.BOOLEAN));
				assertNull(dbh.mapToJava(res, i++, DBType.BOOLEAN));

				assertNull(dbh.mapToJava(res, i++, DBType.CHAR));
				assertInstanceof(dbh.mapToJava(res, i++, DBType.STRING), String.class);

				assertInstanceof(dbh.mapToJava(res, i++, DBType.FLOAT), Float.class);
				assertInstanceof(dbh.mapToJava(res, i++, DBType.DOUBLE), Double.class);

				// returning the sql.Date objects reuslts in much confusion
				// and result in trouble when mixing them (The are not comparable ...)
				// after much debate whe now use only java.util.Date
				assertTrue(dbh.mapToJava(res, i++, DBType.DATE).getClass() == java.util.Date.class);
				assertTrue(dbh.mapToJava(res, i++, DBType.TIME).getClass() == java.util.Date.class);
				assertTrue(dbh.mapToJava(res, i++, DBType.DATETIME).getClass() == java.util.Date.class);
			} finally {
				res.close();
			}
        } finally {
			stm.close();
        }
            
    }

	private String table(String tableName) {
		return getSQLDialect().tableRef(tableName);
	}

    /** 
     * Testcase for {@link DBHelper#prepareSerial(String, Connection)} and 
     *               {@link DBHelper#postcareSerial(long, Statement)}.
     */
	public void testSerial() throws SQLException {
        DBHelper          dbh = getSQLDialect();
        Connection        con = getConnection();

		String nameColumnRef = dbh.columnRef("NAME");
		String identColumnRef = dbh.columnRef("IDENT");
		String autoTestTableRef = dbh.tableRef("AUTO_TEST");

		long id1;
		{
			Statement stm = con.createStatement();
			try {
				id1 = dbh.prepareSerial("AUTO_TEST", con);
				try {
					if (dbh.isSerialNeeded()) {
						stm.executeUpdate(
							"INSERT INTO " + autoTestTableRef + " (" + identColumnRef + ", " + nameColumnRef
								+ ") VALUES(" + id1 + ",'Heinz')");
					} else {
						stm.executeUpdate("INSERT INTO " + autoTestTableRef + " (" + nameColumnRef
							+ ") VALUES('Heinz')",
							Statement.RETURN_GENERATED_KEYS);
					}
					id1 = dbh.postcareSerial(id1, stm);
				} catch (UnsupportedOperationException happensWithMSAccess) {
					return; // Wont dig any further here
				}
				// assertTrue(id1 != 0);
			} finally {
				stm.close();
			}
		}

		long id2;
		{
			PreparedStatement pstm;
			if (dbh.isSerialNeeded()) {
				pstm = con.prepareStatement(
					"INSERT INTO " + autoTestTableRef + " (" + identColumnRef + ", " + nameColumnRef + ") VALUES(?,?)");
			} else {
				pstm = con.prepareStatement("INSERT INTO " + autoTestTableRef + " (" + nameColumnRef + ") VALUES(?)",
					Statement.RETURN_GENERATED_KEYS);
			}
			try {
				id2 = dbh.prepareSerial(autoTestTableRef, con);
				int i = 1;
				if (dbh.isSerialNeeded()) {
					pstm.setLong(i++, id2);
				}
				pstm.setString(i++, "Willy");
				pstm.executeUpdate();
				id2 = dbh.postcareSerial(id2, pstm);
			} finally {
				pstm.close();
			}
		}
		assertTrue("Ids must differ " + id1 + "," + id2, id2 > id1);

		{
			Statement stm = con.createStatement();
			try {
				ResultSet res =
					stm.executeQuery(
						"SELECT " + identColumnRef + " FROM " + autoTestTableRef + " ORDER BY " + identColumnRef);
				try {
					assertTrue(res.next());
					assertEquals(id1, res.getLong(1));
					assertTrue(res.next());
					assertEquals(id2, res.getLong(1));
					assertFalse(res.next());
				} finally {
					res.close();
				}
				con.commit();
			} finally {
				stm.close();
			}
        }
    }
    
	public void testDropIndex() {
        DBHelper dbh = getSQLDialect();
		String dropSQL = dbh.dropIndex("perstest_idx", TABLE_NAME);
        // Not much to assert here
		assertTrue(dropSQL.indexOf(TABLE_NAME) >= 0);
    }

	public void testSupportsBatchInfo() throws SQLException {
		String tableTestBatchInfo = "TestBatchInfo";
		String c1 = "c1";
		String c2 = "c2";
		String c3 = "c3";

		drop(tableTestBatchInfo);

		String createSql =
			"CREATE TABLE " + tableRef(tableTestBatchInfo) + " ("
				+ columnDef(c1, DBType.STRING, 32, true) + ", "
				+ columnDef(c2, DBType.STRING, 32, true) + ", "
				+ columnDef(c3, DBType.LONG, 20, true)
				+ ")"
				+ tableOptions(false, 0);
		execute(createSql);

		PreparedStatement insert = getConnection().prepareStatement(
			"INSERT INTO " + tableRef(tableTestBatchInfo) +
				" VALUES (?, ?, ?)");
		try {
			insert.setString(1, "foo");
			insert.setString(2, "bar");
			insert.setInt(3, 10);
			insert.addBatch();

			insert.setString(1, "foo");
			insert.setString(2, "baz");
			insert.setInt(3, 11);
			insert.addBatch();

			insert.setString(1, "aaa");
			insert.setString(2, "bbb");
			insert.setInt(3, 12);
			insert.addBatch();

			int[] batchInfo = insert.executeBatch();
			assertEquals(3, batchInfo.length);
			assertEquals(
				getSQLDialect().supportsBatchInfo() ? Statement.RETURN_GENERATED_KEYS : Statement.SUCCESS_NO_INFO,
				batchInfo[0]);
			assertEquals(
				getSQLDialect().supportsBatchInfo() ? Statement.RETURN_GENERATED_KEYS : Statement.SUCCESS_NO_INFO,
				batchInfo[1]);
			assertEquals(
				getSQLDialect().supportsBatchInfo() ? Statement.RETURN_GENERATED_KEYS : Statement.SUCCESS_NO_INFO,
				batchInfo[2]);
		} finally {
			insert.close();
		}

		PreparedStatement update = getConnection().prepareStatement(
			"UPDATE " + tableRef(tableTestBatchInfo) +
				" SET " + columnRef(c3) + "=" + columnRef(c3) + "+1" +
				" WHERE " + columnRef(c1) + "=?");
		try {
			update.setString(1, "foo");
			update.addBatch();
			
			update.setString(1, "aaa");
			update.addBatch();
			
			update.setString(1, "zzz");
			update.addBatch();
			
			int[] batchInfo = update.executeBatch();
			assertEquals(3, batchInfo.length);
			assertEquals(getSQLDialect().supportsBatchInfo() ? 2 : Statement.SUCCESS_NO_INFO, batchInfo[0]);
			assertEquals(getSQLDialect().supportsBatchInfo() ? 1 : Statement.SUCCESS_NO_INFO, batchInfo[1]);
			assertEquals(getSQLDialect().supportsBatchInfo() ? 0 : Statement.SUCCESS_NO_INFO, batchInfo[2]);
		} finally {
			update.close();
		}
	}

	private String tableRef(String name) {
		return getSQLDialect().tableRef(name);
	}

	private String tableOptions(boolean usePKeyStorage, int compress) {
		StringBuilder sql = new StringBuilder();
		getSQLDialect().appendTableOptions(sql, usePKeyStorage, compress);
		return sql.toString();
	}

	private String columnDef(String name, DBType type, int size, boolean binary) {
		StringBuilder sql = new StringBuilder();
		sql.append(columnRef(name));
		sql.append(" ");
		getSQLDialect().appendDBType(sql, type, columnRef(name), size, 0, false, binary);
		return sql.toString();
	}

	private String columnRef(String name) {
		return getSQLDialect().columnRef(name);
	}

    // Not yet tested (to complex for now ... )
    // public void testPrepareSerial() {}
    // public void testPostcareSerial() {}
    // public void testFireSQLStatementsReaderStatement() {}
    // public void testFireSQLStatementsReaderConnection() {}
    // public void testGetBLOBOutputStream() {}
    // public void testSupportNullInSetObject() {}

    /** 
     * Return the suite of tests to execute.
     */
	@SuppressWarnings("unused")
	public static Test suite() {
    	TestFactory fac;
    	if (true) {
			fac = DefaultTestFactory.INSTANCE;
    	} else {
    		fac = new TestFactory() {
    			
    			@Override
    			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
    				return TestSuite.createTest(testCase, "testSpecialDateFunctions");
    			}
    		};
    	}
		fac = ServiceTestSetup.createStarterFactory(Settings.Module.INSTANCE, fac);
		return suite(TestDBHelper.class, fac);
    }
}
