/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.security;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.Logger;
import com.top_logic.basic.db.model.DBIndex;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.SchemaExtraction;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.security.SecurityStorage.SecurityStorageExecutor;
import com.top_logic.util.db.DBUtil;

/**
 * {@link SecurityStorageDB2Executor} is a {@link SecurityStorageExecutor} which cares about special
 * <code>DB2</code> handling.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SecurityStorageDB2Executor extends SecurityStorageExecutor {

	private static final String SQL_DROP_DEFAULT_INDEX = "DROP INDEX " + SecurityStorage.DEFAULT_INDEX_NAME;

	/**
	 * Creates a {@link SecurityStorageDB2Executor}.
	 * 
	 * @param connectionPool
	 *        the {@link ConnectionPool} to use for operations
	 * @throws SQLException
	 *         if the initialization of the storage fails
	 */
	public SecurityStorageDB2Executor(ConnectionPool connectionPool) throws SQLException {
		super(connectionPool);
	}

	private static DBIndex getDefaultIndex(ConnectionPool connectionPool) throws SQLException {
		return getTable(connectionPool).getIndex(SecurityStorage.DEFAULT_INDEX_NAME);
	}

	private static DBTable getTable(ConnectionPool connectionPool) throws SQLException {
		PooledConnection readConnection = connectionPool.borrowReadConnection();
		try {
			DBHelper sqlDialect = connectionPool.getSQLDialect();
			SchemaExtraction schemaExtraction = new SchemaExtraction(readConnection.getMetaData(), sqlDialect);
			return schemaExtraction.addTable(DBSchemaFactory.createDBSchema(), SecurityStorage.TABLE_NAME);
		} finally {
			connectionPool.releaseReadConnection(readConnection);
		}
	}

	@Override
	public Collection<DBIndex> disableIndex() throws SQLException {
		DBIndex defaultIndex = getDefaultIndex(this);
		if (defaultIndex != null) {
			DBUtil.executeUpdate(getWriteConnection(), SQL_DROP_DEFAULT_INDEX);
			return Collections.singletonList(defaultIndex);
		} else {
			Logger.warn("Unable to drop index '" + SecurityStorage.DEFAULT_INDEX_NAME + "'. No such index given.",
				SecurityStorageDB2Executor.class);
			return Collections.emptyList();
		}
	}

	@Override
	public void enableIndex() throws SQLException {
		DBHelper sqlDialect = getSQLDialect();

		final String createDefaultIndexSQLStatement = "CREATE INDEX " + SecurityStorage.DEFAULT_INDEX_NAME
			+ " ON " + sqlDialect.tableRef(SecurityStorage.TABLE_NAME) + " ("
			+ sqlDialect.columnRef(SecurityStorage.ATTRIBUTE_BUSINESS_OBJECT) + ","
			+ sqlDialect.columnRef(SecurityStorage.ATTRIBUTE_ROLE) + ","
			+ sqlDialect.columnRef(SecurityStorage.ATTRIBUTE_REASON) + ")";

		// IGNORE FindBugs(SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING):
		// Dynamic SQL construction is necessary for DBMS abstraction. No user-input is
		// passed to the statement source.
		DBUtil.executeUpdate(getWriteConnection(), createDefaultIndexSQLStatement);
	}

}

