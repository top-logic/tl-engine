/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.db.model.DBIndex;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.SchemaExtraction;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.knowledge.security.SecurityStorage.SecurityStorageExecutor;
import com.top_logic.util.db.DBUtil;

/**
 * This is a SecurityStorageExecutor optimized for MySQL database access.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class SecurityStorageMySQLExecutor extends SecurityStorageExecutor {

    /**
     * Creates a new SecurityStorageMySQLExecutor.
     *
     * @param connectionPool
     *            the prepared statement cache to use for read operations
     * @throws SQLException
     *             if the initialization of the storage fails
     */
    public SecurityStorageMySQLExecutor(ConnectionPool connectionPool) throws SQLException {
        super(connectionPool);
    }



    /* The SQL statements used by this class. */
	/**
	 * SQL statement to insert rows into {@link SecurityStorage#TABLE_NAME} ignoring duplicates.
	 * 
	 * @see #MYSQL_INSERT_STATEMENT_PARAMETERS
	 */
    protected static final String MYSQL_INSERT_STATEMENT = "INSERT IGNORE INTO " + SecurityStorage.TABLE_NAME + " VALUES (?, ?, ?, ?)";

	/**
	 * Number of parameters in {@link #MYSQL_INSERT_STATEMENT}.
	 * 
	 * @see #MYSQL_INSERT_STATEMENT
	 */
	protected static final int MYSQL_INSERT_STATEMENT_PARAMETERS = 4;
    protected static final String MYSQL_IS_EMPTY_STATEMENT = "SELECT 1 FROM " + SecurityStorage.TABLE_NAME + " LIMIT 1";
    protected static final String MYSQL_CLEAR_STATEMENT = "TRUNCATE TABLE " + SecurityStorage.TABLE_NAME;
    protected static final String MYSQL_CREATE_DEFAULT_INDEX = "ALTER TABLE " + SecurityStorage.TABLE_NAME + " ADD INDEX " + SecurityStorage.DEFAULT_INDEX_NAME +
                                                               " (" + SecurityStorage.ATTRIBUTE_BUSINESS_OBJECT + "," + SecurityStorage.ATTRIBUTE_ROLE + "," + SecurityStorage.ATTRIBUTE_REASON + ")";

    @Override
	public boolean insert(Object[] aVector) throws SQLException {
        checkVectorNotNull(aVector);
        return DBUtil.executeUpdate(getWriteConnection(), MYSQL_INSERT_STATEMENT, aVector) > 0;
    }

    @Override
    public boolean isEmpty() throws SQLException {
        return !DBUtil.executeQueryAsBoolean(MYSQL_IS_EMPTY_STATEMENT);
    }

    @Override
    public void clearStorage() throws SQLException {
        DBUtil.executeUpdate(getWriteConnection(), MYSQL_CLEAR_STATEMENT);
    }

    @Override
	public int multiInsertIgnore(List<Object[]> vectors) throws SQLException {
        Connection writeConnection = getWriteCache().getConnection();
        PreparedStatement pstm = writeConnection.prepareStatement(MYSQL_INSERT_STATEMENT);
		int maxBatchSize = dbHelper.getMaxBatchSize(MYSQL_INSERT_STATEMENT_PARAMETERS);
        try {
            int result = 0, counter = 0;
			for (Object[] vector : vectors) {
                if (vector == null) continue;
                checkVectorNotNull(vector);
                DBUtil.setVector(pstm, vector);
                pstm.addBatch();
                counter++;
				if (counter >= maxBatchSize) {
                    result += ArrayUtil.sum(pstm.executeBatch());
                    counter = 0;
                }
            }
            if (counter > 0) {
                result += ArrayUtil.sum(pstm.executeBatch());
            }
            return result;
        } finally {
            pstm.close();
        }
    }

    @Override
    public Collection<DBIndex> disableIndex() throws SQLException {
        Connection writeConnection = getWriteConnection();
        DBTable table = new SchemaExtraction(writeConnection.getMetaData(), dbHelper).addTable(DBSchemaFactory.createDBSchema(), SecurityStorage.TABLE_NAME);
        Collection<DBIndex> indices = table.getIndices();
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ").append(SecurityStorage.TABLE_NAME).append(' ');
        boolean firstDone = false;
        for (DBIndex index : indices) {
            if (firstDone) sb.append(", ");
            else firstDone = true;
            sb.append("DROP INDEX ").append(dbHelper.columnRef(index.getDBName()));
        }

		// IGNORE FindBugs(SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING): Dynamic
		// SQL construction is necessary. No user-input is passed to the statement source.
        DBUtil.executeUpdate(writeConnection, sb.toString());
        return indices;
    }

    @Override
    public void enableIndex() throws SQLException {
        DBUtil.executeUpdate(getWriteConnection(), MYSQL_CREATE_DEFAULT_INDEX);
    }

}
