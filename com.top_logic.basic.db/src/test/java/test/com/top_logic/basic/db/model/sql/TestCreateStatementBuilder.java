/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.model.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.SimpleTestFactory;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.LongID;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.model.util.SchemaExtraction;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLBoolean;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLInsert;
import com.top_logic.basic.db.sql.SQLLiteral;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.db.sql.SQLTable;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.InMemoryBinaryData;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;

/**
 * Test case for {@link DBSchemaUtils} and implicitly {@link SchemaExtraction}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestCreateStatementBuilder extends BasicTestCase {
	
	private static Object[] NO_VALUES = new Object[0];

	static void addTests(TestSuite suite, boolean mandatory, boolean binary) {
		for (DBType type : DBType.values()) {
			if (binary && (! type.binaryParam)) {
				continue;
			}
			Object[] values = null;
			Object[] additionalNullValues = NO_VALUES;
			switch (type) {
				case BOOLEAN:
					values = new Object[] { Boolean.TRUE, Boolean.FALSE };
					break;
				case BYTE:
					values =
						new Object[] { Byte.MAX_VALUE, Byte.MIN_VALUE, (byte) 0, (byte) -1, (byte) 77,
							Byte.valueOf((byte) 666) };
					break;
				case CHAR:
					values = new Object[] { 'z' };
					break;
				case DATE:
					// in Type date it is not required that the hour or minutes are stored, so
					// adjust to day begin.
					Date date = DateUtil.adjustToDayBegin(new Date(1350920637547L));
					values = new Object[] { date };
					break;
				case DATETIME:
					// MySQL 5.1 do not handle milli seconds
					// values = new Object[] { new Date(1350920637547L) };
					values = new Object[] { new Date(1350920637000L) };
					break;
				case DOUBLE:
					values = new Object[] { Double.valueOf(1.23456789d) };
					break;
				case FLOAT:
					values = new Object[] { Float.valueOf(1.23f) };
					break;
				case INT:
					values = new Object[] { Integer.valueOf(666) };
					break;
				case LONG:
					values = new Object[] { Long.valueOf(666) };
					break;
				case ID:
					TLID id;
					if (IdentifierUtil.SHORT_IDS) {
						id = LongID.valueOf(667);
					} else {
						id = StringID.createRandomID();
					}
					values = new Object[] { id };
					break;
				case SHORT:
					values = new Object[] { Short.valueOf((short) 666) };
					break;
				case STRING:
					values = new Object[] { "Hello" };
					additionalNullValues = new Object[] { "" };
					break;
				case DECIMAL:
				case TIME:
					values = NO_VALUES;
					break;
				case BLOB:
					InMemoryBinaryData largeBlobContent = newBlobData(50000);
					InMemoryBinaryData smallBlobContent = newBlobData(2000);
					values = new Object[] { smallBlobContent, largeBlobContent };
					additionalNullValues = new Object[] { newBlobData(0) };
					break;
				case CLOB:
					if (binary) {
						values = new Object[] { BasicTestCase.randomString(50000, true, false, false, false) };
					} else {
						values = new Object[] { BasicTestCase.randomString(50000, true, true, true, false) };
					}
					additionalNullValues = new Object[] { "" };
					break;
				default: {
					String testName = TestType.mkName(type, binary, mandatory);
					suite.addTest(SimpleTestFactory.newBrokenTest(false, "No test implemented for " + type, testName));
					continue;

				}
			}
			assert values != null;
			TestType test = new TestType(type, binary, mandatory, values);
			test.setNullValues(additionalNullValues);
			suite.addTest(test);
		}
	}

	private static InMemoryBinaryData newBlobData(int size) {
		Random random = new Random(1234);
		InMemoryBinaryData blobContent = new InMemoryBinaryData(BinaryData.CONTENT_TYPE_OCTET_STREAM);
		for (int i = 0; i < size; i++) {
			blobContent.write(random.nextInt());
		}
		return blobContent;
	}
	
	private static class TestType extends BasicTestCase {
		
		private DBType type;
		private boolean binary;
		private boolean mandatory;
		private Object[] values;
		private Object[] nullValues;
		
		/**
		 * @param values
		 *        values to store and read from database
		 */
		public TestType(DBType type, boolean binary, boolean mandatory, Object... values) {
			super("test" + mkName(type, binary, mandatory));
			
			this.type = type;
			this.binary = binary;
			this.mandatory = mandatory;
			this.values = values;
		}

		public void setNullValues(Object... nullValues) {
			this.nullValues = nullValues;
		}

		@Override
		protected void runTest() throws Throwable {
			ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
			DBSchema schema = DBSchemaFactory.createDBSchema();
			DBTable table = DBSchemaFactory.createTable("TestTypes");
			DBColumn column = addColumn(table, type, binary, mandatory);
			schema.getTables().add(table);
			
			// Safety: Clean up before.
			DBSchemaUtils.recreateTables(pool, schema);
			
			/* checks that the column of the extracted table has a compatible type. This occurs
			 * static without regarding the SQL dialect. This currently does not work, because e.g.
			 * Boolean values may be stored as CHAR so the extracted column has type CHAR which is
			 * incompatible to BOOLEAN. But this doesn't matter because SQL dialect translates the
			 * read char into a boolean. */
//			DBSchema extractedSchema = DBSchemaUtils.extractTable(pool, table.getDBName());
//			
//			BufferingProtocol log = new BufferingProtocol();
//			DBSchemaUtils.checkCompatible(log, schema, extractedSchema, true);
//			assertFalse(log.getError(), log.hasErrors());
			
			// Try to insert a null row.
			{
				DBHelper sqlDialect = pool.getSQLDialect();
				PooledConnection connection = pool.borrowWriteConnection();
				try {
					if (!mandatory) {
						insertNull(connection, sqlDialect, table, column);
						checkValue(connection, sqlDialect, table, column, null);
						truncate(connection, sqlDialect, table);
						for (Object value : nullValues) {
							insertValue(connection, sqlDialect, table, column, value);
							checkValue(connection, sqlDialect, table, column, value, true);
							truncate(connection, sqlDialect, table);
						}
					}
					for (Object value : values) {
						insertValue(connection, sqlDialect, table, column, value);
						checkValue(connection, sqlDialect, table, column, value);
						truncate(connection, sqlDialect, table);
					}
				} finally {
					pool.releaseWriteConnection(connection);
				}
			}
			
			// TODO: Test binary attribute be searching a string value with different case.

			// Clean up table.
			DBSchemaUtils.resetTables(pool, schema, false);
		}

		private void truncate(PooledConnection connection, DBHelper sqlDialect, DBTable table) throws SQLException {
			/* connection must be rolled back as the truncate statement must be the first in a
			 * transaction in DB2. This includes real changes, but also simple select statements. */
			connection.rollback();

			try (Statement stmt = connection.createStatement()) {
				String truncateTableStatement = sqlDialect.getTruncateTableStatement(table.getDBName());
				stmt.execute(truncateTableStatement);
				connection.commit();
			}
		}

		private void insertValue(PooledConnection connection, DBHelper sqlDialect, DBTable table, DBColumn column,
				Object value) throws SQLException {
			assertNotNull("try insertNull for null check", value);
			SQLLiteral columnValue = literal(column.getType(), value);
			CompiledStatement insertSQL = createInsertStatement(sqlDialect, table, column, columnValue);

			insertSQL.executeUpdate(connection);
			connection.commit();
		}

		private void checkValue(PooledConnection connection, DBHelper sqlDialect, DBTable table, DBColumn column,
				Object value) throws SQLException {
			checkValue(connection, sqlDialect, table, column, value, false);
		}

		private void checkValue(PooledConnection connection, DBHelper sqlDialect, DBTable table, DBColumn column,
				Object value, boolean nullValue) throws SQLException {
			List<SQLColumnDefinition> columnDefs =
				Collections.singletonList(columnDef(column(table.getDBName(), column.getDBName(), column.isMandatory()), null));
			SQLSelect select = select(false, columnDefs, tableReference(table), SQLBoolean.TRUE, NO_ORDERS);
			CompiledStatement selectSQL = query(select).toSql(sqlDialect);
			try (ResultSet result = selectSQL.executeQuery(connection)) {
				assertTrue(result.next());
				Object fetchedObject = sqlDialect.mapToJava(result, 1, column.getType());
				if (fetchedObject == null && value != null) {
					if (!nullValue) {
						fail("Null value for column " + column + ": Expected: " + value);
					}
				} else {
					assertEquals("Different value for column " + column, value, fetchedObject);
				}

				assertFalse("More than one result. It may be luck that test is not broken.", result.next());
			}
		}

		private SQLTable tableReference(DBTable table) {
			return table(table.getDBName(), NO_TABLE_ALIAS);
		}

		private void insertNull(PooledConnection connection, DBHelper sqlDialect, DBTable table, DBColumn column) {
			SQLLiteral columnValue = literalNull(column.getType());
			CompiledStatement sql = createInsertStatement(sqlDialect, table, column, columnValue);

			try {
				sql.executeUpdate(connection);
				connection.commit();

				if (mandatory) {
					fail("Null value was allowed for mandatory column.");
				}
			} catch (SQLException ex) {
				if (!mandatory) {
					fail("Cannot insert null value for non-mandatory column.", ex);
				}
			}

		}

		private CompiledStatement createInsertStatement(DBHelper sqlDialect, DBTable table, DBColumn column,
				SQLExpression columnValue) {
			List<String> columnNames = list(column.getDBName());
			List<SQLExpression> columnValues = list(columnValue);
			SQLInsert insert = insert(tableReference(table), columnNames, columnValues);
			return query(insert).toSql(sqlDialect);
		}
		
		private static DBColumn addColumn(DBTable table, DBType type, boolean binary, boolean mandatory) {
			DBColumn column = DBSchemaFactory.createColumn(mkColumnName(type, binary, mandatory));
			
			column.setType(type);
			if (type.sizeParam) {
				switch (type) {
					case CLOB:
					case BLOB:
						/* Do not limit database column, because CLOB and BLOB are designed for
						 * large data. Moreover determination of a concrete size for CLOB is not
						 * easy because it also depends on the char encoding of the database, for
						 * example. */
						break;
					default:
						column.setSize(7);
				}
			}
			if (type.precisionParam) {
				column.setPrecision(2);
			}
			column.setMandatory(mandatory);
			column.setBinary(binary);
			column.setComment("Column for testing type" + (binary ? " binary" : "") + (mandatory ? " mandatory" : "") + " " + type + ".");
			
			table.getColumns().add(column);
			
			return column;
		}

		private static String mkColumnName(DBType type, boolean binary, boolean mandatory) {
			return "C_" + mkName(type, binary, mandatory);
		}

		static String mkName(DBType type, boolean binary, boolean mandatory) {
			return (type + (binary ? "_BINARY" : "") + (mandatory ? "_MANDATORY" : ""));
		}
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		Test t = DatabaseTestSetup.getDBTest(TestCreateStatementBuilder.class,
			//			test.com.top_logic.basic.DatabaseTestSetup.DBType.ORACLE_DB,
			new TestFactory() {
			
			@Override
			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
				TestSuite suite = new TestSuite(suiteName);
				
				addTests(suite, false, false);
				addTests(suite, true, false);
				addTests(suite, false, true);

				// No static tests.
				//
				// TestSuite staticTests = new TestSuite(TestCreateStatementBuilder.class);
				// suite.addTest(staticTests);
				
				return suite;
			}
		});
		t = ServiceTestSetup.createSetup(t, Settings.Module.INSTANCE);
		return ModuleTestSetup.setupModule(t);
	}

}
