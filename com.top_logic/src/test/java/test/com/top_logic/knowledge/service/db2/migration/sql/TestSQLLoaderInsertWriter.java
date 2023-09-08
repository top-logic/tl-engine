/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.migration.sql;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.basic.sql.AbstractConnectionTest;
import test.com.top_logic.basic.sql.ConnectionSetup;

import com.top_logic.basic.Settings;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.knowledge.service.db2.migration.sql.InsertWriter;
import com.top_logic.knowledge.service.db2.migration.sql.SQLLoaderInsertWriter;

/**
 * Test for the {@link SQLLoaderInsertWriter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestSQLLoaderInsertWriter extends AbstractConnectionTest {

	private DBTable _table;

	private File _outDir;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_table = createTable();
		_outDir = new File(Settings.getInstance().getTempDir(), getClass().getSimpleName());
		assertTrue(_outDir.mkdir());
	}

	@Override
	protected void tearDown() throws Exception {
		assertTrue(FileUtilities.deleteR(_outDir));
		super.tearDown();
	}

	private DBTable createTable() {
		DBTable table = DBSchemaFactory.createTable(getClass().getSimpleName());
		table.getColumns().add(doubleColumn());
		return table;

	}

	private DBColumn doubleColumn() {
		DBColumn col = DBSchemaFactory.createColumn("double");
		col.setType(com.top_logic.basic.sql.DBType.DOUBLE);
		return col;
	}

	public void testDouble() throws IOException {
		try (InsertWriter insertWriter = new SQLLoaderInsertWriter(getSQLDialect(), _outDir)) {
			insertWriter.appendInsert(_table, testValues());
		}
	}

	private List<Object[]> testValues() {
		return Arrays.asList(
			new Object[] { null },
			new Object[] { -3d },
			new Object[] { -3.0d },
			new Object[] { 1d },
			new Object[] { -0.0d },
			new Object[] { -0.3d },
			new Object[] { 9.4309873650847083E12d },
			new Object[] { 9430987365084.7083d },
			new Object[] { 9.4309873650847083d },
			new Object[] { 94309873650847083d },
			new Object[] { Double.MIN_NORMAL },
			new Object[] { Double.MAX_VALUE });
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(ConnectionSetup.createSuite(TestSQLLoaderInsertWriter.class,
			DBType.ORACLE_DB, DefaultTestFactory.INSTANCE));
	}
}

