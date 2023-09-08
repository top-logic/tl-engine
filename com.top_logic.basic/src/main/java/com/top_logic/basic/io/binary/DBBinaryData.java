/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;

/**
 * Re-creates some {@link BinaryData} for a static SQL-statement.
 * 
 * @author <a href="mailto:klaus@top-logic.com">klaus</a>
 */
public abstract class DBBinaryData extends BinaryDataProxy {
    
    /** The SQL-Statement is (re-)execute against this pool */
    private final ConnectionPool foundIn;

	/**
	 * Create a {@link DBBinaryData} with the expected size of the re-fetched data.
	 */
    public DBBinaryData(long aSize, ConnectionPool aPool) {
        super(aSize);
        this.foundIn = aPool;
    }

    /**
	 * Create a {@link DBBinaryData} for some existing original which may vanish, later.
	 */
    public DBBinaryData(BinaryData original, ConnectionPool aPool) {
        super(original);
        this.foundIn = aPool;
    }

    /**
	 * Re-create the {@link BinaryData} from SQL.
	 */
    @Override
    protected BinaryData createBinaryData() throws IOException {
    	PooledConnection con = foundIn.borrowReadConnection();
        try {
			return refetch(con);
        } catch (SQLException sqx) {
            throw (IOException) new IOException("Failed to create BinaryData").initCause(sqx);
        } finally {
            foundIn.releaseReadConnection(con);
        }
    }

	/**
	 * Reads a {@link BinaryData} from a BLOB column of the given {@link ResultSet}.
	 * 
	 * @param sqlDialect
	 *        Helper for specific JDBC driver.
	 * @param res
	 *        The database result to read data from.
	 * @param name
	 *        Name of the resulting data.
	 * @param sizeIndex
	 *        The index of the LONG column containing the size of the data. As in SQL standard,
	 *        1-based.
	 * @param blobIndex
	 *        The index of the BLOB column which holds the data. As in SQL standard, 1 based.
	 * 
	 * @return A {@link BinaryData} object containing the data from the {@link ResultSet}. May be
	 *         <code>null</code>.
	 * @throws SQLException
	 *         When accessing the {@link ResultSet} fails.
	 */
	public static BinaryData fromBlobColumn(DBHelper sqlDialect, ResultSet res, String name, int contentTypeIndex,
			int sizeIndex, int blobIndex) throws SQLException {
		name = noEmptyName(name);
		long size = res.getLong(sizeIndex);
		String contentType = res.getString(contentTypeIndex);

		try {
			// Fetch the stream *last*, because the stream is automatically
			// closed when a different getter is called in the ResultSet.
			try (InputStream data = sqlDialect.getBinaryStream(res, blobIndex)) {
				if (data == null) {
					return null;
				}
				return BinaryDataFactory.createBinaryData(data, size, contentType, name);
			}
		} catch (IOException ex) {
			throw new SQLException("Reading BLOB attribute failed.", ex);
		}
	}

	/**
	 * Ensures that the name for a {@link BinaryData} is not empty.
	 * 
	 * @param name
	 *        A potential name for a {@link BinaryData}.
	 * 
	 * @return The given name if not empty, {@link BinaryData#NO_NAME} otherwise.
	 */
	public static String noEmptyName(String name) {
		if (StringServices.isEmpty(name)) {
			name = BinaryData.NO_NAME;
		}
		return name;
	}

	/**
	 * Re-fetched the {@link BinaryData} from the given connection.
	 * 
	 * @param connection
	 *        Access to database.
	 * @return {@link BinaryData} to use as implementation.
	 */
	protected abstract BinaryData refetch(PooledConnection connection) throws SQLException;

}
