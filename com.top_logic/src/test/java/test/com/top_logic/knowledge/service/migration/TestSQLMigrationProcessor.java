/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;

import junit.framework.Test;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.SQLMigrationProcessor;
import com.top_logic.knowledge.service.migration.SQLMigrationProcessor.Config;

/**
 * Test of {@link SQLMigrationProcessor}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestSQLMigrationProcessor extends BasicTestCase {

	private ConnectionPool _pool;

	private PooledConnection _writeConnection;

	private DBHelper _sqlDialect;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		_writeConnection = _pool.borrowWriteConnection();
		_sqlDialect = _pool.getSQLDialect();
	}

	@Override
	protected void tearDown() throws Exception {
		_pool.releaseWriteConnection(_writeConnection);
		super.tearDown();
	}

	public void testUpdateTableLF() throws SQLException {
		testUpdateTable("webinf://test/TestSQLMigrationProcessor/testUpdateTable_LF");
	}

	public void testUpdateTableCRLF() throws SQLException {
		testUpdateTable("webinf://test/TestSQLMigrationProcessor/testUpdateTable_CRLF");
	}

	private void testUpdateTable(String prefix) throws SQLException {
		DBSchema schema = DBSchemaFactory.createDBSchema();
		String tableName = "TEST_UPDATE_TABLE";
		DBTable table = DBSchemaFactory.createTable(tableName);
		schema.getTables().add(table);
		DBColumn col1 = DBSchemaFactory.createColumn("col1");
		col1.setType(DBType.STRING);
		col1.setSize(20);
		table.getColumns().add(col1);
		DBSchemaUtils.createTables(_writeConnection, schema, true);
		try {
			String insertValue = BasicTestCase.randomString(50, true, false, false, false);
			CompiledStatement insert = query(insert(table(tableName, "t"),
			list("col1"), list(literalString(insertValue)))).toSql(_sqlDialect);
			Savepoint savepoint = _sqlDialect.setSavepoint(_writeConnection);
			try {
				insert.executeUpdate(_writeConnection);
				fail("Must not be able to insert, as column is to small");
			} catch (SQLException ex) {
				_sqlDialect.rollback(_writeConnection, savepoint);
				// expected
			} finally {
				_sqlDialect.releaseSavepoint(_writeConnection, savepoint);
			}

			Config configItem = TypedConfiguration.newConfigItem(SQLMigrationProcessor.Config.class);
			configItem.setFileNamePrefix(prefix);
			SQLMigrationProcessor processor =
				SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(configItem);
			MigrationContext context = new MigrationContext(new AssertProtocol(), _writeConnection);
			processor.doMigration(context, new AssertProtocol(TestSQLMigrationProcessor.class.getName()), _writeConnection);

			insert.executeUpdate(_writeConnection);

			CompiledStatement select = query(select(list(columnDef(column("t", "col1"), "col1Name")), table(tableName, "t"))).toSql(_sqlDialect);
			try (ResultSet result = select.executeQuery(_writeConnection)) {
				assertTrue(result.next());
				assertEquals(insertValue, result.getString("col1Name"));
				assertFalse(result.next());
			}
		} finally {
			DBSchemaUtils.resetTables(_writeConnection, schema, false);
		}
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestSQLMigrationProcessor}.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(DatabaseTestSetup.getDBTest(TestSQLMigrationProcessor.class));
	}

}

