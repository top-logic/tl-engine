/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.security;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import com.top_logic.basic.db.model.DBIndex;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.SchemaExtraction;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.knowledge.security.SecurityStorage.SecurityStorageExecutor;
import com.top_logic.util.db.DBUtil;

/**
 * This is a {@link SecurityStorageExecutor} optimized for <code>Oracle</code> database access.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class SecurityStorageOracleExecutor extends SecurityStorageExecutor {

    /**
	 * Creates a new {@link SecurityStorageOracleExecutor}.
	 *
	 * @param connectionPool
	 *        the prepared statement cache to use for read operations
	 * @throws SQLException
	 *         if the initialization of the storage fails
	 */
    public SecurityStorageOracleExecutor(ConnectionPool connectionPool) throws SQLException {
        super(connectionPool);
    }



    /* The SQL statements used by this class. */
	/**
	 * SQL statement checking whether the {@link SecurityStorage#TABLE_NAME} is not empty.
	 */
	protected static final String ORACLE_IS_NOT_EMPTY_STATEMENT =
		"SELECT /*+ FIRST_ROWS(1) */ 1 FROM " + SecurityStorage.TABLE_NAME + " WHERE ROWNUM <= 1";

	/**
	 * SQL statement to truncate {@link SecurityStorage#TABLE_NAME}.
	 */
    protected static final String ORACLE_CLEAR_STATEMENT = "TRUNCATE TABLE " + SecurityStorage.TABLE_NAME;

    @Override
    public void clearStorage() throws SQLException {
        DBUtil.executeUpdate(getWriteConnection(), ORACLE_CLEAR_STATEMENT);
    }

    @Override
    public Collection<DBIndex> disableIndex() throws SQLException {
        Connection writeConnection = getWriteConnection();
        DBTable table = new SchemaExtraction(writeConnection.getMetaData(), dbHelper).addTable(DBSchemaFactory.createDBSchema(), SecurityStorage.TABLE_NAME);
        Collection<DBIndex> indices = table.getIndices();
        for (DBIndex index : indices) {
            StringBuilder sb = new StringBuilder();
            sb.append("DROP INDEX ").append(dbHelper.columnRef(index.getDBName()));
            DBUtil.executeUpdate(writeConnection, sb.toString());
        }
        return indices;
    }

    @Override
    public boolean isEmpty() throws SQLException {
		return !DBUtil.executeQueryAsBoolean(ORACLE_IS_NOT_EMPTY_STATEMENT);
    }

}
