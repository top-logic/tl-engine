/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.sql;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBPrimary;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;

/**
 * {@link InsertWriter} for most SQL dialects.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultInsertWriter extends AbstractInsertWriter {

	private int _sqlRow = 1;

	/** Total number of inserted rows. Is dumped as comment at the end of the script. */
	protected long _insertedRows = 0;

	/** Maximal number of rows to insert within one insert statement. */
	protected final int _insertChunkSize;

	/**
	 * Creates a new {@link DefaultInsertWriter}.
	 * 
	 * @param sqlDialect
	 *        The concrete representation of the database to create SQL for.
	 * @param out
	 *        The output to append SQL commands to.
	 * @param insertChunkSize
	 *        Maximal number of rows to insert within one insert statement.
	 */
	public DefaultInsertWriter(DBHelper sqlDialect, Appendable out, int insertChunkSize) {
		super(sqlDialect, out);
		_insertChunkSize = insertChunkSize;
	}

	@Override
	public void endDump() throws IOException {
		appendLineComment("Totally inserted rows " + _insertedRows);
	}

	@Override
	public final void appendInsert(DBTable table, List<Object[]> values) throws IOException {
		appendSQLNumber();

		sortByPrimaryKey(table, values);
		internalAppendInsert(table, values);
	}

	private void appendSQLNumber() throws IOException {
		newLine();
		appendLineComment("Next is SQL command number " + _sqlRow);
		_sqlRow++;
	}

	/**
	 * Actually inserts the given values.
	 * 
	 * <p>
	 * Insert of multi values is form:
	 * 
	 * <pre>
	 * INSERT INTO t (col1, col2, col3) VALUES 
	 *   ('val1_1', 'val1_2', 'val1_3'),
	 *   ('val2_1', 'val2_2', 'val2_3'),
	 *   ('val3_1', 'val3_2', 'val3_3'),
	 *   .
	 *   .
	 *   .
	 * </pre>
	 * </p>
	 * 
	 * @see #appendInsert(DBTable, List)
	 */
	protected void internalAppendInsert(DBTable table, List<Object[]> values) throws IOException {
		List<DBColumn> columns = table.getColumns();
		Iterator<Object[]> vals = values.iterator();

		while (vals.hasNext()) {
			_out.append("INSERT INTO ");
			_out.append(_sqlDialect.tableRef(table.getDBName()));
			_out.append(" (");
			boolean firstColumn = true;
			for (DBColumn column : columns) {
				if (firstColumn) {
					firstColumn = false;
				} else {
					_out.append(", ");
				}
				_out.append(_sqlDialect.columnRef(column.getDBName()));

			}
			_out.append(")");

			_out.append(" VALUES ");

			int count = 0;
			do {
				newLine();
				Object[] value = vals.next();
				if (count > 0) {
					_out.append(',');
				}
				_out.append("(");

				int columnNumber = 0;
				for (DBColumn column : columns) {
					if (columnNumber > 0) {
						_out.append(",");
					}
					appendLiteral(column.getType(), value[columnNumber]);
					columnNumber++;
				}
				_out.append(")");
				_insertedRows++;
				count++;
				if (count >= _insertChunkSize) {
					break;
				}
			} while (vals.hasNext());

			_out.append(';');
			newLine();
		}
	}

	/**
	 * Appends a literal value of the given type to the output.
	 */
	protected void appendLiteral(DBType type, Object literal) {
		_sqlDialect.literal(_out, type, literal);
	}

	/**
	 * Sorts the given values in "primary key order".
	 */
	protected void sortByPrimaryKey(DBTable table, List<Object[]> values) {
		DBPrimary primaryKey = table.getPrimaryKey();
		if (primaryKey == null) {
			return;
		}
		List<? extends DBColumn> columns = primaryKey.getColumns();
		List<DBColumn> allColumns = table.getColumns();
		int[] indexColIndexes = new int[columns.size()];
		for (int i = 0; i < indexColIndexes.length; i++) {
			int colIndex = allColumns.indexOf(columns.get(i));
			assert colIndex >= 0;
			indexColIndexes[i] = colIndex;
		}
		Collections.sort(values, new Comparator<Object[]>() {

			@Override
			public int compare(Object[] o1, Object[] o2) {
				for (int index : indexColIndexes) {
					@SuppressWarnings({ "rawtypes", "unchecked" })
					Comparable<Object> comparable = (Comparable) o1[index];
					int compRes = comparable.compareTo(o2[index]);
					switch (compRes) {
						case 0:
							continue;
						default:
							return compRes;
					}
				}
				return 0;
			}

		});
	}

}

