/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.knowledge.security.SecurityStorage.SecurityStorageExecutor;
import com.top_logic.util.db.DBUtil;

/**
 * This is a {@link SecurityStorageExecutor} optimized for <code>PostgreSQL</code> database access.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SecurityStoragePostgreSQLExecutor extends SecurityStorageExecutor {

	private final String _insertIgnore;

	private final int _insertIgnoreParameters;

	/**
	 * Creates a new {@link SecurityStoragePostgreSQLExecutor}.
	 *
	 * @param connectionPool
	 *        the prepared statement cache to use for read operations
	 * @throws SQLException
	 *         if the initialization of the storage fails
	 */
    public SecurityStoragePostgreSQLExecutor(ConnectionPool connectionPool) throws SQLException {
        super(connectionPool);
		_insertIgnore = "INSERT INTO " + dbHelper.tableRef(SecurityStorage.TABLE_NAME)
				+ " VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING";
		_insertIgnoreParameters = 4;
    }

    @Override
	public boolean insertIgnore(Object[] aVector) throws SQLException {
        checkVectorNotNull(aVector);
		Connection writeConnection = getWriteConnection();
		Object[] storageValues = storageValues(aVector);
		return DBUtil.executeUpdate(writeConnection, _insertIgnore, storageValues) > 0;
    }

    @Override
	public int multiInsertIgnore(List<Object[]> vectors) throws SQLException {
        Connection writeConnection = getWriteCache().getConnection();
		int maxBatchSize = dbHelper.getMaxBatchSize(_insertIgnoreParameters);
		try (PreparedStatement pstm = writeConnection.prepareStatement(_insertIgnore)) {
            int result = 0, counter = 0;
			for (Object[] vector : vectors) {
                if (vector == null) continue;
                checkVectorNotNull(vector);
				Object[] storageValues = storageValues(vector);
				DBUtil.setVector(pstm, storageValues);
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
        }
    }

}
