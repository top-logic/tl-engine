/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.sql;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.sql.DBHelper;

/**
 * {@link DefaultInsertWriter} for ORACLE SQL dialect.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OracleInsertWriter extends DefaultInsertWriter {

	/**
	 * Creates a new {@link OracleInsertWriter}.
	 */
	public OracleInsertWriter(DBHelper sqlDialect, Appendable out, int insertChunkSize) {
		super(sqlDialect, out, insertChunkSize);
	}

	@Override
	public void beginDump() throws IOException {
		super.beginDump();
		dumpTimeStamp();
	}

	@Override
	public void endDump() throws IOException {
		dumpTimeStamp();
		super.endDump();
	}

	private void dumpTimeStamp() throws IOException {
		appendLine("SELECT CURRENT_TIMESTAMP FROM DUAL;");
	}

	/**
	 * Insert of multi values is form:
	 * 
	 * <pre>
	 * INSERT ALL
	 *   INTO t (col1, col2, col3) VALUES ('val1_1', 'val1_2', 'val1_3')
	 *   INTO t (col1, col2, col3) VALUES ('val2_1', 'val2_2', 'val2_3')
	 *   INTO t (col1, col2, col3) VALUES ('val3_1', 'val3_2', 'val3_3')
	 *   .
	 *   .
	 *   .
	 *   SELECT 1 FROM DUAL;
	 * </pre>
	 */
	@Override
	protected void internalAppendInsert(DBTable table, List<Object[]> values) throws IOException {
		if (_insertChunkSize == 1) {
			super.internalAppendInsert(table, values);
		} else {
			appendInsertAll(table, values);
		}
		appendLine("COMMIT;");
	}

	private void appendInsertAll(DBTable table, List<Object[]> values) throws IOException {
		Iterator<Object[]> vals = values.iterator();
		List<DBColumn> columns = table.getColumns();

		while (vals.hasNext()) {
			appendLine("INSERT ALL ");
			int count = 0;
			while (vals.hasNext() && count++ < _insertChunkSize) {
				Object[] value = vals.next();
				_out.append(" INTO ");
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

				_out.append(" VALUES (");
				int columnNumber = 0;
				for (DBColumn column : columns) {
					if (columnNumber > 0) {
						_out.append(", ");
					}
					_sqlDialect.literal(_out, column.getType(), value[columnNumber]);
					columnNumber++;
				}
				_out.append(")");
				_insertedRows++;
				newLine();
			}
			appendLine("SELECT 1 FROM DUAL;");
		}
	}

}

