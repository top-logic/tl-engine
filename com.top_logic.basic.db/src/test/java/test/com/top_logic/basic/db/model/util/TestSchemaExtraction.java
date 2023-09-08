/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.model.util;

import static com.top_logic.basic.db.model.DBSchemaFactory.*;

import java.sql.SQLException;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBIndex;
import com.top_logic.basic.db.model.DBPrimary;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.model.util.SchemaExtraction;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBType;

/**
 * Test case for {@link SchemaExtraction}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestSchemaExtraction extends BasicTestCase {

	/**
	 * Test extracting table with primary key.
	 */
	public void testPrimaryKey() throws SQLException {
		DBSchema schema = DBSchemaFactory.createDBSchema();
		DBTable table = DBSchemaFactory.createTable("TestPrimaryKey");
		
		DBColumn c1 = DBSchemaFactory.createColumn("c1");
		c1.setType(DBType.INT);
		c1.setSize(100);
		c1.setMandatory(true);
		table.getColumns().add(c1);
		
		DBColumn c2 = DBSchemaFactory.createColumn("c2");
		c2.setType(DBType.INT);
		c2.setMandatory(true);
		table.getColumns().add(c2);
		
		DBColumn c3 = DBSchemaFactory.createColumn("c3");
		c3.setType(DBType.INT);
		c3.setMandatory(false);
		table.getColumns().add(c3);
		
		DBPrimary primary = DBSchemaFactory.createPrimary();
		primary.getColumnRefs().add(ref(c1));
		primary.getColumnRefs().add(ref(c2));
		table.setPrimaryKey(primary);
		
		schema.getTables().add(table);
		
		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		DBSchemaUtils.recreateTables(pool, schema);
		
		DBTable extractedTable = DBSchemaUtils.extractTable(pool, DBSchemaFactory.createDBSchema(), table.getDBName());
		
		DBIndex extractedPrimaryKey = extractedTable.getPrimaryKey();
		assertNotNull(extractedPrimaryKey);
		assertEquals(2, extractedPrimaryKey.getColumnRefs().size());
		try {
			assertEquals("c1", extractedPrimaryKey.getColumnRefs().get(0).getName());
			assertEquals("c2", extractedPrimaryKey.getColumnRefs().get(1).getName());
		} catch (AssertionFailedError err) {
			fail("Ticket #8971: DB2 always returns all upper case column names.", err);
		}
	}
	
	/**
	 * Test extracting table with out primary key.
	 */
	public void testNoPrimaryKey() throws SQLException {
		DBSchema schema = DBSchemaFactory.createDBSchema();
		DBTable table = DBSchemaFactory.createTable("TestPrimaryKey");
		
		DBColumn c1 = DBSchemaFactory.createColumn("c1");
		c1.setType(DBType.INT);
		c1.setMandatory(true);
		table.getColumns().add(c1);
		
		DBColumn c2 = DBSchemaFactory.createColumn("c2");
		c2.setType(DBType.INT);
		c2.setMandatory(true);
		table.getColumns().add(c2);
		
		DBColumn c3 = DBSchemaFactory.createColumn("c3");
		c3.setType(DBType.INT);
		c3.setMandatory(false);
		table.getColumns().add(c3);
		
		schema.getTables().add(table);
		
		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		DBSchemaUtils.recreateTables(pool, schema);
		
		DBTable extractedTable = DBSchemaUtils.extractTable(pool, DBSchemaFactory.createDBSchema(), table.getDBName());
		
		DBIndex extractedPrimaryKey = extractedTable.getPrimaryKey();
		assertNull("Ticket #2792: Got primary key for table without primary key.", extractedPrimaryKey);
	}
	
	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(DatabaseTestSetup.getDBTest(TestSchemaExtraction.class));
	}
	
}
