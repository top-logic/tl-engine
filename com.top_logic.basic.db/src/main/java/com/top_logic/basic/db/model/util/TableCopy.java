/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.sql.BatchController;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;


/**
 * Helper for copying a whole table from one database to another database.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableCopy {
	private final DBHelper fromDialect;

	private final PooledConnection fromConnection;

	private final DBHelper toDialect;

	private final PooledConnection toConnection;

	private final int batchSize;

	/**
	 * Creates a {@link TableCopy}.
	 * 
	 * @param fromDialect
	 *        The dialect of the source database.
	 * @param fromConnection
	 *        A connection to the source database.
	 * @param toDialect
	 *        The dialect of the destination database.
	 * @param toConnection
	 *        A connection to the destination database.
	 * @param batchSize
	 *        The batch size for the copy operation.
	 */
	public TableCopy(DBHelper fromDialect,
			PooledConnection fromConnection, DBHelper toDialect,
			PooledConnection toConnection, int batchSize) {
		this.fromDialect = fromDialect;
		this.fromConnection = fromConnection;
		this.toDialect = toDialect;
		this.toConnection = toConnection;
		this.batchSize = batchSize;
	}

	/**
	 * Performs the actual copy operation.
	 * 
	 * <p>
	 * Note: This operation does not yet send a commit on the destination
	 * connection.
	 * </p>
	 * 
	 * @param table
	 *        The table to copy.
	 * @return The number of rows that have been copied.
	 * 
	 * @throws SQLException
	 *         If copying fails.
	 */
	public long copyTable(DBTable table) throws SQLException {
		StringBuffer fromColumns = new StringBuffer();
		StringBuffer toColumns = new StringBuffer();
		StringBuffer values = new StringBuffer();
		List<DBType> types = new ArrayList<>();
		
		boolean first = true;
		for (DBColumn column : table.getColumns()) {
			if (first) {
				first = false;
			} else {
				fromColumns.append(", ");
				toColumns.append(", ");
				values.append(", ");
			}
			
			String columnName = column.getDBName();
			DBType sqlType = column.getType();
			types.add(sqlType);
			
			fromColumns.append(fromDialect.columnRef(columnName));
			toColumns.append(toDialect.columnRef(columnName));
			values.append('?');
		}
		
		// Copy data.
		String selectDataSql = 
			"SELECT "
			+	fromColumns
			+ " FROM "
			+	fromDialect.tableRef(table.getDBName());
		
		String insertDataSql = 
			"INSERT INTO "
			+	toDialect.tableRef(table.getDBName())
			+ " ("
			+	toColumns
			+ ") VALUES ("
			+	values
			+ ")";
		
		PreparedStatement readData = fromConnection.prepareStatement(selectDataSql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		try {
			fromDialect.setFetchSize(readData, batchSize);
			
			PreparedStatement writeData = toConnection.prepareStatement(insertDataSql);
			try {
				ResultSet data = readData.executeQuery();
				try {
					BatchController batchControl = new BatchController(writeData, batchSize);
					while (data.next()) {
						// Transfer data.
						for (int n = 0, cnt = types.size(); n < cnt; n++) {
							DBType sqlType = types.get(n);
							int index = 1 + n;
							
							Object value = fromDialect.mapToJava(data, index, sqlType);
							
							toDialect.setFromJava(writeData, value, index, sqlType);
						}
						batchControl.addBatch();
					}
					batchControl.finishBatch();

					return batchControl.getDataRows();
				} finally {
					data.close();
				}
			} finally {
				writeData.close();
			}
		} finally {
			readData.close();
		}
	}
}