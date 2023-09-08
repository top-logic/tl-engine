/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.model.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.ResultSetProxy;

/**
 * Reads all rows of a table in chunks using primary key order and limit expressions.
 * 
 * <p>
 * Note: This class is in the test hierachy, because it only works with MySQL and has not benefit
 * over cursor read. See {@link BenchChunkedRead}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ChunkedRead {

	private final DBHelper _sqlDialect;

	private final Connection _readConnection;

	private String _selectFirstSql;

	private String _selectNextSql;

	protected int[] _primaryKeyJdbcIdx;

	public ChunkedRead(DBHelper sqlDialect, Connection readConnection, DBTable table, int chunkSize)
			throws SQLException {
		_sqlDialect = sqlDialect;
		_readConnection = readConnection;

		init(table, chunkSize);
	}

	private void init(DBTable table, int chunkSize) {
		List<DBColumn> columns = table.getColumns();

		StringBuffer columnsExpr = getColumnsExpr(columns);
		List<? extends DBColumn> primaryKeyColumns = table.getPrimaryKey().getColumns();
		int primaryKeySize = primaryKeyColumns.size();
		StringBuffer primaryKeyColumnsExpr = getColumnsExpr(primaryKeyColumns);
		StringBuffer primaryKeyParamsExpr = getParamsExpr(primaryKeySize);

		StringBuffer chunkStart = new StringBuffer();
		{
			chunkStart.append('(');
			chunkStart.append(primaryKeyColumnsExpr);
			chunkStart.append(')');
			chunkStart.append(" > ");
			chunkStart.append('(');
			chunkStart.append(primaryKeyParamsExpr);
			chunkStart.append(')');
		}

		_primaryKeyJdbcIdx = new int[primaryKeySize];
		for (int n = 0, cnt = primaryKeySize; n < cnt; n++) {
			_primaryKeyJdbcIdx[n] = 1 + columns.indexOf(primaryKeyColumns.get(n));
		}

		String tableName = table.getDBName();
		_selectFirstSql = createSelect(tableName, columnsExpr, primaryKeyColumnsExpr, "1=1", chunkSize);
		_selectNextSql = createSelect(tableName, columnsExpr, primaryKeyColumnsExpr, chunkStart, chunkSize);
	}

	public ResultSet executeQuery() throws SQLException {
		final PreparedStatement selectFirst = _readConnection.prepareStatement(_selectFirstSql);
		final PreparedStatement selectNext = _readConnection.prepareStatement(_selectNextSql);

		final ResultSet firstResult = selectFirst.executeQuery();

		return new ResultSetProxy() {
			private boolean _rowReturned = false;

			private Object[] _lastRowId = new Object[_primaryKeyJdbcIdx.length];
			private ResultSet _current = firstResult;

			@Override
			public boolean next() throws SQLException {
				final boolean currentHasNext = super.next();
				if (currentHasNext) {
					for (int n = 0, cnt = _lastRowId.length; n < cnt; n++) {
						_lastRowId[n] = getObject(_primaryKeyJdbcIdx[n]);
					}
					_rowReturned = true;
					return true;
				} else {
					if (_rowReturned) {
						// Retry next chunk.
						_current.close();

						for (int n = 0, cnt = _lastRowId.length; n < cnt; n++) {
							selectNext.setObject(1 + n, _lastRowId[n]);
						}
						_current = selectNext.executeQuery();
						_rowReturned = false;

						return next();
					} else {
						return false;
					}
				}
			}

			@Override
			protected final ResultSet impl() {
				return _current;
			}

			@Override
			public void close() throws SQLException {
				try {
					_current.close();
				} finally {
					try {
						selectFirst.close();
					} finally {
						selectNext.close();
					}
				}
			}
		};
	}

	private String createSelect(String tableName, CharSequence columnsExpr, CharSequence orderByExpr,
			CharSequence whereExpr, int chunkSize) {
		StringBuilder sql = new StringBuilder();
		_sqlDialect.limitStart(sql, chunkSize);
		sql.append("SELECT ");
		_sqlDialect.limitColumns(sql, chunkSize);
		sql.append(columnsExpr);
		sql.append(" FROM ");
		sql.append(_sqlDialect.tableRef(tableName));
		sql.append(" WHERE ");
		sql.append(whereExpr);
		sql.append(" ORDER BY ");
		sql.append(orderByExpr);
		_sqlDialect.limitLast(sql, chunkSize);

		return sql.toString();
	}

	private StringBuffer getParamsExpr(int paramCnt) {
		StringBuffer paramsExpr = new StringBuffer();
		boolean first = true;
		for (int n = 0, cnt = paramCnt; n < cnt; n++) {
			if (first) {
				first = false;
			} else {
				paramsExpr.append(", ");
			}

			paramsExpr.append('?');
		}
		return paramsExpr;
	}

	private StringBuffer getColumnsExpr(List<? extends DBColumn> columns) {
		StringBuffer columnsExpr = new StringBuffer();
		boolean first = true;
		for (DBColumn column : columns) {
			if (first) {
				first = false;
			} else {
				columnsExpr.append(", ");
			}

			columnsExpr.append(_sqlDialect.columnRef(column.getDBName()));
		}
		return columnsExpr;
	}

}
